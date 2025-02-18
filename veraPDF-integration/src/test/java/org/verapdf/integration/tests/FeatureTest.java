package org.verapdf.integration.tests;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.junit.Assert;
import org.junit.Test;
import org.verapdf.component.ComponentDetails;
import org.verapdf.features.FeatureExtractionResult;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.gf.model.GFModelParser;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfbox.foundry.PdfBoxFoundryProvider;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.FeatureTestResult;
import org.verapdf.pdfa.qa.FeatureTestResultImpl;
import org.verapdf.pdfa.qa.ResultSetDetailsImpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author Sergey Shemyakov
 */
public class FeatureTest {

    private static final MustacheFactory MF = new DefaultMustacheFactory(
            "org/verapdf/integration/templates");
    private static final Mustache SUMMARY_MUSTACHE = MF.compile("features-summary.mustache");
    private static ComponentDetails gfDetails;
    private static ComponentDetails pdfBoxDetails;
    private static final File outputDir = new File("target/features-test-results");

    private static Stack<String> failMessages = new Stack<>();
    private static final Map<String, FeatureObjectType> FILES_FEATURES_MAP = new HashMap<>();
    private static final List<FeatureTestResult> results;
    private static final String PATH_FORMAT = "src/test/resources/test-resources/feature-tests/%s.pdf";

    static {
        FILES_FEATURES_MAP.put("Annotations", FeatureObjectType.ANNOTATION);
        FILES_FEATURES_MAP.put("ColorSpaces", FeatureObjectType.COLORSPACE);
        FILES_FEATURES_MAP.put("Font", FeatureObjectType.FONT);
        FILES_FEATURES_MAP.put("Forms", FeatureObjectType.FORM_XOBJECT);
        FILES_FEATURES_MAP.put("InfoDictionary", FeatureObjectType.INFORMATION_DICTIONARY);
        FILES_FEATURES_MAP.put("Outlines", FeatureObjectType.OUTLINES);
        FILES_FEATURES_MAP.put("Pages", FeatureObjectType.PAGE);
        FILES_FEATURES_MAP.put("ICC_CMYK", FeatureObjectType.ICCPROFILE);
        FILES_FEATURES_MAP.put("ICC_GRAY", FeatureObjectType.ICCPROFILE);
        FILES_FEATURES_MAP.put("ICC_RGB", FeatureObjectType.ICCPROFILE);
        results = new ArrayList<>(FILES_FEATURES_MAP.size());
    }

    @Test
    public void testFeatures() throws IOException {
        int exceptionsCount = 0;
        for (Map.Entry<String, FeatureObjectType> entry : FILES_FEATURES_MAP.entrySet()) {
            try {
                testFile(entry);
                results.add(new FeatureTestResultImpl(null, entry.getKey(),
                        entry.getValue()));
            } catch (Throwable e) {
                exceptionsCount++;
                results.add(new FeatureTestResultImpl(e, entry.getKey(),
                        entry.getValue()));
            }
        }
        outputResults();
        Assert.assertEquals("Exceptions during feature detection", 0, exceptionsCount);
    }

    public static void outputResults() throws IOException {
        File rootDir = new File("target/features-test-results");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("pdfBoxDetails", ResultSetDetailsImpl.getNewInstance(pdfBoxDetails));
        scopes.put("gfDetails", ResultSetDetailsImpl.getNewInstance(gfDetails));
        scopes.put("results", results);

        try (Writer writer = new PrintWriter(new File(outputDir, "index.html"))) {
            SUMMARY_MUSTACHE.execute(writer, scopes);
        }
    }

    private void testFile(Map.Entry<String, FeatureObjectType> file)
            throws ModelParsingException, EncryptedPdfException {
        File testFile = new File(String.format(PATH_FORMAT, file.getKey()));
        FeatureExtractorConfig config;
        FeatureExtractionResult gfFeatures;
        try {
            config = FeatureFactory.configFromValues(EnumSet.of(file.getValue()));
            initGfFoundry();
            GFModelParser gfParser = GFModelParser.createModelWithFlavour(testFile, PDFAFlavour.NO_FLAVOUR);
            gfFeatures = gfParser.getFeatures(config);
        } catch (Throwable t) {
            throw new RuntimeException("greenfield exception: " + t.getMessage(), t);
        }
        try {
            initPdfboxFoundry();
            ModelParser pbParser = ModelParser.createModelWithFlavour(testFile, PDFAFlavour.NO_FLAVOUR);
            FeatureExtractionResult pbFeatures = pbParser.getFeatures(config);
            Assert.assertTrue(featureExtractionResultsEqual(gfFeatures, pbFeatures, file.getValue()));
        } catch (Throwable t) {
            throw new RuntimeException("pdfbox exception: " + t.getMessage(), t);
        }
    }

    private static boolean featureExtractionResultsEqual(FeatureExtractionResult res1,
                                                         FeatureExtractionResult res2,
                                                         FeatureObjectType type) {
        List<FeatureTreeNode> features1 = res1.getFeatureTreesForType(type);
        List<FeatureTreeNode> features2 = res2.getFeatureTreesForType(type);
        return featureTreeNodeListsEqual(features1, features2);
    }

    private static boolean featureTreeNodeListsEqual(List<FeatureTreeNode> list1,
                                                     List<FeatureTreeNode> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (FeatureTreeNode child1 : list1) {
            boolean isPresentInNode2 = false;
            for (FeatureTreeNode child2 : list2) {
                if (featureTreeNodesEqual(child1, child2)) {
                    isPresentInNode2 = true;
                    break;
                } else {
                    failMessages.pop();
                }
            }
            if (!isPresentInNode2) {
                failMessages.push("Feature tree node " + list1 + " has child" +
                        " that is not present in " + list2 + ": " + child1);
                return false;
            }
        }
        return true;
    }

    private static boolean featureTreeNodesEqual(FeatureTreeNode node1,
                                                 FeatureTreeNode node2) {
        Map<String, String> node1Attrs = node1.getAttributes();
        Map<String, String> node2Attrs = node2.getAttributes();
        List<FeatureTreeNode> node1Children = node1.getChildren();
        List<FeatureTreeNode> node2Children = node2.getChildren();
        if (node1Attrs.size() != node2Attrs.size() ||
                node1Children.size() != node2Children.size() ||
                node1.isMetadataNode() != node2.isMetadataNode() ||
                !node1.getName().equals(node2.getName())) {
            failMessages.push("Different amount of children or different " +
                    "amount of attributes or different name, nodes: " + node1 + " " + node2);
            return false;
        }

        if (node1.getValue() != null &&
                !node1.getValue().equals(node2.getValue())) {
            failMessages.push("Value of one node is null, value of other is not," +
                    " nodes: " + node1 + " " + node2);
            return false;
        } else if (node1.getValue() == null && node2.getValue() != null) {
            failMessages.push("Value of one node is null, value of other is not," +
                    " nodes: " + node1 + " " + node2);
            return false;
        }

        for (Map.Entry<String, String> entry : node1Attrs.entrySet()) {
            String key = entry.getKey();
            if (!"id".equals(key)) {
                if (!entry.getValue().equals(node2Attrs.get(key))) {
                    failMessages.push("Different value of attribute " + key +
                            " in nodes: " + node1 + " " + node2);
                    return false;
                }
            } else {
                if (!node2Attrs.containsKey("id")) {
                    failMessages.push("One node has id, other has not, nodes: " + node1 + " " + node2);
                    return false;
                }
            }
        }

        return featureTreeNodeListsEqual(node1Children, node2Children);
    }

    private static void initGfFoundry() {
        VeraGreenfieldFoundryProvider.initialise();
        if (gfDetails == null) {
            gfDetails = Foundries.defaultInstance().getDetails();
        }
    }

    private static void initPdfboxFoundry() {
        PdfBoxFoundryProvider.initialise();
        if (pdfBoxDetails == null) {
            pdfBoxDetails = Foundries.defaultInstance().getDetails();
        }
    }

    // utility class for mustache
    public class ThrowableMessageWrapper {
        private Throwable e;
        private boolean exceptionIsNotNull;

        public ThrowableMessageWrapper(Throwable e) {
            this.e = e;
            this.exceptionIsNotNull = e != null;
        }

        public String getExceptionMessage() {
            return e != null ? e.getMessage() : "";
        }

        public boolean isExceptionIsNotNull() {
            return exceptionIsNotNull;
        }
    }
}
