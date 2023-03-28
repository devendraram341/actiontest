package com.attunedlabs.eventframework.dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventAndDispatchFileRead;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

public class EventFrameworkDispatcherServiceTest {

	final Logger log = LoggerFactory.getLogger(EventFrameworkDispatcherServiceTest.class);

	private EventFramework fileStoreDispatch;
	private EventFramework systemEvent;
	private EventFrameworkDispatcherService eventFrameworkDispatcherService;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1).kafkaPorts(9094);

	private EmbeddedKafkaBroker embeddedKafkaBroker;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void loadConfigurations() throws EventFrameworkConfigParserException {
		if (fileStoreDispatch == null)
			fileStoreDispatch = EventAndDispatchFileRead.getFileStore();
		if(systemEvent==null)
			systemEvent=EventAndDispatchFileRead.getSystemEvent();
		if (eventFrameworkDispatcherService == null)
			eventFrameworkDispatcherService = new EventFrameworkDispatcherService();
		if (embeddedKafkaBroker == null)
			embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();

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
		List<Event> eventList = fileStoreDispatch.getEvents().getEvent();
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
		List<Event> eventList = fileStoreDispatch.getEvents().getEvent();
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
	 */
	@Test
	public void testDispatchforEvent()
			throws EventFrameworkConfigurationException, LeapEventTransformationException, MessageDispatchingException {
		RequestContext requestContext = ContextData.getRequestContext();
		ConfigurationContext configContext = ContextData.getConfigContext();
		List<Event> eventList = fileStoreDispatch.getEvents().getEvent();
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
		List<Event> eventList = fileStoreDispatch.getEvents().getEvent();
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
		List<DispatchChannel> disChannel = fileStoreDispatch.getDispatchChannels().getDispatchChannel();
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
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						EventFrameworkDispatcherServiceTest.class.getClassLoader().getResourceAsStream(xslName)));
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
