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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public class CorpusItemImpl implements CorpusItem {
	private static final Logger LOG = Logger.getLogger(CorpusItemImpl.class.getCanonicalName());
    @XmlElement(name = "id")
    private final CorpusItemId id;
    @XmlElement(name = "path")
    private final String path;
    @XmlElement(name = "sha1")
    private final String sha1;

    private CorpusItemImpl(final String path) {
        this(CorpusItemIdImpl.defaultInstance(), "", path);
    }

    private CorpusItemImpl(final CorpusItemId id, final String sha1,
            final String path) {
        this.id = id;
        this.sha1 = sha1;
        this.path = path;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public CorpusItemId getId() {
        return this.id;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getSha1() {
        return this.sha1;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.path == null) ? 0 : this.path.hashCode());
        result = prime * result
                + ((this.sha1 == null) ? 0 : this.sha1.hashCode());
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
        if (!(obj instanceof CorpusItem))
            return false;
        CorpusItem other = (CorpusItem) obj;
        if (this.path == null) {
            if (other.getPath() != null)
                return false;
        } else if (!this.path.equals(other.getPath()))
            return false;
        if (this.sha1 == null) {
            if (other.getSha1() != null)
                return false;
        } else if (!this.sha1.equals(other.getSha1()))
            return false;
        return true;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String toString() {
        return "CorpusItem [path=" + this.path + ", sha1=" + this.sha1 + "]";
    }

    /**
     * @param path
     * @return
     */
    public static CorpusItem fromValues(final String path) {
        return fromValues(path, "");
    }

    /**
     * @param path
     * @param sha1
     * @return
     */
    public static CorpusItem fromValues(final String path, final String sha1) {
        if (path == null)
            throw new NullPointerException("path can not be null.");
        if (path.isEmpty())
            throw new IllegalArgumentException("path can not be empty.");
        if (sha1 == null)
            throw new NullPointerException("sha1 can not be null.");
        return new CorpusItemImpl(CorpusItemIdImpl.defaultInstance(), sha1,
                path);
    }

    /**
     * @param corpusFile
     * @return
     */
    public static CorpusItem fromFile(final File corpusFile) {
        if (corpusFile == null)
            throw new NullPointerException("file can not be null.");
        if (!corpusFile.isFile())
            throw new IllegalArgumentException("file must be an existing file.");
        try (FileInputStream fis = new FileInputStream(corpusFile)) {
            return fromInputStream(fis, corpusFile.getPath());
        } catch (IOException e) {
        	LOG.log(Level.FINE, "IOException closing corpus file.", e);
            // Ignore stream close error
        	e.printStackTrace();
            return fromValues(corpusFile.getPath(), "");
        }
    }

    static CorpusItem fromInputStream(final InputStream corpusStream,
            final String path) {
        if (corpusStream == null)
            throw new NullPointerException("corpusStream can not be null.");
        if (path == null)
            throw new NullPointerException("path can not be null.");
        if (path.isEmpty())
            throw new IllegalArgumentException("path can not be empty.");
        try {
            return CorpusItemImpl.fromValues(path,
                    DigestUtils.sha1Hex(corpusStream));
        } catch (IOException e) {
        	LOG.log(Level.FINE, "IOException calculating file digest", e);
            return CorpusItemImpl.fromValues(path);
        }
    }

    static class Adapter extends XmlAdapter<CorpusItemImpl, CorpusItem> {
        @Override
        public CorpusItem unmarshal(CorpusItemImpl results) {
            return results;
        }

        @Override
        public CorpusItemImpl marshal(CorpusItem results) {
            return (CorpusItemImpl) results;
        }
    }
}
