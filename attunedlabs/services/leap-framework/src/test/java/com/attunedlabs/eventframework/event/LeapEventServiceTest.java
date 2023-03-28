package com.attunedlabs.eventframework.event;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.eventframework.config.EventAndDispatchFileRead;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;

public class LeapEventServiceTest {
	final Logger logger = LoggerFactory.getLogger(LeapEventServiceTest.class);

	private EventFramework eventFrameworkConfig;
	private IEventFrameworkConfigService eventConfigService;
	private ILeapEventService eventService;
	private IConfigPersistenceService perService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws ConfigPersistenceException
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Before
	public void init() throws EventFrameworkConfigParserException, ConfigPersistenceException {
		if (eventConfigService == null)
			eventConfigService = new EventFrameworkConfigService();
		if (perService == null)
			perService = new ConfigPersistenceServiceMySqlImpl();
		if (eventService == null)
			eventService = new LeapEventService();
		if (eventFrameworkConfig == null)
			eventFrameworkConfig = EventAndDispatchFileRead.getFileStore();

		perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
	}

	/**
	 * this method used for publish data to eventSubscription table
	 * 
	 * @throws InvalidEventException
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testPublishEvent() throws InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull("Event value should not be null", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannel();

		Assert.assertNotNull(fileStoreDis);
		eventConfigService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent event = new LeapEvent(evt.getId(), requestContext);
		event.setDispatchChannelId(fileStoreDis.getId());
		eventService.publishEvent(event, GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_REQUEST_ID, false);
	}

	/**
	 * this method used for publish Event with Event Dispatcher.
	 * 
	 * @throws InvalidEventException
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testPublishEventWithEventdispatcher() throws InvalidEventException,
			EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		EventDispatcherTracker dispatcherTracker = new EventDispatcherTracker();
		dispatcherTracker.setTenantId(GenericTestConstant.TEST_TENANT);
		dispatcherTracker.setSiteId(GenericTestConstant.TEST_SITE);
		dispatcherTracker.setLeapEventId("TEST");
		dispatcherTracker.setRequestId(GenericTestConstant.TEST_REQUEST_ID);
		dispatcherTracker.setLeapEvent(GenericTestConstant.TEST_JSON);
		dispatcherTracker.setLeapEventId("DEMO");
		dispatcherTracker.setStatus("COMPLETE");
		dispatcherTracker.setFailureReason(null);
		dispatcherTracker.setDispatchChannelId("DEMO");

		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull("Event value should not be null", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannel();

		Assert.assertNotNull(fileStoreDis);
		eventConfigService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent event = new LeapEvent(evt.getId(), requestContext);
		event.setDispatchChannelId(fileStoreDis.getId());

		eventService.publishEvent(event, GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_REQUEST_ID, false, dispatcherTracker);

	}

	private DispatchChannel getDispatchChannel() {
		List<DispatchChannel> disChannel = eventFrameworkConfig.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", disChannel.size() > 0);
		DispatchChannel fileStoreDis = null;
		for (DispatchChannel event : disChannel) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				fileStoreDis = event;
				break;
			}
		}
		return fileStoreDis;
	}

}
