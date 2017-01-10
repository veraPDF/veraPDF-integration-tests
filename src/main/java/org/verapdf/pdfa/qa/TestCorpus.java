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
import java.util.Set;

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.qa.AbstractTestCorpus.Corpus;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public interface TestCorpus {
    /**
     * @return the name of the TestCorpus instance
     */
    public CorpusDetails getDetails();

    /**
     * @return the number of items held in the corpus
     */
    public int getItemCount();

    /**
     * @return the set of all corpus item names
     */
    public Set<String> getItemNames();

    /**
     * @param flavour
     *            the flavour to select corpus item names for
     * @return the set of corpus item names for the PDFAFlavour {@code flavour}
     */
    public Set<String> getItemNamesForFlavour(PDFAFlavour flavour);

    /**
     * @param itemName
     *            the name of the item to retrieve the input stream for
     * @return an InputStream for the item data
     * @throws IOException
     *             if there's a problem retrieving the stream
     */
    public InputStream getItemStream(String itemName) throws IOException;
    
    public Corpus getType();
}
