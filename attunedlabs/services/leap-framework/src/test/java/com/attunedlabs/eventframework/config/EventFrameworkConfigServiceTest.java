package com.attunedlabs.eventframework.config;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

public class EventFrameworkConfigServiceTest {

	final Logger logger = LoggerFactory.getLogger(EventFrameworkConfigServiceTest.class);

	private EventFramework fileStoreDispatcher;
	private EventFramework hazelcastQueue;
	private EventFramework jmsEventFrameworkConfig;
	private EventFramework systemEvent;
	private IEventFrameworkConfigService eventConfigService;
	private IConfigPersistenceService configPersistenceService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws TimeoutException
	 * @throws IOException
	 */
	@Before
	public void beforeTest() throws EventFrameworkConfigParserException, ConfigNodeDataConfigurationException,
			ConfigPersistenceException, IOException, TimeoutException {
		if (fileStoreDispatcher == null)
			fileStoreDispatcher = EventAndDispatchFileRead.getFileStore();
		if (hazelcastQueue == null)
			hazelcastQueue = EventAndDispatchFileRead.getHazelCastQueue();
		if(systemEvent==null)
			systemEvent=EventAndDispatchFileRead.getSystemEvent();
		if (eventConfigService == null)
			eventConfigService = new EventFrameworkConfigService();
		if (configPersistenceService == null)
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		if (jmsEventFrameworkConfig == null)
			jmsEventFrameworkConfig = EventAndDispatchFileRead.getJMSSubscription();
		configPersistenceService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);

	}

	/**
	 * this method used for adding event detail into configNodeData table into DB.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testAddEventFrameworkConfigurationWithEvent()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull("Event value should be exist ", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Event id [PRINT_SERVICE_JSON] should be match in config node data name : ", "PRINT_SERVICE_JSON",
				loadedConfigNodeData.getConfigName());

		boolean isDeleted = eventConfigService.deleteEventConfiguration(configContext, evt.getId());
		Assert.assertTrue("deleted event from database ::", isDeleted);
	}

	/**
	 * this method used for adding SystemEvent detail into configNodeData table into
	 * DB.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testAddEventFrameworkConfigurationWithSystemEvent()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("SystemEvent list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent successSysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				successSysEvent = event;
				break;
			}
		}
		Assert.assertNotNull("System Event value should be exist ::", successSysEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, successSysEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, successSysEvent.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("System event id [SERVICE_COMPLETION_SUCCESS] should be match configNodeData name",
				"SERVICE_COMPLETION_SUCCESS", loadedConfigNodeData.getConfigName());

		boolean isDeleted = eventConfigService.deleteSystemEventConfiguration(configContext, successSysEvent.getId());
		Assert.assertTrue("Deleted system event data from DB :", isDeleted);
	}

	/**
	 * this method used for adding DispatchChannel detail into configNodeData table
	 * into DB.
	 * 
	 * @throws EventFrameworkConfigurationException
	 */
	@Test
	public void testAddEventFrameworkConfigurationWithDisChannel() throws EventFrameworkConfigurationException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<DispatchChannel> eventList = fileStoreDispatcher.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", eventList.size() > 0);
		DispatchChannel fileStoreDis = null;
		for (DispatchChannel event : eventList) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				fileStoreDis = event;
				break;
			}
		}
		Assert.assertNotNull("Dispatch Channel Value should be exist ::", fileStoreDis);
		eventConfigService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		boolean isDeleted = eventConfigService.deleteDipatcherChannelConfiguration(configContext, fileStoreDis.getId());
		Assert.assertTrue("Deleted Dispatch Chennal from DB :", isDeleted);
	}

	@Test
	public void testAddEventFrameworkConfigurationWithSubEvent()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("Subscribe Event value should be exist ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Subscribe Event id [TestSubscriber] should be match configNodeDara name", "TestSubscriber",
				loadedConfigNodeData.getConfigName());

		boolean isDeleted = eventConfigService.deleteEventSubscriptionConfiguration(configContext,
				subEvent.getSubscriptionId());
		Assert.assertTrue("Deleted dispatch chennal event data from DB : ", isDeleted);
	}

	/**
	 * this method used for adding SubcriptionEvent detail into configNodeData table
	 * into DB.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetEventConfiguration() throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull("Event value should be exist : ", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Event Id[PRINT_SERVICE_JSON] should be same as configNodeData Name ::", "PRINT_SERVICE_JSON",
				loadedConfigNodeData.getConfigName());

		Event event = eventConfigService.getEventConfiguration(configContext, evt.getId());
		Assert.assertNotNull("Event value Should be exist :: " + event);
		Assert.assertTrue("Event value Should be exist ::", !event.toString().isEmpty());
		Assert.assertEquals("event id should be match from getting event id ::", evt.getId(), event.getId());
	}

	/**
	 * this method used for get DispatchChannel Configuration from configNodeData
	 * Table.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetDispatchChannelConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<DispatchChannel> eventList = fileStoreDispatcher.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", eventList.size() > 0);
		DispatchChannel dispatchChannel = null;
		for (DispatchChannel event : eventList) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				dispatchChannel = event;
				break;
			}
		}
		Assert.assertNotNull("Dispatch Channel Should be exist :: ", dispatchChannel);
		eventConfigService.addEventFrameworkConfiguration(configContext, dispatchChannel);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, dispatchChannel.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("dispatch channel id[FILE_STORE] should be same as confignodedata name", "FILE_STORE",
				loadedConfigNodeData.getConfigName());

		DispatchChannel dispatch = eventConfigService.getDispatchChannelConfiguration(configContext,
				dispatchChannel.getId());
		Assert.assertNotNull("Distpatch channel value should be exist and should not be null::", dispatch);
		Assert.assertTrue("Dispatch Channel Value should be exist and should not be empty",
				!dispatch.toString().isEmpty());
		Assert.assertEquals("dispatch channel id should be same as getting dispatch channel id ::",
				dispatchChannel.getId(), dispatch.getId());
	}

	/**
	 * this method use for get EventSubscription config from ConfigNodeData Table.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetEventSubscriptionConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe event should be exist ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribeEvent id [TestSubscriber] should be same as configNodeData name :",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		SubscribeEvent subcribeEvent = eventConfigService.getEventSubscriptionConfiguration(configContext,
				subEvent.getSubscriptionId());
		Assert.assertNotNull("subscribe Event should be exist and should not be null :: ", subcribeEvent);
		Assert.assertTrue("subscribeEvent Id should be exist and should not be empty ::",
				!subcribeEvent.toString().isEmpty());
		Assert.assertEquals("subscribe Event id should be same as config node data name : ",
				subEvent.getSubscriptionId(), subcribeEvent.getSubscriptionId());
	}

	/**
	 * this method used for get all subscriber topic.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetAllSubscriberTopicNames()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subcriber event should be exist ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribeEvent id [TestSubscriber] should be same as configNodeData name :",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		String allSubTopicName = eventConfigService.getAllSubscriberTopicNames();
		Assert.assertNotNull("subscribe all topic name should not be null :: ", allSubTopicName);
		Assert.assertTrue("subscribe topic name should not be empty ::", !allSubTopicName.isEmpty());
	}

	/**
	 * this method used for get all subscriber according to topic name.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetAllTopicSubscribersbyTopicName()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		String topicName = null;
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("Subscribe event should be exist :", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribeEvent id [TestSubscriber] should be same as configNodeData name :",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		topicName = subEvent.getSubscribeTo();
		Assert.assertNotNull("topic name should not be null ::", topicName);

		Set<String> sub = eventConfigService.getAllTopicSubscribersbyTopicName(topicName);
		Assert.assertNotNull("Subscriber Topic should not be null", sub);
		Assert.assertTrue("subscriber topic should not be empty ::", !sub.isEmpty());
	}

	/**
	 * this method used for get subscription topic by subcription id.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetSubscriptionTopicsbySubscriptionId()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		String subId = null;
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscriber Event value should not be null :", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribeEvent id [TestSubscriber] should be same as configNodeData name :",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		subId = subEvent.getSubscriptionId();
		Assert.assertNotNull("subscriber id should be exist ::", subId);

		String finalSubId = GenericTestConstant.TEST_FEATUREGROUP + "-" + GenericTestConstant.TEST_FEATURE + "-"
				+ GenericTestConstant.TEST_IMPL + "-" + GenericTestConstant.TEST_VENDOR + "-"
				+ GenericTestConstant.TEST_VERSION + "-" + subId;

		String getDataWithSubId = eventConfigService.getSubscriptionTopicsbySubscriptionId(finalSubId);
		Assert.assertNotNull("get subscriber topic should be exist :: ", getDataWithSubId);
		Assert.assertTrue("get subscriber topic should not be empty :: ", !getDataWithSubId.isEmpty());
	}

	/**
	 * this method used for get service completion success from the event builder.
	 * 
	 * @throws EventFrameworkConfigurationException
	 */
	@Test
	public void testGetServiceCompletionSuccessEventBuilder() throws EventFrameworkConfigurationException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		AbstractCamelEventBuilder abstractCamelBuilder = eventConfigService
				.getServiceCompletionSuccessEventBuilder(configContext);
		Assert.assertNotNull("Abstract camel event builder must be exist ::", abstractCamelBuilder);
	}

	/**
	 * this method used for get service completion failure from the event builder.
	 */
	@Test
	public void testGetServiceCompletionFailureEventBuilder() {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		AbstractCamelEventBuilder abstractCamelBuilder = eventConfigService
				.getServiceCompletionFailureEventBuilder(configContext);
		Assert.assertNotNull("Abstract camel event builder must be exist ::", abstractCamelBuilder);
	}

	/**
	 * Standard EventBuilder for building ServicePerformanceLoggingEventBuilder.
	 * Builder is configured once for each tenant
	 */
	@Test
	public void testGetServicePerformanceLoggingEventBuilder() {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		AbstractCamelEventBuilder abstractCamelBuilder = eventConfigService
				.getServicePerformanceLoggingEventBuilder(configContext);
		Assert.assertNotNull("Abstract camel event builder must be exist ::", abstractCamelBuilder);
	}

	/**
	 * this method used for get system event detail.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetSystemEventConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);

		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent successSysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				successSysEvent = event;
				break;
			}
		}
		Assert.assertNotNull(successSysEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, successSysEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, successSysEvent.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("SERVICE_COMPLETION_SUCCESS", loadedConfigNodeData.getConfigName());

		SystemEvent systemEvent = eventConfigService.getSystemEventConfiguration(configContext,
				successSysEvent.getId());
		Assert.assertNotNull("System Event value Should Not be null :", systemEvent);
		Assert.assertEquals("SystemEvent Id Should be same as getting systemEvent id :", successSysEvent.getId(),
				systemEvent.getId());
	}

	/**
	 * in this method used change the dispatcher channel status detail.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfDispactherChannelConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<DispatchChannel> eventList = fileStoreDispatcher.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", eventList.size() > 0);
		DispatchChannel dispatchChannel = null;
		for (DispatchChannel event : eventList) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				dispatchChannel = event;
				break;
			}
		}
		Assert.assertNotNull("Dispatch channel value Should not be null ::", dispatchChannel);
		eventConfigService.addEventFrameworkConfiguration(configContext, dispatchChannel);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, dispatchChannel.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Dispatch Channel Id[FILE_STORE] should be match as configNodeData Name ",
				"FILE_STORE", loadedConfigNodeData.getConfigName());

		boolean isChange = eventConfigService.changeStatusOfDispactherChannelConfiguration(configContext,
				dispatchChannel.getId(), true);
		Assert.assertTrue("Successfully Change dispatch Channel status ::", isChange);
	}

	/**
	 * in this method used change the event subscription status detail.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfEventSubscriptionConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe Event value Should be not be null :", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribe Event Id[TestSubscriber] should be same as ConfigNodeData name ::",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		boolean isChange = eventConfigService.changeStatusOfEventSubscriptionConfiguration(configContext,
				subEvent.getSubscriptionId(), true);
		Assert.assertTrue("Successfully Change Event Subscriptio status ::", isChange);
	}

	/**
	 * in this method check system event reload or not
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadSystemEventCacheObject()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);

		RequestContext requestContext = new RequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("Request Context should not be null :", requestContext);

		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("SystemEvent list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent successSysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				successSysEvent = event;
				break;
			}
		}
		Assert.assertNotNull("System Event should not be null :", successSysEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, successSysEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, successSysEvent.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("system event id [SERVICE_COMPLETION_SUCCESS] should be same as configNodeData name :: ",
				"SERVICE_COMPLETION_SUCCESS", loadedConfigNodeData.getConfigName());

		boolean isReload = eventConfigService.reloadSystemEventCacheObject(requestContext, successSysEvent.getId());
		Assert.assertTrue("should be reLoad SystemEvent from DB with systemEventId", isReload);
	}

	/**
	 * in this method used event reload or not
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadEventCacheObject() throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);

		RequestContext requestContext = new RequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("Request Context Should not be null ::", requestContext);

		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull(evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Event Id [PRINT_SERVICE_JSON] should be same as configNodeData Name :", "PRINT_SERVICE_JSON",
				loadedConfigNodeData.getConfigName());

		boolean isReload = eventConfigService.reloadEventCacheObject(requestContext, evt.getId());
		Assert.assertTrue("should be reLoad Event from DB with eventId", isReload);
	}

	/**
	 * in this method used dispatch channel reload or not
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadDispatchChannelCacheObject()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);

		RequestContext requestContext = new RequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("Request Context Should not be null ::", requestContext);

		List<DispatchChannel> eventList = fileStoreDispatcher.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", eventList.size() > 0);
		DispatchChannel dispatchChannel = null;
		for (DispatchChannel event : eventList) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				dispatchChannel = event;
				break;
			}
		}
		Assert.assertNotNull("dispatch channel value should be exist :: ", dispatchChannel);
		eventConfigService.addEventFrameworkConfiguration(configContext, dispatchChannel);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, dispatchChannel.getId(),
				EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("dispatch Channel id[FILE_STORE] should be same as configNodeData name :: ", "FILE_STORE",
				loadedConfigNodeData.getConfigName());

		boolean isReload = eventConfigService.reloadDispatchChannelCacheObject(requestContext, dispatchChannel.getId());
		Assert.assertTrue("should be reLoad Dispatch Channel from DB with DispatchChannelId", isReload);
	}

	/**
	 * this method used subscriptionEvent Reload or not
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadSubscriptionEventCacheObject()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);

		RequestContext requestContext = new RequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("request Context should not be null ::", requestContext);

		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("Subscribe event value should not be null ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should not be null ::", loadedConfigNodeData);
		Assert.assertEquals("subscribeEvent id [TestSubscriber] should be same as configNodeData name :",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		boolean isReload = eventConfigService.reloadSubscriptionEventCacheObject(requestContext,
				subEvent.getSubscriptionId());
		Assert.assertTrue("should be reLoad Subscription Event from DB with SubscriptionEventId", isReload);
	}

	/**
	 * in this method used to change status of event subscription.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfEventSubscriber()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SubscribeEvent> subList = hazelcastQueue.getEventSubscription().getSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		SubscribeEvent subEvent = null;
		for (SubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe event shpuld not be null ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(),
				EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("Subscribe event Id[TestSubscriber] should be same as configNodeData Name",
				"TestSubscriber", loadedConfigNodeData.getConfigName());

		boolean isChange = eventConfigService.changeStatusOfEventSubscriber(configContext, subEvent.getSubscriptionId(),
				subEvent.getSubscriptionId(), false);
		Assert.assertTrue("Event subscriber status is chnage and should be true ::", isChange);
	}

	/**
	 * in this method used to change status of system Event Config.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfSystemEventConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("system event list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent sysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				sysEvent = event;
				break;
			}
		}
		Assert.assertNotNull("System Event value should be exist ::", sysEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, sysEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, sysEvent.getId(),
				EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("System event id [SERVICE_COMPLETION_FAILURE] should be match configNodeData name",
				"SERVICE_COMPLETION_FAILURE", loadedConfigNodeData.getConfigName());

		boolean isChange = eventConfigService.changeStatusOfSystemEventConfiguration(configContext, sysEvent.getId(),
				false);
		Assert.assertTrue("System Event status is chnage and should be true ::", isChange);

	}

	/**
	 * in this method used to change status of event configuration.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfEventConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);

		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				evt = event;
				break;
			}

		}
		Assert.assertNotNull("Event value should be exist ::", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("System event id [PRINT_SERVICE_JSON] should be match configNodeData name",
				"PRINT_SERVICE_JSON", loadedConfigNodeData.getConfigName());

		boolean isChange = eventConfigService.changeStatusOfEventConfiguration(configContext, evt.getId(), true);
		Assert.assertTrue(isChange);
	}

	/**
	 * in this method adding eventFramework configuration.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testAddEventFrameworkConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<JMSSubscribeEvent> subList = jmsEventFrameworkConfig.getEventSubscription().getJmsSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		JMSSubscribeEvent subEvent = null;
		for (JMSSubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("jms")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe event shpuld not be null ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(), "JMSEventSubscription");
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("JMS Subscribe Event id [jms] should be match configNodeDara name", "jms",
				loadedConfigNodeData.getConfigName());

		boolean isDeleted = eventConfigService.deleteEventSubscriptionConfiguration(configContext,
				subEvent.getSubscriptionId());
		Assert.assertTrue("Deleted dispatch chennal event data from DB : ", isDeleted);
	}

	/**
	 * in this method get jms subscription config from confignodedata table.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws TimeoutException
	 * @throws IOException
	 */
	@Test
	public void testGetJMSEventSubscriptionConfiguration()
			throws EventFrameworkConfigurationException, ConfigPersistenceException, IOException, TimeoutException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<JMSSubscribeEvent> subList = jmsEventFrameworkConfig.getEventSubscription().getJmsSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		JMSSubscribeEvent subEvent = null;
		for (JMSSubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("jms")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe event shpuld not be null ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);


		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(), "JMSEventSubscription");
		

		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("jms subscription event id [jms] should be match configNodeData name", "jms",
				loadedConfigNodeData.getConfigName());
		JMSSubscribeEvent jmsSubEvt = eventConfigService.getJMSEventSubscriptionConfiguration(configContext,
				subEvent.getSubscriptionId());
		Assert.assertNotNull("jms subscribe event should not be null ::", jmsSubEvt);
		Assert.assertEquals("jms subscription event id [jms] should be match configNodeData name",
				subEvent.getSubscriptionId(), jmsSubEvt.getSubscriptionId());
	}

	/**
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetSubscriptionQueuebySubscriptionId()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {

		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<JMSSubscribeEvent> subList = jmsEventFrameworkConfig.getEventSubscription().getJmsSubscribeEvent();
		Assert.assertTrue("SubscribeEvent list should not be empty or zero (0) :: ", subList.size() > 0);
		JMSSubscribeEvent subEvent = null;
		for (JMSSubscribeEvent event : subList) {
			if (event.getSubscriptionId().equalsIgnoreCase("jms")) {
				subEvent = event;
				break;
			}
		}
		Assert.assertNotNull("subscribe event shpuld not be null ::", subEvent);
		eventConfigService.addEventFrameworkConfiguration(configContext, subEvent);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, subEvent.getSubscriptionId(), "JMSEventSubscription");
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("jms subscription event id [jms] should be match configNodeData name", "jms",
				loadedConfigNodeData.getConfigName());

		String finalSubId = GenericTestConstant.TEST_FEATUREGROUP + "-" + GenericTestConstant.TEST_FEATURE + "-"
				+ GenericTestConstant.TEST_IMPL + "-" + GenericTestConstant.TEST_VENDOR + "-"
				+ GenericTestConstant.TEST_VERSION + "-" + subEvent.getSubscriptionId();

		String queueName = eventConfigService.getSubscriptionQueuebySubscriptionId(finalSubId);
		Assert.assertNotNull("Queue Name Should not be Null ::", queueName);
		Assert.assertTrue("Queue Name Should not be Empty :", !queueName.isEmpty());
		Assert.assertEquals("JMS Queue name shoud be same as actual queue Name ::", "JMSQueue", queueName);
	}

	/**
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetEventConfigProducerForBean()
			throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);

		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_CUSTOM")) {
				evt = event;
				break;
			}

		}
		Assert.assertNotNull("Event value should be exist ::", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("System event id [PRINT_SERVICE_CUSTOM] should be match configNodeData name",
				"PRINT_SERVICE_CUSTOM", loadedConfigNodeData.getConfigName());

		String fqcn = evt.getCamelEventProducer().getCamelProducerConfig().getComponent();
		Assert.assertNotNull("fqcn should not be null :: " + fqcn);
		Assert.assertTrue("fqcn should not be empty :: ", !fqcn.isEmpty());

		Event test = eventConfigService.getEventConfigProducerForBean(configContext, GenericTestConstant.SERVICE_NAME,
				fqcn);
		Assert.assertNotNull(test);
	}

	/**
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetEventProducerForBean() throws EventFrameworkConfigurationException, ConfigPersistenceException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("config context value should be exist :: ", configContext);
		List<Event> eventList = fileStoreDispatcher.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);

		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_CUSTOM")) {
				evt = event;
				break;
			}
		}
		Assert.assertNotNull("Event value should be exist ::", evt);
		eventConfigService.addEventFrameworkConfiguration(configContext, evt);

		ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, evt.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertEquals("System event id [PRINT_SERVICE_CUSTOM] should be match configNodeData name",
				"PRINT_SERVICE_CUSTOM", loadedConfigNodeData.getConfigName());

		String fqcn = evt.getCamelEventProducer().getCamelProducerConfig().getComponent();
		Assert.assertNotNull(fqcn);

		CamelEventProducer test = eventConfigService.getEventProducerForBean(configContext,
				GenericTestConstant.SERVICE_NAME, fqcn);
		Assert.assertNotNull(test);
	}
	
}