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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public class CorpusDetailsImpl implements CorpusDetails {
    @XmlElement(name = "name")
    final String name;
    @XmlElement(name = "description")
    final String description;
    @XmlElement(name = "hexSha1")
    final String hexSha1;

    private CorpusDetailsImpl(final String name, final String description,
            final String hexSha1) {
        this.name = name;
        this.description = description;
        this.hexSha1 = hexSha1;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getHexSha1() {
        return this.hexSha1;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
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
        if (!(obj instanceof CorpusDetails))
            return false;
        CorpusDetails other = (CorpusDetails) obj;
        if (this.description == null) {
            if (other.getDescription() != null)
                return false;
        } else if (!this.description.equals(other.getDescription()))
            return false;
        if (this.name == null) {
            if (other.getName() != null)
                return false;
        } else if (!this.name.equals(other.getName()))
            return false;
        return true;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String toString() {
        return "CorpusDetails [name=" + this.name + ", description="
                + this.description + "]";
    }

    /**
     * @param name
     *            the name of the TestCorpus
     * @param description
     *            a textual description of the TestCorpus
     * @return a new CorpusDetails instance initialised with the passed param
     *         values
     */
    static CorpusDetails fromValues(final String name,
            final String description, final String hexSha1) {
        if (name == null)
            throw new NullPointerException("Parameter name can not be null");
        if (name.isEmpty())
            throw new NullPointerException("Parameter name can not be empty");
        if (description == null)
            throw new NullPointerException(
                    "Parameter description can not be null");
        return new CorpusDetailsImpl(name, description, hexSha1);
    }

    static class Adapter extends XmlAdapter<CorpusDetailsImpl, CorpusDetails> {
        @Override
        public CorpusDetails unmarshal(CorpusDetailsImpl details) {
            return details;
        }

        @Override
        public CorpusDetailsImpl marshal(CorpusDetails details) {
            return (CorpusDetailsImpl) details;
        }
    }

}
