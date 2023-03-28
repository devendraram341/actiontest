/**
 * 
 */
package com.attunedlabs.eas.model;

import static com.attunedlabs.eas.helper.LeapEASConstant.CONTENT_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.ID_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.METADATA_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.OBJECT_TYPE_KEY;

import org.json.JSONObject;

/**
 * @author bizruntime44
 *
 */
public class ObjectRequest {

	private String objectType;
	private String id;
	private JSONObject processMetaData;
	private Content content;
	private Content metaData;

	public JSONObject getProcessMetaData() {
		return processMetaData;
	}

	public void setProcessMetaData(JSONObject processMetaData) {
		this.processMetaData = processMetaData;
	}

	/**
	 * 
	 */
	public ObjectRequest() {

	}

	/**
	 * @param objectType
	 * @param id
	 * @param content
	 */
	public ObjectRequest(String objectType, String id, Content content, Content metaData) {
		super();
		this.objectType = objectType;
		this.id = id;
		this.content = content;
		this.metaData = metaData;
	}

	/**
	 * @param objectType
	 * @param id
	 * @param content
	 */
	public ObjectRequest(String objectType, String id, JSONObject processMetaData, Content content, Content metaData) {
		super();
		this.objectType = objectType;
		this.id = id;
		this.content = content;
		this.metaData = metaData;
	}

	/**
	 * @param objectType
	 * @param id
	 * @param content
	 */
	public ObjectRequest(String objectType, String id, Content content) {
		super();
		this.objectType = objectType;
		this.id = id;
		this.content = content;
	}

	public ObjectRequest(String objectType, String id) {
		super();
		this.objectType = objectType;
		this.id = id;
	}

	public ObjectRequest(String id, Content content) {
		super();
		this.id = id;
		this.content = content;
	}

	public ObjectRequest(String id) {
		super();
		this.id = id;
	}

	/**
	 * @return the objectType
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the content
	 */
	public Content getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Content content) {
		this.content = content;
	}

	/**
	 * @return the metaData
	 */
	public Content getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData
	 *            the metaData to set
	 */
	public void setMetaData(Content metaData) {
		this.metaData = metaData;
	}

	/**
	 * Method used to convert Leap Event's ObjectRequest as JSON Object.
	 * 
	 * @param actor
	 * @return Return Leap Event's ObjectRequest Object as JSON Object.
	 */
	public JSONObject toJSON() {
		JSONObject objectJson = null;
		if (this != null) {
			objectJson = new JSONObject(this);
		}
		return objectJson;
	}

	public static ObjectRequest createObjectReqFromJSON(JSONObject objectRequestJson) {
		ObjectRequest objectRequest = new ObjectRequest();
		if (objectRequestJson.has(ID_KEY)) {
			objectRequest.setId(objectRequestJson.getString(ID_KEY));
			if (objectRequestJson.has(OBJECT_TYPE_KEY))
				objectRequest.setObjectType(objectRequestJson.getString(OBJECT_TYPE_KEY));

			if (objectRequestJson.has(METADATA_KEY))
				objectRequest.setMetaData(Content.createContentFromJSON(objectRequestJson.getJSONObject(METADATA_KEY)));

			if (objectRequestJson.has(CONTENT_KEY))
				objectRequest.setContent(Content.createContentFromJSON(objectRequestJson.getJSONObject(CONTENT_KEY)));

		} else {
			// Throw error

			// Generate new ID
		}
		return objectRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectRequest [objectType=" + objectType + ", id=" + id + ", content=" + content + ", metaData="
				+ metaData + "]";
	}

}
