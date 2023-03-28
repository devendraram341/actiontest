package com.attunedlabs.leapentity.service.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.eventframework.abstractbean.TableDoesNotExistException;
import com.attunedlabs.leapentity.autoIncrement.ILeapEntityAutoIncrement;
import com.attunedlabs.leapentity.autoIncrement.LeapEntityAutoIncrementException;
import com.attunedlabs.leapentity.autoIncrement.LeapEntityAutoIncrementFactoryBean;
import com.attunedlabs.leapentity.config.jaxb.Attribute;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityAccessConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityColumn;
import com.attunedlabs.leapentity.config.jaxb.EntityColumns;
import com.attunedlabs.leapentity.config.jaxb.EntityDef;
import com.attunedlabs.leapentity.config.jaxb.EntityField;
import com.attunedlabs.leapentity.config.jaxb.EntityFilterKeysMapping;
import com.attunedlabs.leapentity.config.jaxb.EntityInsertKeysMapping;
import com.attunedlabs.leapentity.config.jaxb.EntityKey;
import com.attunedlabs.leapentity.config.jaxb.EntityReadKeysMapping;
import com.attunedlabs.leapentity.config.jaxb.EntityRestAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityTable;
import com.attunedlabs.leapentity.config.jaxb.EntityUpdateKeysMapping;
import com.attunedlabs.leapentity.dao.LeapEntityServiceSqlDAO;
import com.attunedlabs.leapentity.dao.LeapEntityServiceSqlDAOException;
import com.attunedlabs.leapentity.service.ILeapEntityService;
import com.attunedlabs.leapentity.service.LeapEntityServiceException;

/**
 * SQL type Impl of EntityService
 * 
 * @author Reactiveworks42
 *
 */
public class LeapEntityServiceSqlTypeImpl implements ILeapEntityService {

	private static final Logger logger = LoggerFactory.getLogger(LeapEntityServiceSqlTypeImpl.class);
	private LeapEntityServiceSqlDAO entitySqlDao = new LeapEntityServiceSqlDAO();
	private HashMap<String, String> entityFieldToColumnMap = new HashMap<String, String>();
	private HashMap<String, String> entityColumnToFieldMap = new HashMap<String, String>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object performEntityOperation(Entity entity, EntityAccessConfig entityAccessConfig,
			EntityAccess entityAccess, EntityRestAccess entityRestAccess, Object requestJson, Exchange exchange)
			throws LeapEntityServiceException {
		String methodName = "performEntityOperation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			String entityName = entity.getName();
			exchange.getIn().setHeader("leapEndpointKindName", entityName);
			String accessType = entityAccess.getAccessType();
			String tableName = entityAccessConfig.getEntityTable().getTablename();
			boolean autoCreate = entityAccessConfig.getEntityTable().isAutoCreate();
			EntityDef entityDef = entity.getEntityDef();
			String collectionName = entityDef.getEntityCollection().getCollectionName();
			loadEntityFieldAndColumnMap(entity, entityAccessConfig);
			logger.trace("{} entityFieldMap ::: {} ", LEAP_LOG_KEY, entityFieldToColumnMap);
			logger.debug("{} entityName is : {}, autoCreate is : {}, accessType is : {}", LEAP_LOG_KEY, entityName,
					autoCreate, accessType);
			switch (accessType.toLowerCase()) {
			case LeapEntityServiceSolImplConstant.READ:
				List<Map<String, String>> entityFilterkeyMapList = getEntityFilterKeyMap(entityAccess);
				logger.trace("{} entityFilterkeyMapList is : {} " ,LEAP_LOG_KEY, entityFilterkeyMapList);
				List<String> selectColumnsList = getEntityReadKeysMapping(entityAccess);
				if (selectColumnsList.isEmpty()) {
					selectColumnsList = getAllColumnNames();
				}
				logger.debug("{} selectColumnsList is : {} " ,LEAP_LOG_KEY, selectColumnsList);
				Map<String, Object> whereClauseMap = getWhereClauseMap(entityFilterkeyMapList,
						(JSONObject) requestJson);
				logger.debug("{} whereClauseMap is : {} " ,LEAP_LOG_KEY, whereClauseMap);
				boolean isCollection = entityAccess.isIsCollection();
				JSONObject exceuteReadOperation = entitySqlDao.exceuteReadOperation(entityName, collectionName, tableName, selectColumnsList,
						whereClauseMap, isCollection, entityColumnToFieldMap, exchange);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return exceuteReadOperation;
			case LeapEntityServiceSolImplConstant.UPDATE:
				List<Map<String, String>> entityFilterkeyUpdateMapList = getEntityFilterKeyMap(entityAccess);
				logger.trace("{} entityFilterkeyUpdateMapList is : {} " ,LEAP_LOG_KEY, entityFilterkeyUpdateMapList);
				List<Map<String, String>> entityUpdatekeyMapList = getEntityUpdateKeyMap(entityAccess);
				logger.debug("{} entityUpdatekeyMapList is :  {} " ,LEAP_LOG_KEY, entityUpdatekeyMapList.toString());
				Map<String, List> updateMap = getColumnNameAndValueMap(entityUpdatekeyMapList,
						(JSONObject) requestJson);
				logger.debug("{} updateMap is :  {}" ,LEAP_LOG_KEY, updateMap);
				Map<String, Object> updateWhereClauseMap = getWhereClauseMap(entityFilterkeyUpdateMapList,
						(JSONObject) requestJson);
				logger.debug("{} updateWhereClauseMap is :  {}" ,LEAP_LOG_KEY, updateWhereClauseMap);
				entitySqlDao.executeUpdateOperation(entityName, tableName, updateWhereClauseMap, updateMap, exchange);
				JSONObject updateResponseJson = new JSONObject();
				updateResponseJson.put("success", true);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return updateResponseJson;
			case LeapEntityServiceSolImplConstant.CREATE:
				List<Map<String, String>> entityInsertkeyMapList = getEntityInsertKeyMap(entityAccess);
				logger.trace("{} entityInsertkeyMapList is : {}" ,LEAP_LOG_KEY, entityInsertkeyMapList);
				if (entityInsertkeyMapList.isEmpty()) {
					throw new LeapEntityServiceException(new Throwable(), " Unable to perform Entity operation ",
							"EntityInsertKeysMapping is required in configuration", 412);
				}
				Map<String, List> insertMap = getColumnNameAndValueMap(entityInsertkeyMapList,
						(JSONObject) requestJson);
				logger.debug("{} insertMap is : {}" ,LEAP_LOG_KEY, insertMap);
				List columnNameList = insertMap.get("columnName");
				List columnValueList = insertMap.get("columnValue");
				checkAndUpdateColumnsForAutoIncrement(entityAccessConfig, columnNameList, columnValueList);
				logger.debug("{} columnName :: {}",LEAP_LOG_KEY, columnNameList);
				logger.debug("{} columnValue :: {}" ,LEAP_LOG_KEY, columnValueList);
				try {
					entitySqlDao.exceuteInsertOperation(entityName, tableName, columnNameList, columnValueList,
							exchange);
				} catch (TableDoesNotExistException e) {
					if (autoCreate) {
						EntityTable entityTable = entityAccessConfig.getEntityTable();
						autoCreateTable(entityName, entityTable, exchange);
						entitySqlDao.exceuteInsertOperation(entityName, tableName, columnNameList, columnValueList,
								exchange);
					} else {
						throw e;
					}
				}
				JSONObject insertResponseJson = new JSONObject();
				insertResponseJson.put("success", true);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return insertResponseJson;

			case LeapEntityServiceSolImplConstant.DELETE:
				List<Map<String, String>> entityFilterkeydeleteMapList = getEntityFilterKeyMap(entityAccess);
				logger.trace("{} entityFilterkeydeleteMapList is : {} " ,LEAP_LOG_KEY, entityFilterkeydeleteMapList);
				Map<String, Object> deleteWhereClauseMap = getWhereClauseMap(entityFilterkeydeleteMapList,
						(JSONObject) requestJson);
				logger.debug("{} whereClauseMap is : {}" ,LEAP_LOG_KEY, deleteWhereClauseMap);
				int rowsDelete = entitySqlDao.exceuteDeleteOperation(entityName, tableName, deleteWhereClauseMap,
						exchange);
				JSONObject deleteResponseJson = new JSONObject();
				deleteResponseJson.put("success", true);
				deleteResponseJson.put("noOfRowsDeleted", rowsDelete);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return deleteResponseJson;
			default:
				throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
						" accessType " + accessType + " is not valid", 404);
			}

		} catch (LeapEntityServiceException e) {
			logger.error("{} error :: {}",LEAP_LOG_KEY, e.getMessage());
			throw e;

		} catch (LeapEntityServiceSqlDAOException e) {
			e.printStackTrace();
			logger.error("{} Unable to execute Entity {}" ,LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ", e.getMessage(),
					e.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} Unable to execute Entity {}" ,LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation", e.getMessage(),
					500);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void checkAndUpdateColumnsForAutoIncrement(EntityAccessConfig entityAccessConfig, List columnNameList,
			List columnValueList) throws LeapEntityAutoIncrementException {
		EntityTable entityTable = entityAccessConfig.getEntityTable();
		for (EntityColumn entityColumn : entityTable.getEntityColumns().getEntityColumn()) {
			if (entityColumn.isHasAutoIncrement()) {
				String autoIncrementColumnName = entityColumn.getName();
				String dataTypeValue = "";
				String autoIncrementType = "";
				String autoIncrementValue = "";
				List<Attribute> attributes = entityColumn.getAttribute();
				for (Attribute attribute : attributes) {
					if (attribute.getName().equalsIgnoreCase("autoIncrement")) {
						autoIncrementType = attribute.getType();
						autoIncrementValue = attribute.getValue();
						dataTypeValue = attribute.getAttributeDatatype();
						dataTypeValue = dataTypeValue.toLowerCase();
						break;
					}
				}
				logger.trace("{} dataTypeValue :: {}" ,LEAP_LOG_KEY, dataTypeValue);
				ILeapEntityAutoIncrement iLeapEntityAutoIncrement = LeapEntityAutoIncrementFactoryBean
						.getInstance(autoIncrementType, autoIncrementValue);
				if (iLeapEntityAutoIncrement != null) {
					Object autoIncrementColumnValue = iLeapEntityAutoIncrement.getAutoIncrementValue(dataTypeValue);
					logger.debug("{} columnNameList :: {}" ,LEAP_LOG_KEY, columnNameList);
					logger.debug("{} autoIncrementColumn :: {}" ,LEAP_LOG_KEY, autoIncrementColumnName);
					if (!columnNameList.contains(autoIncrementColumnName)) {
						columnNameList.add(autoIncrementColumnName);
						columnValueList.add(autoIncrementColumnValue);
					} else {
						for (int index = 0; index < columnNameList.size(); index++) {
							if (columnNameList.get(index).equals(autoIncrementColumnName)) {
								columnValueList.remove(index);
								columnValueList.add(index, autoIncrementColumnValue);
							}
						}
					}
				}
			}
		}
	}

	private void loadEntityFieldAndColumnMap(Entity entity, EntityAccessConfig entityAccessConfig)
			throws LeapEntityServiceException {
		EntityDef entityDef = entity.getEntityDef();
		List<EntityField> entityFields = entityDef.getEntityFields().getEntityField();
		List<EntityColumn> entityColumns = entityAccessConfig.getEntityTable().getEntityColumns().getEntityColumn();
		int totalColums = entityColumns.size();
		if (totalColums == entityFields.size()) {
			List<String> entityFieldsNameList = new ArrayList<String>();
			entityFields.stream().forEach(field -> entityFieldsNameList.add(field.getName()));
			for (int index = 0; index < totalColums; index++) {
				EntityColumn column = entityColumns.get(index);
				String columnName = column.getName();
				String fieldRefName = column.getEntityFieldNameRef();
				if (entityFieldsNameList.contains(fieldRefName)) {
					entityFieldToColumnMap.put(fieldRefName, columnName);
					entityColumnToFieldMap.put(columnName, fieldRefName);
				} else {
					throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
							" entityFieldNameRef " + fieldRefName + " is not exsits in EntityFields", 412);
				}
			}
		} else {
			throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
					"Mismatch configuration between EntityFields and EntityColums", 412);
		}
	}

	private void autoCreateTable(String entityName, EntityTable entityTable, Exchange exchange) throws Exception {
		logger.debug("autoCreateTable method()");
		EntityColumns entityColumns = sortEntityColumnsWithSequence(entityTable.getEntityColumns());
		entityTable.setEntityColumns(entityColumns);
		entitySqlDao.createTable(entityName, entityTable, exchange);
	}

	private EntityColumns sortEntityColumnsWithSequence(EntityColumns entityColumns) {
		List<EntityColumn> columns = entityColumns.getEntityColumn();
		logger.trace("{} columns :: {}" ,LEAP_LOG_KEY, columns);
		columns.sort((colum1, colum2) -> colum1.getSequence().compareTo(colum2.getSequence()));
		entityColumns.setEntityColumn(columns);
		logger.trace("{} columns :: {}",LEAP_LOG_KEY, columns);
		return entityColumns;
	}

	private Map<String, Object> getWhereClauseMap(List<Map<String, String>> entityInsertkeyMapList,
			JSONObject requestJson) throws LeapEntityServiceException {
		List<String> columnName = new ArrayList<String>();
		List<Object> columnValue = new ArrayList<Object>();
		Map<String, Object> insertMap = new HashMap<String, Object>();
		String predicateString = "";
		for (Map<String, String> filterMap : entityInsertkeyMapList) {
			String entityFieldName = filterMap.get(LeapEntityServiceSolImplConstant.NAME);
			String required = "mandatory";
			if (filterMap.containsKey(LeapEntityServiceSolImplConstant.REQUIRED)) {
				required = filterMap.get(LeapEntityServiceSolImplConstant.REQUIRED);
			}
			if (requestJson.has(entityFieldName)) {
				String entityColumnName = entityFieldToColumnMap.get(entityFieldName);
				columnName.add(entityColumnName);
				columnValue.add(requestJson.get(entityFieldName));
				predicateString = predicateString + entityColumnName + "=? And ";

			} else {
				if (required.equalsIgnoreCase("mandatory")) {
					throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
							" key : " + entityFieldName + " is mandatory", 400);
				}
			}
		}
		if (!predicateString.isEmpty())
			predicateString = predicateString.substring(0, predicateString.lastIndexOf(" And "));
		logger.debug("{} predicate String : {}" ,LEAP_LOG_KEY, predicateString);
		insertMap.put("columnName", columnName);
		insertMap.put("columnValue", columnValue);
		insertMap.put("predicateString", predicateString);
		return insertMap;
	}

	private List<String> getAllColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		columnNames.addAll(entityFieldToColumnMap.values());
		return columnNames;
	}

	private List<Map<String, String>> getEntityFilterKeyMap(EntityAccess entityAccess)
			throws LeapEntityServiceException {
		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();
		EntityFilterKeysMapping entityFilterKeysMapping = entityAccess.getEntityFilterKeysMapping();
		if (entityFilterKeysMapping != null) {
			List<EntityKey> entityKeys = entityFilterKeysMapping.getEntityKey();
			for (EntityKey entityKey : entityKeys) {
				Map<String, String> map = new HashMap<String, String>();
				checkAndPutInMap(map, LeapEntityServiceSolImplConstant.NAME, entityKey.getName(), true);
				// checkAndPutInMap(map,
				// LeapEntityServiceSolImplConstant.MAPPED_TO,
				// entityKey.getMappedTo(), true);
				checkAndPutInMap(map, LeapEntityServiceSolImplConstant.REQUIRED, entityKey.getRequired(), false);
				// checkAndPutInMap(map,
				// LeapEntityServiceSolImplConstant.SEQUENCE,
				// entityKey.getSequence().toString(),
				// false);
				entityKeyMapList.add(map);
			}
		}

		return entityKeyMapList;
	}

	private List<String> getEntityReadKeysMapping(EntityAccess entityAccess) {
		List<String> selectColumnList = new ArrayList<String>();
		EntityReadKeysMapping entityReadKeysMapping = entityAccess.getEntityReadKeysMapping();
		if (entityReadKeysMapping != null) {
			List<EntityKey> entityKeys = entityReadKeysMapping.getEntityKey();
			if (entityKeys != null) {
				for (EntityKey entityKey : entityKeys) {
					String fieldName = entityKey.getName();
					String columnName = entityFieldToColumnMap.get(fieldName);
					selectColumnList.add(columnName);
				}
			}
		}
		return selectColumnList;
	}

	private List<Map<String, String>> getEntityInsertKeyMap(EntityAccess entityAccess)
			throws LeapEntityServiceException {
		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();
		EntityInsertKeysMapping entityInsertKeysMapping = entityAccess.getEntityInsertKeysMapping();
		if (entityInsertKeysMapping != null) {
			List<EntityKey> entityKeys = entityInsertKeysMapping.getEntityKey();
			if (entityKeys != null) {
				for (EntityKey entityKey : entityKeys) {
					Map<String, String> map = new HashMap<String, String>();
					checkAndPutInMap(map, LeapEntityServiceSolImplConstant.NAME, entityKey.getName(), true);
					// checkAndPutInMap(map,
					// LeapEntityServiceSolImplConstant.MAPPED_TO,
					// entityKey.getMappedTo(), true);
					checkAndPutInMap(map, LeapEntityServiceSolImplConstant.REQUIRED, entityKey.getRequired(), false);
					// checkAndPutInMap(map,
					// LeapEntityServiceSolImplConstant.SEQUENCE,
					// entityKey.getSequence().toString(),
					// false);
					entityKeyMapList.add(map);
				}
			}
		}

		return entityKeyMapList;
	}

	private List<Map<String, String>> getEntityUpdateKeyMap(EntityAccess entityAccess)
			throws LeapEntityServiceException {
		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();
		EntityUpdateKeysMapping entityUpdateKeysMapping = entityAccess.getEntityUpdateKeysMapping();
		if (entityUpdateKeysMapping != null) {
			List<EntityKey> entityKeys = entityUpdateKeysMapping.getEntityKey();
			if (entityKeys != null) {
				for (EntityKey entityKey : entityKeys) {
					Map<String, String> map = new HashMap<String, String>();
					checkAndPutInMap(map, LeapEntityServiceSolImplConstant.NAME, entityKey.getName(), true);
					// checkAndPutInMap(map,
					// LeapEntityServiceSolImplConstant.MAPPED_TO,
					// entityKey.getMappedTo(), true);
					checkAndPutInMap(map, LeapEntityServiceSolImplConstant.REQUIRED, entityKey.getRequired(), false);
					// checkAndPutInMap(map,
					// LeapEntityServiceSolImplConstant.SEQUENCE,
					// entityKey.getSequence().toString(),
					// false);
					entityKeyMapList.add(map);
				}
			}
		}

		return entityKeyMapList;
	}

	private void checkAndPutInMap(Map<String, String> map, String key, String value, boolean required)
			throws LeapEntityServiceException {
		if (value != null)
			map.put(key, value);
		else {
			if (required)
				throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
						"attribute " + key + " does not exist ", 412);
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, List> getColumnNameAndValueMap(List<Map<String, String>> entityInsertkeyMapList,
			JSONObject requestJson) throws LeapEntityServiceException {
		logger.debug("{} requestJson ::{} ",LEAP_LOG_KEY,requestJson.toString());
		List<String> columnName = new ArrayList<String>();
		List<Object> columnValue = new ArrayList<Object>();
		Map<String, List> insertMap = new HashMap<String, List>();
		for (Map<String, String> filterMap : entityInsertkeyMapList) {
			String entityFieldName = filterMap.get(LeapEntityServiceSolImplConstant.NAME);
			String required = "mandatory";
			if (filterMap.containsKey(LeapEntityServiceSolImplConstant.REQUIRED)) {
				required = filterMap.get(LeapEntityServiceSolImplConstant.REQUIRED);
			}
			if (requestJson.has(entityFieldName)) {
				columnName.add(entityFieldToColumnMap.get(entityFieldName));
				columnValue.add(requestJson.get(entityFieldName));

			} else {
				if (required.equalsIgnoreCase("mandatory")) {
					throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity operation ",
							"key : " + entityFieldName + " is mandatory", 412);
				}
			}

		}
		insertMap.put("columnName", columnName);
		insertMap.put("columnValue", columnValue);
		return insertMap;
	}

	// @SuppressWarnings("rawtypes")
	// private List<Map<String, List>> getColumnNameAndValueMap(List<Map<String,
	// String>> entityInsertkeyMapList,
	// JSONArray requestJsonArray) throws LeapEntityException {
	// List<Map<String, List>> ListOfMap = new ArrayList<Map<String, List>>();
	// for (int index = 0; index < requestJsonArray.length(); index++) {
	// JSONObject requestJson = requestJsonArray.getJSONObject(index);
	// List<String> columnName = new ArrayList<String>();
	// List<Object> columnValue = new ArrayList<Object>();
	// Map<String, List> insertMap = new HashMap<String, List>();
	// for (Map<String, String> filterMap : entityInsertkeyMapList) {
	// String mappedTo =
	// filterMap.get(LeapEntityServiceSolImplConstant.MAPPED_TO);
	// String required = "mandatory";
	// if (filterMap.containsKey(LeapEntityServiceSolImplConstant.REQUIRED)) {
	// required = filterMap.get(LeapEntityServiceSolImplConstant.REQUIRED);
	// }
	// if (requestJson.has(mappedTo)) {
	// columnName.add(filterMap.get(LeapEntityServiceSolImplConstant.NAME));
	// columnValue.add(requestJson.get(mappedTo));
	//
	// } else {
	// if (required.equalsIgnoreCase("mandatory")) {
	// throw new LeapEntityException("key : " + mappedTo + " is mandatory");
	// }
	// }
	//
	// }
	// insertMap.put("columnName", columnName);
	// insertMap.put("columnValue", columnValue);
	// ListOfMap.add(insertMap);
	// }
	// return ListOfMap;
	// }

}
