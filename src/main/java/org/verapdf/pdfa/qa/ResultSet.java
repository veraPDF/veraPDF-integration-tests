/**
 * 
 */
package org.verapdf.pdfa.qa;

import java.util.Comparator;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.verapdf.pdfa.qa.CorpusItemIdImpl.CorpusItemIdComparator;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.ValidationProfile;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
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
     * @return the {@link ValidationProfile} used for the result set
     */
    public ValidationProfile getValidationProfile();

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
     *
     */
    public static final class Result {
        private final CorpusItemId corpusItemId;
        private final ValidationResult result;

        Result(final CorpusItemId corpusItemId, final ValidationResult result) {
            this.corpusItemId = corpusItemId;
            this.result = result;
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

        /**
         * { @inheritDoc }
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int hashResult = 1;
            hashResult = prime
                    * hashResult
                    + ((this.corpusItemId == null) ? 0 : this.corpusItemId
                            .hashCode());
            hashResult = prime * hashResult
                    + ((this.result == null) ? 0 : this.result.hashCode());
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
            return "Result [corpusItemid=" + this.corpusItemId + ", result="
                    + this.result + "]";
        }

    }

    /**
     * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
     *
     */
    public static class Incomplete {
        private final CorpusItemId corpusItemId;
        private final String cause;

        /**
         * @param corpusItem
         * @param cause
         */
        public Incomplete(final CorpusItemId corpusItemId, final Exception cause) {
            this.corpusItemId = corpusItemId;
            this.cause = cause.getMessage();
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
        public String getCause() {
            return this.cause;
        }

        /**
         * { @inheritDoc }
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((this.cause == null) ? 0 : this.cause.hashCode());
            result = prime
                    * result
                    + ((this.corpusItemId == null) ? 0 : this.corpusItemId
                            .hashCode());
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
            return "Incomplete [corpusItemId=" + this.corpusItemId + ", cause="
                    + this.cause + "]";
        }
    }
    
    public static class ResultComparator implements Comparator<Result> {
        @Override
        public int compare(Result firstResult, Result secondResult) {
            return new CorpusItemIdComparator().compare(firstResult.corpusItemId, secondResult.corpusItemId);
        }
    }

}
