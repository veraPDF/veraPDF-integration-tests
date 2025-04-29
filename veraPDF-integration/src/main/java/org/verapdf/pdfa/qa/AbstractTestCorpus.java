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

import org.verapdf.pdfa.flavours.PDFAFlavour;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@XmlRootElement(namespace = "http://www.verapdf.org/corpus", name = "corpus")
public abstract class AbstractTestCorpus<L> implements TestCorpus {
    private static final String VERA_URL = "https://github.com/veraPDF/veraPDF-corpus/archive/rel/1.28.zip";
    private static final String ISARTOR_URL = "https://corpora.openpreservation.org/veraPDF/isartor-pdfa-2008-08-13.zip";
    private static final String BFO_URL = "https://github.com/bfosupport/pdfa-testsuite/archive/master.zip";

    @XmlElement(name = "details")
    private final CorpusDetails details;
    @XmlElementWrapper
    @XmlElement(name = "item")
    private final Map<String, L> itemMap;
    protected final Corpus type;

    protected AbstractTestCorpus(final CorpusDetails details, final Corpus type, final Map<String, L> itemMap) {
        this.details = details;
        this.type = type;
        this.itemMap = new HashMap<>(itemMap);
    }

    @Override
    public Corpus getType() {
        return this.type;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public CorpusDetails getDetails() {
        return this.details;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public int getItemCount() {
        return this.itemMap.size();
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public Set<String> getItemNames() {
        return Collections.unmodifiableSet(this.itemMap.keySet());
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public Set<String> getItemNamesForFlavour(PDFAFlavour flavour) {
        // TODO Look at implementing filtering by flavour
        return Collections.unmodifiableSet(this.itemMap.keySet());
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public InputStream getItemStream(String itemName) throws IOException {
        if (!this.itemMap.containsKey(itemName))
            throw new IOException("No element found for name=" + itemName);
        return getStreamFromReference(this.itemMap.get(itemName));
    }

    protected abstract InputStream getStreamFromReference(final L reference) throws IOException;

    /**
     * { @inheritDoc }
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.details == null) ? 0 : this.details.hashCode());
        result = prime * result + ((this.getItemNames() == null) ? 0 : this.getItemNames().hashCode());
        return result;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TestCorpus))
            return false;
        TestCorpus other = (TestCorpus) obj;
        if (this.details == null) {
            if (other.getDetails() != null)
                return false;
        } else if (!this.details.equals(other.getDetails()))
            return false;
        if (this.getItemNames() == null) {
            if (other.getItemNames() != null)
                return false;
        } else if (!this.getItemNames().equals(other.getItemNames()))
            return false;
        return true;
    }

    public enum Corpus {
        VERA("veraPDF",
                EnumSet.of(PDFAFlavour.PDFA_1_A, PDFAFlavour.PDFA_1_B, PDFAFlavour.PDFA_2_A, PDFAFlavour.PDFA_2_B,
                        PDFAFlavour.PDFA_2_U, PDFAFlavour.PDFA_3_B, PDFAFlavour.PDFA_4, PDFAFlavour.PDFA_4_F, 
                        PDFAFlavour.PDFA_4_E, PDFAFlavour.PDFUA_1, PDFAFlavour.PDFUA_2,
                        PDFAFlavour.WTPDF_1_0_REUSE, PDFAFlavour.WTPDF_1_0_ACCESSIBILITY),
                URI.create(VERA_URL), "veraCorp-"),
        ISARTOR("Isartor", EnumSet.of(PDFAFlavour.PDFA_1_B), URI.create(ISARTOR_URL), "isartCorp-"),
        BFO("BFO", EnumSet.of(PDFAFlavour.PDFA_2_B), URI.create(BFO_URL), "bfoCorp-"),
        TWG("TWG", EnumSet.of(PDFAFlavour.NO_FLAVOUR), VERA.getZipFile().toURI(), "twgCorp-");

        private static final String desc = "Synthetic test files for PDF/A validation.";
        private static final Map<String, Corpus> ID_LOOKUP = new HashMap<>();
        static {
            for (Corpus corpus : Corpus.values()) {
                ID_LOOKUP.put(corpus.id, corpus);
            }
        }

        private final String id;
        private final EnumSet<PDFAFlavour> flavours;
        private final File zipFile;

        private Corpus(final String id, final EnumSet<PDFAFlavour> flavours, final URI downloadUri,
                final String prefix) {
            this.id = id;
            this.flavours = EnumSet.copyOf(flavours);
            try {
                this.zipFile = createTempFileFromCorpus(downloadUri.toURL(), prefix);
            } catch (IOException excep) {
                throw new IllegalStateException(excep);
            }
        }

        public String getId() {
            return this.id;
        }

        @SuppressWarnings("static-method")
        public String getDescription() {
            return desc;
        }

        public File getZipFile() {
            return this.zipFile;
        }

        public Set<PDFAFlavour> getFlavours() {
            return this.flavours;
        }

        public static Corpus fromId(final String id) {
            return ID_LOOKUP.get(id);
        }
    }

    public static File createTempFileFromCorpus(final URL downloadLoc, final String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, ".zip");
        System.out.println("Downloading: " + downloadLoc + ", to temp:" + tempFile);
        int totalBytes = 0;
        try (OutputStream output = new FileOutputStream(tempFile);
                InputStream corpusInput = handleRedirects(downloadLoc)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = corpusInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
        }
        System.out.println("Downloaded: " + totalBytes + " bytes");
        tempFile.deleteOnExit();
        return tempFile;
    }

    static InputStream handleRedirects(URL url) throws IOException {
        if (!url.getProtocol().startsWith("http")) {
            return url.openStream();
        }
        URL resourceUrl;
        URL base;
        URL next;
        Map<String, Integer> visited;
        HttpURLConnection conn;
        String location;
        String urlString = url.toExternalForm();
        int times;

        visited = new HashMap<>();

        while (true) {
            times = visited.compute(urlString, (key, count) -> count == null ? 1 : count + 1);

            if (times > 3)
                throw new IOException("Stuck in redirect loop");

            resourceUrl = new URL(urlString);
            conn = (HttpURLConnection) resourceUrl.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false); // Make the logic below easier to detect redirections
            conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    base = new URL(urlString);
                    next = new URL(base, location); // Deal with relative URLs
                    urlString = next.toExternalForm();
                    continue;
            }

            break;
        }

        return conn.getInputStream();
    }
}
