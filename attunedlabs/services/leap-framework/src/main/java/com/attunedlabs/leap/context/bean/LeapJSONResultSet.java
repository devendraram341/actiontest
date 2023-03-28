package com.attunedlabs.leap.context.bean;

import org.json.JSONObject;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapJSONResultSet implements LeapResultSet {

	private String type = "JSON";
	private JSONObject data;

	public LeapJSONResultSet() {
		super();
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public JSONObject getData() {
		return this.data;
	}

	@Override
	public void setData(Object data) {
		this.data = (JSONObject) data;
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
