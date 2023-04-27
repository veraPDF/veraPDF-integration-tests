package org.verapdf.pdfa.qa;

import org.verapdf.core.VeraPDFException;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validation.validators.ValidatorConfigBuilder;
import org.verapdf.policy.PolicyChecker;
import org.verapdf.processor.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private static final String testFilesZipUrl = "https://github.com/veraPDF/veraPDF-regression-tests/archive/refs/heads/rc/1.24.zip";

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

    public void getFailedPolicyComplianceFiles(Map<String, List<FailedPolicyCheck>> failedFiles, PDFAFlavour flavour, ValidationProfile customProfile, Set<String> fileNames) throws JAXBException, IOException {
        MetadataFixerConfig fixConf = FixerFactory.configFromValues("test", true);
        ProcessorConfig processorConfig = customProfile == null ?
                ProcessorFactory.fromValues(new ValidatorConfigBuilder().flavour(flavour)
                                .defaultFlavour(PDFAFlavour.NO_FLAVOUR).recordPasses(true).maxFails(0)
                                .isLogsEnabled(true).showErrorMessages(false).build(),
                        null, null, fixConf, EnumSet.of(TaskType.VALIDATE), (String) null) :
                ProcessorFactory.fromValues(new ValidatorConfigBuilder()
                                .defaultFlavour(PDFAFlavour.NO_FLAVOUR).recordPasses(true).maxFails(0)
                                .isLogsEnabled(true).showErrorMessages(false).build(),
                        null, null, fixConf, EnumSet.of(TaskType.VALIDATE), customProfile, null);
        BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig);

        File tempSchFile = File.createTempFile("veraPDF", ".sch");
        File tempPdfFile = File.createTempFile("veraPDF", ".pdf");
        File tempMrrFile = File.createTempFile("veraPDF", ".mrr");
        File tempResultFile = File.createTempFile("veraPDF", ".xml");
        List<File> files = new ArrayList<>();
        files.add(tempPdfFile);

        for (String pdfName : fileNames) {
            System.out.println(pdfName);
            try (OutputStream reportStream = new FileOutputStream(tempMrrFile)) {
                copyInputStreamToFile(this.getPdfStream(pdfName), tempPdfFile);
                processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, false, reportStream, false));
                reportStream.flush();
            } catch (IOException | VeraPDFException e) {
                failedFiles.put(pdfName, Collections.singletonList(new FailedPolicyCheck(e.getMessage())));
                e.printStackTrace();
            }
            try {
                String schName = pdfName.substring(0, pdfName.length() - 3) + "sch";
                copyInputStreamToFile(this.getSchStream(schName), tempSchFile);
                applyPolicy(tempSchFile, tempMrrFile, tempResultFile);
                int failedPolicyJobsCount = countFailedPolicyJobs(tempResultFile);
                if (failedPolicyJobsCount > 0) {
                    failedFiles.put(pdfName, getFailedChecks(tempResultFile));
                }
            } catch (Exception e) {
                failedFiles.put(pdfName, Collections.singletonList(new FailedPolicyCheck(e.getMessage())));
                e.printStackTrace();
            }
        }

        tempSchFile.deleteOnExit();
        tempPdfFile.deleteOnExit();
        tempMrrFile.deleteOnExit();
        tempResultFile.deleteOnExit();
    }

    public static void printResult(Map<String, List<FailedPolicyCheck>> failedFiles) {
        if (failedFiles.size() > 0) {
            System.out.println("Some files is not compliant with policy: ");
            for (Map.Entry<String, List<FailedPolicyCheck>> entry : failedFiles.entrySet()) {
                System.out.println(entry.getKey());
                for (FailedPolicyCheck check : entry.getValue()) {
                    System.out.println(check);
                }
                System.out.println();
            }
        } else {
            System.out.println("Files are compliant with policies");
        }
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

    public static List<FailedPolicyCheck> getFailedChecks(File xmlReport) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(xmlReport);
        XPath path = XPathFactory.newInstance().newXPath();
        List<FailedPolicyCheck> failedChecks = new LinkedList<>();
        NodeList list = ((NodeList)path.evaluate("//policyReport/failedChecks/check", document, XPathConstants.NODESET));
        for (int i = 0; i < list.getLength(); i++) {
            Element check = (Element)list.item(i);
            String test = check.getAttribute("test");
            String messageValue = getProperty(check, "message").getTextContent();
            Element node = (Element)path.evaluate(check.getAttribute("location"), document, XPathConstants.NODE);
            failedChecks.add(new FailedPolicyCheck(node, messageValue, test));
        }
        return failedChecks;
    }

    public static void applyPolicy(File policyFile, File tempMrrFile, File tempResultFile) throws IOException, VeraPDFException {
        File tempPolicyResult = File.createTempFile("policyResult", "veraPDF");
        try (InputStream mrrIs = new FileInputStream(tempMrrFile);
             OutputStream policyResultOs = new FileOutputStream(tempPolicyResult)) {
            PolicyChecker.applyPolicy(policyFile, mrrIs, policyResultOs);
        }
        try (OutputStream mrrReport = new FileOutputStream(tempResultFile)) {
            PolicyChecker.insertPolicyReport(tempPolicyResult, tempMrrFile, mrrReport);
        }

        if (!tempPolicyResult.delete()) {
            tempPolicyResult.deleteOnExit();
        }
    }

    private static Node getProperty(Node parent, String propertyName) {
        if (parent == null) {
            return null;
        }
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (propertyName.equals(item.getNodeName())){
                return item;
            }
        }
        return null;
    }
}
