package com.attunedlabs.leap.selflink;

public class SelfLinkResponse {

	private String id;
	private String apiVersion;
	private String context;
	private String selfLink;
	private String userId;
	private Integer statusCode;
	private String created;
	private SelfLinkData data;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getSelfLink() {
		return selfLink;
	}

	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public SelfLinkData getData() {
		return data;
	}

	public void setData(SelfLinkData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "SelfLinkResponse [id=" + id + ", apiVersion=" + apiVersion + ", context=" + context + ", selfLink="
				+ selfLink + ", userId=" + userId + ", statusCode=" + statusCode + ", created=" + created + ", data="
				+ data + "]";
	}

}
