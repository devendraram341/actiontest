package com.attunedlabs.eas.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eas.model.Actor;
import com.attunedlabs.eas.model.Content;
import com.attunedlabs.eas.model.LeapEvent;
import com.attunedlabs.eas.model.ObjectRequest;
import com.attunedlabs.eas.model.SnapShot;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;

public class LeapEASEventHelper {
	private static final Logger logger = LoggerFactory.getLogger(LeapEASEventHelper.class);

	/**
	 * Method used to create JSON Leap Event for the given ObjectRequest Content
	 * Data and exchange used to add metadata which contains tenant information.
	 * 
	 * @param object
	 * @param exchange
	 * @return Returns JSON Leap Event.
	 */
	public static LeapEvent createJSONLeapEvent(Object object, Exchange exchange) {
		String methodName = "createJSONLeapEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapEvent event = new LeapEvent();
		JSONObject metadata = new JSONObject();
		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		// Get serviceContext to get tenant and site information.
		LeapServiceContext serviceContext = leapDataCtx.getServiceDataContext();

		ObjectRequest objectRequest = new ObjectRequest();
		event.setObject(objectRequest);

		// Set Leap ID.
		event.setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Meta Data
		metadata.put(ACCOUNT_ID_KEY, serviceContext.getTenant());
		metadata.put(SITE_ID_KEY, serviceContext.getSite());
		event.setMetadata(metadata);

		// Set ObjectReq details
		event.getObject().setId(UUID.randomUUID().toString().replace("-", ""));
		event.getObject().setContent(Content.createContent(object));

		// Set Updated Time.
		event.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant(ZoneOffset.UTC)
				.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return event;

	}

	/**
	 * Method used to create JSON Leap Event for the given ObjectRequest Content
	 * Data and meta data which contains tenant information.
	 * 
	 * @param object
	 * @param exchange
	 * @return Returns JSON Leap Event.
	 */
	public static LeapEvent createJSONLeapEvent(Object object, JSONObject metaData) {
		String methodName = "createJSONLeapEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapEvent event = new LeapEvent();

		ObjectRequest objectRequest = new ObjectRequest();
		event.setObject(objectRequest);

		// Set Leap ID.
		event.setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Meta Data
		event.setMetadata(metaData);

		// Set ObjectReq details
		event.getObject().setId(UUID.randomUUID().toString().replace("-", ""));
		event.getObject().setContent(Content.createContent(object));

		// Set Updated Time.
		event.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant(ZoneOffset.UTC)
				.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return event;

	}

	/**
	 * Method used to create JSON Leap EAS Event for the given ObjectRequest Content
	 * Data and exchange used to add meta data which contains tenant information.
	 * Actor ID is Random hence its Anonymous Actor EAS Event.
	 * 
	 * @param object
	 * @param exchange
	 * @return Returns JSON Leap EAS Event.
	 */
	public static LeapEvent createAnonymousActorEASEvent(Object objectReqData, Exchange exchange) {
		String methodName = "createAnonymousActorEASEvent";
		logger.debug("{} entered into the method {} objectReqData{}", LEAP_LOG_KEY, methodName, objectReqData);

		LeapEvent event = new LeapEvent();
		JSONObject metadata = new JSONObject();
		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext serviceContext = leapDataCtx.getServiceDataContext();

		ObjectRequest objectRequest = new ObjectRequest();
		Actor actorObj = new Actor();
		SnapShot snapShot = new SnapShot();

		// Set Leap ID.
		event.setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Actor Details.
		event.setActor(actorObj);
		event.getActor().setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Meta Data
		metadata.put(ACCOUNT_ID_KEY, serviceContext.getTenant());
		metadata.put(SITE_ID_KEY, serviceContext.getSite());
		event.setMetadata(metadata);

		// Set ObjectReq details
		event.setObject(objectRequest);
		event.getObject().setId(UUID.randomUUID().toString().replace("-", ""));
		event.getObject().setContent(Content.createContent(objectReqData));

		// Set Empty SnapShot
		event.setSnapshot(snapShot);

		// Set Updated Time.
		event.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant(ZoneOffset.UTC)
				.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return event;

	}

	/**
	 * Method used to create JSON Leap EAS Event for the given ObjectRequest Content
	 * Data and exchange used to add meta data which contains tenant information.
	 * Actor ID is provided by user hence its Non-Anonymous Actor EAS Event.
	 * 
	 * @param object
	 * @param exchange
	 * @return Returns JSON Leap EAS Event.
	 */
	public static LeapEvent createNonAnonymousActorEASEvent(Object objectReqData, String actorId,
			Object actorContentData, Exchange exchange) {
		String methodName = "createNonAnonymousActorEASEvent";
		logger.debug("{} entered into the method {} objectReqData{}", LEAP_LOG_KEY, methodName, objectReqData);

		LeapEvent event = new LeapEvent();
		JSONObject metadata = new JSONObject();
		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext serviceContext = leapDataCtx.getServiceDataContext();

		ObjectRequest objectRequest = new ObjectRequest();
		Actor actorObj = new Actor();
		SnapShot snapShot = new SnapShot();

		// Set Leap ID.
		event.setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Actor Details.
		event.setActor(actorObj);
		event.getActor().setId(actorId);
		event.getActor().setContent(Content.createContent(actorContentData));

		// Set Meta Data
		metadata.put(ACCOUNT_ID_KEY, serviceContext.getTenant());
		metadata.put(SITE_ID_KEY, serviceContext.getSite());
		event.setMetadata(metadata);

		// Set ObjectReq details
		event.setObject(objectRequest);
		event.getObject().setId(UUID.randomUUID().toString().replace("-", ""));
		event.getObject().setContent(Content.createContent(objectReqData));

		// Set Empty SnapShot
		event.setSnapshot(snapShot);

		// Set Updated Time.
		event.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant(ZoneOffset.UTC)
				.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return event;

	}

	/**
	 * Method used to create JSON Leap EAS Event for the given ObjectRequest Content
	 * Data and JSON meta Data used to add tenant information. Actor ID is provided
	 * by user hence its Non-Anonymous Actor EAS Event.
	 * 
	 * @param object
	 * @param exchange
	 * @return Returns JSON Leap EAS Event.
	 */
	public static LeapEvent createNonAnonymousActorEASEvent(Object objectReqData, String actorId,
			Object actorContentData, JSONObject metaData) {
		String methodName = "getLeapEventObjectRequestData";
		logger.debug("{} entered into the method {} actorId{}", LEAP_LOG_KEY, methodName, actorId);

		LeapEvent event = new LeapEvent();

		ObjectRequest objectRequest = new ObjectRequest();
		Actor actorObj = new Actor();
		SnapShot snapShot = new SnapShot();

		// Set Leap ID.
		event.setId(UUID.randomUUID().toString().replace("-", ""));

		// Set Actor Details.
		event.setActor(actorObj);
		event.getActor().setId(actorId);
		event.getActor().setContent(Content.createContent(actorContentData));

		// Set Meta Data
		event.setMetadata(metaData);

		// Set ObjectReq details
		event.setObject(objectRequest);
		event.getObject().setId(UUID.randomUUID().toString().replace("-", ""));
		event.getObject().setContent(Content.createContent(objectReqData));

		// Set Empty SnapShot
		event.setSnapshot(snapShot);

		// Set Updated Time.
		event.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant(ZoneOffset.UTC)
				.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return event;

	}

	public static boolean isLeapType(JSONObject jsonObject) {
		if (jsonObject.has(API_VERSION_KEY) && jsonObject.has(DATA_KEY))
			return true;
		else
			return false;
	}

	public static String getEventType(JSONObject event) {
		String eventType = null;
		if (isLeapEASEvent(event)) {
			return eventType = "LeapEASEvent";
		} else if (isLeapEvent(event)) {
			return eventType = "LeapEvent";
		}
		return eventType;
	}

	public static boolean isLeapEASEvent(JSONObject event) {
		boolean isLeapEASEvent = false;

		if (event.has(ACTOR_KEY) && event.has(OBJECT_KEY) && event.has(SNAPSHOT_KEY) && event.has(ACTIVITY_TYPE_KEY)
				&& event.has(ID_KEY) && event.has(METADATA_KEY)) {
			isLeapEASEvent = true;
			return isLeapEASEvent;
		}
		return isLeapEASEvent;
	}

	public static boolean isLeapEvent(JSONObject event) {
		boolean isLeapEvent = false;

		if (event.has(ACTOR_KEY) && event.has(OBJECT_KEY) && event.has(SNAPSHOT_KEY) && event.has(ACTIVITY_TYPE_KEY)
				&& event.has(METADATA_KEY)) {
			return isLeapEvent = true;
		} else if (event.has(OBJECT_KEY) && event.has(METADATA_KEY) && event.has(ID_KEY)) {
			return isLeapEvent = true;
		}
		return isLeapEvent;
	}

	/**
	 * Method used to get the Leap Event Object's Content Data
	 * 
	 * @param event
	 * @return Returns Event Object's Content Data or null if Data does not exist.
	 */
	public static Object getLeapEventObjectRequestData(JSONObject event) {
		String methodName = "getLeapEventObjectRequestData";
		logger.debug("{} entered into the method {} event{}", LEAP_LOG_KEY, methodName, event);
		Object contentObjData = null;
		LeapEvent leapEvent = null;

		if (isLeapEvent(event)) {
			leapEvent = LeapEvent.createLeapEventFromJSON(event);
			return leapEvent.getObject().getContent().getData();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return contentObjData;

	}

	public static String getContentType(Object object) {

		if (object != null) {
			if (object instanceof JSONObject) {
				if (LeapEASEventHelper.isLeapType((JSONObject) object)) {
					return LEAP_CONTENT_TYPE;
				} else {
					return JSON_CONTENT_TYPE;
				}

			} else if (object instanceof String) {
				if (object != null) {
					if ((object.toString().startsWith("<") && object.toString().endsWith(">")))
						return XML_CONTENT_TYPE;
				}
				return PLAIN_TEXT_CONTENT_TYPE;
			}
		}
		return PLAIN_TEXT_CONTENT_TYPE;
	}

	/**
	 * Method used to provide ContentType of the Leap Event's Object. Content.
	 * 
	 * @param event
	 * @return Returns ContentType String value or null if there is no Content Type.
	 */
	public static String getEventObjectRequestContentType(JSONObject event) {
		String methodName = "getEventObjectRequestContentType";
		logger.debug("{} entered into the method {} event{}", LEAP_LOG_KEY, methodName, event);
		LeapEvent leapEvent = null;
		String contentType = null;
		if (isLeapEvent(event)) {
			leapEvent = LeapEvent.createLeapEventFromJSON(event);
			contentType = leapEvent.getObject().getContent().getContentType();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return contentType;
	}

	// Create LeapEvent from given JSON.

	/**
	 * Method used to provide tenant information for the given JSON Leap Event.
	 * 
	 * @param event
	 * @return Returns a Tenant info JSON Object.
	 */
	public static JSONObject getTenantDetails(JSONObject event) {
		JSONObject tenantDetails = new JSONObject();
		if (isLeapEvent(event)) {

			// create a Leap
			LeapEvent leapEvent = LeapEvent.createLeapEventFromJSON(event);

			JSONObject metaData = leapEvent.getMetadata();

			if (metaData != null) {
				if (metaData.has(ACCOUNT_ID_KEY) & metaData.has(SITE_ID_KEY)) {
					tenantDetails.put(ACCOUNT_ID_KEY, metaData.getString(ACCOUNT_ID_KEY));
					tenantDetails.put(SITE_ID_KEY, metaData.getString(SITE_ID_KEY));
				}
				return tenantDetails;
			}
		}

		return tenantDetails;

	}

}
