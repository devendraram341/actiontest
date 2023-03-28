package com.attunedlabs.featuremetainfo.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.jaxb.*;

public class FeatureMetaInfoConfigXmlParser {
	final Logger logger = LoggerFactory.getLogger(FeatureMetaInfoConfigXmlParser.class);
	public static final String FEATURE_METAINFO_SCHEMA_NAME = "featureMetaInfo.xsd";

	/**
	 * Validates File for against the XML SCHEMA FOR FeatureMetaInfo Configurations
	 * 
	 * @throws FeatureMetaInfoConfigParserException
	 */
	private void validateXml(String configXMLFile) throws FeatureMetaInfoConfigParserException {
		String methodName = "validateXml";
		logger.debug("{} entered into the method {} of FeatureMetaInfoConfigXmlParser", LEAP_LOG_KEY, methodName);
		try {
			logger.trace("{} Custom Error Handler while Validating XML against XSD", LEAP_LOG_KEY);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory
					.newSchema(FeatureConfigXMLParser.class.getClassLoader().getResource(FEATURE_METAINFO_SCHEMA_NAME));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("{} Feature MetaInfo Config XML Schema Validation Failed for file ", LEAP_LOG_KEY, exp);
			throw new FeatureMetaInfoConfigParserException(
					"Feature MetaInfo Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public FeatureMetainfo marshallConfigXMLtoObject(String configXMLFile) throws FeatureMetaInfoConfigParserException {
		String methodName = "marshallConfigXMLtoObject";
		logger.debug("{} entered into the method {} of FeatureMetaInfoConfigXmlParser", LEAP_LOG_KEY, methodName);
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeatureMetainfo.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			FeatureMetainfo featureMetaInfoconfigurations = (FeatureMetainfo) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureMetaInfoconfigurations;

		} catch (JAXBException exp) {
			logger.error("{} FeatureMetaInfoConfig XMLParsing Failed for file {}", LEAP_LOG_KEY, exp);
			throw new FeatureMetaInfoConfigParserException("FeatureMetaInfoConfig XMLParsing Failed for file ", exp);
		}
	}

	public FeatureMetainfo marshallXMLtoObject(String featureConfigxml) throws FeatureMetaInfoConfigParserException {
		try {
			String methodName = "marshallXMLtoObject";
			logger.debug("{} entered into the method {} of FeatureMetaInfoConfigXmlParser", LEAP_LOG_KEY, methodName);
			JAXBContext jaxbContext = JAXBContext.newInstance(com.attunedlabs.featuremetainfo.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeatureMetainfo featureMetaInfoconfigurations = (FeatureMetainfo) jaxbUnmarshaller
					.unmarshal(new StringReader(featureConfigxml));
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureMetaInfoconfigurations;

		} catch (Exception exp) {
			logger.error("FeatureMetaInfoConfig XMLParsing Failed ", exp);
			throw new FeatureMetaInfoConfigParserException("FeatureMetaInfoConfig XMLParsing Failed ", exp);
		}
	}
}
