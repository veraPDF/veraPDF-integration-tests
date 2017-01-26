/**
 * This file is part of veraPDF Quality Assurance, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Quality Assurance is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Quality Assurance as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Quality Assurance as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.verapdf.pdfa.qa;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.verapdf.pdfa.validation.profiles.RuleId;

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
