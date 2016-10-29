/**
 * 
 */
package org.verapdf.integration;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.AbstractTestCorpus.Corpus;
import org.verapdf.pdfa.qa.TestCorpus;
import org.verapdf.pdfa.qa.ZipBackedTestCorpus;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
public final class CorpusManager {
	// Reference to corpus zip temp file
	private static final EnumMap<PDFAFlavour, Set<TestCorpus>> corporaByFlavour = new EnumMap<>(PDFAFlavour.class);

	private CorpusManager() {
		assert (false);
	}

	public static void initialise() throws IOException {
		if (!corporaByFlavour.isEmpty())
			return;
		for (Corpus corpus : Corpus.values()) {
			for (PDFAFlavour flavour : corpus.getFlavours()) {
				TestCorpus toAdd = ZipBackedTestCorpus.fromZipSource(corpus.getId(), corpus, corpus.getDescription(), flavour);
				if (!corporaByFlavour.containsKey(flavour)) {
					corporaByFlavour.put(flavour, new HashSet<TestCorpus>());
				}
				corporaByFlavour.get(flavour).add(toAdd);
			}
		}
	}

	public static Set<PDFAFlavour> testableFlavours() {
		return Collections.unmodifiableSet(corporaByFlavour.keySet());
	}

	public static Set<TestCorpus> corporaForFlavour(final PDFAFlavour key) {
		return Collections.unmodifiableSet(corporaByFlavour.get(key));
	}

	public static TestCorpus corpusByFlavourAndType(final PDFAFlavour key, final Corpus type) {
		for (TestCorpus corpus : corporaByFlavour.get(key)) {
			if (corpus.getType() == type) {
				return corpus;
			}
		}
		return null;
	}
}
