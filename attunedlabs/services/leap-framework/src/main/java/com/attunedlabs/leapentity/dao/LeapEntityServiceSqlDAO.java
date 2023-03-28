package com.attunedlabs.leapentity.dao;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.cassandra.CassandraCustomDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
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
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leapentity.config.jaxb.Attribute;
import com.attunedlabs.leapentity.config.jaxb.EntityColumn;
import com.attunedlabs.leapentity.config.jaxb.EntityIndex;
import com.attunedlabs.leapentity.config.jaxb.EntityTable;

public class LeapEntityServiceSqlDAO extends LeapMetaModelBean {
	public static final String INITIAL_CONTEXT = "#leap_initial";
	private static final String DATATYPE = "datatype";
	private static final String ISPRIMARYKEY = "isprimarykey";
	private static final String ISNOTNULL = "isnotnull";
	private static final Logger logger = LoggerFactory.getLogger(LeapEntityServiceSqlDAO.class);
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

	@SuppressWarnings("unchecked")
	public JSONObject exceuteReadOperation(String entityName, String collectionName, String tableName,
			List<String> selectColumnsList, Map<String, Object> whereClauseMap, boolean isCollection,
			Map<String, String> entityColumnToFieldMap, Exchange exchange) throws LeapEntityServiceSqlDAOException {
		String methodName = "exceuteReadOperation";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		try {
			JSONArray responseArray = new JSONArray();
			DataContext datacontext = null;
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);

			String predicateString = (String) whereClauseMap.get("predicateString");
			List<Object> predicatefieldList = (List<Object>) whereClauseMap.get("columnValue");
			DataSet dataSet = doLeapSelect(tableName, predicateString, predicatefieldList, selectColumnsList,
					jdbcContextAndOperation, leapServiceContext);
			Object obj = jdbcContextAndOperation.get(0);
			if (obj instanceof CassandraCustomDataContext)
				datacontext = (CassandraCustomDataContext) obj;
			else
				datacontext = (JdbcDataContext) obj;

			Table table = datacontext.getTableByQualifiedLabel(tableName);
			List<Column> coulmnsList = null;
			if (selectColumnsList.isEmpty()) {
				coulmnsList = table.getColumns();
			} else
				coulmnsList = getSelectColumn(selectColumnsList, table);

			while (dataSet.next()) {
				JSONObject responseJson = new JSONObject();
				Row row = dataSet.getRow();
				for (Column column : coulmnsList) {
					String entityFieldName = entityColumnToFieldMap.get(column.getName());
					responseJson.put(entityFieldName, row.getValue(column));
				}
				responseArray.put(responseJson);
			}
			logger.info("{} response  is : {}", LEAP_LOG_KEY, responseArray);
			JSONObject json = new JSONObject();
			if (!isCollection) {
				JSONObject jsonObject = new JSONObject();
				if (responseArray.length() > 1) {
					throw new LeapEntityServiceSqlDAOException("Unable to do read operation for entity " + entityName
							+ " Conflict in response due collection property is false", 409);
				} else {
					jsonObject = responseArray.getJSONObject(0);
				}
				json.put(entityName, jsonObject);

			} else {
				if (collectionName.isEmpty()) {
					throw new LeapEntityServiceSqlDAOException(
							"Unable to do read operation for entity " + entityName + " collectionName is Not Found ",
							404);
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(entityName, responseArray);
				json.put(collectionName, jsonObject);
			}
			logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
			return json;
		} catch (LeapEntityServiceSqlDAOException e) {
			logger.error("{} error : {}", LEAP_LOG_KEY, e.getMessage());
			throw e;

		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityServiceSqlDAOException(
					"Unable to do read operation for entity " + entityName + " :" + e.getMessage(), 500);
		}

	}

	@SuppressWarnings("unchecked")
	public int exceuteDeleteOperation(String entityName, String tableName, Map<String, Object> whereClauseMap,
			Exchange exchange) throws LeapEntityServiceSqlDAOException {
		String methodName = "exceuteDeleteOperation";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);
			String predicateString = (String) whereClauseMap.get("predicateString");
			List<Object> predicatefieldList = (List<Object>) whereClauseMap.get("columnValue");
			List<String> predicateColumnName = (List<String>) whereClauseMap.get("columnName");
			int doLeapDelete = doLeapDelete(tableName, predicateString, predicatefieldList, predicateColumnName,
					jdbcContextAndOperation, leapServiceContext);
			logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
			return doLeapDelete;
		} catch (Exception e) {
			throw new LeapEntityServiceSqlDAOException(
					"Unable to do read operation for entity " + entityName + " :" + e.getMessage(), 500);
		}

	}

	public void exceuteInsertOperation(String entityName, String tableName, List<String> insertableColumns,
			List<Object> insertableValues, Exchange exchange)
			throws LeapEntityServiceSqlDAOException, TableDoesNotExistException {

		String methodName = "exceuteInsertOperation";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		LeapServiceContext leapServiceContext = null;
		List<Object> jdbcContextAndOperation = null;
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			leapServiceContext = leapDataContext.getServiceDataContext();
			jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);

			doLeapInsert(tableName, insertableColumns, insertableValues, jdbcContextAndOperation, leapServiceContext);

		} catch (TableDoesNotExistException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityServiceSqlDAOException(
					"Unable to do read operation for entity " + entityName + " :" + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeUpdateOperation(String entityName, String tableName, Map<String, Object> updateWhereClauseMap,
			Map<String, List> updateMap, Exchange exchange) throws LeapEntityServiceSqlDAOException {
		String methodName = "executeUpdateOperation";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
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
			doLeapUpdate(tableName, predicateString, predicatefieldList, predicateColumnList, updateColumnNameList,
					updateColumnValueList, jdbcContextAndOperation, leapServiceContext);
		} catch (Exception e) {
			throw new LeapEntityServiceSqlDAOException(
					"Unable to do read operation for entity " + entityName + " :" + e.getMessage(), 500);
		}
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);

	}

	public void createTable(String entityName, EntityTable entityTable, Exchange exchange) throws Exception {
		String methodName = "createTable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			List<Object> jdbcContextAndOperation = getDataContext(null, leapServiceContext, exchange);
			JdbcDataContext dataContext = (JdbcDataContext) jdbcContextAndOperation.get(0);
			String schemaName = dataContext.getDefaultSchema().getName();
			String tableName = entityTable.getTablename();
			MutableTable table = new MutableTable(tableName, TableType.TABLE, new MutableSchema(schemaName));
			List<EntityColumn> entityColumnsList = entityTable.getEntityColumns().getEntityColumn();
			List<EntityIndex> entityIndexs = entityTable.getEntityIndexs().getEntityIndex();
			int index = 0;
			for (EntityColumn entitycolumn : entityColumnsList) {
				MutableColumn column = new MutableColumn();
				String columnName = entitycolumn.getName();
				column.setName(columnName);
				List<Attribute> attributeList = entitycolumn.getAttribute();
				if (!entityIndexs.isEmpty()) {
					for (int i = 0; i < entityIndexs.size(); i++) {
						EntityIndex entityIndex = entityIndexs.get(i);
						if (entityIndex.getEntityColumnRef().equals(columnName)) {
							column.setIndexed(true);
							logger.debug("{} added index for :: {}", LEAP_LOG_KEY, columnName);
							entityIndexs.remove(i);
						}
					}
				}
				for (Attribute attribute : attributeList) {
					String attributeName = attribute.getName();
					String value = attribute.getValue();
					int size = 25;
					if (attribute.getSize() != null) {
						size = attribute.getSize();
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
					case "autoincrement":
						break;
					case "metadata":
						break;
					default:
						throw new LeapEntityServiceSqlDAOException(
								"Unable to create entity " + entityName + " :" + attributeName + " is not valid ", 404);
					}
				}
				logger.debug("{} column :: {}, isindexed :: {}, isprimaryKey  ::{}", LEAP_LOG_KEY, column.toString(),
						column.isIndexed(), column.isPrimaryKey());
				table.addColumn(index++, column);
			}

			MutableColumn accountId = new MutableColumn();
			accountId.setName("accountId");
			accountId.setType(ColumnType.VARCHAR);
			accountId.setColumnSize(25);
			table.addColumn(index++, accountId);
			MutableColumn siteId = new MutableColumn();
			siteId.setName("siteId");
			siteId.setType(ColumnType.VARCHAR);
			siteId.setColumnSize(25);
			table.addColumn(index, siteId);
			logger.trace("{} added",LEAP_LOG_KEY);
			logger.info("{} schemaName:: {} " ,LEAP_LOG_KEY, schemaName);
			dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback updateCallback) {
					TableCreationBuilder sqlQuery = updateCallback.createTable(schemaName, tableName).like(table);
					String sql = sqlQuery.toSql();
					logger.debug("{} sql Query to create Table :: {} ",LEAP_LOG_KEY, sql);
					sqlQuery.execute();
				}
			});
		} catch (LeapEntityServiceSqlDAOException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityServiceSqlDAOException("Unable to create entity " + entityName + " : " + e.getMessage(),
					500);
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
		logger.debug("{} predicate string is  : {} " ,LEAP_LOG_KEY, predicateString);
		logger.debug("{} fieldValueList is  : {} " ,LEAP_LOG_KEY, fieldValueList);
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

	private ColumnType getColumnType(String value) throws LeapEntityServiceSqlDAOException {
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
			throw new LeapEntityServiceSqlDAOException(" DataType " + value + " is not exits", 404);
		}
	}

}
