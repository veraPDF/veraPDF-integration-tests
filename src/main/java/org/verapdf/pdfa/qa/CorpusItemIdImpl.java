/**
 * 
 */
package org.verapdf.pdfa.qa;

import org.verapdf.pdfa.flavours.PDFAFlavour.Specification;
import org.verapdf.pdfa.validation.Profiles;
import org.verapdf.pdfa.validation.Profiles.RuleIdComparator;
import org.verapdf.pdfa.validation.RuleId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Comparator;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
public class CorpusItemIdImpl implements CorpusItemId {
    private static final CorpusItemIdImpl DEFAULT = new CorpusItemIdImpl();
    private static final String SEPARATOR = "-";
    private static final String PASS = "pass";
    private static final String FAIL = "fail";
    private static final String TEST_PREFIX = "t";
    private static final String TEST_FILE_EXT = ".pdf";
    @XmlElement(name = "ruleId")
    private final RuleId ruleId;
    @XmlElement(name = "sha1")
    private final String hexSha1;
    @XmlElement(name = "testCode")
    private final String testCode;
    @XmlElement(name = "result")
    private final boolean result;
    @XmlElement(name = "testType")
    private final TestType type;

    private CorpusItemIdImpl() {
        this(Profiles.defaultRuleId(), "sha1", "testCode", false, TestType.NOT_APPLICABLE);
    }

    /**
     * @param ruleId
     * @param testCode
     * @param status
     */
    private CorpusItemIdImpl(final RuleId ruleId, final String hexSha1,
            final String testCode, final boolean result, final TestType type) {
        super();
        this.ruleId = ruleId;
        this.hexSha1 = hexSha1;
        this.testCode = testCode;
        this.result = result;
        this.type = type;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String getName() {
        return this.ruleId.getClause()
                + "-t"
                + this.ruleId.getTestNumber() + "-"
                + this.type.id + "-" + this.testCode;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public RuleId getRuleId() {
        return this.ruleId;
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
    public String getTestCode() {
        return this.testCode;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public boolean getExpectedResult() {
        return this.result;
    }
    
    /**
     * { @inheritDoc }
     */
    @Override
    public TestType getTestType() {
        return this.type;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int hashResult = 1;
        hashResult = prime * hashResult + (this.result ? 1231 : 1237);
        hashResult = prime * hashResult
                + ((this.ruleId == null) ? 0 : this.ruleId.hashCode());
        hashResult = prime * hashResult
                + ((this.testCode == null) ? 0 : this.testCode.hashCode());
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
        if (!(obj instanceof CorpusItemId))
            return false;
        CorpusItemId other = (CorpusItemId) obj;
        if (this.result != other.getExpectedResult())
            return false;
        if (this.ruleId == null) {
            if (other.getRuleId() != null)
                return false;
        } else if (!this.ruleId.equals(other.getRuleId()))
            return false;
        if (this.testCode == null) {
            if (other.getTestCode() != null)
                return false;
        } else if (!this.testCode.equals(other.getTestCode()))
            return false;
        return true;
    }

    /**
     * { @inheritDoc }
     */
    @Override
    public String toString() {
        return "CorpusItemId [ruleId=" + this.ruleId + ", testCode="
                + this.testCode + ", result=" + this.result + "]";
    }

    /**
     * @return the default CorpusItemId instance
     */
    public static CorpusItemId defaultInstance() {
        return DEFAULT;
    }

    /**
     * @param ruleId
     *            the {@link RuleId} value for the instance
     * @param testCode
     *            the test code value for the instance
     * @param result
     *            the expected boolean test fresult for the associated
     *            CorpusItem
     * @return a {@code CorpusItemId} instance initialised with the passed
     *         values
     */
    public static CorpusItemId fromValues(final RuleId ruleId,
            final String testCode, final boolean result, final String hexSha1) {
        return new CorpusItemIdImpl(ruleId, hexSha1, testCode, result, result ? TestType.PASS : TestType.FAIL);
    }

    /**
     * @param ruleId
     *            the {@link RuleId} value for the instance
     * @param testCode
     *            the test code value for the instance
     * @param result
     *            the expected boolean test fresult for the associated
     *            CorpusItem
     * @return a {@code CorpusItemId} instance initialised with the passed
     *         values
     */
    public static CorpusItemId fromValues(final RuleId ruleId,
            final String testCode, final boolean result, final String hexSha1, final TestType type) {
        return new CorpusItemIdImpl(ruleId, hexSha1, testCode, result, type);
    }

    /**
     * Parses the {@code String fileName} parameter and combines it with the
     * passed {@code Specification specification} parameter and returns a
     * {@code CorpusItemId} instance initialised from the results.
     * 
     * @param specification
     *            the {@link Specification} associated with the
     *            {@code CorpusItemId}.
     * @param fileName
     *            the file name of the corpus item
     * @return a {@code CorpusItemId} instance initialised from the passed
     *         parameters
     */
    public static CorpusItemId fromFileName(final Specification specification,
            final String fileName, final String sha1) {
        for (String part : fileName.split("/")) {
            if (part.endsWith(TEST_FILE_EXT)) {
                if (part.contains("bfo")) {
                    return fromBFOCode(specification, part, sha1);
                }
                return fromCode(specification, part, sha1);
            }
        }
        return DEFAULT;
    }

    private static CorpusItemId fromCode(final Specification specification,
            final String code, final String sha1) {
        StringBuilder builder = new StringBuilder();
        String separator = "";
        boolean status = false;
        String testCode = "";
        TestType type = TestType.NOT_APPLICABLE;
        int testNumber = 0;
        for (String part : code.split(SEPARATOR)) {
            if (part.endsWith(TEST_FILE_EXT)) {
                testCode = part.substring(0, 1);
            } else if (TestType.fromId(part) != TestType.NOT_APPLICABLE) {
                status = testPassFail(part);
                type = TestType.fromId(part);
            } else if (part.startsWith(TEST_PREFIX)) {
                testNumber = Integer.parseInt(part.substring(1));
            } else {
                if (part.contains(" ")) {
                    part = part.substring(part.lastIndexOf(" ") + 1);
                } else if (part.equalsIgnoreCase("isartor")) {
                    continue;
                }
                builder.append(separator);
                builder.append(part);
                separator = ".";
            }
        }
        RuleId ruleId = Profiles.ruleIdFromValues(specification,
                builder.toString(), testNumber);
        return CorpusItemIdImpl.fromValues(ruleId, testCode, status, sha1, type);
    }

    private static CorpusItemId fromBFOCode(final Specification specification,
            final String code, final String sha1) {
        StringBuilder builder = new StringBuilder();
        String separator = "";
        boolean status = false;
        String testCode = "a";
        int testNumber = 0;
        for (String part : code.split(SEPARATOR)) {
            if (part.endsWith(TEST_FILE_EXT) && isTestResult(part.substring(0, 4))) {
                status = testPassFail(part.substring(0, 4));
            } else if (part.startsWith(TEST_PREFIX)) {
                testNumber = Integer.parseInt(part.substring(1));
            } else if (part.startsWith("pdf")) {
                continue;
            } else if (part.startsWith("bfo")) {
                continue;
            } else {
                builder.append(separator);
                builder.append(part);
                separator = ".";
            }
        }
        RuleId ruleId = Profiles.ruleIdFromValues(specification,
                builder.toString(), testNumber);
        return CorpusItemIdImpl.fromValues(ruleId, testCode, status, sha1);
    }

    private static boolean isTestResult(final String code) {
        return code.equalsIgnoreCase(PASS) || code.equalsIgnoreCase(FAIL);
    }

    private static boolean testPassFail(final String code) {
        return code.equalsIgnoreCase(PASS);
    }

    static class Adapter extends XmlAdapter<CorpusItemIdImpl, CorpusItemId> {
        @Override
        public CorpusItemId unmarshal(CorpusItemIdImpl corpusItem) {
            return corpusItem;
        }

        @Override
        public CorpusItemIdImpl marshal(CorpusItemId corpusItem) {
            return (CorpusItemIdImpl) corpusItem;
        }
    }
    
    public static class CorpusItemIdComparator implements Comparator<CorpusItemId> {
        @Override
        public int compare(CorpusItemId firstId, CorpusItemId secondId) {
            int ruleIdResult = -100;
            try {
                ruleIdResult = new RuleIdComparator().compare(firstId.getRuleId(), secondId.getRuleId());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (ruleIdResult != 0) {
                return ruleIdResult;
            }
            int testCodeResult = firstId.getTestCode().compareToIgnoreCase(secondId.getTestCode());
            if (testCodeResult != 0) {
                return testCodeResult;
            }
            //for test files with similar names but different expected result
            return 1;
        }
    }
}