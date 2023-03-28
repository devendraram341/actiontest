package com.attunedlabs.eventframework.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LeapEvent implements Serializable {
	public static final long serialVersionUID = -8905889714275195707L;

	private String id;
	private String dispatchChannelId;
	private Map<String, Serializable> metadata;
	private Map<String, Serializable> object;

	public LeapEvent() {
	}
	
	public LeapEvent(String id) {
		metadata = new HashMap<String, Serializable>();
		object = EventFrameworkConfigHelper.getLeapEventStructure();
		this.id = id;
	}

	public LeapEvent(String eventId, RequestContext reqContext) {
		metadata = new HashMap<String, Serializable>();
		object = EventFrameworkConfigHelper.getLeapEventStructure();
		this.id = eventId;
		metadata.put(ConfigurationConstant.EVENT_CONTEXT_KEY, reqContext);
	}

	public String getId() {
		return id;
	}

	public void setId(String eventId) {
		this.id = eventId;
	}

	public String getDispatchChannelId() {
		return dispatchChannelId;
	}

	public void setDispatchChannelId(String dispatchChannelId) {
		this.dispatchChannelId = dispatchChannelId;
	}

	@JsonIgnore
	public RequestContext getRequestContext() {
		RequestContext rq = (RequestContext) metadata.get(ConfigurationConstant.EVENT_CONTEXT_KEY);
		return rq;
	}

//	private void setRequestContext(RequestContext rq) {
//		eventHeader.put(ConfigurationConstant.EVENT_CONTEXT_KEY, rq);
//	}

	public Map<String, Serializable> getObject() {
		if (object == null) {
			object = EventFrameworkConfigHelper.getLeapEventStructure();
		}
		return object;
	}

	public void setObjectContent(JSONObject json) {
		if (object == null) {
			getObject();
		}
		String serializable = object.get(EventFrameworkConstants.EVENT_OBJECT_CONTENT_KEY).toString();
		JSONObject jsonn = new JSONObject(serializable);
		jsonn.put(EventFrameworkConstants.EVENT_OBJECT_API_VERSION, "1.0");
		jsonn.put(EventFrameworkConstants.EVENT_METADATA_DATA, json);
		jsonn.put(EventFrameworkConstants.EVENT_OBJECT_CONTENT_TYPE, "json");
		object.put(EventFrameworkConstants.EVENT_OBJECT_CONTENT_KEY, jsonn.toString());
	}

	public void setEventParamMetadata(String data, String contentType) {
		if (object == null) {
			getObject();
		}
		String serializable = object.get(EventFrameworkConstants.EVENT_OBJECT_METADATA_KEY).toString();
		JSONObject jsonn = new JSONObject(serializable);
		jsonn.put(EventFrameworkConstants.EVENT_METADATA_DATA, data);
		jsonn.put(EventFrameworkConstants.EVENT_OBJECT_CONTENT_TYPE, contentType);
		object.put(EventFrameworkConstants.EVENT_OBJECT_METADATA_KEY, jsonn.toString());
	}

	public void setObjectProcessMetaData(JSONObject json) {
		if (object == null) {
			getObject();
		}
		object.put(EventFrameworkConstants.EVENT_OBJECT_PROCESSMETADATA_KEY, json.toString());
	}

	public void setObject(Map<String, Serializable> eventParam) {
		this.object = eventParam;
	}

	public Map<String, Serializable> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Serializable> eventHeaderParam) {
		this.metadata = eventHeaderParam;
	}

	public void addMetadata(String headerParamKey, Serializable value) {
		metadata.put(headerParamKey, value);
	}

	public void addObject(String eventParamKey, Serializable value) {
		object.put(eventParamKey, value);
	}

	@Override
	public String toString() {
		return "LeapEvent [id=" + id + ", dispatchChannelId=" + dispatchChannelId + ", metadata="
				+ metadata + ", object=" + object + "]";
	}

}
