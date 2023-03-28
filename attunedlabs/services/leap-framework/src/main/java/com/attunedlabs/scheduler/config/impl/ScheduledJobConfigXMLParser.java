package com.attunedlabs.scheduler.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.ByteArrayOutputStream;
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

import com.attunedlabs.scheduler.ScheduledJobConfigParserException;
import com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration;
import com.attunedlabs.scheduler.jaxb.ScheduledJobConfigurations;

public class ScheduledJobConfigXMLParser {

	Logger logger = LoggerFactory.getLogger(ScheduledJobConfigXMLParser.class);
	public static final String SCHEMA_NAME = "schedulerconfig.xsd";

	/**
	 * Validates File for against the XML SCHEMA FOR Scheduler Configurations
	 */
	private void validateXml(String configXMLFile) throws ScheduledJobConfigParserException {
		String methodName = "validateXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {

			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory
					.newSchema(ScheduledJobConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();

			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("{} Scheduler Config XML Schema Validation Failed for file ", LEAP_LOG_KEY, exp);
			throw new ScheduledJobConfigParserException("Scheduler Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public ScheduledJobConfigurations marshallConfigXMLtoObject(String configXMLFile)
			throws ScheduledJobConfigParserException {
		String methodName = "marshallConfigXMLtoObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(ScheduledJobConfigurations.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			ScheduledJobConfigurations configurations = (ScheduledJobConfigurations) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} SchedulerConfig XMLParsing Failed for file ", LEAP_LOG_KEY, exp);
			throw new ScheduledJobConfigParserException("SchedulerConfig XMLParsing Failed for file ", exp);
		}

	}

	public String unmarshallConfigXMLtoObject(ScheduledJobConfiguration scheduler)
			throws ScheduledJobConfigParserException {
		String methodName = "unmarshallConfigXMLtoObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ScheduledJobConfiguration.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jaxbMarshaller.marshal(scheduler, baos);
			String response = baos.toString();
			logger.trace("{} response : {}" ,LEAP_LOG_KEY, response);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return response;

		} catch (JAXBException exp) {
			logger.error("{} Scheduler XMLParsing Failed for file ",LEAP_LOG_KEY, exp);
			throw new ScheduledJobConfigParserException("SchedulerConfig XMLParsing Failed for file ", exp);
		}
	}

	public String unmarshallObjecttoXML(ScheduledJobConfiguration schedulerConfiguration)
			throws ScheduledJobConfigParserException {
		String methodName = "unmarshallObjecttoXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			ScheduledJobConfigurations configs = new ScheduledJobConfigurations();
			List<ScheduledJobConfiguration> configList = configs.getScheduledJobConfiguration();
			configList.add(schedulerConfiguration);

			JAXBContext jaxbContext = JAXBContext.newInstance(ScheduledJobConfigurations.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(configs, writer);
			// marshaller.marshal(new JAXBElement<PermaStoreConfiguration>(new QName("",
			// "PermaStoreConfiguration"),
			// PermaStoreConfiguration.class, permaStoreConfiguration), writer);
			String theXML = writer.toString();
			logger.trace("{} xmlObjecttoXML()= {} ",LEAP_LOG_KEY,theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert SchedulerConfiguration back to XML",LEAP_LOG_KEY, e);
			throw new ScheduledJobConfigParserException("Failed to convert SchedulerConfiguration back to XML ", e);
		}
	}

}
