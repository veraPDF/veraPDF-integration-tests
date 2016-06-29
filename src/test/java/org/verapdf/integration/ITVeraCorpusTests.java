package org.verapdf.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.ResultSet;
import org.verapdf.pdfa.qa.ResultSetDetailsImpl;
import org.verapdf.pdfa.qa.ResultSetImpl;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfa.validators.Validators;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@SuppressWarnings({ "javadoc" })
public class ITVeraCorpusTests {
    // Directory of validation profiles poulated by download from GitHub
    private static final List<ResultSet> RESULTS = new ArrayList<>();
    private static final MustacheFactory MF = new DefaultMustacheFactory(
            "org/verapdf/integration/templates");
    private static final Mustache RESULTS_MUSTACHE = MF
            .compile("corpus-results.mustache");
    private static final Mustache SUMMARY_MUSTACHE = MF
            .compile("test-summary.mustache");

    @AfterClass
    public static void outputResults() throws IOException {
        outputCorpusResults();
    }

    @Test
    public void testIsartor1b() throws IOException {
        TestCorpus isartorCorpus = CorpusManager.getIsartorCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_1_B, false);
        ResultSet results = ResultSetImpl.validateCorpus(isartorCorpus,
                validator);
        RESULTS.add(results);
    }

    @Test
    public void testVera1b() throws IOException {
        TestCorpus veraCorpus = CorpusManager.getVera1BCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_1_B, false);
        ResultSet results = ResultSetImpl.validateCorpus(veraCorpus, validator);
        RESULTS.add(results);
    }

    @Test
    public void testVera1a() throws IOException {
        TestCorpus veraCorpus = CorpusManager.getVera1ACorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_1_A, false);
        ResultSet results = ResultSetImpl.validateCorpus(veraCorpus, validator);
        RESULTS.add(results);
    }

    @Test
    public void testVera2b() throws IOException {
        TestCorpus veraCorpus = CorpusManager.getVera2BCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_2_B, false);
        ResultSet results = ResultSetImpl.validateCorpus(veraCorpus, validator);
        RESULTS.add(results);
    }

    @Test
    public void testVera2u() throws IOException {
        TestCorpus veraCorpus = CorpusManager.getVera2UCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_2_U, false);
        ResultSet results = ResultSetImpl.validateCorpus(veraCorpus, validator);
        RESULTS.add(results);
    }

    @Test
    public void testVera3b() throws IOException {
        TestCorpus veraCorpus = CorpusManager.getVera3BCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_3_B, false);
        ResultSet results = ResultSetImpl.validateCorpus(veraCorpus, validator);
        RESULTS.add(results);
    }

    @Test
    public void testBFO2b() throws IOException {
        TestCorpus BFOCorpus = CorpusManager.getBFOCorpus();
        PDFAValidator validator = Validators.createValidator(
                PDFAFlavour.PDFA_2_B, false);
        ResultSet results = ResultSetImpl.validateCorpus(BFOCorpus, validator);
        RESULTS.add(results);
    }

    /**
     * Tests the passed String {@code parseForMatches} and returns true if
     * {@link PDFAFlavour#fromString(String)} returns one of the flavours in
     * {@code filters}.
     * 
     * @param parseForMatches
     *            string to test for flavour matches
     * @param filters
     *            {@code List} of {@link PDFAFlavour}s to test against for
     *            matches
     * @return true of {@code PDFAFlavour} parsed from {@code parseForMatches}
     *         is contained in {@code filters}.
     */
    @SuppressWarnings("unused")
    private static boolean matchesFlavourFilter(final String parseForMatches,
            final List<PDFAFlavour> filters) {
        PDFAFlavour flavour = PDFAFlavour.fromString(parseForMatches);
        return filters.contains(flavour);
    }

    private static void outputCorpusResults() throws IOException {
        File rootOutputDir = new File("target/test-results");
        if (!rootOutputDir.exists())
            rootOutputDir.mkdirs();
        outputSummaryToFile(rootOutputDir);
        for (ResultSet results : RESULTS) {
            if (rootOutputDir.isDirectory() && rootOutputDir.canWrite()) {
                String dirName = results.getCorpusDetails().getName()
                        + "-"
                        + results.getValidationProfile().getPDFAFlavour()
                                .getId();
                outputResultsToFile(results, new File(rootOutputDir, dirName));
            } else {
                RESULTS_MUSTACHE.execute(new PrintWriter(System.out), results)
                        .flush();
            }
        }
    }

    private static void outputSummaryToFile(final File outputDir)
            throws FileNotFoundException, IOException {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("details", ResultSetDetailsImpl.getNewInstance());
        scopes.put("results", RESULTS);
        try (Writer writer = new PrintWriter(new File(outputDir, "index.html"))) {
            SUMMARY_MUSTACHE.execute(writer, scopes);
        }
    }

    private static void outputResultsToFile(final ResultSet results,
            final File outputDir) throws FileNotFoundException, IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (Writer writer = new PrintWriter(new File(outputDir, "index.html"))) {
            RESULTS_MUSTACHE.execute(writer, results).flush();
        }
    }
}
