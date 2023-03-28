package com.attunedlabs.resourcemanagement.config.impl;

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

import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.dynastore.config.DynaStoreConfigParserException;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfigurations;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigParserException;
import com.attunedlabs.resourcemanagement.jaxb.ConnectionInfo;
import com.attunedlabs.resourcemanagement.jaxb.GetResourceContent;
import com.attunedlabs.resourcemanagement.jaxb.ResourceManagerConfig;

public class ResourceManagementConfigXmlParser {

	final Logger logger = LoggerFactory.getLogger(ResourceManagementConfigXmlParser.class);
	static final String SCHEMA_NAME = "resourceManagement.xsd";

	/**
	 * validate string dynastoreconfigxml file against to XML SCHEMA of
	 * Dynastore.xsd
	 * 
	 * @param configXMLFile
	 * @throws DynaStoreConfigParserException
	 */
	private void validateXml(String configXMLFile) throws ResourceManagementConfigParserException {

		String methodName = "validateXml";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		try {

			logger.trace("{} .validateXml() Custom Error Handler while Validating XML against XSD", LEAP_LOG_KEY);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory
					.newSchema(ResourceManagementConfigXmlParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();

			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("{} ResourceManagent Config XML Schema Validation Failed for file ", LEAP_LOG_KEY, exp);
			throw new ResourceManagementConfigParserException(
					"ResourceManagent Config XML Schema Validation Failed for file ", exp);
		}
	}

	/**
	 * to convert configXmlString file to Pojo
	 * 
	 * @param configXMLFile
	 * @return ResourceManagerConfig
	 * @throws ResourceManagementConfigParserException
	 */
	public ResourceManagerConfig marshallConfigXMLtoObject(String configXMLFile)
			throws ResourceManagementConfigParserException {
		// String fileName=configXMLFile.getName();
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(ResourceManagerConfig.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			ResourceManagerConfig configurations = (ResourceManagerConfig) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} ResourceManagerConfig XMLParsing Failed for file ", LEAP_LOG_KEY, exp);
			throw new ResourceManagementConfigParserException("ResourceManagerConfig XMLParsing Failed for file ", exp);
		}
	}

	/**
	 * to convert pojo to xml string
	 * 
	 * @param resourceManageConfiguration
	 * @return string configuration Xml file
	 * @throws ResourceManagementConfigParserException
	 */
	public String unmarshallObjecttoXML(ResourceManagerConfig resourceManageConfiguration)
			throws ResourceManagementConfigParserException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ResourceManagerConfig.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(resourceManageConfiguration, writer);
			String theXML = writer.toString();
			logger.trace("{} xmlObjecttoXML()={} ", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert ResourceManagerConfig context back to XML", LEAP_LOG_KEY, e);
			throw new ResourceManagementConfigParserException("Failed to convert ResourceManagerConfig back to XML ",
					e);
		}
	}

	/**
	 * to convert pojo to xml string
	 * 
	 * @param getResourceContent
	 * @return string configuration Xml file
	 * @throws ResourceManagementConfigParserException
	 */
	public String unmarshallObjecttoXML(GetResourceContent getResourceContent)
			throws ResourceManagementConfigParserException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(GetResourceContent.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(getResourceContent, writer);
			String theXML = writer.toString();
			logger.trace("{} xmlObjecttoXML()= {} ", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert ResourceManagerConfig context back to XML", LEAP_LOG_KEY, e);
			throw new ResourceManagementConfigParserException("Failed to convert getResourceContent back to XML ", e);
		}
	}

	/**
	 * to convert pojo to xml string
	 * 
	 * @param connectionInfo
	 * @return string configuration Xml file
	 * @throws ResourceManagementConfigParserException
	 */
	public String unmarshallObjecttoXML(ConnectionInfo connectionInfo) throws ResourceManagementConfigParserException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ConnectionInfo.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(connectionInfo, writer);
			String theXML = writer.toString();
			logger.trace("{} xmlObjecttoXML()={} ", LEAP_LOG_KEY, theXML);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert connectionInfo context back to XML", LEAP_LOG_KEY, e);
			throw new ResourceManagementConfigParserException("Failed to convert connectionInfo back to XML ", e);
		}
	}
}
