package com.attunedlabs.leap.context.bean;

import org.json.JSONObject;

/**
 * @author Reactiveworks
 *
 */
public class LeapRequest {

	private String apiVersion;
	private String context;
	private String lang;
	private JSONObject data;
	
	

	public LeapRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LeapRequest(String apiVersion, String context, String lang, JSONObject data) {
		super();
		this.apiVersion = apiVersion;
		this.context = context;
		this.lang = lang;
		this.data = data;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "LeapRequest [apiVersion=" + apiVersion + ", context=" + context + ", lang=" + lang + ", data=" + data
				+ "]";
	}

}
