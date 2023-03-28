package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.abstractbean.AbstractCassandraBean;
import com.attunedlabs.eventframework.abstractbean.util.CassandraClusterException;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.abstractbean.util.ConnectionConfigurationException;
import com.attunedlabs.integrationfwk.config.jaxb.FieldMapper;
import com.attunedlabs.integrationfwk.config.jaxb.JDBCIntActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.JdbcIntActivityConfigurationQueryProcessor;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.JdbcIntActivityQueryProcessingException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityExecutionException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityStringParserException;
import com.datastax.driver.core.Cluster;

public class JDBCIntActivityCassandra extends AbstractCassandraBean {
	private final Logger log = LoggerFactory.getLogger(JdbcIntActivityConfigurationQueryProcessor.class.getName());

	/**
	 * processor of the Sql query from the Configuration JAXBObject
	 * 
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	@Override
	protected void processBean(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		String methodName = "processBean";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		log.trace("{} jdbcIntActivity..{}", LEAP_LOG_KEY, pipeactivity);
		JDBCIntActivity configObject = pipeactivity.getJDBCIntActivity();
		Set<String> xpathExpression;
		Map<String, String> xpathExpressionMapping;
		String newQuery;
		String operation;
		String dbType = configObject.getDBConfig().getDbType();
		try {
			/*
			 * @note: this snippet is before execution of Query, where substitution of field
			 * happens
			 */
			xpathExpression = getXpathExpressionFromJdbcActivityUnit(configObject);
			xpathExpressionMapping = getMapForExpressionFromJdbcActivityUnit(configObject);
			ArrayList<String> fieldKey = getListofFieldMapperKeys(configObject);
			String sqlQueryConfig = getSqlStringFromJdbcActivityConfigUnit(configObject);
			operation = getSqlOperationFromJdbcActivityConfigUnit(configObject);
			// String inputXml = XML_RAW_VALUE;
			String inputXml = retreiveXmlInputFromExchangeBody(exchange);
			log.trace("{} before un escaped Xml : {}", LEAP_LOG_KEY, inputXml);

			inputXml = StringEscapeUtils.unescapeXml(inputXml);
			log.trace("{} escaped Xml : {}", LEAP_LOG_KEY, inputXml);

			Document xmlDocument = configHelper.generateDocumentFromString(inputXml);

			try {
				if (operation.equalsIgnoreCase("SELECT")) {
					List<Object> setOfValuesProcessed = configHelper.xpathProcessingOnInputXml(xpathExpression,
							xmlDocument);
					newQuery = configHelper.processSqlFieldSubstitution(sqlQueryConfig, fieldKey, setOfValuesProcessed,
							operation);
					log.info("{} The new query: {}", LEAP_LOG_KEY, newQuery);
					// executeSQLQuery(operation, newQuery, xmlDocument,
					// configObject, exchange, setOfValuesProcessed);
				}
				executeSQLQuery(dbType, operation, sqlQueryConfig, xmlDocument, configObject, exchange,
						xpathExpressionMapping);
			} catch (JdbcIntActivityExecutionException | ActivityEnricherException e) {
				throw new JdbcIntActivityQueryProcessingException("unable to execute the sql query", e);
			}
		} catch (JdbcIntActivityConfigurationException e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Unable to process , as pre-processing failed! in preparation stage", e);
		}
		log.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);

	}// ..end of the method

	/*
	 * 
	 * 
	 * 
	 * 
	 */

	/**
	 * This method is used to decide if the method is of type select or
	 * other(insert|update|delete) and call method which suitable for operation
	 * 
	 * @param operation      : SQL operation like (select,update,delete,insert) in
	 *                       string format
	 * @param query          : SQL query in string format
	 * @param sqlQueryConfig
	 * @throws JdbcIntActivityQueryProcessingException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private void executeSQLQuery(String dbType, String operation, String query, Document xmlDocument,
			JDBCIntActivity configObject, Exchange exchange, Map<String, String> setOfValuesProcessed)
			throws JdbcIntActivityQueryProcessingException, JdbcIntActivityExecutionException,
			ActivityEnricherException, JdbcIntActivityConfigurationException {

		String methodName = "executeSQLQuery";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper(operation, query);
		if (operation.equalsIgnoreCase("SELECT")) {
			Object response = executeSQLQueryByDatContext(dbType, exchange, configObject, operation, query,
					configHelper, xmlDocument, setOfValuesProcessed);
			ArrayList<String> colums = configHelper.getColumnNamesFromSelectQuery(query);
			Row rowresp = (Row) response;
			List<Object> listOfValues = Arrays.asList(rowresp.getValues());
			if (listOfValues.size() == colums.size()) {
				String xmlEnriched = configHelper.processxmlEnrichment(xmlDocument, configObject, colums, listOfValues);
				exchange.getIn().setBody(xmlEnriched);
			}
		} else {
			executeSQLQueryByUpdateableDataContext(dbType, exchange, configObject, operation, query, configHelper,
					setOfValuesProcessed, xmlDocument);
		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used execute the select operation of sql using apache
	 * metamodel. To perform select, it require to get cluster object and pass it to
	 * metamodel api to get data context
	 * 
	 * @param configObject
	 * 
	 * @param operation
	 * @param query
	 * @param configHelper
	 * @throws JdbcIntActivityQueryProcessingException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Object executeSQLQueryByDatContext(String dbType, Exchange exchange, JDBCIntActivity configObject,
			String operation, String query, JdbcIntActivityConfigHelper configHelper, Document xmlDocument,
			Map<String, String> setOfValuesProcessed)
			throws JdbcIntActivityQueryProcessingException, JdbcIntActivityConfigurationException {
		String methodName = "executeSQLQueryByDatContext";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			try {
				Cluster cluster = getCassandraCluster();
				String keyspace = LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.KEYSPACE_KEY,LeapDefaultConstants.DEFAULT_KEYSPACE_KEY);
				log.trace("{} keyspace : {}", LEAP_LOG_KEY, keyspace);
				DataContext datacontext = getDataContextForCassandraByCluster(cluster, keyspace);
				configHelper.setDataContext(datacontext);
				try {
					// passing datacontext as select operation is done using
					// datacontext object,therefore passsing
					// updateableDatacontext as null
					Object response = configHelper.decideQueryToPerform(dbType, exchange, configObject, operation,
							query, null, datacontext, setOfValuesProcessed, xmlDocument);
					log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return response;
				} catch (JdbcIntActivityExecutionException | JdbcIntActivityStringParserException e) {
					throw new JdbcIntActivityQueryProcessingException("Unable to process the query string : " + query);
				}
			} catch ( PropertiesConfigException e1) {
				throw new JdbcIntActivityQueryProcessingException("");
			}
		} catch (CassandraClusterException e) {
			throw new JdbcIntActivityQueryProcessingException("Unable to process the query string : " + query
					+ " due to unable to get the cluster with cassandra");

		}
	}// end of method executeSQLQueryByDatContext

	/**
	 * This method is used execute the update|insert|delete operation of sql using
	 * apache metamodel and "cassandra-jdbc" driver. To perform
	 * update|insert|delete, it require to get connection object and pass it to
	 * metamodel api to get updateable data context
	 * 
	 * @param configObject
	 * 
	 * @param operation
	 * @param query
	 * @param configHelper
	 * @throws JdbcIntActivityQueryProcessingException
	 * @throws JdbcIntActivityConfigurationException
	 */
	private void executeSQLQueryByUpdateableDataContext(String dbType, Exchange exchange, JDBCIntActivity configObject,
			String operation, String query, JdbcIntActivityConfigHelper configHelper,
			Map<String, String> setOfValuesProcessed, Document xmlDocument)
			throws JdbcIntActivityQueryProcessingException, JdbcIntActivityConfigurationException {
		String methodName = "executeSQLQueryByUpdateableDataContext";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection connection = null;
		try {
			log.trace("{} operation provided for sql is either insert|delete|update {}", LEAP_LOG_KEY);

			connection = getCassandraConnection();
			UpdateableDataContext updateableDataContext = getUpdateableDataContextForCassandra(connection);
			((JdbcDataContext) updateableDataContext).setIsInTransaction(false);
			configHelper.setUpdateableDataContext(updateableDataContext);
			try {
				// passing updateableDataContext because cassandra
				// insert|delete|update workes using wrapper, so passing value
				// for datacontext as null
				configHelper.decideQueryToPerform(dbType, exchange, configObject, operation, query,
						updateableDataContext, null, setOfValuesProcessed, xmlDocument);
			} catch (JdbcIntActivityExecutionException | JdbcIntActivityStringParserException e) {
				throw new JdbcIntActivityQueryProcessingException("Unable to process the query string : " + query);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new JdbcIntActivityQueryProcessingException("Unable to process the query string : " + query
					+ " due to unable to get the connection with cassandra");

		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method executeSQLQueryByUpdateableDataContext

	/**
	 * gets the exchange body and traverse the JsonObject to get the XmlToProcess
	 * 
	 * @param exchange, is used to get the xml from exchange Body
	 * @return returns the xmlString
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	private String retreiveXmlInputFromExchangeBody(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		String methodName = "retreiveXmlInputFromExchangeBody";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String xmlInput = exchange.getIn().getBody(String.class);
		log.trace("xml input in exchange body : " + xmlInput);
		if (xmlInput != null && !(xmlInput.isEmpty())) {
			log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return xmlInput;
		} else {
			throw new JdbcIntActivityQueryProcessingException("exchange body is null,There is no data to process");
		}

	}// ..end of the method

	/**
	 * gets the xpath expressions from the JDBCIntActivity object
	 * 
	 * @param configObject, is loaded with the XpathExpressions
	 * @return the setOfXpathExpressions from the ConfigurationObject
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Set<String> getXpathExpressionFromJdbcActivityUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getXpathExpressionFromJdbcActivityUnit";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Set<String> set = new LinkedHashSet<>();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		for (int i = 0; i < arr.size(); i++) {
			String fieldStr = arr.get(i).getXPath();
			set.add(fieldStr);
		}
		if (!set.isEmpty() || set.size() != arr.size()) {
			log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return set;
		} else {
			throw new JdbcIntActivityConfigurationException(
					"Activity configuration is not formed well, as it encountered empty xpath Expression");
		}
	}// .. end of the method

	/**
	 * gets the xpath expressions from the JDBCIntActivity object
	 * 
	 * @param configObject, is loaded with the XpathExpressions
	 * @return the setOfXpathExpressions from the ConfigurationObject
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Map<String, String> getMapForExpressionFromJdbcActivityUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getMapForExpressionFromJdbcActivityUnit";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, String> set = new HashMap<String, String>();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		for (int i = 0; i < arr.size(); i++) {
			String fieldStr = arr.get(i).getXPath();
			String fieldName = arr.get(i).getField();
			set.put(fieldName, fieldStr);
		}
		if (!set.isEmpty() || set.size() != arr.size()) {
			log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return set;
		} else {
			throw new JdbcIntActivityConfigurationException(
					"Activity configuration is not formed well, as it encountered empty xpath Expression");
		}
	}// .. end of the method

	/**
	 * gets the list of field mappers available in the configuration
	 * 
	 * @param configObject, is loaded with field Mappers
	 * @return list of field mapper Keys
	 * @throws JdbcIntActivityConfigurationException
	 */
	private ArrayList<String> getListofFieldMapperKeys(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getListofFieldMapperKeys";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<String> list = new ArrayList<>();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		if (!arr.isEmpty()) {
			for (int i = 0; i < arr.size(); i++) {
				String fieldStr = arr.get(i).getField();
				list.add(fieldStr);
			}
			log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return list;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get non-empty list of fieldMapper..");
		}
	}// ..end of the method

	/**
	 * gets the sql query configured, as String
	 * 
	 * @param configObject, the configuration object is loaded with the SQL
	 * @return sql queryString to be processed
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getSqlStringFromJdbcActivityConfigUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String sqlQuery = configObject.getSQL();
		if (!sqlQuery.isEmpty()) {
			return sqlQuery;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get the Sql query configured..");
		}
	}// ..end of the method

	/**
	 * gets the SQL operation specified will be processed
	 * 
	 * @param configObject, loaded with the type of Operation
	 * @return Operation specified in String
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getSqlOperationFromJdbcActivityConfigUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String sqlOperation = configObject.getDBConfig().getOperation();
		if (!sqlOperation.isEmpty()) {
			log.info(
					"{} FrameWork is instructed to process the operation : {} : Hence preparing the rest to progress..",
					LEAP_LOG_KEY, sqlOperation);
			return sqlOperation;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get the Sql operation configured..");
		}
	}// ..end of the method

}