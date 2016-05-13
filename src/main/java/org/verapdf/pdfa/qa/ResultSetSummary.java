/**
 * 
 */
package org.verapdf.pdfa.qa;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author  <a href="mailto:carl@openpreservation.org">Carl Wilson</a>.
 *
 */
@XmlJavaTypeAdapter(ResultSetSummaryImpl.Adapter.class)
public interface ResultSetSummary {
    public String getPassed();
    public int invalidCases();
    public int invalidCasesFailed();
    public int invalidCasesPassed();
    public int validCases();
    public int validCasesFailed();
    public int validCasesPassed();
    public int undefinedCases();
    public int inapplicableCases();
    public int exceptions();
}
