package com.attunedlabs.eventframework.eventtracker;

import java.io.Serializable;
import java.util.Date;

/**
 * <code>EventDispatcherTracker</code> keeps track of all the dispatching
 * eventingList.
 * 
 * @author Reactiveworks42
 *
 */
public class EventDispatcherTracker implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tenantId;
	private String siteId;
	private String requestId;
	private String eventStoreId;
	private Date eventCreatedDTM;
	private Date lastFailureDTM;
	private String status;
	private String failureReason;
	private Integer retryCount;
	private String leapEventId;
	private String leapEvent;
    private String dispatchChannelId;
	
	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * @param tenantId the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * @return the siteId
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the eventStoreId
	 */
	public String getEventStoreId() {
		return eventStoreId;
	}

	/**
	 * @param eventStoreId the eventStoreId to set
	 */
	public void setEventStoreId(String eventStoreId) {
		this.eventStoreId = eventStoreId;
	}

	/**
	 * @return the eventCreatedDTM
	 */
	public Date getEventCreatedDTM() {
		return eventCreatedDTM;
	}

	/**
	 * @param eventCreatedDTM the eventCreatedDTM to set
	 */
	public void setEventCreatedDTM(Date eventCreatedDTM) {
		this.eventCreatedDTM = eventCreatedDTM;
	}

	/**
	 * @return the lastFailureDTM
	 */
	public Date getLastFailureDTM() {
		return lastFailureDTM;
	}

	/**
	 * @param lastFailureDTM the lastFailureDTM to set
	 */
	public void setLastFailureDTM(Date lastFailureDTM) {
		this.lastFailureDTM = lastFailureDTM;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the failureReason
	 */
	public String getFailureReason() {
		return failureReason;
	}

	/**
	 * @param failureReason the failureReason to set
	 */
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	/**
	 * @return the retryCount
	 */
	public Integer getRetryCount() {
		return retryCount;
	}

	/**
	 * @param retryCount the retryCount to set
	 */
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * @return the leapEventId
	 */
	public String getLeapEventId() {
		return leapEventId;
	}

	/**
	 * @param leapEventId the leapEventId to set
	 */
	public void setLeapEventId(String leapEventId) {
		this.leapEventId = leapEventId;
	}

	public String getLeapEvent() {
		return leapEvent;
	}

	public void setLeapEvent(String leapEvent) {
		this.leapEvent = leapEvent;
	}

	
	public String getDispatchChannelId() {
		return dispatchChannelId;
	}

	public void setDispatchChannelId(String dispatchChannelId) {
		this.dispatchChannelId = dispatchChannelId;
	}

	@Override
	public String toString() {
		return "EventDispatcherTracker [tenantId=" + tenantId + ", siteId=" + siteId + ", requestId=" + requestId
				+ ", eventStoreId=" + eventStoreId + ", eventCreatedDTM=" + eventCreatedDTM + ", lastFailureDTM="
				+ lastFailureDTM + ", status=" + status + ", failureReason=" + failureReason + ", retryCount="
				+ retryCount + ", leapEventId=" + leapEventId + ", leapEvent=" + leapEvent + ", dispatchChannelId="
				+ dispatchChannelId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dispatchChannelId == null) ? 0 : dispatchChannelId.hashCode());
		result = prime * result + ((eventCreatedDTM == null) ? 0 : eventCreatedDTM.hashCode());
		result = prime * result + ((eventStoreId == null) ? 0 : eventStoreId.hashCode());
		result = prime * result + ((failureReason == null) ? 0 : failureReason.hashCode());
		result = prime * result + ((lastFailureDTM == null) ? 0 : lastFailureDTM.hashCode());
		result = prime * result + ((leapEvent == null) ? 0 : leapEvent.hashCode());
		result = prime * result + ((leapEventId == null) ? 0 : leapEventId.hashCode());
		result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
		result = prime * result + ((retryCount == null) ? 0 : retryCount.hashCode());
		result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventDispatcherTracker other = (EventDispatcherTracker) obj;
		if (dispatchChannelId == null) {
			if (other.dispatchChannelId != null)
				return false;
		} else if (!dispatchChannelId.equals(other.dispatchChannelId))
			return false;
		if (eventCreatedDTM == null) {
			if (other.eventCreatedDTM != null)
				return false;
		} else if (!eventCreatedDTM.equals(other.eventCreatedDTM))
			return false;
		if (eventStoreId == null) {
			if (other.eventStoreId != null)
				return false;
		} else if (!eventStoreId.equals(other.eventStoreId))
			return false;
		if (failureReason == null) {
			if (other.failureReason != null)
				return false;
		} else if (!failureReason.equals(other.failureReason))
			return false;
		if (lastFailureDTM == null) {
			if (other.lastFailureDTM != null)
				return false;
		} else if (!lastFailureDTM.equals(other.lastFailureDTM))
			return false;
		if (leapEvent == null) {
			if (other.leapEvent != null)
				return false;
		} else if (!leapEvent.equals(other.leapEvent))
			return false;
		if (leapEventId == null) {
			if (other.leapEventId != null)
				return false;
		} else if (!leapEventId.equals(other.leapEventId))
			return false;
		if (requestId == null) {
			if (other.requestId != null)
				return false;
		} else if (!requestId.equals(other.requestId))
			return false;
		if (retryCount == null) {
			if (other.retryCount != null)
				return false;
		} else if (!retryCount.equals(other.retryCount))
			return false;
		if (siteId == null) {
			if (other.siteId != null)
				return false;
		} else if (!siteId.equals(other.siteId))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} else if (!tenantId.equals(other.tenantId))
			return false;
		return true;
	}

	

	

}
