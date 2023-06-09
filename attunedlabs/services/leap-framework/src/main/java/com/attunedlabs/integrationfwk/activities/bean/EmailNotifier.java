package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class EmailNotifier {

	private Logger logger = LoggerFactory.getLogger(FtlEnricher.class.getName());
	private final String TRANSPORT_KEY = "transport";
	private final String EMAIL_HOST_KEY = "mailHost";
	private final String AUTHUSER_KEY = "authUser";
	private final String AUTH_PASSWORD_KEY = "authPassword";
	private final String SMTP_PORT_KEY = "smtpport";
	private final String SMTP_AUTHANTICATE_KEY = "authenticate";
	private final String SMTP_START_SSL_KEY = "starttlsenable";
	private final String SERVICE_CHANNEL_EMAIL_CONFIG_KEY = "serviceChannelEmailConfig";

	/**
	 * processor of EmailNotifier for creating the smtp string [which is the
	 * endpoint] and setting that string into the header to use it in the route
	 * afterwards
	 * 
	 * @param exchange
	 * @throws EmailNotifierException
	 */
	public void processor(Exchange exchange) throws EmailNotifierException {
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		List<String> recepientNodeList = new LinkedList<String>();
		recepientNodeList.add(pipeactivity.getEmailNotifyActivity().getEmailNotification().getRecepientIdXpath());
		// Getting from exchange and putting into Document Object
		Document xmlDocument = generateDocumentFromString(exchange.getIn().getBody(String.class));
		List<Object> recepientList = xpathProcessingOnInputXml(recepientNodeList, xmlDocument);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		org.json.simple.JSONObject emailConfg = getDataFromCacheData(SERVICE_CHANNEL_EMAIL_CONFIG_KEY,
				serviceDataContext);
		// SMTP Endpoint

		String endP = emailConfg.get(TRANSPORT_KEY).toString() + "://" + emailConfg.get(EMAIL_HOST_KEY).toString() + ":"
				+ emailConfg.get(SMTP_PORT_KEY) + "?password=" + emailConfg.get(AUTH_PASSWORD_KEY).toString()
				+ "&username=" + emailConfg.get(AUTHUSER_KEY).toString() + "&mail.smtp.auth="
				+ emailConfg.get(SMTP_AUTHANTICATE_KEY) + "&mail.smtp.starttls.enable="
				+ emailConfg.get(SMTP_START_SSL_KEY) + printRecipients(recepientList) + "&mail.smtp.ssl.trust="
				+ emailConfg.get(EMAIL_HOST_KEY).toString() + "&subject=" + setSubjectinMail(exchange, pipeactivity);
		// Setting the header for endpoint
		exchange.getIn().setHeader("smtpEndpoint", endP);
	}

	/**
	 * Set Subject fetched from the xpath expression provided in the pipline
	 * configuration
	 * 
	 * @param exchange
	 * @return
	 * @throws EmailNotifierException
	 */
	private String setSubjectinMail(Exchange exchange, PipeActivity pipeActivity) throws EmailNotifierException {
		String xmlInput = exchange.getIn().getBody(String.class);
		Document exchangeDocument = generateDocumentFromString(xmlInput);
		List<String> subjectXpath = new LinkedList<String>();
		subjectXpath.add(pipeActivity.getEmailNotifyActivity().getEmailNotification().getMailSubjectXpath());
		List<Object> subjectSet = xpathProcessingOnInputXml(subjectXpath, exchangeDocument);
		String subject = subjectSet.get(0).toString();
		return subject;
	}

	/**
	 * 
	 * Method to print the recepients in the endP [SMTP String to be sent]
	 * 
	 * @param recepientList
	 * @return String
	 */
	private String printRecipients(List<Object> recepientList) {
		Iterator iterator = recepientList.iterator();
		StringBuffer recepientSmtpString = new StringBuffer();
		recepientSmtpString.append("&to=");
		do {
			recepientSmtpString.append(iterator.next());
			if (iterator.hasNext()) {
				recepientSmtpString.append(",");
			}
		} while (iterator.hasNext());
		return recepientSmtpString.toString();
	}// .. end of the Method

	/**
	 * to process the xpath expression on document to get the respective FiledValues
	 * to be substituted by
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @return non duplicate values as set
	 * @throws EmailNotifierException
	 */
	public List<Object> xpathProcessingOnInputXml(List<String> expression, Document xmlDocument)
			throws EmailNotifierException {
		String methodName = "xpathProcessingOnInputXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object fieldVal;
		XPath xPath = XPathFactory.newInstance().newXPath();
		List<Object> fieldValList = new LinkedList();
		for (int x = 0; x < expression.size(); x++) {
			NodeList nodeList = null;
			try {
				nodeList = (NodeList) xPath.compile((String) expression.toArray()[x]).evaluate(xmlDocument,
						XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					fieldVal = nodeList.item(i).getTextContent();
					fieldValList.add(fieldVal);
				}
			} catch (XPathExpressionException e) {
				throw new EmailNotifierException("Unable to compile the xpath expression at index - " + x
						+ " when evaluating document - " + xmlDocument + "..", e);
			}
		}
		if (!fieldValList.isEmpty()) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return fieldValList;
		} else {
			throw new EmailNotifierException(
					"Unable to get the substitutable fields from the fieldMapper configured - listOfSubstitutable fields are -"
							+ fieldValList);
		}
	}// ..end of the method

	/**
	 * to generate the document object once and all from the xml input which is of
	 * String
	 * 
	 * @param xmlInput
	 * @return documentObject
	 * @throws EmailNotifierException
	 * @throws ParserConfigurationException
	 */
	public Document generateDocumentFromString(String xmlInput) throws EmailNotifierException {
		String methodName = "generateDocumentFromString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document xmlDocument;
		xmlInput = xmlInput.trim();
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new EmailNotifierException("Unable to initiate the document builder..", e);
		}
		try {
			xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
		} catch (SAXException | IOException e) {
			throw new EmailNotifierException("Unable to parse the xmlString into document..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xmlDocument;
	}// ..end of method

	/**
	 * This is to get permastore cache data from leap header
	 * 
	 * @param searchString    : cache key
	 * @param parmaConfigname : permastore config key
	 * @param leapHeader      : LeapHeader Object
	 * @return String
	 * @throws EmailNotifierException
	 * @throws SCNotifyRequestProcessingException
	 */
	public org.json.simple.JSONObject getDataFromCacheData(String parmaConfigname,
			LeapServiceContext serviceDataContext) throws EmailNotifierException {
		String methodName = "getDataFromCacheData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String data = "";
		org.json.simple.JSONObject emailConfig = null;
		Map<String, Object> permaCacheObjectInMap = serviceDataContext.getPermastoreFromServiceContext();
		logger.trace("{} providerkey : {} Permastore Data :{} " ,LEAP_LOG_KEY,parmaConfigname, permaCacheObjectInMap);
		Map<String, Object> permaCacheObject = (Map<String, Object>) permaCacheObjectInMap.get(parmaConfigname.trim());
		logger.debug("Permastore Data : " + permaCacheObject);

		Object object = permaCacheObject;
		logger.trace("{} object : {}" ,LEAP_LOG_KEY, object);

		if (object == null)
			throw new EmailNotifierException(
					"provider Emailconfiguration not found in permatsore parmaConfigname = " + parmaConfigname);

		if (!(object instanceof org.json.simple.JSONObject))
			throw new EmailNotifierException(
					"provider Emailconfiguration not found in permatsore  parmaConfigname = " + parmaConfigname);
		emailConfig = (org.json.simple.JSONObject) object;
		logger.trace("{} emilConfig from cache  = {} " ,LEAP_LOG_KEY, emailConfig);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return emailConfig;
	}// ..end of method getDataFromCacheData
}