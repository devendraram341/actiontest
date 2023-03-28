/**
 * 
 */
package com.attunedlabs.eas.model;

import static com.attunedlabs.eas.helper.LeapEASConstant.ACTOR_NAME_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.ACTOR_TYPE_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.CONTENT_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.ID_KEY;
import static com.attunedlabs.eas.helper.LeapEASConstant.LINK_KEY;

import org.json.JSONObject;

/**
 * @author Reactivewroks.
 *
 */
public class Actor {

	private String link;
	private String id;
	private String name;
	private String actortype;
	private Content content;

	/**
	 * 
	 */
	public Actor() {
	}

	public Actor(String id) {
		super();
		this.id = id;
	}

	public Actor(String id, String actortype) {
		super();
		this.id = id;
		this.actortype = actortype;
	}

	public Actor(String id, Content content) {
		super();
		this.id = id;
		this.content = content;
	}

	public Actor(String id, String name, String actortype) {
		super();
		this.id = id;
		this.name = name;
		this.actortype = actortype;
	}

	public Actor(String id, String link, Content content) {
		super();
		this.link = link;
		this.id = id;
		this.content = content;
	}

	public Actor(String id, String link, String name, Content content) {
		super();
		this.link = link;
		this.id = id;
		this.name = name;
		this.content = content;
	}

	/**
	 * @param link
	 * @param id
	 * @param name
	 * @param actortype
	 * @param content
	 */
	public Actor(String link, String id, String name, String actortype, Content content) {
		super();
		this.link = link;
		this.id = id;
		this.name = name;
		this.actortype = actortype;
		this.content = content;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(String link) {
		this.link = link;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the actortype
	 */
	public String getActortype() {
		return actortype;
	}

	/**
	 * @param actortype
	 *            the actortype to set
	 */
	public void setActortype(String actortype) {
		this.actortype = actortype;
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

	public static Actor createActorFromJSON(JSONObject actor) {
		Actor actorObj = new Actor();
		if (actor.has(ID_KEY)) {
			actorObj.setId(actor.getString(ID_KEY));
			if (actor.has(ACTOR_TYPE_KEY))
				actorObj.setActortype(actor.getString(ACTOR_TYPE_KEY));

			if (actor.has(ACTOR_NAME_KEY))
				actorObj.setName(actor.getString(ACTOR_NAME_KEY));

			if (actor.has(LINK_KEY))
				actorObj.setName(actor.getString(LINK_KEY));

			if (actor.has(CONTENT_KEY))
				actorObj.setContent(Content.createContentFromJSON(actor.getJSONObject(CONTENT_KEY)));

		} else {
			// Throw error

			// Generate new ID
		}
		return actorObj;
	}

	/**
	 * Method used to convert Leap Event's Actor as JSON Object.
	 * 
	 * @param actor
	 * @return Return Leap Event's Actor Object as JSON Object.
	 */
	public JSONObject toJSON() {
		JSONObject actorJson = null;
		if (this != null) {
			actorJson = new JSONObject(this);
		}
		return actorJson;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Actor [link=" + link + ", id=" + id + ", name=" + name + ", actortype=" + actortype + ", content="
				+ content + "]";
	}

}
