package com.attunedlabs.leap;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATUREGROUP;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.HEADER;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.IMPLEMENTATION_NAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.INITIAL_CONTEXT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LDC_DATA_MAP;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAPDEFAULT_TAXONOMY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.REQUEST_LEAP_lOCAL;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.REQUEST_METHOD;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.REQUEST_TAXONOMY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.REQUEST_TAXONOMY_ID;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.SERVICENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.SITEID;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.TENANTID;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.VENDOR;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.VERSION;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.PROVIDER;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURE_DEPLOYMENT;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.metamodel.data.CachingDataSetHeader;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.InMemoryDataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featuremetainfo.taxonomy.FeatureTaxonomyHelper;
import com.attunedlabs.featuremetainfo.taxonomy.TaxonomyConfigException;
import com.attunedlabs.leap.context.bean.DisplayMetaData;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapJSONArrayResultSet;
import com.attunedlabs.leap.context.bean.LeapJSONResultSet;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.bean.MetaData;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;
import com.attunedlabs.leap.context.helper.LeapDataContextHelper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Reactiveworks
 *
 *         This holds the context elements inside a Deque
 */
public class LeapDataContext {

	static Logger logger = LoggerFactory.getLogger(LeapDataContext.class);
	private Deque<LeapDataContextElement> dequeContextElement;
	private List<String> allElementTagsList; // List Element to store
												// Context Tags, used for an
												// additional check if the
												// 'tag' is present only
												// then iterate the Deque
	private JSONArray projectionsArr;
	private JSONArray flattenedrojectionArr;
	private JSONObject jsonSchema;
	private JSONObject taxonomyJson;
	private String vendorTaxonomyId;
	private Integer configNodeId = null;
	private boolean taxonomyRequired = true;
	private InitialLeapDataContextElement initialContextConfig;
	private FeatureTaxonomyHelper taxonomyHelper = new FeatureTaxonomyHelper();
	private FeatureDeploymentService featureDeploymentService = new FeatureDeploymentService();
	private FeatureDeployment featureDeployment = new FeatureDeployment();
	private Set<String> parentKinds = new HashSet<>();
	private Map<String, JSONArray> contextMap = new HashMap<>();
	private Set<String> parentKindIdentifiers = new HashSet<>();
	private List<JSONObject> finalJsonStructured = new ArrayList<>();
	private Map<String, String> mapToGetActualColumnName = new HashMap<>();
	private Map<String, String> mapToGetEffectiveColumnName = new HashMap<>();
	private Map<String, HashMap<String, Object>> cachedJsonSchema = new HashMap<>();
	private Map<String, ArrayListValuedHashMap<String, Object>> mapOfAllUIDData = new HashMap<>();
	private Map<String, ArrayListValuedHashMap<String, Object>> mapOfParentUIDData = new HashMap<>();
	private ArrayListValuedHashMap<String, String> childOfCacheMap = new ArrayListValuedHashMap<>();
	private LeapServiceContext leapServiceContext;
	private Map<String, String> metaDataMap = new HashMap<>();
	static final String SEARCH_IN_ITEMS = "items";
	static final String SEARCH_IN_PROPERITES = "properties";
	static final String SEARCH_REQUIRED = "required";
	private List<DisplayMetaData> displayMetaDataList;
	private static Map<String, Object> mapForLDCFilesContent = new HashMap<>();
	private static final String UNDER_SCORE = "_";

	public LeapDataContext() {
		dequeContextElement = new LinkedList<LeapDataContextElement>();
		allElementTagsList = new ArrayList<>();
		initialContextConfig = new InitialLeapDataContextElement();
	}

	/**
	 * Method to add LeapDataContextElement inside the Dequeue against a 'tagName'
	 * 
	 * @param ctxElement
	 * @param tags
	 */
	public void addContextElement(LeapDataContextElement ctxElement, String tagName) {
		ctxElement.setTagName(tagName.toLowerCase());
		dequeContextElement.push(ctxElement);
		allElementTagsList.add(tagName.toLowerCase());
		logger.debug("{} dequeContextElement :: {}", LEAP_LOG_KEY, dequeContextElement);
		logger.debug("{} allElementTagsList  ::{} ", LEAP_LOG_KEY, allElementTagsList);
	}

	/**
	 * Method to add LeapDataContextElement inside the Dequeue against a 'kind' &
	 * 'tagName'
	 * 
	 * @param ctxElement
	 * @param tags
	 */
	public void addContextElement(Object ctxElement, String kind, String tagName, String taxonomyId)
			throws LeapDataContextInitialzerException {
		String methodName = "addContextElement";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		/**
		 * This part of code i have commented because their is no need to get if
		 * taxonamyID is null from feature deploymnet which is handled by alok, if we
		 * have faced ant issues then we can uncomment this part for LDC
		 */
		// try {
		// if (taxonomyId == null) {
		// featureDeployment = initializeConfigNodeId();
		// taxonomyId = featureDeployment.getVendorTaxonomyId();
		// }
		// logger.debug("taxonomy Id while adding is : " + taxonomyId);
		// } catch (Exception e) {
		// logger.warn("no taxonomy found ....!" + e.getMessage(), e);
		// }

		try {
			LeapDataContextElement leapDataCtxElement = new LeapDataContextElement();
			LeapDataElement leapDataElement = null;
			if (ctxElement instanceof JSONObject) {
				try {
					JSONObject contextJson = (JSONObject) ctxElement;
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromJSONObject(contextJson, kind,
							taxonomyId, this);
				} catch (LeapDataContextConfigException jsonExp) {
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
							taxonomyId, this);
				}
				leapDataCtxElement.setDataElement(leapDataElement);
			} else if (ctxElement instanceof JSONArray) {
				try {
					JSONArray contextArray = (JSONArray) ctxElement;
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromJSONArray(contextArray, kind,
							taxonomyId, this);
				} catch (LeapDataContextConfigException jsonArrayExp) {
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
							taxonomyId, this);
				}
				leapDataCtxElement.setDataElement(leapDataElement);
			} else if (ctxElement instanceof org.apache.metamodel.data.DataSet) {
				DataSet dataSet = (DataSet) ctxElement;
				CachingDataSetHeader header = new CachingDataSetHeader(dataSet.getSelectItems());
				DataSet newdataSet = new InMemoryDataSet(header, dataSet.toRows());
				leapDataElement = LeapDataContextHelper.constructLeapDataElementFromMMDataSet(newdataSet, kind,
						taxonomyId, this);
				leapDataCtxElement.setDataElement(leapDataElement);
			} else if (ctxElement instanceof String) {
				leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
						taxonomyId, this);
				leapDataCtxElement.setDataElement(leapDataElement);
			}

			// this is to handle if already the LeapDataContextElement we have
			// created in impl or framework and push into LDC
			else if (ctxElement instanceof LeapDataContextElement)
				leapDataCtxElement = (LeapDataContextElement) ctxElement;

			leapDataCtxElement.setTagName(tagName.toLowerCase());
			logger.debug("{} leapDataElement is : {}", LEAP_LOG_KEY, leapDataElement);
			logger.debug("{} leapDataCtxElement is : {} ", LEAP_LOG_KEY, leapDataCtxElement);
			dequeContextElement.push(leapDataCtxElement);
			allElementTagsList.add(tagName.toLowerCase());
		} catch (LeapDataContextConfigException exp) {
			throw new LeapDataContextInitialzerException(exp.getMessage(), exp.getCause(),
					"Unable to add the element into Context", 400);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to add get LDC response format without taxonomyId
	 * 
	 * @param ctxElement
	 * @param tags
	 */
	public JSONObject getLDCResponse(Object ctxElement, String kind) throws LeapDataContextInitialzerException {
		String methodName = "getLDCResponse";
		logger.debug("{} entered into the method {}, kind ={} ", LEAP_LOG_KEY, methodName, kind);
		try {
			LeapDataElement leapDataElement = new LeapDataElement();
			if (ctxElement instanceof JSONObject) {
				try {
					JSONObject contextJson = (JSONObject) ctxElement;
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromJSONObject(contextJson, kind,
							this);
					JSONObject formatLeapResponse = getFormatLeapResponse(leapDataElement);
					logger.info("{} successfully get LDC Response :{} ", LEAP_LOG_KEY, formatLeapResponse);
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return formatLeapResponse;
				} catch (LeapDataContextConfigException jsonExp) {
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
							this);
				}
			} else if (ctxElement instanceof JSONArray) {
				try {
					JSONArray contextArray = (JSONArray) ctxElement;
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromJSONArray(contextArray, kind,
							this);
					JSONObject formatLeapResponse = getFormatLeapResponse(leapDataElement);
					logger.info("{} successfully get LDC Response :{} ", LEAP_LOG_KEY, formatLeapResponse);
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return formatLeapResponse;
				} catch (LeapDataContextConfigException jsonArrayExp) {
					leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
							this);
				}
			} else if (ctxElement instanceof String) {
				leapDataElement = LeapDataContextHelper.constructLeapDataElementFromXML(ctxElement.toString(), kind,
						this);
			}
			if (this.displayMetaDataList != null) {
				LeapData data = leapDataElement.getData();
				if (data != null) {
					data.setDisplayMetaDatas(displayMetaDataList);
				}
			}
			logger.info("{} successfully get LDC Response :{} ", LEAP_LOG_KEY, leapDataElement);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return new JSONObject(leapDataElement);
		} catch (LeapDataContextConfigException exp) {
			throw new LeapDataContextInitialzerException(exp.getMessage(), exp.getCause(),
					"Unable to add the element into Context", 400);
		}
	}

	/**
	 * Method used to convert a LeapDataElement into required Leap Response
	 * Structure.
	 * 
	 * @param leapDataElement
	 * @return Returns a new proper JSON Leap Response with Items type is array and
	 *         also with totalItems.
	 */
	public static JSONObject getFormatLeapResponse(LeapDataElement leapDataElement) {
		String methodName = "getFormatLeapResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapData leapData = leapDataElement.getData();
		JSONObject newLeapDataElement = new JSONObject(leapDataElement);

		// If leapData not null means its Leap Response else its a Leap Error Response.
		if (leapData != null) {
			if (leapData.getItems() instanceof LeapJSONResultSet) {
				logger.trace("{} Instance of LeapJSONResultSet", LEAP_LOG_KEY);
				JSONObject newLeapData = null;
				JSONArray itemsArray = new JSONArray();
				LeapJSONResultSet resultSet = (LeapJSONResultSet) leapData.getItems();// 1ms

				itemsArray.put(resultSet.getData());

				newLeapData = getNewJsonLeapData(itemsArray, leapData.getKind(), itemsArray.length(),
						leapData.getMetadata());// 12ms
				newLeapDataElement.put(LeapDataContextConstant.DATA, newLeapData);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return newLeapDataElement;

			} else if (leapData.getItems() instanceof LeapJSONArrayResultSet) {
				logger.trace("{} Instance of LeapJSONArrayResultSet", LEAP_LOG_KEY);
				JSONObject newLeapData = null;
				JSONArray jsonArrayData = null;
				LeapJSONArrayResultSet resultSet = (LeapJSONArrayResultSet) leapData.getItems();
				jsonArrayData = resultSet.getData();
				newLeapData = getNewJsonLeapData(jsonArrayData, leapData.getKind(), jsonArrayData.length(),
						leapData.getMetadata());
				newLeapDataElement.put(LeapDataContextConstant.DATA, newLeapData);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return newLeapDataElement;
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return newLeapDataElement;
	}

	/**
	 * Method used to build the Leap Response data structure.
	 * 
	 * @param items
	 * @param kind
	 * @param totalItems
	 * @param metadata
	 * @return
	 */
	public static JSONObject getNewJsonLeapData(JSONArray items, String kind, int totalItems, List<MetaData> metadata) {
		String methodName = "getNewJsonLeapData";
		logger.debug("{} entered into the method {},instance of metaData={}", LEAP_LOG_KEY, methodName,
				metadata.getClass());
		JSONObject newLeapData = new JSONObject();
		newLeapData.put(LeapDataContextConstant.ITEMS, items);
		newLeapData.put(LeapDataContextConstant.TOTAL_ITEMS, totalItems);
		newLeapData.put(LeapDataContextConstant.META_DATA, metadata);
		newLeapData.put(LeapDataContextConstant.KIND_VAL, kind);
		newLeapData.put(LeapDataContextConstant.UPDATED, LocalDateTime.now().atZone(ZoneId.systemDefault())
				.toLocalDateTime().toInstant(ZoneOffset.UTC).toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return newLeapData;
	}

	/**
	 * Method used to build the Leap Response data structure.
	 * 
	 * @param items
	 * @param kind
	 * @param totalItems
	 * @param metadata
	 * @return
	 */
	public static JSONObject getNewJsonLeapData(JSONArray items, String kind, int totalItems, JSONArray metadata) {
		String methodName = "getNewJsonLeapData";
		logger.debug("{} entered into the method {},instance of metaData={}", LEAP_LOG_KEY, methodName,
				metadata.getClass());
		JSONObject newLeapData = new JSONObject();
		newLeapData.put(LeapDataContextConstant.ITEMS, items);
		newLeapData.put(LeapDataContextConstant.TOTAL_ITEMS, totalItems);
		newLeapData.put(LeapDataContextConstant.META_DATA, metadata);
		newLeapData.put(LeapDataContextConstant.KIND_VAL, kind);
		newLeapData.put(LeapDataContextConstant.UPDATED, LocalDateTime.now().atZone(ZoneId.systemDefault())
				.toLocalDateTime().toInstant(ZoneOffset.UTC).toString());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return newLeapData;
	}

	/**
	 * Method to add get LDC response format, the taxonomyId should be same as
	 * taxonomyId used to apply taxonomy. If taxonomyId is null, then vendor
	 * taxonomy is used.
	 * 
	 * @param ctxElement
	 * @param tags
	 */
	public JSONObject getLDCResponse(Object ctxElement, String kind, String taxonomyId)
			throws LeapDataContextInitialzerException {
		String methodName = "getLDCResponse";
		logger.debug("{} entered into the method {}, kind={}, taxonomyId={}", LEAP_LOG_KEY, methodName, kind,
				taxonomyId);
		try {
			if (taxonomyId == null) {
				featureDeployment = initializeConfigNodeId();
				taxonomyId = featureDeployment.getVendorTaxonomyId();
			}
		} catch (Exception e) {
			logger.warn("{} no taxonomy  found ....! :{}", LEAP_LOG_KEY, e.getMessage(), e);
		}
		logger.info("{} vendorTaxonomyId is : {} ", LEAP_LOG_KEY, vendorTaxonomyId);
		logger.info("{} taxonomyId is : {} ", LEAP_LOG_KEY, taxonomyId);
		if (!taxonomyId.equals(vendorTaxonomyId)) {
			throw new LeapDataContextInitialzerException(
					"taxonomyId is different from taxonomyId used in applying taxonomy || taxonomy is not applied",
					new Throwable(), "unable to get response", 400);
		}

		try {
			if (taxonomyId.equalsIgnoreCase(LeapDataContextConstant.LEAPDEFAULT_TAXONOMY)) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return getLDCResponse(ctxElement, kind);
			}

			LeapDataElement leapDataElement = new LeapDataElement();
			if (ctxElement instanceof JSONObject) {
				try {
					JSONObject contextJson = (JSONObject) ctxElement;
					leapDataElement = LeapDataContextHelper.constructResponseLeapDataElementFromJSONObject(contextJson,
							kind, metaDataMap, this);
				} catch (LeapDataContextConfigException jsonExp) {
					leapDataElement = LeapDataContextHelper
							.constructResponseLeapDataElementFromXML(ctxElement.toString(), kind, metaDataMap, this);
				}
			} else if (ctxElement instanceof JSONArray) {
				try {
					JSONArray contextArray = (JSONArray) ctxElement;
					leapDataElement = LeapDataContextHelper.constructResponseLeapDataElementFromJSONArray(contextArray,
							kind, metaDataMap, this);
				} catch (LeapDataContextConfigException jsonArrayExp) {
					leapDataElement = LeapDataContextHelper
							.constructResponseLeapDataElementFromXML(ctxElement.toString(), kind, metaDataMap, this);
				}
			} else if (ctxElement instanceof String) {
				leapDataElement = LeapDataContextHelper.constructResponseLeapDataElementFromXML(ctxElement.toString(),
						kind, metaDataMap, this);
			}
			if (this.displayMetaDataList != null) {
				LeapData data = leapDataElement.getData();
				if (data != null) {
					updateI18NforDisplayMetaData(data.getMetadata());
					data.setDisplayMetaDatas(displayMetaDataList);
				}
			}
			logger.info("{} successfully get LDC Response :{} ", LEAP_LOG_KEY, leapDataElement);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return new JSONObject(leapDataElement);
		} catch (LeapDataContextConfigException exp) {
			throw new LeapDataContextInitialzerException(exp.getMessage(), exp.getCause(),
					"Unable to add the element into Context", 400);
		}
	}

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service.
	 * 
	 * @param tenant : internal tenantId
	 * @param site   : site id
	 * @return LeapServiceContext Object
	 */
	public LeapServiceContext getServiceDataContext(String tenant, String site) {
		String methodName = "getServiceDataContext";
		logger.debug("{} entered into the method {}, tenant={}, site={}", LEAP_LOG_KEY, methodName, tenant, site);
		LeapServiceContext leapServiceContext = null;
		if (this.leapServiceContext == null) {
			leapServiceContext = new LeapServiceContext(tenant, site);
			this.leapServiceContext = leapServiceContext;
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return leapServiceContext;
		} else {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return this.leapServiceContext;
		}

	}// end of method getServiceDataContext

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service.
	 * 
	 * @param tenant : internal tenantId
	 * @param site   : site id
	 * @return LeapServiceContext Object
	 */
	public LeapServiceContext getServiceDataContext() {
		String methodName = "getServiceDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (this.leapServiceContext == null) {
			leapServiceContext = new LeapServiceContext();
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return leapServiceContext;
		} else {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return this.leapServiceContext;
		}

	}// end of method getServiceDataContext

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service.
	 * 
	 * @param tenant       : internal tenantId
	 * @param site         : site id
	 * @param featureGroup : feature group name
	 * @param featureName  : feature name
	 * @return LeapServiceContext Object
	 */
	public LeapServiceContext getServiceDataContext(String tenant, String site, String featureGroup,
			String featureName) {
		String methodName = "getServiceDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceContext leapServiceContext = null;
		if (this.leapServiceContext == null) {
			leapServiceContext = new LeapServiceContext(tenant, site, featureGroup, featureName);
			this.leapServiceContext = leapServiceContext;
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return leapServiceContext;
		} else {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return this.leapServiceContext;
		}

	}// end of method getServiceDataContext

	/**
	 * 
	 * @return
	 * @throws TaxonomyConfigException
	 * @throws FeatureDeploymentServiceException
	 */
	private FeatureDeployment initializeConfigNodeId()
			throws TaxonomyConfigException, FeatureDeploymentServiceException {
		FeatureDeployment featureDeployedDeatils = null;
		if (configNodeId == null) {
			JSONObject requestHeaders = this.getRequestHeaders(HEADER);
			if (requestHeaders != null) {
				String tenantId = requestHeaders.getString(TENANTID);
				String siteId = requestHeaders.getString(SITEID);
				String vendorName = requestHeaders.getString(VENDOR);
				String version = requestHeaders.getString(VERSION);
				String featureGroup = requestHeaders.getString(FEATUREGROUP);
				String featureName = requestHeaders.getString(FEATURENAME);
				String implementation = requestHeaders.getString(IMPLEMENTATION_NAME);
				ConfigurationContext configContext = new ConfigurationContext(tenantId, siteId, featureGroup,
						featureName, implementation, vendorName, version);
				featureDeployedDeatils = featureDeploymentService.getFeatureDeployedDeatils(configContext);
				taxonomyHelper.initializeConfigNodeId(configContext);

			}
		}
		return featureDeployedDeatils;
	}

	/**
	 * Method to get the List of context element on list of 'tagName'
	 * 
	 * @param listoftags
	 * @return List<LeapDataElement>
	 */
	public List<LeapDataElement> getRawContextElementForTags(List<String> listoftags) {
		String methodName = "getRawContextElementForTags";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContextElement ctxtElement;
		LeapDataElement dataElement;
		List<LeapDataElement> leapDataContextElementList = new ArrayList<>();
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();

		while (itr.hasNext()) {
			ctxtElement = itr.next();
			for (String singleTag : listoftags) {
				if (allElementTagsList.contains(singleTag.toLowerCase())
						&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(singleTag.toLowerCase())) {
					dataElement = ctxtElement.getDataElement();
					leapDataContextElementList.add(dataElement);
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapDataContextElementList;
	}

	/**
	 * Method to get the List of context element as 'JSONObject' for list of
	 * 'tagName'
	 * 
	 * @param listoftags
	 * @return {@link JSONObject}
	 */
	public JSONObject getContextElementsForTags(List<String> listoftags) {
		String methodName = "getContextElementsForTags";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataElement dataElement;
		JSONObject leapDataElementJson = null;
		for (String singleTag : listoftags) {
			for (LeapDataContextElement ctxtElement : dequeContextElement) {
				if (allElementTagsList.contains(singleTag.toLowerCase())
						&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(singleTag.toLowerCase())) {
					dataElement = ctxtElement.getDataElement();
					leapDataElementJson = LeapDataContextHelper.getListOfContextElementAsJson(dataElement, singleTag);
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapDataElementJson;
	}

	/**
	 * Method to get the Map of tag and RawData by 'tagName'
	 * 
	 * @param listoftags
	 *
	 */
	public void getDataByTags(List<String> listoftags, Exchange exchange) {
		LeapDataElement dataElement;
		Map<String, Object> rawDataMap = new HashMap<String, Object>();
		if (listoftags != null) {
			for (String singleTag : listoftags) {
				for (LeapDataContextElement ctxtElement : dequeContextElement) {
					if (allElementTagsList.contains(singleTag.toLowerCase())
							&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(singleTag.toLowerCase())) {
						dataElement = ctxtElement.getDataElement();
						LeapData data = dataElement.getData();
						LeapResultSet items = data.getItems();
						Object reqresData = items.getData();
						rawDataMap.put(singleTag, reqresData);

					}
				}
			}
			exchange.getIn().setHeader(LDC_DATA_MAP, rawDataMap);
		}
	}

	/**
	 * Method to fetch LeapDataElement Object for a 'tag'
	 * 
	 * @param tagName
	 * @return LeapDataElement
	 */
	public LeapDataElement getContextElement(String tagName) {
		LeapDataContextElement ctxtElement;
		LeapDataElement dataElement = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (tagName != null) {
			if (allElementTagsList.contains(tagName.toLowerCase())) {
				while (itr.hasNext()) {
					ctxtElement = itr.next();
					if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
						dataElement = ctxtElement.getDataElement();
						return dataElement;
					}
				}
			}
		}
		return dataElement;
	}

	/**
	 * Method to remove LeapDataElement from deque
	 * 
	 * @param tagName
	 * @return LeapDataElement
	 */
	public void removeContextElement(String tagName) {
		LeapDataContextElement ctxtElement = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					break;
				}
			}
			if (ctxtElement != null) {
				dequeContextElement.remove(ctxtElement);
				allElementTagsList.remove(tagName.toLowerCase());
			}
		}
	}

	/**
	 * Method to fetch Context Element for a 'tag'
	 * 
	 * @param tagName
	 * @return {@link JSONObject}
	 */
	public JSONObject getContextElementForTag(String tagName) {
		LeapDataContextElement ctxtElement;
		LeapDataElement dataElement = null;
		JSONObject leapDataElementJson = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				if (ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tagName.toLowerCase())) {
					dataElement = ctxtElement.getDataElement();
					leapDataElementJson = LeapDataContextHelper.getContextElementAsJson(dataElement, tagName);
					return leapDataElementJson;
				}
			}
		}
		return leapDataElementJson;
	}

	/**
	 * Method to fetch List of Context Elements for the same 'tag'
	 * 
	 * @param tagName
	 * @return List<JSONObject>
	 */
	public List<JSONObject> getContextElementsForTag(String tagName) {
		LeapDataContextElement ctxtElement;
		LeapDataElement dataElement = null;
		JSONObject leapDataElementJson = null;
		List<JSONObject> listOfLeapDataElements = new ArrayList<>();
		Iterator<LeapDataContextElement> itr = dequeContextElement.descendingIterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				if (ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tagName.toLowerCase())) {
					dataElement = ctxtElement.getDataElement();
					leapDataElementJson = LeapDataContextHelper.getContextElementAsJson(dataElement, tagName);
					listOfLeapDataElements.add(leapDataElementJson);
				}
			}
		}
		return listOfLeapDataElements;
	}

	/**
	 * Method to fetch Context Element for a list of 'kinds'
	 * 
	 * @param kinds
	 * @return {@link JSONArray}
	 * @throws LeapDataContextConfigException
	 */
	public JSONArray getContextElementForKinds(List<String> kinds) throws LeapDataContextConfigException {
		Object leapDataElementObject;
		LeapDataElement dataElement;
		LeapDataContextElement ctxtElement;
		JSONArray listOfLeapDataElements = null;
		String kind = null, kindLowerCase = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.descendingIterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (ctxtElement.getDataElement() instanceof InitialLeapDataContextElement) {
				continue;
			} else {
				kindLowerCase = ctxtElement.getDataElement().getData().getKind();
			}
			for (int x = 0; x < kinds.size(); x++) {
				listOfLeapDataElements = new JSONArray();
				kind = kinds.get(x);
				if (kind.contains(".")) {
					String[] split = kind.split("\\.");
					kind = split[split.length - 1];
				}
				if (kindLowerCase.equalsIgnoreCase(kind)) {
					dataElement = ctxtElement.getDataElement();
					leapDataElementObject = LeapDataContextHelper.getContextElementAsJsonForKind(dataElement, kind);
					if (leapDataElementObject instanceof JSONObject) {
						listOfLeapDataElements.put(new JSONObject(leapDataElementObject.toString()));
					} else if (leapDataElementObject instanceof JSONArray) {
						JSONArray newJsonArray = new JSONArray(leapDataElementObject.toString());
						for (Object eachJsonObject : newJsonArray) {
							listOfLeapDataElements.put(new JSONObject(eachJsonObject.toString()));
						}
					}
				}
			}
		}
		return listOfLeapDataElements;
	}

	/**
	 * Method to fetch the Context Elements for a bunch of kind's
	 * 
	 * @param kinds
	 * @return {@link JSONArray}
	 * @throws LeapDataContextConfigException
	 */
	public void getContextElementForKindsAndBuildContextMap(List<String> kinds) throws LeapDataContextConfigException {
		LeapDataContextElement ctxtElement;
		JSONArray listOfLeapDataElements = null;
		String kind = null, kindLowerCase = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.descendingIterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (ctxtElement.getDataElement() instanceof InitialLeapDataContextElement) {
				continue;
			} else {
				kindLowerCase = ctxtElement.getDataElement().getData().getKind();
			}
			for (int x = 0; x < kinds.size(); x++) {
				listOfLeapDataElements = new JSONArray();
				kind = kinds.get(x);
				if (kind.contains(".")) {
					String[] split = kind.split("\\.");
					kind = split[split.length - 1];
				}
				if (kindLowerCase.equalsIgnoreCase(kind)) {
					buildContextDataMap(kind, ctxtElement, listOfLeapDataElements);
				}
			}
		}
	}

	/**
	 * Method to fetch the Context Elements for the same 'kind'
	 * 
	 * @param kind
	 * @return {@link JSONArray}
	 * @throws LeapDataContextConfigException
	 */
	public JSONArray getContextElementForKind(String kind) throws LeapDataContextConfigException {
		LeapDataContextElement ctxtElement;
		JSONArray listOfLeapDataElements = new JSONArray();
		String kindLowerCase = null;
		Object leapDataElementObject = null;
		LeapDataElement dataElement = null;

		Iterator<LeapDataContextElement> itr = dequeContextElement.descendingIterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			dataElement = ctxtElement.getDataElement();
			if (ctxtElement.getDataElement() instanceof InitialLeapDataContextElement) {
				continue;
			} else {
				kindLowerCase = ctxtElement.getDataElement().getData().getKind();
			}
			if (kindLowerCase.equalsIgnoreCase(kind)) {
				leapDataElementObject = LeapDataContextHelper.getContextElementAsJsonForKind(dataElement, kind);
				if (leapDataElementObject instanceof JSONObject) {
					listOfLeapDataElements.put(new JSONObject(leapDataElementObject.toString()));
				} else if (leapDataElementObject instanceof JSONArray) {
					JSONArray newJsonArray = new JSONArray(leapDataElementObject.toString());
					for (Object eachJsonObject : newJsonArray) {
						listOfLeapDataElements.put(new JSONObject(eachJsonObject.toString()));
					}
				}
			}
		}
		return listOfLeapDataElements;
	}

	/**
	 * 
	 * @param kind
	 * @param ctxtElement
	 * @param listOfLeapDataElements
	 * @throws LeapDataContextConfigException
	 */
	private void buildContextDataMap(String kind, LeapDataContextElement ctxtElement, JSONArray listOfLeapDataElements)
			throws LeapDataContextConfigException {
		Object leapDataElementObject = null;
		LeapDataElement dataElement = null;
		dataElement = ctxtElement.getDataElement();
		leapDataElementObject = LeapDataContextHelper.getContextElementAsJsonForKind(dataElement, kind);
		if (leapDataElementObject instanceof JSONObject) {
			listOfLeapDataElements.put(new JSONObject(leapDataElementObject.toString()));
		} else if (leapDataElementObject instanceof JSONArray) {
			JSONArray newJsonArray = new JSONArray(leapDataElementObject.toString());
			for (Object eachJsonObject : newJsonArray) {
				listOfLeapDataElements.put(new JSONObject(eachJsonObject.toString()));
			}
		}
		if (contextMap.containsKey(kind)) {
			JSONArray list = contextMap.get(kind);
			for (int x = 0; x < listOfLeapDataElements.length(); x++) {
				Object object = listOfLeapDataElements.get(x);
				JSONObject jsonObject = new JSONObject(object.toString());
				list.put(jsonObject);
			}
			contextMap.put(kind, list);
		} else
			contextMap.put(kind, listOfLeapDataElements);
	}

	/**
	 * Method to get the related MetaData of a Context Element by
	 * EffectiveColumnName
	 * 
	 * @param effectiveName
	 * @return MetaData Object
	 */
	public MetaData getMetaDataByEffectiveName(String effectiveName) {
		LeapDataContextElement ctxtElement;
		List<MetaData> metaDataList = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
			leapData = leapDataElement.getData();
			metaDataList = leapData.getMetadata();
			for (MetaData metaDataElement : metaDataList) {
				String effectiveColumnName = metaDataElement.getEffectiveColumnName();
				if (effectiveColumnName.equalsIgnoreCase(effectiveName)) {
					return metaDataElement;
				}
			}
		}
		return null;
	}

	/**
	 * Method to get the related MetaData of a Context Element by ActualColumnName
	 * 
	 * @param actualName
	 * @return MetaData Object
	 */
	public MetaData getMetaDataByActualName(String actualName) {
		LeapDataContextElement ctxtElement;
		List<MetaData> metaDataList = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
			leapData = leapDataElement.getData();
			metaDataList = leapData.getMetadata();
			for (MetaData metaDataElement : metaDataList) {
				String actualColumnName = metaDataElement.getActualColumnName();
				if (actualColumnName.equalsIgnoreCase(actualName)) {
					return metaDataElement;
				}
			}
		}
		return null;
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'tagName'
	 * 
	 * @param tagName
	 * @return List<MetaData>
	 */
	public List<MetaData> getMetaDataOfContextElementByTag(String tagName) {
		LeapDataContextElement ctxtElement;
		List<MetaData> metaDataList = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					leapData = leapDataElement.getData();
					metaDataList = leapData.getMetadata();
					return metaDataList;
				}
			}
		}
		return metaDataList;
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'tagName'
	 * and 'kind'
	 * 
	 * @param tagName
	 * @param kind
	 * @return List<MetaData>
	 */
	public List<MetaData> getMetaDataOfContextElementByTagKind(String tagName, String kind) {
		LeapDataContextElement ctxtElement;
		List<MetaData> metaDataList = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					leapData = leapDataElement.getData();
					if (leapData.getKind().equalsIgnoreCase(kind)) {
						metaDataList = leapData.getMetadata();
						return metaDataList;
					}
				}
			}
		}
		return metaDataList;
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'kind'
	 * 
	 * @param kind
	 * @return List<MetaData>
	 */
	public List<MetaData> getMetaDataOfContextElementByKind(String kind) {
		LeapDataContextElement ctxtElement;
		List<MetaData> metaDataList = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
			leapData = leapDataElement.getData();
			if (leapData.getKind().equalsIgnoreCase(kind)) {
				metaDataList = leapData.getMetadata();
				return metaDataList;
			}
		}
		return metaDataList;
	}

	/**
	 * Method to fetch the Initial Raw Request Data for a 'tag'
	 * 
	 * @param tagName
	 * @return Object
	 */
	public LeapResultSet getInitialRequestData() {
		String tagName = LeapDataContextConstant.INITIAL_CONTEXT;
		LeapDataContextElement ctxtElement;
		LeapResultSet resultSet = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					leapData = leapDataElement.getData();
					resultSet = leapData.getItems();
					return resultSet;
				}
			}
		}
		return resultSet;
	}

	/**
	 * Method to fetch the Initial Raw Request Data for a 'tag'
	 * 
	 * @param tagName
	 * @return Object
	 */
	public LeapResultSet getDataByTag(String tagName) {
		LeapDataContextElement ctxtElement;
		LeapResultSet resultSet = null;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (itr.hasNext()) {
				ctxtElement = itr.next();
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					leapData = leapDataElement.getData();
					resultSet = leapData.getItems();
					return resultSet;
				}
			}
		}
		return resultSet;
	}

	/**
	 * Method to fetch All Context Elements Stored
	 * 
	 * @return List<Object>
	 */
	public List<LeapDataElement> getAllContextElement() {
		LeapDataContextElement ctxtElement;
		List<LeapDataElement> resultSet = new ArrayList<>();
		// Map<?, ?> resultMap = new HashMap<>();
		LeapDataElement leapDataElement = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			leapDataElement = ctxtElement.getDataElement();
			resultSet.add(leapDataElement);
		}
		return resultSet;
	}

	/**
	 * Method to fetch All Context Elements Stored with Mapped 'tags'
	 * 
	 * @return List<Map<String, LeapDataElement>>
	 */
	public List<Map<String, LeapDataElement>> getAllContextElementWithMappedTag() {
		LeapDataContextElement ctxtElement;
		List<Map<String, LeapDataElement>> resultSet = new ArrayList<>();
		// Map<?, ?> resultMap = new HashMap<>();
		LeapDataElement leapDataElement = null;
		String tag = null;
		Map<String, LeapDataElement> resultMap = new HashMap<>();
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			leapDataElement = ctxtElement.getDataElement();
			tag = ctxtElement.getTagName();
			if (tag.equalsIgnoreCase("#initial") || tag.equalsIgnoreCase("#privateheader")
					|| tag.equalsIgnoreCase("#header")) {
				continue;
			} else {
				resultMap.put(tag, leapDataElement);
				resultSet.add(resultMap);
			}
		}
		return resultSet;
	}

	/**
	 * Method to get all recent/latest context elements from the Deque
	 * 
	 * @return List<Object>
	 */
	public List<JSONObject> getNormalizedContext() {
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		String tag = null;
		List<JSONObject> resultSet = new ArrayList<>();
		List<Object> tagSet = new ArrayList<>();
		Map<String, JSONObject> resultMap = new HashMap<>();
		Iterator<LeapDataContextElement> reverse = dequeContextElement.descendingIterator();
		while (reverse.hasNext()) {
			ctxtElement = reverse.next();
			leapDataElement = ctxtElement.getDataElement();
			tag = ctxtElement.getTagName();
			if (resultMap.isEmpty()) {
				JSONObject contextElementAsJson;
				contextElementAsJson = LeapDataContextHelper.getContextElementAsJson(leapDataElement, tag);
				resultMap.put(tag, contextElementAsJson);
				tagSet.add(tag);
			} else if (resultSet.contains(leapDataElement)) {
				JSONObject contextElementAsJson = LeapDataContextHelper.getContextElementAsJson(leapDataElement, tag);
				resultMap.remove(contextElementAsJson);
				resultMap.put(tag, contextElementAsJson);
				tagSet.add(tag);
			} else if (tagSet.contains(tag)) {
				JSONObject contextElementAsJson = LeapDataContextHelper.getContextElementAsJson(leapDataElement, tag);
				resultMap.replace(tag, contextElementAsJson);
			} else {
				JSONObject contextElementAsJson = LeapDataContextHelper.getContextElementAsJson(leapDataElement, tag);
				resultMap.put(tag, contextElementAsJson);
				tagSet.add(tag);
			}
		}
		resultSet = new ArrayList<>(resultMap.values());
		return resultSet;
	}

	/**
	 * Method to get the request headers based on the 'tagName'
	 * 
	 * @param tagName
	 * @return {@link JSONObject}
	 */
	public JSONObject getRequestHeaders(String tagName) {
		String methodName = "getRequestHeaders";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContextElement ctxtElement;
		InitialLeapDataContextElement initialLeapDataContextElement;
		JSONObject headers = null;
		Iterator<LeapDataContextElement> reverse = dequeContextElement.descendingIterator();
		logger.trace("{} allTagElements we have added into the deque ::: {} ", LEAP_LOG_KEY, allElementTagsList);
		logger.trace("{} allTagElements we have added into the deque size ::: {} ", LEAP_LOG_KEY,
				allElementTagsList.size());
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (reverse.hasNext()) {
				ctxtElement = reverse.next();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					initialLeapDataContextElement = (InitialLeapDataContextElement) ctxtElement.getDataElement();
					headers = new JSONObject(initialLeapDataContextElement.getRequestHeaderElement());
					return headers;
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return headers;
	}

	/**
	 * Method to get the request headers based on the 'tagName'
	 * 
	 * @param tagName
	 * @return {@link JSONObject}
	 */
	public Map<String, Object> getRequestMapHeaders(String tagName) {
		String methodName = "getRequestMapHeaders";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContextElement ctxtElement;
		Map<String, Object> requestHeaderElement = null;
		InitialLeapDataContextElement initialLeapDataContextElement;
		Iterator<LeapDataContextElement> reverse = dequeContextElement.descendingIterator();
		logger.trace("{} allTagElements we have added into the deque ::: {} ", LEAP_LOG_KEY, allElementTagsList);
		logger.trace("{} allTagElements we have added into the deque size ::: {} ", LEAP_LOG_KEY,
				allElementTagsList.size());
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			while (reverse.hasNext()) {
				ctxtElement = reverse.next();
				if (ctxtElement.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
					initialLeapDataContextElement = (InitialLeapDataContextElement) ctxtElement.getDataElement();
					requestHeaderElement = initialLeapDataContextElement.getRequestHeaderElement();
					return requestHeaderElement;
				}
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return requestHeaderElement;
	}

	/**
	 * Method to get the private headers based on the 'tagName'
	 * 
	 * // TODO Need to make the access to this method and data as 'restricted'
	 * 
	 * @param tagName
	 * @return {@link JSONObject}
	 */
	public JSONObject getPrivateHeaders(String tagName) {
		LeapDataContextElement ctxtElement;
		InitialLeapDataContextElement initialLeapDataContextElement;
		JSONObject headers = null;
		if (allElementTagsList.contains(tagName.toLowerCase())) {
			ctxtElement = dequeContextElement.peekLast();
			initialLeapDataContextElement = (InitialLeapDataContextElement) ctxtElement.getDataElement();
			headers = new JSONObject(initialLeapDataContextElement.getPrivateHeaderElement());
			return headers;
		}
		return headers;
	}

	/**
	 * Method to get the count of row data present inside the DataSet Object stored
	 * inside the context element
	 * 
	 * @param tagName
	 * @return Integer value
	 */
	public int getRowCountForResponseDataSet(String tagName) {
		int count = 0;
		LeapResultSet resultsetItems;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tagName.toLowerCase())) {
				if (ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tagName.toLowerCase())) {
					leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
					leapData = leapDataElement.getData();
					resultsetItems = leapData.getItems();
					Object resultSet = resultsetItems.getData();
					if (resultSet instanceof JSONArray) {
						JSONArray dataArray = (JSONArray) resultSet;
						return dataArray.length();
					}
				}
			}
		}
		return count;
	}

	/**
	 * Method to determine the instance of the request/response data stored inside
	 * the context element
	 * 
	 * @param tagName
	 * @return Class reference
	 */
	public Class<? extends Object> getContextElementDataType(String tagName) {
		LeapResultSet items;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tagName.toLowerCase())) {
				if (ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tagName.toLowerCase())) {
					leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
					leapData = leapDataElement.getData();
					items = leapData.getItems();
					Object data = items.getData();
					if (items.getType().equalsIgnoreCase("MetaModelDataSet")) {
						return data.getClass();
					} else if (items.getType().equalsIgnoreCase("JSON")) {
						return data.getClass();
					} else
						return data.getClass();
				}
			}
		}
		return null;
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element
	 * 
	 * @param key
	 * @return Object
	 */
	public Object getDataElementFromResponse(String tag, String... key) {
		LeapResultSet resultsetItems;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		String kayValue = null;
		// List<String> listOfValues = new ArrayList<>();
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tag.toLowerCase())
					&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tag.toLowerCase())) {
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				leapData = leapDataElement.getData();
				resultsetItems = leapData.getItems();
				Object resultSet = resultsetItems.getData();
				JSONObject resultSetJson;
				if (resultSet instanceof JSONObject) {
					// kayValue = ((JSONObject) resultSet).get(key);
					// logger.debug("value : " + kayValue);
					resultSetJson = new JSONObject(resultSet.toString());
					List<String> valueFromJson = LeapDataContextHelper.getValuesFromJson(resultSetJson, key);
					return valueFromJson;
				} /*
					 * else if (resultSet instanceof java.lang.String) { resultSetJson =
					 * XML.toJSONObject(resultSet.toString()); logger.debug("resultSetJson : " +
					 * resultSetJson); // resultSetJson = resultSetJson.getJSONObject("root");
					 * Iterator<String> keys = resultSetJson.keys(); while (keys.hasNext()) { String
					 * jsonkey = keys.next(); logger.debug("jsonKey : " + jsonkey); // if
					 * (jsonkey.equalsIgnoreCase(key)) { kayValue = resultSetJson.get(jsonkey);
					 * logger.debug("kayValue : " + kayValue); long start =
					 * System.currentTimeMillis(); List<String> valueFromJson =
					 * LeapDataContextHelper.getValueFromJson(resultSetJson, key);
					 * System.out.println("Time : " + (System.currentTimeMillis() - start));
					 * logger.debug("valueFromJson : " + valueFromJson); if (!(valueFromJson.size()
					 * > 1)) { kayValue = valueFromJson.get(0); } else { kayValue = valueFromJson; }
					 * // } } return kayValue; }
					 */
			}
		}
		return kayValue;
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element using JSONPath
	 * 
	 * @param tag
	 * @param key
	 * @return
	 */
	public Object getDataElementFromResponseUsingJSONPath(String tag, String... key) {
		LeapResultSet resultsetItems;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		String kayValue = null;
		// List<String> listOfValues = new ArrayList<>();
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tag.toLowerCase())
					&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tag.toLowerCase())) {
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				leapData = leapDataElement.getData();
				resultsetItems = leapData.getItems();
				Object resultSet = resultsetItems.getData();
				JSONObject resultSetJson;
				if (resultSet instanceof JSONObject) {
					resultSetJson = new JSONObject(resultSet.toString());
					List<String> valueFromJson = LeapDataContextHelper.getValuesFromJson(resultSetJson, key);
					return valueFromJson;
				} /*
					 * else if (resultSet instanceof java.lang.String) { resultSetJson =
					 * XML.toJSONObject(resultSet.toString()); logger.debug("resultSetJson : " +
					 * resultSetJson); // resultSetJson = resultSetJson.getJSONObject("root");
					 * Iterator<String> keys = resultSetJson.keys(); while (keys.hasNext()) { String
					 * jsonkey = keys.next(); logger.debug("jsonKey : " + jsonkey); // if
					 * (jsonkey.equalsIgnoreCase(key)) { kayValue = resultSetJson.get(jsonkey);
					 * logger.debug("kayValue : " + kayValue); long start =
					 * System.currentTimeMillis(); List<String> valueFromJson =
					 * LeapDataContextHelper.getValueFromJson(resultSetJson, key);
					 * System.out.println("Time : " + (System.currentTimeMillis() - start));
					 * logger.debug("valueFromJson : " + valueFromJson); if (!(valueFromJson.size()
					 * > 1)) { kayValue = valueFromJson.get(0); } else { kayValue = valueFromJson; }
					 * // } } return kayValue; }
					 */
			}
		}
		return kayValue;
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element using XPath
	 * 
	 * @param tag
	 * @param key
	 * @return {@link Object}
	 * @throws LeapDataContextConfigException
	 */
	public Object getDataElementFromResponseUsingXPath(String tag, String key) throws LeapDataContextConfigException {
		LeapResultSet resultsetItems;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		String kayValue = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tag.toLowerCase())
					&& ctxtElement.getTagName().equalsIgnoreCase(tag.toLowerCase())) {
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				leapData = leapDataElement.getData();
				resultsetItems = leapData.getItems();
				Object resultSet = resultsetItems.getData();
				JSONObject resultSetJson;
				if (resultSet instanceof JSONObject) {
					resultSetJson = new JSONObject(resultSet.toString());
					String xml = XML.toString(resultSetJson);
					List<String> valueFromXml = LeapDataContextHelper.getValuesFromXml(xml, key);
					JSONObject jsonValue = XML.toJSONObject(valueFromXml.toString());
					return jsonValue;
				}
			}
		}
		return kayValue;
	}

	/**
	 * Method to get the list of data elements from the response
	 * 
	 * @param tag
	 * @param key
	 * @return {@link Object}
	 */
	public Object getListOfDataElementFromResponse(String tag, String... key) {
		LeapResultSet resultsetItems;
		LeapDataContextElement ctxtElement;
		LeapDataElement leapDataElement = null;
		LeapData leapData = null;
		String kayValue = null;
		Iterator<LeapDataContextElement> itr = dequeContextElement.iterator();
		while (itr.hasNext()) {
			ctxtElement = itr.next();
			if (allElementTagsList.contains(tag.toLowerCase())
					&& ctxtElement.getTagName().toLowerCase().equalsIgnoreCase(tag.toLowerCase())) {
				leapDataElement = (LeapDataElement) ctxtElement.getDataElement();
				leapData = leapDataElement.getData();
				resultsetItems = leapData.getItems();
				Object resultSet = resultsetItems.getData();
				JSONObject resultSetJson;
				if (resultSet instanceof JSONObject) {
					resultSetJson = new JSONObject(resultSet.toString());
					List<String> valueFromJson = LeapDataContextHelper.getValuesFromJson(resultSetJson, key);
					return valueFromJson;
				}
			}
		}
		return kayValue;
	}

	/**
	 * Method to get the LDC Response as Normalized Context based on the Template
	 * 
	 * @param fileName
	 * @return {@link JSONArray}
	 * @throws LeapDataContextInitialzerException
	 * @throws Exception
	 */
	public Object getNormalizedContextUsingTemplate(String fileName, String projectionSource, String projectionFileName,
			String taxonomyId) throws LeapDataContextInitialzerException {
		long start = System.currentTimeMillis();
		String methodName = "getNormalizedContextUsingTemplate";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace("{} fileName : {}, projectionSource : {}, projectionFileName : {},  taxonomyId: {} ", LEAP_LOG_KEY,
				fileName, projectionSource, projectionFileName, taxonomyId);
		// logger.debug("DEQUE : " + dequeContextElement);
		JSONArray finalResponseArray = new JSONArray();
		try {
			vendorTaxonomyId = taxonomyId;

			String templateStr = LeapDataContextHelper.getFile(fileName, LeapDataContextConstant.TEMPLATE_KEY,
					taxonomyId, this);
			logger.debug(
					"{} templateStr in getNormalizedContextUsingTemplate the projection and template is true::: {} ",
					LEAP_LOG_KEY, templateStr);
			String prjectionStr = LeapDataContextHelper.getFile(projectionFileName,
					LeapDataContextConstant.PROJECTION_KEY, taxonomyId, this);

			JSONObject templateJson = new JSONObject(templateStr);
			logger.debug(
					"{} templateJson in getNormalizedContextUsingTemplate the projection and template is true::: {} ",
					LEAP_LOG_KEY, templateJson);
			logger.trace("{} json schema object  value before getting from template :: {} ", LEAP_LOG_KEY, jsonSchema);
			jsonSchema = templateJson.getJSONObject("schema");
			logger.debug("{} json schema object  value after getting from template :: {} ", LEAP_LOG_KEY,
					jsonSchema.toString());

			try {
				switch (projectionSource) {
				case "swagger":
					if (projectionFileName != null && !projectionFileName.isEmpty()) {
						JSONObject requestHeaders = this.getRequestHeaders(HEADER);
						logger.trace("{} requestHeaders in getNormalizedContextUsingTemplate for projection :: {} ",
								LEAP_LOG_KEY, requestHeaders);
						String reqUrl = "/" + requestHeaders.get(FEATUREGROUP) + "/" + requestHeaders.get(FEATURENAME)
								+ "/" + requestHeaders.get(SERVICENAME);
						logger.debug("{} reqUrl in getNormalizedContextUsingTemplate for projection :: {}",
								LEAP_LOG_KEY, reqUrl);
						// get the projection elements
						JSONObject swaggerJson = new JSONObject(prjectionStr);
						logger.debug("{} swaggerJson in getNormalizedContextUsingTemplate :: {} ", LEAP_LOG_KEY,
								swaggerJson);
						JSONObject projectionObject = swaggerJson.getJSONObject("paths").getJSONObject(reqUrl)
								.getJSONObject(requestHeaders.getString(REQUEST_METHOD).toLowerCase());
						String projection = projectionObject.getString("x-leapProjection");
						if (projectionObject.has("x-flattenedleapProjection")) {
							String flattenedProjectionString = projectionObject.getString("x-flattenedleapProjection");
							flattenedrojectionArr = new JSONArray(flattenedProjectionString);
							logger.trace("flattenedProjectionString ::" + flattenedProjectionString);
						}
						JSONArray templateData = new JSONArray(projection);
						logger.debug("{} templateData in getNormalizedContextUsingTemplate :: {} ", LEAP_LOG_KEY,
								templateData);
						logger.trace(
								"{} projectionsArr in getNormalizedContextUsingTemplate before initializng the value as templateData:: {} ",
								LEAP_LOG_KEY, projectionsArr);
						projectionsArr = templateData;
						logger.debug(
								"{} projectionsArr in getNormalizedContextUsingTemplate after initializng the value as templateData:: {} ",
								LEAP_LOG_KEY, projectionsArr);
					}
					break;
				case "jsonSchema":
					if (projectionFileName != null && !projectionFileName.isEmpty()) {
						JSONObject projJson = new JSONObject(prjectionStr);
						projectionsArr = new JSONArray();
						generateProjectionForRequiredOnly(projJson, "");
						logger.debug("{} projectionsArr :: {} ", LEAP_LOG_KEY, projectionsArr);
					}
					break;
				default:
					break;
				}

			} catch (Exception e) {
				logger.error("{} Unable to find the 'projection' in  file {},will try to search in template ...! ",
						LEAP_LOG_KEY, prjectionStr, fileName, e);
				if (templateJson.has("projection")) {
					JSONArray templateData = templateJson.getJSONArray("projection");
					projectionsArr = templateData;
				}
				throw new LeapDataContextInitialzerException(
						"Unable to find the 'projection' in  file " + "prjectionStr", e, "Resource Not Found", 404);
			}

			String structuralType = templateJson.getString("structuralType");

			String contextType = null;
			if (templateJson.has("Content-Type")) {
				contextType = templateJson.getString("Content-Type");
			}

			if (structuralType.equalsIgnoreCase("Nested")) {
				finalResponseArray = buildNormalizedContext();
			} else if (structuralType.equalsIgnoreCase("Flattened")) {
				finalResponseArray = buildFlattenedStructure();
			}

			if (!(contextType == null) && contextType.equalsIgnoreCase("application/xml")) {
				logger.debug("{} Time taken to build structure using template : {} ", LEAP_LOG_KEY,
						(System.currentTimeMillis() - start));
				return XML.toString(finalResponseArray).replaceAll("<array>", "").replaceAll("</array>", "");
			}
		} catch (Exception e) {
			logger.error("Unable to apply template...! " + e);
			throw new LeapDataContextInitialzerException("Unable to apply template...! ", e, "Invalid Request!", 400);
		}
		logger.debug("{} Time taken to build structure using template : {} ", LEAP_LOG_KEY,
				(System.currentTimeMillis() - start));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalResponseArray;
	}

	/**
	 * Method apply projection but no taxonomyId
	 * 
	 * 
	 * @param fileName
	 * @return {@link JSONArray}
	 * @throws LeapDataContextInitialzerException
	 * @throws Exception
	 */
	public Object getNormalizedContext(String projectionSource, String projectionFileName)
			throws LeapDataContextInitialzerException {
		String methodName = "getNormalizedContext";
		logger.debug("{} entered into the method {}, projectionFileName={} ", LEAP_LOG_KEY, methodName,
				projectionFileName);
		long start = System.currentTimeMillis();
		JSONArray finalResponseArray = new JSONArray();
		try {
			taxonomyRequired = false;
			logger.debug("{} vendorTaxonomyId is : {}", LEAP_LOG_KEY, vendorTaxonomyId);
			if (projectionSource != null && projectionFileName != null) {
				logger.trace("projectionFileName: {}", LEAP_LOG_KEY, projectionFileName);
				String prjectionStr = LeapDataContextHelper.getFile(projectionFileName,
						LeapDataContextConstant.PROJECTION_KEY, vendorTaxonomyId, this);

				try {
					switch (projectionSource) {
					case "swagger":
						if (projectionFileName != null && !projectionFileName.isEmpty()) {
							JSONObject requestHeaders = this.getRequestHeaders(HEADER);
							String reqUrl = "/" + requestHeaders.get(FEATUREGROUP) + "/"
									+ requestHeaders.get(FEATURENAME) + "/" + requestHeaders.get(SERVICENAME);
							// get the projection elements
							JSONObject swaggerJson = new JSONObject(prjectionStr);

							String projection = swaggerJson.getJSONObject("paths").getJSONObject(reqUrl)
									.getJSONObject(requestHeaders.getString(REQUEST_METHOD).toLowerCase())
									.getString("x-leapProjection");
							JSONArray templateData = new JSONArray(projection);
							projectionsArr = templateData;
						}
						break;
					case "jsonSchema":
						if (projectionFileName != null && !projectionFileName.isEmpty()) {
							JSONObject projJson = new JSONObject(prjectionStr);
							projectionsArr = new JSONArray();
							generateProjectionForRequiredOnly(projJson, "");
						}
						break;
					default:
						break;

					}
					logger.debug("{} projectionsArr is : {}", LEAP_LOG_KEY, projectionsArr);
				} catch (Exception e) {
					throw new LeapDataContextInitialzerException(
							"Unable to find the 'projection' in file " + "prjectionStr", e, "Resource Not Found", 404);
				}
			} else {
				throw new LeapDataContextInitialzerException("Unable to apply projection...! ", new Exception(),
						"projection source or projection is null", 400);
			}
			logger.debug("{} allElementTagsList is : {}", LEAP_LOG_KEY, allElementTagsList);
			LeapDataElement contextElement = getContextElement(allElementTagsList.get(allElementTagsList.size() - 1));
			if (contextElement != null) {
				LeapResultSet items = contextElement.getData().getItems();
				Object data = items.getData();
				JSONObject dataJson;
				if (((String) data).startsWith("<")) {
					data = XML.toJSONObject((String) data);
					dataJson = new JSONObject(data.toString());
					dataJson = new JSONObject().put("root", dataJson);
				} else {
					dataJson = new JSONObject().put("root", new JSONObject(data.toString()));
				}
				logger.trace("{} data from LDC is : {}", LEAP_LOG_KEY, dataJson);
				setTaxonomyJSON(LeapDataContextConstant.LEAPDEFAULT_TAXONOMY);
				// convert the data into leapDefault taxonomy before applying
				// projection or taxonomy
				dataJson = applyTaxonomy(dataJson, new JSONObject());
				// applies projections and does not apply taxonomy because
				// taxonomyRequired is false.
				finalResponseArray = buildNestedStructure(projectionsArr, dataJson);
			}
			logger.info("{} finalJsonResponse is : {}", LEAP_LOG_KEY, finalResponseArray);

		} catch (Exception e) {
			throw new LeapDataContextInitialzerException("Unable to apply template...! ", e, "Invalid Request!", 400);
		}
		logger.debug("{} Time taken to build structure using template : {}", LEAP_LOG_KEY,
				(System.currentTimeMillis() - start));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalResponseArray;
	}

	/**
	 * Apply only taxonomy not projection . If taxonomyId is null, vendor taxonomyId
	 * is applied
	 * 
	 * @param taxonomyId
	 * @return
	 * @throws LeapDataContextInitialzerException
	 */
	public Object getNormalizedContext(String taxonomyId) throws LeapDataContextInitialzerException {
		String methodName = "getNormalizedContext";
		logger.debug("{} entered into the method {}, taxonomyId={} ", LEAP_LOG_KEY, methodName, taxonomyId);
		long start = System.currentTimeMillis();
		JSONArray finalResponseArray = new JSONArray();
		try {
			if (taxonomyId == null) {
				featureDeployment = initializeConfigNodeId();
				taxonomyId = featureDeployment.getVendorTaxonomyId();
			}
		} catch (Exception e) {
			logger.warn("{} no taxonomy  found ....! {}", LEAP_LOG_KEY, e.getMessage(), e);
		}

		try {
			taxonomyRequired = true;
			vendorTaxonomyId = taxonomyId;
			logger.debug("{} allElementTagsList is : {} ", LEAP_LOG_KEY, allElementTagsList);
			LeapDataElement contextElement = getContextElement(allElementTagsList.get(allElementTagsList.size() - 1));
			if (contextElement != null) {
				LeapResultSet items = contextElement.getData().getItems();
				Object data = items.getData();
				JSONObject finalResponse;
				JSONObject dataJson;
				if (data instanceof String) {
					if (((String) data).startsWith("<")) {
						data = XML.toJSONObject((String) data);
						dataJson = new JSONObject(data.toString());
						dataJson = new JSONObject().put("root", dataJson);
					} else {
						dataJson = new JSONObject().put("root", new JSONObject(data.toString()));
					}
				} else if (data instanceof JSONObject) {
					dataJson = new JSONObject().put("root", new JSONObject(data.toString()));
				} else {
					dataJson = new JSONObject().put("root", new JSONArray(data.toString()));
				}
				logger.trace("{} data from LDC is : {] ", LEAP_LOG_KEY, dataJson);
				setTaxonomyJSON(LEAPDEFAULT_TAXONOMY);
				// convert the data into Whereworks taxonomy before applying
				// projection or taxonomy
				dataJson = applyTaxonomy(dataJson, new JSONObject());
				setTaxonomyJSON(vendorTaxonomyId);
				// applies vendor taxonomy Id
				finalResponse = applyTaxonomy(dataJson, new JSONObject());
				finalResponseArray.put(finalResponse);
			}
			logger.info("{} finalJsonResponse is : {}", LEAP_LOG_KEY, finalResponseArray);
		} catch (Exception e) {
			logger.error("Unable to apply template...! " + e);
			throw new LeapDataContextInitialzerException("Unable to apply template...! ", e, "Invalid Request!", 400);
		}
		logger.debug("{} Time taken to build structure using template : {} ", LEAP_LOG_KEY,
				(System.currentTimeMillis() - start));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalResponseArray;
	}

	/**
	 * both projection and taxonomy is applied . All arguments should not be null or
	 * empty.
	 * 
	 * @param projectionSource
	 * @param projectionFileName
	 * @param taxonomyId
	 * @return
	 * @throws LeapDataContextInitialzerException
	 */
	public Object getNormalizedContext(String projectionSource, String projectionFileName, String taxonomyId)
			throws LeapDataContextInitialzerException {
		String methodName = "getNormalizedContext";
		logger.debug("{} entered into the method {}, projectionFileName={}, taxonomyId={}", LEAP_LOG_KEY, methodName,
				projectionFileName, taxonomyId);
		long start = System.currentTimeMillis();
		JSONArray finalResponseArray = new JSONArray();
		try {
			if (LeapDataContextHelper.checkForNullAndEmpty(taxonomyId)
					&& LeapDataContextHelper.checkForNullAndEmpty(projectionFileName)
					&& LeapDataContextHelper.checkForNullAndEmpty(projectionSource)) {
				throw new LeapDataContextInitialzerException("Unable to apply template...! ", new Exception(),
						"taxonomyId || projectionSource || projectionFileName is null || empty", 400);
			} else {
				logger.trace("taxonomyId : {} ", LEAP_LOG_KEY, taxonomyId);
				taxonomyRequired = true;
				vendorTaxonomyId = taxonomyId;
				logger.trace("{} projectionFileName: {} ", LEAP_LOG_KEY, projectionFileName);
				String prjectionStr = LeapDataContextHelper.getFile(projectionFileName,
						LeapDataContextConstant.PROJECTION_KEY, taxonomyId, this);
				try {
					switch (projectionSource) {
					case "swagger":

						JSONObject requestHeaders = this.getRequestHeaders(HEADER);
						String reqUrl = "/" + requestHeaders.get(FEATUREGROUP) + "/" + requestHeaders.get(FEATURENAME)
								+ "/" + requestHeaders.get(SERVICENAME);
						// get the projection elements
						JSONObject swaggerJson = new JSONObject(prjectionStr);

						String projection = swaggerJson.getJSONObject("paths").getJSONObject(reqUrl)
								.getJSONObject(requestHeaders.getString(REQUEST_METHOD).toLowerCase())
								.getString("x-leapProjection");
						JSONArray templateData = new JSONArray(projection);
						projectionsArr = templateData;

						break;
					case "jsonSchema":

						JSONObject projJson = new JSONObject(prjectionStr);
						projectionsArr = new JSONArray();
						generateProjectionForRequiredOnly(projJson, "");

						break;
					default:
						break;

					}
					logger.debug("{} projectionsArr is : {}", LEAP_LOG_KEY, projectionsArr);
				} catch (Exception e) {
					throw new LeapDataContextInitialzerException(
							"Unable to find the 'projection' in file " + "prjectionStr", e, "Resource Not Found", 404);
				}
				LeapDataElement contextElement = getContextElement(
						allElementTagsList.get(allElementTagsList.size() - 1));
				if (contextElement != null) {
					LeapResultSet items = contextElement.getData().getItems();
					Object data = items.getData();
					logger.debug("{} data in getNormalizedContext applying projection and taxonomy :: {}", LEAP_LOG_KEY,
							data);
					JSONObject dataJson;
					if (data instanceof String) {
						if (((String) data).trim().startsWith("<")) {
							data = XML.toJSONObject((String) data);
							dataJson = new JSONObject(data.toString());
							dataJson = new JSONObject().put("root", dataJson);
						} else if (((String) data).trim().startsWith("{")) {
							dataJson = new JSONObject().put("root", new JSONObject(data.toString()));
						} else {
							dataJson = new JSONObject().put("root", new JSONArray(data.toString()));
						}
					} else if (data instanceof JSONObject) {
						dataJson = new JSONObject().put("root", data);
					} else {
						dataJson = new JSONObject().put("root", new JSONArray(data.toString()));
					}
					logger.trace("{} data from LDC is : {}", LEAP_LOG_KEY, dataJson);
					setTaxonomyJSON(LEAPDEFAULT_TAXONOMY);
					// convert the data into Whereworks taxonomy before applying
					// projection or taxonomy
					dataJson = applyTaxonomy(dataJson, new JSONObject());
					logger.debug("{} data after applying whereworks taxonomyId : {} ", LEAP_LOG_KEY, dataJson);
					setTaxonomyJSON(vendorTaxonomyId);
					// applies projection and vendor taxonomy
					finalResponseArray = buildNestedStructure(projectionsArr, dataJson);
				}
				logger.info("{} finalJsonResponse is : {}", LEAP_LOG_KEY, finalResponseArray);
			}
		} catch (Exception e) {
			throw new LeapDataContextInitialzerException("Unable to apply template...! " + e.getMessage(), e,
					"Invalid Request!", 400);
		}
		logger.debug("{} Time taken to build structure using template : {}", LEAP_LOG_KEY,
				(System.currentTimeMillis() - start));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalResponseArray;
	}

	/**
	 * this method is used to get the vendorTaxonomyId. If vendor taxonomy is null,
	 * return taxonomyId present in request. If it is null return default
	 * taxonomyId.
	 * 
	 * @return String
	 * @throws FeatureDeploymentServiceException
	 */
	public void getVendorTaxonomyId(Exchange exchange) throws FeatureDeploymentServiceException {
		String methodName = "getVendorTaxonomyId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapserviceContext = leapDataContext.getServiceDataContext();
		JSONObject requestHeaders = this.getRequestHeaders(LeapDataContextConstant.HEADER);
		String provider = null;
		if (exchange.getIn().getHeaders().containsKey(PROVIDER)) {
			provider = (String) exchange.getIn().getHeader(PROVIDER);
			logger.trace("{} provider is {} ", LEAP_LOG_KEY, provider);
		}
		FeatureDeployment featureDeployment = null;
		String taxonomyId = null;
		if (requestHeaders != null) {
			String tenantId = requestHeaders.getString(LeapDataContextConstant.TENANTID);
			String siteId = requestHeaders.getString(LeapDataContextConstant.SITEID);
			String featureName = requestHeaders.getString(LeapDataContextConstant.FEATURENAME);
			if (provider != null) {
				featureDeployment = featureDeploymentService.getActiveAndPrimaryFeatureDeployedFromCache(tenantId,
						siteId, featureName, provider, leapserviceContext);
			} else {
				logger.debug("{} provider is Null ", LEAP_LOG_KEY);

				featureDeployment = (FeatureDeployment) exchange.getIn().getHeader(FEATURE_DEPLOYMENT);
				logger.trace("{} featureDeployment :: {}", LEAP_LOG_KEY, featureDeployment);
			}

			if (featureDeployment != null) {
				taxonomyId = featureDeployment.getVendorTaxonomyId();
				if (taxonomyId == null) {
					taxonomyId = getTaxonomyIdFromRequest();
				}
			} else {
				taxonomyId = getTaxonomyIdFromRequest();
			}

			if (taxonomyId == null) {
				taxonomyId = LEAPDEFAULT_TAXONOMY;
			}
		} else {
			taxonomyId = LEAPDEFAULT_TAXONOMY;
		}
		leapserviceContext.setVendorTaxonomyId(taxonomyId);
		exchange.getIn().setHeader(LeapDataContextConstant.TAXONOMY_ID_INHEADER, taxonomyId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		// return taxonomyId;

	}

	/**
	 * get the data from the LDC from the list of keys. Sets the Key and Value map
	 * in header.
	 * 
	 * @param keysList
	 * @param exchange
	 * @throws Exception
	 */
	public void getDataFromLDC(List<String> keysList, Exchange exchange) throws Exception {
		String methodName = "getDataFromLDC";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			setTaxonomyJSON(LEAPDEFAULT_TAXONOMY);
			Map<String, Object> LdcDataMap = new HashMap<String, Object>();
			// rootAndKeysMap contains the root as key and and list of keys under that root
			// as
			// Value.
			Map<String, List<String>> rootAndKeysMap = splitKeysIntoRootTagAndKey(keysList);
			// Set of all roots
			Set<String> rootSet = rootAndKeysMap.keySet();
			LeapDataContextElement ldcElement = null;
			Iterator<LeapDataContextElement> ldcElements = dequeContextElement.descendingIterator();
			while (ldcElements.hasNext()) {
				ldcElement = ldcElements.next();
				logger.debug("{} ldcElement is : {} ", LEAP_LOG_KEY, ldcElement);
				LeapData leapData = ldcElement.getDataElement().getData();
				if (leapData != null) {
					LeapResultSet leapResultSet = leapData.getItems();
					Object dataInFormat = leapResultSet.getData();
					JSONObject jsonData = null;
					if (dataInFormat instanceof JSONObject) {
						logger.trace("{} dataInFormat is JSONObject", LEAP_LOG_KEY);
						jsonData = (JSONObject) dataInFormat;
					} else if (dataInFormat instanceof String) {
						if (((String) dataInFormat).startsWith("<")) {
							logger.trace("dataInFormat is XML", LEAP_LOG_KEY);
							jsonData = XML.toJSONObject((String) dataInFormat);
						} else if (((String) dataInFormat).startsWith("{")) {
							jsonData = new JSONObject((String) dataInFormat);
						} else {
							exchange.getIn().setHeader(LDC_DATA_MAP, LdcDataMap);
							return;
						}
					} else {
						exchange.getIn().setHeader(LDC_DATA_MAP, LdcDataMap);
						return;
					}
					logger.debug("{} ldc data is : {}", LEAP_LOG_KEY, jsonData);
					// keySet is a set of root keys in the LeapData
					Set<String> keySet = jsonData.keySet();
					for (String rootKey : keySet) {
						logger.debug("{} root key is : {}", LEAP_LOG_KEY, rootKey);
						// converting the root key into the DefaultLeap taxonomy.
						String rootkeyInDefault = getVendorTaxonomy(rootKey);
						// checking if rootkeyInDefault is matching with the any root in the keysList
						if (rootSet.contains(rootkeyInDefault)) {
							logger.trace(" {} rootkeyInDefault is : {} ", LEAP_LOG_KEY, rootkeyInDefault);
							JSONObject dataJson = new JSONObject().put("root", jsonData);
							// if taxonomy is null or empty then LeapData is already in LeapDefault
							// taxonomy. Otherwise apply LeapDefault taxonomy.
							if (leapData.getTaxonomyId() != null && !leapData.getTaxonomyId().isEmpty())
								dataJson = applyTaxonomy(dataJson, new JSONObject());
							logger.debug("{} after applying Default taxonomy : {}", LEAP_LOG_KEY, dataJson);
							// apply jsonPath(which is a value of rootAndKeysMap ex : Order.OrderNum) on
							// dataJson and get the value and put in the LdcDataMap
							getValueByApplyingJSONPath(rootAndKeysMap, LdcDataMap, rootkeyInDefault, dataJson);
						}
					}

				}
			}
			logger.trace("LdcDataMap is : ", LEAP_LOG_KEY, LdcDataMap);
			exchange.getIn().setHeader(LDC_DATA_MAP, LdcDataMap);
		} catch (Exception e) {
			throw new LeapDataContextInitialzerException("Unable get Data form LDC...! ", e,
					"Invalid Keys!:" + e.getMessage(), 400);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * get values from Json by applying jsonpath.
	 * 
	 * @param rootAndKeysMap
	 * @param ldcDataMap
	 * @param rootkeyInDefault
	 * @param dataJson
	 */
	private void getValueByApplyingJSONPath(Map<String, List<String>> rootAndKeysMap, Map<String, Object> ldcDataMap,
			String rootkeyInDefault, JSONObject dataJson) {
		dataJson = dataJson.getJSONObject("root");
		// for Key Order we will get values like [OderNum , OrderLine.ItemNum] which we
		// are considering as jsonPath
		List<String> jsonPathList = rootAndKeysMap.get(rootkeyInDefault);
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(dataJson.toString());
		for (String jsonPath : jsonPathList) {
			jsonPath = rootkeyInDefault + "." + jsonPath;
			Object value = JsonPath.read(document, jsonPath);
			logger.debug("{} json path is : {}, value is : {} ", LEAP_LOG_KEY, jsonPath, value);
			ldcDataMap.put(jsonPath, value);

		}

	}

	/**
	 * converting the keysList into map with key as root tag and value as keys under
	 * the root tag.
	 * 
	 * @param keysList
	 * @return
	 */
	private static Map<String, List<String>> splitKeysIntoRootTagAndKey(List<String> keysList) {
		Map<String, List<String>> rootTagAndKeysMap = new HashMap<String, List<String>>();
		// Splitting the keysList into root and the corresponding keys. Ex:
		// [Order.OrderNum, Order.OrderLine.ItemNum] is converted into an entry in map
		// with key as Order and value is [OrderNum, OrderLine.ItemNum]
		if (keysList != null) {
			for (String key : keysList) {
				if (key.contains(".")) {
					String[] split = key.split("\\.", 2);
					if (rootTagAndKeysMap.containsKey(split[0])) {
						List<String> list = rootTagAndKeysMap.get(split[0]);
						list.add(split[1]);
					} else {
						List<String> list = new ArrayList<>();
						list.add(split[1]);
						rootTagAndKeysMap.put(split[0], list);
					}

				}
			}
		}
		logger.debug("{} rootTagAndKeysMap is : {}", LEAP_LOG_KEY, rootTagAndKeysMap);
		return rootTagAndKeysMap;
	}

	/**
	 * this return taxonomyId from the request . if not present in request returns
	 * default taxonomyId.
	 * 
	 * @return taxonomyId
	 */
	public String getTaxonomyId(Exchange exchange) {
		String taxonomyId = getTaxonomyIdFromRequest();
		if (taxonomyId == null) {
			taxonomyId = LEAPDEFAULT_TAXONOMY;
		}
		LeapDataContext leapDataContext = exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
				LeapDataContext.class);
		leapDataContext.getServiceDataContext().setTaxonomyId(taxonomyId);
		exchange.getIn().setHeader(LeapDataContextConstant.TAXONOMY_ID_INHEADER, taxonomyId);
		return taxonomyId;

	}

	/**
	 * this return taxonomyId from the request . if not present in request returns
	 * null.
	 * 
	 * @return taxonomyId
	 */
	private String getTaxonomyIdFromRequest() {
		try {
			LeapDataElement requestContextElement = getContextElement(INITIAL_CONTEXT);
			LeapResultSet items = requestContextElement.getData().getItems();
			Object data = items.getData();
			JSONObject requestData = new JSONObject(data.toString());
			logger.debug("{} request object is : {} ", LEAP_LOG_KEY, requestData.toString());
			String taxonomy_id = requestData.getJSONObject(REQUEST_LEAP_lOCAL).getJSONObject(REQUEST_TAXONOMY)
					.getString(REQUEST_TAXONOMY_ID);
			logger.trace("{} getTaxonomyIdFromRequest is : {}", LEAP_LOG_KEY, taxonomy_id);
			return taxonomy_id;
		} catch (Exception e) {
			logger.error("{} exception is : {}", LEAP_LOG_KEY, e.getMessage());

			return null;
		}

	}

	/**
	 * this method is used to apply Template, projection and taxonomy
	 * 
	 * @param isTemplateRequired
	 * @param isProjectionRequired
	 * @param taxonomyId
	 * @param projectionSource
	 * @throws UnableToApplyTemplateException
	 */
	public Object applyTemplate(boolean isTemplateRequired, boolean isProjectionRequired, String taxonomyId,
			String projectionSource) throws UnableToApplyTemplateException {
		String methodName = "applyTemplate";
		logger.debug(
				"{} entered into the method {},isTemplateRequired :{},isProjectionRequired :{},taxonomyId :{},projectionSource :{} ",
				LEAP_LOG_KEY, methodName, isTemplateRequired, isProjectionRequired, taxonomyId, projectionSource);

		try {
			String templateFileName = null;
			String projectionFileName = null;
			LeapServiceContext serviceDataContext = getServiceDataContext();
			String featureGroup = serviceDataContext.getFeatureGroup();
			String feature = serviceDataContext.getFeatureName();
			String baseFileName = null;
			if (taxonomyId != null) {
				baseFileName = featureGroup + "_" + feature + "_" + serviceDataContext.getRunningContextServiceName()
						+ "_" + taxonomyId;
				templateFileName = baseFileName + "_" + LeapDataContextConstant.TEMPLATE_FILE_PATTERN;
				if (projectionSource != null) {
					if (projectionSource.equalsIgnoreCase(LeapDataContextConstant.SWAGGER)) {
						projectionFileName = baseFileName + "_" + LeapDataContextConstant.SWAGGER_FILE_PATTERN;
					} else if (projectionSource.equalsIgnoreCase(LeapDataContextConstant.JSON_SCHEMA)) {
						projectionFileName = baseFileName + "_" + LeapDataContextConstant.JSON_SCHEMA;
					} else {
						throw new UnableToApplyTemplateException(" projection source is invalid ",
								"projection source is invalid", 400);
					}
				} else {
					if (isProjectionRequired) {
						throw new UnableToApplyTemplateException(" projection source is not available in header",
								"projection source is not available in header", 400);
					}
				}

			}

			logger.trace("{} templatefileName :{}, projectionFileName :{}, taxonomyId :{}", LEAP_LOG_KEY,
					templateFileName, projectionFileName, taxonomyId);
			if (isTemplateRequired) {
				if (isProjectionRequired) {
					if (taxonomyId != null) {
						return getNormalizedContextUsingTemplate(templateFileName, projectionSource, projectionFileName,
								taxonomyId);
					} else {
						return "";// TODO ;
					}

				} else {
					if (taxonomyId != null) {
						return "";// TODO
					} else {
						return "";// TODO
					}
				}
			}

			else {
				if (isProjectionRequired) {
					if (taxonomyId != null) {
						return getNormalizedContext(projectionSource, projectionFileName, taxonomyId);
					} else {
						return getNormalizedContext(projectionSource, projectionFileName);
					}
				} else {
					if (taxonomyId != null) {
						return getNormalizedContext(taxonomyId);
					} else {
						return "";// TODO
					}
				}
			}

		} catch (Exception e) {
			logger.error("unable to apply template : " + e);
			throw new UnableToApplyTemplateException("unable to apply template : " + e.getMessage(), e.getCause(),
					e.getMessage(), 400);
		}

		finally {
			cachedJsonSchema.clear();
			taxonomyJson = null;
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		}

	}

	/**
	 * this method is used to apply Template, projection and taxonomy
	 * 
	 * @param isTemplateRequired
	 * @param isProjectionRequired
	 * @param taxonomyId
	 * @param projectionSource
	 * @throws UnableToApplyTemplateException
	 */
	public Object applyTemplate(boolean isTemplateRequired, boolean isProjectionRequired, String templateFileName,
			String projectionFileName, String projectionSource, String taxonomyId)
			throws UnableToApplyTemplateException {
		String methodName = "applyTemplate";
		logger.debug(
				"{} entered into the method {},isTemplateRequired :{},isProjectionRequired :{}, taxonomyId :{},templatefileName :{},projectionFileName :{}, projectionSource :{} ",
				LEAP_LOG_KEY, methodName, isTemplateRequired, isProjectionRequired, taxonomyId, templateFileName,
				projectionFileName, projectionSource);

		try {
			if (isTemplateRequired) {
				if (isProjectionRequired) {
					if (taxonomyId != null) {
						return getNormalizedContextUsingTemplate(templateFileName, projectionSource, projectionFileName,
								taxonomyId);
					} else {
						return "";// TODO ;
					}

				} else {
					if (taxonomyId != null) {
						return "";// TODO
					} else {
						return "";// TODO
					}
				}
			}

			else {
				if (isProjectionRequired) {
					if (taxonomyId != null) {
						return getNormalizedContext(projectionSource, projectionFileName, taxonomyId);
					} else {
						return getNormalizedContext(projectionSource, projectionFileName);
					}
				} else {
					if (taxonomyId != null) {
						return getNormalizedContext(taxonomyId);
					} else {
						return "";// TODO
					}
				}
			}

		} catch (Exception e) {
			logger.error("unable to apply template : " + e);
			throw new UnableToApplyTemplateException("unable to apply template : " + e.getMessage(), e.getCause(),
					e.getMessage(), 400);
		}

		finally {
			cachedJsonSchema.clear();
			taxonomyJson = null;
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		}

	}

	/**
	 * this method is used to set the taxonomy
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 */
	public void setTaxonomyJSON(String taxonomyId) throws LeapDataContextInitialzerException, IOException {

		if (taxonomyId != null && !taxonomyId.isEmpty()) {
			String taxonomyFileName = taxonomyId + "_Taxonomy.json";
			String taxonomyData = LeapDataContextHelper.getFile(taxonomyFileName, LeapDataContextConstant.TAXONOMY_KEY,
					taxonomyId, this);
			taxonomyJson = new JSONObject(taxonomyData);
		} else
			taxonomyJson = new JSONObject();

		logger.debug("{} taxonomyJson is : {}", LEAP_LOG_KEY, taxonomyJson);

	}

	/**
	 * Method to apply taxonomy
	 *
	 * @param template
	 * @param contextData
	 * @param response
	 * @param parentkey
	 * @return {@link JSONObject}
	 */
	public JSONObject applyTaxonomy(JSONObject contextData, JSONObject response) {
		Iterator<String> keys = contextData.keys();
		String key;
		Object value = null, actualVal;
		JSONArray valueFromContext, tempValues;
		JSONObject tempObj;
		boolean isNotJson = true;
		while (keys.hasNext()) {

			key = keys.next();
			logger.debug("{} key is : {}", LEAP_LOG_KEY, key);
			if (contextData.has(key)) {
				value = contextData.get(key);
			}
			actualVal = mergeValues(value);
			logger.debug("{} actualVal is : {}", LEAP_LOG_KEY, actualVal);
			isNotJson = !actualVal.getClass().getName().contains("JSON");
			if (isNotJson) {
				logger.trace("{} isNotJson is : ", LEAP_LOG_KEY);
				String key1 = getVendorTaxonomy(key);
				logger.debug("{} key is : {}", LEAP_LOG_KEY, key1);
				response.put(key1, actualVal);
				metaDataMap.put(key1, key);
				logger.debug("{} response is : {}", LEAP_LOG_KEY, response);
			} else {
				if (actualVal instanceof JSONObject) {
					tempObj = new JSONObject();
					applyTaxonomy((JSONObject) actualVal, tempObj);
					if (tempObj.has(key)) {
						String key2 = getVendorTaxonomy(key);
						response.put(key2, tempObj.get(key));
						metaDataMap.put(key2, key);
					} else {
						String key3 = getVendorTaxonomy(key);
						response.put(key3, tempObj);
						metaDataMap.put(key3, key);
					}
				} else {
					valueFromContext = (JSONArray) actualVal;
					tempValues = new JSONArray();
					String key4 = getVendorTaxonomy(key);
					response.put(key4, tempValues);
					metaDataMap.put(key4, key);
					for (int i = 0; i < valueFromContext.length(); i++) {
						tempObj = new JSONObject();
						applyTaxonomy(valueFromContext.getJSONObject(i), tempObj);
						if (tempObj.has(key)) {
							tempValues.put(tempObj.get(key));
						} else {
							tempValues.put(tempObj);
						}
					}
				}
			}
		}
		return response;
	}

	/**
	 * Method to build the Normalized Context
	 * 
	 * @param projectionsArr
	 * @return {@link JSONArray}
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 */
	private JSONArray buildNormalizedContext() throws LeapDataContextInitialzerException, IOException {
		// getTemplateData();
		String methodName = "buildNormalizedContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		long start = System.currentTimeMillis();
		finalizeDataStructure();
		logger.info("{} finalJsonStructured is :: {}", LEAP_LOG_KEY, finalJsonStructured);
		Map<String, List<JSONObject>> nextChildMap = finalJsonStructured.stream()
				.collect(Collectors.groupingBy(LeapDataContext.keyGroupBy(),
						Collectors.mapping(LeapDataContext.keyGroupByValue(), Collectors.toList())));
		JSONObject finalResponse = new JSONObject();
		finalResponse.put("root", nextChildMap);
		JSONArray finalJsonResponse;
		// finalJsonResponse.put(root);
		// System.out.println("Total Time to build data based on Schema from LDC
		// : " + (System.currentTimeMillis() - start));
		logger.info("{} projectionsArr :{}, Final Response: {} ", LEAP_LOG_KEY, projectionsArr, finalResponse);
		finalJsonResponse = buildNestedStructure(projectionsArr, finalResponse);
		// System.out.println("Total time to apply projection and destination
		// taxonomy : " + (System.currentTimeMillis() - start2));
		// logger.debug("finalJsonResponse : " + finalJsonResponse);
		logger.info("{} finalJSonResponse: {} ", LEAP_LOG_KEY, finalJsonResponse);
		long start2 = System.currentTimeMillis();
		logger.debug("{} total time taken for buildNormalizedContext : {} ", LEAP_LOG_KEY, (start2 - start));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalJsonResponse;

	}

	/**
	 * Method to construct the structure for the response from LDC
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 */
	private void finalizeDataStructure() throws LeapDataContextInitialzerException, IOException {
		String methodName = "finalizeDataStructure";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Iterator<LeapDataContextElement> ldcElements = dequeContextElement.descendingIterator();
		LeapDataContextElement ldcElement = null;
		String taxonomyId = null;
		List<MetaData> metadata = null;

		while (ldcElements.hasNext()) {
			ldcElement = ldcElements.next();
			if (ldcElement.getDataElement() instanceof InitialLeapDataContextElement)
				continue;
			LeapData leapData = ldcElement.getDataElement().getData();
			logger.debug("{} leapData is :{} ", LEAP_LOG_KEY, leapData.toString());
			LeapResultSet leapResultSet = leapData.getItems();
			Object dataInFormat = leapResultSet.getData();
			String kind = leapData.getKind();
			taxonomyId = leapData.getTaxonomyId();
			logger.trace("{} taxonomyId is : {}", LEAP_LOG_KEY, taxonomyId);
			logger.info("{} vendorTaxonomyId is : {}", LEAP_LOG_KEY, vendorTaxonomyId);
			logger.trace("{} taxonomyJson before initialize is : {}", LEAP_LOG_KEY, taxonomyJson);
			if (taxonomyJson == null) {

				if (taxonomyId == null) {
					String taxonomyFileName = vendorTaxonomyId + "_Taxonomy.json";
					logger.trace("{} taxonomyFileName is : {} ", LEAP_LOG_KEY, taxonomyFileName);
					String taxonomyData = LeapDataContextHelper.getFile(taxonomyFileName,
							LeapDataContextConstant.TAXONOMY_KEY, vendorTaxonomyId, this);
					taxonomyJson = new JSONObject(taxonomyData);
					logger.debug("{} taxonomyJson after initialize is : {}", LEAP_LOG_KEY, taxonomyJson);

				} else if (taxonomyId != null && !(vendorTaxonomyId.equalsIgnoreCase(taxonomyId))) {
					String taxonomyFileName = vendorTaxonomyId + "_Taxonomy.json";
					logger.trace("{} taxonomyFileName is : {}", LEAP_LOG_KEY, taxonomyFileName);
					String taxonomyData = LeapDataContextHelper.getFile(taxonomyFileName,
							LeapDataContextConstant.TAXONOMY_KEY, vendorTaxonomyId, this);
					taxonomyJson = new JSONObject(taxonomyData);
					logger.debug("{} taxonomyJson after initialize is : {}", LEAP_LOG_KEY, taxonomyJson);
				} else
					taxonomyRequired = false;
			}

			if (taxonomyId == null) {
				taxonomyId = vendorTaxonomyId;
			}

			String actualColumnName = null, effectiveColumnName = null;
			metadata = leapData.getMetadata();
			if (taxonomyId != null && !(taxonomyId.equalsIgnoreCase(LEAPDEFAULT_TAXONOMY))) {

				for (int x = 0; x < metadata.size(); x++) {
					MetaData eachMetaData = metadata.get(x);
					effectiveColumnName = eachMetaData.getEffectiveColumnName();
					if (effectiveColumnName.equalsIgnoreCase(kind)) {
						actualColumnName = eachMetaData.getActualColumnName();
						break;
					}
				}
			}

			storeMetaData(metadata);
			if (actualColumnName == null) {
				dataFormatIdentifierAndKindEvaluation(dataInFormat, kind, metadata, taxonomyId);
			} else {
				dataFormatIdentifierAndKindEvaluation(dataInFormat, actualColumnName, metadata, taxonomyId);
			}
		} // ..end of while loop
		evaluateAndContructFinalJsonStructure(metadata);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to identify the datatype and evaluate data based on 'kind'
	 * 
	 * @param dataInFormat
	 * @param kind
	 * @param metadata
	 * @param taxonomyId
	 * @throws LeapDataContextInitialzerException
	 */
	private void dataFormatIdentifierAndKindEvaluation(Object dataInFormat, String kind, List<MetaData> metadata,
			String taxonomyId) throws LeapDataContextInitialzerException {
		String methodName = "dataFormatIdentifierAndKindEvaluation";
		logger.debug("{} entered into the method {}, Kind={}, taxonomyId={},instance of dataInFormat :: {} ",
				LEAP_LOG_KEY, methodName, kind, taxonomyId, dataInFormat.getClass());
		if (dataInFormat instanceof String)
			if (((String) dataInFormat).startsWith("<")) {
				dataInFormat = XML.toJSONObject((String) dataInFormat);
			} else if (((String) dataInFormat).startsWith("{")) {
				dataInFormat = new JSONObject((String) dataInFormat);
			} else if (((String) dataInFormat).startsWith("[{"))
				dataInFormat = new JSONArray((String) dataInFormat);

		if (dataInFormat instanceof JSONObject) {
			if (((JSONObject) dataInFormat).has(kind)) {
				Object arrayOrJsonObj = ((JSONObject) dataInFormat).get(kind);
				getInstanceOf(arrayOrJsonObj, kind, metadata, taxonomyId);
			} else
				logger.warn("{} kind skipped while applying template : {} ", LEAP_LOG_KEY, kind);
		} else if (dataInFormat instanceof JSONArray) {
			JSONArray jsonObjArray = (JSONArray) dataInFormat;
			for (int i = 0; i < jsonObjArray.length(); i++)
				dataFormatIdentifierAndKindEvaluation(jsonObjArray.get(i), kind, metadata, taxonomyId);
		} else if (dataInFormat instanceof org.apache.metamodel.data.InMemoryDataSet) {
			dataSetFormatEvaluation(dataInFormat, kind, metadata, taxonomyId);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to check the instance of each element and process
	 * 
	 * @param arrayOrJsonObj
	 * @param kind
	 * @param metadata
	 * @param taxonomyId
	 */
	@SuppressWarnings("unchecked")
	private void getInstanceOf(Object arrayOrJsonObj, String kind, List<MetaData> metadata, String taxonomyId) {
		String methodName = "getInstanceOf";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (arrayOrJsonObj instanceof JSONObject) {
			JSONObject kindObj = (JSONObject) arrayOrJsonObj;
			// String kindStr = arrayOrJsonObj.toString();
			logger.trace("{} inside getInstanceOf method kindObj :: {}", kindObj);
			logger.trace("{} inside getInstanceOf method kind  :: {}, metaData :{}", LEAP_LOG_KEY, kind, metadata);
			String effectiveKind = getEffectiveColumnName(kind, metadata);
			logger.trace("{} effectiveKind is :: {}", LEAP_LOG_KEY, effectiveKind);
			HashMap<String, Object> kindJsonSchema = buildAndGetJsonSchemaMap(effectiveKind, metadata);
			if (kindJsonSchema == null)
				return;
			logger.trace("{} kindJsonSchema :: {}", LEAP_LOG_KEY, kindJsonSchema);
			HashMap<String, String> filters = null;
			logger.trace("filters::");
			if (!(kindJsonSchema.isEmpty())) {
				filters = (HashMap<String, String>) kindJsonSchema.get("filters");
			}
			// if parent kind then using unique-key to store kindObj
			String uniqueKeys = (String) kindJsonSchema.get("uniqueBy");
			Object dataBasedOnTaxonomy = LeapDataContextHelper.getDataBasedOnTaxonomy(taxonomyId, kindObj, this);
			DocumentContext documentContext = JsonPath.parse(dataBasedOnTaxonomy.toString());
			JSONObject kindDummy = new JSONObject();
			kindDummy.put(effectiveKind, dataBasedOnTaxonomy);
			logger.debug("{} kindDummy inside getInstanceOf method  :: {} ", LEAP_LOG_KEY, kindDummy.toString());
			boolean isParent = true;
			// if not parent kind then using ref-key to store kindObj
			if (!parentKinds.contains(effectiveKind)) {
				isParent = false;
				uniqueKeys = (String) kindJsonSchema.get("referenceKey");
			}
			if (!literalsAndUIDCheck(filters, uniqueKeys, documentContext, kind, kindDummy, isParent, metadata))
				return;
		} else if (arrayOrJsonObj instanceof JSONArray) {
			JSONArray kindObjArray = (JSONArray) arrayOrJsonObj;
			for (int i = 0; i < kindObjArray.length(); i++)
				getInstanceOf(kindObjArray.get(i), kind, metadata, taxonomyId);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Caches the Json Schema for Particular Kind on every First Request.
	 * 
	 * @param getKind
	 * @param metadata
	 * @return HashMap<String, Object>
	 */
	private HashMap<String, Object> buildAndGetJsonSchemaMap(String getKind, List<MetaData> metadata) {
		String methodName = "buildAndGetJsonSchemaMap";
		logger.debug("{} entered into the method {}, cachedJsonSchema :{} ", LEAP_LOG_KEY, methodName,
				cachedJsonSchema);
		if (cachedJsonSchema.isEmpty()) {
			jsonSchema.keySet().stream().forEach(kind -> {
				try {
					HashMap<String, Object> kindCachedSchema = new HashMap<>();
					boolean isParent = true;
					JSONObject kindSchema = jsonSchema.getJSONObject(kind);
					JSONArray uniqueByArray = kindSchema.getJSONArray("uniqueBy");
					StringBuffer uniqueIndentifer = new StringBuffer();
					uniqueByArray.forEach(e -> uniqueIndentifer.append(e).append(" "));
					if (uniqueIndentifer.length() > 0) {
						String uniqueBy = uniqueIndentifer.toString();
						kindCachedSchema.put("uniqueBy", uniqueBy);
						mapOfAllUIDData.putIfAbsent(uniqueBy, new ArrayListValuedHashMap<String, Object>());
					}
					if (kindSchema.has("relationship")) {
						JSONObject relationships = kindSchema.getJSONObject("relationship");
						kindCachedSchema.put("childOf", relationships.getString("childOf"));
						StringBuffer references = new StringBuffer();
						relationships.getJSONArray("referenceKey").forEach(e -> references.append(e).append(" "));
						if (references.length() > 0) {
							String referencesList = references.toString();
							kindCachedSchema.put("referenceKey", referencesList);
							mapOfAllUIDData.putIfAbsent(referencesList, new ArrayListValuedHashMap<String, Object>());
						}
						isParent = false;
					}
					HashMap<String, String> filters = new HashMap<>();
					if (kindSchema.has("filters"))
						kindSchema.getJSONArray("filters").forEach(e -> {
							JSONObject filter = (JSONObject) e;
							filters.put(filter.getString("name"), filter.get("value").toString());
						});
					kindCachedSchema.put("filters", filters);

					if (isParent) {
						parentKinds.add(kind);
						parentKindIdentifiers.add(uniqueIndentifer.toString());
					}
					cachedJsonSchema.put(kind, kindCachedSchema);
				} catch (Exception e) {
					logger.error("{} Schema Parsing Issue : {}, error-message :{}", LEAP_LOG_KEY, kind, e.getMessage(),
							e);
				}
			});

		}
		String effectiveColumnName = getEffectiveColumnName(getKind, metadata);
		logger.debug("{} effectiveColumnName value {} in {} method", LEAP_LOG_KEY, effectiveColumnName, methodName);
		logger.debug("{} cachedJsonSchema value {} in {} method :: ", LEAP_LOG_KEY, cachedJsonSchema, methodName);
		if (effectiveColumnName == null) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return cachedJsonSchema.get(getKind);
		} else {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return cachedJsonSchema.get(effectiveColumnName);
		}
	}

	/**
	 * Method to check for Filters defined if any
	 * 
	 * @param filters
	 * @param uniqueKeys
	 * @param documentContext
	 * @param kind
	 * @param kindObj
	 * @param isParent
	 * @param metadata
	 * @return {@link Boolean}
	 */
	private boolean literalsAndUIDCheck(HashMap<String, String> filters, String uniqueKeys,
			DocumentContext documentContext, String kind, JSONObject kindObj, boolean isParent,
			List<MetaData> metadata) {
		if (!filters.isEmpty()) {
			boolean isVerified = true;
			Set<String> keys = filters.keySet();
			for (String key : keys) {
				String expectedValue = filters.get(key);
				List<Object> literalValues = documentContext.read("$.." + key);
				if (literalValues.isEmpty())
					return false;
				else
					isVerified = expectedValue.equals(literalValues.get(0).toString());
				if (!isVerified)
					return false;
			}

		}
		StringBuffer uniqueKeyVals = new StringBuffer();
		for (String uniqueKey : Arrays.asList(uniqueKeys.split(" "))) {
			List<Object> pidVals = documentContext.read("$.." + uniqueKey.trim());
			if (pidVals.isEmpty())
				return false;
			else {
				String pidVal = pidVals.get(0).toString();
				uniqueKeyVals.append(pidVal).append("_");
			}
		}
		ArrayListValuedHashMap<String, Object> uidMetaData = mapOfAllUIDData.get(uniqueKeys);
		if (isParent) {
			finalJsonStructured.add(kindObj);
			uidMetaData.put(uniqueKeyVals.toString(), (finalJsonStructured.size() - 1));
		} else {
			uidMetaData.put(uniqueKeyVals.toString(), kindObj);
		}
		return true;
	}

	/**
	 * Method to identify the datatype as 'MetaModel DataSet' thus process and
	 * evaluate it for the 'kind'
	 * 
	 * @param dataInFormat
	 * @param kind
	 * @param metadata
	 * @return {@link Boolean}
	 * @throws LeapDataContextInitialzerException
	 */
	@SuppressWarnings("unchecked")
	private boolean dataSetFormatEvaluation(Object dataInFormat, String kind, List<MetaData> metadata,
			String taxonomyId) throws LeapDataContextInitialzerException {
		DataSet dataSet = (DataSet) dataInFormat;
		OUTER: while (dataSet.next()) {
			JSONObject rowJSONObject = new JSONObject();
			JSONObject rowJsonParent = new JSONObject();
			Row row = dataSet.getRow();
			List<SelectItem> selectItems = dataSet.getSelectItems();
			String effectiveKind = getEffectiveColumnName(kind, metadata);
			HashMap<String, Object> kindJsonSchema = buildAndGetJsonSchemaMap(effectiveKind, metadata);
			// if parent kind then using unique-key to store kindObj
			String uniqueKeys = (String) kindJsonSchema.get("uniqueBy");
			HashMap<String, String> filters = null;
			if (!(kindJsonSchema.isEmpty())) {
				filters = (HashMap<String, String>) kindJsonSchema.get("filters");
			}
			boolean isParent = true;
			// if not parent kind then using ref-key to store kindObj
			if (!parentKinds.contains(effectiveKind)) {
				isParent = false;
				uniqueKeys = (String) kindJsonSchema.get("referenceKey");
			}
			int indexi = 0;
			int index = 0;
			boolean keyExists = true;
			if (!filters.isEmpty()) {
				boolean isVerified = true;
				Set<String> keys = filters.keySet();
				for (String key : keys) {
					for (SelectItem item : selectItems) {
						key = getActualColumnName(key, metadata);
						keyExists = item.getColumn().getName().equalsIgnoreCase(key);
						if (keyExists) {
							indexi = selectItems.indexOf(item);
							break;
						}
					}
					String literalValues = (String) row.getValue(indexi);
					if (literalValues.isEmpty())
						continue OUTER;
					else {
						key = getEffectiveColumnName(key, metadata);
						isVerified = filters.get(key).equalsIgnoreCase(literalValues);
					}
					if (!isVerified)
						continue OUTER;
				}
			}
			StringBuffer uniqueKeyVals = new StringBuffer();
			for (String pid : uniqueKeys.split(" ")) {
				pid = getActualColumnName(pid, metadata);
				for (SelectItem item : selectItems) {
					keyExists = item.getColumn().getName().equalsIgnoreCase(pid);
					if (keyExists) {
						index = selectItems.indexOf(item);
						break;
					}
				}
				if (index == -1)
					continue OUTER;
				String pidVal = (String) row.getValue(index);
				uniqueKeyVals.append(pidVal).append("_");
				index = -1;
			}
			selectItems.stream().forEach(selectItem -> {
				rowJSONObject.put(selectItem.getColumn().getName(), row.getValue(selectItem));
				rowJsonParent.put(kind, rowJSONObject);
			});

			Object dataBasedOnTaxonomy = LeapDataContextHelper.getDataBasedOnTaxonomy(taxonomyId, rowJsonParent, this);

			ArrayListValuedHashMap<String, Object> uidMetaData = mapOfAllUIDData.get(uniqueKeys);
			if (isParent) {
				finalJsonStructured.add((JSONObject) dataBasedOnTaxonomy);
				uidMetaData.put(uniqueKeyVals.toString(), (finalJsonStructured.size() - 1));
			} else
				uidMetaData.put(uniqueKeyVals.toString(), (JSONObject) dataBasedOnTaxonomy);
		}
		return true;
	}

	/**
	 * Evaluate the Index Map and data to build the Final Json Structure for the
	 * response
	 * 
	 * @param metadata
	 */
	@SuppressWarnings("unchecked")
	private void evaluateAndContructFinalJsonStructure(List<MetaData> metadata) {
		String methodName = "evaluateAndContructFinalJsonStructure";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		separateParentAndChildData();
		logger.debug("{} =============After Separation of Parent and Child Data================", LEAP_LOG_KEY);
		logger.debug("{} mapOfParentUIDData : ", LEAP_LOG_KEY, mapOfParentUIDData);
		logger.debug("{} mapOfAllUIDData : ", LEAP_LOG_KEY, mapOfAllUIDData);
		logger.debug("{} parentKindIdentifiers : ", LEAP_LOG_KEY, parentKindIdentifiers);
		logger.debug("{} finalJsonStructured : ", LEAP_LOG_KEY, finalJsonStructured);
		Set<String> uidKeys = mapOfParentUIDData.keySet();
		uidKeys.stream().forEach(parentUid -> {
			ArrayListValuedHashMap<String, Object> parentUidData = mapOfParentUIDData.get(parentUid);
			Set<String> puidValues = parentUidData.keySet();
			puidValues.stream().forEach(puidValue -> {
				List<Object> puidData$Indexs = parentUidData.get(puidValue);
				HashMap<String, Object> indexesAndData = splitIndexAndData(puidData$Indexs);
				Set<Integer> indexes = (Set<Integer>) indexesAndData.get("indexes");
				indexesAndData.remove("indexes");
				if (indexesAndData.isEmpty())
					iteratorOnParentData(indexes, metadata);
				else {
					Set<String> firstChildrens = indexesAndData.keySet();
					firstChildrens.stream().forEach(firstChildName -> {
						List<JSONObject> firstChildDataList = (List<JSONObject>) indexesAndData.get(firstChildName);
						// works sequential as well as parallel
						indexes.parallelStream().forEach(index -> {
							JSONObject parentDataWithName = finalJsonStructured.get(index);
							String parentName = parentDataWithName.keys().next();
							JSONObject parentData = parentDataWithName.getJSONObject(parentName);
							firstChildDataList.forEach(childData -> {
								try {
									if (parentData.has(firstChildName)) {
										Object childObj = parentData.get(firstChildName);
										if (childObj instanceof JSONObject) {
											JSONArray childNewArray = new JSONArray();
											JSONObject childJSONObj = (JSONObject) childObj;
											childNewArray.put(childJSONObj);
											childNewArray.put(childData);
											parentData.put(firstChildName, childNewArray);
										} else {
											JSONArray childOldArray = (JSONArray) childObj;
											childOldArray.put(childData);
											parentData.put(firstChildName, childOldArray);
										}
									} else
										parentData.put(firstChildName, childData);
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
						});
					});
					iteratorOnParentData(indexes, metadata);
				}
			});
		});
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to separate the parent and child related data
	 */
	private void separateParentAndChildData() {
		parentKindIdentifiers.forEach(uniqueKey -> {
			mapOfParentUIDData.put(uniqueKey, mapOfAllUIDData.get(uniqueKey));
			mapOfAllUIDData.remove(uniqueKey);
		});
	}

	/**
	 * 
	 * @param puidData$Indexs
	 * @return {@link HashMap}
	 */
	private HashMap<String, Object> splitIndexAndData(List<Object> puidData$Indexs) {
		HashMap<String, Object> puidData$Index = new HashMap<>();
		puidData$Index.put("indexes", puidData$Indexs.parallelStream().filter(LeapDataContext::instanceOfInteger)
				.map(e -> (Integer) e).collect(Collectors.toSet()));
		puidData$Index.putAll(puidData$Indexs.stream().filter(LeapDataContext::instanceOfJSONObject)
				.map(e -> (JSONObject) e).collect(Collectors.groupingBy(LeapDataContext.keyGroupBy(),
						Collectors.mapping(LeapDataContext.keyGroupByValue(), Collectors.toList()))));
		return puidData$Index;
	}

	/**
	 * 
	 * @param indexes
	 * @param metadata
	 */
	private void iteratorOnParentData(Set<Integer> indexes, List<MetaData> metadata) {
		if (mapOfAllUIDData.isEmpty())
			return;
		// works sequential as well as parallel
		indexes.parallelStream().forEach(index -> {
			JSONObject parentData = finalJsonStructured.get(index);
			String parentName = parentData.keys().next();
			updateFinalDataAtIndex(parentData.getJSONObject(parentName), getChildOf(parentName), parentName, null,
					metadata);
		});
	}

	private void updateFinalDataAtIndex(JSONObject data, List<String> childrens, String name, JSONObject prevData,
			List<MetaData> metadata) {

		// logger.debug("------------------");
		// logger.debug("data : "+data);
		// logger.debug("childrens : "+childrens);
		// logger.debug("name : "+name);
		// logger.debug("prevData : "+prevData);
		// logger.debug(index);
		// logger.debug("------------------");

		for (String child : childrens)
			if (data.has(child)) {
				Object childDataObj = data.get(child);
				boolean isObj = false;
				if (childDataObj instanceof JSONObject) {
					JSONObject childJSONObj = (JSONObject) childDataObj;
					updateFinalDataAtIndex(childJSONObj, getChildOf(child), child, data, metadata);
					isObj = true;
				} else {
					JSONArray childJSonArray = (JSONArray) childDataObj;
					for (int i = 0; i < childJSonArray.length(); i++)
						updateFinalDataAtIndex(childJSonArray.getJSONObject(i), getChildOf(child), child, data,
								metadata);
				}
				if (prevData != null) {
					Map<String, List<JSONObject>> nextChildMap = getDataFromMapByRelationship(name, prevData, data,
							metadata);
					if (nextChildMap != null) {
						JSONArray childNewArray = new JSONArray();
						if (nextChildMap.containsKey(child)) {
							if (isObj) {
								childNewArray = new JSONArray();
								childNewArray.put((JSONObject) childDataObj);
								data.put(child, childNewArray);
							}
							List<JSONObject> nextChildrenData = nextChildMap.get(child);
							nextChildrenData.stream().forEach(nextChildDataEle -> {
								try {
									JSONObject nextChildData = new JSONObject(nextChildDataEle.toString());
									JSONArray jsonArrayChild = data.getJSONArray(child);
									jsonArrayChild.put(nextChildData);
									updateFinalDataAtIndex(nextChildData, getChildOf(child), child, data, metadata);
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							});
						}
					}
				}
			} else {
				if (!parentKinds.contains(name)) {
					Map<String, List<JSONObject>> nextChildMap = getDataFromMapByRelationship(name, prevData, data,
							metadata);
					if (nextChildMap == null)
						return;
					Set<String> nextChildNames = nextChildMap.keySet();
					for (String nextChild : nextChildNames) {
						List<JSONObject> nextChildrenData = nextChildMap.get(nextChild);
						nextChildrenData.stream().forEach(nextChildDataEle -> {
							try {
								JSONObject nextChildData = new JSONObject(nextChildDataEle.toString());
								if (data.has(nextChild)) {
									Object childObj = data.get(nextChild);
									JSONArray childNewArray = new JSONArray();
									if (childObj instanceof JSONObject) {
										JSONObject childJSONObj = (JSONObject) childObj;
										childNewArray.put(childJSONObj);
										childNewArray.put(nextChildData);
										data.put(nextChild, childNewArray);
									} else {
										childNewArray = (JSONArray) childObj;
										childNewArray.put(nextChildData);
										data.put(nextChild, childNewArray);
									}
								} else
									data.put(nextChild, nextChildData);
								updateFinalDataAtIndex(nextChildData, getChildOf(nextChild), nextChild, data, metadata);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						});
					}
				}
			}
	}

	/**
	 * This method will return the child of given name
	 * 
	 * @param parent
	 * @return
	 */
	private List<String> getChildOf(String parent) {
		List<String> childrens = childOfCacheMap.get(parent);
		if (childrens.isEmpty()) {
			childrens = cachedJsonSchema.entrySet().parallelStream().filter(
					e -> e.getValue().containsKey("childOf") ? e.getValue().get("childOf").equals(parent) : false)
					.map(e -> e.getKey()).collect(Collectors.toList());
			childrens.forEach(child -> childOfCacheMap.put(parent, child));
		}
		return childrens;
	}

	/**
	 * Method to fetch the data fromt global map based on relationship
	 * 
	 * @param name
	 * @param prevData
	 * @param data
	 * @param metadata
	 * @return {@link Map} of Map<String, List<JSONObject>>
	 * @throws Exception
	 */
	private Map<String, List<JSONObject>> getDataFromMapByRelationship(String name, JSONObject prevData,
			JSONObject data, List<MetaData> metadata) {
		// name = mapOfActualToEffectiveColumnName.get(name);
		String uniqueIdentifier = (String) buildAndGetJsonSchemaMap(name, metadata).get("uniqueBy");
		StringBuffer uniqueKeyValues = new StringBuffer();
		for (String uniqueKey : Arrays.asList(uniqueIdentifier.split(" "))) {
			if (data.has(uniqueKey))
				uniqueKeyValues.append(data.get(uniqueKey).toString()).append("_");
			else if (prevData.has(uniqueKey))
				uniqueKeyValues.append(prevData.get(uniqueKey).toString()).append("_");
			else
				return null;
		}

		ArrayListValuedHashMap<String, Object> dataOfALLChild = mapOfAllUIDData.get(uniqueIdentifier);

		if (dataOfALLChild == null)
			return null;
		List<Object> listOfNextChildData = dataOfALLChild.get(uniqueKeyValues.toString());

		Map<String, List<JSONObject>> nextChildMap = listOfNextChildData.stream().map(e -> (JSONObject) e)
				.collect(Collectors.groupingBy(LeapDataContext.keyGroupBy(),
						Collectors.mapping(LeapDataContext.keyGroupByValue(), Collectors.toList())));

		return nextChildMap;
	}

	/**
	 * Method to build the Netsed Structure for the response data based on
	 * projection
	 * 
	 * @param projArr
	 * @param jsData
	 * @return {@link JSONArray}
	 */
	public JSONArray buildNestedStructure(JSONArray projArr, JSONObject jsData) {
		String methodName = "buildNestedStructure";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONArray finalArray = new JSONArray();
		JSONObject json;
		Object data;
		JSONObject template = buildTemplate(projArr);
		String key = LeapDataContextHelper.getRootTag(template);
		if (key != null) {
			template = template.getJSONObject(key);
		} else {
			key = "root";
		}
		JSONObject contextData = jsData;
		json = new JSONObject();
		data = buildStructure(template, contextData.getJSONObject(key), json, key);
		finalArray.put(new JSONObject().put(key, data));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return finalArray;
	}

	/**
	 * Method to build template data based on
	 * 
	 * @param projArr
	 * @return {@link JSONObject}
	 */
	private JSONObject buildTemplate(JSONArray projArr) {
		String proj;
		Object value;
		JSONObject response = new JSONObject();
		for (int i = 0; i < projArr.length(); i++) {
			proj = "root." + projArr.getString(i);
			if (proj.contains(".*")) {
				proj = proj.replace(".*", "");
				value = new JSONObject();
			} else {
				value = "";
			}
			buildNestedJSON(response, value, proj);
		}
		return response;
	}

	/**
	 * Method to build structure and apply taxonomy
	 * 
	 * @param template
	 * @param contextData
	 * @param response
	 * @param parentkey
	 * @return {@link JSONObject}
	 */
	private JSONObject buildStructure(JSONObject template, JSONObject contextData, JSONObject response,
			String parentkey) {

		if (template.length() == 0) {
			return response.put(parentkey, contextData);
		}
		Iterator<String> keys = template.keys();
		String key;
		Object value = null, actualVal;
		JSONArray valueFromContext, tempValues;
		JSONObject tempObj;
		boolean isNotJson = true;
		while (keys.hasNext()) {
			key = keys.next();
			if (contextData.has(key)) {
				value = contextData.get(key);
			}
			actualVal = mergeValues(value);
			isNotJson = !actualVal.getClass().getName().contains("JSON");
			if (isNotJson) {
				String key1 = getVendorTaxonomy(key);
				response.put(key1, actualVal);
				metaDataMap.put(key1, key);
				// response.put(key, actualVal);
			} else {
				if (actualVal instanceof JSONObject) {
					tempObj = new JSONObject();
					buildStructure(template.getJSONObject(key), (JSONObject) actualVal, tempObj, key);
					if (tempObj.has(key)) {
						String key2 = getVendorTaxonomy(key);
						response.put(key2, tempObj.get(key));
						metaDataMap.put(key2, key);
						// response.put(key, tempObj.get(key));
					} else {
						String key3 = getVendorTaxonomy(key);
						response.put(key3, tempObj);
						metaDataMap.put(key3, key);
						// response.put(key, tempObj);
					}
				} else {
					valueFromContext = (JSONArray) actualVal;
					tempValues = new JSONArray();
					String key4 = getVendorTaxonomy(key);
					response.put(key4, tempValues);
					metaDataMap.put(key4, key);
					// response.put(key, tempValues);
					for (int i = 0; i < valueFromContext.length(); i++) {
						tempObj = new JSONObject();
						buildStructure(template.getJSONObject(key), valueFromContext.getJSONObject(i), tempObj, key);
						if (tempObj.has(key)) {
							tempValues.put(tempObj.get(key));
						} else {
							tempValues.put(tempObj);
						}
					}
				}
			}
		}
		return response;
	}

	/**
	 * Method to 'key' name based on taxonomy
	 * 
	 * @param key
	 * @return {@link String}
	 */
	private String getVendorTaxonomy(String key) {
		String changedKey = null;
		if (taxonomyRequired) {
			if (taxonomyJson.has(key)) {
				changedKey = taxonomyJson.getString(key);
				return changedKey;
			} else {
				return key;
			}
		} else {
			return key;
		}
	}

	/**
	 * Method to store MetaData for each ldc element into global maps
	 * 
	 * @param metadata
	 */
	private void storeMetaData(List<MetaData> metadata) {
		String effectiveColumnName = null, actualColumnName = null;
		for (int x = 0; x < metadata.size(); x++) {
			MetaData eachMetaData = metadata.get(x);
			// logger.debug("eachMetaData : " + eachMetaData);
			effectiveColumnName = eachMetaData.getEffectiveColumnName();
			actualColumnName = eachMetaData.getActualColumnName();
			mapToGetActualColumnName.put(effectiveColumnName, actualColumnName);
			mapToGetEffectiveColumnName.put(actualColumnName, effectiveColumnName);
		}
		logger.debug("{} mapToGetActualColumnName in storeMetaData :: {}, mapToGetEffectiveColumnName :: {}",
				LEAP_LOG_KEY, mapToGetActualColumnName, mapToGetEffectiveColumnName);
	}

	/**
	 * Method to fetch ActulaColumnName from the MetaData based on the
	 * EffectiveColumnName
	 * 
	 * @param key
	 * @param metadata
	 * @return {@link String}
	 */
	public String getActualColumnName(String key, List<MetaData> metadata) {
		// logger.debug(" inside .getActualColumnName() ");
		StringBuffer uniqueKeyVals = new StringBuffer();
		List<String> keyList;
		if (key.contains(" ")) {
			keyList = Arrays.asList(key.split(" "));
			for (String uniqueKey : keyList) {
				if (mapToGetActualColumnName.containsKey(uniqueKey)) {
					uniqueKeyVals.append(mapToGetActualColumnName.get(uniqueKey)).append(" ");
				} else
					uniqueKeyVals.append(key).append(" ");
			}
			return uniqueKeyVals.toString();
		} else {
			if (mapToGetActualColumnName.containsKey(key)) {
				return mapToGetActualColumnName.get(key);
			}
		}
		return key;
	}

	/**
	 * Method to fetch EffectiveColumnName from the MetaData based on the
	 * ActulaColumnName
	 * 
	 * @param key
	 * @param metadata
	 * @return {@link String}
	 */
	public String getEffectiveColumnName(String key, List<MetaData> metadata) {
		// logger.debug(" inside .getEffectiveColumnName() ");
		StringBuffer uniqueKeyVals = new StringBuffer();
		List<String> keyList;

		if (key.contains(" ")) {
			keyList = Arrays.asList(key.split(" "));
			for (String uniqueKey : keyList) {
				if (mapToGetEffectiveColumnName.containsKey(uniqueKey)) {
					uniqueKeyVals.append(mapToGetEffectiveColumnName.get(uniqueKey)).append(" ");
				} else
					uniqueKeyVals.append(key).append(" ");
			}
			return uniqueKeyVals.toString();
		} else {
			if (mapToGetEffectiveColumnName.containsKey(key)) {
				return mapToGetEffectiveColumnName.get(key);
			}
		}
		return key;
	}

	/**
	 * Method to construct the Flattened Structure for the LDC Response
	 * 
	 * @return JSONArray
	 * @throws LeapDataContextInitialzerException
	 */
	private JSONArray buildFlattenedStructure() throws LeapDataContextInitialzerException {
		String methodName = "buildFlattenedStructure";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		long start = System.currentTimeMillis();
		JSONArray response = new JSONArray();
		try {
			JSONArray genericData = buildNormalizedContext();
			logger.info("{} generic Data is : {} ", LEAP_LOG_KEY, genericData);
			List<JSONObject> temp;
			Map<String, Object> datMap;
			for (int i = 0; i < genericData.length(); i++) {
				datMap = buildDataMap(genericData.getJSONObject(i), getProjectionMap(flattenedrojectionArr));
				datMap = modifyDataMap(datMap);
				temp = getFlatJsonFromMap(datMap);
				if (isDataExist(temp)) {
					for (JSONObject eachtem : temp) {
						response.put(eachtem);
					}
				}
			}
		} catch (JSONException | IOException exp) {
			exp.printStackTrace();
			throw new LeapDataContextInitialzerException(exp.getMessage(), exp.getCause(),
					"Unable to build flattened structure", 400);
		}
		// System.out.println("Time only to generate flattened structure :
		// "+(System.currentTimeMillis()-start));
		logger.info("{} response : {}", LEAP_LOG_KEY, response);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return response;
	}

	/**
	 * 
	 * @param projections
	 * @return Map<String, List<String>>
	 */
	private Map<String, List<String>> getProjectionMap(JSONArray projections) {
		Map<String, List<String>> map = new TreeMap<>(getComparator());
		List<String> list;
		String tempKey, tempVal;
		String arr[];
		StringBuilder sb;
		List<String> emptylist = new ArrayList<>();
		for (int i = 0; i < projections.length(); i++) {
			tempKey = "root." + projections.getString(i);
			arr = tempKey.split("\\.");
			sb = new StringBuilder();
			sb.append(arr[0]);
			map.put(sb.toString(), emptylist);
			for (int j = 1; j < arr.length - 1; j++) {
				sb.append(".");
				sb.append(arr[j]);
				map.put(sb.toString(), emptylist);
			}
		}
		logger.debug("{} map 1 : {}", LEAP_LOG_KEY, map);
		int index;
		for (int i = 0; i < projections.length(); i++) {
			tempKey = projections.getString(i);
			index = tempKey.lastIndexOf(".");
			if (index > 0) {
				tempVal = tempKey.substring(index + 1);
				tempKey = tempKey.substring(0, index);

				if (map.containsKey(tempKey)) {
					list = map.get(tempKey);
					if (list.isEmpty()) {
						list = new ArrayList<>();
					}
				} else {
					list = new ArrayList<>();
				}
				list.add(tempVal);
				map.put(tempKey, list);
			}
		}

		Iterator<String> mapIterator = map.keySet().iterator();
		while (mapIterator.hasNext()) {
			String key = mapIterator.next();
			if (map.get(key).isEmpty() && !key.equalsIgnoreCase("root")) {
				mapIterator.remove();
			}
		}
		logger.debug("{} Atlast::map:: {}", LEAP_LOG_KEY, map);
		return map;
	}

	/**
	 * 
	 * @return Comparator<String>
	 */
	private Comparator<String> getComparator() {
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				if (s1.length() > s2.length()) {
					return 1;
				} else if (s1.length() < s2.length()) {
					return -1;
				}
				return s1.compareTo(s2);
			}
		};
		return comparator;
	}

	/**
	 * 
	 * @param object
	 * @return boolean
	 */
	private boolean isDataExist(Object object) {
		if (object instanceof JSONObject) {
			JSONObject js = (JSONObject) object;
			if (js.length() == 0) {
				return false;
			}
		} else if (object instanceof JSONArray) {
			JSONArray js = (JSONArray) object;
			if (js.length() == 0) {
				return false;
			} else if (js.length() == 1) {
				return isDataExist(js.get(0));
			}
		} else if (object instanceof List) {
			List<?> js = (List<?>) object;
			if (js.size() == 0) {
				return false;
			} else if (js.size() == 1) {
				return isDataExist(js.get(0));
			}
		}
		return true;
	}

	/**
	 * 
	 * @param entry
	 * @param jsonvalue
	 * @return Object
	 */
	private Object getValuesForKey(Entry<String, List<String>> entry, Object jsonvalue) {
		Object response;
		if (jsonvalue instanceof JSONObject) {
			response = getJSONObjForKey(entry, (JSONObject) jsonvalue);
		} else {
			JSONArray jsArr = (JSONArray) jsonvalue;
			JSONArray new_jsArr = new JSONArray();
			for (int i = 0; i < jsArr.length(); i++) {
				new_jsArr.put(getJSONObjForKey(entry, jsArr.getJSONObject(i)));
			}
			response = new_jsArr;
		}
		return response;
	}

	/**
	 * 
	 * @param entry
	 * @param json
	 * @return JSONObject
	 */
	private JSONObject getJSONObjForKey(Entry<String, List<String>> entry, JSONObject json) {
		JSONObject jsonvalueObj = json;
		JSONObject new_obj = new JSONObject();
		List<String> value = entry.getValue();
		if (value.size() == 1) {
			if (value.get(0).equals("*")) {
				return jsonvalueObj;
			}
		}
		for (String str : value) {
			if (jsonvalueObj.has(str)) {
				new_obj.put(str, jsonvalueObj.get(str));
			}
		}
		return new_obj;
	}

	/**
	 * 
	 * @param key
	 * @param genericdata
	 * @return Object
	 */
	private Object getDataFormGenericData(String key, JSONObject genericdata) {
		StringBuilder sb = new StringBuilder("$..");
		key = modifyKey(key);
		String temp;
		sb.append(key);
		Object read = JsonPath.read(genericdata.toString(), sb.toString());
		JSONArray jsonArray = new JSONArray(read.toString());
		if (jsonArray.length() == 1) {
			return jsonArray.get(0);
		} else {
			JSONArray newjsonArray = new JSONArray();
			for (Object object : jsonArray) {
				temp = object.toString();
//				if (temp.startsWith("[")) {
//					JSONArray eachArray = new JSONArray(object.toString());
//					newjsonArray.put(eachArray.get(0));
//				}
				if (temp.startsWith("[")) {
					JSONArray eachArray = new JSONArray(object.toString());
					newjsonArray.put(eachArray.get(0));
				} else {
					logger.trace("{} inside else block", LEAP_LOG_KEY);
					newjsonArray.put(object);
				}
			}
			logger.trace("{} newjsonArray:: {}", LEAP_LOG_KEY, newjsonArray);
			return newjsonArray;
		}
	}

	/**
	 * 
	 * @param datMap
	 * @return
	 */
	private Map<String, Object> modifyDataMap(Map<String, Object> datMap) {
		Object obj;
		logger.debug("inside modifyDataMap");
		Map<String, Object> finalmap = new TreeMap<>(getComparator());
		for (Entry<String, Object> entry : datMap.entrySet()) {
			obj = createFlatObjects(entry);
			if (obj != null) {
				finalmap.put(entry.getKey(), createFlatObjects(entry));
			}
		}
		return finalmap;
	}

	/**
	 * 
	 * @param entry
	 * @return
	 */
	private Object createFlatObjects(Entry<String, Object> entry) {
		Object response;
		JSONObject tempval;
		String key = entry.getKey();
		key = modifyKey(key);
		JSONObject createFlatJSONObject;
		Object value = entry.getValue();
		if (value instanceof JSONObject) {
			tempval = (JSONObject) value;
			response = createFlatJSONObject(tempval, key);
		} else {
			JSONArray jsArr = (JSONArray) value;
			JSONArray new_jsArr = new JSONArray();
			for (int i = 0; i < jsArr.length(); i++) {
				createFlatJSONObject = createFlatJSONObject(jsArr.getJSONObject(i), key);
				if (createFlatJSONObject.length() > 0) {
					new_jsArr.put(createFlatJSONObject);
				}
			}
			if (new_jsArr.length() > 0) {
				response = new_jsArr;
			} else {
				response = null;
			}
		}
		return response;
	}

	/**
	 *
	 * @param key
	 * @return String
	 */
	private String modifyKey(String key) {
		int lastIndexOf = key.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return key;
		} else {
			return key.substring(lastIndexOf + 1);
		}
	}

	/**
	 * 
	 * @param tempval
	 * @param basekey
	 * @return JSONObject
	 */
	private JSONObject createFlatJSONObject(JSONObject tempval, String basekey) {
		JSONObject result = new JSONObject();
		String key;
		Iterator<String> keys = tempval.keys();
		while (keys.hasNext()) {
			key = keys.next();
			result.put(basekey + "." + key, tempval.get(key));
		}
		return result;
	}

	/**
	 * 
	 * @param genericdata
	 * @param projectionMap
	 * @return Map<String, Object>
	 */
	private Map<String, Object> buildDataMap(JSONObject genericdata, Map<String, List<String>> projectionMap) {
		String key;
		Object jsonValue, tempVal;
		Map<String, Object> datMap = new TreeMap<>();
		for (Entry<String, List<String>> entry : projectionMap.entrySet()) {
			logger.trace("{} entry {}", LEAP_LOG_KEY, entry);
			key = entry.getKey();
			jsonValue = getDataFormGenericData(entry.getKey(), genericdata);
			tempVal = getValuesForKey(entry, jsonValue);
			datMap.put(key, tempVal);
		}
		logger.trace("{} map value in buildDataMap method: {}", LEAP_LOG_KEY, datMap);
		return datMap;
	}

	/**
	 * 
	 * @param map
	 * @return List<JSONObject>
	 */
	private List<JSONObject> getFlatJsonFromMap(Map<String, Object> map) {
		List<Entry<String, Object>> list = new ArrayList<>(map.entrySet());
		List<JSONObject> result = new ArrayList<>();
		boolean isResultEmpty = true;
		List<JSONObject> tempList;
		JSONObject baseJson = (JSONObject) list.get(0).getValue();
		result.add(baseJson);
		for (int i = 1; i < list.size(); i++) {
			// tempList = populateTempList(list.get(i).getValue(), result, isResultEmpty);
			tempList = populateTempList(list.get(i).getValue(), list.get(i).getKey(), result, isResultEmpty);
			result = tempList;
			isResultEmpty = false;
		}
		return result;
	}

	/**
	 * 
	 * @param entryVal
	 * @param result
	 * @return List<JSONObject>
	 */
	private List<JSONObject> populateTempList(Object entryVal, String mainKey, List<JSONObject> result,
			boolean isResultEmpty) {
		logger.debug("{} entryVal :{}, result :{}", LEAP_LOG_KEY, entryVal, result);
		JSONArray jsArr;
		if (entryVal instanceof JSONObject) {
			jsArr = new JSONArray();
			jsArr.put((JSONObject) entryVal);
		} else {
			jsArr = (JSONArray) entryVal;
		}
		List<JSONObject> tempList = new ArrayList<>();
		JSONObject temp, tempArrObj, obj;
		Iterator<String> keys;
		String key;

		for (int i = 0; i < result.size(); i++) {
			temp = new JSONObject(result.get(i).toString());
			logger.debug("{} temp :: {}", LEAP_LOG_KEY, temp);
			if (isResultEmpty) {
				for (int j = 0; j < jsArr.length(); j++) {
					obj = new JSONObject(temp.toString());
					logger.trace("{} obj :: {}", LEAP_LOG_KEY, obj);
					tempArrObj = jsArr.getJSONObject(j);
					keys = tempArrObj.keys();
					while (keys.hasNext()) {
						key = keys.next();
						logger.debug("{} key  :{}", LEAP_LOG_KEY, key);
						obj.put(key, tempArrObj.get(key));
						logger.debug("{} obj ::: {}", LEAP_LOG_KEY, obj);
					}
					tempList.add(obj);

				}
			} else {
				obj = new JSONObject(temp.toString());
				logger.trace("{} obj :: {}", LEAP_LOG_KEY, obj);
				tempArrObj = jsArr.getJSONObject(i);
				keys = tempArrObj.keys();
				while (keys.hasNext()) {
					key = keys.next();
					logger.debug("{} key  : {}", LEAP_LOG_KEY, key);
					logger.debug("{} main key:: {}", LEAP_LOG_KEY, mainKey);
					String newkey = null;
					if (!tempArrObj.get(key).toString().equals("")) {
						int index = key.lastIndexOf(".");
						String subString = key.substring(0, index);
						logger.trace("{} subString {}", LEAP_LOG_KEY, subString);
						if (mainKey.contains(subString)) {
							newkey = mainKey + key.substring(index);
						}
						logger.debug("{} subString {}", LEAP_LOG_KEY, subString);
						obj.put(newkey, tempArrObj.get(key));
					}
					logger.debug("{} obj ::: {}", LEAP_LOG_KEY, obj);
				}
				tempList.add(obj);
			}
		}
		logger.debug("{} tempList :: {}", LEAP_LOG_KEY, tempList);
		return tempList;
	}

	public static Function<JSONObject, JSONObject> keyGroupByValue() {
		return t -> t.getJSONObject(t.keys().next());
	}

	public static Function<JSONObject, String> keyGroupBy() {
		return t -> t.keys().next();
	}

	public static boolean instanceOfInteger(Object val) {
		return val instanceof Integer;
	}

	public static boolean instanceOfJSONObject(Object val) {
		return val instanceof JSONObject;
	}

	/**
	 * Method to get the resource files
	 * 
	 * @param fileName
	 * @return {@link String}
	 */
	public String getResourceFile(String fileName, String type) {
		String methodName = "getResourceFile";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			RequestContext requestContext = this.getServiceDataContext().getRequestContext();
			// we are storing ldc file in this format =>
			// tenantId_siteId_fg_fn_imp_Ven_serviceName_filetype
			String keyToLoadLDCFilesContent = getKeyToLoadLDCFilesContent(requestContext, type, fileName);
			logger.debug("{} key To Load LDC Files Content :: {}", LEAP_LOG_KEY, keyToLoadLDCFilesContent);
			String fileContent = getFileContentFromCache(requestContext, keyToLoadLDCFilesContent);
			if (fileContent == null) {
				logger.info("{} fileName :{} doesn't exist in map or cache, So loading into the map", LEAP_LOG_KEY,
						fileName);
				fileContent = IOUtils.toString(LeapConfigUtil.getFile(requestContext, fileName));
				mapForLDCFilesContent.put(keyToLoadLDCFilesContent, fileContent);
			}
			logger.debug("{} fileName: {}, file data :{}", fileName, fileContent);
			return fileContent;
		} catch (Exception e) {
			logger.error("{} Unable to find the required file: {}", LEAP_LOG_KEY, fileName);
			return null;
		}
	}

	/**
	 * This method is used to get the file(taxonomy, projections, template) content
	 * from cache
	 * 
	 * @param requestContext
	 * @param keyToLoadLDCFilesContent
	 * @return
	 */
	private String getFileContentFromCache(RequestContext requestContext, String keyToLoadLDCFilesContent) {
		long start = System.currentTimeMillis();
		if (mapForLDCFilesContent.containsKey(keyToLoadLDCFilesContent)) {
			logger.trace("{} file does exist in map or cache", LEAP_LOG_KEY);
			return mapForLDCFilesContent.get(keyToLoadLDCFilesContent).toString();
		}
		long end = System.currentTimeMillis();
		logger.debug("{} time taken to read file from cache is:- {} ms", LEAP_LOG_KEY, (end - start));
		return null;
	}

	private String getKeyToLoadLDCFilesContent(RequestContext requestContext, String ldcFileType, String fileName) {
		StringBuilder keyToLoadLDCFilesContent = new StringBuilder();
		keyToLoadLDCFilesContent.append(requestContext.getTenantId());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(requestContext.getSiteId());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(requestContext.getFeatureGroup());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(requestContext.getFeatureName());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(requestContext.getImplementationName());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(requestContext.getVendor());
		if (requestContext.getProvider() != null) {
			keyToLoadLDCFilesContent.append(UNDER_SCORE);
			keyToLoadLDCFilesContent.append(requestContext.getProvider());
		}
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(this.getServiceDataContext().getRunningContextServiceName());
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(fileName);
		keyToLoadLDCFilesContent.append(UNDER_SCORE);
		keyToLoadLDCFilesContent.append(ldcFileType);
		return keyToLoadLDCFilesContent.toString();
	}

	private Object mergeValues(Object value) {
		return value == null ? "" : value;
	}

	private void buildNestedJSON(JSONObject response, Object value, String proj) {
		String[] projection = proj.split("\\.");
		JSONObject temp = response;
		int length = projection.length - 1;
		for (int i = 0; i <= length; i++) {
			if (i == length) {
				temp.put(projection[i], value);
				break;
			}
			if (!temp.has(projection[i])) {
				temp.put(projection[i], new JSONObject());
			}
			temp = temp.getJSONObject(projection[i]);
		}
	}

	private void generateProjectionForRequiredOnly(JSONObject projJson, String projection) {
		if (projJson.has(SEARCH_IN_PROPERITES) && projJson.has(SEARCH_REQUIRED)) {
			JSONObject propsObj = projJson.getJSONObject(SEARCH_IN_PROPERITES);
			JSONArray requiredKeys = projJson.getJSONArray(SEARCH_REQUIRED);
			requiredKeys.forEach(tag -> {
				String temp = projection;
				JSONObject tagObj = propsObj.getJSONObject(tag.toString());
				if (!temp.isEmpty())
					temp = temp + "." + tag.toString();
				else
					temp = tag.toString();
				generateProjectionForRequiredOnly(tagObj, temp);
			});
		} else if (projJson.has(SEARCH_IN_ITEMS)) {
			JSONObject itemsJSON = projJson.getJSONObject(SEARCH_IN_ITEMS);
			if (itemsJSON.has(SEARCH_IN_PROPERITES) && itemsJSON.has(SEARCH_REQUIRED)) {
				JSONObject propsObj = itemsJSON.getJSONObject(SEARCH_IN_PROPERITES);
				JSONArray requiredKeys = itemsJSON.getJSONArray(SEARCH_REQUIRED);
				requiredKeys.forEach(tag -> {
					String temp = projection;
					JSONObject tagObj = propsObj.getJSONObject(tag.toString());
					if (!temp.isEmpty())
						temp = temp + "." + tag.toString();
					else
						temp = tag.toString();
					generateProjectionForRequiredOnly(tagObj, temp);
				});
			}
		} else
			projectionsArr.put(projection);
	}

	private void generateProjectionForAll(JSONObject projJson, String projection) {
		if (projJson.has(SEARCH_IN_PROPERITES)) {
			JSONObject propsObj = projJson.getJSONObject(SEARCH_IN_PROPERITES);
			Set<String> tags = propsObj.keySet();
			tags.forEach(tag -> {
				String temp = projection;
				JSONObject tagObj = propsObj.getJSONObject(tag.toString());
				if (!temp.isEmpty())
					temp = temp + "." + tag.toString();
				else
					temp = tag.toString();
				generateProjectionForAll(tagObj, temp);
			});
		} else if (projJson.has(SEARCH_IN_ITEMS)) {
			JSONObject itemsJSON = projJson.getJSONObject(SEARCH_IN_ITEMS);
			JSONObject propsObj = itemsJSON.getJSONObject(SEARCH_IN_PROPERITES);
			Set<String> tags = propsObj.keySet();
			tags.forEach(tag -> {
				String temp = projection;
				JSONObject tagObj = propsObj.getJSONObject(tag.toString());
				if (!temp.isEmpty())
					temp = temp + "." + tag.toString();
				else
					temp = tag.toString();
				generateProjectionForAll(tagObj, temp);
			});
		} else
			projectionsArr.put(projection);
	}

	public void setDisplayMetaDataInLdc(List<DisplayMetaData> displayMetaDatas) {
		if (displayMetaDatas != null)
			this.displayMetaDataList = displayMetaDatas;
	}

	public void clearCachedJsonSchema() {
		cachedJsonSchema.clear();
		taxonomyJson = null;
		// metaDataMap = new HashMap<>();
	}

	private void updateI18NforDisplayMetaData(List<MetaData> metaDatas) {
		if (metaDatas != null)
			for (DisplayMetaData displayMetaData : this.displayMetaDataList) {
				String entityFieldRefName = displayMetaData.getEntityFieldNameRef();
				if (entityFieldRefName != null)
					for (MetaData metaData : metaDatas) {
						String actualName = metaData.getActualColumnName();
						if (actualName != null && actualName.equals(entityFieldRefName)) {
							displayMetaData.updateI18NFieldName(metaData.getEffectiveColumnName());
							break;
						}
					}
			}
	}
	/*
	 * @Override public String toString() { return
	 * "LeapDataContext [contextElement=" + dequeContextElement + ", tagsList=" +
	 * allElementTagsList + ", contextConfig=" + initialContextConfig + "]"; }
	 */

}
