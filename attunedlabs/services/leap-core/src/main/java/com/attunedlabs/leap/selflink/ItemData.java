package com.attunedlabs.leap.selflink;

import java.util.Map;

public class ItemData {
	private Map<String, Object> request;
	private Map<String, Object> profiling;
	private Map<String, Object> response;

	public Map<String, Object> getRequest() {
		return request;
	}

	public void setRequest(Map<String, Object> request) {
		this.request = request;
	}

	public Map<String, Object> getProfiling() {
		return profiling;
	}

	public void setProfiling(Map<String, Object> profiling) {
		this.profiling = profiling;
	}

	public Map<String, Object> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Object> response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "ItemData [request=" + request + ", profiling=" + profiling + ", response=" + response + "]";
	}

}
