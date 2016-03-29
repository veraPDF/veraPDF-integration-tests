/**
 * 
 */
package org.verapdf.pdfa.qa;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.verapdf.pdfa.validation.RuleId;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@XmlJavaTypeAdapter(CorpusItemIdImpl.Adapter.class)
public interface CorpusItemId {
    public static final String PASS_ID = "pass";
    public static final String FAIL_ID = "fail";
    public static final String UNDEFINED_ID = "undefined";
    public static final String NOT_APPLICABLE_ID = "na";

    /**
     * @return the full name of the corpus item
     */
    public String getName();

    /**
     * @return the RuleId
     */
    public RuleId getRuleId();

    /**
     * @return the single character test code for the corpus item
     */
    public String getTestCode();

    /**
     * @return the single character test code for the corpus item
     */
    public String getHexSha1();

    /**
     * @return the expected result
     */
    public boolean getExpectedResult();

    /**
     * @return the {@link TestTyoe} for the corpus item
     */
    public TestType getTestType();

    public enum TestType {
        PASS(CorpusItemId.PASS_ID), FAIL(CorpusItemId.FAIL_ID), UNDEFINED(
                CorpusItemId.UNDEFINED_ID), NOT_APPLICABLE(
                CorpusItemId.NOT_APPLICABLE_ID);

        final String id;

        TestType(final String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public final static TestType fromId(final String id) {
            for (TestType type : TestType.values()) {
                if (id.equalsIgnoreCase(type.id)) {
                    return type;
                }
            }
            return TestType.NOT_APPLICABLE;
        }
    }

}
