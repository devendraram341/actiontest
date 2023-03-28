package com.attunedlabs.applicationmetainfo.config;

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

import com.attunedlabs.applicationmetainfo.jaxb.AppMetainfo;

public class ApplicationMetaInfoConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(ApplicationMetaInfoConfigXMLParser.class);
	public static final String APP_Meta_INFO_XSD = "applicationMetaInfo.xsd";

	/**
	 * Validates File for against the XML SCHEMA FOR Permastore Configurations
	 * 
	 * @throws ApplicationMetaInfoParsingException
	 */
	private void validateXml(String configXMLFile, String schemaName) throws ApplicationMetaInfoParsingException {
		String methodName = "validateXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory
					.newSchema(ApplicationMetaInfoConfigXMLParser.class.getClassLoader().getResource(schemaName));
			Validator validator = schema.newValidator();

			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("Validation is successful {}", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("ApplicationMetaInfo Config XML Schema Validation Failed for file ", exp);
			throw new ApplicationMetaInfoParsingException(
					"ApplicationMetaInfo Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public AppMetainfo marshallConfigXMLtoObject(String configXMLFile) throws ApplicationMetaInfoParsingException {
		try {
			validateXml(configXMLFile, APP_Meta_INFO_XSD);
			JAXBContext jaxbContext = JAXBContext.newInstance(AppMetainfo.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			AppMetainfo configurations = (AppMetainfo) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);

			return configurations;

		} catch (JAXBException exp) {
			logger.error("ApplicationMetaInfo XMLParsing Failed for file ", exp);
			throw new ApplicationMetaInfoParsingException("ApplicationMetaInfo Config XMLParsing Failed for file ",
					exp);
		}
	}

}
