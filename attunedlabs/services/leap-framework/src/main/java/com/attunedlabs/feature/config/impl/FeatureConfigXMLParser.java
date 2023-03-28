package com.attunedlabs.feature.config.impl;

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

import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.Features;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;

public class FeatureConfigXMLParser {

	final Logger logger = LoggerFactory.getLogger(FeatureConfigXMLParser.class);
	public static final String SCHEMA_NAME = "featureService.xsd";

	/** Validates File for against the XML SCHEMA FOR feature Configurations */
	private void validateXml(String configXMLFile) throws FeatureConfigParserException {
		String methodName = "validateXml";
		logger.debug("{} entered into the method {} of FeatureConfigXMLParser", LEAP_LOG_KEY, methodName);
		// String fileName=configXMLFile.getName();
		try {

			logger.trace("{} Custom Error Handler while Validating XML against XSD", LEAP_LOG_KEY);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(FeatureConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("Feature Config XML Schema Validation Failed for file ", exp);
			throw new FeatureConfigParserException("Feature Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public FeaturesServiceInfo marshallConfigXMLtoObject(String configXMLFile) throws FeatureConfigParserException {
		// String fileName=configXMLFile.getName();
		String methodName = "marshallConfigXMLtoObject";
		logger.debug("{} entered into the method {} of FeatureConfigXMLParser", LEAP_LOG_KEY, methodName);
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeaturesServiceInfo.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			FeaturesServiceInfo featureconfigurations = (FeaturesServiceInfo) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureconfigurations;
		} catch (JAXBException exp) {
			logger.error("{} FeaturesServiceInfoConfig XMLParsing Failed for file ", LEAP_LOG_KEY, exp);
			throw new FeatureConfigParserException("FeaturesServiceInfoConfig XMLParsing Failed for file ", exp);
		}
	}

	public FeaturesServiceInfo marshallXMLtoObject(String featureConfigxml) throws FeatureConfigParserException {
		String methodName = "marshallXMLtoObject ";
		logger.debug("{} entered into the method {} of FeatureConfigXMLParser", LEAP_LOG_KEY, methodName);
		try {
			logger.trace("{} xmlString is {}", LEAP_LOG_KEY, featureConfigxml);
			JAXBContext jaxbContext = JAXBContext.newInstance(com.attunedlabs.feature.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FeaturesServiceInfo featureconfigurations = (FeaturesServiceInfo) jaxbUnmarshaller
					.unmarshal(new StringReader(featureConfigxml));
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureconfigurations;
		} catch (Exception exp) {
			logger.error("{} FeatureConfig XMLParsing Failed ", LEAP_LOG_KEY, exp);
			throw new FeatureConfigParserException("FeatureConfig XMLParsing Failed ", exp);
		}
	}

	public String unmarshallObjecttoXML(Feature feature, String featureGroup) throws FeatureConfigParserException {
		String methodName = "unmarshallObjecttoXML";
		logger.debug("{} entered into the method {} of FeatureConfigXMLParser", LEAP_LOG_KEY, methodName);
		try {
			FeaturesServiceInfo featureServiceconfigs = new FeaturesServiceInfo();
			Features features = new Features();
			features.setFeatureGroup(featureGroup);
			features.setFeature(feature);
			featureServiceconfigs.setFeatures(features);
			logger.trace("{} features object :{}", LEAP_LOG_KEY, features);
			JAXBContext jaxbContext = JAXBContext.newInstance(FeaturesServiceInfo.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(featureServiceconfigs, writer);
			String theXML = writer.toString();
			logger.trace("{} xmlObjecttoXML()={}", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert featureConfiguration back to XML ", LEAP_LOG_KEY, e);
			throw new FeatureConfigParserException("Failed to convert FeatureConfiguration back to XML ", e);
		}
	}

}
