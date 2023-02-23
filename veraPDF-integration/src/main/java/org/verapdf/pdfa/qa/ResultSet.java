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

import org.verapdf.component.AuditDuration;
import org.verapdf.pdfa.qa.CorpusItemId.TestType;
import org.verapdf.pdfa.qa.CorpusItemIdImpl.CorpusItemIdComparator;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Comparator;
import java.util.Set;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 */
@XmlJavaTypeAdapter(ResultSetImpl.Adapter.class)
public interface ResultSet {
    /**
     * @return the {@link ResultSetDetails} for the instance
     */
    public ResultSetDetails getDetails();

    /**
     * @return the {@link CorpusDetails} for the instance
     */
    public CorpusDetails getCorpusDetails();

    /**
     * @return the {@link ValidationProfile} used to generate the result set
     */
    public ValidationProfile getValidationProfile();

    /**
     * @return the {@link ResultSetSummary} for the result set
     */
    public ResultSetSummary getSummary();

    /**
     * @return the {@code Set} of {@link Result}s
     */
    public Set<Result> getResults();

    /**
     * @return
     */
    public Set<Incomplete> getExceptions();

    /**
     * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
     */
    public static final class Result {
        private final CorpusItemId corpusItemId;
        private final ValidationResult result;
        private final AuditDuration duration;
        private final long memoryUsed;

        Result(final CorpusItemId corpusItemId, final ValidationResult result, final AuditDuration duration,
                long memoryUsed) {
            this.corpusItemId = corpusItemId;
            this.result = result;
            this.duration = duration;
            this.memoryUsed = memoryUsed;
        }

        /**
         * @return the corpusItem
         */
        public CorpusItemId getCorpusItemId() {
            return this.corpusItemId;
        }

        /**
         * @return the result
         */
        public ValidationResult getResult() {
            return this.result;
        }

        public boolean isExpectedResult() {
            return this.corpusItemId.getExpectedResult() == this.result.isCompliant();
        }

        public String getTestType() {
            if ((this.corpusItemId.getTestType() == TestType.PASS)
                    || (this.corpusItemId.getTestType() == TestType.FAIL)) {
                return this.isExpectedResult() ? "pass" : "fail";
            }
            return this.corpusItemId.getTestType().getId();
        }

        public String getCorpusItemName() {
            return this.corpusItemId.getName();
        }

        public String getDuration() {
            return this.duration.getDuration();
        }

        public long getMemoryUsed() {
            return this.memoryUsed;
        }

        /**
         * { @inheritDoc }
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int hashResult = 1;
            hashResult = prime * hashResult + ((this.corpusItemId == null) ? 0 : this.corpusItemId.hashCode());
            hashResult = prime * hashResult + ((this.result == null) ? 0 : this.result.hashCode());
            return hashResult;
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
            if (getClass() != obj.getClass())
                return false;
            Result other = (Result) obj;
            if (this.corpusItemId == null) {
                if (other.corpusItemId != null)
                    return false;
            } else if (!this.corpusItemId.equals(other.corpusItemId))
                return false;
            if (this.result == null) {
                if (other.result != null)
                    return false;
            } else if (!this.result.equals(other.result))
                return false;
            return true;
        }

        /**
         * { @inheritDoc }
         */
        @Override
        public String toString() {
            return "Result [corpusItemid=" + this.corpusItemId + ", result=" + this.result + "]";
        }

    }

    /**
     * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
     */
    public static class Incomplete {
        private final CorpusItemId corpusItemId;
        private final Throwable cause;

        /**
         * @param corpusItem
         * @param cause
         */
        public Incomplete(final CorpusItemId corpusItemId, final Throwable cause) {
            this.corpusItemId = corpusItemId;
            this.cause = cause;
        }

        /**
         * @return the corpusItem
         */
        public CorpusItemId getCorpusItemId() {
            return this.corpusItemId;
        }

        /**
         * @return the cause
         */
        public Throwable getCause() {
            return this.cause;
        }

        /**
         * { @inheritDoc }
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.cause == null) ? 0 : this.cause.hashCode());
            result = prime * result + ((this.corpusItemId == null) ? 0 : this.corpusItemId.hashCode());
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
            if (getClass() != obj.getClass())
                return false;
            Incomplete other = (Incomplete) obj;
            if (this.cause == null) {
                if (other.cause != null)
                    return false;
            } else if (!this.cause.equals(other.cause))
                return false;
            if (this.corpusItemId == null) {
                if (other.corpusItemId != null)
                    return false;
            } else if (!this.corpusItemId.equals(other.corpusItemId))
                return false;
            return true;
        }

        /**
         * { @inheritDoc }
         */
        @Override
        public String toString() {
            return "Incomplete [corpusItemId=" + this.corpusItemId + ", cause=" + this.cause + "]";
        }
    }

    public static class ResultComparator implements Comparator<Result> {
        @Override
        public int compare(Result firstResult, Result secondResult) {
            return new CorpusItemIdComparator().compare(firstResult.getCorpusItemId(), secondResult.getCorpusItemId());
        }
    }

}
