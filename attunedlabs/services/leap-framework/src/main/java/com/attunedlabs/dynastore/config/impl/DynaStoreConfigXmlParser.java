package com.attunedlabs.dynastore.config.impl;

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

import com.attunedlabs.dynastore.config.DynaStoreConfigParserException;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfigurations;

public class DynaStoreConfigXmlParser {

	final Logger logger = LoggerFactory.getLogger(DynaStoreConfigXmlParser.class);
	static final String SCHEMA_NAME = "dynastore.xsd";

	/**
	 * validate string dynastoreconfigxml file against to XML SCHEMA of
	 * Dynastore.xsd
	 * 
	 * @param configXMLFile
	 * @throws DynaStoreConfigParserException
	 */
	private void validateXml(String configXMLFile) throws DynaStoreConfigParserException {
		try {
			String methodName = "validateXml";
			logger.debug("{} entered into the method {} configXMLFile{}", LEAP_LOG_KEY, methodName, configXMLFile);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(DynaStoreConfigXmlParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("{} Dyna Store Config XML Schema Validation Failed for file {} ", LEAP_LOG_KEY, exp);
			throw new DynaStoreConfigParserException("DynaSTore Config XML Schema Validation Failed for file ", exp);
		}
	}

	/**
	 * to convert configXmlString file to Pojo
	 * 
	 * @param configXMLFile
	 * @return DynastoreConfigurations
	 * @throws DynaStoreConfigParserException
	 */
	public DynastoreConfigurations marshallConfigXMLtoObject(String configXMLFile)
			throws DynaStoreConfigParserException {
		// String fileName=configXMLFile.getName();
		try {
			String methodName = "marshallConfigXMLtoObject";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(DynastoreConfigurations.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			DynastoreConfigurations configurations = (DynastoreConfigurations) jaxbUnmarshaller
					.unmarshal(inputSourceConfigXml);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} DynaStoreConfiguration XMLParsing Failed for file {}", LEAP_LOG_KEY, exp);
			throw new DynaStoreConfigParserException("DynaStoreConfiguration XMLParsing Failed for file ", exp);
		}
	}

	/**
	 * to convert pojo to xml string
	 * 
	 * @param dynastoreConfiguration
	 * @return string configuration Xml file
	 * @throws DynaStoreConfigParserException
	 */
	public String unmarshallObjecttoXML(DynastoreConfiguration dynastoreConfiguration)
			throws DynaStoreConfigParserException {
		try {
			String methodName = "unmarshallObjecttoXML";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			DynastoreConfigurations configs = new DynastoreConfigurations();
			List<DynastoreConfiguration> configList = configs.getDynastoreConfiguration();
			configList.add(dynastoreConfiguration);

			JAXBContext jaxbContext = JAXBContext.newInstance(DynastoreConfigurations.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(configs, writer);
			String theXML = writer.toString();
			logger.trace("xmlObjecttoXML()= {}", LEAP_LOG_KEY, theXML);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert DynastoreConfiguration back to XML {}", LEAP_LOG_KEY, e);
			throw new DynaStoreConfigParserException("Failed to convert DynaStoreConfiguration back to XML ", e);
		}
	}

}
