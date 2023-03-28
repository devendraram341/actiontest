package com.attunedlabs.eventframework.event;

import java.util.Date;

import com.attunedlabs.config.RequestContext;
//#TODO Move from instance variable to event header and event param in the base Leap
public class ServiceCompletionFailureEvent extends LeapEvent {
	private static final long serialVersionUID = 195566530802505078L;
	public static final String EVENTID="SERVICE_COMPLETION_FAILURE";
	
	private String serviceName;
	private Date completedDtm;
	private String executedOnIPAddr;
	private String failedRouteId;
	
	public ServiceCompletionFailureEvent(RequestContext reqCtx) {
		super(EVENTID,reqCtx);
	}


	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Date getCompletedDtm() {
		return completedDtm;
	}

	public void setCompletedDtm(Date completedDtm) {
		this.completedDtm = completedDtm;
	}

	public String getExecutedOnIPAddr() {
		return executedOnIPAddr;
	}

	public void setExecutedOnIPAddr(String executedOnIPAddr) {
		this.executedOnIPAddr = executedOnIPAddr;
	}

	public String getFailedRouteId() {
		return failedRouteId;
	}

	public void setFailedRouteId(String failedRouteId) {
		this.failedRouteId = failedRouteId;
	}

	@Override
	public String toString() {
		return "ServiceCompletionFailureEvent [Failed to complete the process :  completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", failedRouteId=" + failedRouteId + "]";		
		/*return "ServiceCompletionFailureEvent [serviceName=" + serviceName
				+ ", completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", failedRouteId=" + failedRouteId + ", "+super.toString()+"]";*/
	}


	
	

}
