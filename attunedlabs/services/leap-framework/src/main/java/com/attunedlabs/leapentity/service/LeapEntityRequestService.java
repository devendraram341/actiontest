package com.attunedlabs.leapentity.service;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.DisplayMetaData;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;
import com.attunedlabs.leapentity.LeapEntityArchivalUtility;
import com.attunedlabs.leapentity.config.jaxb.ApplyLDCConfig;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityAccessConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityDef;
import com.attunedlabs.leapentity.config.jaxb.EntityRestAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequest;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequestBody;
import com.attunedlabs.leapentity.config.jaxb.LDCProjection;
import com.attunedlabs.leapentity.config.jaxb.LDCRequestConfigs;
import com.attunedlabs.leapentity.config.jaxb.LDCResponseConfigs;
import com.attunedlabs.leapentity.config.jaxb.LDCSchema;
import com.attunedlabs.leapentity.config.jaxb.LDCTaxonomy;
import com.attunedlabs.leapentity.config.jaxb.MetadataField;
import com.attunedlabs.leapentity.config.jaxb.TransformationConfig;
import com.attunedlabs.leapentity.dao.LeapEntityServiceSqlDAOException;

/**
 * this class is perform some operation on entity request and response
 * 
 * @author Reactiveworks
 *
 */
public class LeapEntityRequestService {

	private static final Logger logger = LoggerFactory.getLogger(LeapEntityRequestService.class);

	// initialization of variables
	private Map<String, Object> validResDataEntityObject = null;

	/**
	 * this method will convert the incoming request into defaultTaxonomy and set
	 * into header
	 * 
	 * @param exchange
	 * @throws LeapEntityServiceException
	 */
	public void convertRequestIntoDefaultTaxonomy(Exchange exchange) throws LeapEntityServiceException {
		String methodName = "convertRequestIntoDefaultTaxonomy";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("{} request time :- {}", LEAP_LOG_KEY, System.currentTimeMillis());
		try {
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
					.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
			String httpMethod = (String) exchange.getIn().getHeader(LeapDataContextConstant.CAMEL_HTTP_METHOD);
			Object requestObject = LeapEntityArchivalUtility.getIntialRequestBodyFromLDC(exchange);
			logger.info("{} incoming request is :: {}", LEAP_LOG_KEY, requestObject);
			if (requestObject != null) {
				Map<String, Object> entityDataMap = getEntitiesMapFromService(leapDataContext);
				String authorizedResource = getAuthorizedServiceNameURL(leapDataContext, exchange);
				validResDataEntityObject = validateAuthorizedResourceAndGetEntityDataAsMap(entityDataMap,
						authorizedResource, httpMethod);
				leapDataContext.addContextElement(requestObject, LeapEntityServiceConstant.ENTITY_REQ_KEY,
						LeapEntityServiceConstant.ENTITY_REQ_KEY, LeapEntityServiceConstant.DEFAULT_TAXONOMY);
				requestObject = leapDataContext.applyTemplate(false, false, LeapEntityServiceConstant.DEFAULT_TAXONOMY,
						LeapEntityServiceConstant.PROJECTION_SOURCE_SWAGGER);
				if (requestObject instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) requestObject;
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					requestObject = jsonObject.get("root");
					if (requestObject instanceof JSONArray) {
						JSONArray jsonArray1 = (JSONArray) requestObject;
						requestObject = jsonArray1.get(0);
					}
				} else {
					JSONObject jsonObject = (JSONObject) requestObject;
					requestObject = jsonObject.get("root");
				}
			}
			logger.info("{} leapDefault request :: {}", LEAP_LOG_KEY, requestObject.toString());
			leapDataContext.addContextElement(requestObject, LeapEntityServiceConstant.ENTITY_REQ_KEY,
					LeapEntityServiceConstant.ENTITY_REQ_KEY, null);
		} catch (Exception e) {
			logger.error("{} Unable to proccess entity request : {}", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(), " Unable to proccess Entity request ", e.getMessage(),
					412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * this method will convert the incoming request into Taxonomy provided in the
	 * entity configuration
	 * 
	 * @param exchange
	 * @throws LeapEntityServiceException
	 */
	public void convertRequestIntoTaxonomy(Exchange exchange) throws LeapEntityServiceException {
		String methodName = "convertRequestIntoTaxonomy";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("{} request time :- ", LEAP_LOG_KEY, System.currentTimeMillis());
		try {
			String httpMethod = (String) exchange.getIn().getHeader(LeapDataContextConstant.CAMEL_HTTP_METHOD);
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
					.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
			Object requestObject = LeapEntityArchivalUtility.getIntialRequestBodyFromLDC(exchange);
			logger.info("{} incoming request is :: {}", LEAP_LOG_KEY, requestObject);
			if (requestObject != null) {
				Map<String, Object> entityDataMap = getEntitiesMapFromService(leapDataContext);
				String authorizedResource = getAuthorizedServiceNameURL(leapDataContext, exchange);
				validResDataEntityObject = validateAuthorizedResourceAndGetRestEntityDataAsMap(entityDataMap,
						authorizedResource, httpMethod);
				checkTransforamationRequiredForEntity(validResDataEntityObject, exchange);
				Object ldcConfigRequestObject = buildTaxonomyDataFromEntityConfig(validResDataEntityObject,
						leapDataContext);
				if (ldcConfigRequestObject != null) {
					if (ldcConfigRequestObject instanceof JSONArray) {
						JSONArray jsonArray = (JSONArray) ldcConfigRequestObject;
						JSONObject jsonObject = jsonArray.getJSONObject(0);
						ldcConfigRequestObject = jsonObject.get("root");
						if (ldcConfigRequestObject instanceof JSONArray) {
							JSONArray jsonArray1 = (JSONArray) ldcConfigRequestObject;
							ldcConfigRequestObject = jsonArray1.get(0);
						}
					} else {
						JSONObject jsonObject = (JSONObject) ldcConfigRequestObject;
						ldcConfigRequestObject = jsonObject.get("root");
					}
					logger.info("{} after applying the ldc configs to the incoming request :: {}", LEAP_LOG_KEY,
							ldcConfigRequestObject.toString());
					leapDataContext.addContextElement(ldcConfigRequestObject, LeapEntityServiceConstant.ENTITY_REQ_KEY,
							LeapEntityServiceConstant.ENTITY_REQ_KEY, null);
				}
			} // ..end of if condition for checking the request body is null or
				// not
		} catch (Exception e) {
			logger.error("{} Unable to proccess entity request : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(), " Unable to proccess Entity request ", e.getMessage(),
					412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * this method will convert the request based on tagName into Taxonomy provided
	 * in the entity configuration
	 * 
	 * @param tagName
	 * @param exchange
	 * @throws LeapEntityServiceException
	 */
	public void convertRequestIntoTaxonomyByTagName(String tagName, Exchange exchange)
			throws LeapEntityServiceException {
		String methodName = "convertRequestIntoTaxonomyByTagName";
		logger.debug("{} entered into the method {}, tagName={} ", LEAP_LOG_KEY, methodName, tagName);
		logger.debug("{} request time :- {}", LEAP_LOG_KEY, System.currentTimeMillis());
		try {
			String httpMethod = (String) exchange.getIn().getHeader(LeapDataContextConstant.CAMEL_HTTP_METHOD);
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
					.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
			LeapResultSet resLeapResultSet = leapDataContext.getDataByTag(tagName);
			Object requestObject = resLeapResultSet.getData();
			logger.info("{} incoming request in convertRequestIntoTaxonomy tagName ::{} ", LEAP_LOG_KEY,
					requestObject);
			if (requestObject != null) {
				Map<String, Object> entityDataMap = getEntitiesMapFromService(leapDataContext);
				String authorizedResource = getAuthorizedServiceNameURL(leapDataContext, exchange);
				validResDataEntityObject = validateAuthorizedResourceAndGetRestEntityDataAsMap(entityDataMap,
						authorizedResource, httpMethod);
				checkTransforamationRequiredForEntity(validResDataEntityObject, exchange);
				Object ldcConfigRequestObject = buildTaxonomyDataFromEntityConfig(validResDataEntityObject,
						leapDataContext);
				if (ldcConfigRequestObject != null) {
					if (ldcConfigRequestObject instanceof JSONArray) {
						JSONArray jsonArray = (JSONArray) ldcConfigRequestObject;
						JSONObject jsonObject = jsonArray.getJSONObject(0);
						ldcConfigRequestObject = jsonObject.get("root");
						if (ldcConfigRequestObject instanceof JSONArray) {
							JSONArray jsonArray1 = (JSONArray) ldcConfigRequestObject;
							ldcConfigRequestObject = jsonArray1;
						}
					} else {
						JSONObject jsonObject = (JSONObject) ldcConfigRequestObject;
						ldcConfigRequestObject = jsonObject.get("root");
					}
					logger.info("{} after applying the ldc configs to the incoming request tagName :: {}",
							LEAP_LOG_KEY, ldcConfigRequestObject.toString());
					leapDataContext.addContextElement(ldcConfigRequestObject, LeapEntityServiceConstant.ENTITY_REQ_KEY,
							LeapEntityServiceConstant.ENTITY_REQ_KEY, null);
				}
			} // ..end of if condition for checking the request body is null or
				// not
		} catch (Exception e) {
			logger.error("{} Unable to proccess entity request tagName: {}", LEAP_LOG_KEY, tagName + e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(),
					" Unable to proccess Entity request for tagName " + tagName, e.getMessage(), 412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * this method will convert the incoming response from the external service into
	 * Taxonomy provided in the entity configuration
	 * 
	 * @param exchange
	 * @throws LeapEntityServiceException
	 */
	public void convertResponseIntoTaxonomy(Exchange exchange) throws LeapEntityServiceException {
		String methodName = "convertResponseIntoTaxonomy";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("{} request time :- {}", LEAP_LOG_KEY, System.currentTimeMillis());
		EntityAccess vaildEntityAccess = null;
		EntityRestAccess vaildEntityRestAccess = null;
		String responseBody = null;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapResultSet leapResultSet = null;
		leapResultSet = leapDataContext.getDataByTag("#entity_response");
		if (leapResultSet != null) {
			responseBody = leapResultSet.getData().toString();
		}
		try {
			Object entityAccessObject = validResDataEntityObject.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
			if (entityAccessObject instanceof EntityAccess) {
				vaildEntityAccess = (EntityAccess) validResDataEntityObject
						.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
				JSONArray ldcResponseConfig = getLDCResponseConfig(vaildEntityAccess, null);
				leapDataContext.addContextElement(ldcResponseConfig, LeapEntityServiceConstant.LDC_RESPONSE_CONFIG,
						LeapEntityServiceConstant.LDC_RESPONSE_CONFIG, null);
				leapDataContext.addContextElement(responseBody, LeapEntityServiceConstant.ENTITY_RESULT_SET,
						LeapEntityServiceConstant.ENTITY_RESULT_SET, null);
			}
			if (entityAccessObject instanceof EntityRestAccess) {
				vaildEntityRestAccess = (EntityRestAccess) validResDataEntityObject
						.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
				JSONArray ldcResponseConfig = getLDCResponseConfig(null, vaildEntityRestAccess);
				leapDataContext.addContextElement(ldcResponseConfig, LeapEntityServiceConstant.LDC_RESPONSE_CONFIG,
						LeapEntityServiceConstant.LDC_RESPONSE_CONFIG, null);
				leapDataContext.addContextElement(responseBody, LeapEntityServiceConstant.ENTITY_RESULT_SET,
						LeapEntityServiceConstant.ENTITY_RESULT_SET, null);
			}

		} catch (Exception e) {
			logger.error("{} Unable to proccess entity request :{}", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapEntityServiceException(new Throwable(), " Unable to proccess Entity request ", e.getMessage(),
					412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// ..end of the method convertResponseIntoTaxonomy

	/**
	 * this method will validate the incoming request is valid or not
	 * 
	 * @param exchange
	 * @throws LeapEntityServiceException
	 * @throws LeapEntityServiceSqlDAOException
	 * @throws LeapDataContextInitialzerException
	 */
	public void executeEntity(Exchange exchange) throws LeapEntityServiceException, LeapDataContextInitialzerException {
		String methodName = "executeEntity";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		EntityAccess vaildEntityAccess = null;
		EntityRestAccess vaildEntityRestAccess = null;
		Object entityDefaultReq = null;
		Object response = null;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapResultSet leapResultSet = null;
		leapResultSet = leapDataContext.getDataByTag("#enriched_leap_initial");
		if (leapResultSet == null)
			leapResultSet = leapDataContext.getDataByTag(LeapEntityServiceConstant.ENTITY_REQ_KEY);
		if (leapResultSet != null) {
			entityDefaultReq = leapResultSet.getData();
		}
		boolean createDisplayMetaData = (Boolean) exchange.getIn()
				.getHeader(LeapEntityServiceConstant.CREATE_DISPLAY_META_DATA);

		Entity vaildEntity = (Entity) validResDataEntityObject.get(LeapEntityServiceConstant.ENTITY_KEY);
		EntityAccessConfig vaildEntityAccessConfig = (EntityAccessConfig) validResDataEntityObject
				.get(LeapEntityServiceConstant.ENTITY_ACCESSCONFIG_KEY);
		Object entityAccessObject = validResDataEntityObject.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);

		ILeapEntityService leapEntityService = LeapEntityServiceFactory
				.getInstance(vaildEntityAccessConfig.getConfigType());
		logger.debug("{} createDisplayMetaData :: {} ", LEAP_LOG_KEY, createDisplayMetaData);

		if (createDisplayMetaData) {
			List<DisplayMetaData> displayMetaDatas = createDisplayMetaDataForEntity(vaildEntity);
			leapDataContext.setDisplayMetaDataInLdc(displayMetaDatas);
		}

		if (entityAccessObject instanceof EntityAccess) {
			vaildEntityAccess = (EntityAccess) validResDataEntityObject
					.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
			response = leapEntityService.performEntityOperation(vaildEntity, vaildEntityAccessConfig, vaildEntityAccess,
					null, entityDefaultReq, exchange);
			JSONArray ldcResponseConfig = getLDCResponseConfig(vaildEntityAccess, null);
			leapDataContext.addContextElement(ldcResponseConfig, LeapEntityServiceConstant.LDC_RESPONSE_CONFIG,
					LeapEntityServiceConstant.LDC_RESPONSE_CONFIG, null);
			leapDataContext.addContextElement(response.toString(), LeapEntityServiceConstant.ENTITY_RESULT_SET,
					LeapEntityServiceConstant.ENTITY_RESULT_SET, LeapEntityServiceConstant.LEAP_DEFAULT);
		}
		if (entityAccessObject instanceof EntityRestAccess) {
			vaildEntityRestAccess = (EntityRestAccess) validResDataEntityObject
					.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
			response = leapEntityService.performEntityOperation(vaildEntity, vaildEntityAccessConfig, null,
					vaildEntityRestAccess, entityDefaultReq, exchange);
			JSONArray ldcResponseConfig = getLDCResponseConfig(null, vaildEntityRestAccess);
			leapDataContext.addContextElement(ldcResponseConfig, LeapEntityServiceConstant.LDC_RESPONSE_CONFIG,
					LeapEntityServiceConstant.LDC_RESPONSE_CONFIG, null);
			leapDataContext.addContextElement(response.toString(), LeapEntityServiceConstant.ENTITY_RESULT_SET,
					LeapEntityServiceConstant.ENTITY_RESULT_SET, null);
		}

		logger.info("{} response : {} ", LEAP_LOG_KEY, response);
		exchange.getIn().removeHeader(LeapEntityServiceConstant.ENTITY_REQUEST_HEADER_KEY);
		exchange.getIn().removeHeader(LeapEntityServiceConstant.CREATE_DISPLAY_META_DATA);
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
		String methodName = "setLDCConfigInHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Message message = exchange.getIn();
		LeapDataContext leapDataContext = (LeapDataContext) message
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		String entityLDCRespString = leapDataContext.getDataByTag(LeapEntityServiceConstant.LDC_RESPONSE_CONFIG)
				.getData().toString();
		logger.info("{} entityLDCRespString :: {} " ,LEAP_LOG_KEY, entityLDCRespString);
		boolean applyLDC = false;
		JSONArray LDCResponseConfigArray = new JSONArray(entityLDCRespString);
		if (LDCResponseConfigArray.length() != 0) {
			for (int i = 0; i < LDCResponseConfigArray.length(); i++) {
				JSONObject json = (JSONObject) LDCResponseConfigArray.get(i);
				if (json.getString(LeapEntityServiceConstant.SEQUENCE).trim().equals(sequence + "")
						&& json.getString(LeapEntityServiceConstant.APPLY_AT).equalsIgnoreCase(applyAt)) {
					JSONObject ldcJson = (JSONObject) json.get(LeapEntityServiceConstant.LDC_PROPERTIES);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.SCHEMA_REQUIRED);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.SCHEMA_FILE_NAME);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.PROJECTION_REQUIRED);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.PROJECTION_FILE_NAME);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.PROJECTION_SOURCE);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.TAXONOMY_REQUIRED);
					setValuesInHeader(message, ldcJson, LeapEntityServiceConstant.TAXONOMY_FILE_NAME);
					applyLDC = true;
					break;
				}
			}

		}
		message.setHeader(LeapEntityServiceConstant.APPLY_LDC, applyLDC);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void setValuesInHeader(Message message, JSONObject ldcJson, String key) {
		if (ldcJson.has(key)) {
			message.setHeader(key, ldcJson.get(key));
		}

	}

	/*
	 * to set the ldc configuration
	 */
	private JSONArray getLDCResponseConfig(EntityAccess entityAccess, EntityRestAccess entityRestAccess) {
		String methodName = "getLDCResponseConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONArray ldcResponseConfigArray = new JSONArray();
		boolean applyTemp = false;
		LDCResponseConfigs responseConfig = null;
		if (entityAccess != null)
			responseConfig = entityAccess.getLDCResponseConfigs();
		if (entityRestAccess != null) {
			responseConfig = entityRestAccess.getEntityRestResponse().getLDCResponseConfigs();
		}
		if (responseConfig != null) {
			ApplyLDCConfig applyLDCConfig = responseConfig.getApplyLDCConfig();
			if (applyLDCConfig != null) {
				JSONObject ldcResponseConfigJson = new JSONObject();
				String applyAt = applyLDCConfig.getApplyAt();
				String sequence = applyLDCConfig.getSequence().toString();
				ldcResponseConfigJson.put(LeapEntityServiceConstant.APPLY_AT, applyAt);
				ldcResponseConfigJson.put(LeapEntityServiceConstant.SEQUENCE, sequence);
				JSONObject ldcPropertyJson = new JSONObject();
				LDCSchema ldcSchema = applyLDCConfig.getLDCSchema();
				boolean schemaRequired = ldcSchema.isRequired();
				ldcPropertyJson.put(LeapEntityServiceConstant.SCHEMA_REQUIRED, schemaRequired);
				if (schemaRequired) {
					applyTemp = true;
					ldcPropertyJson.put(LeapEntityServiceConstant.SCHEMA_FILE_NAME, ldcSchema.getSchemaFileName());
				}
				LDCProjection ldcProjection = applyLDCConfig.getLDCProjection();
				boolean projectionRequired = ldcProjection.isRequired();
				ldcPropertyJson.put(LeapEntityServiceConstant.PROJECTION_REQUIRED, projectionRequired);
				if (projectionRequired) {
					applyTemp = true;
					ldcPropertyJson.put(LeapEntityServiceConstant.PROJECTION_FILE_NAME,
							ldcProjection.getProjectionFileName());
					ldcPropertyJson.put(LeapEntityServiceConstant.PROJECTION_SOURCE,
							ldcProjection.getProjectionSource());
				}
				LDCTaxonomy ldcTaxonomy = applyLDCConfig.getLDCTaxonomy();
				boolean taxonomyRequ = ldcTaxonomy.isRequired();
				if (taxonomyRequ) {
					applyTemp = true;
					String taxonomyFileName = ldcTaxonomy.getTaxonomyFileName();
					if (taxonomyFileName != null)
						ldcPropertyJson.put(LeapEntityServiceConstant.TAXONOMY_FILE_NAME, taxonomyFileName);
				}
				ldcPropertyJson.put(LeapEntityServiceConstant.TAXONOMY_REQUIRED, taxonomyRequ);
				ldcResponseConfigJson.put(LeapEntityServiceConstant.LDC_PROPERTIES, ldcPropertyJson);
				if (applyTemp)
					ldcResponseConfigArray.put(ldcResponseConfigJson);

			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return ldcResponseConfigArray;
	}

	/*
	 * to validate the resource is valid or not
	 */
	private Map<String, Object> validateAuthorizedResourceAndGetEntityDataAsMap(Map<String, Object> entityMap,
			String authorizedResource, String HttpMethod) throws LeapEntityServiceException {
		String methodName = "validateAuthorizedResourceAndGetEntityDataAsMap";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> vaildEntityDataMap = new HashMap<String, Object>();
		Entity entity = null;
		EntityAccess validEntityAccess = null;
		EntityAccessConfig entityAccessConfig = null;
		boolean IshttpMethodsVaild = false;
		String entityName = null;
		Iterator<Map.Entry<String, Object>> entityMapIterator = entityMap.entrySet().iterator();
		outerLoop: while (entityMapIterator.hasNext()) {
			Map.Entry<String, Object> entry = entityMapIterator.next();
			logger.trace("{} key is :: {}" ,LEAP_LOG_KEY, entry.getKey());
			entity = (Entity) entry.getValue();
			if (entity != null) {
				entityName = entity.getName();
				List<EntityAccessConfig> accessConfigs = entity.getEntityAccessConfig();
				for (EntityAccessConfig accessConfig : accessConfigs) {
					entityAccessConfig = accessConfig;
					List<EntityAccess> entityAccesses = accessConfig.getEntityAccess();
					for (EntityAccess access : entityAccesses) {
						String resourceName = entityName + "/" + access.getAuthorizedResource();
						if (resourceName.equals(authorizedResource)) {
							logger.trace("{} authorizedResource :: {}",LEAP_LOG_KEY, authorizedResource);
							logger.trace("{} resourceName :: {}" ,LEAP_LOG_KEY, resourceName);
							validEntityAccess = access;
							break outerLoop;
						}
					}
				}
			}
		}
		if (validEntityAccess != null) {
			String[] accessMethods = validEntityAccess.getAccessMethod().split(",");
			for (String accessMethod : accessMethods) {
				if (accessMethod.equalsIgnoreCase(HttpMethod)) {
					IshttpMethodsVaild = true;
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_KEY, entity);
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_ACCESS_KEY, validEntityAccess);
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_ACCESSCONFIG_KEY, entityAccessConfig);
				}
			}
			if (!IshttpMethodsVaild)
				throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity Operation ",
						"http method is invalid", 412);

		} else {
			throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity Operation ",
					" authorizedResource : " + authorizedResource + " does not exist", 412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return vaildEntityDataMap;
	}

	private List<DisplayMetaData> createDisplayMetaDataForEntity(Entity entity) {
		List<DisplayMetaData> displayMetaDatas = new ArrayList<DisplayMetaData>();
		String entityName = entity.getName();
		EntityDef entityDef = entity.getEntityDef();
		List<MetadataField> entityMetaList = entityDef.getEntityMetadata().getMetadataField();
		for (MetadataField metadataField : entityMetaList) {
			String metaDataFieldValueForString = null;
			Boolean metaDataFieldValueForBoolean = null;
			DisplayMetaData displayMetaData = new DisplayMetaData();
			displayMetaData.setEntityFieldNameRef(metadataField.getEntityFieldNameRef());
			metaDataFieldValueForString = metadataField.getMaxLength();
			if (metaDataFieldValueForString != null)
				displayMetaData.setMaxLength(metaDataFieldValueForString);
			metaDataFieldValueForString = metadataField.getMinLength();
			if (metaDataFieldValueForString != null)
				displayMetaData.setMinLength(metaDataFieldValueForString);
			metaDataFieldValueForString = metadataField.getPattern();
			if (metaDataFieldValueForString != null)
				displayMetaData.setPattern(metaDataFieldValueForString);
			metaDataFieldValueForBoolean = metadataField.isReadOnly();
			if (metaDataFieldValueForBoolean != null)
				displayMetaData.setReadOnly(metaDataFieldValueForBoolean);
			metaDataFieldValueForBoolean = metadataField.isSearchable();
			if (metaDataFieldValueForBoolean != null)
				displayMetaData.setSearchable(metaDataFieldValueForBoolean);

			displayMetaData.setI18N(entityName);
			displayMetaDatas.add(displayMetaData);

		}
		logger.trace("{} displayMetaDatas :: {}" ,LEAP_LOG_KEY, displayMetaDatas);

		return displayMetaDatas;
	}

	/*
	 * to validate the resource is valid or not
	 */
	private Map<String, Object> validateAuthorizedResourceAndGetRestEntityDataAsMap(Map<String, Object> entityMap,
			String authorizedResource, String HttpMethod) throws LeapEntityServiceException {
		String methodName = "validateAuthorizedResourceAndGetRestEntityDataAsMap";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> vaildEntityDataMap = new HashMap<String, Object>();
		Entity entity = null;
		EntityRestAccess validRestEntityAccess = null;
		EntityAccessConfig entityAccessConfig = null;
		boolean IshttpMethodsVaild = false;
		String entityName = null;
		Iterator<Map.Entry<String, Object>> entityMapIterator = entityMap.entrySet().iterator();
		outerLoop: while (entityMapIterator.hasNext()) {
			Map.Entry<String, Object> entry = entityMapIterator.next();
			logger.trace("{} key is :: {} " ,LEAP_LOG_KEY, entry.getKey());
			entity = (Entity) entry.getValue();
			if (entity != null) {
				entityName = entity.getName();
				List<EntityAccessConfig> accessConfigs = entity.getEntityAccessConfig();
				for (EntityAccessConfig accessConfig : accessConfigs) {
					entityAccessConfig = accessConfig;
					List<EntityRestAccess> entityAccesses = accessConfig.getEntityRestAccess();
					for (EntityRestAccess access : entityAccesses) {
						String resourceName = entityName + "/" + access.getAuthorizedResource();
						if (resourceName.equals(authorizedResource)) {
							logger.trace("{} authorizedResource :: {}" ,LEAP_LOG_KEY, authorizedResource);
							logger.trace("{} resourceName :: {}" ,LEAP_LOG_KEY, resourceName);
							validRestEntityAccess = access;
							break outerLoop;
						}
					}

				}
			}
		}
		if (validRestEntityAccess != null) {
			String[] accessMethods = validRestEntityAccess.getAccessMethod().split(",");
			for (String accessMethod : accessMethods) {
				if (accessMethod.equalsIgnoreCase(HttpMethod)) {
					IshttpMethodsVaild = true;
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_KEY, entity);
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_ACCESS_KEY, validRestEntityAccess);
					vaildEntityDataMap.put(LeapEntityServiceConstant.ENTITY_ACCESSCONFIG_KEY, entityAccessConfig);
				}
			}
			if (!IshttpMethodsVaild)
				throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity Operation ",
						"http method is invalid", 412);

		} else {
			throw new LeapEntityServiceException(new Throwable(), "Unable to perform Entity Operation ",
					" authorizedResource : " + authorizedResource + " does not exist", 412);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return vaildEntityDataMap;
	}// ..end of the validateAuthorizedResourceAndGetRestEntityDataAsMap

	/**
	 * This method is used to get the Entities map from current running service
	 * context
	 * 
	 * @param leapContext : {@link LeapDataContext}
	 * @return {@link Map} -> entites map
	 */
	private Map<String, Object> getEntitiesMapFromService(LeapDataContext leapContext) {
		String methodName = "getEntitiesMapFromService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceContext serviceDataContext = leapContext.getServiceDataContext();
		Map<String, Object> entityData = serviceDataContext.getCurrentLeapServiceRuntimeContext().getEntityData();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityData;
	}// ..end of the method getEntitiesMapFromService

	/**
	 * This method is used to get the Authorized name mapping with entity from URL
	 * 
	 * @param leapDataContext :: {@link LeapDataContext}
	 * @param exchange        :: {@link Exchange}
	 * @return {@link String} -> name of the authorized resource
	 */
	private String getAuthorizedServiceNameURL(LeapDataContext leapDataContext, Exchange exchange) {
		String methodName = "getAuthorizedServiceNameURL";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String serviceName = exchange.getIn().getHeader("servicetype", String.class);
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
		} else {
			authorizedResource = incomingUrl.substring(incomingUrl.lastIndexOf(serviceName + "/") + serviceNameSize,
					incomingUrl.length());
		}
		logger.trace("{} authorizedResource : {}" ,LEAP_LOG_KEY, authorizedResource);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return authorizedResource;

	}// ..end of the method getAuthorizedServiceNameURL

	/**
	 * This method is used to update the initial request body based on the ldc
	 * configs in entity config object
	 * 
	 * @param valideResDataEntityObject : {@link Map} -> authorized entity object
	 * @param leapDataContext           : {@link LeapDataContext}
	 * @return {@link Object} -> Ldc config request body
	 * @throws UnableToApplyTemplateException
	 */
	private Object buildTaxonomyDataFromEntityConfig(Map<String, Object> valideResDataEntityObject,
			LeapDataContext leapDataContext) throws UnableToApplyTemplateException {
		String methodName = "buildTaxonomyDataFromEntityConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object ldcConfigRequestBody = null;
		String schemaFileName = null;
		String projectionFileName = null;
		String projectionSource = null;
		String taxonomyFileName = null;
		EntityRestAccess validRestEntity = (EntityRestAccess) valideResDataEntityObject
				.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
		EntityRestRequestBody entityRequestBody = validRestEntity.getEntityRestRequest().getEntityRestRequestBody();
		LDCRequestConfigs ldcRequestConfigs = entityRequestBody.getLDCRequestConfigs();
		if (ldcRequestConfigs != null) {
			ApplyLDCConfig applyLdcConfig = ldcRequestConfigs.getApplyLDCConfig();
			boolean schemaflag = applyLdcConfig.getLDCSchema().isRequired();
			if (schemaflag)
				schemaFileName = applyLdcConfig.getLDCSchema().getSchemaFileName();

			boolean projectionflag = applyLdcConfig.getLDCProjection().isRequired();
			if (projectionflag) {
				projectionFileName = applyLdcConfig.getLDCProjection().getProjectionFileName();
				projectionSource = applyLdcConfig.getLDCProjection().getProjectionSource();
			}

			boolean taxonomyflag = applyLdcConfig.getLDCTaxonomy().isRequired();
			if (taxonomyflag)
				taxonomyFileName = applyLdcConfig.getLDCTaxonomy().getTaxonomyFileName();

			ldcConfigRequestBody = leapDataContext.applyTemplate(schemaflag, projectionflag, schemaFileName,
					projectionFileName, projectionSource, taxonomyFileName);

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return ldcConfigRequestBody;
	}// ..end of the method getTaxonomyFromEntityConfig

	/**
	 * This method is used to checked for entity transformation is required or not
	 * 
	 * @param validResDataEntityObject
	 * @param exchange
	 */
	private void checkTransforamationRequiredForEntity(Map<String, Object> validResDataEntityObject,
			Exchange exchange) {
		boolean requiredFlag = false;
		EntityRestAccess validRestEntity = (EntityRestAccess) validResDataEntityObject
				.get(LeapEntityServiceConstant.ENTITY_ACCESS_KEY);
		EntityRestRequest entityRestRequest = validRestEntity.getEntityRestRequest();
		String contentType = entityRestRequest.getContentType();
		EntityRestRequestBody entityRequestBody = entityRestRequest.getEntityRestRequestBody();
		if (entityRequestBody.getTransformationConfig() != null) {
			TransformationConfig transformationConfig = entityRequestBody.getTransformationConfig();
			requiredFlag = transformationConfig.isRequired();
			String fileName = transformationConfig.getFileName();
			exchange.getIn().setHeader(LeapEntityServiceConstant.IS_TRANSFORMATION_FLAG, requiredFlag);
			exchange.getIn().setHeader(LeapEntityServiceConstant.TRANSFORMATION_FILE_NAME, fileName);
		}
		exchange.getIn().setHeader(LeapEntityServiceConstant.IS_TRANSFORMATION_FLAG, requiredFlag);
		exchange.getIn().setHeader(LeapEntityServiceConstant.CONTENT_TYPE, contentType);
	}// ..end of the method checkTransforamationRequiredForEntity
}
