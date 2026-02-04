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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class FailedPolicyCheck {
	private final Element node;
	private final String errorMessage;
	private final String test;

	public FailedPolicyCheck(Element node, String errorMessage, String test) {
		this.node = node;
		this.errorMessage = errorMessage;
		this.test = test;
	}

	public FailedPolicyCheck(String errorMessage) {
		this.node = null;
		this.errorMessage = errorMessage;
		this.test = null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getTest() {
		return test;
	}

	public String getNode() {
		return getNodeString();
	}
	
	public String toString() {
		if (test != null) {
			return test + "\n" + getNodeString();
		}
		return errorMessage;
	}

	private String getNodeString() {
		StringBuilder str = new StringBuilder();
		str.append("{");
		str.append("node: ");
		str.append(node.getTagName());
		str.append(", attributes: [");
		NamedNodeMap attributes = node.getAttributes();
		str.append(attributes.item(0));
		for (int i = 1; i < attributes.getLength(); i++) {
			str.append(", ");
			str.append(attributes.item(i));
		}
		str.append("]}");
		return str.toString();
	}
}
