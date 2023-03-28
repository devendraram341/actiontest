package com.attunedlabs.leapentity.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.LeapDataServices;

public class LeapEntityConfigXMLParser {

	final Logger logger = LoggerFactory.getLogger(LeapEntityConfigXMLParser.class);

	public static final String SCHEMA_NAME = "LeapDataServices.xsd";
	public static final String SCHEMA_NAME_SUPPORTING = "LeapDataServiceSupporting.xsd";

	/** Validates File for against the XML SCHEMA FOR LeapEntity Configurations */
	private void validateXml(String configXMLFile, String xsd_Name) throws LeapEntityConfigParserException {
		// String fileName=configXMLFile.getName();
		String methodName = "validateXml";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(LeapEntityConfigXMLParser.class.getClassLoader().getResource(xsd_Name));
			Validator validator = schema.newValidator();
			logger.trace("$File$" + LeapEntityConfigXMLParser.class.getClassLoader().getResource(xsd_Name).getFile());
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful",LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("leapEntity Config XML Schema Validation Failed for file ", exp);
			throw new LeapEntityConfigParserException("leapEntity Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * marshal configXMLFile to LeapDataServiceConfiguation
	 * 
	 * @param configXMLFile
	 * @return
	 * @throws LeapEntityConfigParserException
	 */
	public LeapDataServices marshallConfigXMLtoObject(String configXMLFile) throws LeapEntityConfigParserException {

		try {
			validateXml(configXMLFile, SCHEMA_NAME);
			JAXBContext JAXBCONTEXT = JAXBContext.newInstance(LeapDataServices.class);
			Unmarshaller jaxbUnmarshaller = JAXBCONTEXT.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			LeapDataServices configurations = (LeapDataServices) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
			return configurations;

		} catch (JAXBException exp) {
			exp.printStackTrace();
			logger.error("{} leapEntity XMLParsing Failed for file ",LEAP_LOG_KEY, exp);
			throw new LeapEntityConfigParserException("leapEntity XMLParsing Failed for file ", exp);
		}
	}

	public Entity marshallEntityConfigXMLtoObject(String configXMLFile) throws LeapEntityConfigParserException {
		try {
			validateXml(configXMLFile, SCHEMA_NAME_SUPPORTING);
			JAXBContext JAXBCONTEXT = JAXBContext.newInstance(Entity.class);
			Unmarshaller jaxbUnmarshaller = JAXBCONTEXT.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));
			Entity entity = (Entity) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);
			return entity;

		} catch (Exception exp) {
			exp.printStackTrace();
			logger.error("{} leapEntity XMLParsing Failed for file ",LEAP_LOG_KEY, exp);
			throw new LeapEntityConfigParserException("leapEntity XMLParsing Failed for file ", exp);
		}
	}

	public String unmarshallEntityObjecttoXML(Entity entity) throws LeapEntityConfigParserException {
		try {
			JAXBContext JAXBCONTEXT = JAXBContext.newInstance(Entity.class);
			Marshaller marshaller = JAXBCONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			JAXBElement<Entity> entityjaxbElement = new JAXBElement<Entity>(new QName("", "Entity"), Entity.class,
					entity);
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(entityjaxbElement, stringWriter);
			return stringWriter.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new LeapEntityConfigParserException("leapEntity ObjectParsing Failed", e);
		}
	}

}
