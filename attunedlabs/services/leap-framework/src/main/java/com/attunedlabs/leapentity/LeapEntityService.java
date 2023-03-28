package com.attunedlabs.leapentity;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.attunedlabs.eventframework.abstractbean.TableDoesNotExistException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leapentity.autoIncrement.ILeapEntityAutoIncrement;
import com.attunedlabs.leapentity.autoIncrement.LeapEntityAutoIncrementFactoryBean;

public class LeapEntityService {

	private static final String AUTHORIZED_RESOURCE = "authorizedResource";
	private static final String ENTITY_ACCESS = "EntityAccess";
	private static final String ACCESS_METHOD = "accessMethod";
	private static final String ACCESS_TYPE = "accessType";
	private static final String ENTITY_FILTER_KEYS_MAPPING = "EntityFilterKeysMapping";
	private static final String ENTITY_INSERT_KEYS_MAPPING = "EntityInsertKeysMapping";
	private static final String ENTITY_READ_KEYS_MAPPING = "EntityReadKeysMapping";
	private static final String ENTITY_UPDATE_KEYS_MAPPING = "EntityUpdateKeysMapping";
	private static final String MAPPED_TO = "mappedTo";
	private static final String NAME = "name";
	private static final String ENTITY_COLUMN = "EntityColumn";
	private static final String ENTITY_COLUMNS = "EntityColumns";
	private static final String DELETE = "delete";
	private static final String UPDATE = "update";
	private static final String CREATE = "create";
	private static final String READ = "read";
	private static final String ENTITY_NAME = "entityName";
	private static final String AUTO_CREATE = "autoCreate";
	private static final String LDC_TAXONOMY = "LDCTaxonomy";
	private static final String LDC_PROJECTION = "LDCProjection";
	private static final String REQUIRED = "required";
	private static final String LDC_SCHEMA = "LDCSchema";
	private static final String TAXONOMY_REQUIRED = "taxonomyRequired";
	private static final String PROJECTION_SOURCE = "projectionSource";
	private static final String PROJECTION_FILE_NAME = "projectionFileName";
	private static final String PROJECTION_REQUIRED = "projectionRequired";
	private static final String SCHEMA_FILE_NAME = "schemaFileName";
	private static final String SCHEMA_REQUIRED = "schemaRequired";
	private static final String LDC_PROPERTIES = "ldcProperties";
	private static final String APPLY_AT = "applyAt";
	private static final String SEQUENCE = "sequence";
	private static final String LEAP_DEFAULT = "LeapDefault";
	private static final String ENTITY_RESULT_SET = "EntityResultSet";
	private static final String LDC_RESPONSE_CONFIG = "LDCResponseConfig";
	private static final String DATA = "data";
	private static final String API_VERSION = "apiVersion";
	private static final String CONTEXT = "context";
	private static final String LANG = "lang";
	static Logger logger = LoggerFactory.getLogger(LeapEntityService.class);
	private final static String DATA_SERVICE_XML = "leapDataServices.xml";
	private final static String AUTO_INCREMENT_COLUMN = "autoIncrementColumn";
	LeapEntityDAO archivalDAO = new LeapEntityDAO();
	LeapEntityArchivalUtility utility = new LeapEntityArchivalUtility();

	/**
	 * it is the entry point for entity .
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void executeEntity(Exchange exchange) throws Exception {
		String methodName = "executeEntity";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject requestJson = null;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapDataElement requestContextElement = leapDataContext
				.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		LeapData leapData = requestContextElement.getData();
		if (leapData == null) {
			requestJson = new JSONObject();
		} else {
			LeapResultSet items = leapData.getItems();
			Object data = items.getData();
			if (data != null) {
				requestJson = new JSONObject(data.toString());
			} else {
				throw new LeapEntityException("request is empty");
			}
		}
		String serviceName = leapDataContext.getServiceDataContext().getRunningContextServiceName();
		int serviceNameSize = serviceName.length() + 1;
		String incomingUrl = exchange.getIn().getHeader(Exchange.HTTP_URI).toString();
		String authorizedResource = null;
		String httpMethod = (String) exchange.getIn().getHeader(LeapDataContextConstant.CAMEL_HTTP_METHOD);
		if (httpMethod.equalsIgnoreCase("get")) {
			if (incomingUrl.contains("?"))
				authorizedResource = incomingUrl.substring(incomingUrl.lastIndexOf(serviceName + "/") + serviceNameSize,
						incomingUrl.indexOf("?"));
			else
				authorizedResource = incomingUrl.substring(incomingUrl.lastIndexOf(serviceName + "/") + serviceNameSize,
						incomingUrl.length());
			if (requestJson.has(DATA))
				requestJson = requestJson.getJSONArray(DATA).getJSONObject(0);
		} else {
			authorizedResource = incomingUrl.substring(incomingUrl.lastIndexOf(serviceName + "/") + serviceNameSize,
					incomingUrl.length());
		}
		if (requestJson.has(API_VERSION) && requestJson.has(CONTEXT) && requestJson.has(LANG)) {
			requestJson = requestJson.getJSONObject(DATA);
		}
		logger.debug("{} authorizedResource : {} " ,LEAP_LOG_KEY, authorizedResource);
		logger.info("{} request is : {} " ,LEAP_LOG_KEY, requestJson.toString());
		String featureGroup = leapDataContext.getServiceDataContext().getFeatureGroup();
		String featureName = leapDataContext.getServiceDataContext().getFeatureName();
		Document entityDoc = LeapEntityArchivalUtility
				.readEntityXmlFile(featureGroup + "_" + featureName + "_" + DATA_SERVICE_XML);
		Node entityAccessNode = getEntityAccessNode(entityDoc, authorizedResource);
		String accessType = null;
		if (entityAccessNode != null)
			accessType = validateAgaintsEntityXml(entityAccessNode, httpMethod);
		else
			throw new LeapEntityException("authorizedResource : " + authorizedResource + " does not exist");
		logger.trace("{} accessType is : {}" ,LEAP_LOG_KEY, accessType);
		JSONObject response = createQueryAndExcecute(entityAccessNode, accessType, requestJson, exchange);
		logger.info("{} response is: {}", LEAP_LOG_KEY, response);
		leapDataContext.addContextElement(response, ENTITY_RESULT_SET, ENTITY_RESULT_SET, LEAP_DEFAULT);
		JSONArray ldcResponseConfig = getLDCResponseConfig(entityAccessNode);
		logger.info("{} ldc Response Configuration is : {}",LEAP_LOG_KEY, ldcResponseConfig);
		exchange.getIn().setHeader(LDC_RESPONSE_CONFIG, ldcResponseConfig);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * set the ldc properties like schemaFileName, projectionFile etc in the
	 * exchange header.
	 * 
	 * @param applyAt
	 * @param sequence
	 * @param exchange
	 */
	public void setLDCConfigInHeader(String applyAt, int sequence, Exchange exchange) {
		Message message = exchange.getIn();
		JSONArray LDCResponseConfigArray = (JSONArray) message.getHeader(LDC_RESPONSE_CONFIG);
		if (LDCResponseConfigArray.length() == 0) {
			message.setHeader(SCHEMA_REQUIRED, false);
			message.setHeader(PROJECTION_REQUIRED, false);
			message.setHeader(TAXONOMY_REQUIRED, false);
		}
		for (int i = 0; i < LDCResponseConfigArray.length(); i++) {
			JSONObject json = (JSONObject) LDCResponseConfigArray.get(i);
			if (json.getString(SEQUENCE).trim().equals(sequence + "")
					&& json.getString(APPLY_AT).equalsIgnoreCase(applyAt)) {
				JSONObject ldcJson = (JSONObject) json.get(LDC_PROPERTIES);
				setValuesInHeader(message, ldcJson, SCHEMA_REQUIRED);
				setValuesInHeader(message, ldcJson, SCHEMA_FILE_NAME);
				setValuesInHeader(message, ldcJson, PROJECTION_REQUIRED);
				setValuesInHeader(message, ldcJson, PROJECTION_FILE_NAME);
				setValuesInHeader(message, ldcJson, PROJECTION_SOURCE);
				setValuesInHeader(message, ldcJson, TAXONOMY_REQUIRED);
				break;
			}

		}

	}

	private void setValuesInHeader(Message message, JSONObject ldcJson, String key) {
		if (ldcJson.has(key)) {
			message.setHeader(key, ldcJson.get(key));
		}

	}

	private JSONArray getLDCResponseConfig(Node entityAccessNode) {

		JSONArray ldcResponseConfigArray = new JSONArray();
		Element entityAccessElement = (Element) entityAccessNode;
		NodeList ldcResponseNodeList = entityAccessElement.getElementsByTagName(LDC_RESPONSE_CONFIG);
		if (ldcResponseNodeList != null) {
			Node entityKeyMappingNode = ldcResponseNodeList.item(0);
			if (entityKeyMappingNode != null) {
				NodeList childNodes = entityKeyMappingNode.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					JSONObject ldcResponseConfigJson = new JSONObject();
					Node childNode = childNodes.item(i);
					NamedNodeMap attributes = childNode.getAttributes();
					if (attributes != null) {
						String applyAt = attributes.getNamedItem(APPLY_AT).getNodeValue();
						String sequence = attributes.getNamedItem(SEQUENCE).getNodeValue();
						ldcResponseConfigJson.put(APPLY_AT, applyAt);
						ldcResponseConfigJson.put(SEQUENCE, sequence);
						JSONObject ldcPropertyJson = new JSONObject();
						Element applyLdcConfigELement = (Element) childNode;
						Node lDCSchemaNode = applyLdcConfigELement.getElementsByTagName(LDC_SCHEMA).item(0);
						String schemaRequired = getAttributeValue(lDCSchemaNode, REQUIRED);
						if (schemaRequired != null) {
							ldcPropertyJson.put(SCHEMA_REQUIRED, Boolean.valueOf(schemaRequired));
							if (schemaRequired.equalsIgnoreCase("true")) {
								String schemaFileName = getAttributeValue(lDCSchemaNode, SCHEMA_FILE_NAME);
								ldcPropertyJson.put(SCHEMA_FILE_NAME, schemaFileName);
							}
						}
						Node lDCProjectionNode = applyLdcConfigELement.getElementsByTagName(LDC_PROJECTION).item(0);
						String projectionRequired = getAttributeValue(lDCProjectionNode, REQUIRED);
						if (projectionRequired != null) {
							ldcPropertyJson.put(PROJECTION_REQUIRED, Boolean.valueOf(projectionRequired));
							if (projectionRequired.equalsIgnoreCase("true")) {
								String projectionFileName = getAttributeValue(lDCProjectionNode, PROJECTION_FILE_NAME);
								String projectionSource = getAttributeValue(lDCProjectionNode, PROJECTION_SOURCE);
								ldcPropertyJson.put(PROJECTION_FILE_NAME, projectionFileName);
								ldcPropertyJson.put(PROJECTION_SOURCE, projectionSource);
							}
						}
						Node lDCTaxonomyNode = applyLdcConfigELement.getElementsByTagName(LDC_TAXONOMY).item(0);
						String taxonomyRequired = getAttributeValue(lDCTaxonomyNode, REQUIRED);
						ldcPropertyJson.put(TAXONOMY_REQUIRED, Boolean.valueOf(taxonomyRequired));
						ldcResponseConfigJson.put(LDC_PROPERTIES, ldcPropertyJson);
						ldcResponseConfigArray.put(ldcResponseConfigJson);

					}
				}
			}
		}
		return ldcResponseConfigArray;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JSONObject createQueryAndExcecute(Node entityAccessNode, String accessType, JSONObject requestJson,
			Exchange exchange) throws LeapEntityException, LeapEntityDAOException {
		String methodName = "createQueryAndExcecute";
		logger.debug("{} entered into the method {}, AccessType={}", LEAP_LOG_KEY, methodName,accessType);
		try {
			Node parentNode = entityAccessNode.getParentNode().getParentNode();
			String entityName = getAttributeValue(parentNode, ENTITY_NAME);
			boolean autoCreate = false;
			String autoCreateString = getAttributeValue(parentNode, AUTO_CREATE);
			String autoIncrementColumn = getAttributeValue(parentNode, AUTO_INCREMENT_COLUMN);
			if (autoIncrementColumn == null)
				autoIncrementColumn = "";
			if (autoCreateString != null) {
				autoCreate = Boolean.parseBoolean(autoCreateString.trim());
			}
			logger.trace("{} entityName is : {} " ,LEAP_LOG_KEY, entityName);
			logger.trace("{} autoCreate is : {} " ,LEAP_LOG_KEY, autoCreate);
			logger.trace("{} autoIncrementColumn is : {}" ,LEAP_LOG_KEY, autoIncrementColumn);
			switch (accessType.toLowerCase()) {
			case READ:
				List<Map<String, String>> entityFilterkeyMapList = getEntityFilterKeyMap(entityAccessNode);
				logger.debug("{} entitykeyMapList is : {} " ,LEAP_LOG_KEY, entityFilterkeyMapList);
				List<String> selectColumnsList = getEntityReadKeysMapping(entityAccessNode);
				logger.trace("{} entitykeyMapList is : {}" ,LEAP_LOG_KEY, selectColumnsList);
				if (selectColumnsList.isEmpty()) {
					selectColumnsList = getAllColumnNames(parentNode);
				}
				Map<String, Object> whereClauseMap = getWhereClauseMap(entityFilterkeyMapList, requestJson);
				logger.trace("{} whereClauseMap is : {}",LEAP_LOG_KEY, whereClauseMap);
				JSONObject exceuteReadOperation = archivalDAO.exceuteReadOperation(entityName, selectColumnsList, whereClauseMap, exchange);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return exceuteReadOperation;
			case CREATE:
				List<Map<String, String>> entityInsertkeyMapList = getEntityInsertKeyMap(entityAccessNode);
				logger.trace("{} entityInsertkeyMapList is : {}" ,LEAP_LOG_KEY, entityInsertkeyMapList);
				if (entityInsertkeyMapList.isEmpty()) {
					throw new LeapEntityException("EntityInsertKeysMapping is required in configuration");
				}
				Map<String, List> insertMap = getColumnNameAndValueMap(entityInsertkeyMapList, requestJson);
				logger.debug("{} insertMap is : {}" ,LEAP_LOG_KEY, insertMap);
				List columnNameList = insertMap.get("columnName");
				List columnValueList = insertMap.get("columnValue");
				if (!autoIncrementColumn.isEmpty()) {
					List<Map<String, Object>> tabledata = getTableMetaData(entityName, parentNode);
					String dataTypeValue = "";
					String autoIncrementType = "";
					String autoIncrementValue = "";
					for (Map<String, Object> columnData : tabledata) {
						String columnName = (String) columnData.get("columnName");
						if (columnName.equalsIgnoreCase(autoIncrementColumn)) {
							autoIncrementColumn = columnName;
							List<Map<String, Object>> attributeList = (List<Map<String, Object>>) columnData
									.get("attributeList");
							for (Map<String, Object> attributemap : attributeList) {
								String attributeName = attributemap.get("attributeName").toString();

								if (attributeName.equalsIgnoreCase("datatype")) {
									dataTypeValue = attributemap.get("value").toString();
									if (dataTypeValue.equalsIgnoreCase("varchar")
											|| dataTypeValue.equalsIgnoreCase("text")) {
										dataTypeValue = "string";
									} else {
										dataTypeValue = "long";
									}
								}
								if (attributeName.equalsIgnoreCase("autoIncrement")) {
									autoIncrementType = attributemap.get("type").toString();
									autoIncrementValue = attributemap.get("value").toString();
								}
							}

						}
					}
					logger.debug("{} dataTypeValue :: {}" ,LEAP_LOG_KEY, dataTypeValue);

					ILeapEntityAutoIncrement iLeapEntityAutoIncrement = LeapEntityAutoIncrementFactoryBean
							.getInstance(autoIncrementType, autoIncrementValue);
					if (iLeapEntityAutoIncrement != null) {
						Object autoIncrementColumnValue = iLeapEntityAutoIncrement.getAutoIncrementValue(dataTypeValue);
						logger.trace("columnNameList ::" + columnNameList);
						logger.trace(" autoIncrementColumn ::" + autoIncrementColumn);
						if (!columnNameList.contains(autoIncrementColumn)) {
							columnNameList.add(autoIncrementColumn);
							columnValueList.add(autoIncrementColumnValue);
						}
					}
				}
				try {
					archivalDAO.exceuteInsertOperation(entityName, columnNameList, columnValueList, exchange,
							autoIncrementColumn);
				} catch (TableDoesNotExistException e) {

					if (autoCreate) {
						autoCreateTable(entityName, parentNode, exchange);
						archivalDAO.exceuteInsertOperation(entityName, insertMap.get("columnName"),
								insertMap.get("columnValue"), exchange, autoIncrementColumn);
					} else {
						throw e;
					}
				}
				JSONObject insertResponseJson = new JSONObject();
				insertResponseJson.put("success", true);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return insertResponseJson;

			case UPDATE:
				List<Map<String, String>> entityFilterkeyUpdateMapList = getEntityFilterKeyMap(entityAccessNode);
				logger.debug("entityFilterkeyUpdateMapList is : " + entityFilterkeyUpdateMapList);
				List<Map<String, String>> entityUpdatekeyMapList = getEntityUpdateKeyMap(entityAccessNode);
				logger.trace("entityUpdatekeyMapList is :  " + entityUpdatekeyMapList.toString());
				Map<String, List> updateMap = getColumnNameAndValueMap(entityUpdatekeyMapList, requestJson);
				Map<String, Object> updateWhereClauseMap = getWhereClauseMap(entityFilterkeyUpdateMapList, requestJson);
				archivalDAO.executeUpdateOperation(entityName, updateWhereClauseMap, updateMap, exchange);
				JSONObject updateResponseJson = new JSONObject();
				updateResponseJson.put("success", true);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return updateResponseJson;

			case DELETE:
				List<Map<String, String>> entityFilterkeydeleteMapList = getEntityFilterKeyMap(entityAccessNode);
				logger.debug("entityFilterkeydeleteMapList is : " + entityFilterkeydeleteMapList);
				Map<String, Object> deleteWhereClauseMap = getWhereClauseMap(entityFilterkeydeleteMapList, requestJson);
				logger.trace("whereClauseMap is : " + deleteWhereClauseMap);
				int rowsDelete = archivalDAO.exceuteDeleteOperation(entityName, deleteWhereClauseMap, exchange);
				JSONObject deleteResponseJson = new JSONObject();
				deleteResponseJson.put("success", true);
				deleteResponseJson.put("noOfRowsDeleted", rowsDelete);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return deleteResponseJson;
			default:
				throw new LeapEntityException("accessType " + accessType + " is not valid");
			}

		} catch (LeapEntityException e) {
			e.printStackTrace();
			throw e;
		} catch (LeapEntityDAOException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapEntityException(" Unable to execute Entity " + e.getMessage());
		}
		
	}

	private void autoCreateTable(String entityName, Node parentNode, Exchange exchange) throws Exception {
		String methodName = "autoCreateTable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<Map<String, Object>> metaDetaList = getTableMetaData(entityName, parentNode);
		logger.debug("{} metaDetaList :: {}",LEAP_LOG_KEY, metaDetaList);
		archivalDAO.createTable(entityName, metaDetaList, exchange);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private List<Map<String, Object>> getTableMetaData(String entityName, Node parentNode) throws LeapEntityException {
		String methodName = "getTableMetaData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		Element element = (Element) parentNode;
		List<Map<String, Object>> metaDetaList = new ArrayList<Map<String, Object>>();

		NodeList entityColumnsNodeList = element.getElementsByTagName(ENTITY_COLUMNS);

		if (entityColumnsNodeList != null) {
			Node entityColumnsNode = entityColumnsNodeList.item(0);
			Element entityColumnElement = (Element) entityColumnsNode;
			NodeList entityColumnNodeList = entityColumnElement.getElementsByTagName(ENTITY_COLUMN);
			if (entityColumnNodeList != null)
				for (int i = 0; i < entityColumnNodeList.getLength(); i++) {
					Map<String, Object> entityColumn = new HashMap<String, Object>();
					Node entityColumnNode = entityColumnNodeList.item(i);
					if (entityColumnNode != null) {
						logger.trace("{} entityColumnNode name : {}" ,LEAP_LOG_KEY, entityColumnNode.getNodeName());
						String columnName = getAttributeValue(entityColumnNode, NAME);
						checkAndPutInMap(entityColumn, "columnName", columnName, true);
						Element entityColumnNodeElement = (Element) entityColumnNode;
						NodeList attributeNodeList = entityColumnNodeElement.getElementsByTagName("Attribute");
						List<Map<String, Object>> attributeList = new ArrayList<Map<String, Object>>();
						for (int j = 0; j < attributeNodeList.getLength(); j++) {
							Map<String, Object> attributeMap = new HashMap<String, Object>();
							Node attributeNode = attributeNodeList.item(j);
							if (attributeNode != null) {
								String attributeName = getAttributeValue(attributeNode, NAME);
								checkAndPutInMap(attributeMap, "attributeName", attributeName, true);
								String value = getAttributeValue(attributeNode, "value");
								checkAndPutInMap(attributeMap, "value", value, true);
								String attributeDatatype = getAttributeValue(attributeNode, "attributeDatatype");
								checkAndPutInMap(attributeMap, "attributeDatatype", attributeDatatype, true);
								String size = getAttributeValue(attributeNode, "size");
								checkAndPutInMap(attributeMap, "size", size, false);
								String type = getAttributeValue(attributeNode, "type");
								checkAndPutInMap(attributeMap, "type", type, false);
								attributeList.add(attributeMap);
							}
						}

						checkAndPutInMap(entityColumn, "attributeList", attributeList, true);
					}
					metaDetaList.add(entityColumn);
				}
		}
		logger.debug("{} metaDetaList is : {}", LEAP_LOG_KEY, metaDetaList);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return metaDetaList;
	}

	private List<String> getAllColumnNames(Node node) {
		List<String> columnNames = new ArrayList<String>();
		Element element = (Element) node;
		NodeList entityColumnNodeList = element.getElementsByTagName(ENTITY_COLUMN);
		if (entityColumnNodeList != null) {
			for (int i = 0; i < entityColumnNodeList.getLength(); i++) {
				Node item = entityColumnNodeList.item(i);
				String columnName = getAttributeValue(item, NAME);
				columnNames.add(columnName);
			}
		}
		logger.debug("{} all column names : {}" ,LEAP_LOG_KEY, columnNames);
		return columnNames;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, List> getColumnNameAndValueMap(List<Map<String, String>> entityInsertkeyMapList,
			JSONObject requestJson) throws LeapEntityException {
		List<String> columnName = new ArrayList<String>();
		List<Object> columnValue = new ArrayList<Object>();
		Map<String, List> insertMap = new HashMap<String, List>();
		for (Map<String, String> filterMap : entityInsertkeyMapList) {
			String mappedTo = filterMap.get(MAPPED_TO);
			String required = "mandatory";
			if (filterMap.containsKey(REQUIRED)) {
				required = filterMap.get(REQUIRED);
			}
			if (requestJson.has(mappedTo)) {
				columnName.add(filterMap.get(NAME));
				columnValue.add(requestJson.get(mappedTo));

			} else {
				if (required.equalsIgnoreCase("mandatory")) {
					throw new LeapEntityException("key : " + mappedTo + " is mandatory");
				}
			}

		}
		insertMap.put("columnName", columnName);
		insertMap.put("columnValue", columnValue);
		return insertMap;
	}

	private Map<String, Object> getWhereClauseMap(List<Map<String, String>> entityInsertkeyMapList,
			JSONObject requestJson) throws LeapEntityException {
		List<String> columnName = new ArrayList<String>();
		List<Object> columnValue = new ArrayList<Object>();
		Map<String, Object> insertMap = new HashMap<String, Object>();
		String predicateString = "";
		for (Map<String, String> filterMap : entityInsertkeyMapList) {
			String mappedTo = filterMap.get(MAPPED_TO);
			String required = "mandatory";
			if (filterMap.containsKey(REQUIRED)) {
				required = filterMap.get(REQUIRED);
			}
			if (requestJson.has(mappedTo)) {
				columnName.add(filterMap.get(NAME));
				columnValue.add(requestJson.get(mappedTo));
				predicateString = predicateString + filterMap.get(NAME) + "=? And ";

			} else {
				if (required.equalsIgnoreCase("mandatory")) {
					throw new LeapEntityException("key : " + mappedTo + " is mandatory");
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

	private String getAttributeValue(Node parentNode, String attributeName) {
		NamedNodeMap attributes = parentNode.getAttributes();
		if (attributes != null) {
			Node namedItem = attributes.getNamedItem(attributeName);
			if (namedItem != null) {
				return namedItem.getNodeValue();
			}
		}
		return null;
	}

	private List<String> getEntityReadKeysMapping(Node entityAccessNode) {

		List<String> selectColumnList = new ArrayList<String>();
		Element entityAccessElement = (Element) entityAccessNode;
		NodeList EntityReadKeysMappingNodeList = entityAccessElement.getElementsByTagName(ENTITY_READ_KEYS_MAPPING);
		if (EntityReadKeysMappingNodeList != null) {
			Node EntityReadKeysMappingNode = EntityReadKeysMappingNodeList.item(0);
			if (EntityReadKeysMappingNode != null) {
				NodeList childNodes = EntityReadKeysMappingNode.getChildNodes();
				if (childNodes != null) {
					for (int i = 0; i < childNodes.getLength(); i++) {
						Node node = childNodes.item(i);
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null) {
							Node namedItem = attributes.getNamedItem(NAME);
							if (namedItem != null) {
								selectColumnList.add(namedItem.getNodeValue());
							}
						}

					}

				}

			}
		}
		return selectColumnList;

	}

	private List<Map<String, String>> getEntityFilterKeyMap(Node entityAccessNode) throws LeapEntityException {

		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();

		Element entityAccessElement = (Element) entityAccessNode;
		NodeList elementsByTagName = entityAccessElement.getElementsByTagName(ENTITY_FILTER_KEYS_MAPPING);
		if (elementsByTagName != null) {
			Node entityKeyMappingNode = elementsByTagName.item(0);

			if (entityKeyMappingNode != null) {
				NodeList childNodes = entityKeyMappingNode.getChildNodes();
				if (childNodes != null) {

					for (int i = 0; i < childNodes.getLength(); i++) {
						Node node = childNodes.item(i);
						Map<String, String> map = new HashMap<>();
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null) {
							checkAndPutInMap(map, NAME, attributes.getNamedItem(NAME), true);
							checkAndPutInMap(map, SEQUENCE, attributes.getNamedItem(SEQUENCE), false);
							checkAndPutInMap(map, MAPPED_TO, attributes.getNamedItem(MAPPED_TO), true);
							checkAndPutInMap(map, REQUIRED, attributes.getNamedItem(REQUIRED), false);
							entityKeyMapList.add(map);
						}

					}
				}
			}
		}

		return entityKeyMapList;

	}

	private List<Map<String, String>> getEntityInsertKeyMap(Node entityAccessNode) throws LeapEntityException {

		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();

		Element entityAccessElement = (Element) entityAccessNode;
		NodeList elementsByTagName = entityAccessElement.getElementsByTagName(ENTITY_INSERT_KEYS_MAPPING);
		if (elementsByTagName != null) {
			Node entityKeyMappingNode = elementsByTagName.item(0);

			if (entityKeyMappingNode != null) {
				NodeList childNodes = entityKeyMappingNode.getChildNodes();
				if (childNodes != null) {

					for (int i = 0; i < childNodes.getLength(); i++) {
						Node node = childNodes.item(i);
						Map<String, String> map = new HashMap<>();
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null) {
							checkAndPutInMap(map, NAME, attributes.getNamedItem(NAME), true);
							checkAndPutInMap(map, SEQUENCE, attributes.getNamedItem(SEQUENCE), false);
							checkAndPutInMap(map, MAPPED_TO, attributes.getNamedItem(MAPPED_TO), true);
							checkAndPutInMap(map, REQUIRED, attributes.getNamedItem(REQUIRED), false);
							entityKeyMapList.add(map);
						}

					}
				}
			}
		}

		return entityKeyMapList;

	}

	private List<Map<String, String>> getEntityUpdateKeyMap(Node entityAccessNode) throws LeapEntityException {
		List<Map<String, String>> entityKeyMapList = new ArrayList<Map<String, String>>();

		Element entityAccessElement = (Element) entityAccessNode;
		NodeList elementsByTagName = entityAccessElement.getElementsByTagName(ENTITY_UPDATE_KEYS_MAPPING);
		if (elementsByTagName != null) {
			Node entityKeyMappingNode = elementsByTagName.item(0);

			if (entityKeyMappingNode != null) {
				NodeList childNodes = entityKeyMappingNode.getChildNodes();
				if (childNodes != null) {

					for (int i = 0; i < childNodes.getLength(); i++) {
						Node node = childNodes.item(i);
						Map<String, String> map = new HashMap<>();
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null) {
							checkAndPutInMap(map, NAME, attributes.getNamedItem(NAME), true);
							checkAndPutInMap(map, SEQUENCE, attributes.getNamedItem(SEQUENCE), false);
							checkAndPutInMap(map, MAPPED_TO, attributes.getNamedItem(MAPPED_TO), true);
							checkAndPutInMap(map, REQUIRED, attributes.getNamedItem(REQUIRED), false);
							entityKeyMapList.add(map);
						}

					}
				}
			}
		}

		return entityKeyMapList;
	}

	private void checkAndPutInMap(Map<String, String> map, String key, Node value, boolean required)
			throws LeapEntityException {

		if (value != null)
			map.put(key, value.getNodeValue());
		else {
			if (required)
				throw new LeapEntityException("attribute " + key + " does not exist ");
		}

	}

	private void checkAndPutInMap(Map<String, Object> map, String key, Object value, boolean required)
			throws LeapEntityException {

		if (value != null)
			map.put(key, value);
		else {
			if (required)
				throw new LeapEntityException("attribute " + key + " does not exist ");
		}

	}

	private String validateAgaintsEntityXml(Node entityAccessNode, String httpMethod) throws LeapEntityException {
		String methodName = "validateAgaintsEntityXml";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		Element entityAccessElement = (Element) entityAccessNode;
		String accessType = entityAccessElement.getAttribute(ACCESS_TYPE);
		logger.debug("{} accessType is : {}" ,LEAP_LOG_KEY, accessType);

		String accessMethod = entityAccessElement.getAttribute(ACCESS_METHOD);
		logger.debug("{} accessMethod is : {}" ,LEAP_LOG_KEY, accessMethod);
		if (!accessMethod.toLowerCase().contains(httpMethod.toLowerCase())) {
			throw new LeapEntityException("http method is  invalid");
		}
		if (accessType == null) {
			throw new LeapEntityException("accessType  is  not defined ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return accessType;

	}

	private Node getEntityAccessNode(Document entityDoc, String key) {
		NodeList nodeList = entityDoc.getElementsByTagName(ENTITY_ACCESS);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);
			String authorizedResource = e.getAttribute(AUTHORIZED_RESOURCE);
			if (authorizedResource.equalsIgnoreCase(key)) {
				return nodeList.item(i);
			}

		}
		return null;
	}

}
