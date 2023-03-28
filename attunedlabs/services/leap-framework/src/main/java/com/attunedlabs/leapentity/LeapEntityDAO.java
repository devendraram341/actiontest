package com.attunedlabs.leapentity;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.cassandra.CassandraCustomDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.schema.TableType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.eventframework.abstractbean.LeapMetaModelBean;
import com.attunedlabs.eventframework.abstractbean.TableDoesNotExistException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.base.ComputeTimeBean;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leapentity.leapdata.LeapDataConfiguration;

public class LeapEntityDAO extends LeapMetaModelBean {
	public static final String INITIAL_CONTEXT = "#leap_initial";
	private static final String DATATYPE = "datatype";
	private static final String ISPRIMARYKEY = "isprimarykey";
	private static final String ISNOTNULL = "isnotnull";
	private static final String ISINDEXED = "isindexed";
	private static final String SIZE = "size";
	private static final Logger logger = LoggerFactory.getLogger(LeapEntityDAO.class);
	private static final String DATATYPE_INTEGER = "integer";
	private static final String DATATYPE_VARCHAR = "varchar";
	private static final String DATATYPE_DATE = "date";
	private static final String DATATYPE_DOUBLE = "double";
	private static final String DATATYPE_BOOLEAN = "boolean";
	private static final String DATATYPE_TIME = "time";
	private static final String DATATYPE_TIMESTAMP = "timestamp";
	private static final String DATATYPE_TEXT = "text";
	private static final String DATATYPE_DECIMAL = "decimal";
	private static final String DATATYPE_FLOAT = "float";
	private static final String DATATYPE_CHAR = "char";
	private static final String DATATYPE_LONG = "long";
	private static final String DATATYPE_NUMBER = "number";
	private static final String DATATYPE_NVARCHAR = "nvarchar";

	LeapDataConfiguration leapdataConfig = new LeapDataConfiguration();
	ComputeTimeBean time = new ComputeTimeBean();

	@SuppressWarnings("unchecked")
	public JSONObject exceuteReadOperation(String entityName, List<String> selectColumnsList,
			Map<String, Object> whereClauseMap, Exchange exchange) throws LeapEntityDAOException {
		String methodName = "exceuteReadOperation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			JSONArray responseArray = new JSONArray();
			DataContext datacontext = null;
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);

			String predicateString = (String) whereClauseMap.get("predicateString");
			List<Object> predicatefieldList = (List<Object>) whereClauseMap.get("columnValue");
			DataSet dataSet = doLeapSelect(entityName, predicateString, predicatefieldList, selectColumnsList,
					jdbcContextAndOperation, leapServiceContext);
			Object obj = jdbcContextAndOperation.get(0);
			if (obj instanceof CassandraCustomDataContext)
				datacontext = (CassandraCustomDataContext) obj;
			else
				datacontext = (JdbcDataContext) obj;

			Table table = datacontext.getTableByQualifiedLabel(entityName);
			List<Column> coulmnsList = null;
			if (selectColumnsList.isEmpty()) {
				coulmnsList = table.getColumns();
			} else
				coulmnsList = getSelectColumn(selectColumnsList, table);

			while (dataSet.next()) {
				JSONObject responseJson = new JSONObject();
				Row row = dataSet.getRow();
				for (Column column : coulmnsList) {
					responseJson.put(column.getName(), row.getValue(column));
				}
				responseArray.put(responseJson);
			}
			logger.info("{} response  is : {}", LEAP_LOG_KEY, responseArray);
			JSONObject json = new JSONObject();
			json.put(entityName, responseArray);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityDAOException(
					"Unable to do read operation for entity " + entityName + " :" + e.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	public int exceuteDeleteOperation(String entityName, Map<String, Object> whereClauseMap, Exchange exchange)
			throws LeapEntityDAOException {
		String methodName = "exceuteDeleteOperation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);
			String predicateString = (String) whereClauseMap.get("predicateString");
			List<Object> predicatefieldList = (List<Object>) whereClauseMap.get("columnValue");
			List<String> predicateColumnName = (List<String>) whereClauseMap.get("columnName");
			int doLeapDelete = doLeapDelete(entityName, predicateString, predicatefieldList, predicateColumnName,
					jdbcContextAndOperation, leapServiceContext);
			logger.info("{} impact row of deleted operation {}", LEAP_LOG_KEY, doLeapDelete);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, getClass().getName(), methodName);
			return doLeapDelete;
		} catch (Exception e) {
			throw new LeapEntityDAOException(
					"Unable to do delete operation for entity " + entityName + " :" + e.getMessage());
		}

	}

	public void exceuteInsertOperation(String entityName, List<String> insertableColumns, List<Object> insertableValues,
			Exchange exchange, String autoIncrement) throws LeapEntityDAOException, TableDoesNotExistException {

		String methodName = "exceuteInsertOperation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceContext leapServiceContext = null;
		List<Object> jdbcContextAndOperation = null;
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			leapServiceContext = leapDataContext.getServiceDataContext();
			jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);

			doLeapInsert(entityName, insertableColumns, insertableValues, jdbcContextAndOperation, leapServiceContext);

		} catch (TableDoesNotExistException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityDAOException(
					"Unable to do insert operation for entity " + entityName + " :" + e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeUpdateOperation(String entityName, Map<String, Object> updateWhereClauseMap,
			Map<String, List> updateMap, Exchange exchange) throws LeapEntityDAOException {
		String methodName = "executeUpdateOperation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);

			String predicateString = (String) updateWhereClauseMap.get("predicateString");
			List<String> predicatefieldList = (List<String>) updateWhereClauseMap.get("columnValue");
			List<String> predicateColumnList = (List<String>) updateWhereClauseMap.get("columnName");
			List<String> updateColumnValueList = updateMap.get("columnValue");
			List<String> updateColumnNameList = updateMap.get("columnName");
			doLeapUpdate(entityName, predicateString, predicatefieldList, predicateColumnList, updateColumnNameList,
					updateColumnValueList, jdbcContextAndOperation, leapServiceContext);
		} catch (Exception e) {
			throw new LeapEntityDAOException(
					"Unable to do update operation for entity " + entityName + " :" + e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void createTable(String entityName, List<Map<String, Object>> metaDetaList, Exchange exchange)
			throws Exception {
		String methodName = "createTable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);
			JdbcDataContext dataContext = (JdbcDataContext) jdbcContextAndOperation.get(0);
			String schemaName = dataContext.getDefaultSchema().getName();
			MutableTable table = new MutableTable(entityName, TableType.TABLE, new MutableSchema(schemaName));
			int metaDataColumnCount = metaDetaList.size();
			for (int index = 0; index < metaDataColumnCount; index++) {
				Map<String, Object> metaDeta = metaDetaList.get(index);
				MutableColumn column = new MutableColumn();
				column.setName(metaDeta.get("columnName").toString());
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> attributeList = (List<Map<String, Object>>) metaDeta.get("attributeList");
				for (Map<String, Object> attribute : attributeList) {
					String attributeName = attribute.get("attributeName").toString();
					String value = attribute.get("value").toString();
					int size = 25;
					if (attribute.containsKey(SIZE)) {
						size = Integer.parseInt(attribute.get(SIZE).toString());
					}
					switch (attributeName.toLowerCase()) {
					case DATATYPE:
						ColumnType columnType = getColumnType(value);
						if (columnType != null) {
							column.setType(columnType);
							if (columnType.equals(ColumnType.VARCHAR)) {
								column.setColumnSize(size);
							}
							if (columnType.equals(ColumnType.NVARCHAR)) {
								column.setColumnSize(size);
							}
						}
						break;
					case ISPRIMARYKEY:
						if (value.equalsIgnoreCase("true")) {
							column.setPrimaryKey(true);
						}
						break;
					case ISNOTNULL:
						if (value.equalsIgnoreCase("true")) {
							column.setNullable(false);
						}
						break;
					case ISINDEXED:
						if (value.equalsIgnoreCase("true")) {
							column.setIndexed(true);
						}
						break;
					case "autoincrement":
						break;
					default:
						throw new LeapEntityDAOException(
								"Unable to create entity " + entityName + " :" + attributeName + " is not valid ");
					}
				}
				logger.debug("{} column :: {}, isindexed :{}, isprimaryKey ::{}", LEAP_LOG_KEY, column.toString(),
						column.isIndexed(), column.isPrimaryKey());
				table.addColumn(index, column);
			}

			MutableColumn accountId = new MutableColumn();
			accountId.setName("accountId");
			accountId.setType(ColumnType.VARCHAR);
			accountId.setColumnSize(25);
			table.addColumn(metaDataColumnCount, accountId);
			MutableColumn siteId = new MutableColumn();
			siteId.setName("siteId");
			siteId.setType(ColumnType.VARCHAR);
			siteId.setColumnSize(25);
			table.addColumn(metaDataColumnCount + 1, siteId);
			logger.trace("added");

			logger.debug("{} schemaName:: {}", LEAP_LOG_KEY, schemaName);
			dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback updateCallback) {
					String sql = updateCallback.createTable(schemaName, entityName).like(table).toSql();
					logger.debug("{} sql :: {}", LEAP_LOG_KEY, sql);
					updateCallback.createTable(schemaName, entityName).like(table).execute();
				}
			});
		} catch (LeapEntityDAOException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityDAOException("Unable to create entity " + entityName + " : " + e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	@SuppressWarnings("unused")
	private Map<String, Object> getPredicateStringAndFieldValue(Map<String, Object> whereClauseMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		Set<String> keySet = whereClauseMap.keySet();
		List<Object> fieldValueList = new ArrayList<Object>();
		List<String> columnName = new ArrayList<String>();
		String predicateString = "";
		for (String key : keySet) {
			predicateString = predicateString + key + "=? And ";
			fieldValueList.add(whereClauseMap.get(key));
			columnName.add(key);
		}
		if (!predicateString.isEmpty())
			predicateString = predicateString.substring(0, predicateString.lastIndexOf(" And "));
		logger.debug("{} predicate string is  : {} ", LEAP_LOG_KEY, predicateString);
		logger.debug("{} fieldValueList is  : {} ", LEAP_LOG_KEY, fieldValueList);
		map.put("predicateString", predicateString);
		map.put("fieldValueList", fieldValueList);
		map.put("predicateColumn", columnName);
		return map;

	}

	private List<Column> getSelectColumn(List<String> selectColumnsList, Table table) {
		List<Column> columnsList = new ArrayList<Column>();
		for (String column : selectColumnsList) {
			columnsList.add(table.getColumnByName(column));
		}
		return columnsList;
	}

	private ColumnType getColumnType(String value) throws Exception {
		switch (value.toLowerCase()) {
		case DATATYPE_INTEGER:
			return ColumnType.INTEGER;
		case DATATYPE_VARCHAR:
			return ColumnType.VARCHAR;
		case DATATYPE_DATE:
			return ColumnType.DATE;
		case DATATYPE_DOUBLE:
			return ColumnType.DOUBLE;
		case DATATYPE_BOOLEAN:
			return ColumnType.BOOLEAN;
		case DATATYPE_TIME:
			return ColumnType.TIME;
		case DATATYPE_TIMESTAMP:
			return ColumnType.TIMESTAMP;
		case DATATYPE_TEXT:
			return ColumnType.STRING;
		case DATATYPE_DECIMAL:
			return ColumnType.DECIMAL;
		case DATATYPE_FLOAT:
			return ColumnType.FLOAT;
		case DATATYPE_CHAR:
			return ColumnType.CHAR;
		case DATATYPE_LONG:
			return ColumnType.BIGINT;
		case DATATYPE_NUMBER:
			return ColumnType.NUMBER;
		case DATATYPE_NVARCHAR:
			return ColumnType.NVARCHAR;
		default:
			throw new Exception(" DataType " + value + " is not valid");
		}
	}

}
