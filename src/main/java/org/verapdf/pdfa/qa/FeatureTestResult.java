package org.verapdf.pdfa.qa;

/**
 * @author Sergey Shemyakov
 */
public interface FeatureTestResult {

    boolean getErrorPresent();

    String getEqual();

    String getFileName();

    String getFeatureType();

    String getExceptionMessage();
}
