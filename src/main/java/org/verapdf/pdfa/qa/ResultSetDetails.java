/**
 * 
 */
package org.verapdf.pdfa.qa;

import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.verapdf.ReleaseDetails;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@XmlJavaTypeAdapter(ResultSetDetailsImpl.Adapter.class)
public interface ResultSetDetails {
    /**
     * @return the date that the result set was created
     */
    public Date getDateCreated();

    public Set<ReleaseDetails> getDependencies();
}
