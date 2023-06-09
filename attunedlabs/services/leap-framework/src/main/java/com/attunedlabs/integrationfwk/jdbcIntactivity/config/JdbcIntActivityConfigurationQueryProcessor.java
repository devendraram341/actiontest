package com.attunedlabs.integrationfwk.jdbcIntactivity.config;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.config.jaxb.FieldMapper;
import com.attunedlabs.integrationfwk.config.jaxb.JDBCIntActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityExecutionException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityStringParserException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityConnectionException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.impl.JdbcIntActivityMetaModelUtil;

/**
 * Bean called from the jdbcIntActivity-implementation route
 * 
 * @author Bizruntime
 *
 */
public class JdbcIntActivityConfigurationQueryProcessor extends AbstractMetaModelBean {

	private final Logger log = LoggerFactory.getLogger(JdbcIntActivityConfigurationQueryProcessor.class.getName());
	private static final String DATA_SOURCE_KEY = "dataSourceA";
	private static final String INPUT_DATA_KEY = "data";
	private static final String INPUT_KEY = "data";

	/**
	 * processor of the Sql query from the Configuration JAXBObject
	 * 
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	@Override
	protected void processBean(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		String methodName = "processBean";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
		JdbcIntActivityConfigHelper configHelper = new JdbcIntActivityConfigHelper();
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		logger.trace("{} inProcessBean jdbcIntActivity..{}" ,LEAP_LOG_KEY, pipeactivity);
		JDBCIntActivity configObject = pipeactivity.getJDBCIntActivity();
		Set<String> xpathExpression;
		String newQuery;
		String operation;
			String dbType = configObject.getDBConfig().getDbType();

		try {
			/*
			 * @note: this snippet is before execution of Query, where
			 * substitution of field happens
			 */
			xpathExpression = getXpathExpressionFromJdbcActivityUnit(configObject);
			ArrayList<String> fieldKey = getListofFieldMapperKeys(configObject);
			String sqlQueryConfig = getSqlStringFromJdbcActivityConfigUnit(configObject);
			operation = getSqlOperationFromJdbcActivityConfigUnit(configObject);

			String inputXml = retreiveXmlFromInputJson(exchange);
			Document xmlDocument = configHelper.generateDocumentFromString(inputXml);
			List<Object> setOfValuesProcessed = configHelper.xpathProcessingOnInputXml(xpathExpression, xmlDocument);

			newQuery = configHelper.processSqlFieldSubstitution(sqlQueryConfig, fieldKey, setOfValuesProcessed,operation);
			log.info("{} The new query: {} " ,LEAP_LOG_KEY, newQuery);
		} catch (JdbcIntActivityConfigurationException e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Unable to process , as pre-processing failed! in preparation stage", e);
		}
		try {
			/*
			 * this snippet is to execute, based on the operation specified in
			 * configuration
			 */
			setDataSource(JdbcIntActivityMetaModelUtil.getDataSource(exchange.getContext(), DATA_SOURCE_KEY));
			JdbcDataContext datacontext = getLocalDataContext(exchange);
			configHelper = new JdbcIntActivityConfigHelper(datacontext, operation, newQuery);
			Object response = configHelper.chooseExecutor(dbType,exchange,configObject);
			exchange.getIn().setBody(response);
		} catch (Exception e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Not a valid jdbc operation to get it executed..The operation specified is : " + operation, e);
		}
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
	}// ..end of the method

	/**
	 * gets the exchange body and traverse the JsonObject to get the
	 * XmlToProcess
	 * 
	 * @param exchange,
	 *            is used to get the xml from exchange Body
	 * @return returns the xmlString
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	private String retreiveXmlFromInputJson(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		JSONObject jsonObject;
		String xmlInput = null;
		try {
			String jsonIn = exchange.getIn().getBody(String.class);
			jsonObject = new JSONObject(jsonIn);
			JSONObject jsonObject2 = (JSONObject) jsonObject.getJSONArray(INPUT_DATA_KEY).get(0);
			xmlInput = (String) jsonObject2.get(INPUT_KEY);
		} catch (JSONException e) {
			throw new JdbcIntActivityQueryProcessingException("Unable to process the JsonObject to get the xmlInput..",
					e);
		}
		return xmlInput;
	}// ..end of the method

	/**
	 * it gets the connection object useful for the dml operations
	 * 
	 * @param exchange,
	 *            is send to get the Context
	 * @param lookupDSName,
	 *            based on this name, it will lookup in the Context
	 * @return datasource Connection
	 * @throws JdbcIntActivityConnectionException
	 * @throws SQLException
	 */
	private Connection getDSConnection(Exchange exchange, String lookupDSName)
			throws JdbcIntActivityConnectionException {
		String methodName = "getDSConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection connection = null;
		try {
			connection = getConnection(JdbcIntActivityMetaModelUtil.getDataSource(exchange.getContext(), lookupDSName),
					exchange);
		} catch (SQLException | JdbcIntActivityPersistenceException e) {
			throw new JdbcIntActivityConnectionException("Unable to establish DataSource connection..", e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return connection;
	}// ..end of the method

	/**
	 * gets the xpath expressions from the JDBCIntActivity object
	 * 
	 * @param configObject,
	 *            is loaded with the XpathExpressions
	 * @return the setOfXpathExpressions from the ConfigurationObject
	 * @throws JdbcIntActivityConfigurationException
	 */
	private Set<String> getXpathExpressionFromJdbcActivityUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getXpathExpressionFromJdbcActivityUnit";
		logger.debug("{} entered into the method {}, ", LEAP_LOG_KEY, methodName);
		Set<String> set = new LinkedHashSet();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		for (int i = 0; i < arr.size(); i++) {
			String fieldStr = arr.get(i).getXPath();
			set.add(fieldStr);
		}
		if (!set.isEmpty() || set.size() != arr.size()) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return set;
		} else {
			throw new JdbcIntActivityConfigurationException(
					"Activity configuration is not formed well, as it encountered empty xpath Expression");
		}
	}// .. end of the method

	/**
	 * gets the list of field mappers available in the configuration
	 * 
	 * @param configObject,
	 *            is loaded with field Mappers
	 * @return list of field mapper Keys
	 * @throws JdbcIntActivityConfigurationException
	 */
	private ArrayList<String> getListofFieldMapperKeys(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getListofFieldMapperKeys";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<String> list = new ArrayList<>();
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) configObject.getDbmsMapper().getFieldMapper();
		if (!arr.isEmpty()) {
			for (int i = 0; i < arr.size(); i++) {
				String fieldStr = arr.get(i).getField();
				list.add(fieldStr);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return list;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get non-empty list of fieldMapper..");
		}
	}// ..end of the method

	/**
	 * gets the sql query configured, as String
	 * 
	 * @param configObject,
	 *            the configuration object is loaded with the SQL
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
	 * @param configObject,
	 *            loaded with the type of Operation
	 * @return Operation specified in String
	 * @throws JdbcIntActivityConfigurationException
	 */
	private String getSqlOperationFromJdbcActivityConfigUnit(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String sqlOperation = configObject.getDBConfig().getOperation();
		if (!sqlOperation.isEmpty()) {
			log.info("{} FrameWork is instructed to process the operation : {} : Hence preparing the rest to progress..",LEAP_LOG_KEY,sqlOperation);
			return sqlOperation;
		} else {
			throw new JdbcIntActivityConfigurationException("Unable to get the Sql operation configured..");
		}
	}// ..end of the method

	/**
	 * takes the configName as parameter, which searches the exchange header to
	 * get the ConfigurationObject
	 * 
	 * @param exchange,
	 *            in order to process the exchange header
	 * @param configName,
	 *            acts as key to get the Object
	 * @return JDBCIntActivity object
	 * @throws JdbcIntActivityConfigurationLoaderException
	 */
	private JDBCIntActivity getJdbcIntActivityUnitByNameFromHeader(Exchange exchange, String configHeaderName)
			throws JdbcIntActivityConfigurationLoaderException {
		String methodName = "getJdbcIntActivityUnitByNameFromHeader";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		log.trace("{} Checking the headers and values available: {}" ,LEAP_LOG_KEY, exchange.getIn().getHeaders());
		JDBCIntActivity configObject = (JDBCIntActivity) exchange.getIn().getHeader(configHeaderName);
		log.trace("{} the config object loaded from header  {}" ,LEAP_LOG_KEY, configObject);
		if (configObject != null) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configObject;
		} else {
			throw new JdbcIntActivityConfigurationLoaderException(
					"Unable to load non-empty the configuration - " + configHeaderName + " - from contextHeader");
		}
		
	}// ..end of the method
}
