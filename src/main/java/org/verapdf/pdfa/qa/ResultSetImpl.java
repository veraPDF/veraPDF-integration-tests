/**
 * 
 */
package org.verapdf.pdfa.qa;

import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public class ResultSetImpl implements ResultSet {
    @XmlElement(name = "resultDetails")
    private final ResultSetDetails details = ResultSetDetailsImpl
            .getNewInstance();
    @XmlElement(name = "corpusDetails")
    private final CorpusDetails corpusDetails;
    @XmlElement(name = "profile")
    private final ValidationProfile profile;
    @XmlElement(name = "summary")
    private final ResultSetSummary summary;
    @XmlElementWrapper
    @XmlElement(name = "result")
    private final SortedSet<Result> results;
    @XmlElementWrapper
    @XmlElement(name = "exception")
    private final Set<Incomplete> exceptions;

    private ResultSetImpl(final CorpusDetails corpusDetails,
            final ValidationProfile profile, final Set<Result> results,
            final Set<Incomplete> exceptions) {
        this.corpusDetails = corpusDetails;
        this.profile = profile;
        this.results = new TreeSet<>(new ResultComparator());
        this.results.addAll(results);
        this.summary = ResultSetSummaryImpl.fromResults(results, exceptions);
        this.exceptions = new HashSet<>(exceptions);
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public ResultSetDetails getDetails() {
        return this.details;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public CorpusDetails getCorpusDetails() {
        return this.corpusDetails;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public ValidationProfile getValidationProfile() {
        return this.profile;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public ResultSetSummary getSummary() {
        return this.summary;
    }
    
    /**
     * { @inheritDoc }
     */
    @Override
    public Set<Result> getResults() {
        return this.results;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public Set<Incomplete> getExceptions() {
        return this.exceptions;
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
                + ((this.corpusDetails == null) ? 0 : this.corpusDetails
                        .hashCode());
        result = prime * result
                + ((this.details == null) ? 0 : this.details.hashCode());
        result = prime * result
                + ((this.profile == null) ? 0 : this.profile.hashCode());
        result = prime * result
                + ((this.results == null) ? 0 : this.results.hashCode());
        result = prime * result
                + ((this.exceptions == null) ? 0 : this.results.hashCode());
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
        if (!(obj instanceof ResultSet))
            return false;
        ResultSet other = (ResultSet) obj;
        if (this.corpusDetails == null) {
            if (other.getCorpusDetails() != null)
                return false;
        } else if (!this.corpusDetails.equals(other.getCorpusDetails()))
            return false;
        if (this.details == null) {
            if (other.getDetails() != null)
                return false;
        } else if (!this.details.equals(other.getDetails()))
            return false;
        if (this.profile == null) {
            if (other.getValidationProfile() != null)
                return false;
        } else if (!this.profile.equals(other.getValidationProfile()))
            return false;
        if (this.results == null) {
            if (other.getResults() != null)
                return false;
        } else if (!this.results.equals(other.getResults()))
            return false;
        if (this.exceptions == null) {
            if (other.getExceptions() != null)
                return false;
        } else if (!this.exceptions.equals(other.getExceptions()))
            return false;
        return true;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String toString() {
        return "ResultSet [details=" + this.details + ", corpusDetails="
                + this.corpusDetails + ", profile=" + this.profile
                + ", results=" + this.results + ", exceptions="
                + this.exceptions + "]";
    }

    /**
     * @param corpus
     * @param validator
     * @return
     */
    public static ResultSet validateCorpus(final TestCorpus corpus,
            final PDFAValidator validator) {
        Set<Result> results = new HashSet<>();
        Set<Incomplete> exceptions = new HashSet<>();
        for (String itemName : corpus.getItemNames()) {
            CorpusItemId id = null;
            try {
                id = CorpusItemIdImpl.fromFileName(validator.getProfile()
                        .getPDFAFlavour().getPart(), itemName, "");
            } catch (IllegalArgumentException excep) {
                excep.printStackTrace();
            }
            if (id != null) {
                try (ModelParser loader = ModelParser.createModelWithFlavour(
                        corpus.getItemStream(itemName), validator.getProfile()
                                .getPDFAFlavour())) {
                    ValidationResult result = validator.validate(loader);
                    results.add(new Result(id, result));
                } catch (Exception e) {
                	e.printStackTrace();
                	e.getCause().printStackTrace();
                    exceptions.add(new Incomplete(id, e));
                }
            }
        }
        return new ResultSetImpl(corpus.getDetails(), validator.getProfile(),
                results, exceptions);
    }

    static class Adapter extends XmlAdapter<ResultSetImpl, ResultSet> {
        @Override
        public ResultSet unmarshal(ResultSetImpl results) {
            return results;
        }

        @Override
        public ResultSetImpl marshal(ResultSet results) {
            return (ResultSetImpl) results;
        }
    }
}
