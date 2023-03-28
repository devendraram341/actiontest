package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.eventframework.abstractbean.LeapMetaModelBean;
import com.attunedlabs.integrationfwk.config.jaxb.DbmsMapper;
import com.attunedlabs.integrationfwk.config.jaxb.FieldMapper;
import com.attunedlabs.integrationfwk.config.jaxb.JDBCIntActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.JdbcIntActivityConfigurationLoaderException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.JdbcIntActivityQueryProcessingException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigHelper;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper.JdbcIntActivityConfigurationException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityConnectionException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.JdbcIntActivityPersistenceException;
import com.attunedlabs.integrationfwk.jdbcIntactivity.config.persistence.impl.JdbcIntActivityMetaModelUtil;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;

public class JdbcIntActivity extends AbstractMetaModelBean {

	private final Logger log = LoggerFactory.getLogger(JdbcIntActivity.class.getName());

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
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		log.trace("{} jdbcIntActivity..{} ", LEAP_LOG_KEY, pipeactivity);
		JDBCIntActivity configObject = pipeactivity.getJDBCIntActivity();
		Set<String> xpathExpression;
		String newQuery;
		String operation;
		Document xmlDocument;
		String dbType = configObject.getDBConfig().getDbType();

		try {
			/*
			 * @note: this snippet is before execution of Query, where substitution of field
			 * happens
			 */
			xpathExpression = getXpathExpressionFromJdbcActivityUnit(configObject);
			ArrayList<String> fieldKey = getListofFieldMapperKeys(configObject);
			String sqlQueryConfig = getSqlStringFromJdbcActivityConfigUnit(configObject);
			operation = getSqlOperationFromJdbcActivityConfigUnit(configObject);
			// String inputXml = XML_RAW_VALUE;
			String inputXml = retreiveXmlInputFromExchangeBody(exchange);
			log.trace("{} before un escaped Xml : {}", LEAP_LOG_KEY, inputXml);

			inputXml = StringEscapeUtils.unescapeXml(inputXml);
			log.trace("{} escaped Xml : {}", LEAP_LOG_KEY, inputXml);
			xmlDocument = configHelper.generateDocumentFromString(inputXml);
			List<Object> setOfValuesProcessed = configHelper.xpathProcessingOnInputXml(xpathExpression, xmlDocument);

			newQuery = configHelper.processSqlFieldSubstitution(sqlQueryConfig, fieldKey, setOfValuesProcessed,
					operation);
			log.info("{} The new query: {}", LEAP_LOG_KEY, newQuery);
		} catch (JdbcIntActivityConfigurationException e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Unable to process , as pre-processing failed! in preparation stage", e);
		}
		try {
			/*
			 * this snippet is to execute, based on the operation specified in configuration
			 * 
			 */
			setDataSource(JdbcIntActivityMetaModelUtil.getDataSource(exchange.getContext(),
					getDatacontext(leapServiceContext).getDbBeanRefName()));
			JdbcDataContext datacontext = getLocalDataContext(exchange);
			configHelper = new JdbcIntActivityConfigHelper(datacontext, operation, newQuery);
			Object response = configHelper.chooseExecutor(dbType, exchange, configObject);
			if (response == null)
				return;
			if ("SELECT".equals(operation)) {
				ArrayList<String> colums = configHelper.getColumnNamesFromSelectQuery(newQuery);
				log.trace("{} columns : {}", LEAP_LOG_KEY, colums);
				Row rowresp = (Row) response;
				List<Object> listOfValues = Arrays.asList(rowresp.getValues());
				if (listOfValues.size() == colums.size()) {
					String xmlEnriched = configHelper.processxmlEnrichment(xmlDocument, configObject, colums,
							listOfValues);
					exchange.getIn().setBody(xmlEnriched);
				} else if (newQuery.toLowerCase().contains("*")) {
					String tableName = JdbcIntActivityConfigHelper.getTableNameFromSelectQuery(newQuery);
					colums = (ArrayList<String>) datacontext.getDefaultSchema().getTableByName(tableName)
							.getColumnNames();
					String xmlEnriched = configHelper.processxmlEnrichment(xmlDocument, configObject, colums,
							listOfValues);
					exchange.getIn().setBody(xmlEnriched);
				}
			} else {
				exchange.getIn().setBody(response);
			}
		} catch (Exception e) {
			throw new JdbcIntActivityQueryProcessingException(
					"Not a valid jdbc operation to get it executed..The operation specified is : " + operation + " : "
							+ e.getMessage(),
					e);
		}
		log.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}// ..end of the method

	private static DataContext getDatacontext(LeapServiceContext leapServiceContext) throws Exception {
		LeapMetaModelBean bean = new LeapMetaModelBean();
		return bean.loadDataContext(leapServiceContext);
	}

	/**
	 * gets the exchange body and traverse the JsonObject to get the XmlToProcess
	 * 
	 * @param exchange, is used to get the xml from exchange Body
	 * @return returns the xmlString
	 * @throws JdbcIntActivityQueryProcessingException
	 */
	private String retreiveXmlInputFromExchangeBody(Exchange exchange) throws JdbcIntActivityQueryProcessingException {
		String methodName = "retreiveXmlInputFromExchangeBody";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String xmlInput = exchange.getIn().getBody(String.class);
		log.info("{} xml input in exchange body : {} " + xmlInput);
		if (xmlInput != null && !(xmlInput.isEmpty())) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return xmlInput;
		} else {
			throw new JdbcIntActivityQueryProcessingException("exchange body is null,There is no data to process");
		}

	}// ..end of the method

	/**
	 * it gets the connection object useful for the dml operations
	 * 
	 * @param exchange,     is send to get the Context
	 * @param lookupDSName, based on this name, it will lookup in the Context
	 * @return datasource Connection
	 * @throws JdbcIntActivityConnectionException
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
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
		log.trace("{} Connection object, returned: {}", LEAP_LOG_KEY, connection);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return connection;
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
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Set<String> set = new LinkedHashSet<>();
		DbmsMapper dbmsMapper = configObject.getDbmsMapper();
		if (dbmsMapper == null)
			return null;
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) dbmsMapper.getFieldMapper();
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
	 * @param configObject, is loaded with field Mappers
	 * @return list of field mapper Keys
	 * @throws JdbcIntActivityConfigurationException
	 */
	private ArrayList<String> getListofFieldMapperKeys(JDBCIntActivity configObject)
			throws JdbcIntActivityConfigurationException {
		String methodName = "getListofFieldMapperKeys";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<String> list = new ArrayList<>();
		DbmsMapper dbmsMapper = configObject.getDbmsMapper();
		if (dbmsMapper == null)
			return null;
		ArrayList<FieldMapper> arr = (ArrayList<FieldMapper>) dbmsMapper.getFieldMapper();
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

	/**
	 * takes the configName as parameter, which searches the exchange header to get
	 * the ConfigurationObject
	 * 
	 * @param exchange,   in order to process the exchange header
	 * @param configName, acts as key to get the Object
	 * @return JDBCIntActivity object
	 * @throws JdbcIntActivityConfigurationLoaderException
	 */
	@SuppressWarnings("unused")
	private JDBCIntActivity getJdbcIntActivityUnitByNameFromHeader(Exchange exchange, String configHeaderName)
			throws JdbcIntActivityConfigurationLoaderException {
		log.trace("{} Checking the headers and values available: {}",LEAP_LOG_KEY, exchange.getIn().getHeaders());
		JDBCIntActivity configObject = (JDBCIntActivity) exchange.getIn().getHeader(configHeaderName);
		log.trace("{} the config object loaded from header {}",LEAP_LOG_KEY, configObject);
		if (configObject != null) {
			return configObject;
		} else {
			throw new JdbcIntActivityConfigurationLoaderException(
					"Unable to load non-empty the configuration - " + configHeaderName + " - from contextHeader");
		}
	}// ..end of the method
}
