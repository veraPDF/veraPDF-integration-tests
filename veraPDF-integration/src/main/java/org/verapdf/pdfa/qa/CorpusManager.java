/**
 * This file is part of veraPDF Quality Assurance, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Quality Assurance is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Quality Assurance as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Quality Assurance as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.verapdf.pdfa.qa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.AbstractTestCorpus.Corpus;
import org.yaml.snakeyaml.Yaml;

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
                TestCorpus toAdd = ZipBackedTestCorpus.fromZipSource(corpus.getId(), corpus, corpus.getDescription(),
                        flavour);
                corporaByFlavour.computeIfAbsent(flavour, k -> new HashSet<>());
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
