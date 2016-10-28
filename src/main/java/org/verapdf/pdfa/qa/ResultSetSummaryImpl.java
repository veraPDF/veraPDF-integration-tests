/**
 * 
 */
package org.verapdf.pdfa.qa;

import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.verapdf.component.AuditDuration;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>.
 *
 */
public final class ResultSetSummaryImpl implements ResultSetSummary {
    private final boolean isPassed;
    private final int invalidCases;
    private final int invalidCasesPassed;
    private final int invalidCasesFailed;
    private final int validCases;
    private final int validCasesPassed;
    private final int validCasesFailed;
    private final int undefinedCases;
    private final int inapplicableCases;
    private final int exceptions;
    private final AuditDuration duration;
    private final long memoryUsed;

    private ResultSetSummaryImpl(final boolean isPassed,
            final int invalidCases,final int invalidCasesFailed, final int validCases, final int validCasesFailed,
            final int undefinedCases, final int inapplicableCases,
            final int exceptions, final AuditDuration duration, final long memoryUsed) {
        super();
        this.isPassed = isPassed;
        this.invalidCases = invalidCases;
        this.invalidCasesPassed = invalidCases - invalidCasesFailed;
        this.invalidCasesFailed = invalidCasesFailed;
        this.validCases = validCases;
        this.validCasesPassed = validCases - validCasesFailed;
        this.validCasesFailed = validCasesFailed;
        this.undefinedCases = undefinedCases;
        this.inapplicableCases = inapplicableCases;
        this.exceptions = exceptions;
        this.duration = duration;
        this.memoryUsed = memoryUsed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#isPassed()
     */
    @Override
    public String getPassed() {
        return this.isPassed ? "pass" : "fail";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#failedTestCount()
     */
    @Override
    public int invalidCases() {
        return this.invalidCases;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#failedTestCount()
     */
    @Override
    public int invalidCasesPassed() {
        return this.invalidCasesPassed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#failedTestCount()
     */
    @Override
    public int invalidCasesFailed() {
        return this.invalidCasesFailed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#passedTestCount()
     */
    @Override
    public int validCases() {
        return this.validCases;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#failedTestCount()
     */
    @Override
    public int validCasesPassed() {
        return this.validCasesPassed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#failedTestCount()
     */
    @Override
    public int validCasesFailed() {
        return this.validCasesFailed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#undefinedTestCount()
     */
    @Override
    public int undefinedCases() {
        return this.undefinedCases;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#inapplicableTestCount()
     */
    @Override
    public int inapplicableCases() {
        return this.inapplicableCases;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.verapdf.pdfa.qa.ResultSetSummary#exceptionCount()
     */
    @Override
    public int exceptions() {
        return this.exceptions;
    }

    public static ResultSetSummary fromResults(final Set<ResultSet.Result> results,
            final Set<ResultSet.Incomplete> incompletes, final AuditDuration duration, final long memoryUsed) {
        int validCases = 0;
        int validFailed = 0;
        int invalidCases = 0;
        int invalidFailed = 0;
        int undefinedCases = 0;
        int inapplicableTestCount = 0;
        int exceptions = incompletes.size();

        for (ResultSet.Result result : results) {
            switch (result.getCorpusItemId().getTestType()) {
            case PASS:
                validCases++;
                if (!result.getResult().isCompliant()) validFailed++; 
                break;
            case FAIL:
                invalidCases++;
                if (result.getResult().isCompliant()) invalidFailed++; 
                break;
            case UNDEFINED:
                undefinedCases++;
                break;
            case NOT_APPLICABLE:
                inapplicableTestCount++;
                break;
            default:
                break;
            }
        }

        return new ResultSetSummaryImpl((validFailed == 0 && invalidFailed == 0 && exceptions == 0), invalidCases, invalidFailed, validCases, validFailed,
                undefinedCases, inapplicableTestCount, exceptions, duration, memoryUsed);
    }

    static class Adapter extends
            XmlAdapter<ResultSetSummaryImpl, ResultSetSummary> {
        @Override
        public ResultSetSummary unmarshal(ResultSetSummaryImpl summary) {
            return summary;
        }

        @Override
        public ResultSetSummaryImpl marshal(ResultSetSummary summary) {
            return (ResultSetSummaryImpl) summary;
        }
    }

	@Override
	public AuditDuration getDuration() {
		// TODO Auto-generated method stub
		return duration;
	}
	
	@Override
	public long getMemoryUsed() {
		return this.memoryUsed;
	}

}
