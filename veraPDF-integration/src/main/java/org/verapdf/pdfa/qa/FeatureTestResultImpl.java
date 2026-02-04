/*
 * This file is part of veraPDF Integration Tests, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Integration Tests is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Integration Tests as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Integration Tests as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
