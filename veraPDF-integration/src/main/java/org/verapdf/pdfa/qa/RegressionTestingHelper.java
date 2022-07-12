package org.verapdf.pdfa.qa;

import org.verapdf.core.VeraPDFException;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validation.validators.BaseValidator;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.policy.PolicyChecker;
import org.verapdf.processor.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RegressionTestingHelper {
    private static final String testFilesZipUrl = "https://github.com/veraPDF/veraPDF-regression-tests/archive/refs/heads/integration.zip";

    private final ZipFile zipSource;
    private final Map<String, ZipEntry> pdfMap;
    private final Map<String, ZipEntry> schMap;

    public RegressionTestingHelper(boolean isWcag) throws IOException {
        VeraGreenfieldFoundryProvider.initialise();
        File zipFile;
        try {
            zipFile = AbstractTestCorpus.createTempFileFromCorpus(new URL(testFilesZipUrl), "regression");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.pdfMap = new HashMap<>();
        this.schMap = new HashMap<>();
        this.itemsMapFromZipSource(zipFile, isWcag);
        this.zipSource = new ZipFile(zipFile);
    }

    public Set<String> getPdfFileNames() {
        return pdfMap.keySet();
    }

    public List<String> getFailedPolicyComplianceFiles(PDFAFlavour flavour, ValidationProfile customProfile, Set<String> fileNames) throws JAXBException, IOException {
        List<String> failedFiles = new ArrayList<>();

        MetadataFixerConfig fixConf = FixerFactory.configFromValues("test", true);
        ProcessorConfig processorConfig = customProfile == null ?
                ProcessorFactory.fromValues(ValidatorFactory.createConfig(flavour, PDFAFlavour.NO_FLAVOUR,
                        true, 0, false, true, Level.WARNING,
                        BaseValidator.DEFAULT_MAX_NUMBER_OF_DISPLAYED_FAILED_CHECKS, false),
                        null, null, fixConf, EnumSet.of(TaskType.VALIDATE), (String) null) :
                ProcessorFactory.fromValues(ValidatorFactory.createConfig(PDFAFlavour.NO_FLAVOUR, PDFAFlavour.NO_FLAVOUR,
                        true, 0, false, true, Level.WARNING,
                        BaseValidator.DEFAULT_MAX_NUMBER_OF_DISPLAYED_FAILED_CHECKS, false),
                        null, null, fixConf, EnumSet.of(TaskType.VALIDATE), customProfile, null);
        BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig);

        File tempSchFile = File.createTempFile("veraPDF", ".sch");
        File tempPdfFile = File.createTempFile("veraPDF", ".pdf");
        File tempMrrFile = File.createTempFile("veraPDF", ".mrr");
        File tempResultFile = File.createTempFile("veraPDF", ".xml");
        List<File> files = new ArrayList<>();
        files.add(tempPdfFile);

        for (String pdfName : fileNames) {
            try (OutputStream reportStream = new FileOutputStream(tempMrrFile)) {
                copyInputStreamToFile(this.getPdfStream(pdfName), tempPdfFile);
                processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, false, reportStream, false));
                reportStream.flush();
            } catch (IOException | VeraPDFException e) {
                failedFiles.add(pdfName);
            }
            try {
                String schName = pdfName.substring(0, pdfName.length() - 3) + "sch";
                copyInputStreamToFile(this.getSchStream(schName), tempSchFile);
                applyPolicy(tempSchFile, tempMrrFile, tempResultFile);
                int failedPolicyJobsCount = countFailedPolicyJobs(tempResultFile);
                if (failedPolicyJobsCount > 0) {
                    failedFiles.add(pdfName);
                }
            } catch (Exception e) {
                failedFiles.add(pdfName);
                e.printStackTrace();
            }
        }

        tempSchFile.deleteOnExit();
        tempPdfFile.deleteOnExit();
        tempMrrFile.deleteOnExit();
        tempResultFile.deleteOnExit();

        return failedFiles;
    }

    public static int totalFailedPolicyJobsCount(List<String> failedFiles) {
        if (failedFiles.size() > 0) {
            System.out.println("Some files is not compliant with policy: ");
            failedFiles.forEach(System.out::println);
        } else {
            System.out.println("Files are compliant with policies");
        }
        return failedFiles.size();
    }

    private void itemsMapFromZipSource(File zipFile, boolean isWcag) throws IOException {
        try (ZipFile zipSource = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zipSource.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if ((isWcag == entryName.contains("WCAG-21")) && !entry.isDirectory()) {
                    if (entryName.endsWith(".pdf")) {
                        this.pdfMap.put(entryName, entry);
                    } else if (entryName.endsWith(".sch")) {
                        this.schMap.put(entryName, entry);
                    }
                }
            }
        }
    }

    private InputStream getPdfStream(String pdfName) throws IOException {
        return this.getStreamFromReference(this.pdfMap.get(pdfName));
    }

    private InputStream getSchStream(String schName) throws IOException {
        return this.getStreamFromReference(this.schMap.get(schName));
    }

    private InputStream getStreamFromReference(ZipEntry reference) throws IOException {
        return this.zipSource.getInputStream(reference);
    }

    public static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            byte[] bytes = new byte[2048];
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    public static int countFailedPolicyJobs(File xmlReport) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(xmlReport);
        XPath path = XPathFactory.newInstance().newXPath();
        return ((Number) path.evaluate("count(//policyReport[@failedChecks > 0])", document, XPathConstants.NUMBER)).intValue();
    }

    public static void applyPolicy(File policyFile, File tempMrrFile, File tempResultFile) throws IOException, VeraPDFException {
        File tempPolicyResult = File.createTempFile("policyResult", "veraPDF");
        try (InputStream mrrIs = new FileInputStream(tempMrrFile);
             OutputStream policyResultOs = new FileOutputStream(tempPolicyResult);
             OutputStream mrrReport = new FileOutputStream(tempResultFile)) {
            PolicyChecker.applyPolicy(policyFile, mrrIs, policyResultOs);
            PolicyChecker.insertPolicyReport(tempPolicyResult, tempMrrFile, mrrReport);
        }

        if (!tempPolicyResult.delete()) {
            tempPolicyResult.deleteOnExit();
        }
    }
}
