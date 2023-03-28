package com.attunedlabs.eventframework.dispatcher.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;

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
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaTopicDispatchChannelTest {

	private final String jsonConfigStr = "{\"queueName\":\"testTopic\"}";
	final Logger logger = LoggerFactory.getLogger(KafkaTopicDispatchChannelTest.class);

	private EventFramework eventFrameworkConfig;
	private IConfigPersistenceService perService;
	private EventFramework systemEvent;
	private EventFrameworkDispatcherService eventFrameworkDispatcherService;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1).kafkaPorts(9095);

	private EmbeddedKafkaBroker embeddedKafkaBroker;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Before
	public void init() throws EventFrameworkConfigParserException, ConfigPersistenceException {
		if (eventFrameworkConfig == null)
			eventFrameworkConfig = EventAndDispatchFileRead.getKafkaTopic();
		if(systemEvent==null)
			systemEvent=EventAndDispatchFileRead.getSystemEvent();
		if (perService == null)
			perService = new ConfigPersistenceServiceMySqlImpl();
		if (embeddedKafkaBroker == null)
			embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();
		if (eventFrameworkDispatcherService == null)
			eventFrameworkDispatcherService = new EventFrameworkDispatcherService();

		perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
	}

	/**
	 * this method used for check kafka Topic DispatcherChannel.
	 * 
	 * @throws DispatchChannelInitializationException
	 * @throws InterruptedException
	 * @throws MessageDispatchingException
	 * @throws EventFrameworkConfigurationException
	 */
	@Test
	public void testAKafkaTopicDispatchChannel() throws DispatchChannelInitializationException, InterruptedException,
			MessageDispatchingException, EventFrameworkConfigurationException {
		RequestContext requestContext = ContextData.getRequestContext();
		List<DispatchChannel> disChannelList = eventFrameworkConfig.getDispatchChannels().getDispatchChannel();
		Assert.assertNotEquals("disChennalList Should be greater then zero (0) ", 0, disChannelList.size());

		DispatchChannel disChannelConfig = null;
		for (DispatchChannel disChannel : disChannelList) {
			if (disChannel.getId().equalsIgnoreCase("KAFKA_TOPIC")) {
				disChannelConfig = disChannel;
				break;
			}
		}

		Assert.assertNotNull("dispatch channel config should not be null :: ", disChannelConfig);
		KafkaTopicDispatchChannel hcDisChannel = new KafkaTopicDispatchChannel(
				disChannelConfig.getChannelConfiguration());
		hcDisChannel.dispatchMsg(jsonConfigStr, requestContext, "PRINT_SERVICE");
	}

	@Test
	public void testBConsumer() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("TestGroup", "true", embeddedKafkaBroker);

		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		Consumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProps);
		consumer.subscribe(Collections.singletonList("testTopic"));

		while (true) {
			boolean flag = false;
			ConsumerRecords<String, String> messageConsumed = consumer.poll(10);
			for (ConsumerRecord<String, String> record : messageConsumed) {
				flag = true;
				Assert.assertEquals(1, messageConsumed.count());
				Assert.assertEquals("{\"queueName\":\"testTopic\"}", record.value());
			}
			if (flag) {
				break;
			}
		}
		consumer.close();
	}
	
	/**
	 * this method used for dispatch for event with retry status is true
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testDispatchforEventWithRetryStatusTrue()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
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
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, true);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	/**
	 * this method used for dispatch for event with retry status is false.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testDispatchforEventWithRetryStatusFalse()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
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
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, false);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	/**
	 * this method used for dispatch with event
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 * @throws IOException 
	 */
	@Test
	public void testDispatchforEvent()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException, IOException {
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

		Assert.assertNotNull("Event Value should not be null :: ", evt);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, false, null);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	// using custom event dispatcher

	/**
	 * this method used for dispatch for event with eventdispatcher type XML-XSLT.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 * @throws IOException
	 */

	@Test
	public void testDispatchforEventWithXSLT() throws EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException, IOException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_XSLT")) {
				evt = event;
				break;
			}
		}

		setXsltAsStringData(evt);

		Assert.assertNotNull("Event Value should not be null :: ", evt);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, false, null);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	/**
	 * this method used for dispatch for event with eventdispatcher type XML-XSLT.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 * @throws IOException
	 */
	@Test
	public void testDispatchforEventWithCustom() throws EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException, IOException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<Event> eventList = eventFrameworkConfig.getEvents().getEvent();
		Assert.assertTrue("event list should not be empty or zero (0) :: ", eventList.size() > 0);
		Event evt = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_CUSTOM")) {
				evt = event;
				break;
			}
		}

		Assert.assertNotNull("Event Value should not be null :: ", evt);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel kafkaDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(kafkaDis);
		configService.addEventFrameworkConfiguration(configContext, kafkaDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(kafkaDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, false, null);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());

	}

	/**
	 * this method used for dispatch for event with eventdispatcher type XML-XSLT.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 * @throws IOException
	 */
	@Test
	public void testDispatchforEventWithJSON() throws EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException, IOException {
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

		Assert.assertNotNull("Event Value should not be null :: ", evt);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, evt);

		DispatchChannel kafkaDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(kafkaDis);
		configService.addEventFrameworkConfiguration(configContext, kafkaDis);

		LeapEvent leapEvent = new LeapEvent(evt.getId(), requestContext);
		leapEvent.setDispatchChannelId(kafkaDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, false, null);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());

	}

	/**
	 * this method used for dispatch for system Event using Default EventDispatcher
	 * type Json
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testDispatchforSystemEventWithJson()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("SystemEvent list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent sysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				sysEvent = event;
				break;
			}
		}
		Assert.assertNotNull("System Event value should be exist ::", sysEvent);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, sysEvent);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(sysEvent.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforSystemEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	/**
	 * this method used for dispatch for systemEvent with Custom like convert JSON
	 * to XML.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 * @throws MessageDispatchingException
	 */
	@Test
	public void testDispatchforSystemEventWithCustom()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<SystemEvent> eventList = systemEvent.getSystemEvents().getSystemEvent();
		Assert.assertTrue("SystemEvent list should not be empty or zero (0) :: ", eventList.size() > 0);
		SystemEvent sysEvent = null;
		for (SystemEvent event : eventList) {
			if (event.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				sysEvent = event;
				break;
			}
		}
		Assert.assertNotNull("System Event value should be exist ::", sysEvent);
		IEventFrameworkConfigService configService = new EventFrameworkConfigService();
		configService.addEventFrameworkConfiguration(configContext, sysEvent);

		DispatchChannel fileStoreDis = getDispatchChannelWithFileStore();

		Assert.assertNotNull(fileStoreDis);
		configService.addEventFrameworkConfiguration(configContext, fileStoreDis);

		LeapEvent leapEvent = new LeapEvent(sysEvent.getId(), requestContext);
		leapEvent.setDispatchChannelId(fileStoreDis.getId());
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		eventFrameworkDispatcherService.dispatchforSystemEvent(leapEvent, GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID);

		Assert.assertEquals("tenant id of request context value from leap event same as TestTanant :: ",
				GenericTestConstant.TEST_TENANT, leapEvent.getRequestContext().getTenantId());
	}

	/**
	 * this method used for get dispatchChannel.
	 * 
	 * @return
	 */
	private DispatchChannel getDispatchChannelWithFileStore() {
		List<DispatchChannel> disChannel = eventFrameworkConfig.getDispatchChannels().getDispatchChannel();
		Assert.assertTrue("DispatchChannel list should not be empty or zero (0) :: ", disChannel.size() > 0);
		DispatchChannel fileStoreDis = null;
		for (DispatchChannel event : disChannel) {
			if (event.getId().equalsIgnoreCase("KAFKA_TOPIC")) {
				fileStoreDis = event;
				break;
			}
		}
		return fileStoreDis;
	}

	/**
	 * this method required to setXsltAsString data for eventDispatcher Type
	 * XML-XSLT
	 * 
	 * @param event
	 * @throws IOException
	 */
	private void setXsltAsStringData(Event event) throws IOException {
		List<EventDispatcher> eventDispacherList = event.getEventDispatchers().getEventDispatcher();
		for (EventDispatcher eventDispacher : eventDispacherList) {
			String transformationtype = eventDispacher.getEventTransformation().getType();
			if (transformationtype.equalsIgnoreCase("XML-XSLT")) {
				String xslName = eventDispacher.getEventTransformation().getXSLTName();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(HazelcastTopicDispatchChannelTest.class.getClassLoader().getResourceAsStream(xslName)));
				StringBuffer buffer = new StringBuffer();
				String line = "";
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				eventDispacher.getEventTransformation().setXsltAsString(buffer.toString());
			}
		}
	}
}
