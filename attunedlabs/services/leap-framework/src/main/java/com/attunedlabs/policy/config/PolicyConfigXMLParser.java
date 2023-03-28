package com.attunedlabs.policy.config;

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

import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.policy.jaxb.Policies;
import com.attunedlabs.policy.jaxb.Policy;

public class PolicyConfigXMLParser {
	final Logger logger = LoggerFactory.getLogger(PolicyConfigXMLParser.class);
	public static final String SCHEMA_NAME = "policies.xsd";

	/** Validates File for against the XML SCHEMA FOR Permastore Configurations */
	private void validateXml(String configXMLFile) throws PolicyConfigXMLParserException {
		String methodName = "validateXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {

			logger.trace("{} Custom Error Handler while Validating XML against XSD", LEAP_LOG_KEY);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(PolicyConfigXMLParser.class.getClassLoader().getResource(SCHEMA_NAME));
			Validator validator = schema.newValidator();
			StringReader stringReader = new StringReader(configXMLFile);
			validator.validate(new StreamSource(stringReader));
			logger.info("{} Validation is successful", LEAP_LOG_KEY);
		} catch (IOException | SAXException exp) {
			logger.error("Policy Config XML Schema Validation Failed for file ", exp);
			throw new PolicyConfigXMLParserException("Policy Config XML Schema Validation Failed for file ", exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Validate the file and parse it using jaxb nd returns the Policies Object
	 * 
	 * @param configXMLFile
	 * @return
	 * @throws PolicyConfigXMLParserException
	 */
	public Policies marshallConfigXMLtoObject(String configXMLFile) throws PolicyConfigXMLParserException {
		try {
			validateXml(configXMLFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(Policies.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InputSource inputSourceConfigXml = new InputSource(new StringReader(configXMLFile));

			Policies configurations = (Policies) jaxbUnmarshaller.unmarshal(inputSourceConfigXml);

			return configurations;

		} catch (JAXBException exp) {
			logger.error("{} Policies Config XMLParsing Failed for file {}" ,LEAP_LOG_KEY, exp);
			throw new PolicyConfigXMLParserException("Policies Config XMLParsing Failed for file " + exp);
		}
	}

	/**
	 * Takes the Policies xml as an String and marchals xml to Policies bean
	 * 
	 * @param policyConfigxml
	 * @return
	 * @throws PermaStoreConfigParserException
	 */
	public Policies marshallXMLtoObject(String policyConfigxml) throws PolicyConfigXMLParserException {
		try {

			logger.trace("{} marshallXMLtoObject() xmlString is {}",LEAP_LOG_KEY, policyConfigxml);
			JAXBContext jaxbContext = JAXBContext.newInstance(com.attunedlabs.policy.jaxb.ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Policies configurations = (Policies) jaxbUnmarshaller.unmarshal(new StringReader(policyConfigxml));

			return configurations;

		} catch (Exception exp) {
			logger.error("{} Policies Config XMLParsing Failed {}",LEAP_LOG_KEY, exp);
			throw new PolicyConfigXMLParserException("Policies Config XMLParsing Failed ", exp);
		}
	}

	/**
	 * Converts/marshals the Policy object to an xml String
	 * 
	 * @param policy
	 * @return
	 * @throws PolicyConfigXMLParserException
	 */
	public String unmarshallObjecttoXML(Policy policy) throws PolicyConfigXMLParserException {
		try {
			Policies policies = new Policies();
			List<Policy> policyList = policies.getPolicy();
			policyList.add(policy);

			JAXBContext jaxbContext = JAXBContext.newInstance(Policies.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(policies, writer);

			String theXML = writer.toString();
			logger.trace("{} policies xmlObjecttoXML()= {} ",LEAP_LOG_KEY,theXML);
			return theXML;
		} catch (JAXBException e) {
			logger.error("{} Failed to convert Policies object back to XML ",LEAP_LOG_KEY, e);
			throw new PolicyConfigXMLParserException("Failed to convert Policies object back to XML ", e);
		}

	}
}
