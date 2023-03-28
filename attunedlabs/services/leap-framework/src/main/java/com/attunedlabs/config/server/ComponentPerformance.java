package com.attunedlabs.config.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO created to keep component data for specific request.
 * 
 * @author Reactiveworks42
 *
 */
public class ComponentPerformance implements Serializable {
	private static final long serialVersionUID = 3057141913929469210L;
	private String requestUUID;
	private List<Object> endpoint;
	private List<Object> timeTaken;

	public ComponentPerformance(String requestUUID) {
		super();
		this.requestUUID = requestUUID;
		this.endpoint = new ArrayList<>();
		this.timeTaken = new ArrayList<>();
	}

	/**
	 * @return the requestUUID
	 */
	public String getRequestUUID() {
		return requestUUID;
	}

	/**
	 * @return the endpoint
	 */
	public List<Object> getEndpoint() {
		return endpoint;
	}

	/**
	 * @return the timeTaken
	 */
	public List<Object> getTimeTaken() {
		return timeTaken;
	}

	

	/**
	 * adds the time taken by the component to Component Performance.
	 * 
	 * @param endpointKey
	 * @param timeTakenByEndpoint
	 */
	public void addNewComponent(String endpointKey, String timeTakenByEndpoint) {
		endpoint.add(endpointKey);
		timeTaken.add(timeTakenByEndpoint);

	}

	/**
	 * use this to add internal calls taken by endpoint.
	 * 
	 * @return the timeTaken
	 */
	public List<String> createAndGetInsideCallsForEndpoint() {
		List<String> insideCallsOfComponent = new ArrayList<String>();
		endpoint.add(insideCallsOfComponent);
		return insideCallsOfComponent;
	}

	/**
	 * use this to add internal call time taken by endpoint.
	 * 
	 * 
	 * @return the timeTaken
	 */
	public List<String> createAndGetInsideCallTimeForEndpoint() {
		List<String> insideCallTimeOfComponent = new ArrayList<String>();
		timeTaken.add(insideCallTimeOfComponent);
		return insideCallTimeOfComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "ComponentPerformance [requestUUID=" + requestUUID + ", endpoint=" + endpoint
				+ ", timeTaken=" + timeTaken + "]";
	}

}
