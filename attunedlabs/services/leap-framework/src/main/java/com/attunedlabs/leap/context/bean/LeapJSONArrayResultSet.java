package com.attunedlabs.leap.context.bean;

import org.json.JSONArray;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapJSONArrayResultSet implements LeapResultSet {

	private String type = "JSON";
	private JSONArray data;

	public LeapJSONArrayResultSet() {
		super();
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public JSONArray getData() {
		return this.data;
	}

	@Override
	public void setData(Object data) {
		this.data = (JSONArray) data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeapJSONResultSet [type=");
		builder.append(type);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}

}
