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

import java.util.*;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public class CorpusSampler {
    private CorpusSampler() {
        /**
         * Disable default constructor
         */
    }

    /**
     * @param corpus
     *            a {@link TestCorpus} instance to sample from
     * @param sampleSize
     *            the size of the sample to take
     * @return the {@code Set<String>} of names that make up the sample
     */
    public static Set<String> randomSample(final TestCorpus corpus,
            final int sampleSize) {
        if (corpus == null)
            throw new NullPointerException("Parameter corpus can not be null");
        if (sampleSize < 1)
            throw new NullPointerException("Parameter sampleSize=" + sampleSize
                    + ", must be > 0");
        Random random = new Random(new Date().getTime());
        int setSize = corpus.getItemNames().size();
        List<String> names = new ArrayList<>(corpus.getItemNames());
        Set<String> sample = new HashSet<>();
        for (int index = 0; index < sampleSize; index++) {
            sample.add(names.get(random.nextInt(setSize)));
        }
        return sample;
    }
}
