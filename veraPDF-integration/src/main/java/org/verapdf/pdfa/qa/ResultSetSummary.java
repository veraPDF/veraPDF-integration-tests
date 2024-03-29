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

import org.verapdf.component.AuditDuration;

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
    public AuditDuration getDuration();
    public long getMemoryUsed();
}
