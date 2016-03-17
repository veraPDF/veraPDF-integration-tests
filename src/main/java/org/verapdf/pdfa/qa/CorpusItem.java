/**
 * 
 */
package org.verapdf.pdfa.qa;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Interface that encapsulates a basic corpus item.
 * 
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *
 */
@XmlJavaTypeAdapter(CorpusItemImpl.Adapter.class)
public interface CorpusItem {
    /**
     * @return
     */
    public CorpusItemId getId();

    /**
     * @return the {@code String} path of the item
     */
    public String getPath();

    /**
     * @return the SHA-1 digest of the item, if known, an empty string if not
     *         known
     */
    public String getSha1();
}
