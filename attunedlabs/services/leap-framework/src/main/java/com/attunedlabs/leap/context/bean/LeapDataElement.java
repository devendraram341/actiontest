package com.attunedlabs.leap.context.bean;

import org.json.JSONObject;

/**
 * 
 * @author Reactiveworks
 *
 */

public class LeapDataElement {

	private String apiVersion;
	private String context;
	private String lang;
	private String selfLink;
	private String id;
	private JSONObject error;
	private LeapData data;

	public LeapDataElement() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	public LeapDataElement(String apiVersion, String context, String lang, LeapData data) {
		super();
		this.apiVersion = apiVersion;
		this.context = context;
		this.lang = lang;
		this.data = data;
	}

	public LeapDataElement(String apiVersion, String context, String lang, String selfLink, String id, LeapData data) {
		super();
		this.apiVersion = apiVersion;
		this.context = context;
		this.lang = lang;
		this.selfLink = selfLink;
		this.id = id;
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

	public String getSelfLink() {
		return selfLink;
	}

	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LeapData getData() {
		return data;
	}

	public void setData(LeapData data) {
		this.data = data;
	}
	
	public JSONObject getError() {
		return error;
	}

	public void setError(JSONObject error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "LeapDataElement [apiVersion=" + apiVersion + ", context=" + context + ", lang=" + lang + ", selfLink="
				+ selfLink + ", id=" + id + ", error=" + error + ", data=" + data + "]";
	}

}
