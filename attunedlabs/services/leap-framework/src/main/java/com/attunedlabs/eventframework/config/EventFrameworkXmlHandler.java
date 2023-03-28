package com.attunedlabs.eventframework.config;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.DispatchChannels;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.EventSubscription;
import com.attunedlabs.eventframework.jaxb.Events;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.ObjectFactory;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvents;

/**
 * This class is to read xsd and validate xml against xsd.Also generate java
 * classes for XSD
 * 
 * @author ubuntu
 *
 */

public class EventFrameworkXmlHandler {
	final Logger logger = LoggerFactory.getLogger(EventFrameworkXmlHandler.class);

	/**
	 * this method is to validate xml against xsd defined
	 * 
	 * @param url : URL Object to get xml path
	 * @throws EventingXmlXSDValidationException
	 * @throws EventFrameworkXSDLoadingException
	 * @throws EventFrameworkConfigParserException
	 */
	private void validateXml(String configXMLFile) throws EventFrameworkConfigParserException {
		try {
			logger.debug("{} Custom Error Handler while Validating XML against XSD", LEAP_LOG_KEY);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(
					EventFrameworkXmlHandler.class.getClassLoader().getResource(EventFrameworkConstants.SCHEMA_NAME));
			Validator validator = schema.newValidator();

			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));

			logger.debug("Validation is successful {}", LEAP_LOG_KEY);
		} catch (IOException | SAXException e) {
			logger.error("{} Exception while validating xml against schema {}", LEAP_LOG_KEY, e);
			throw new EventFrameworkConfigParserException("EventFramework Config XML schema validation failed", e);
		}
	}

	public EventFramework marshallConfigXMLtoObject(String configXMLFile) throws EventFrameworkConfigParserException {
		validateXml(configXMLFile);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			EventFramework evtFwkConfig = (EventFramework) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
			return evtFwkConfig;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("EventFramework Config XML parsing failed for file", e);
		}
	}

	public EventFramework marshallXMLtoObject(String eventConfigxml) throws EventFrameworkConfigParserException {
		try {

			String methodName = "marshallXMLtoObject";
			logger.debug("{} entered into the method {} eventConfigxml {}", LEAP_LOG_KEY, methodName, eventConfigxml);
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			EventFramework eventFramework = (EventFramework) jaxbUnmarshaller
					.unmarshal(new StringReader(eventConfigxml));
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return eventFramework;

		} catch (Exception exp) {
			logger.error("{} EventConfig XMLParsing Failed {}", LEAP_LOG_KEY, exp);
			throw new EventFrameworkConfigParserException("EventConfig XMLParsing Failed ", exp);
		}
	}

	public String unmarshallObjecttoXML(DispatchChannel dispatchChannelConfig)
			throws EventFrameworkConfigParserException {
		EventFramework evtFramework = new EventFramework();
		DispatchChannels dispatchChannels = new DispatchChannels();
		List<DispatchChannel> dispatchChannelList = dispatchChannels.getDispatchChannel();
		dispatchChannelList.add(dispatchChannelConfig);
		// add Dispatch Channel to event Framework
		evtFramework.setDispatchChannels(dispatchChannels);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework, writer);
			String theXML = writer.toString();
			logger.debug("{} final xml:: {}", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException(
					"Failed to convert DispatchChannel to xml for dispatchChannel{ID:" + dispatchChannelConfig.getId()
							+ "}",
					e);
		}
	}

	public String unmarshallObjecttoXML(SystemEvent systemEventConfig) throws EventFrameworkConfigParserException {
		EventFramework evtFramework = new EventFramework();
		SystemEvents sysEvents = new SystemEvents();
		List<SystemEvent> sysEventList = sysEvents.getSystemEvent();
		sysEventList.add(systemEventConfig);
		// add SystemEvents to event Framework
		evtFramework.setSystemEvents(sysEvents);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework, writer);
			String theXML = writer.toString();
			logger.debug("{} SystemEvent XML {}", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException(
					"Failed to convert SystemEvent to xml for systemEvent{ID:" + systemEventConfig.getId() + "}", e);
		}
	}

	public String unmarshallObjecttoXML(Event eventConfig) throws EventFrameworkConfigParserException {

		EventFramework evtFramework = new EventFramework();
		Events events = new Events();
		List<Event> eventList = events.getEvent();
		eventList.add(eventConfig);
		evtFramework.setEvents(events);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework, writer);
			String theXML = writer.toString();
			logger.debug("{} Event XML {}", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException(
					"Failed to convert Event to xml for Event{ID:" + eventConfig.getId() + "}", e);
		}
	}

	public String unmarshallObjecttoXML(SubscribeEvent eventSubscription) throws EventFrameworkConfigParserException {

		EventFramework evtFramework = new EventFramework();
		EventSubscription eventSubscriptions = new EventSubscription();
		List<SubscribeEvent> eventSubscriptionList = eventSubscriptions.getSubscribeEvent();
		eventSubscriptionList.add(eventSubscription);
		evtFramework.setEventSubscription(eventSubscriptions);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework, writer);
			String theXML = writer.toString();
			logger.debug("{} Event XML {}", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("Failed to convert EventSubscription to xml for Event{ID:"
					+ eventSubscription.getSubscriptionId() + "}", e);
		}
	}

	public String unmarshallObjecttoXML(JMSSubscribeEvent jmsEventSubscription)
			throws EventFrameworkConfigParserException {

		EventFramework evtFramework = new EventFramework();
		EventSubscription eventSubscriptions = new EventSubscription();
		List<JMSSubscribeEvent> jmsEventSubscriptionList = eventSubscriptions.getJmsSubscribeEvent();
		jmsEventSubscriptionList.add(jmsEventSubscription);
		evtFramework.setEventSubscription(eventSubscriptions);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(EventFramework.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(evtFramework, writer);
			String theXML = writer.toString();
			logger.debug("{} JMS Event OsL {}", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			throw new EventFrameworkConfigParserException("Failed to convert JMSEventSubscription to xml for Event{ID:"
					+ jmsEventSubscription.getSubscriptionId() + "}", e);
		}

	}
}
