package com.attunedlabs.leapentity;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.LeapMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.base.ComputeTimeBean;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.google.common.reflect.TypeToken;

public class LeapEntityArchivalUtility {

	private final static Logger logger = LoggerFactory.getLogger(LeapEntityArchivalUtility.class);

	public static Document readEntityXmlFile(String fileName) throws LeapEntityException {
		DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
		Document entityDoc = null;
		try {
			DocumentBuilder dbuilder = dbc.newDocumentBuilder();
			entityDoc = dbuilder.parse(LeapEntityArchivalUtility.class.getClassLoader().getResourceAsStream(fileName));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new LeapEntityException("unable to read entity xml file :" + e.getMessage());
		}
		return entityDoc;

	}

	public List<String> parseEntityColumns() throws LeapEntityException {
		String methodName = "parseEntityColumns";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);

		DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder;
		String name = null;
		List<String> list = new ArrayList<>();
		try {
			dbuilder = dbc.newDocumentBuilder();
			Document doc = dbuilder.parse(new InputSource(new StringReader(getEntityConfiguration())));
			NodeList nl = doc.getElementsByTagName("EntityColumn");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				name = e.getAttribute("name");
				// if (name.equals(key)) {
				// logger.debug("name : " + name);
				list.add(name);
				// }
			}
		} catch (Exception e) {
			throw new LeapEntityException(e.getMessage(), e.getCause());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws LeapEntityException
	 */
	public List<String> getAttribute(String key) throws LeapEntityException {
		String methodName = "getAttribute";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder;
		String name = null, accessType = null;
		List<String> list = new ArrayList<>();
		try {
			dbuilder = dbc.newDocumentBuilder();
			Document doc = dbuilder.parse(new InputSource(new StringReader(getEntityConfiguration())));
			NodeList n2 = doc.getElementsByTagName("EntityAccess");
			for (int i = 0; i < n2.getLength(); i++) {
				Element e = (Element) n2.item(i);
				accessType = e.getAttribute("accessType");
				list.add(accessType);
			}
			NodeList nl = doc.getElementsByTagName("EntityColumn");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				name = e.getAttribute("name");
				if (name.equals(key)) {
					logger.trace("{} name : {}", LEAP_LOG_KEY, name);
					list.add(name);
				}
			}
		} catch (Exception e) {
			throw new LeapEntityException(e.getMessage(), e.getCause());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * To get {@link DataContext} Object.
	 * 
	 * @param exchange
	 * @param columnName - Name of the column(only for Cassandra database).
	 * @param operation  - Name of the operation like SELECT, INSERT, INSERT,
	 *                   DELETE...(only for Cassandra database).
	 * @return {@link DataContext} Object.
	 * @throws PropertiesConfigException
	 */
	public static DataContext getDataContext(Exchange exchange, String operation, String columnName)
			throws PropertiesConfigException {
		if (isSQL(exchange))
			return getCassandraDataContext(columnName, operation);
		else {
			return getJDBCDataContext(exchange);
		}
	}// end of getDataContext() method.

	
	/**
	 * Check whether the given request is for SQL or NO-SQL Database
	 * 
	 * @param exchange
	 * @return true or false
	 */
	public static boolean isSQL(Exchange exchange) {
		Object noSqlObj = exchange.getIn().getHeader(LeapEntityArchivalConstant.NO_SQL_HEADER_KEY);
		boolean noSql = false;
		if (noSqlObj != null)
			noSql = Boolean.valueOf(noSqlObj.toString().trim());
		return noSql;
	}// end of isSQL() method.

	/**
	 * This method is used to get the intial request body from the LDC
	 * 
	 * @param exchange
	 * @return
	 */
	public static Object getIntialRequestBodyFromLDC(Exchange exchange) {

		String methodName = "getIntialRequestBodyFromLDC";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY,LeapEntityArchivalUtility.class.getName(), methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);

		Object requestObject = null;
		JSONObject requestJson = null;
		LeapDataElement requestContextElement = null;
//		requestContextElement = leapDataContext.getContextElement("#enriched_leap_initial");
		if (requestContextElement == null)
			requestContextElement = leapDataContext.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		logger.debug("{} requestContextElement value {} in {} method :: " ,LEAP_LOG_KEY, requestContextElement,methodName);

		LeapData leapData = requestContextElement.getData();

		if (leapData != null) {
			LeapResultSet items = leapData.getItems();
			Object data = items.getData();
			if (data != null) {
				requestObject = data;
			}
		} else {
			requestObject = new JSONObject();
		}
		if (requestObject instanceof JSONObject) {
			requestJson = (JSONObject) requestObject;
			if (requestJson.has(LeapDataContextConstant.API_VERSION) && requestJson.has(LeapDataContextConstant.CONTEXT)
					&& requestJson.has(LeapDataContextConstant.LANG)) {
				requestObject = requestJson.get(LeapDataContextConstant.DATA);
			} else {
				if (requestJson.has(LeapDataContextConstant.DATA)) {
					requestObject = requestJson.get(LeapDataContextConstant.DATA);
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return requestObject;

	}// ..end of the method getIntialRequestBodyFromLDC

	/**
	 * To get {@link DataContext} Object.
	 * 
	 * @param exchange
	 * @return {@link DataContext} Object.
	 */
	private static DataContext getJDBCDataContext(Exchange exchange) {
		Registry registry = exchange.getContext().getRegistry();
		DataSource dataSource = (DataSource) registry.lookupByName("dataSourceSQL");
		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(dataSource);
		} catch (CannotGetJdbcConnectionException e) {
			logger.warn("{} Error in getting the Connection from {}" ,LEAP_LOG_KEY, dataSource);
		}
		DataContext metamodelJdbcContext = new JdbcDataContext(con);
		return metamodelJdbcContext;
	}// end of getJDBCDataContext() method.

	/**
	 * To get Cassandra {@link DataContext} Object.
	 * 
	 * @param columnName - Name of the column(if column has to be added in
	 *                   {@link TypeToken}).
	 * @param operation  - Name of the operation like SELECT, INSERT, INSERT,
	 *                   DELETE....
	 * @return {@link DataContext} Object.
	 * @throws PropertiesConfigException
	 */
	private static DataContext getCassandraDataContext(String columnName, String operation)
			throws PropertiesConfigException {
		HashMap<String, TypeToken<?>> typeTokenMap = new HashMap<>();
		typeTokenMap.put(columnName, new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1L;
		});
		DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
		if (operation.toUpperCase().equals(LeapMetaModelBean.SELECT_OPERATION))
			properties.setDataContextType(LeapEntityArchivalConstant.CASSANDRA);
		else
			properties.setDataContextType(LeapEntityArchivalConstant.JDBC);

		properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME,
				LeapConfigUtil.getGlobalPropertyValue(LeapEntityArchivalConstant.HOST,LeapDefaultConstants.DEFAULT_HOST_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_PORT,
				LeapConfigUtil.getGlobalPropertyValue(LeapEntityArchivalConstant.PORT,LeapDefaultConstants.DEFAULT_PORT_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_URL,
				LeapConfigUtil.getGlobalPropertyValue(LeapEntityArchivalConstant.URL,LeapDefaultConstants.DEFAULT_URL_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS,
				LeapConfigUtil.getGlobalPropertyValue(LeapEntityArchivalConstant.DRIVER_CLASS,LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE,
				LeapConfigUtil.getGlobalPropertyValue(LeapEntityArchivalConstant.KEYSPACE,LeapDefaultConstants.DEFAULT_KEYSPACE_KEY));
		properties.put(LeapEntityArchivalConstant.TYPE_TOKEN, typeTokenMap);

		org.apache.metamodel.DataContext dataContext = DataContextFactoryRegistryImpl.getDefaultInstance()
				.createDataContext(properties);
		return dataContext;
	}// end of getCassandraDataContext() method.

	/**
	 * 
	 * @return
	 * @throws LeapEntityException
	 */
	private String getEntityConfiguration() throws LeapEntityException {
		InputStream leapDataServices = ComputeTimeBean.class.getClassLoader()
				.getResourceAsStream(LeapEntityArchivalConstant.LEAP_DATA_SERVICES);
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(leapDataServices));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			throw new LeapEntityException(e.getMessage(), e.getCause());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		logger.trace("{} sb.toString() : {}" ,LEAP_LOG_KEY, sb.toString());
		return sb.toString();
	}

}
