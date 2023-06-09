package com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.attunedlabs.integrationfwk.activities.bean.ActivityEnricherException;
import com.attunedlabs.integrationfwk.config.jaxb.JDBCIntActivity;
import com.attunedlabs.integrationfwk.config.jaxb.XmlFieldMapper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.IJdbcIntActivityService;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.impl.JdbcIntActivityConfigDAO;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * Class which aids the jdbcIntactivity object creation and operations on it
 * 
 * @author Bizruntime
 *
 */
public class JdbcIntActivityConfigHelper {

	private final static Logger logger = LoggerFactory.getLogger(JdbcIntActivityConfigHelper.class.getName());
	private static final String SIMPLE_DATE_FORMAT = "MM/dd/yy HH:mm:ss";
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final String TIMEZONE_ID = "UTC";
	private static final String UPDT_QUERY_UPPER_PATTERN = "SET(.*?)WHERE";
	private static final String UPDT_QUERY_LOWER_PATTERN = "set(.*?)where";
	private static final String PARANTHESIS_PARSE_PATTERN = "\\(([^)]+)\\)";
	private static final String CONSTRAINTS = "constraintOne";
	private static final String JOIN = "join";
	private static final String INNER_JOIN = "inner join";
	private static final String LEFT_OUTER_JOIN = "left outer join";
	private static final String RIGHT_OUTER_JOIN = "right outer join";
	private static final String TABLE_ONE = "tableOne";
	private static final String TABLE_TWO = "tableTwo";
	private static final String WHERE_UPPER = "(WHERE)";
	private static final String WHERE_LOWER = "(where)";
	public static final String REPLACER = "~!@##@!~";
	public static final String SPACEREPLACER = "~!@#S#@!~";
	// public static final String SPACE_ONLY_REPLACER = "~!@#OS#@!~";

	private JdbcDataContext jdbcDatacontext;
	private UpdateableDataContext updateableDataContext;
	private DataContext dataContext;
	private String operation;
	private String queryString;

	/**
	 * this is the default constructor
	 */
	public JdbcIntActivityConfigHelper() {
		super();
	}

	public JdbcIntActivityConfigHelper(JdbcDataContext jdbcDatacontext, String operation, String queryString) {
		super();
		this.jdbcDatacontext = jdbcDatacontext;
		this.operation = operation;
		this.queryString = queryString;
	}

	public JdbcIntActivityConfigHelper(String operation, String queryString) {
		super();
		this.operation = operation;
		this.queryString = queryString;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public UpdateableDataContext getUpdateableDataContext() {
		return updateableDataContext;
	}

	public void setUpdateableDataContext(UpdateableDataContext updateableDataContext) {
		this.updateableDataContext = updateableDataContext;
	}

	public DataContext getDataContext() {
		return dataContext;
	}

	public void setDataContext(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	/**
	 * called locally to enrich the request xml with the values which is configured
	 * for the SelectQuery in JdbcIntActivity
	 * 
	 * @param xmlDocument
	 * @param configObject
	 * @param colums
	 * @param listOfValues
	 * @return
	 * @throws ActivityEnricherException
	 */
	public String processxmlEnrichment(Document xmlDocument, JDBCIntActivity configObject, ArrayList<String> colums,
			List<Object> listOfValues) throws ActivityEnricherException {
		String methodName = "processxmlEnrichment";
		logger.debug("{} entered into the method {}, colums-listOfValues={}-{}", LEAP_LOG_KEY, methodName, colums,
				listOfValues);
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node = null;
		String attributeValue = null;
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		List<XmlFieldMapper> listOfRowMappers = configObject.getRowMapper().getXmlFieldMapper();
		for (XmlFieldMapper xmlFieldMapper : listOfRowMappers) {
			String xpath = xmlFieldMapper.getXpath();
			String colKey = xmlFieldMapper.getColumnKey();
			int colIndex = colums.indexOf(colKey);
			if (!hasAttribute(xpath)) {
				setElementsToExistingxml(node, xPath, xpath, xmlDocument, listOfValues, colIndex);
			} else {
				setAttributesToExistingxml(attributeValue, xPath, xpath, xmlDocument, listOfValues, colIndex);
			}
		}
		try {
			DOMSource domSource = new DOMSource(xmlDocument);
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");

			transformer.transform(domSource, result);
		} catch (TransformerException e) {
			throw new ActivityEnricherException("Unable to save the enrichment for the Xml document"
					+ xmlDocument.toString() + " for the xpaths specified.. ", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return writer.toString();
	}// ..end of the method

	/**
	 * To set the Elements/tags to the existing xml(Enrichment)
	 * 
	 * @param node
	 * @param xPath
	 * @param xpath
	 * @param xmlDocument
	 * @param listOfValues
	 * @param colIndex
	 * @throws ActivityEnricherException
	 */
	private void setElementsToExistingxml(Node node, XPath xPath, String xpath, Document xmlDocument,
			List<Object> listOfValues, int colIndex) throws ActivityEnricherException {
		try {
			node = (Node) xPath.compile(xpath).evaluate(xmlDocument, XPathConstants.NODE);
			if (node == null || node.equals("") || node.equals(null)) {
				String[] nodesFromXpath = xpath.split("/");
				String nodeParentName = nodesFromXpath[nodesFromXpath.length - 2];
				NodeList nodeList = xmlDocument.getElementsByTagName(nodeParentName);
				Node newNode = nodeList.item(0);
				String newTagName = nodesFromXpath[nodesFromXpath.length - 1];
				Element newElement = xmlDocument.createElement(newTagName);
				Text text = xmlDocument.createTextNode(listOfValues.get(colIndex).toString());
				newElement.appendChild(text);
				newNode.appendChild(newElement);
			} else {
				node.setTextContent(listOfValues.get(colIndex).toString());
			}
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to enrich the Xml document for the elements in.."
					+ xmlDocument.toString() + " for for the xpaths specified.. ", e);
		}
	}// .. end of the method

	/**
	 * To set the attributes if specified in the xpath, enrichment for the incoming
	 * xml
	 * 
	 * @param attributeValue
	 * @param xPath
	 * @param xpath
	 * @param xmlDocument
	 * @param listOfValues
	 * @param colIndex
	 * @throws ActivityEnricherException
	 */
	private void setAttributesToExistingxml(String attributeValue, XPath xPath, String xpath, Document xmlDocument,
			List<Object> listOfValues, int colIndex) throws ActivityEnricherException {
		try {
			attributeValue = (String) xPath.compile(xpath).evaluate(xmlDocument, XPathConstants.STRING);
			String[] arr = xpath.split("/@");
			String newExpression = arr[0];
			String attrKey;
			try {
				attrKey = arr[1];
			} catch (ArrayIndexOutOfBoundsException aie) {
				throw new ActivityEnricherException("Invalid attribute configured in pipeLine Congiguration: " + xpath,
						aie);
			}
			Node nodeofAttr = (Node) xPath.compile(newExpression).evaluate(xmlDocument, XPathConstants.NODE);
			if (!nodeofAttr.getAttributes().getNamedItem(attributeValue).equals(null)) {
				Element el1 = (Element) nodeofAttr;
				el1.setAttribute(arr[1], listOfValues.get(colIndex).toString());
			} // ..if the attribute Key exists, and have to set a new value to
				// the existing attributes..
			else {
				NodeList nodeList = (NodeList) xPath.compile(newExpression).evaluate(xmlDocument,
						XPathConstants.NODESET);
				Element el = (Element) nodeList.item(0);
				el.setAttribute(attrKey, listOfValues.get(colIndex).toString());
			} // ..if the attribute specified doesn't exist, set the new
				// attribute to the existing xml document
		} catch (XPathExpressionException e) {
			throw new ActivityEnricherException("Unable to enrich the Xml document for the Attribute in.."
					+ xmlDocument.toString() + " for for the xpaths specified.. ", e);
		}
	}

	/**
	 * Regex for identifying whether specified xpath has attribues...
	 * 
	 * @param xpathExpression
	 * @return
	 */
	private static boolean hasAttribute(String xpathExpression) {

		String re1 = ".*?"; // Non-greedy match on filler
		String re2 = "(@)"; // Any Single Character 1

		Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(xpathExpression);
		if (m.find()) {
			return true;
		}
		return false;
	}// ..end of the method

	/**
	 * to process the xpath expression on document to get the respective FiledValues
	 * to be substituted by
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @return non duplicate values as set
	 * @throws JdbcIntActivityConfigurationException
	 */
	public String xpathProcessingOnInputXml(String expression, Document xmlDocument)
			throws JdbcIntActivityConfigurationException {
		String methodName = "xpathProcessingOnInputXml";
		logger.debug("{} entered into the method {}, Expression={} ", LEAP_LOG_KEY, methodName, expression);
		XPath xPath = XPathFactory.newInstance().newXPath();

		String fieldVal = null;
		NodeList nodeList = null;
		try {

			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

			if (nodeList != null) {
				logger.trace("{} The length of nodeList: {}", LEAP_LOG_KEY, nodeList.getLength());
				logger.debug("{} node Name : {}", LEAP_LOG_KEY, nodeList.item(0).getNodeName());

				for (int i = 0; i < nodeList.getLength(); i++) {
					if (nodeList.item(0).getChildNodes().getLength() == 1) {
						fieldVal = nodeList.item(0).getTextContent().trim();
						// fieldValList.add(val.replaceAll(" ",
						// SPACE_ONLY_REPLACER));
						// fieldValList.add(val);
						logger.trace("{} val : {} ", LEAP_LOG_KEY, fieldVal);
						// logger.debug("nodeList.item(0).getTextContent() : " +
						// nodeList.item(0).getTextContent());
					} else {
						org.w3c.dom.Node givenNode = nodeList.item(0);
						// fieldValList.add(nodeToString(givenNode).replaceAll("
						// ", SPACE_ONLY_REPLACER));
						fieldVal = nodeToString(givenNode);

						logger.trace("{} nodeToString(givenNode) : {}", LEAP_LOG_KEY, fieldVal);
					}
				}
			}
		} catch (XPathExpressionException e) {
			throw new JdbcIntActivityConfigurationException("Unable to compile the xpath expression at index"
					+ " when evaluating document - " + xmlDocument + "..", e);
		}

		if (fieldVal == null) {
			throw new JdbcIntActivityConfigurationException(
					"Unable to get the substitutable fields from the fieldMapper configured - listOfSubstitutable fields are -"
							+ fieldVal);
		} else {
			// logger.debug("List of FieldSubstitutable non-empty Values: " +
			// fieldValList);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return fieldVal;
		}
	}// ..end of the method

	/**
	 * to process the xpath expression on document to get the respective FiledValues
	 * to be substituted by
	 * 
	 * @param expression
	 * @param xmlDocument
	 * @return non duplicate values as set
	 * @throws JdbcIntActivityConfigurationException
	 */
	public List<Object> xpathProcessingOnInputXml(Set<String> expression, Document xmlDocument)
			throws JdbcIntActivityConfigurationException {
		if (expression == null || expression.isEmpty())
			return null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String methodName = "xpathProcessingOnInputXml";
		logger.debug("{} entered into the method {}, expression = {} and size={} ", LEAP_LOG_KEY, methodName,
				expression, expression.size());
		String fieldVal;
		List<Object> fieldValList = new ArrayList<>();
		for (int x = 0; x < expression.size(); x++) {
			NodeList nodeList = null;
			try {

				nodeList = (NodeList) xPath.compile((String) expression.toArray()[x]).evaluate(xmlDocument,
						XPathConstants.NODESET);
				if (nodeList != null) {
					logger.trace("{} The length of nodeList: {}", LEAP_LOG_KEY, nodeList.getLength());
					logger.debug("{} Node Name:{} ", LEAP_LOG_KEY, nodeList.item(0).getNodeName());

					for (int i = 0; i < nodeList.getLength(); i++) {
						if (nodeList.item(0).getChildNodes().getLength() == 1) {
							String val = nodeList.item(0).getTextContent().trim();
							// fieldValList.add(val.replaceAll(" ",
							// SPACE_ONLY_REPLACER));
							fieldValList.add(val);
							logger.trace("{} value : {}", LEAP_LOG_KEY, val);

							// logger.debug("nodeList.item(0).getTextContent() :
							// " + nodeList.item(0).getTextContent());
						} else {
							org.w3c.dom.Node givenNode = nodeList.item(0);

							// fieldValList.add(nodeToString(givenNode).replaceAll("
							// ", SPACE_ONLY_REPLACER));
							fieldVal = nodeToString(givenNode);
							fieldValList.add(fieldVal);
							logger.trace("{} nodeToString(givenNode) : {}", LEAP_LOG_KEY, fieldVal);
						}
					}
				}
			} catch (XPathExpressionException e) {
				throw new JdbcIntActivityConfigurationException("Unable to compile the xpath expression at index - " + x
						+ " when evaluating document - " + xmlDocument + "..", e);
			}
		}
		if (!fieldValList.isEmpty()) {
			logger.trace("{} List of FieldSubstitutable non-empty Values: {}", LEAP_LOG_KEY, fieldValList);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return fieldValList;
		} else {
			throw new JdbcIntActivityConfigurationException(
					"Unable to get the substitutable fields from the fieldMapper configured - listOfSubstitutable fields are -"
							+ fieldValList);
		}
	}// ..end of the method

	/*
	 * public List<Object> xpathProcessingOnInputXml(Set<String> expression,
	 * Document xmlDocument) throws JdbcIntActivityConfigurationException {
	 * logger.debug( "The expressionSet inside xpathProcessor: " + expression +
	 * "and its size is: " + expression.size()); Object fieldVal; XPath xPath =
	 * XPathFactory.newInstance().newXPath(); List<Object> fieldValList = new
	 * ArrayList<>(); for (int x = 0; x < expression.size(); x++) { NodeList
	 * nodeList = null; try { nodeList = (NodeList) xPath.compile((String)
	 * expression.toArray()[x]).evaluate(xmlDocument, XPathConstants.NODESET); Node
	 * childNode = nodeList.item(0);
	 * 
	 * logger.debug("The length of nodeList: " + nodeList.getLength());
	 * logger.debug("The length of nodeList name :  " +
	 * nodeList.item(0).getNodeName()); if (childNode.getNodeType() ==
	 * Node.TEXT_NODE) { fieldValList.add(nodeList.item(0).getTextContent());
	 * logger.debug("nodeList.item(0).getTextContent() : " +
	 * nodeList.item(0).getTextContent()); } else { org.w3c.dom.Node givenNode =
	 * nodeList.item(0); fieldValList.add(nodeToString(givenNode));
	 * logger.debug("nodeToString(givenNode) : " + nodeToString(givenNode)); }
	 * 
	 * } catch (XPathExpressionException e) { throw new
	 * JdbcIntActivityConfigurationException(
	 * "Unable to compile the xpath expression at index - " + x +
	 * " when evaluating document - " + xmlDocument + "..", e); } } if
	 * (!fieldValList.isEmpty()) { logger.debug(
	 * "List of FieldSubstitutable non-empty Values: " + fieldValList); return
	 * fieldValList; } else { throw new JdbcIntActivityConfigurationException(
	 * "Unable to get the substitutable fields from the fieldMapper configured - listOfSubstitutable fields are -"
	 * + fieldValList); } }
	 */// ..end of the method

	/**
	 * to generate the document object once and all from the xml input which is of
	 * String
	 * 
	 * @param xmlInput
	 * @return documentObject
	 * @throws JdbcIntActivityConfigurationException
	 */
	public Document generateDocumentFromString(String xmlInput) throws JdbcIntActivityConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document xmlDocument;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new JdbcIntActivityConfigurationException("Unable to initiate the document builder..", e);
		}
		try {

			xmlDocument = builder.parse(new ByteArrayInputStream(xmlInput.getBytes("UTF-16")));
		} catch (SAXException | IOException e) {
			throw new JdbcIntActivityConfigurationException("Unable to parse the xmlString into document..", e);
		}
		return xmlDocument;
	}// ..end of method

	/**
	 * to process the substitution of the values like fld1,fld2,fld3 etc with its
	 * respective values
	 * 
	 * @param configObject
	 * @param sqlConfigQuery
	 * @param configField
	 * @param fieldValue
	 * @return new Sql substituted query
	 * @throws JdbcIntActivityConfigurationException
	 */
	public String processSqlFieldSubstitution(String sqlConfigQuery, List<String> configField, List<Object> fieldValue,
			String operation) throws JdbcIntActivityConfigurationException {
		String methodName = "processSqlFieldSubstitution";
		logger.debug("{} entered into the method {}, configFiled={}, fieldValue={}", LEAP_LOG_KEY, methodName,
				configField, fieldValue);
		String substitutedQuery = null;
		String temp;
		String val = null;
		if (configField == null || configField.isEmpty())
			return sqlConfigQuery;
		String sqlQureyTobeReplaced = sqlConfigQuery;
		logger.trace("column length : ", LEAP_LOG_KEY, configField.size());
		logger.trace("column values length : ", LEAP_LOG_KEY, fieldValue.size());
		for (int i = 0; i < configField.size(); i++) {
			temp = sqlQureyTobeReplaced;
			// #TODO substitute string value " checking added string value
			// support for all condtion'
			val = (String) fieldValue.toArray()[i];
			try {
				if (val != null) {
					if (operation.equalsIgnoreCase("SELECT") || operation.equalsIgnoreCase("DELETE")) {
						val = val.replaceAll(", ", SPACEREPLACER).replaceAll(",", REPLACER);
						substitutedQuery = temp.replaceAll(configField.get(i), "'" + val + "'");
					} else if (operation.equalsIgnoreCase("UPDATE")) {
						val = val.replaceAll(", ", SPACEREPLACER).replaceAll(",", REPLACER);
						substitutedQuery = temp.replaceAll(configField.get(i), "'" + val + "'");
					} else {
						substitutedQuery = temp.replaceAll(configField.get(i), val);
					}

				} else {
					substitutedQuery = temp.replaceAll(configField.get(i), val);

				}
			} catch (ClassCastException | NullPointerException cce) {
				throw new JdbcIntActivityConfigurationException(
						"Unable to substitute the Sql string field maps with the corresponding values..", cce);
			}
			sqlQureyTobeReplaced = substitutedQuery;
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return substitutedQuery;
	}// ..end of the method

	/**
	 * This method is used to decide which operation implementation to be called
	 * based on SQL operation type (Select|Insert|Update|Delete). #TODO, update and
	 * delete impl is not yet provided, work in progress
	 * 
	 * @param configObject
	 * 
	 * @param operation
	 * @param queryString
	 * @param updateableDataContext
	 * @param datacontext
	 * @return
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityStringParserException
	 * @throws JdbcIntActivityConfigurationException
	 */
	public Object decideQueryToPerform(String dbType, Exchange exchange, JDBCIntActivity configObject, String operation,
			String queryString, UpdateableDataContext updateableDataContext, DataContext datacontext,
			Map<String, String> setOfValuesProcessed, Document xmlDocument) throws JdbcIntActivityExecutionException,
			JdbcIntActivityStringParserException, JdbcIntActivityConfigurationException {
		String methodName = "decideQueryToPerform";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		switch (operation) {
		case "INSERT":
			logger.debug("{} Executing INSERT - operation..", LEAP_LOG_KEY);
			return performInsertOperation(dbType, exchange, configObject, updateableDataContext, queryString,
					setOfValuesProcessed, xmlDocument);

		case "UPDATE":
			logger.debug("{} Executing UPDATE - operation..", LEAP_LOG_KEY);
			return performUpdateOperationForCassandra(updateableDataContext, queryString, setOfValuesProcessed,
					xmlDocument);

		case "DELETE":
			logger.debug("{} Executing DELETE - operation..", LEAP_LOG_KEY);
			return performDeleteOperationForCassandra(updateableDataContext, queryString, setOfValuesProcessed,
					xmlDocument);

		case "SELECT":
			logger.debug("{} Executing SELECT - operation..", LEAP_LOG_KEY);
			Row selectOutput = performSelectOperationForCassandra(datacontext, queryString, setOfValuesProcessed,
					xmlDocument);
			logger.info("{} The output from select operation: {}", LEAP_LOG_KEY, selectOutput);
			return selectOutput;
		default:
			throw new JdbcIntActivityExecutionException(
					"Unable to process the request for the configuration: for the operation - " + operation
							+ "-for the queryString-" + queryString
							+ "Accepted operations are 'INSERT','UPDATE','DELETE','SELECT'");
		}

	}

	/**
	 * to aid the operation mentioned successfully, exception thrown when syntax
	 * malformed
	 * 
	 * @return Objects, either String/Integer
	 * 
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityStringParserException
	 */
	public Object chooseExecutor(String dbType, Exchange exchange, JDBCIntActivity configObject)
			throws JdbcIntActivityExecutionException, JdbcIntActivityStringParserException {

		switch (operation) {
		case "INSERT":
			logger.debug("{} Executing INSERT - operation..", LEAP_LOG_KEY);
			return performInsertOperation(dbType, exchange, configObject, jdbcDatacontext, queryString);

		case "UPDATE":
			logger.debug("{} Executing UPDATE - operation..", LEAP_LOG_KEY);
			return performUpdateOperation(jdbcDatacontext, queryString);

		case "DELETE":
			logger.debug("{} Executing DELETE - operation..", LEAP_LOG_KEY);
			return performDeleteOperation(jdbcDatacontext, queryString);

		case "SELECT":
			logger.debug("{} Executing SELECT - operation..", LEAP_LOG_KEY);
			Row selectOutput = performSelectOperation(jdbcDatacontext, queryString);
			logger.info("{} The output from select operation: {}", LEAP_LOG_KEY, selectOutput);
			return selectOutput;
		default:
			throw new JdbcIntActivityExecutionException(
					"Unable to process the request for the configuration: for the operation - " + operation
							+ "-for the queryString-" + queryString
							+ "Accepted operations are 'INSERT','UPDATE','DELETE','SELECT'");
		}
	}// ..end of the method

	/**
	 * accessed when operation is INSERT
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return returns successful integer
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityStringParserException
	 */
	private int performInsertOperation(String dbType, Exchange exchange, JDBCIntActivity configObject,
			JdbcDataContext datacontext, String queryString)
			throws JdbcIntActivityExecutionException, JdbcIntActivityStringParserException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();

		String insertTableName = getTableNameFromInsertQuery(queryString);
		logger.debug("{} Table Name found in insert Query : {}", LEAP_LOG_KEY, insertTableName);
		if (!insertTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(insertTableName);
			Set<String> columnKeySet = getColumnKeySetFromInsertQuery(dbType, exchange, queryString);
			ArrayList<Object> listOfValues = getRespectiveColumnValuestoInsert(dbType, exchange, queryString);
			try {
				return activityService.insertActivityConfigParams(datacontext, table, columnKeySet, listOfValues);
			} catch (JdbcIntActivityPersistenceException e1) {
				throw new JdbcIntActivityExecutionException(
						"Unable to perform INSERT operation for the JdbcIntActivityConfig..", e1);
			}
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the Insert query: " + queryString);
		}
	}// ..end of the method

	/**
	 * accessed when operation is INSERT
	 * 
	 * @param configObject
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return returns successful integer
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityStringParserException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private int performInsertOperation(String dbType, Exchange exchange, JDBCIntActivity configObject,
			UpdateableDataContext datacontext, String queryString, Map<String, String> setOfValuesProcessed,
			Document xmlDocument) throws JdbcIntActivityExecutionException, JdbcIntActivityStringParserException,
			JdbcIntActivityConfigurationException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String insertTableName = getTableNameFromInsertQuery(queryString);
		if (!insertTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(insertTableName);
			Set<String> columnKeySet = getColumnKeySetFromInsertQuery(dbType, exchange, queryString);
			ArrayList<Object> listOfValues = getRespectiveColumnValuestoInsert(dbType, exchange, queryString);
			return activityService.insertActivityConfigParams(dbType, exchange, configObject, datacontext, table,
					columnKeySet, listOfValues, setOfValuesProcessed, xmlDocument);
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the Insert query: " + queryString);
		}
	}// ..end of the method

	/**
	 * performed when operation is UPDATE
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return successful integer
	 * @throws JdbcIntActivityExecutionException
	 */
	private int performUpdateOperation(JdbcDataContext datacontext, String queryString)
			throws JdbcIntActivityExecutionException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String updateTableName = getTableNameFromUpdateQuery(queryString);
		logger.debug("{} Table name inside updateTableName : {} ", LEAP_LOG_KEY, updateTableName);
		if (!updateTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(updateTableName);
			Map<String, Object> mapPair = parseToGetColumnKeyValueMap(queryString);
			Set<String> columnUpdateKeySet = mapPair.keySet();
			ArrayList<Object> listOfUpdateValues = new ArrayList<>(mapPair.values());
			Map<String, Map<String, Object>> mapOfConstraints = getConstraintsFromUpdateQuery(queryString);
			try {
				return activityService.updateActivityConfigParams(datacontext, table, columnUpdateKeySet,
						listOfUpdateValues, mapOfConstraints);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException(
						"Unable to perform UPDATE operation for the JdbcIntActivityConfig..", e);
			}
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the query: " + queryString);
		}
	}// ..end of the method

	/**
	 * performed when operation is UPDATE
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return successful integer
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private int performUpdateOperationForCassandra(UpdateableDataContext datacontext, String queryString,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityExecutionException, JdbcIntActivityConfigurationException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String updateTableName = getTableNameFromUpdateQuery(queryString);
		logger.debug("{} Table name inside updateTableName for cassansdra :  {}", LEAP_LOG_KEY, updateTableName);
		if (!updateTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(updateTableName);
			Map<String, Object> mapPair = parseToGetColumnKeyValueMap(queryString);
			Set<String> columnUpdateKeySet = mapPair.keySet();
			ArrayList<Object> listOfUpdateValues = new ArrayList<>(mapPair.values());

			String whereConstraints = getConstraintsFromUpdateQueryForCassandra(queryString);
			try {
				return activityService.updateActivityConfigParamsForCassandra(datacontext, table, columnUpdateKeySet,
						listOfUpdateValues, whereConstraints, setOfValuesProcessed, xmlDocument);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException(
						"Unable to perform UPDATE operation for the JdbcIntActivityConfig..", e);
			}
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the query: " + queryString);
		}
	}// ..end of the method

	/**
	 * method to add config
	 * 
	 * 
	 */

	/**
	 * performed when operation is DELETE
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return successful integer
	 * @throws JdbcIntActivityExecutionException
	 */
	private int performDeleteOperation(JdbcDataContext datacontext, String queryString)
			throws JdbcIntActivityExecutionException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();

		String deleteTableName = getTableNameFromDeleteQuery(queryString);
		if (!deleteTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(deleteTableName);
			try {
				Map<String, Map<String, Object>> mapOfConstraints = getConstraintsFromDeleteQuery(queryString);
				return activityService.deleteActivityConfigParams(datacontext, table, mapOfConstraints);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException(
						"Unable to perform DELETE operation for the JdbcIntActivityConfig..", e);
			}
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the query: " + queryString);
		}
	}// ..end of the method

	/**
	 * performed when operation is DELETE
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return successful integer
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private int performDeleteOperationForCassandra(UpdateableDataContext datacontext, String queryString,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityExecutionException, JdbcIntActivityConfigurationException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String deleteTableName = getTableNameFromDeleteQuery(queryString).trim();
		logger.debug("{} query string from delete : {}", LEAP_LOG_KEY, queryString);
		if (!deleteTableName.isEmpty()) {
			Table table = datacontext.getDefaultSchema().getTableByName(deleteTableName);
			String whereConstraints = getConstraintsFromUpdateQueryForCassandra(queryString);
			try {
				return activityService.deleteActivityConfigParamsForCassandra(datacontext, table, whereConstraints,
						setOfValuesProcessed, xmlDocument);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException(
						"Unable to perform DELETE operation for the JdbcIntActivityConfig..", e);
			}
		} else {
			throw new JdbcIntActivityExecutionException(
					"Unable to get a non empty table name fom the query: " + queryString);
		}
	}// ..end of the method

	/**
	 * This method is used for node to String format
	 * 
	 * @param givenNode : Node Object
	 * @return
	 * @throws JdbcIntActivityConfigurationException
	 */
	Transformer t = null;
	{
		try {
			t = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, PipelineServiceConstant.YES);
		t.setOutputProperty(OutputKeys.ENCODING, PipelineServiceConstant.YES);
	}

	private String nodeToString(Node givenNode) throws JdbcIntActivityConfigurationException {

		String str = XmlUtils.elementToString(givenNode);
		return str;

	}

	/**
	 * performed when the operation is SELECT
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return
	 * @throws JdbcIntActivityExecutionException
	 */
	private Row performSelectOperation(JdbcDataContext datacontext, String queryString)
			throws JdbcIntActivityExecutionException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String selectTableName = getTableNameFromSelectQuery(queryString);
		boolean exists = queryString.toLowerCase().contains(JOIN.toLowerCase());
		boolean existsInnerJoin = queryString.toLowerCase().contains(INNER_JOIN.toLowerCase());
		boolean existsLeftOuterJoin = queryString.toLowerCase().contains(LEFT_OUTER_JOIN.toLowerCase());
		boolean existsRightOuterJoin = queryString.toLowerCase().contains(RIGHT_OUTER_JOIN.toLowerCase());
		Table table1 = null;
		Table table2 = null;
		try {
			table1 = datacontext.getDefaultSchema().getTableByName(selectTableName);
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException(
					"Unable to retreive the Table object, as it has returned with an empty table for selectOperation..",
					npe);
		}
		if (!exists) {
			ArrayList<String> columnSelectKeySet = getColumnNamesFromSelectQuery(queryString);
			Map<String, Map<String, Object>> mapOfConstraints = getConstraintsFromSelectQuery(queryString);
			try {
				return activityService.selectActivityConfigParams(datacontext, table1, null, columnSelectKeySet,
						mapOfConstraints, exists, null);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException("Unable to perform SELECT operation ..", e);
			}
		} // ..end of if, checks the condition if its a join query or not
		else {
			if (existsInnerJoin) {
				return prepareForInnerJoin(datacontext, existsInnerJoin);
			} // ..end of condition checking whether is innerJoin or not
			else if (existsLeftOuterJoin) {
				return prepareForLeftOuterJoin(datacontext, existsLeftOuterJoin);
			} // ..end of condition checking whether is leftOuterJoin
			else if (existsRightOuterJoin) {
				return prepareForRightOuterJoin(datacontext, existsRightOuterJoin);
			} // ..end of condition checking whether is rightOuterJoin
			else {
				throw new JdbcIntActivityExecutionException("Unable to decide the join operation in the query.. ");
			}
		}

	}// ..end of the method

	/**
	 * performed when the operation is SELECT using apache metamodel cassandra using
	 * DataContext
	 * 
	 * @param datacontext
	 * @param queryString
	 * @return
	 * @throws JdbcIntActivityExecutionException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Row performSelectOperationForCassandra(DataContext datacontext, String queryString,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityExecutionException, JdbcIntActivityConfigurationException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		String selectTableName = getTableNameFromSelectQuery(queryString);
		boolean exists = queryString.toLowerCase().contains(JOIN.toLowerCase());
		boolean existsInnerJoin = queryString.toLowerCase().contains(INNER_JOIN.toLowerCase());
		boolean existsLeftOuterJoin = queryString.toLowerCase().contains(LEFT_OUTER_JOIN.toLowerCase());
		boolean existsRightOuterJoin = queryString.toLowerCase().contains(RIGHT_OUTER_JOIN.toLowerCase());
		Table table1 = null;
		Table table2 = null;
		try {
			table1 = datacontext.getDefaultSchema().getTableByName(selectTableName);
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException(
					"Unable to retreive the Table object, as it has returned with an empty table for selectOperation..",
					npe);
		}
		if (!exists) {
			ArrayList<String> columnSelectKeySet = getColumnNamesFromSelectQuery(queryString);
			Map<String, Map<String, Object>> mapOfConstraints = getConstraintsFromSelectQuery(queryString,
					setOfValuesProcessed, xmlDocument);
			try {
				return activityService.selectActivityConfigParams(datacontext, table1, null, columnSelectKeySet,
						mapOfConstraints, exists, null);
			} catch (JdbcIntActivityPersistenceException e) {
				throw new JdbcIntActivityExecutionException("Unable to perform SELECT operation ..", e);
			}
		} // ..end of if, checks the condition if its a join query or not
		else {
			if (existsInnerJoin) {
				return prepareForInnerJoin(datacontext, existsInnerJoin);
			} // ..end of condition checking whether is innerJoin or not
			else if (existsLeftOuterJoin) {
				return prepareForLeftOuterJoin(datacontext, existsLeftOuterJoin);
			} // ..end of condition checking whether is leftOuterJoin
			else if (existsRightOuterJoin) {
				return prepareForRightOuterJoin(datacontext, existsRightOuterJoin);
			} // ..end of condition checking whether is rightOuterJoin
			else {
				throw new JdbcIntActivityExecutionException("Unable to decide the join operation in the query.. ");
			}
		}

	}// ..end of the method

	/**
	 * preparation , execution of the leftOuterJoin select operation
	 * 
	 * @param datacontext,         jdbcDatacontext object
	 * @param table1,              first-table table object
	 * @param table2,              second-table table object
	 * @param existsRightOuterJoin
	 * @return rowsetInString
	 * @throws JdbcIntActivityExecutionException
	 */
	private Row prepareForLeftOuterJoin(DataContext datacontext, boolean existsLeftOuterJoin)
			throws JdbcIntActivityExecutionException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		Table tableOne;
		Table tableTwo;
		ArrayList<String> setOfColumns = getColumnSetFromSelectJoinQuery(queryString);
		Map<String, String> mapOfTables = getTableNamesFromSelectJoinQuery(queryString, LEFT_OUTER_JOIN.toUpperCase());
		Map<String, Map<String, Object>> mapOfConstraints = getPostJoinConstraintsFromSelectQuery(queryString);
		try {
			tableOne = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_ONE));
			tableTwo = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_TWO));
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException(
					"Unable to retreive the Table object, as it has returned with an empty tableName..", npe);
		}
		try {
			return activityService.selectActivityConfigParams(datacontext, tableOne, tableTwo, setOfColumns,
					mapOfConstraints, existsLeftOuterJoin, LEFT_OUTER_JOIN.toUpperCase());
		} catch (JdbcIntActivityPersistenceException e) {
			throw new JdbcIntActivityExecutionException("Unable to perform SELECT operation ..", e);
		}
	}// ..end of the method

	/**
	 * prepares and calls the DAO to do the right outerJoin
	 * 
	 * @param datacontext
	 * @param table1
	 * @param table2
	 * @param existsRightOuterJoin
	 * @return rowsetInString
	 * @throws JdbcIntActivityExecutionException
	 */
	private Row prepareForRightOuterJoin(DataContext datacontext, boolean existsRightOuterJoin)
			throws JdbcIntActivityExecutionException {
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		Table tableOne;
		Table tableTwo;
		ArrayList<String> setOfColumns = getColumnSetFromSelectJoinQuery(queryString);
		Map<String, String> mapOfTables = getTableNamesFromSelectJoinQuery(queryString, RIGHT_OUTER_JOIN.toUpperCase());
		Map<String, Map<String, Object>> mapOfConstraints = getPostJoinConstraintsFromSelectQuery(queryString);
		try {
			tableOne = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_ONE));
			tableTwo = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_TWO));
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException(
					"Unable to retreive the Table object, as it has returned with an empty table..", npe);
		}
		try {
			return activityService.selectActivityConfigParams(datacontext, tableOne, tableTwo, setOfColumns,
					mapOfConstraints, existsRightOuterJoin, RIGHT_OUTER_JOIN.toUpperCase());
		} catch (JdbcIntActivityPersistenceException e) {
			throw new JdbcIntActivityExecutionException(
					"Unable to perform SELECT operation for the JdbcIntActivityConfig..", e);
		}
	}// ..end of the method

	/**
	 * prepares and perform the DAO to do the InnerJoin
	 * 
	 * @param datacontext,    is the jdbcDatacontext
	 * @param table1,         first-table, table object
	 * @param table2,         second-table, table object
	 * @param existsInnerJoin
	 * @return rowsetINString
	 * @throws JdbcIntActivityExecutionException
	 */
	private Row prepareForInnerJoin(DataContext datacontext, boolean existsInnerJoin)
			throws JdbcIntActivityExecutionException {
		Table tableOne;
		Table tableTwo;
		IJdbcIntActivityService activityService = new JdbcIntActivityConfigDAO();
		ArrayList<String> setOfColumns = getColumnSetFromSelectJoinQuery(queryString);
		Map<String, String> mapOfTables = getTableNamesFromSelectJoinQuery(queryString, INNER_JOIN.toUpperCase());
		Map<String, Map<String, Object>> mapOfConstraints = getPostJoinConstraintsFromSelectQuery(queryString);
		try {
			tableOne = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_ONE));
			tableTwo = datacontext.getDefaultSchema().getTableByName(mapOfTables.get(TABLE_TWO));
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException(
					"Unable to retreive the Table object, as it has returned with an empty table..", npe);
		}
		try {
			return activityService.selectActivityConfigParams(datacontext, tableOne, tableTwo, setOfColumns,
					mapOfConstraints, existsInnerJoin, INNER_JOIN.toUpperCase());
		} catch (JdbcIntActivityPersistenceException e) {
			throw new JdbcIntActivityExecutionException(
					"Unable to perform SELECT operation for the JdbcIntActivityConfig..", e);
		}
	}// ..end of the method

	/**
	 * to get the column names from the select query example.. eg: SELECT gid,
	 * first_name, last_name, pid, gardener_id, plant_name FROM Gardners INNER JOIN
	 * Plantings ON gid = gardener_id
	 * 
	 * @param queryIn
	 * @return list of column names
	 * @return boolean value
	 * @throws JdbcIntActivityConfigurationException
	 */
	private ArrayList<String> getColumnSetFromSelectJoinQuery(String queryIn) throws JdbcIntActivityExecutionException {
		String upperCasePattern = "SELECT(.*?)FROM";
		Pattern p = Pattern.compile(upperCasePattern);
		Matcher m = p.matcher(queryIn);

		String[] colArr;
		ArrayList<String> colKeySet = new ArrayList<>();
		try {
			String out = null;
			while (m.find()) {
				out = m.group(1).trim();
			}
			colArr = out.split(",");
		} catch (NullPointerException npe) {
			throw new JdbcIntActivityExecutionException("", npe);
		}
		for (String colKey : colArr) {
			colKeySet.add(colKey.trim());
		}
		return colKeySet;
	}// ..end of the method

	/**
	 * 
	 * @param queryIn
	 * @param joinType
	 * @return map of table One and two
	 */
	private Map<String, String> getTableNamesFromSelectJoinQuery(String queryIn, String joinType) {
		String upperCasePattern = "FROM(.*?)" + joinType;
		String upperTable2 = "JOIN(.*?)ON";
		Map<String, String> map = new LinkedHashMap<>();
		Pattern p = Pattern.compile(upperCasePattern);
		Matcher m = p.matcher(queryIn);
		String tableOne = null;
		while (m.find()) {
			tableOne = m.group(1).trim();
		}
		Pattern p1 = Pattern.compile(upperTable2);
		Matcher m1 = p1.matcher(queryIn);
		String tableTwo = null;
		while (m1.find()) {
			tableTwo = m1.group(1).trim();
		}
		map.put(TABLE_ONE, tableOne);
		map.put(TABLE_TWO, tableTwo);
		return map;
	}// ..end of the method

	/**
	 * gets the constraints from the query
	 * 
	 * @param queryIn
	 * @return map of column key pair values
	 */
	private Map<String, Map<String, Object>> getPostJoinConstraintsFromSelectQuery(String queryIn) {
		String[] arrOfQuerySplit = queryIn.split("ON");
		Map<String, Object> map = new LinkedHashMap<>();
		String[] arrOfCons = arrOfQuerySplit[1].trim().split("=");
		map.put(arrOfCons[0], arrOfCons[1]);
		Map<String, Map<String, Object>> mapParent = new HashMap<>();
		mapParent.put(CONSTRAINTS, map);
		return mapParent;
	}// ..end of the method

	/**
	 * 
	 * @param queryString
	 * @return
	 */
	private Map<String, Map<String, Object>> getConstraintsFromUpdateQuery(String queryString) {
		String re1 = ".*?";
		String re2 = WHERE_UPPER;
		String re3 = WHERE_LOWER;
		String[] arrOfConstrnts;
		Map<String, Map<String, Object>> parentMap = new HashMap<>();
		Map<String, Object> frstMap = new HashMap<>();
		try {
			arrOfConstrnts = queryString.split(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String dummy = arrOfConstrnts[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("{} Didn't get the string parsed with the index in ..getConstraintsFromUpdateQuery",
					LEAP_LOG_KEY, e);
			arrOfConstrnts = queryString.split(re1 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
		String arrConstWhere[] = arrOfConstrnts[1].trim().split("=");
		frstMap.put(arrConstWhere[0], arrConstWhere[1]);
		parentMap.put(CONSTRAINTS, frstMap);
		return parentMap;
	}// ..end of the method

	/**
	 * 
	 * @param queryString
	 * @param xmlDocument
	 * @param setOfValuesProcessed
	 * @return
	 */
	private String getConstraintsFromUpdateQueryForCassandra(String queryString) {
		String re1 = ".*?";
		String re2 = WHERE_UPPER;
		String re3 = WHERE_LOWER;
		String[] arrOfConstrnts;
		try {
			arrOfConstrnts = queryString.split(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			// logger.debug("array of Constraints :: "+arrOfConstrnts);
			// String dummy = arrOfConstrnts[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("{} Didn't get the string parsed with the index in ..getConstraintsFromUpdateQuery",
					LEAP_LOG_KEY, e);
			arrOfConstrnts = queryString.split(re1 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
		// String sqlSqueryWhereConstraninst =
		// arrOfConstrnts[1].trim().replaceAll(SPACEREPLACER, ",
		// ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, " ");
		String sqlSqueryWhereConstraninst = arrOfConstrnts[1].trim();
		logger.trace("{} array of Constraints :: {}", LEAP_LOG_KEY, sqlSqueryWhereConstraninst);
		/*
		 * logger.debug("sqlqueryWhereConstraninst : " +sqlSqueryWhereConstraninst);
		 * String[] arrOfwordsafterwhere = sqlSqueryWhereConstraninst.split("=");
		 * logger.debug(
		 * "arrOfwordsafterwhere value column : "+arrOfwordsafterwhere[1]); String
		 * value=checkConditionColumnDataType(arrOfwordsafterwhere[1]);
		 */
		return sqlSqueryWhereConstraninst;
	}// ..end of the method getConstraintsFromUpdateQueryForCassandra

	private Object checkConditionColumnDataType(String value) {
		logger.debug(".checkConditionColumnDataType method of JDBCIntActivityConfigHelper");

		return value;

	}

	/**
	 * 
	 * @param deleteQuery
	 * @return
	 */// #TODO duplicate logic have to remove
	private Map<String, Map<String, Object>> getConstraintsFromDeleteQuery(String deleteQuery) {

		String re1 = ".*?";
		String re2 = WHERE_UPPER;
		String re3 = WHERE_LOWER;
		String[] arrOfConstrnts;
		Map<String, Map<String, Object>> parentMap = new HashMap<>();
		Map<String, Object> frstMap = new HashMap<>();
		try {
			arrOfConstrnts = deleteQuery.split(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String dummy = arrOfConstrnts[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			arrOfConstrnts = deleteQuery.split(re1 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
		String[] arrOfwordsafterwhere = null;
		try {
			arrOfwordsafterwhere = arrOfConstrnts[1].trim().split(" ");
			String[] arrOfwrdsSplitbysymbol = arrOfwordsafterwhere[0].split("=");
			frstMap.put(arrOfwrdsSplitbysymbol[0], arrOfwrdsSplitbysymbol[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			arrOfwordsafterwhere = arrOfConstrnts[0].trim().split(" ");
		}
		parentMap.put(CONSTRAINTS, frstMap);
		return parentMap;
	}// ..end of the method

	/**
	 * to parse the SELECT string to get the values
	 * 
	 * @param selectQuery
	 * @return column names in array
	 * @throws JdbcIntActivityExecutionException
	 */
	public ArrayList<String> getColumnNamesFromSelectQuery(String selectQuery)
			throws JdbcIntActivityExecutionException {
		Matcher m = Pattern.compile(PARANTHESIS_PARSE_PATTERN).matcher(selectQuery);
		String matched = "";
		while (m.find()) {
			matched = m.group(1);
		}
		String[] arr = matched.split(",");
		ArrayList<String> objarr = new ArrayList<>();
		for (int i = 0; i < arr.length; i++) {
			objarr.add(arr[i]);
		}
		if (!(objarr.get(0) == null || objarr.get(0) == "" || objarr.get(0).isEmpty())) {
			return objarr;
		} else {
			logger.trace("{} when condition..column names doesn't exists..", LEAP_LOG_KEY);
			objarr.add("*");
			return objarr;
		}
	}// ..end of the method

	/**
	 * parses the string to get the table name from SelectQuery
	 * 
	 * @param queryIn
	 * @return tableName at the index 3 - "SELECT (a,b) from testtable where a =
	 *         '01';"
	 * @throws JdbcIntActivityExecutionException
	 */
	public static String getTableNameFromSelectQuery(String queryIn) throws JdbcIntActivityExecutionException {
		String methodName = "getTableNameFromSelectQuery";
		logger.debug("{} entered into the method {}, QueryIn={} ", LEAP_LOG_KEY, methodName, queryIn);
		String[] arraySqldialects = queryIn.split(" ");
		if (!arraySqldialects[3].trim().isEmpty()) {
			logger.trace("{} TableName is : {} ", LEAP_LOG_KEY, arraySqldialects[3].trim());
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return arraySqldialects[3].trim();
		} else {
			throw new JdbcIntActivityExecutionException(
					"JdbcIntActivityExecutor received empty table name to process from SELECT query: " + queryIn);
		}
	}// ..end of the method

	/**
	 * logic to get tableName in string format from deleteQuery passedIn, decided to
	 * use when the operation is 'DELETE'
	 * 
	 * @param queryIn
	 * @return table name in string
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getTableNameFromDeleteQuery(String queryIn) throws JdbcIntActivityExecutionException {

		String[] arraySqldialects = queryIn.trim().split(" ");
		if (!arraySqldialects[2].trim().isEmpty()) {
			return arraySqldialects[2].trim();
		} else {
			throw new JdbcIntActivityExecutionException(
					"JdbcIntActivityExecutor received empty table name to process from DELETE query: " + queryIn);
		}
	}// ..end of the method

	/**
	 * logic to get tableName in string format from insertQuery passedIn, decided to
	 * use when the operation is 'INSERT'
	 * 
	 * @param queryIn
	 * @return table name in string
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getTableNameFromInsertQuery(String queryIn) throws JdbcIntActivityExecutionException {
		String methodName = "getTableNameFromInsertQuery";
		logger.debug("{} entered into the method {}, QueryIn={} ", LEAP_LOG_KEY, methodName, queryIn);

		String[] arraySqldialects = queryIn.split(" ");
		if (!arraySqldialects[2].trim().isEmpty()) {
			logger.trace("{} Table Name from InsertQuery: {}", LEAP_LOG_KEY, arraySqldialects[2].trim());
			return arraySqldialects[2].trim();
		} else {
			throw new JdbcIntActivityExecutionException(
					"JdbcIntActivityExecutor received empty table name to process from query: " + queryIn);
		}

	}// ..end of the method

	/**
	 * logic to get tableName in string format from updateQuery passedIn, decided to
	 * use when the operation is 'UPDATE'
	 * 
	 * @param queryIn
	 * @return table name in string
	 * @throws JdbcIntActivityExecutionException
	 */
	private String getTableNameFromUpdateQuery(String queryIn) throws JdbcIntActivityExecutionException {

		String[] arraySqldialects = queryIn.split(" ");
		if (!arraySqldialects[1].trim().isEmpty()) {
			return arraySqldialects[1].trim();
		} else {
			throw new JdbcIntActivityExecutionException(
					"JdbcIntActivityExecutor received empty table name to process from query: " + queryIn);
		}

	}// ..end of the method

	/**
	 * returns column name set where values to be inserted, parsing the sql string
	 * eg: insert into table (col1,col2) values (one,two); ~ [col1,col2]
	 * 
	 * @param inPutSqlString
	 * @return ordered set of column names parsed from the query string
	 */
	private Set<String> getColumnKeySetFromInsertQuery(String dbType, Exchange exchange, String inPutSqlString) {
		Set<String> columnKeySet;
		String[] arraySqldialects = inPutSqlString.split(" ");
		ArrayList<Object> arrColumnKey = getValuesinParanthesis(dbType, exchange, arraySqldialects[3]);
		if (arrColumnKey.get(0) == "") {
			columnKeySet = Collections.emptySet();
			logger.trace("{} the columns are not mentioned..", LEAP_LOG_KEY);
		} else {
			columnKeySet = new LinkedHashSet(arrColumnKey);
			logger.trace("{} NonEmpty ColumnKeys: {} ", LEAP_LOG_KEY, columnKeySet);
		}
		return columnKeySet;
	}// .. end of the method

	/**
	 * returns list of values to be inserted, parsing the sql string eg: insert into
	 * table (col1,col2) values (one,two); ~ [one,two]
	 * 
	 * @param inputSqlString
	 * @return listOfValues corresponding to the columnKeySet
	 * @throws JdbcIntActivityStringParserException
	 */
	private ArrayList<Object> getRespectiveColumnValuestoInsert(String dbType, Exchange exchange, String inputSqlString)
			throws JdbcIntActivityStringParserException {
		String methodName = "getRespectiveColumnValuestoInsert";
		logger.debug("{} entered into the method {}, imputSQLString : {}", LEAP_LOG_KEY, methodName, inputSqlString);
		ArrayList<Object> list = null;
		String[] arraySqldialects = inputSqlString.split("VALUES");
		try {
			ArrayList<Object> arrColumnValue = getValueswithinOuterParanthesis(dbType, exchange, arraySqldialects[1]);
			if (arrColumnValue.get(0) == "") {
				logger.trace("{} The values to be inserted id empty..", LEAP_LOG_KEY);
				throw new JdbcIntActivityStringParserException(
						"Unable to get the Columns to be inserter from the query non-empty " + inputSqlString);
			} else {
				list = arrColumnValue;
			}
		} catch (Exception e) {
			ArrayList<Object> arrColumnValue = getValueswithinOuterParanthesis(dbType, exchange, arraySqldialects[4]);
			if (arrColumnValue.get(0) == "") {
				logger.trace("{} The values to be inserted id empty..", LEAP_LOG_KEY);
				throw new JdbcIntActivityStringParserException(
						"Unable to get the Columns to be inserter from the query non-empty " + inputSqlString, e);
			} else {
				list = arrColumnValue;
				logger.trace("{} The non empty list of values: {}", LEAP_LOG_KEY, list);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}// ..end of the method

	/**
	 * to operate regex for getting values with in the parenthesis of the query
	 * string
	 * 
	 * @param inString
	 * @return string array available in query dialect with in parenthesis
	 */
	public ArrayList<Object> getValuesinParanthesis(String dbType, Exchange exchange, String inString) {
		String methodName = "getValuesinParanthesis";
		logger.debug("{} entered into the method {}, inString : {}", LEAP_LOG_KEY, methodName, inString);
		Matcher m = Pattern.compile(PARANTHESIS_PARSE_PATTERN).matcher(inString);
		String matched = "";
		while (m.find()) {
			matched = m.group(1);
			logger.trace("{} executing", LEAP_LOG_KEY);
		}
		String[] arr = matched.split(",");
		ArrayList<Object> objarr = new ArrayList<>();
		for (int i = 0; i < arr.length; i++) {
			objarr.add(arr[i].trim());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return objarr;
	}// .. end of the method

	/**
	 * optional method if needed use it to get the values which has paranthesis
	 * 
	 * @param inString
	 * @return list of Values with paranthesis also
	 */
	private ArrayList<Object> getValueswithinOuterParanthesis(String dbType, Exchange exchange, String inString) {
		String methodName = "getValueswithinOuterParanthesis";
		logger.debug("{} entered into the method {}, inString :{}", LEAP_LOG_KEY, methodName, inString);
		char[] chArr = inString.toCharArray();
		int indexOfLParanthasis = inString.indexOf('(') + 1;
		int counterR = 0;
		int lengthOfChar = chArr.length;
		for (int i = lengthOfChar - 1; i >= 1; --i) {
			if (chArr[i] == ')') {
				counterR = i;
				break;
			}
		}
		// String newStr = null;
		String stringTosplit = inString.substring(indexOfLParanthasis, counterR);
		/*
		 * if (stringTosplit.contains(", ")) { newStr = stringTosplit.replaceAll(", ",
		 * SPACEREPLACER); } else
		 */

		/*
		 * if (stringTosplit.contains(",")) { newStr = stringTosplit.replaceAll(",",
		 * REPLACER); } else { newStr = stringTosplit; }
		 */
		// logger.debug("newStr : "+newStr);
		String[] arr = stringTosplit.split(",");
		logger.trace("{} newStr after split {}", LEAP_LOG_KEY, arr.toString());
		ArrayList<Object> objarr = new ArrayList<>();
		/*
		 * for (int i = 0; i < arr.length; i++) { objarr.add(parseIfDateexists(arr[i]));
		 * }
		 */
		for (int i = 0; i < arr.length; i++) {
			String eachString = arr[i];
			logger.trace("{} eachString : {}", LEAP_LOG_KEY, eachString);
			// eachString = eachString.replaceAll(SPACEREPLACER, ",
			// ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, "
			// ");
			eachString = eachString.replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER, ",");
			logger.debug("{} eachString value after replacing {}", LEAP_LOG_KEY, eachString);
			objarr.add(eachString.trim());
			// objarr.add(parseIfDateexists(dbType, exchange, eachString));
		}
		logger.info("Array which is withinOuter Paranthesis returns..." + objarr);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return objarr;
	}// ..end of the method

	/**
	 * method to used along with the xpath parsing, cz to check any dae format
	 * exists or not inorder to make sure smooth flow of dml queries concerts to utc
	 * format
	 * 
	 * @param valueTobeCasted
	 * @param dateFormat
	 * @return objects with corresponding DataTypes
	 */
	public static Object parseIfDateexists(String dbType, Exchange exchange, String valueTobeCasted,
			String dateFormat) {
		String methodName = "parseIfDateexists";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String trimmedStrings = valueTobeCasted.trim();
		Object isDate = null;
		try {
			if (dbType.equals("CASSANDRA")) {
				String timeZoneId = exchange.getIn().getHeader(LeapHeaderConstant.TIMEZONE, String.class);

				TimeZone tenantSiteTimeZone;
				if (timeZoneId != null)
					tenantSiteTimeZone = TimeZone.getTimeZone(timeZoneId);
				else {
					tenantSiteTimeZone = TimeZone.getDefault();
					logger.trace("{} Time zone not specified for tenant will be using system local timezone {}",
							LEAP_LOG_KEY, tenantSiteTimeZone.getDisplayName());
				}

				if (dateFormat == null || dateFormat.trim().isEmpty()) {
					logger.trace(
							"{} Timestamp column specified without dateFormat using default date format pattern {}",
							LEAP_LOG_KEY, DATE_FORMAT);
					dateFormat = DATE_FORMAT;
				}
				TimeZone orignalLocalTimeZone = TimeZone.getDefault();
				TimeZone.setDefault(tenantSiteTimeZone);

				DateFormat tenantDf = new SimpleDateFormat(dateFormat);
				DateFormat utcTenantDF = new SimpleDateFormat(dateFormat);

				Date receivedDTM = tenantDf.parse(trimmedStrings);
				Calendar tenantCalendar = new GregorianCalendar();
				tenantCalendar.setTime(receivedDTM);
				logger.trace("{} initial time  :: {}", LEAP_LOG_KEY, receivedDTM);
				tenantDf.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
				String formattedUTCTime = tenantDf.format(tenantCalendar.getTime());
				logger.trace("{} before foramtting utc time :: {}", LEAP_LOG_KEY, formattedUTCTime);
				Date utcDATE = utcTenantDF.parse(formattedUTCTime);
				logger.trace("{} actual utc time to be inserted  :: {}", LEAP_LOG_KEY, utcDATE);

				// formatting must be done before based on the time zone
				// added
				DateFormat cassandraLocalDF = new SimpleDateFormat(dateFormat);
				DateFormat cassandraUTCDF = new SimpleDateFormat(dateFormat);

				// once all calculation's are done setting default value
				// of timezone after formatting so we will add the
				TimeZone.setDefault(orignalLocalTimeZone);

				long receivedDTMms = receivedDTM.getTime();
				long actualUTCms = utcDATE.getTime();
				logger.trace("{} boolean  (convertedutc < receiveddtm) :{} ", LEAP_LOG_KEY,
						(actualUTCms < receivedDTMms));
				if (actualUTCms <= receivedDTMms) {
					isDate = addExtraTimeForCassandratoRemove(utcDATE, cassandraLocalDF, cassandraUTCDF, actualUTCms);
				} else {
					isDate = removeExtraTimeForCassandratoAdd(utcDATE, cassandraLocalDF, cassandraUTCDF, actualUTCms);
				}

			} else {
				DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
				isDate = sdf.parse((String) trimmedStrings);
			}
		} catch (ParseException | ClassCastException e) {
			// if any the ParseException, ClassCastException occurs, then the
			// object passed will be propagated
			logger.error("{} Didn't get the date object..{}", LEAP_LOG_KEY, e);
			isDate = trimmedStrings.toString();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isDate;
	}

	/**
	 * adding the time in existing date because cassandra driver will remove that
	 * much time based on local utc.In order to get actual timestamp.
	 * 
	 */
	private static Long addExtraTimeForCassandratoRemove(Date utcDATE, DateFormat cassandraLocalDF,
			DateFormat cassandraUTCDF, long actualUTCms) throws ParseException {
		Long isDate;
		Calendar cassandraCalendar = new GregorianCalendar();
		cassandraCalendar.setTime(utcDATE);
		logger.debug("{} utc to be converted based on cassandra : {}", LEAP_LOG_KEY, utcDATE);
		cassandraLocalDF.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
		String cassandraExtraAddedTime = cassandraLocalDF.format(cassandraCalendar.getTime());
		logger.trace("{} before foramtting utc time :: {} ", LEAP_LOG_KEY, cassandraExtraAddedTime);
		Date extraOffsetAddedDate = cassandraUTCDF.parse(cassandraExtraAddedTime);

		long addedTimeToActualDTM = extraOffsetAddedDate.getTime();
		long diff = actualUTCms - addedTimeToActualDTM;
		isDate = new Long(new Date(actualUTCms + diff).getTime());
		logger.debug("{} cassandra utc extra added time based on local:: {}", LEAP_LOG_KEY, new Date(isDate));
		return isDate;
	}

	/**
	 * removing extra time from existing utc date because cassandra driver will add
	 * that much time based on local utc..In order to get actual timestamp
	 */
	private static Long removeExtraTimeForCassandratoAdd(Date utcDATE, DateFormat cassandraLocalDF,
			DateFormat cassandraUTCDF, long actualUTCms) throws ParseException {
		Long isDate;
		Calendar cassandraCalendar = new GregorianCalendar();
		cassandraCalendar.setTime(utcDATE);
		logger.debug("{} utc to be converted based on cassandra : {}", LEAP_LOG_KEY, utcDATE);
		cassandraLocalDF.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
		String cassandraExtraAddedTime = cassandraLocalDF.format(cassandraCalendar.getTime());
		logger.trace("{} before foramtting utc time :: {}", LEAP_LOG_KEY, cassandraExtraAddedTime);
		Date extraOffsetAddedDate = cassandraUTCDF.parse(cassandraExtraAddedTime);

		long removingExtraTime = extraOffsetAddedDate.getTime();
		long diff = removingExtraTime - actualUTCms;
		isDate = new Long(new Date(actualUTCms - diff).getTime());
		logger.debug("{}cassandra utc extra added time based on local:: {}", LEAP_LOG_KEY, new Date(isDate));
		return isDate;
	}

	/**
	 * Accepts the sqlUpdate String, parse and get the column-value pair eg: UPDATE
	 * testtable SET firstName = Alfred, age = 25; ~= {firstName=Alfred, age=25}
	 * 
	 * @param sqlUpdate
	 * @return map of column and corresponding values
	 */
	private Map<String, Object> parseToGetColumnKeyValueMap(String sqlUpdate) {

		String methodName = "parseToGetColumnKeyValueMap";
		logger.debug("{} entered into the method {}, sqlUpdate : {}", LEAP_LOG_KEY, methodName, sqlUpdate);
		String matchedString = null;
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(UPDT_QUERY_UPPER_PATTERN);
		matcher = pattern.matcher(sqlUpdate);
		while (matcher.find()) {
			matchedString = matcher.group(1).trim();
		}
		if (matchedString == null) {
			pattern = Pattern.compile(UPDT_QUERY_LOWER_PATTERN);
			matcher = pattern.matcher(sqlUpdate);
			while (matcher.find()) {
				matchedString = matcher.group(1).trim();
			}
		} // #TODO removing the whitespace can hamper for instances such as ,
			// FullName = 'Sir Alex Ferguson'
			// String wSpace = matchedString.replaceAll("\\s+", "");
		Map<String, Object> myMap = new HashMap<>();
		logger.debug("{} matchedString  : {}", LEAP_LOG_KEY, matchedString);
		String[] pairs = matchedString.split(",");
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			logger.trace("{} pair : {}", LEAP_LOG_KEY, pair);
			String[] keyValue = pair.trim().split("=");
			String value = "";
			for (int j = 1; j < keyValue.length; j++) {
				// logger.debug("keyValue : : : "+keyValue[j].toString());
				value = value.concat(keyValue[j].trim());
				if (j < keyValue.length - 1) {
					value = value.concat("=");
				}
				// value = value.replaceAll(SPACEREPLACER, ",
				// ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER,"
				// ");
				value = value.replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER, ",");
				logger.debug("{} value : {}", LEAP_LOG_KEY, value);
			}
			myMap.put(keyValue[0].trim(), value);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return myMap;
	}// ..end
		// of
		// the
		// method

	/**
	 * to parse the sql string to get the constraints to be executed
	 * 
	 * @param selectQueryString
	 * @return mapObject containing all the primary three constraints, where, and ,
	 *         or.
	 */
	private Map<String, Map<String, Object>> getConstraintsFromSelectQuery(String selectQueryString) {
		String methodName = "getConstraintsFromSelectQuery";
		logger.debug("{} entered into the method {} ,selectQueryString : {}", LEAP_LOG_KEY, methodName,
				selectQueryString);
		if (!selectQueryString.toLowerCase().contains("where"))
			return null;
		String re1 = ".*?";
		String re2 = WHERE_UPPER;
		String re3 = WHERE_LOWER;
		String[] arrOfConstrnts;
		try {
			arrOfConstrnts = selectQueryString.split(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String dummy = arrOfConstrnts[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("{} Unable to get the desired value at the specified index hence process continues..",
					LEAP_LOG_KEY, e);
			arrOfConstrnts = selectQueryString.split(re1 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		}
		Map<String, Map<String, Object>> parentMap = new HashMap<>();
		String[] arrOfwordsafterwhere = null;
		try {

			arrOfwordsafterwhere = arrOfConstrnts[1].trim().split(" ");
			int arrLen = arrOfwordsafterwhere.length;
			Map<String, Object> andMap = new HashMap<>();
			Map<String, Object> orMap = new HashMap<>();
			Map<String, Object> firstMap = new HashMap<>();
			logger.debug("{} Array of WhereConstraints : {}, arrLen : {}", LEAP_LOG_KEY, arrOfwordsafterwhere, arrLen);
			firstMap.put(arrOfwordsafterwhere[0], arrOfwordsafterwhere[2].replace("'", "").replace("\"", ""));
			for (int i = 0; i < arrLen; i++) {
				logger.trace("{} Constraintents : {}", LEAP_LOG_KEY, arrOfwordsafterwhere[i]);
				if ("and".equals(arrOfwordsafterwhere[i]) || "AND".equals(arrOfwordsafterwhere[i])) {
					/*
					 * andMap.put(arrOfwordsafterwhere[i + 1].replaceAll(SPACEREPLACER,
					 * ", ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, " "),
					 * arrOfwordsafterwhere[i + 3].replace("'", "").replace("\"", "")
					 * .replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER,
					 * ",").replaceAll(SPACE_ONLY_REPLACER, " "));
					 */
					andMap.put(arrOfwordsafterwhere[i + 1].replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER, ","),
							arrOfwordsafterwhere[i + 3].replace("'", "").replace("\"", "")
									.replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER, ","));
				} else if ("or".equals(arrOfwordsafterwhere[i]) || "OR".equals(arrOfwordsafterwhere[i])) {
					/*
					 * orMap.put(arrOfwordsafterwhere[i + 1], arrOfwordsafterwhere[i +
					 * 3].replace("'", "") .replace("\"", "").replaceAll(SPACEREPLACER,
					 * ", ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, " "));
					 */
					orMap.put(arrOfwordsafterwhere[i + 1], arrOfwordsafterwhere[i + 3].replace("'", "")
							.replace("\"", "").replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER, ","));
				}
				parentMap.put(CONSTRAINTS, firstMap);
				parentMap.put("and", andMap);
				parentMap.put("or", orMap);
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			// This is when the query constraints were not been parsed...that is
			// if there is no constraint
			logger.error("{} Unable to parse any of the constraints from the sql string..", LEAP_LOG_KEY, ae);
			parentMap.put(CONSTRAINTS, null);
		}
		logger.debug("{} the mapObject is : {}", LEAP_LOG_KEY, parentMap);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return parentMap;
	}

	/**
	 * to parse the sql string to get the constraints to be executed
	 * 
	 * @param selectQueryString
	 * @return mapObject containing all the primary three constraints, where, and ,
	 *         or.
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Map<String, Map<String, Object>> getConstraintsFromSelectQuery(String selectQueryString,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getConstraintsFromSelectQuery";
		logger.debug("{} entered into the method {}, QueryString :{}", LEAP_LOG_KEY, methodName, selectQueryString);
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		String re1 = ".*?";
		String re2 = WHERE_UPPER;
		String re3 = WHERE_LOWER;
		String[] arrOfConstrnts;
		try {
			arrOfConstrnts = selectQueryString.split(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			String dummy = arrOfConstrnts[1];
			logger.debug("{} setOfValuesProcessed : {}", LEAP_LOG_KEY, setOfValuesProcessed.toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("{} Unable to get the desired value at the specified index hence process continues..",
					LEAP_LOG_KEY, e);
			arrOfConstrnts = selectQueryString.split(re1 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			logger.debug("{} setOfValuesProcessed : {}", LEAP_LOG_KEY, setOfValuesProcessed.toString());
		}
		Map<String, Map<String, Object>> parentMap = new HashMap<>();
		String[] arrOfwordsafterwhere = null;
		try {

			arrOfwordsafterwhere = arrOfConstrnts[1].trim().split(" ");
			int arrLen = arrOfwordsafterwhere.length;
			Map<String, Object> andMap = new HashMap<>();
			Map<String, Object> orMap = new HashMap<>();
			Map<String, Object> firstMap = new HashMap<>();
			logger.debug("{} Array of WhereConstraints : {}, arrLen : {}", LEAP_LOG_KEY, arrOfwordsafterwhere, arrLen);

			firstMap.put(arrOfwordsafterwhere[0], configHelper.xpathProcessingOnInputXml(
					setOfValuesProcessed.get(arrOfwordsafterwhere[2].replace("'", "").replace("\"", "")), xmlDocument));
			for (int i = 0; i < arrLen; i++) {
				logger.trace("{} Constraintents :  {}" ,LEAP_LOG_KEY, arrOfwordsafterwhere[i]);
				if ("and".equals(arrOfwordsafterwhere[i]) || "AND".equals(arrOfwordsafterwhere[i])) {
					/*
					 * andMap.put(arrOfwordsafterwhere[i + 1].replaceAll(SPACEREPLACER,
					 * ", ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, " "),
					 * arrOfwordsafterwhere[i + 3].replace("'", "").replace("\"", "")
					 * .replaceAll(SPACEREPLACER, ", ").replaceAll(REPLACER,
					 * ",").replaceAll(SPACE_ONLY_REPLACER, " "));
					 */
					andMap.put(arrOfwordsafterwhere[i + 1],
							configHelper.xpathProcessingOnInputXml(
									setOfValuesProcessed
											.get(arrOfwordsafterwhere[i + 3].replace("'", "").replace("\"", "")),
									xmlDocument));
				} else if ("or".equals(arrOfwordsafterwhere[i]) || "OR".equals(arrOfwordsafterwhere[i])) {
					/*
					 * orMap.put(arrOfwordsafterwhere[i + 1], arrOfwordsafterwhere[i +
					 * 3].replace("'", "") .replace("\"", "").replaceAll(SPACEREPLACER,
					 * ", ").replaceAll(REPLACER, ",").replaceAll(SPACE_ONLY_REPLACER, " "));
					 */
					orMap.put(arrOfwordsafterwhere[i + 1],
							configHelper.xpathProcessingOnInputXml(
									setOfValuesProcessed
											.get(arrOfwordsafterwhere[i + 3].replace("'", "").replace("\"", "")),
									xmlDocument));
				}
				parentMap.put(CONSTRAINTS, firstMap);
				parentMap.put("and", andMap);
				parentMap.put("or", orMap);
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			// This is when the query constraints were not been parsed...that is
			// if there is no constraint
			logger.error("{} Unable to parse any of the constraints from the sql string..", LEAP_LOG_KEY, ae);
			parentMap.put(CONSTRAINTS, null);
		}
		logger.debug("{} the mapObject is : {}" ,LEAP_LOG_KEY, parentMap);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return parentMap;
	}
}
