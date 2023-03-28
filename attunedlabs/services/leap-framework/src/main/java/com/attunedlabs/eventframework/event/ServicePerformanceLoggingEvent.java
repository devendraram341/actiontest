package com.attunedlabs.eventframework.event;

import java.util.Date;

import com.attunedlabs.config.RequestContext;
//#TODO Move instance variable to LeapEvent header and param variable
public class ServicePerformanceLoggingEvent extends LeapEvent {
	private static final long serialVersionUID = 195566530802505078L;
	public static final String EVENTID="SERVICE_PERFORMANCE_LOGGING";
	
	private String serviceName;
	private Date completedDtm;
	private String executedOnIPAddr;
	
	public ServicePerformanceLoggingEvent(RequestContext reqCtx) {
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

	@Override
	public String toString() {
		
	//	return "ServiceCompletionSuccessEvent [completedDtm=" + completedDtm + "]";
		
		return "ServicePerformanceLoggingEvent [serviceName=" + serviceName
				+ ", completedDtm=" + completedDtm + ", executedOnIPAddr="
				+ executedOnIPAddr + ", "+super.toString()+"]";
	}

	
	

}
