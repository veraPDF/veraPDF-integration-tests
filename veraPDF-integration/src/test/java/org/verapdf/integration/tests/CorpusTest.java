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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Comparator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.verapdf.component.ComponentDetails;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.RuleId;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.CorpusManager;
import org.verapdf.pdfa.qa.ResultSet;
import org.verapdf.pdfa.qa.ResultSetDetailsImpl;
import org.verapdf.pdfa.qa.ResultSetImpl;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfbox.foundry.PdfBoxFoundryProvider;

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

	@BeforeClass
	public static final void SetUp() throws IOException {
		try {
			CorpusManager.initialise();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@AfterClass
	public static void outputResults() throws IOException {
		writeResults();
	}

	@Rule
	public ErrorCollector collector = new ErrorCollector();

//	@Test
	public void testPdfBox() {
		PdfBoxFoundryProvider.initialise();
		pdfBoxDetails = Foundries.defaultInstance().getDetails();
		testCorpora(pdfBoxResults);
		for (ResultSet set : pdfBoxResults) {
			testResults(set);
		}
		collector.checkThat("Exceptions thrown during PDF Box testing.", countExceptions(pdfBoxResults), equalTo(0));
}

	@Test
	public void testGreenfield() {
		VeraGreenfieldFoundryProvider.initialise();
		gfDetails = Foundries.defaultInstance().getDetails();
		testCorpora(gfResults);
		printStatistic(gfResults);
		for (ResultSet set : gfResults) {
			testResults(set);
		}
		collector.checkThat("Exceptions thrown during greenfield testing.", countExceptions(gfResults), equalTo(0));
	}

	private static void printStatistic(final List<ResultSet> resultSets) {
		SortedMap<PDFAFlavour, SortedMap<RuleId, ArlingtonResult>> map = new TreeMap<>();
		for (ResultSet set : resultSets) {
			for (ResultSet.Result result : set.getResults()) {
				ValidationResult validationResult = result.getResult();
				String fileName = set.getCorpusDetails().getName() + set.getValidationProfile().getPDFAFlavour() + " " + result.getCorpusItemName();
				SortedMap<RuleId, ArlingtonResult> arlingtonResultMap = map.computeIfAbsent(validationResult.getPDFAFlavour(),
						k -> new TreeMap<>(new RuleIdComparator()));
				for (RuleId ruleId : validationResult.getFailedChecks().keySet()) {
					ArlingtonResult arlingtonResult = arlingtonResultMap.get(ruleId);
					if (arlingtonResult == null) {
						arlingtonResultMap.put(ruleId, new ArlingtonResult(ruleId, 1, fileName));
					} else {
						if (result.getCorpusItemId().getExpectedResult()) {
							arlingtonResult.addFileName(fileName);
						}
						arlingtonResult.setFailedTestNumber(arlingtonResult.getFailedTestNumber() + 1);
					}
				}
			}
		}
		System.out.println("Tests result:");
		for (Map.Entry<PDFAFlavour, SortedMap<RuleId, ArlingtonResult>> entry : map.entrySet()) {
			System.out.println(entry.getKey());
			for (ArlingtonResult result : entry.getValue().values()) {
				System.out.println(result.getRuleId() + " failed tests: " + result.getFailedTestNumber() + " file examples: ");
				System.out.println(result.getFileNames());
				System.out.println();
			}
		}
	}

	static class RuleIdComparator implements Comparator<RuleId>{

		public int compare(RuleId a, RuleId b){
			int result = a.getSpecification().compareTo(b.getSpecification());
			if (result != 0) {
				return result;
			}
			result = a.getClause().compareTo(b.getClause());
			if (result != 0) {
				return result;
			}
			return a.getTestNumber() - b.getTestNumber();
		}
	}

	static class ArlingtonResult {
		private RuleId ruleId;
		private int failedTestNumber;
		private List<String> fileNames = new LinkedList<>();

		public ArlingtonResult() {
		}

		public ArlingtonResult(RuleId ruleId, int failedTestNumber, String fileName) {
			this.ruleId = ruleId;
			this.failedTestNumber = failedTestNumber;
			this.fileNames.add(fileName);
		}

		public RuleId getRuleId() {
			return ruleId;
		}

		public void setRuleId(RuleId ruleId) {
			this.ruleId = ruleId;
		}

		public int getFailedTestNumber() {
			return failedTestNumber;
		}

		public void setFailedTestNumber(int failedTestNumber) {
			this.failedTestNumber = failedTestNumber;
		}

		public List<String> getFileNames() {
			return fileNames;
		}

		public void addFileName(String fileName) {
			this.fileNames.add(fileName);
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
						ResultSet results = ResultSetImpl.validateCorpus(corpus, validator);
						resultSets.add(results);
					} catch (IOException excep) {
						// Just exception closing validator
						excep.printStackTrace();
					}
				} else {
					ResultSet results = ResultSetImpl.validateCorpus(corpus);
					resultSets.add(results);
				}
			}
		}
	}

	private void testResults(final ResultSet results) {
		for (ResultSet.Result result : results.getResults()) {
			collector
					.checkThat(
							String.format("Unexpected result for corpus %s, item %s",
									results.getCorpusDetails().getName(), result.getCorpusItemName()),
							result.isExpectedResult(), equalTo(true));
		}
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
		for (ResultSet gfResult : gfResults) {
			Map<String, Object> scopes = new HashMap<>();
			scopes.put("gfResult", gfResult);
			if (rootDir.isDirectory() && rootDir.canWrite()) {
				String dirName = gfResult.getCorpusDetails().getName() + "-"
						+ gfResult.getValidationProfile().getPDFAFlavour().getId();
				outputResultsToFile(scopes, new File(rootDir, dirName));
			} else {
				RESULTS_MUSTACHE.execute(new PrintWriter(System.out), scopes).flush();
			}
		}
	}

	private static void writeSummaries(final File outputDir) throws FileNotFoundException, IOException {
		Map<String, Object> scopes = new HashMap<>();
//		scopes.put("pdfBoxDetails", ResultSetDetailsImpl.getNewInstance(pdfBoxDetails));
		scopes.put("gfDetails", ResultSetDetailsImpl.getNewInstance(gfDetails));
//		scopes.put("pdfBoxResults", pdfBoxResults);
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
}
