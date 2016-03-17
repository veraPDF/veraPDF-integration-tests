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

}
