package com.attunedlabs.eventframework.dispatcher;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.channel.AbstractDispatchChannel;
import com.attunedlabs.eventframework.dispatcher.channel.DispatchChannelService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.eventtracker.util.EventTrackerUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;

/**
 * @author Reactiveworks
 *
 */
public class LeapEventDispatchTask implements Runnable, Serializable, HazelcastInstanceAware {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(LeapEventDispatchTask.class);

	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	private transient HazelcastInstance hazelcastInstance;

	private LeapEvent leapEvt;
	private String channelId;
	private Serializable transformedMsg;
	private LeapExchangeHeader header;
	private EventDispatcherTracker tracker;
	private String processingWay;

	/**
	 * LeapEventDispatchTask object creation for normal Event dispatching.
	 * 
	 * @param leapEvt
	 * @param channelId
	 * @param transformedMsg
	 * @param header
	 */
	public LeapEventDispatchTask(LeapEvent leapEvt, String channelId, Serializable transformedMsg,
			LeapExchangeHeader header) {
		this.leapEvt = leapEvt;
		this.channelId = channelId;
		this.transformedMsg = transformedMsg;
		this.header = header;
	}

	/**
	 * LeapEventDispatchTask object creation for failed Event dispatching.
	 * 
	 * @param leapEvt
	 * @param channelId
	 * @param transformedMsg
	 * @param header
	 * @param tracker
	 */
	public LeapEventDispatchTask(LeapEvent leapEvt, String channelId, Serializable transformedMsg,
			LeapExchangeHeader header, EventDispatcherTracker tracker) {
		this(leapEvt, channelId, transformedMsg, header);
		this.tracker = tracker;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	/**
	 * dispatching to channel task given specific amount of time.
	 * 
	 * @throws LeapEventTransformationException
	 * @throws EventDispatcherTrackerException
	 * 
	 * @throws EventFrameworkDispatcherException
	 * 
	 */
	@Override
	public void run() {

		// getting status and setting processing way.
		String status = getStatusAndSetProcessingWay();

		// fetching basic details from the header.
		String tenantId = header.getTenant();
		String siteId = header.getSite();
		String requestId = header.getRequestUUID();
		String eventStoreKey = header.getRequestUUID();
		String leapEventId = leapEvt.getId();
		try {
			String value = "leapEventId : " + leapEventId + ", tenant : " + tenantId + ", site : " + siteId
					+ ", requestId : " + requestId + ", :dispatchChannelId : " + channelId;
			logger.debug("{} Dispatching event for {}", LEAP_LOG_KEY, value);

			// dispatching the event and updating the database.
			boolean dispatchStatus = dispatchAndUpdateDB(status, tenantId, siteId, requestId, eventStoreKey,
					leapEventId, transformedMsg);
			logger.debug("{} dispatchStatus ::{} ", LEAP_LOG_KEY, dispatchStatus);
			if (dispatchStatus) {
				eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
						EventTrackerTableConstants.STATUS_COMPLETE, leapEventId, false, null, false, false, channelId);
				logger.debug("{} removing event record after dispatching is COMPLETE for {}, chanlId :{}", LEAP_LOG_KEY,
						leapEventId, channelId);
				boolean removedTrackRecord = eventDispatcherTrackerService.removeEventTrackRecord(tenantId, siteId,
						requestId, eventStoreKey, channelId, leapEventId);
				logger.debug("{} removed from DB  :{} , eventId :{}", LEAP_LOG_KEY, removedTrackRecord, leapEventId);
			}
		} catch (EventDispatcherTrackerException e) {
			logger.error("Exception Occured while dispatching task due to {} {}", LEAP_LOG_KEY, e.getMessage());
		}
	}// end of method

	private String getStatusAndSetProcessingWay() {

		// setting processing way.
		if (processingWay == null)
			processingWay = EventTrackerTableConstants.PROCESSING_WAY_ASYNC;

		String status = EventTrackerTableConstants.STATUS_FAILED;
		if (header.isRetryCall())
			status = EventTrackerTableConstants.STATUS_RETRY_FAILED;
		return status;
	}// end of method

	private boolean dispatchAndUpdateDB(String status, String tenantId, String siteId, String requestId,
			String eventStoreKey, String leapEventId, Serializable transformedMsg)
			throws EventDispatcherTrackerException {

		JSONObject failureJson = new JSONObject();
		boolean isRetried = false;
		boolean dispatchStatus = false;
		try {
			// Fetching dispatch channel service.
			DispatchChannelService disChannelSer = DispatchChannelService.getDispatchChannelService();

			logger.debug("{} Dispatch Channel Service {}", LEAP_LOG_KEY, disChannelSer.toString());
			// Getting the dispatcher channel using the channel Id.
			AbstractDispatchChannel channel = disChannelSer.getDispatchChannel(leapEvt.getRequestContext(), channelId);
			logger.debug("{} after AbstractDispatchChannel ", LEAP_LOG_KEY);
			logger.debug("{} Transformed message {}", LEAP_LOG_KEY, transformedMsg.toString());

			// removing EVT_CONTEXT and requestContext in transformed Message
			// before Dispatch message.

			String message = (String) transformedMsg;
			JSONObject json = null;
			if (message.startsWith("{")) {
				json = new JSONObject(message);
				json = EventFrameworkConfigHelper.removeEventContextAndRequestContext(json);
				EventFrameworkConfigHelper.formattingEventStructure(json);
				// Dispatching the message through the channel.
				channel.dispatchMsg((Serializable) json.toString(), leapEvt.getRequestContext(), leapEventId);

			} else if (message.startsWith("<")) {
				json = XML.toJSONObject(message);
				json = EventFrameworkConfigHelper.removeEventContextAndRequestContext(json);
				if (!json.has("Event"))
					EventFrameworkConfigHelper.formattingEventStructure(json);

				// Some Issues with "content" key in JSON because JSON not
				// converted into XML format properly inside content key of
				// JSON.

				String jsonString = json.toString().replace("content", "temp");
				String xml = XML.toString(new JSONObject(jsonString));
				xml = xml.replace("temp", "content");

				if (!xml.contains("Event"))
					xml = "<Event>" + xml + "</Event>";

				// Dispatching the message through the channel.
				channel.dispatchMsg((Serializable) xml, leapEvt.getRequestContext(), leapEventId);

			} else {
				// Dispatching the message through the channel.
				channel.dispatchMsg(transformedMsg, leapEvt.getRequestContext(), leapEventId);
			}

			dispatchStatus = true;
			logger.debug("{} successfully dispatched event for eventId :{} ", LEAP_LOG_KEY, leapEventId + "!");
		} catch (

		MessageDispatchingException mDispatchingException) {
			String failureMsg = mDispatchingException.getMessage();
			EventTrackerUtil.setFailureJSONString(failureJson, mDispatchingException, failureMsg);
			logger.error("{} Failed to publish event for eventId :{} {}", LEAP_LOG_KEY, leapEventId + "!",
					mDispatchingException);

			// if
			// (processingWay.equals(EventTrackerTableConstants.PROCESSING_WAY_ASYNC))
			eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey, status,
					leapEventId, true, failureJson.toString(), true, isRetried, channelId);
			// else
			// logger.error("Failed to publish event for eventId : " +
			// leapEventId + "! due to "
			// + mDispatchingException.getMessage());
		} catch (Exception e) {
			String failureMsg = e.getMessage();
			EventTrackerUtil.setFailureJSONString(failureJson, e, failureMsg);
			logger.error("{} Failed to publish event for eventId : {} {}", LEAP_LOG_KEY, leapEventId + "!", e);
			// if
			// (processingWay.equals(EventTrackerTableConstants.PROCESSING_WAY_ASYNC))
			eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey, status,
					leapEventId, true, failureJson.toString(), true, isRetried, channelId);
			// else
			// logger.error("Failed to publish event for eventId : " +
			// leapEventId + "! due to " + e.getMessage());
		}
		if (tracker != null)
			removeEventDispatcherFromCache(tracker);
		return dispatchStatus;
	}// end of method

	private void initializeHazelcastInstance() {
		hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
	}// end of method

	private void removeEventDispatcherFromCache(EventDispatcherTracker eventDispatcherTracker) {
		logger.debug("{} removing from the cache after distaching is complete for {}", LEAP_LOG_KEY,
				eventDispatcherTracker.getLeapEventId());
		if (hazelcastInstance == null)
			initializeHazelcastInstance();
		IMap<String, List<EventDispatcherTracker>> cachedTrackersMap = hazelcastInstance
				.getMap(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS);
		if (!cachedTrackersMap.isEmpty()) {
			List<EventDispatcherTracker> cachedList = cachedTrackersMap
					.get(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS);
			if (!cachedList.isEmpty())
				cachedList.remove(eventDispatcherTracker);
			cachedTrackersMap.put(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS, cachedList);
			logger.debug("{} size of event dispatcher cache list :{} ", LEAP_LOG_KEY, cachedList.size());
		}
	}// end of method

	public void setProcessingWay(String processingWay) {
		this.processingWay = processingWay;
	}// end of method

	public void setTracker(EventDispatcherTracker tracker) {
		this.tracker = tracker;
	}// end of method

}
