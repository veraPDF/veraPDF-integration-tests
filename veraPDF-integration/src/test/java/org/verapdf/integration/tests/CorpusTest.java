/**
 * This file is part of veraPDF Quality Assurance, a module of the veraPDF
 * project. Copyright (c) 2015, veraPDF Consortium <info@verapdf.org> All rights
 * reserved. veraPDF Quality Assurance is free software: you can redistribute it
 * and/or modify it under the terms of either: The GNU General public license
 * GPLv3+. You should have received a copy of the GNU General Public License
 * along with veraPDF Quality Assurance as the LICENSE.GPL file in the root of
 * the source tree. If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html. The Mozilla Public License
 * MPLv2+. You should have received a copy of the Mozilla Public License along
 * with veraPDF Quality Assurance as the LICENSE.MPL file in the root of the
 * source tree. If a copy of the MPL was not distributed with this file, you can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.integration.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.verapdf.component.ComponentDetails;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.*;
import org.verapdf.pdfa.qa.AbstractTestCorpus.Corpus;
import org.verapdf.pdfbox.foundry.PdfBoxFoundryProvider;
import org.yaml.snakeyaml.Yaml;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@SuppressWarnings({ "javadoc" })
public class CorpusTest {
    private static ComponentDetails gfDetails;
    private static ComponentDetails pdfBoxDetails;
    private static final List<ResultSet> gfResults = new ArrayList<>();
    private static final List<ResultSet> pdfBoxResults = new ArrayList<>();
    private static final MustacheFactory MF = new DefaultMustacheFactory("org/verapdf/integration/templates");
    private static final Mustache RESULTS_MUSTACHE = MF.compile("corpus-results.mustache");
    private static final Mustache SUMMARY_MUSTACHE = MF.compile("test-summary.mustache");

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @BeforeClass
    public static final void SetUp() throws IOException {
        CorpusManager.initialise();
    }

    private static Set<String> expectedForCorpus(final Corpus corpus, final PDFAFlavour flavour,
            final EnumMap<Corpus, EnumMap<PDFAFlavour, Set<String>>> expectedFailureSets) {
        EnumMap<PDFAFlavour, Set<String>> expectedByFlavour = expectedFailureSets.get(corpus);
        if (expectedByFlavour == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(expectedByFlavour.get(flavour));
    }

    @AfterClass
    public static void outputResults() throws IOException {
        writeResults();
    }

    @Test
    public void testPdfBox() throws Exception {
        PdfBoxFoundryProvider.initialise();
        RegressionTestingHelper.printDependencies();
        assertTrue(Foundries.defaultParserIsPDFBox());
        pdfBoxDetails = Foundries.defaultInstance().getDetails();
        test(pdfBoxResults, "org/verapdf/integration/tests/rules/corpus-pdfbox.yml");
        collector.checkThat("Exceptions thrown during PDFBox testing.", countExceptions(pdfBoxResults), equalTo(0));
    }

    @Test
    public void testGreenfield() throws Exception {
        VeraGreenfieldFoundryProvider.initialise();
        RegressionTestingHelper.printDependencies();
        assertFalse(Foundries.defaultParserIsPDFBox());
        gfDetails = Foundries.defaultInstance().getDetails();
        test(gfResults, "org/verapdf/integration/tests/rules/corpus-gf.yml");
        collector.checkThat("Exceptions thrown during Greenfield testing.", countExceptions(gfResults), equalTo(0));
    }

    private void test(List<ResultSet> results, String ymlPath) throws Exception {
        testCorpora(results);
        final EnumMap<Corpus, EnumMap<PDFAFlavour, Set<String>>> failures = createExpectedFailures(ymlPath);
        for (ResultSet set : results) {
            Set<String> expected = expectedForCorpus(Corpus.fromId(set.getCorpusId()), set
                    .getValidationProfile().getPDFAFlavour(), failures);
            testResults(set, expected);
        }
    }

    private static int countExceptions(final List<ResultSet> resultSets) {
        int exceptionCount = 0;
        for (ResultSet set : resultSets) {
            exceptionCount += set.getExceptions().size();
        }
        return exceptionCount;
    }

    private static void testCorpora(final List<ResultSet> resultSets) {
        for (PDFAFlavour flavour : CorpusManager.testableFlavours()) {
            for (TestCorpus corpus : CorpusManager.corporaForFlavour(flavour)) {
                if (flavour != PDFAFlavour.NO_FLAVOUR) {
                    try (PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false)) {
                        ResultSet results = ResultSetImpl.validateCorpus(corpus, validator, flavour);
                        resultSets.add(results);
                    } catch (IOException excep) {
                        // Just exception closing validator
                        excep.printStackTrace();
                    }
                } else {
                    ResultSet results = ResultSetImpl.validateCorpus(corpus, null, flavour);
                    resultSets.add(results);
                }
            }
        }
    }

    private void testResults(final ResultSet results, final Set<String> expected) {
        if (expected == null) {
            return;
        }
        for (ResultSet.Result result : results.getResults()) {
            collector
                    .checkThat(
                            String.format("Unexpected result for corpus %s-%s, item %s",
                                    results.getCorpusDetails().getName(), results.getValidationProfile().getPDFAFlavour(),
                                    result.getCorpusItemName()),
                            result.isExpectedResult(), equalTo(!expected.contains(result.getCorpusItemName())));
        }
    }

    /**
     * Tests the passed String {@code parseForMatches} and returns true if
     * {@link PDFAFlavour#fromString(String)} returns one of the flavours in
     * {@code filters}.
     * 
     * @param parseForMatches
     *                        string to test for flavour matches
     * @param filters
     *                        {@code List} of {@link PDFAFlavour}s to test against
     *                        for
     *                        matches
     * @return true of {@code PDFAFlavour} parsed from {@code parseForMatches}
     *         is contained in {@code filters}.
     */
    @SuppressWarnings("unused")
    private static boolean matchesFlavourFilter(final String parseForMatches, final List<PDFAFlavour> filters) {
        PDFAFlavour flavour = PDFAFlavour.fromString(parseForMatches);
        return filters.contains(flavour);
    }

    private static void writeResults() throws IOException {
        File rootDir = new File("target/test-results");
        if (!rootDir.exists())
            rootDir.mkdirs();
        writeSummaries(rootDir);
        int index = 0;
        for (ResultSet pdfBoxResult : pdfBoxResults) {
            ResultSet gfResult = gfResults.get(index++);
            Map<String, Object> scopes = new HashMap<>();
            scopes.put("pdfBoxResult", pdfBoxResult);
            scopes.put("gfResult", gfResult);
            scopes.put("profile", pdfBoxResult.getValidationProfile().getPDFAFlavour().getId());
            if (rootDir.isDirectory() && rootDir.canWrite()) {
                String dirName = pdfBoxResult.getCorpusDetails().getName() + "-"
                        + pdfBoxResult.getValidationProfile().getPDFAFlavour().getId();
                outputResultsToFile(scopes, new File(rootDir, dirName));
            } else {
                RESULTS_MUSTACHE.execute(new PrintWriter(System.out), scopes).flush();
            }
        }
    }

    private static void writeSummaries(final File outputDir) throws FileNotFoundException, IOException {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("pdfBoxDetails", ResultSetDetailsImpl.getNewInstance(pdfBoxDetails));
        scopes.put("gfDetails", ResultSetDetailsImpl.getNewInstance(gfDetails));
        scopes.put("pdfBoxResults", pdfBoxResults);
        scopes.put("gfResults", gfResults);
        try (Writer writer = new PrintWriter(new File(outputDir, "index.html"))) {
            SUMMARY_MUSTACHE.execute(writer, scopes);
        }
    }

    private static void outputResultsToFile(Map<String, Object> scopes, final File outputDir)
            throws FileNotFoundException, IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (Writer writer = new PrintWriter(new File(outputDir, "index.html"))) {
            RESULTS_MUSTACHE.execute(writer, scopes).flush();
        }
    }

    private static final EnumMap<Corpus, EnumMap<PDFAFlavour, Set<String>>> createExpectedFailures(
            final String rulesFile) throws Exception {
        final EnumMap<Corpus, EnumMap<PDFAFlavour, Set<String>>> expectedFailureSets = new EnumMap<>(
                Corpus.class);
        try (InputStream rulesStream = CorpusTest.class.getClassLoader()
                .getResourceAsStream(rulesFile)) {
            assertNotNull("No resource found for rulesFile: " + rulesFile, rulesStream);

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(rulesStream);
            for (Corpus corpus : Corpus.values()) {
                Map<String, Object> corpusData = (Map<String, Object>) data.get(corpus.getId());
                if (corpusData == null)
                    continue;
                for (PDFAFlavour flavour : corpus.getFlavours()) {
                    List<String> expected = (List<String>) corpusData.get(flavour.getId());
                    if (expected == null)
                        continue;
                    expectedFailureSets.computeIfAbsent(corpus,
                            k -> new EnumMap<>(PDFAFlavour.class));
                    expectedFailureSets.get(corpus).computeIfAbsent(flavour, k -> new HashSet<>(expected));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return expectedFailureSets;
    }
}
