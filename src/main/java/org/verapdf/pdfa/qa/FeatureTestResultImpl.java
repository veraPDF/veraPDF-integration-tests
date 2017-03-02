package org.verapdf.pdfa.qa;

import org.verapdf.features.FeatureObjectType;

/**
 * @author Sergey Shemyakov
 */
public class FeatureTestResultImpl implements FeatureTestResult {

    private Throwable error;
    private String fileName;
    private FeatureObjectType featureType;


    public FeatureTestResultImpl(Throwable error,
                                 String fileName, FeatureObjectType featureType) {
        this.error = error;
        this.fileName = fileName;
        this.featureType = featureType;
    }

    @Override
    public boolean getErrorPresent() {
        return error != null;
    }

    @Override
    public String getEqual() {
        return error == null ? "pass" : "fail";
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFeatureType() {
        return featureType.getFullName();
    }

    @Override
    public String getExceptionMessage() {
        return error == null ? "" : error.getMessage();
    }
}
