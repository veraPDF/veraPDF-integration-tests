/**
 * 
 */
package org.verapdf.pdfa.qa;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@XmlJavaTypeAdapter(CorpusDetailsImpl.Adapter.class)
public interface CorpusDetails {
    /**
     * @return the name of the TestCorpus
     */
    public String getName();

    /**
     * @return a textual description of the TestCorpus
     */
    public String getDescription();
    
    /**
     * @return a unique SHA1 identifier for the corpus
     */
    public String getHexSha1();
}
