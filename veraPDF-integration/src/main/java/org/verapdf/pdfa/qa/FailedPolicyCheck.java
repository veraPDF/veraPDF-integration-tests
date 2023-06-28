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
