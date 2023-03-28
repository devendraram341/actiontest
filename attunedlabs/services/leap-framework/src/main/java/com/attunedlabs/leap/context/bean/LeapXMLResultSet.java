package com.attunedlabs.leap.context.bean;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapXMLResultSet implements LeapResultSet{

	private String type = "XML";
	private String data;

	public LeapXMLResultSet() {
		super();
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public String getData() {
		return this.data;
	}

	@Override
	public void setData(Object data) {
		String xml = data.toString();
		this.data = xml;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeapXMLResultSet [type=");
		builder.append(type);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}
	

}
