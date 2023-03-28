package com.attunedlabs.leap.context.bean;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapDataContextElement {

	private String tagName;
	private LeapDataElement dataElementObject;

	public LeapDataContextElement() {
		super();
	}

	public LeapDataContextElement(String tagName, LeapDataElement dataElementObject) {
		super();
		this.tagName = tagName;
		this.dataElementObject = dataElementObject;
	}

	public String getTagName() {
		return tagName;
	}

	public LeapDataElement getDataElement() {
		return dataElementObject;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setDataElement(LeapDataElement dataElementObject) {
		this.dataElementObject = dataElementObject;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeapDataContextElement [");
		if (tagName != null) {
			builder.append("tagName=");
			builder.append(tagName);
			builder.append(", ");
		}
		if (dataElementObject != null) {
			builder.append("dataElementObject=");
			builder.append(dataElementObject);
		}
		builder.append("]");
		return builder.toString();
	}


	// search logic to be written

}
