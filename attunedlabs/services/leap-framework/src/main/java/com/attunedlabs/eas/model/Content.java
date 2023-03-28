/**
 * 
 */
package com.attunedlabs.eas.model;

import static com.attunedlabs.eas.helper.LeapEASConstant.CONTENT_TYPE_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.DATA_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.JSON_CONTENT_TYPE;
import static com.attunedlabs.eas.helper.LeapEASConstant.LEAP_CONTENT_TYPE;

import org.json.JSONObject;

import com.attunedlabs.eas.helper.LeapEASEventHelper;

/**
 * @author bizruntime44
 *
 */
public class Content {

	private String contentType;
	private Object data;

	/**
	 * 
	 */
	public Content() {
	}

	/**
	 * @param contentType
	 * @param data
	 */
	public Content(String contentType, Object data) {
		super();
		this.contentType = contentType;
		this.data = data;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	public static Content createContentFromJSON(JSONObject content) {
		Content contentObj = new Content();

		if (content.has(CONTENT_TYPE_KEY))
			contentObj.setContentType(CONTENT_TYPE_KEY);
		if (content.has(DATA_KEY))
			contentObj.setData(content.get(DATA_KEY));
		return contentObj;

	}

	/**
	 * Method used to provide Content Class.
	 * 
	 * @param contentType
	 * @param contentData
	 * @return Returns Content Class.
	 */
	public static Content createContent(Object request) {
		Content content = new Content();
		String contentType = LeapEASEventHelper.getContentType(request);
		if ((contentType != null & !contentType.isEmpty())) {
			if (contentType.equalsIgnoreCase(LEAP_CONTENT_TYPE) || contentType.equalsIgnoreCase(JSON_CONTENT_TYPE)) {
				if (request != null) {
					content.setContentType(contentType);
					content.setData(request);
					return content;
				} else {
					JSONObject actualRequest = new JSONObject();
					content.setContentType(contentType);
					content.setData(actualRequest);
					return content;
				}

			} else {
				content.setContentType(contentType);
				content.setData(request);
				return content;
			}
		}
		return content;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Content [contentType=" + contentType + ", data=" + data + "]";
	}

}
