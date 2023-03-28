/**
 * 
 */
package com.attunedlabs.eas.model;

import static com.attunedlabs.eas.helper.LeapEASConstant.CONTENT_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.METADATA_KEY;

import org.json.JSONObject;

/**
 * @author bizruntime44
 *
 */
public class SnapShot {

	private Content content;
	private Content metaData;

	/**
	 * 
	 */
	public SnapShot() {
	}

	public SnapShot(Content content) {
		super();
		this.content = content;
	}

	/**
	 * @param content
	 * @param metaData
	 */
	public SnapShot(Content content, Content metaData) {
		super();
		this.content = content;
		this.metaData = metaData;
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
	 * Method used to convert Leap Event's SnapShot as JSON Object.
	 * 
	 * @param actor
	 * @return Return Leap Event's SnapShot Object as JSON Object.
	 */
	public JSONObject toJSON() {
		JSONObject snapShotJson = null;
		if (this != null) {
			snapShotJson = new JSONObject(this);
		}
		return snapShotJson;
	}

	public static SnapShot createSnapShotFromJSON(JSONObject snapshotJSON) {
		SnapShot snapShotObj = null;
		if (snapshotJSON != null) {
			snapShotObj = new SnapShot();
			if (snapshotJSON.has(METADATA_KEY))
				snapShotObj.setMetaData(Content.createContentFromJSON(snapshotJSON.getJSONObject(METADATA_KEY)));
			if (snapshotJSON.has(CONTENT_KEY))
				snapShotObj.setContent(Content.createContentFromJSON(snapshotJSON.getJSONObject(CONTENT_KEY)));
		}

		return snapShotObj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SnapShot [content=" + content + ", metaData=" + metaData + "]";
	}

}
