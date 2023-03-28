package com.attunedlabs.datacontext.config.impl;

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

import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;

/**
 * This class is used for datacontext xml marshaling and Unmarshaling
 * 
 * @author bizruntime
 *
 */
public class DataContextConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(DataContextConfigXMLParser.class);
	public static final String SCHEMA_NAME = "featureDataContext.xsd";

	/**
	 * Validates File for against the XML SCHEMA FOR data context Configurations
	 * 
	 * @throws DataContextParserException
	 */
	private void validateXml(String configXMLFile) throws DataContextParserException {
		try {
			String methodName = "validateXml";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(FeatureConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.debug("{} Validation is successful", LEAP_LOG_KEY);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (IOException | SAXException exp) {
			logger.error("datacontext Config XML Schema Validation Failed for file {} {} ", LEAP_LOG_KEY, exp);
			throw new DataContextParserException("datacontext Config XML Schema Validation Failed for file ", exp);
		}
	}// end of method

	public FeatureDataContext marshallConfigXMLtoObject(String configXMLFile) throws DataContextParserException {
		try {
			String methodName = "marshallConfigXMLtoObject";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeatureDataContext.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			FeatureDataContext featureDataContext = (FeatureDataContext) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureDataContext;
		} catch (JAXBException exp) {
			logger.error("{} datacontext Config XMLParsing Failed for file {}", LEAP_LOG_KEY, exp);
			throw new DataContextParserException("datacontext config XMLParsing Failed for file ", exp);
		}
	}// end of method

	public FeatureDataContext marshallXMLtoObject(String featureDataContextConfigxml)
			throws DataContextParserException {
		try {
			String methodName = "marshallXMLtoObject";
			logger.debug("{} entered into the method {} featureDataContextConfigxml{}", LEAP_LOG_KEY, methodName,
					featureDataContextConfigxml);
			JAXBContext jaxbContext = JAXBContext.newInstance(com.attunedlabs.datacontext.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeatureDataContext featureDataContext = (FeatureDataContext) jaxbUnmarshaller
					.unmarshal(new StringReader(featureDataContextConfigxml));
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureDataContext;
		} catch (Exception exp) {
			logger.error("{} FeatureDataContextConfig XMLParsing Failed {} ", LEAP_LOG_KEY, exp);
			throw new DataContextParserException("FeatureDataContextConfig XMLParsing Failed ", exp);
		}
	}

	public String unmarshallObjecttoXML(FeatureDataContext featureDataContext) throws DataContextParserException {
		try {
			String methodName = "marshallXMLtoObject";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeatureDataContext.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(featureDataContext, writer);
			String theXML = writer.toString();
			logger.trace("xmlObjecttoXML()=[{}]", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert feature data context back to XML{}", LEAP_LOG_KEY, e);
			throw new DataContextParserException("Failed to convert feature data context back to XML ", e);
		}
	}// end of method

}
