package com.attunedlabs.dynastore.config;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.PublishEvent;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.DynastoreEvent;
import com.attunedlabs.eventframework.jaxb.Event;

public class DynaStoreHelper {
	protected static final Logger logger = LoggerFactory.getLogger(DynaStoreHelper.class);

	public DynastoreEvent entryAdditionPostHandler(DynastoreConfiguration config, RequestContext reqCtx)
			throws DynaStoreEventBuilderException {
		String methodName = "entryAdditionPostHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PublishEvent pubEvent = config.getPublishEvent();
		if (pubEvent == null)
			return null;// no event Configured no post Processing Nothing Doing
		String onEntryAddedEventId = pubEvent.getOnEntryAdded();
		if (onEntryAddedEventId == null || onEntryAddedEventId.isEmpty())
			return null; // No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName = config.getDynastoreName().getValue();

		try {
			Event eventConfig = getEventForEventId(reqCtx, onEntryAddedEventId);
			if (eventConfig == null) {
				throw new DynaStoreEventBuilderException(
						"Failure to Build event on entryAddition for DynaStore{" + dynaStoreName + "} for eventId {"
								+ onEntryAddedEventId + "} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryAddedDynaEvent = new DynastoreEvent(onEntryAddedEventId, dynaStoreName,
					DynastoreEvent.EVENTTYPE_ONENTRY_ADDED, reqCtx);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return entryAddedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("{} Failure to Build event on entryAddition for DynaStore {} for eventId {}", LEAP_LOG_KEY,
					dynaStoreName, onEntryAddedEventId, e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryAddition for DynaStore{"
					+ dynaStoreName + "} for eventId {" + onEntryAddedEventId + "}", e);
		}

	}

	public DynastoreEvent entryDeletionPostHandler(DynastoreConfiguration config, RequestContext reqCtx)
			throws DynaStoreEventBuilderException {
		PublishEvent pubEvent = config.getPublishEvent();
		if (pubEvent == null)
			return null;// no event Configured no post Processing Nothing Doing
		String onEntryDeletedEventId = pubEvent.getOnEntryDeleted();
		if (onEntryDeletedEventId == null || onEntryDeletedEventId.isEmpty())
			return null; // No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName = config.getDynastoreName().getValue();
		try {
			Event eventConfig = getEventForEventId(reqCtx, onEntryDeletedEventId);
			if (eventConfig == null) {
				throw new DynaStoreEventBuilderException(
						"Failure to Build event on entryDeletion for DynaStore{" + dynaStoreName + "} for eventId {"
								+ onEntryDeletedEventId + "} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryDeletedDynaEvent = new DynastoreEvent(onEntryDeletedEventId, dynaStoreName,
					DynastoreEvent.EVENTTYPE_ONENTRY_DELETED, reqCtx);
			return entryDeletedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("{} Failure to Build event on entryDeleted for DynaStore {} for eventId {}", LEAP_LOG_KEY,
					dynaStoreName, onEntryDeletedEventId, e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryAddition for DynaStore{"
					+ dynaStoreName + "} for eventId {" + onEntryDeletedEventId + "}", e);
		}

	}

	public DynastoreEvent entryUpdationPostHandler(DynastoreConfiguration config, RequestContext reqCtx)
			throws DynaStoreEventBuilderException {
		String methodName = "entryUpdationPostHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PublishEvent pubEvent = config.getPublishEvent();
		if (pubEvent == null)
			return null;// no event Configured no post Processing Nothing Doing
		String onEntryUpdatedEventId = pubEvent.getOnEntryUpdated();
		if (onEntryUpdatedEventId == null || onEntryUpdatedEventId.isEmpty())
			return null; // No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName = config.getDynastoreName().getValue();

		try {
			Event eventConfig = getEventForEventId(reqCtx, onEntryUpdatedEventId);
			if (eventConfig == null) {
				throw new DynaStoreEventBuilderException(
						"Failure to Build event on entryUpdation for DynaStore{" + dynaStoreName + "} for eventId {"
								+ onEntryUpdatedEventId + "} may be eventId not configured in eventing system");
			}
			DynastoreEvent entryUpdatedDynaEvent = new DynastoreEvent(onEntryUpdatedEventId, dynaStoreName,
					DynastoreEvent.EVENTTYPE_ONENTRY_UPDATED, reqCtx);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return entryUpdatedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("{} Failure to Build event on entryUpdation for DynaStore {} for eventId {}", LEAP_LOG_KEY,
					dynaStoreName, onEntryUpdatedEventId, e);
			throw new DynaStoreEventBuilderException("Failure to Build event on entryUpdation for DynaStore{"
					+ dynaStoreName + "} for eventId {" + onEntryUpdatedEventId + "}", e);
		}

	}

	public DynastoreEvent sessionTerminationPostHandler(DynastoreConfiguration config, RequestContext reqCtx)
			throws DynaStoreEventBuilderException {
		String methodName = "sessionTerminationPostHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PublishEvent pubEvent = config.getPublishEvent();
		if (pubEvent == null)
			return null;// no event Configured no post Processing Nothing Doing
		String sessionTerminationEventId = pubEvent.getOnTermination();
		if (sessionTerminationEventId == null || sessionTerminationEventId.isEmpty())
			return null; // No <onEntryAdded> Event Configured so nothing doing
		String dynaStoreName = config.getDynastoreName().getValue();

		try {
			Event eventConfig = getEventForEventId(reqCtx, sessionTerminationEventId);
			if (eventConfig == null) {
				throw new DynaStoreEventBuilderException("Failure to Build event on session Termination for DynaStore{"
						+ dynaStoreName + "} for eventId {" + sessionTerminationEventId
						+ "} may be eventId not configured in eventing system");
			}
			DynastoreEvent sessionTerminatedDynaEvent = new DynastoreEvent(sessionTerminationEventId, dynaStoreName,
					DynastoreEvent.EVENTTYPE_ONTERMINATION, reqCtx);
			logger.trace("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return sessionTerminatedDynaEvent;
		} catch (EventFrameworkConfigurationException e) {
			logger.error("Failure to Build event on sessionTermination for DynaStore {} for eventId {}", dynaStoreName,
					sessionTerminationEventId, e);
			throw new DynaStoreEventBuilderException("Failure to Build event on sessionTermination for DynaStore{"
					+ dynaStoreName + "} for eventId {" + sessionTerminationEventId + "}", e);
		}

	}

	private Event getEventForEventId(RequestContext reqCtx, String eventId)
			throws EventFrameworkConfigurationException {
		IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
		Event eventConfig = eventConfigService.getEventConfiguration(reqCtx.getConfigurationContext(), eventId);
		return eventConfig;

	}
}
