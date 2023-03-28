package com.attunedlabs.servicehandlers.config;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;

public class ServiceHandlerConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(ServiceHandlerConfigXMLParser.class);
	public static final String SCHEMA_NAME = "servicehandlerconfig.xsd";
	public static final String APP_SCHEMA_NAME = "app_servicehandlerconfig.xsd";

	/**
	 * Validates File for against the XML SCHEMA FOR Permastore Configurations
	 */
	private void validateXml(String configXMLFile, String schemaName) throws ServiceHandlerConfigParserException {
		// String fileName=configXMLFile.getName();
		String methodName = "validateXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {

			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory
					.newSchema(ServiceHandlerConfigXMLParser.class.getClassLoader().getResource(schemaName));
			Validator validator = schema.newValidator();

			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("{} ServiceHandler Config XML Schema Validation Failed for file {} ", LEAP_LOG_KEY, exp);
			throw new ServiceHandlerConfigParserException(
					"ServiceHandler Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public ServiceHandlerConfiguration marshallConfigXMLtoObject(String configXMLFile)
			throws ServiceHandlerConfigParserException {
		String methodName = "marshallConfigXMLtoObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			validateXml(configXMLFile, SCHEMA_NAME);
			JAXBContext jaxbContext = JAXBContext.newInstance(ServiceHandlerConfiguration.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			ServiceHandlerConfiguration configurations = (ServiceHandlerConfiguration) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} ServiceHandlerConfig XMLParsing Failed for file : {}", LEAP_LOG_KEY, exp);
			throw new ServiceHandlerConfigParserException("ServiceHandler Config XMLParsing Failed for file ", exp);
		}
	}

	public ApplicationServiceHandlerConfiguration marshallConfigXMLtoAppObject(String configXMLFile)
			throws ServiceHandlerConfigParserException {
		try {
			String methodName = "marshallConfigXMLtoAppObject";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			validateXml(configXMLFile, APP_SCHEMA_NAME);
			JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationServiceHandlerConfiguration.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			ApplicationServiceHandlerConfiguration configurations = (ApplicationServiceHandlerConfiguration) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} ApplicationServiceHandlerConfiguration XMLParsing Failed for file ", LEAP_LOG_KEY, exp);
			throw new ServiceHandlerConfigParserException(
					"ApplicationServiceHandlerConfiguration Config XMLParsing Failed for file ", exp);
		}
	}

	public String unmarshallCommonServiceHandlerObjecttoXML(CommonServiceHandler commonServiceHandler)
			throws ServiceHandlerConfigParserException {
		String methodName = "unmarshallCommonServiceHandlerObjecttoXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ApplicationServiceHandlerConfiguration applicationServiceHandlerConfiguration = new ApplicationServiceHandlerConfiguration();
		applicationServiceHandlerConfiguration.setCommonServiceHandler(commonServiceHandler);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationServiceHandlerConfiguration.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(applicationServiceHandlerConfiguration, writer);
			String theXML = writer.toString();
			logger.trace("{} CommonServiceHandler ObjecttoXML()= {} ", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			throw new ServiceHandlerConfigParserException("Failed to convert Application CommonServiceHandlers to xml",
					e);
		}
	}

	public String unmarshallFeatureServiceHandlerObjecttoXML(FeatureServiceHandler featureServiceHandler)
			throws ServiceHandlerConfigParserException {
		String methodName = "unmarshallFeatureServiceHandlerObjecttoXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ApplicationServiceHandlerConfiguration applicationServiceHandlerConfiguration = new ApplicationServiceHandlerConfiguration();
		applicationServiceHandlerConfiguration.setFeatureServiceHandler(featureServiceHandler);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationServiceHandlerConfiguration.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(applicationServiceHandlerConfiguration, writer);
			String theXML = writer.toString();
			logger.trace(" {} FeatureServiceHandler ObjecttoXML()= {}", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			throw new ServiceHandlerConfigParserException("Failed to convert Application FeatureServiceHandlers to xml",
					e);
		}
	}

	public String unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML(
			com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler)
			throws ServiceHandlerConfigParserException {
		String methodName = "unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ServiceHandlerConfiguration applicationServiceHandlerConfiguration = new ServiceHandlerConfiguration();
		applicationServiceHandlerConfiguration.setFeatureServiceHandler(featureServiceHandler);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ServiceHandlerConfiguration.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(applicationServiceHandlerConfiguration, writer);
			String theXML = writer.toString();
			logger.trace(" {} FeatureServiceHandler ObjecttoXML()= {}", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			throw new ServiceHandlerConfigParserException("Failed to convert Feature FeatureServiceHandlers to xml", e);
		}
	}
}
