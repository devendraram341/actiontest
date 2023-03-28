package com.attunedlabs.leap.util;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.API_VERSION;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.DATA;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATUREGROUP;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.IMPLEMENTATION_NAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.ITEMS;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.KIND_VAL;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.TOTAL_ITEMS;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.UPDATED;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.VENDOR;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.DataContextPropertiesImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLParserConfiguration;
import org.json.XMLXsiTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNode;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapConfigUtilException;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContexts;
import com.attunedlabs.eventframework.abstractbean.util.CassandraUtil;
import com.attunedlabs.eventframework.config.EventRequestContext;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leap.context.generic.LeapGenericConstant;
import com.attunedlabs.leap.header.initializer.JsonParserException;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyConfigurationUnit;
import com.attunedlabs.policy.config.PolicyEvaluationConfigurationUnit;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.impl.PolicyConfigurationService;
import com.google.common.reflect.TypeToken;

public class LeapConfigurationUtil {

	private static final Logger logger = LoggerFactory.getLogger(LeapConfigurationUtil.class);
	protected static Logger perflogger = LoggerFactory.getLogger("performanceLog");
	private static final String LEAP_DATA_TYPE_ATTRIBUTE = "leap_data_type";
	private static final String XSI_TYPE_ATTRIBUTE = "xsi:type";

	private static final String ID = "id";
	private static final String FUNCTIONALITIES = "functionalities";

	/**
	 * This method is used to get LDC from exchange
	 * 
	 * @param exchange
	 * @return
	 */

	public static LeapDataContext getLDCFromExchange(Exchange exchange) {
		String methodName = "getLDCFromExchange";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = null;
		if (exchange != null) {
			leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
					LeapDataContext.class);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapDataContext;
	}

	/**
	 * This method help us to get RequestBody from LDC either by tag name or from
	 * initialRequest
	 * 
	 * @param leapDataCtx
	 * @param ldcTagToGet - it is optional parameter. if you give null then it will
	 *                    fetch request body from initialRequest Data
	 * @return
	 */
	public static String getRequestBodyFromLDC(LeapDataContext leapDataCtx, String ldcTagToGet) {
		String methodName = "getRequestBodyFromLDC";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapResultSet reslultSet = (ldcTagToGet == null || ldcTagToGet.isEmpty()) ? leapDataCtx.getInitialRequestData()
				: leapDataCtx.getDataByTag(ldcTagToGet);
		String xmlString = reslultSet.getData().toString();
		logger.debug("{} body with tag: {}, is: {}", LEAP_LOG_KEY, ldcTagToGet, xmlString);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xmlString;
	}

	/**
	 * This method is used to set the response code for the exchange
	 * 
	 * @param key             :key String
	 * @param exchange        :exchange Object
	 * @param responseMessage :responseMessage String
	 * @throws JsonParserException
	 * @throws JSONException
	 */

	public static void setResponseCode(int key, Exchange exchange, String responseMessage) throws JSONException {
		String methodName = "setResponseCode";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, key);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("http response code ", exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));
		jsonObj.put("response message ", responseMessage);
		exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
		exchange.getIn().setHeader("Access-Control-Allow-Methods", "POST");
		exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
		exchange.getIn().setBody(jsonObj);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method setResponseCode

	/**
	 * This method is used to check the perforamce of the each method
	 * 
	 * @param nameOfMethod -> {@link String} -> name of the method
	 */
	public static void performanceChecker(String nameOfMethod, long startTime, long endTime) {
		Long diff = endTime - startTime;
		perflogger.info("{} total time to took for {} {} ms", LEAP_LOG_KEY, nameOfMethod, diff);

	}// ..end of the performanceChecker

	/**
	 * This method is used to check the start time of the each method
	 * 
	 * @param nameOfMethod -> {@link String} -> name of the method
	 */
	public static long startTimeChecker(String nameOfMethod) {
		Long startTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		// perflogger.info("inside the " + nameOfMethod + "method startTime in
		// milli seconds " + startTime + "ms");
		return startTime;

	}// ..end of the startTimeChecker

	/**
	 * This method is used to check the perforamce of the each method
	 * 
	 * @param nameOfMethod -> {@link String} -> name of the method
	 */
	public static long endTimeChecker(String nameOfMethod) {
		Long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		// perflogger.info("end of the " + nameOfMethod + " method endTime in
		// milli seconds " + endTime + "ms");
		return endTime;

	}// ..end of the endTimeChecker

	public void storeAllEntitiesConfigurationInServiceContext(Exchange exchange)
			throws LeapDataServiceConfigurationException {
		String methodName = "storeAllEntitiesConfigurationInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		try {
			if (leapServiceContext != null) {
				leapServiceContext.storeAllEntityConfigToServiceContext();

			} else {
				throw new LeapDataServiceConfigurationException(
						"unable to get the Entities configuration for the service"
								+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} catch (LeapDataServiceConfigurationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new LeapDataServiceConfigurationException("unable to get the Entities configuration from the service "
					+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void storeEntityConfigurationInServiceContext(String configName, Exchange exchange)
			throws LeapDataServiceConfigurationException {
		String methodName = "storeEntityConfigurationInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		try {
			if (leapServiceContext != null || (configName != null && !(configName.isEmpty()))) {
				leapServiceContext.storeEntityConfigToServiceContext(configName);
			} else {
				throw new LeapDataServiceConfigurationException(
						"unable to get the Entity configuration from the configName " + configName + "for the service"
								+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} catch (LeapDataServiceConfigurationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new LeapDataServiceConfigurationException("unable to get the Entity configuration for " + configName
					+ " from the service " + exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME)
					+ ", feature : " + exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
					+ " and feature group : " + exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is use to store the permastore from the cache using configname in
	 * the running service context
	 * 
	 * @param configName
	 * @param exchange
	 * @throws PermaStoreConfigRequestException
	 * @throws JSONException
	 */
	public void storePermastoreConfigurationInServiceContext(String configName, Exchange exchange)
			throws PermaStoreConfigRequestException, JSONException {
		String methodName = "storePermastoreConfigurationInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		try {
			if (leapServiceContext != null || (configName != null && !(configName.isEmpty()))) {
				leapServiceContext.storePermastoreConfigToServiceContext(configName);
			} else {
				throw new PermaStoreConfigRequestException(
						"unable to get the permastore configuration from the configName " + configName
								+ "for the service" + exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME)
								+ ", feature : " + exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} catch (PermaStoreConfigRequestException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new PermaStoreConfigRequestException("unable to get the permastore configuration from the service "
					+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method storePermastoreConfigurationInServiceContext

	/**
	 * This method is used to get the policy configuration
	 * 
	 * @param configName
	 * @param exchange
	 * @throws PolicyConfigurationException
	 * @throws JSONException
	 */
	public void getPolicyConfiguration(String configName, Exchange exchange)
			throws PolicyConfigurationException, JSONException {
		String methodName = "getPolicyConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		// create the policy request context
		try {
			if (leapServiceContext != null || (configName != null && !(configName.isEmpty()))) {
				String tenant = leapServiceContext.getTenant();
				String site = leapServiceContext.getSite();
				String featureGroup = leapServiceContext.getFeatureGroup();
				String featureName = leapServiceContext.getFeatureName();
				String vendor = leapServiceContext.getVendor();
				String version = leapServiceContext.getVersion();
				String implName = leapServiceContext.getImplementationName();
				logger.trace("{} vendor :{} , version : {} ", LEAP_LOG_KEY, vendor, version);
				Map<String, Object> policyReqVariableInMap = leapServiceContext.getPolicyFromServiceContext();

				PolicyRequestContext requestContext = new PolicyRequestContext(tenant, site, featureGroup, featureName,
						implName, vendor, version);
				IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
				PolicyConfigurationUnit policyConfigUnit = policyConfigService
						.getPolicyConfigurationUnit(requestContext, configName);
				List<PolicyEvaluationConfigurationUnit> policyEvalConfigUnitList = policyConfigUnit
						.getEvaluationUnitList();
				for (PolicyEvaluationConfigurationUnit policyEvalConfigUnit : policyEvalConfigUnitList) {
					List<String> reqVarList = policyEvalConfigUnit.getReqVarList();
					for (String reqVar : reqVarList) {
						Object reqValue = getRequestVariableValueFromExchange(exchange, reqVar);
						policyReqVariableInMap.put("$" + reqVar, reqValue);
					}

				} // end of for on list of PolicyEvaluationConfigurationUnit
			} // end of if checking the config name value exists or it is empty
			else {
				throw new PolicyConfigurationException("unable to get the permastore configuration from the configName "
						+ configName + "for the service"
						+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
						+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
						+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} catch (PolicyConfigurationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new PolicyConfigurationException("unable to get the permastore configuration from the service "
					+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
					+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to initialize EventRequestContext object with data store
	 * in leapHeader
	 * 
	 * @param exchange : Exchange object to getleapHeader
	 */
	public EventRequestContext initializeEventRequestContext(Exchange exchange) {
		String methodName = "initializeEventRequestContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		// initialize EventRequestContext with data in leapHeader
		EventRequestContext eventRequestContext = new EventRequestContext();
		eventRequestContext.setTenant(leapServiceContext.getTenant());
		eventRequestContext.setSite(leapServiceContext.getSite());
		eventRequestContext.setServiceName(leapServiceContext.getRunningContextServiceName());
		eventRequestContext.setFeatureGroup(leapServiceContext.getFeatureGroup());
		eventRequestContext.setFeatureName(leapServiceContext.getFeatureName());
		eventRequestContext.setHazelcastTransactionalContext(leapServiceContext.getHazelcastTransactionalContext());
		eventRequestContext.setRequestUUID(leapServiceContext.getRequestUUID());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventRequestContext;
	}

	/**
	 * This method is used to compare datacontext for this feature and other
	 * feature. If same then create Apache metamodel datacontext else create
	 * composite datacontext
	 * 
	 * @param requestContext        : Feature Request Context Object
	 * @param featureDataContext    : FeatureDataContext Object of current feature
	 * @param refFeatureDataContext : FeatureDataContext Object of reference feature
	 * @return
	 */
	public boolean compareDataContext(RequestContext requestContext, DataContext featureDataContext,
			FeatureDataContext refFeatureDataContext) {
		String methodName = "compareDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean flag = false;
		String dbBeanRefName = featureDataContext.getDbBeanRefName();
		String dbType = featureDataContext.getDbType();
		String dbHost = featureDataContext.getDbHost();
		String dbPort = featureDataContext.getDbPort();
		String dbSchema = featureDataContext.getDbSchema();
		List<RefDataContexts> refDataContextsList = refFeatureDataContext.getRefDataContexts();
		for (RefDataContexts refDataContexts : refDataContextsList) {
			String featureGroup = refDataContexts.getFeatureGroup();
			String featureName = refDataContexts.getFeatureName();
			if (featureGroup.equalsIgnoreCase(requestContext.getFeatureGroup())
					&& featureName.equalsIgnoreCase(requestContext.getFeatureName())) {
				List<RefDataContext> refDataContextList = refDataContexts.getRefDataContext();
				for (RefDataContext refDataContext : refDataContextList) {
					if (refDataContext.getDbBeanRefName().equalsIgnoreCase(dbBeanRefName)
							&& refDataContext.getDbType().equalsIgnoreCase(dbType)
							&& refDataContext.getDbHost().equalsIgnoreCase(dbHost)
							&& refDataContext.getDbPort().equalsIgnoreCase(dbPort)
							&& refDataContext.getDbSchema().equalsIgnoreCase(dbSchema)) {
						flag = true;
					} else {
						flag = false;
					}
				}
			} // end of if matching fetaureGroup and featureName
		} // end of for(RefDataContexts refDataContexts:refDataContextsList)
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return flag;
	}

	/**
	 * method to get IntegrationPipeContext
	 * 
	 * @param exchange
	 */
	public void getIntegrationPipeContext(Exchange exchange) {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		Map<String, Object> body = serviceDataContext.getPipelineContextDataFromServiceContext();
		String exchangeBody = (String) body.get(LeapGenericConstant.PIPE_CONTEXT_KEY);
		exchange.getIn().setBody(exchangeBody);
	}// end of method

	/**
	 * method to set IntegrationPipeContext
	 * 
	 */
	public void setIntegrationPipeContext(String str, Exchange exchange) {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		Map<String, Object> body = serviceDataContext.getPipelineContextDataFromServiceContext();
		String exchangeBody = str;
		body.put(LeapGenericConstant.PIPE_CONTEXT_KEY, exchangeBody);
	}

	/**
	 * the newly added integrationPipeline configuration object, retrieved from the
	 * leap header
	 * 
	 * @param configName
	 * @param exchange
	 * @throws IntegrationPipelineConfigException
	 */
	public void getIntegrationPipelineConfiguration(String configName, RequestContext reqcontext,
			LeapServiceContext serviceDataContext, Exchange exchange) throws IntegrationPipelineConfigException {
		String methodName = "getIntegrationPipelineConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace(" Request Context:{} {} ", LEAP_LOG_KEY, reqcontext);
		IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipeLineConfigurationService
				.getIntegrationPipeConfiguration(reqcontext, configName);
		RequestContext request = new RequestContext(reqcontext.getTenantId(), reqcontext.getSiteId(),
				reqcontext.getFeatureGroup(), reqcontext.getFeatureName(), reqcontext.getImplementationName(),
				reqcontext.getVendor(), reqcontext.getVersion());
		if (pipelineConfigUnit == null) {
			// tO GET PIPElINE dETAILS FROM DEFAULT TENANT GROUP, take it from
			// framework
			logger.trace("pipelineConfigUnit == null", LEAP_LOG_KEY);
			request.setTenantId(LeapGenericConstant.DEFAULT_TENANTID);
			request.setSiteId(LeapGenericConstant.DEFAULT_SITEID);
			pipelineConfigUnit = pipeLineConfigurationService.getIntegrationPipeConfiguration(request, configName);
			logger.trace("{} pipeLineConfiguration Unit After setting the tenant And Site as All : {}", LEAP_LOG_KEY,
					pipelineConfigUnit.toString());
		}
		logger.trace(".inIPipelineLeapConfigUtil..{} ConfigurationName in leapConfigUtil..{}, {}", LEAP_LOG_KEY,
				pipelineConfigUnit, configName);
		logger.trace("{} Request Context {} ", LEAP_LOG_KEY, reqcontext);
		Map<String, Object> integrationCahedObject = serviceDataContext.getIntegrationPipelineFromServiceContext();
		if (integrationCahedObject == null)
			integrationCahedObject = new HashMap<String, Object>();
		integrationCahedObject.put(LeapDataContextConstant.PIPELINE_CONFIG_KEY, pipelineConfigUnit);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	public org.apache.metamodel.DataContext getDataContext(Exchange exchange, String operation)
			throws PropertiesConfigException {

		HashMap<String, TypeToken<?>> tyHashMap = new HashMap<>();
		tyHashMap.put("eventkeys", new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1353193293707810284L;
		});
		DataContextPropertiesImpl properties = new DataContextPropertiesImpl();
		properties.put(DataContextPropertiesImpl.PROPERTY_HOSTNAME,
				LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.HOST_KEY, LeapDefaultConstants.DEFAULT_HOST_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_PORT,
				LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.PORT_KEY, LeapDefaultConstants.DEFAULT_PORT_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_URL,
				LeapConfigUtil.getGlobalPropertyValue(CassandraUtil.URL_KEY, LeapDefaultConstants.DEFAULT_URL_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_DRIVER_CLASS, LeapConfigUtil
				.getGlobalPropertyValue(CassandraUtil.DRIVER_CLASS_KEY, LeapDefaultConstants.DEFAULT_DRIVER_CLASS_KEY));
		properties.put(DataContextPropertiesImpl.PROPERTY_DATABASE, LeapConfigUtil
				.getGlobalPropertyValue(CassandraUtil.KEYSPACE_KEY, LeapDefaultConstants.DEFAULT_KEYSPACE_KEY));
		properties.put("type-token", tyHashMap);
		org.apache.metamodel.DataContext dataContext = DataContextFactoryRegistryImpl.getDefaultInstance()
				.createDataContext(properties);
		return dataContext;
	}

	/**
	 * This method is used to get the request body from LDC by tag or without tag,
	 * and convert it into xml string
	 * 
	 * @param tagName  - tag name to get body from LDC
	 * @param exchange
	 * @return
	 * @throws LeapDataServiceConfigurationException
	 */
	public String jsonToXMLFormLDCGetTag(String tagName, Exchange exchange)
			throws LeapDataServiceConfigurationException {
		String methodName = "jsonToXMLFormLDCGetTag";
		logger.debug("{} entered into the method {} tagName:{}", LEAP_LOG_KEY, methodName, tagName);
		LeapDataContext ldcFromExchange = getLDCFromExchange(exchange);
		String reqBody = getRequestBodyFromLDC(ldcFromExchange, tagName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return jsonToXMLFromObject(reqBody);
	}

	/**
	 * This method is used to get the request body from LDC by tag or without tag,
	 * and convert it into xml string and Push it to LDC using PushTag.
	 * 
	 * @param tagName  - it is optional parameter. If you give null then it will
	 *                 fetch request body from initialRequest Data
	 * @param pushTag  - tag name to push data into LDC
	 * @param exchange
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapDataContextInitialzerException
	 */
	public void jsonToXMLWithLDC(String tagName, String pushTag, Exchange exchange)
			throws LeapDataServiceConfigurationException, LeapDataContextInitialzerException {
		String methodName = "jsonToXMLFormLDCGetTag";
		logger.debug("{} entered into the method {} tagName:{}  push Tag is:{}", LEAP_LOG_KEY, methodName, tagName,
				pushTag);
		nullOrEmptyCheck(pushTag, "LDC push tag name cannot be null or empty");
		LeapDataContext ldcFromExchange = getLDCFromExchange(exchange);
		String reqBody = getRequestBodyFromLDC(ldcFromExchange, tagName);
		String xml = jsonToXMLFromObject(reqBody);
		ldcFromExchange.addContextElement(xml, pushTag, pushTag, null);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to conver json to xml string.</br>
	 * <p>
	 * <b>Note:</b>
	 * </p>
	 * <p>
	 * 1) If given input is JSONArray then we are formatting it as follow
	 * </p>
	 * 
	 * <pre>
	 *  <i><b>Input</b></i></br>
	 * [
	{
		"key": "value1",
		"id": "10001"
	},
	{
		"key": "value2",
		"id": "10002"
	},
	{
		"key": "value3",
		"id": "10003"
	}
	]
	 * </pre>
	 * 
	 * <pre>
	 * <i><b>Output:</b></i></br>
	 * 
	&lt;root&gt;
		&lt;array&gt;
	   		&lt;id&gt;10001&lt;/id&gt;
	   		&lt;key&gt;value1&lt;/key&gt;
		&lt;/array&gt;
		&lt;array&gt;
	   		&lt;id&gt;10002&lt;/id&gt;
	   		&lt;key&gt;value2&lt;/key&gt;
		&lt;/array&gt;
		&lt;array&gt;
	   		&lt;id&gt;10003&lt;/id&gt;
	   		&lt;key&gt;value3&lt;/key&gt;
		&lt;/array&gt;
	&lt;/root&gt;
	 * </pre>
	 * <p>
	 * 2) If Json doesnt have root element then it will wrap xml into root tag and
	 * return it
	 * </p>
	 * 
	 * <pre>
	 * Before converting
	 * {
	 * "name":"abc",
	 * "id":"1dg5"
	 * }
	 * After conversion
	 * &lt;root&gt;
	 * 	&lt;name&gt;abc&lt;/name&gt;
	 * 	&lt;id&gt;1dg5&lt;/id&gt;
	 * &lt;/root&gt;
	 * </pre>
	 * 
	 * @param json - It accepts JsonObject object, JsonArray object and also json as
	 *             String
	 * @return -retuns xml as string
	 * @throws LeapDataServiceConfigurationException
	 */
	public String jsonToXMLFromObject(Object json) throws LeapDataServiceConfigurationException {
		String methodName = "jsonToXMLFromObject";
		logger.debug("{} entered into the method {} json:{}", LEAP_LOG_KEY, methodName, json);
		nullOrEmptyCheck(json, "json cannot be null or empty");
		if (json instanceof String) {
			String jsonString = json.toString();
			if (jsonString.startsWith("[")) {
				json = new JSONArray(jsonString);
			} else if (jsonString.startsWith("{")) {
				json = new JSONObject(jsonString);
			} else {
				logger.error(" invalid Json format:{} {}", LEAP_LOG_KEY, jsonString);
				throw new LeapDataServiceConfigurationException("invalid Json format");
			}
		}
		// disabled the CDATA processing.
		XMLParserConfiguration withcDataTagName = new XMLParserConfiguration().withcDataTagName(null);
		String xml = XML.toString(json, null, withcDataTagName);
		try {
			boolean xmlContainsRoot = isXmlContainsRoot(xml);
			if (!xmlContainsRoot) {
				logger.debug("adding root element {}", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return "<root>".concat(xml).concat("</root>");
			}
		} catch (Exception e) {
			logger.error("unable to convert json to xml {} {} ", LEAP_LOG_KEY, e.getMessage());
			throw new LeapDataServiceConfigurationException(e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xml;
	}

	/**
	 * This method is used to get body from exchange and convert into xml string
	 * 
	 * @param exchangeJson - camel exchange object which has json as req body
	 * @return
	 * @throws LeapDataServiceConfigurationException
	 */
	public String jsonToXMLFromExchange(Exchange exchangeJson) throws LeapDataServiceConfigurationException {
		String methodName = "jsonToXMLFromExchange";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (exchangeJson != null) {
			String body = exchangeJson.getIn().getBody(String.class);
			if (body != null && !body.isEmpty()) {
				return jsonToXMLFromObject(body);
			} else {
				throw new LeapDataServiceConfigurationException("exchange request body cannot be null or empty");
			}
		}
		throw new LeapDataServiceConfigurationException("exchange cannot be null");

	}

	/**
	 * This method get xml body from LDC by tag or without tag and Convert into json
	 * string
	 * 
	 * @param tagName  -it is optional parameter. if you give null then it will
	 *                 fetch request body from initialRequest Data
	 * @param exchange
	 * @return
	 */
	public String xmlToJson(String tagName, Set<String> forceList, Exchange exchange) {
		String methodName = "xmlToJson";
		logger.debug("{} entered into the method {} tagName:{} forceList{}", LEAP_LOG_KEY, methodName, tagName,
				forceList);
		LeapDataContext ldcFromExchange = getLDCFromExchange(exchange);
		String reqBody = getRequestBodyFromLDC(ldcFromExchange, tagName);
		JSONObject json = xmlToJsonWithXMLParserConfiguration(forceList, reqBody);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return json.toString();
	}

	/**
	 * This method get xml body from Exchange and save it in Exchange as well
	 * 
	 * @param tagName  -it is optional parameter. if you give null then it will
	 *                 fetch request body from initialRequest Data
	 * @param exchange
	 * @return
	 */
	public void xmlToJsonWithExchange(Set<String> forceList, Exchange exchange) {
		String methodName = "xmlToJsonWithExchange";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject json = xmlToJsonWithXMLParserConfiguration(forceList, exchange.getIn().getBody(String.class));
		exchange.getIn().setBody(json.toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to get xml body from LDC by tag or without tag and
	 * Convert into json string and Push it to LDC using push tag
	 * 
	 * @param tagName  -it is optional parameter. if you give null then it will
	 *                 fetch request body from initialRequest Data
	 * @param pushTag  - tag name to push data into LDC
	 * @param exchange
	 * @return
	 * @throws LeapDataContextInitialzerException
	 */
	public void xmlToJsonWithLDC(String tagName, String pushTag, Set<String> forceList, Exchange exchange)
			throws LeapDataContextInitialzerException {
		String methodName = "xmlToJsonWithLDC";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext ldcFromExchange = getLDCFromExchange(exchange);
		String reqBody = getRequestBodyFromLDC(ldcFromExchange, tagName);
		JSONObject jsonObject = xmlToJsonWithXMLParserConfiguration(forceList, reqBody);
		ldcFromExchange.addContextElement(jsonObject, pushTag, pushTag, null);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to convert Document object into json string
	 * 
	 * @param xmldoc Document Object
	 * @return json String
	 * @throws IOException
	 * @throws TransformerException
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapConfigUtilException
	 */
	public String xmlToJsonWithDocument(Document xmldoc, Set<String> forceList)
			throws IOException, TransformerException, LeapConfigUtilException, LeapDataServiceConfigurationException {
		String methodName = "xmlToJsonWithDocument";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String xmlString = toXmlString(xmldoc, null, null);
		JSONObject jsonObject = xmlToJsonWithXMLParserConfiguration(forceList, xmlString);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return jsonObject.toString();
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * pushing back to LDC
	 * 
	 * @param xslFileName xsl file name
	 * @param ldcTagToGet - used to get data from LDC by tag name. If you give null
	 *                    or empty it will get data from "initial request data"
	 * @param ldcTagToPut - used to store data into LDC
	 * @param exchange    {@link Exchange} Object
	 * @throws LeapDataServiceConfigurationException
	 */
	public void transformDataWithLDC(String xslFileName, String ldcTagToGet, String ldcTagToPut, Exchange exchange)
			throws LeapDataServiceConfigurationException {
		String methodName = "transformDataWithLDC";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			LeapDataContext leapDataCtx = getLDCFromExchange(exchange);
			String xmlString = getRequestBodyFromLDC(leapDataCtx, ldcTagToGet);
			nullOrEmptyCheck(xslFileName, "xslt file name");
			nullOrEmptyCheck(ldcTagToPut, "ldc push tag cannot be null or empty");
			if (xmlString.startsWith("{") || xmlString.startsWith("[")) {
				xmlString = jsonToXMLFromObject(xmlString);
			}
			logger.trace("{} request body  from LDC in transformEventBody: {} ", LEAP_LOG_KEY, xmlString);
			Document document = getDocumentFromXml(xmlString);
			String xmlresponse = toXmlString(document, xslFileName, leapDataCtx);
			logger.trace("{} after applying xslt in transformEventBody ::{}", LEAP_LOG_KEY, xmlresponse);
			leapDataCtx.addContextElement(xmlresponse, ldcTagToPut, ldcTagToPut, null);
		} catch (LeapDataServiceConfigurationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} unable to apply transformation:{}  ", LEAP_LOG_KEY, e.getMessage());
			throw new LeapDataServiceConfigurationException("unable to apply transformation: " + e.getMessage());
		}
	}// ..end of the transformData(..) method

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * return it as String
	 * 
	 * @param xslFileName xsl file Name
	 * @param ldcTagToGet - used to get data from ldc by tag name. If you give null
	 *                    or empty it will get data from "initial request data"
	 * @param exchange
	 * @return
	 * @throws LeapDataServiceConfigurationException
	 */
	public String transformDataFromLdcGetTag(String xslFileName, String ldcTagToGet, Exchange exchange)
			throws LeapDataServiceConfigurationException {
		try {
			LeapDataContext leapDataCtx = getLDCFromExchange(exchange);
			nullOrEmptyCheck(xslFileName, "xslt file name cannot be null or empty");
			String xmlString = getRequestBodyFromLDC(leapDataCtx, ldcTagToGet);
			if (xmlString.startsWith("{") || xmlString.startsWith("[")) {
				xmlString = jsonToXMLFromObject(xmlString);
			}
			logger.debug("{} request body  from LDC in transformEventBody  :: {}", LEAP_LOG_KEY, xmlString);
			Document document = getDocumentFromXml(xmlString);
			String xmlresponse = toXmlString(document, xslFileName, leapDataCtx);
			logger.debug("{} after applying xslt in transformEventBody ::{} ", LEAP_LOG_KEY, xmlresponse);
			return xmlresponse;
		} catch (LeapDataServiceConfigurationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} unable to apply transformation:{} ", LEAP_LOG_KEY, e.getMessage());
			throw new LeapDataServiceConfigurationException("unable to apply transformation: " + e.getMessage());
		}
	}// ..end of the transformData(..) method

	/**
	 * This method is used to transform the body from Exchange ,applying the XSLT
	 * and store it in exchange
	 * 
	 * @param xslFileName xslt file Name
	 * @param exchange
	 * @return
	 * @throws LeapDataServiceConfigurationException
	 */
	public void transformDataWithExchange(String xslFileName, Exchange exchange)
			throws LeapDataServiceConfigurationException {
		String methodName = "transformDataWithExchange";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			nullOrEmptyCheck(xslFileName, "xslt file name cannot be null or empty");
			LeapDataContext leapDataCtx = getLDCFromExchange(exchange);
			String xmlString = exchange.getIn().getBody(String.class);
			if (xmlString.startsWith("{") || xmlString.startsWith("[")) {
				xmlString = jsonToXMLFromObject(xmlString);
			}
			logger.trace("{} request body  from LDC in transformEventBody  :: {}", LEAP_LOG_KEY, xmlString);
			Document document = getDocumentFromXml(xmlString);
			String xmlresponse = toXmlString(document, xslFileName, leapDataCtx);
			logger.trace("{} after applying xslt in transformEventBody ::{}", LEAP_LOG_KEY, xmlresponse);
			exchange.getIn().setBody(xmlresponse);
		} catch (LeapDataServiceConfigurationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} unable to apply transformation: {}", LEAP_LOG_KEY, e.getMessage());
			throw new LeapDataServiceConfigurationException("unable to apply transformation: " + e.getMessage());
		}
	}// ..end of the transformEventBody(..) method

	/**
	 * This method is used to store request body from exchange to LDC using pushTag
	 * 
	 * @param pushTag
	 * @param exchange
	 * @throws LeapDataContextInitialzerException
	 */
	public void pushBodyToLDCFromExchange(String pushTag, Exchange exchange) throws LeapDataContextInitialzerException {
		String methodName = "pushBodyToLDCFromExchange";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String body = exchange.getIn().getBody(String.class);
		LeapDataContext ldc = getLDCFromExchange(exchange);
		ldc.addContextElement(body, pushTag, pushTag, null);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used for especially for leap-cli to describe about our feature
	 * what functionalities have implemented by hitting the service
	 * (feature-describe)
	 * 
	 * @param exchange {@link Exchange}
	 * @return {@link String} -> json string to the exchange to display response
	 * @throws LeapDataServiceConfigurationException
	 */
	public String getFeatureDescription(Exchange exchange) throws LeapDataServiceConfigurationException {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		JSONObject featureJson = new JSONObject();
		ConfigNodeDAO configNode = new ConfigNodeDAO();
		ConfigNodeDataDAO configNodeData = new ConfigNodeDataDAO();
		List<String> functionalitiesList = new ArrayList<String>();
		try {
			String featureGroup = exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP).toString();
			FeatureDeployment featureDeploymentObj = (FeatureDeployment) exchange.getIn()
					.getHeader(LeapDataContextConstant.FEATURE_DEPLOYMENT);
			String featureName = featureDeploymentObj.getFeatureName();
			String implName = featureDeploymentObj.getImplementationName();
			String vendorName = featureDeploymentObj.getVendorName();
			int implNodeId = configNode.getNodeIdByNodeNameAndByType(implName, "implementation");
			int vendorNodeId = implNodeId + 1;
			List<ConfigNodeData> configNodeDataByNodeId = configNodeData.getConfigNodeDataByNodeId(vendorNodeId);
			functionalitiesList.add("FeatureService");
			for (int i = 0; i < configNodeDataByNodeId.size(); i++) {
				if (!configNodeDataByNodeId.get(i).getConfigType().equalsIgnoreCase(FEATURENAME)
						&& configNodeDataByNodeId.get(i).isEnabled())
					functionalitiesList.add(configNodeDataByNodeId.get(i).getConfigType());
			}
			String timeStamp = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss:SSS").format(new java.util.Date());
			featureJson.put(FEATUREGROUP, featureGroup);
			featureJson.put(FEATURENAME, featureName);
			featureJson.put(IMPLEMENTATION_NAME, implName);
			featureJson.put(VENDOR, vendorName);
			featureJson.put(FUNCTIONALITIES, functionalitiesList);
			innerJson.put(KIND_VAL, "FeatureDescription");
			innerJson.put(UPDATED, timeStamp);
			innerJson.put(TOTAL_ITEMS, 1);
			innerJson.put(ITEMS, featureJson);
			json.put(ID, exchange.getExchangeId());
			json.put(API_VERSION, "1.0");
			json.put(DATA, innerJson);
		} catch (ConfigNodeConfigurationException | ConfigNodeDataConfigurationException e) {
			throw new LeapDataServiceConfigurationException("unable to get feature description: " + e.getMessage());
		}
		return json.toString();
	}

	/**
	 * This method is used to check given object is null or not
	 * 
	 * @param obj
	 * @param text
	 * @throws LeapDataServiceConfigurationException
	 */
	private void nullOrEmptyCheck(Object obj, String text) throws LeapDataServiceConfigurationException {
		if (obj == null) {
			logger.error("{} {}", LEAP_LOG_KEY, text);
			throw new LeapDataServiceConfigurationException(text);
		}
		if (obj instanceof String && obj.toString().isEmpty()) {
			logger.error("{} {}", LEAP_LOG_KEY, text);
			throw new LeapDataServiceConfigurationException(text);
		}
	}

	/**
	 * This method helps us to check whether transformed xml contains 'root' tag or
	 * not.
	 * 
	 * @param xmlString
	 * @return
	 * @throws ParserConfigurationException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private boolean isXmlContainsRoot(String xmlString)
			throws ParserConfigurationException, UnsupportedEncodingException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		try {
			db.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
		} catch (SAXException e) {
			return false;
		}
		return true;
	}

	/**
	 * This method helps us to apply xsl on xml if required or helps us to convert
	 * Document as xml string
	 * 
	 * @param doc
	 * @param xsltFileName xslt file Name and it is optional parameter
	 * @param leapDataCtx  its an optional parameter
	 * @return xml string
	 * @throws IOException
	 * @throws TransformerException
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapConfigUtilException
	 */
	private String toXmlString(Document doc, String xsltFileName, LeapDataContext leapDataCtx)
			throws IOException, TransformerException, LeapDataServiceConfigurationException, LeapConfigUtilException {
		String methodName = "toXmlString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = null;
			if (xsltFileName == null || xsltFileName.isEmpty()) {
				transformer = factory.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			} else {
				if (leapDataCtx != null) {
					RequestContext requestContext = leapDataCtx.getServiceDataContext().getRequestContext();
					InputStream xslFileAsInputstream = LeapConfigUtil.getFile(requestContext, xsltFileName);
					nullOrEmptyCheck(xslFileAsInputstream,
							xsltFileName + " file not found at feature spcific folder or in classpath");
					transformer = factory.newTransformer(new StreamSource(xslFileAsInputstream));
				}
			}
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(output);
			transformer.transform(source, result);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return output.toString(StandardCharsets.UTF_8);
		}

	}

	/**
	 * This method is used to get Document object from given xml String
	 * 
	 * @param xmlString
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document getDocumentFromXml(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(new InputSource(new StringReader(xmlString)));
		return document;
	}

	private Object getRequestVariableValueFromExchange(Exchange exchange, String reqVar) {
		String methodName = "#";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		// checking in exchange header
		logger.trace("{} checking in exchange header using {}", LEAP_LOG_KEY, reqVar);
		Object reqvalue = (Object) exchange.getIn().getHeader(reqVar);
		if (reqvalue == null) {
			logger.trace("{} checking in exchange header using ${}", LEAP_LOG_KEY, reqVar);
			reqvalue = (Object) exchange.getIn().getHeader("$" + reqVar);
		}

		// checking into leapHeader data value
		if (reqvalue == null) {
			logger.debug("{} {} not found in exchange so checking in leapHeader data ", LEAP_LOG_KEY, reqVar);
			Map<String, Object> geniricdata = leapServiceContext.getGenericDataFromServiceContext();
			try {
				JSONArray jsonArray = (JSONArray) geniricdata.get(LeapDataContextConstant.DATA_KEY);
				int jsonLen = jsonArray.length();
				logger.debug("{} data's in json array for key data is :{} ", LEAP_LOG_KEY, jsonLen);
				for (int i = 0; i < jsonLen; i++) {
					JSONObject jobj = (JSONObject) jsonArray.get(i);
					logger.debug("{} checking in  leapHeader using {}", LEAP_LOG_KEY, reqVar);
					reqvalue = (Object) jobj.get(reqVar);
					if (reqvalue == null) {
						logger.debug("{} checking in  leapHeader using ${}", LEAP_LOG_KEY, reqVar);
						reqvalue = (Object) jobj.get("$" + reqVar);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// checking in exchange.property
		if (reqvalue == null) {
			logger.debug("{} {} not found in exchange so checking in exchange property data ", LEAP_LOG_KEY, reqVar);
			reqvalue = (String) exchange.getProperty(reqVar);
			logger.trace("{} checking in  leapHeader using {}", LEAP_LOG_KEY, reqVar);
			if (reqvalue == null) {
				logger.trace("{} checking in  leapHeader using ${}", LEAP_LOG_KEY, reqVar);
				reqvalue = (String) exchange.getProperty("$" + reqVar);
			}

		}

		return reqvalue;
	}// end of method

	/**
	 * This method helps us to convert xml to json with XMLParserConfiguration
	 * 
	 * @param forceList - helps us to convert json keys to Json Array
	 * @param xml
	 * @return
	 */
	private JSONObject xmlToJsonWithXMLParserConfiguration(Set<String> forceList, String xml) {
		logger.debug("{} forceList:{} \n xml{}", LEAP_LOG_KEY, forceList, xml);
		if (forceList == null) {
			forceList = Collections.emptySet();
		}
		xml = xml.replaceAll(LEAP_DATA_TYPE_ATTRIBUTE, XSI_TYPE_ATTRIBUTE);
		Map<String, XMLXsiTypeConverter<?>> xsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>();
		xsiTypeMap.put("string", value -> value);
		xsiTypeMap.put("number", value -> {
			BigDecimal bd = new BigDecimal(value);
			return bd;
		});
		xsiTypeMap.put("boolean", value -> Boolean.valueOf(value));
		XMLParserConfiguration config = new XMLParserConfiguration().withXsiTypeMap(xsiTypeMap)
				.withForceList(forceList);
		JSONObject jsonObject = XML.toJSONObject(xml, config);
		logger.info("{} after applying XMLParserConfiguration and converting xml to json::\n{}", LEAP_LOG_KEY,
				jsonObject);
		return jsonObject;

	}
}
