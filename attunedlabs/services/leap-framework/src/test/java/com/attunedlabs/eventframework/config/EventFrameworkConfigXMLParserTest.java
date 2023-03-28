package com.attunedlabs.eventframework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;

public class EventFrameworkConfigXMLParserTest {
	final Logger logger = LoggerFactory.getLogger(EventFrameworkConfigXMLParserTest.class);

	private String fileStore;
	private String systemEvent;
	private String kafkaSub;
	private EventFrameworkXmlHandler parser;

	/**
	 * This methd use for before each method
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Before
	public void getFileObject() throws EventFrameworkConfigParserException {
		if(fileStore==null)
			fileStore=getFileStore();
		if(systemEvent==null)
			systemEvent=getSystemEvent();
		if(kafkaSub==null)
			kafkaSub=getKafkaSub();
	}

	/**
	 * this method used for xml to object
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testMarshallConfigxmlToObject() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(fileStore);
		Assert.assertNotNull("EventFramework Configuration xml should not be null and should be valid xml String",
				eventFwkConfig);
	}

	/**
	 * this method used for Unmarshaling, object to dispatcher channel.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testUnmarshallObjecttoXMLDispatchChannel() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(fileStore);
		List<DispatchChannel> channelList = eventFwkConfig.getDispatchChannels().getDispatchChannel();
		DispatchChannel fileStoreChannel = null;
		for (DispatchChannel channel : channelList) {
			if (channel.getId().equalsIgnoreCase("FILE_STORE")) {
				fileStoreChannel = channel;
				break;
			}
		}
		String xmlString = parser.unmarshallObjecttoXML(fileStoreChannel);
		Assert.assertNotNull("Failed to convert DispatchChannel to respective XML", xmlString);

	}

	/**
	 * this method used for unmarshaling, object to SystemEvent Xml.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testUnmarshallObjecttoXMLSystemEvent() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(systemEvent);
		List<SystemEvent> sysEventList = eventFwkConfig.getSystemEvents().getSystemEvent();
		SystemEvent serviceCompEvent = null;
		for (SystemEvent sysEvent : sysEventList) {
			if (sysEvent.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
				serviceCompEvent = sysEvent;
				break;
			}
		}
		String xmlString = parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert SystemEvent to respective XML", xmlString);
	}

	/**
	 * this method used for unmarshaling, object to Event Xml
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testUnmarshallObjecttoXMLEvent() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(fileStore);
		List<Event> eventList = eventFwkConfig.getEvents().getEvent();
		Event serviceCompEvent = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				serviceCompEvent = event;
				break;
			}
		}
		String xmlString = parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert Event to respective XML", xmlString);
	}

	/**
	 * this method used for unmarshaling then marshaling of Event.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testUnmarshallThanMarshallOFEvent() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(fileStore);
		List<Event> eventList = eventFwkConfig.getEvents().getEvent();
		Event serviceCompEvent = null;
		for (Event event : eventList) {
			if (event.getId().equalsIgnoreCase("PRINT_SERVICE_JSON")) {
				serviceCompEvent = event;
				break;
			}
		}
		String xmlString = parser.unmarshallObjecttoXML(serviceCompEvent);
		Assert.assertNotNull("Failed to convert Event to respective XML", xmlString);

		EventFramework eventFwkConfig2 = parser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("EventFramework Failure converting Object to xml and than xml to back to Object",
				eventFwkConfig2);
	}

	/**
	 * this method used for unmarshaling then marshaling of eventSubscription.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testUnmarshallThanMarshallOFEventSubscription() throws EventFrameworkConfigParserException {
		EventFramework eventFwkConfig = parser.marshallConfigXMLtoObject(kafkaSub);
		List<SubscribeEvent> eventSubscriptionList = eventFwkConfig.getEventSubscription().getSubscribeEvent();
		SubscribeEvent testSubscription = null;
		for (SubscribeEvent evtSub : eventSubscriptionList) {
			if (evtSub.getSubscriptionId().equalsIgnoreCase("TestSubscriber")) {
				testSubscription = evtSub;
				break;
			}
		}
		String xmlString = parser.unmarshallObjecttoXML(testSubscription);
		Assert.assertNotNull("Failed to convert EventSubscription to respective XML", xmlString);

		EventFramework eventFwkConfig2 = parser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("EventFramework Failure converting Object to xml and than xml to back to Object",
				eventFwkConfig2);
	}
	
	
	
	private String getFileStore() throws EventFrameworkConfigParserException {
		parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.FILE_STORE);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
	
	private String getSystemEvent() throws EventFrameworkConfigParserException {
		parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.SYSTEM_EVENT);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
	
	private String getKafkaSub() throws EventFrameworkConfigParserException {
		parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(EventFrameworkTestConstant.KAFKA_SUBSCRIPTION);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new EventFrameworkConfigParserException("eventFramework file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
	

}
