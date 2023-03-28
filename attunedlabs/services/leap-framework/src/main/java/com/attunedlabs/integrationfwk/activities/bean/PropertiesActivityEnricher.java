package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PropertiesMapping;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class PropertiesActivityEnricher {

	private Logger logger = LoggerFactory.getLogger(PropertiesActivityEnricher.class.getName());

	/**
	 * This method is to fetch the propertyvalue from the pipeline configuration and
	 * putting it into Exchange's body whose xpath it is fetching from the pipeline
	 * again.
	 * 
	 * @param exchange
	 * @throws PropertyActivityException
	 * @throws TransformerException
	 * @throws ActivityEnricherException
	 */
	public void processorBean(Exchange exchange)
			throws PropertyActivityException, ActivityEnricherException, TransformerException {
		String methodName = "processorBean";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		List<PropertiesMapping> propertyMappingList = pipeactivity.getPropertiesActivity().getPropertiesMapping();
		Document xmlDocument = generateDocumentFromString(exchange.getIn().getBody(String.class));
		logger.trace("{} xml document  : {}", LEAP_LOG_KEY, xmlDocument);
		for (int i = 0; i < propertyMappingList.size(); i++) {
			logger.trace("{} Entered Loop", LEAP_LOG_KEY);
			String propertyValue = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i)
					.getPropertyValue();
			logger.trace("{} propertyValue : {}", LEAP_LOG_KEY, propertyValue);
			String nodeToAdd = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getElementToAdd();
			logger.trace("{} nodeToAdd : {}", LEAP_LOG_KEY, nodeToAdd);
			String toXpath = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).getSetToXpath()
					.toString();
			logger.trace("{} got the xpath :{} ", LEAP_LOG_KEY, toXpath.toString());
			String propertyValueSource = pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i)
					.getPropertyValueSource();
			logger.trace("{} got the propertyValueSource : {}", LEAP_LOG_KEY, propertyValueSource);
			String newXmlDoc = checkPropertyValueSource(propertyValueSource, toXpath, xmlDocument, propertyValue,
					nodeToAdd, exchange, i);
			logger.debug("{} newXMLDoc : {}", LEAP_LOG_KEY, newXmlDoc);
			xmlDocument = generateDocumentFromString(newXmlDoc);
		}
		exchange.getIn().setBody(documentToString(xmlDocument));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of method

	/**
	 * to generate the document object once and all from the xml input which is of
	 * String
	 * 
	 * @param xmlInput
	 * @return documentObject
	 * @throws EmailNotifierException
	 * @throws ParserConfigurationException
	 */
	public Document generateDocumentFromString(String xmlInput) throws PropertyActivityException {
		String methodName = "generateDocumentFromString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		xmlInput = StringEscapeUtils.unescapeXml(xmlInput);
		DocumentBuilder builder = null;
		Document xmlDocument;
		xmlInput = xmlInput.trim();
		// xmlInput = StringEscapeUtils.unescapeXml();
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PropertyActivityException("Unable to initiate the document builder..", e);
		}
		try {
			xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
		} catch (SAXException | IOException e) {
			throw new PropertyActivityException("Unable to parse the xmlString into document..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xmlDocument;
	}// ..end of method

	private String checkPropertyValueSource(String propertyValueSource, String toXpath, Document xmlDocument,
			String propertyValue, String nodeToAdd, Exchange exchange, int i)
			throws ActivityEnricherException, TransformerException, PropertyActivityException {
		String methodName = "checkPropertyValueSource";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String newXmlDoc = null;
		logger.trace("{} xml document  : {}", LEAP_LOG_KEY, xmlDocument);
		if (propertyValueSource != null && propertyValueSource.length() > 0 && !(propertyValueSource.isEmpty())) {
			switch (propertyValueSource) {
			case "LeapServiceContext":
				logger.trace("{} propertyValueSource is LeapServiceContext", LEAP_LOG_KEY);
				newXmlDoc = getNodeValueFromLeapHeaderAndAppend(toXpath, xmlDocument, propertyValue, nodeToAdd,
						exchange, i);
				break;

			case "Exchange":
				logger.trace("{} propertyValueSource is Exchange", LEAP_LOG_KEY);
				newXmlDoc = getNodeValueFromExchangeAndAppend(toXpath, xmlDocument, propertyValue, nodeToAdd, exchange,
						i);
				break;
			case "Xpath":
				logger.trace("{} propertyValueSource is Xpath", LEAP_LOG_KEY);
				newXmlDoc = getNodeAndAppendFromXpath(exchange, toXpath, xmlDocument, propertyValue, nodeToAdd, i);
				break;
			default:
				logger.trace("{} propertyValueSource is direct", LEAP_LOG_KEY);
				newXmlDoc = getNodeAndAppend(exchange, toXpath, xmlDocument, propertyValue, nodeToAdd, i);
				break;

			}
		} else {
			// #TODO , need to throw proper exception
			logger.trace("{} propertyValueSource shouldnot be null", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return newXmlDoc;
	}

	private String getNodeAndAppendFromXpath(Exchange exchange, String expression, Document xmlDocument, String res,
			String nodeToAdd, int i) throws ActivityEnricherException, TransformerException, PropertyActivityException {
		String methodName = "getNodeAndAppendFromXpath";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		try {
			logger.trace("{} expression : {}", LEAP_LOG_KEY, expression);
			logger.trace("{} doc : {}", LEAP_LOG_KEY, documentToString(xmlDocument));
			String xmlStr = documentToString(xmlDocument);
			xmlDocument = generateDocumentFromString(xmlStr);
			logger.trace("{}xpath : {}", LEAP_LOG_KEY, res);
			logger.debug("{} xmlDocument : {}", LEAP_LOG_KEY, getvalueFromDocument(xmlDocument, res));
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			// Node has been added fetched from the pipline configuration
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode()) {
				logger.trace("{} override true : {}", LEAP_LOG_KEY, nodeToAdd);
				// if(nodeToAdd.trim().startsWith("["));

				if (nodeToAdd.startsWith("@") && nodeToAdd.contains("=")) {
					logger.trace("{} starts with @ and contain =", LEAP_LOG_KEY);
					int num = nodeList.getLength();
					logger.info("{} Number of nodes {}", LEAP_LOG_KEY, num);
					for (int nodeCount = 0; nodeCount < num; nodeCount++) {
						Element element = (Element) nodeList.item(nodeCount);
						logger.info("{} Element name : {}", LEAP_LOG_KEY, element.getNodeName());
						NamedNodeMap attributes = element.getAttributes();
						int numAttrs = attributes.getLength();

						for (int attributeCount = 0; attributeCount < numAttrs; attributeCount++) {
							Attr attr = (Attr) attributes.item(attributeCount);
							String attrName = attr.getNodeName();
							String attrValue = attr.getNodeValue();
							logger.info("{} Found attribute Key: {} with Name: {}, attribute value: {}", LEAP_LOG_KEY,
									attrName, attrValue, element.getTextContent());
							logger.info("{} comparing attribute to add {} with {}", LEAP_LOG_KEY, nodeToAdd, attrValue);
							if (nodeToAdd.contains(attrValue)) {
								logger.info("{} before changing the element {}", LEAP_LOG_KEY,
										element.getTextContent());
								element.setTextContent(getvalueFromDocument(xmlDocument, res));
								logger.info("{} after changing the element {}", LEAP_LOG_KEY, element.getTextContent());
							}
						}
					}
				} else if (nodeToAdd.startsWith("@") && !nodeToAdd.contains("=")) {
					logger.trace("{} starts with @", LEAP_LOG_KEY);
					logger.debug("{} nodeToAdd is an attribute : {}", LEAP_LOG_KEY, nodeToAdd);
					Element node = (Element) nodeList.item(nodeList.getLength() - 1);
					logger.debug("{} Adding attribute to : {}", LEAP_LOG_KEY, node.getNodeName());
					node.setAttribute(nodeToAdd, getvalueFromDocument(xmlDocument, res));
				} else {
					logger.trace("{} normal ELement", LEAP_LOG_KEY);
					logger.debug("{} nodeListElement : {}, value: {}", LEAP_LOG_KEY, nodeList.item(0).getNodeName(),
							nodeList.item(0).getNodeName());
					Element nodeListElement = (Element) nodeList.item(0);
					Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
					logger.trace("{} doc : {}", LEAP_LOG_KEY, documentToString(xmlDocument));
					logger.trace("{} xpath : {}", LEAP_LOG_KEY, res);
					logger.debug("{} xmlDocument : {}", LEAP_LOG_KEY, getvalueFromDocument(xmlDocument, res));
					existingNode.setTextContent(getvalueFromDocument(xmlDocument, res));
				}
			} else {
				logger.trace("{} override false", LEAP_LOG_KEY);
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent(getvalueFromDocument(xmlDocument, res));
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return appendedDocument;
	}

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeAndAppend(Exchange exchange, String expression, Document xmlDocument, String res,
			String nodeToAdd, int i) throws ActivityEnricherException, TransformerException {
		String methodName = "getNodeAndAppend";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			// Node has been added fetched from the pipline configuration
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode()) {
				Element nodeListElement = (Element) nodeList.item(0);
				Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
				existingNode.setTextContent(res);
			} else {
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent(res);
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return appendedDocument;
	}// ..end of the method

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeValueFromLeapHeaderAndAppend(String expression, Document xmlDocument, String res,
			String nodeToAdd, Exchange exchange, int i) throws ActivityEnricherException, TransformerException {
		String methodName = "getNodeValueFromLeapHeaderAndAppend";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		Map<String, Object> serviceRequestDataValue = serviceDataContext.getServiceRequestDataFromServiceContext();
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;

		try {
			logger.trace("{} fetching the nodeList", LEAP_LOG_KEY);
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			logger.debug("got the nodeList : " + nodeList.item(nodeList.getLength() - 1).getNodeName());
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode() != null) {
				logger.trace("{} property Node To Add is overrided", LEAP_LOG_KEY);
				Element nodeListElement = (Element) nodeList.item(0);
				if (nodeListElement.getElementsByTagName(nodeToAdd).item(0) != null) {
					Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
					existingNode.setTextContent((String) serviceRequestDataValue.get(res));
				} else {
					if (nodeToAdd.startsWith("@")) {
						logger.trace("{} nodeToAdd is an attribute : {}", LEAP_LOG_KEY, nodeToAdd);
						Element node = (Element) nodeList.item(nodeList.getLength() - 1);
						logger.debug("{} Adding attribute to : {}", LEAP_LOG_KEY, node.getNodeName());
						node.setAttribute(nodeToAdd, (String) serviceRequestDataValue.get(res));
					} else {
						Element node = xmlDocument.createElement(nodeToAdd);
						node.setTextContent((String) serviceRequestDataValue.get(res));
						nodeList.item(0).appendChild(node);
					}
				}
			} else {
				if (nodeToAdd.startsWith("@")) {
					logger.trace("{} nodeToAdd is an attribute : {}", LEAP_LOG_KEY, nodeToAdd);
					Element node = (Element) nodeList.item(nodeList.getLength() - 1);
					logger.debug("{} Adding attribute to : {}", LEAP_LOG_KEY, node.getNodeName());
					node.setAttribute(nodeToAdd.substring(1), (String) serviceRequestDataValue.get(res));
				} else {

					// Node has been added fetched from the pipline
					// configuration
					Element node = xmlDocument.createElement(nodeToAdd);
					node.setTextContent((String) serviceRequestDataValue.get(res));
					// appended the element as a child to the node fetched from
					// the
					// xpath
					nodeList.item(0).appendChild(node);
				}
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return appendedDocument;
	}// ..end of the method

	/**
	 * to append string(res) into node fetched by using xpath expression
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @param nodeToAdd
	 * @return
	 * @return non duplicate values as set
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String getNodeValueFromExchangeAndAppend(String expression, Document xmlDocument, String res,
			String nodeToAdd, Exchange exchange, int i) throws ActivityEnricherException, TransformerException {
		String methodName = "getNodeValueFromExchangeAndAppend";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String appendedDocument = null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = null;
		logger.trace("{} expression : {}", LEAP_LOG_KEY, expression);
		logger.trace("{} xmlDocument : {}", LEAP_LOG_KEY, documentToString(xmlDocument));
		logger.trace("{} res : {}", LEAP_LOG_KEY, res);
		logger.trace("{} nodeToAdd : {}", LEAP_LOG_KEY, nodeToAdd);
		logger.debug("{} exchange headers : {}", LEAP_LOG_KEY, exchange.getIn().getHeaders().toString());
		logger.info("{} woXML : {}", LEAP_LOG_KEY, exchange.getIn().getBody(String.class));
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			if (pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode() != null
					&& pipeactivity.getPropertiesActivity().getPropertiesMapping().get(i).isOverrideExistingNode()) {
				Element nodeListElement = (Element) nodeList.item(0);
				Element existingNode = (Element) nodeListElement.getElementsByTagName(nodeToAdd).item(0);
				existingNode.setTextContent((String) exchange.getIn().getHeader(res));
			} else {
				// Node has been added fetched from the pipline configuration
				Element node = xmlDocument.createElement(nodeToAdd);
				node.setTextContent((String) exchange.getIn().getHeader(res));
				// appended the element as a child to the node fetched from the
				// xpath
				nodeList.item(0).appendChild(node);
			}
			appendedDocument = documentToString(xmlDocument);
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to compile the xpath expression at index - "
					+ " when evaluating document - " + xmlDocument + "..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return appendedDocument;
	}// ..end of the method

	/**
	 * locally used to instantiate the transformation factory and to process the
	 * transformation
	 * 
	 * @param inputXml
	 * @param xsltName
	 * @return transformed xml
	 * @throws ActivityEnricherException
	 * @throws TransformerException
	 */
	private String documentToString(Document xmlDocument) throws ActivityEnricherException, TransformerException {
		DOMSource domSource = new DOMSource(xmlDocument);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
		transformer.transform(domSource, result);
		return writer.toString();
	}// ..end of the method

	/**
	 * To get Value From XmlDocument based on given xpath
	 * 
	 * @param document
	 * @param exhExchange
	 * @return
	 * @throws SCNotifyRequestProcessingException
	 */
	public String getvalueFromDocument(Document document, String xpath) {
		String methodName = "getvalueFromDocument";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String val = null;
		if (document != null) {

			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeTypeList;
			try {
				nodeTypeList = (NodeList) xPath.compile((String) xpath).evaluate(document, XPathConstants.NODESET);
				// checking if
				if (nodeTypeList != null && nodeTypeList.getLength() > 0) {
					Node node = nodeTypeList.item(0);
					val = node.getTextContent();
				}
			} catch (XPathExpressionException e) {
				logger.error("{} Error in Evavluating  Xpath Expression to get  value  ", LEAP_LOG_KEY, e);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return val;
	}// .. End of Method getValueFromXmlData
}