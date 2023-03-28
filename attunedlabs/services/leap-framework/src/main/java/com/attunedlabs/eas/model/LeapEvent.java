package com.attunedlabs.eas.model;

import static com.attunedlabs.eas.helper.LeapEASConstant.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eas.helper.LeapEASEventHelper;

public class LeapEvent {

	private static final Logger logger = LoggerFactory.getLogger(LeapEASEventHelper.class);
	private Actor actor;
	private String id;
	private JSONObject metadata;
	private String activityType;
	private JSONArray activityTags;
	private String updated;
	private SnapShot snapshot;
	private ObjectRequest object;

	public LeapEvent() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor which hepl's in creating Leap event as EAS event structure.
	 * 
	 * @param actor
	 * @param id
	 * @param metadata
	 * @param activityType
	 * @param activityTags
	 * @param updated
	 * @param snapshot
	 * @param object
	 */
	public LeapEvent(Actor actor, String id, JSONObject metadata, String activityType, JSONArray activityTags,
			String updated, SnapShot snapshot, ObjectRequest object) {
		super();
		this.actor = actor;
		this.id = id;
		this.metadata = metadata;
		this.activityType = activityType;
		this.activityTags = activityTags;
		this.updated = updated;
		this.snapshot = snapshot;
		this.object = object;
	}

	/**
	 * Constructor used to create Leap Event.
	 * 
	 * @param id
	 * @param metadata
	 * @param object
	 */
	public LeapEvent(String id, JSONObject metadata, ObjectRequest object) {
		super();
		this.id = id;
		this.metadata = metadata;
		this.object = object;
	}

	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JSONObject getMetadata() {
		return metadata;
	}

	public void setMetadata(JSONObject metadata) {
		this.metadata = metadata;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public JSONArray getActivityTags() {
		return activityTags;
	}

	public void setActivityTags(JSONArray activityTags) {
		this.activityTags = activityTags;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public SnapShot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(SnapShot snapshot) {
		this.snapshot = snapshot;
	}

	public ObjectRequest getObject() {
		return object;
	}

	public void setObject(ObjectRequest object) {
		this.object = object;
	}

	/**
	 * Method used to provide tenant information for the given Leap Event Class.
	 * 
	 * @param event
	 * @return Returns a Tenant info JSON Object.
	 */
	public JSONObject getTenantDetails(LeapEvent event) {
		JSONObject metaData = event.getMetadata();
		JSONObject tenantDetails = new JSONObject();
		if (metaData != null) {
			if (metaData.has(ACCOUNT_ID_KEY) & metaData.has(SITE_ID_KEY)) {
				tenantDetails.put(ACCOUNT_ID_KEY, metaData.getString(ACCOUNT_ID_KEY));
				tenantDetails.put(SITE_ID_KEY, metaData.getString(SITE_ID_KEY));
			}
			return tenantDetails;
		}
		return tenantDetails;

	}

	/**
	 * Method used to create LeapEvent Object for the given Leap Event in JSON.
	 * 
	 * @param event
	 * @return Returns LeapEvent Object.
	 */
	public static LeapEvent createLeapEventFromJSON(JSONObject event) {
		logger.debug("Inside getLeapEventFromJSON");
		LeapEvent leapEvent = new LeapEvent();
		if (LeapEASEventHelper.isLeapEvent(event)) {
			if (LeapEASEventHelper.isLeapEASEvent(event)) {
				logger.debug("Its LEAP EAS event....");
				// Mandatory fields for minimal Normal leap EAS Event.
				leapEvent.setId(event.getString(ID_KEY));
				leapEvent.setActivityType(event.getString(ACTIVITY_TYPE_KEY));
				leapEvent.setActor(Actor.createActorFromJSON(event.getJSONObject(ACTOR_KEY)));
				leapEvent.setObject(ObjectRequest.createObjectReqFromJSON(event.getJSONObject(OBJECT_KEY)));
				leapEvent.setSnapshot(SnapShot.createSnapShotFromJSON(event.getJSONObject(SNAPSHOT_KEY)));
				leapEvent.setMetadata(event.getJSONObject(METADATA_KEY));

				// If some other fields are also coming then add to Leap Event.

				if (event.has(ACTIVITY_TAGS_KEY))
					leapEvent.setActivityTags(event.getJSONArray(ACTIVITY_TAGS_KEY));
				if (event.has(UPDATED_TIME_KEY))
					leapEvent.setUpdated(event.getString(UPDATED_TIME_KEY));
				return leapEvent;
			} else {
				logger.debug("Its Leap Event....");
				// Mandatory fields for minimal Normal leap Event.
				leapEvent.setId(event.getString(ID_KEY));
				leapEvent.setMetadata(event.getJSONObject(METADATA_KEY));
				leapEvent.setObject(ObjectRequest.createObjectReqFromJSON(event.getJSONObject(OBJECT_KEY)));

				// If some other fields are also coming then add to LeapEvent.
				if (event.has(ACTOR_KEY))
					leapEvent.setActor(Actor.createActorFromJSON(event.getJSONObject(ACTOR_KEY)));
				if (event.has(SNAPSHOT_KEY))
					leapEvent.setSnapshot(SnapShot.createSnapShotFromJSON(event.getJSONObject(SNAPSHOT_KEY)));
				if (event.has(ACTIVITY_TYPE_KEY))
					leapEvent.setActivityType(event.getString(ACTIVITY_TYPE_KEY));
				if (event.has(ACTIVITY_TAGS_KEY))
					leapEvent.setActivityTags(event.getJSONArray(ACTIVITY_TAGS_KEY));
				if (event.has(UPDATED_TIME_KEY))
					leapEvent.setUpdated(UPDATED_TIME_KEY);

				return leapEvent;
			}
		}
		return leapEvent;

	}

	/**
	 * Method used to convert LeapEvent object as JSON Object.
	 * 
	 * @param event
	 * @return return Leap Event Object as JSON Object.
	 */
	public JSONObject toJSON() {
		logger.debug("Inside LeapEasEventAsJSONObject");
		JSONObject leapEventAsJsonObject = null;
		if (this != null) {
			leapEventAsJsonObject = new JSONObject(this);
			return leapEventAsJsonObject;
		}
		return leapEventAsJsonObject;

	}

}
