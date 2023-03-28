package com.attunedlabs.eventframework.dispatcher.transformer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * This class is to Generate Custom event xml from Event Object
 * 
 * @author Reactiveworks42
 *
 */
public class XmlTransformerHelper {
	protected static final Logger logger = LoggerFactory.getLogger(XmlTransformerHelper.class);

	/**
	 * This method is to create custom event xml using xslt
	 * 
	 * @param eventxml : Event xml in String format
	 * @return custom event xml in String format
	 */
	public String createCustomXml(String eventxml, String xslname, String xsltAsString) {
		String methodName = "createCustomXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName); // #TODO PROPER Exception Handling
		StringWriter stringwriter = new StringWriter();

		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(new StreamSource(new StringReader(xsltAsString)));

			try {
				transformer.transform(new StreamSource(new StringReader(eventxml)), new StreamResult(stringwriter));
			} catch (TransformerException e) {
				// TODO will create custom exception here
				logger.error("unable to transform event xml to xustom event xml :{} {} ", LEAP_LOG_KEY, e.getMessage());
			}
		} catch (TransformerConfigurationException e) {
			// TODO will create custom exception here
			logger.error("{} unable to configure Transformer from classpath for resource :{} -- {} ", LEAP_LOG_KEY,
					xslname, e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			// TODO will create custom exception here
			logger.error("{} unable to get the TranformerConfiguration Object :{} ", LEAP_LOG_KEY, e.getMessage());
		}
		return stringwriter.toString();
	}

	/**
	 * This method is to convert Event Object to xml format
	 * 
	 * @param event : LeapEvent Object
	 * @return : event xml in String format
	 */
	public String convertEventObjectToXml(LeapEvent event) {
		String methodName = "convertEventObjectToXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Serializable> eventParam = event.getObject();
		Map<String, Serializable> eventHeader = event.getMetadata();
		Set<String> headerSets = eventHeader.keySet();
		Set<String> paramSets = eventParam.keySet();
		logger.trace("{} Header Sets :{} => {} ", LEAP_LOG_KEY, headerSets, eventHeader.toString());
		logger.trace("{} Param Sets :{} => {} ", LEAP_LOG_KEY, paramSets, eventParam.toString());

		// Adding own Code
		paramSets.remove(ConfigurationConstant.TENANT_ID);

		logger.trace("{} Header Sets :{} => {} ", LEAP_LOG_KEY, headerSets, eventHeader.toString());
		logger.trace("{} Param Sets :{} => {} ", LEAP_LOG_KEY, paramSets, eventParam.toString());

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
		doc.appendChild(rootElement);

		// event elements
		Element eventid = doc.createElement(EventFrameworkConstants.EVENT_ID_KEY);
		eventid.appendChild(doc.createTextNode(event.getId()));
		rootElement.appendChild(eventid);
		// event elements
		Element eventParamE = doc.createElement(EventFrameworkConstants.OBJECT_KEY);
		rootElement.appendChild(eventParamE);
		// event elements
		Element eventHeaderE = doc.createElement(EventFrameworkConstants.METADATA_KEY);
		rootElement.appendChild(eventHeaderE);

		// set attribute to tenant element
		RequestContext reqCtx = event.getRequestContext();

		Element tenantE = doc.createElement(LeapHeaderConstant.TENANT_KEY);
		tenantE.appendChild(doc.createTextNode(reqCtx.getTenantId()));
		eventHeaderE.appendChild(tenantE);
		Element siteE = doc.createElement(LeapHeaderConstant.SITE_KEY);
		siteE.appendChild(doc.createTextNode(reqCtx.getSiteId()));
		eventHeaderE.appendChild(siteE);
		Element featureGE = doc.createElement(LeapHeaderConstant.FEATURE_GROUP_KEY);
		featureGE.appendChild(doc.createTextNode(reqCtx.getFeatureGroup()));
		eventHeaderE.appendChild(featureGE);
		Element featureNE = doc.createElement(LeapHeaderConstant.FEATURE_KEY);
		featureNE.appendChild(doc.createTextNode(reqCtx.getFeatureName()));
		eventHeaderE.appendChild(featureNE);
		for (String headerEventKey : headerSets) {
			JSONObject headerJobj = null;
			Object value = eventHeader.get(headerEventKey);
			String headerValue = "";
			if (value != null)
				headerValue = value.toString();
			logger.trace("{} Header Value: ={}", LEAP_LOG_KEY, headerValue);
			try {

				Element headerEle = doc.createElement(headerEventKey);
				try {
					headerJobj = new JSONObject(headerValue);
					headerValue = XML.toString(headerJobj);
				} catch (Exception e1) {
					logger.error("{} Cannot get EventHeader from Serializable Object into JSONObject", LEAP_LOG_KEY);
				}

				headerEle.appendChild(doc.createTextNode(headerValue));
				eventHeaderE.appendChild(headerEle);
			} catch (Exception e) {
				logger.warn("{} empty value found for :{} ", LEAP_LOG_KEY, headerEventKey);
			}
		}

		for (String paramEventKey : paramSets) {
			JSONObject paramJobj = null;
			Object value = eventParam.get(paramEventKey);
			String paramValue = "";
			if (value != null)
				paramValue = value.toString();

			if (paramValue != null && paramValue.startsWith("{") && !paramValue.isEmpty()) {
				try {
					try {
						logger.trace("{} Param value =:-{}  param Event key :={}", LEAP_LOG_KEY, paramValue,
								paramEventKey.toString());
						paramJobj = new JSONObject(paramValue);
						paramValue = XML.toString(paramJobj, paramEventKey);

					} catch (Exception e1) {
						logger.warn("{} Cannot get EventParam from Serializable Object into JSONObject", LEAP_LOG_KEY);
					}
					logger.trace("{} param value in xml {}", LEAP_LOG_KEY, paramValue);
					Document doc2 = docBuilder.parse(new ByteArrayInputStream(paramValue.getBytes()));
					Node node = doc.importNode(doc2.getDocumentElement(), true);
					eventParamE.appendChild(node);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warn("empty value found for :{} {} ", LEAP_LOG_KEY, paramEventKey);
				}
			}
		}

		logger.debug("{} XML Event Format={}", LEAP_LOG_KEY, getEventXmlAsString(doc));
		return getEventXmlAsString(doc);
	}

	/**
	 * method to convert xml string into Document
	 * 
	 * @param subject
	 * @return
	 */
	public static Document parse(String subject) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			return factory.newDocumentBuilder().parse(new InputSource(new StringReader(subject)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error("Unable to Parse EventParam XML String");
		}
		return null;
	}

	/**
	 * This method is to get event xml in String format
	 * 
	 * @param source : DOMSource Object
	 * @return : event xml in String format
	 */
	private String getEventXmlAsString(Document document) {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			return output;
		} catch (NullPointerException | TransformerException e) {
			logger.error("error in tranforming leapevent xml into String writer object");
		}
		return "";

	}

	/**
	 * method to load the node of the eventParams from the jsonObject and store it
	 * in the doc
	 * 
	 * @param doc
	 * @param headerJobj
	 * @param eventHeader
	 * @param headerEventKey
	 * @param headerToXMLString
	 * @return
	 */
	public Node getHeaderNode(Document doc, JSONObject headerJobj, Map<String, Serializable> eventHeader,
			String headerEventKey, RequestContext reqCtx) {
		String headerToXMLString = null;
		try {

			headerJobj = new JSONObject(eventHeader.get(headerEventKey).toString());
		} catch (JSONException e1) {
			eventHeader.get(headerEventKey);
			logger.error("{} Cannot get EventParam from Serializable Object into JSONObject", LEAP_LOG_KEY);
		}
		// converting the JSONObject into XML from eventHeader and
		// eventParam
		try {
			headerToXMLString = XML.toString(headerJobj);
			headerToXMLString = "<" + headerEventKey + ">" + headerToXMLString + "</" + headerEventKey + ">";
			logger.debug("eventHeader in XML Format : {} {}", LEAP_LOG_KEY, headerToXMLString);
		} catch (JSONException e) {
			logger.error("The EventParameter of {}  cannot be converted into xml Doc  : {}", LEAP_LOG_KEY,
					headerEventKey, e);
		}

		// creating a document object from xmlString of event parameters and
		// header
		Document headerDoc = parse(headerToXMLString);
		// got the nodeList of headerParam - Header
		NodeList headerNodeList = headerDoc.getElementsByTagName(headerEventKey);
		Element headerElement = (Element) headerNodeList.item(0);
		// appending the nodes into the existing Document object
		return doc.importNode(headerElement, true);
	}

	/**
	 * method to load the node of the eventParams from the jsonObject and store it
	 * in the doc
	 * 
	 * @param doc
	 * @param paramJobj
	 * @param eventParam
	 * @param reqCtx
	 * @return Node
	 */
	public Node getParamNode(Document doc, JSONObject paramJobj, Map<String, Serializable> eventParam,
			String paramEventKey, RequestContext reqCtx) {
		String paramToXMLString = null;
		// getting the jsonObject from eventParam and eventHeader
		try {
			paramJobj = new JSONObject(eventParam.get(paramEventKey).toString());
		} catch (JSONException e1) {
			logger.error("{} Cannot get EventParam from Serializable Object into JSONObject", LEAP_LOG_KEY);
		}
		// converting the JSONObject into XML from eventHeader and
		// eventParam
		try {
			paramToXMLString = XML.toString(paramJobj);
			paramToXMLString = "<" + paramEventKey + ">" + paramToXMLString + "</" + paramEventKey + ">";
			logger.debug("eventParam in XML Format :{} {} ", LEAP_LOG_KEY, paramToXMLString);
		} catch (JSONException e) {
			logger.error("The EventParameters cannot be converted into xml Doc  :{} {} ", LEAP_LOG_KEY, e);
		}
		// creating a document object from xmlString of event parameters and
		// header
		Document paramDoc = parse(paramToXMLString);
		// got the nodeList of eventParam - Payload
		NodeList paramNodeList = paramDoc.getElementsByTagName(paramEventKey);
		Element paramElement = (Element) paramNodeList.item(0);
		return doc.importNode(paramElement, true);

	}

	/**
	 * This method is to get the child nodes for the element
	 * 
	 * @param obj : Object
	 * @param doc : Document Object
	 * @param ele : Element Object
	 * @return Element
	 * @throws IllegalAccessException
	 */
	private Element getChildNodes(Object obj, Document doc, Element ele) throws IllegalAccessException {

		// getting the simple name of class in object
		String simpleClassName = obj.getClass().getSimpleName();

		// getting the fully qualified class name in object
		String fqcn = obj.getClass().getName();

		logger.debug("{} FQCN :{} ", LEAP_LOG_KEY, fqcn);

		// loading class and methods
		Class<?> classobj = null;

		try {
			classobj = Class.forName(fqcn);
		} catch (ClassNotFoundException e1) {
			logger.error("{} unable to load the class of name : {}  beacuse :{} ", LEAP_LOG_KEY, fqcn, e1.getMessage());
		}

		// get all the declare method from loaded class
		Method[] methods = classobj.getDeclaredMethods();

		// creating element using simple name of a class
		Element innerdata = doc.createElement(simpleClassName);
		ele.appendChild(innerdata);

		// getting methods in the class loaded dynamically
		for (Method method : methods) {
			String allmethod = method.getName();

			// getting only those method which starts from get
			if (allmethod.startsWith("get")) {

				// getting the parameters method take
				Type[] methodParamType = method.getGenericParameterTypes();

				// checking if getter method doesnot take any param
				if (methodParamType.length == 0) {
					try {
						Object methodObj = method.invoke(obj);
						logger.debug("{} method object :{} ", LEAP_LOG_KEY, methodObj);
						if (methodObj instanceof String) {
							logger.debug("method return type is string {}", LEAP_LOG_KEY);
							String strdata = (String) methodObj;
							Element innerdatachild = doc.createElement("childnode");
							innerdatachild.appendChild(doc.createTextNode(strdata));
							innerdata.appendChild(innerdatachild);

						} else if (methodObj instanceof ArrayList<?>) {
							logger.debug("{} method return type is ArrayList", LEAP_LOG_KEY);
							List<?> loyobj = (ArrayList<?>) methodObj;
							for (Object objjj : loyobj) {
								getChildNodes(objjj, doc, innerdata);
							}

						} else {
							logger.debug("{} inside else {}", LEAP_LOG_KEY, methodObj);
							if (methodObj != null)
								getChildNodes(methodObj, doc, innerdata);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				} // no. of params method take zero
			}
		}
		return innerdata;

	}
}
