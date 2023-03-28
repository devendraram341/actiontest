package com.attunedlabs.leap.context.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapDataContextConfigException;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapJSONArrayResultSet;
import com.attunedlabs.leap.context.bean.LeapJSONResultSet;
import com.attunedlabs.leap.context.bean.LeapMMDataSet;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.bean.LeapXMLResultSet;
import com.attunedlabs.leap.context.bean.MetaData;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.resourcemanagement.config.IResourceManagementConfigService;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigurationException;
import com.attunedlabs.resourcemanagement.config.impl.ResourceManagementConfigService;
import com.attunedlabs.resourcemanagement.jaxb.ConnectionInfo;
import com.attunedlabs.resourcemanagement.jaxb.GetResourceContent;
import com.jayway.jsonpath.JsonPath;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapDataContextHelper {

	private static final String URI = "URI";

	private static final String TYPE = "Type";

	private static final String RESOURCE_SCOPE = "ResourceScope";

	private static final String RESOURCE_NAME = "ResourceName";

	private static final String HTTP = "http://";

	private static final String HTTPS = "https://";

	static Logger logger = LoggerFactory.getLogger(LeapDataContextHelper.class);

	private static final String USER_AGENT = "Mozilla/5.0";

	private final static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder builder;

	private final static TransformerFactory tf = TransformerFactory.newInstance();
	private static Transformer transformer;
	private static JSONObject templateJson;
	private final static String LEAP_DEFAULT_TAXONOMY_FILE = "LeapDefault_Taxonomy.json";
	static JSONObject leapDataContextElementJson = new JSONObject();

	/**
	 * Method to convert LeapDataElement Object as a {@link JSONObject}
	 * 
	 * @param dataElement
	 * @param tag
	 * @return {@link JSONObject}
	 */
	public static JSONObject getContextElementAsJson(LeapDataElement dataElement, String tag) {
		JSONObject leapDataContextElementJson = new JSONObject();
		JSONObject headers = null;
		if (dataElement instanceof InitialLeapDataContextElement) {
			InitialLeapDataContextElement initialLeapDataContextElement = (InitialLeapDataContextElement) dataElement;
			if (initialLeapDataContextElement.getRequestHeaderElement() != null
					&& initialLeapDataContextElement.getPrivateHeaderElement() == null) {
				headers = new JSONObject(initialLeapDataContextElement.getRequestHeaderElement());
				leapDataContextElementJson.put(tag, headers);
			} else if (initialLeapDataContextElement.getRequestHeaderElement() == null
					&& initialLeapDataContextElement.getPrivateHeaderElement() != null) {
				headers = new JSONObject(initialLeapDataContextElement.getPrivateHeaderElement());
				leapDataContextElementJson.put(tag, headers);
			} else {
				headers = new JSONObject(initialLeapDataContextElement);
				return headers;
			}
		} else {
			String apiversion = dataElement.getApiVersion();
			String context = dataElement.getContext();
			String lang = dataElement.getLang();

			LeapData data = dataElement.getData();
			String kind = data.getKind();
			List<MetaData> metadataList = data.getMetadata();
			LeapResultSet items = data.getItems();
			String dataType = items.getType();
			Object reqresData = items.getData();

			JSONArray metaDataArray = new JSONArray();
			metaDataArray = convertToJson(metadataList);

			JSONObject itemsJson = new JSONObject();
			itemsJson.put(LeapDataContextConstant.TYPE, dataType);
			itemsJson.put(LeapDataContextConstant.ELEMENT, reqresData);

			JSONObject leapDataJson = new JSONObject();
			leapDataJson.put(LeapDataContextConstant.KIND, kind);
			leapDataJson.put(LeapDataContextConstant.META_DATA, metaDataArray);
			leapDataJson.put(LeapDataContextConstant.ITEMS, itemsJson);

			JSONObject leapDataElementJson = new JSONObject();

			leapDataElementJson.put(LeapDataContextConstant.API_VERSION, apiversion);
			leapDataElementJson.put(LeapDataContextConstant.CONTEXT, context);
			leapDataElementJson.put(LeapDataContextConstant.LANG, lang);
			leapDataElementJson.put(LeapDataContextConstant.DATA, leapDataJson);

			leapDataContextElementJson.put(tag, leapDataElementJson);
		}
		return leapDataContextElementJson;
	}

	/**
	 * Method to convert the 'element' data of LeapDataElement Object as a
	 * {@link JSONObject}
	 * 
	 * @param dataElement
	 * @param kind
	 * @return {@link Object}
	 * @throws LeapDataContextConfigException
	 */
	public static Object getContextElementAsJsonForKind(LeapDataElement dataElement, String kind)
			throws LeapDataContextConfigException {
		JSONObject leapDataContextElementJson = new JSONObject();
		JSONObject headers = null;
		try {
			if (dataElement instanceof InitialLeapDataContextElement) {
				InitialLeapDataContextElement initialLeapDataContextElement = (InitialLeapDataContextElement) dataElement;
				if (initialLeapDataContextElement.getRequestHeaderElement() != null
						&& initialLeapDataContextElement.getPrivateHeaderElement() == null) {
					headers = new JSONObject(initialLeapDataContextElement.getRequestHeaderElement());
					leapDataContextElementJson.put(kind, headers);
				} else {
					headers = new JSONObject(initialLeapDataContextElement.getPrivateHeaderElement());
					leapDataContextElementJson.put(kind, headers);
				}
			} else {

				LeapData data = dataElement.getData();
				List<MetaData> metadataList = data.getMetadata();
				LeapResultSet items = data.getItems();
				Object reqresData = items.getData();

				JSONObject itemsJson = new JSONObject();
				itemsJson.put(LeapDataContextConstant.ELEMENT, reqresData);

				if (reqresData instanceof JSONArray) {
					JSONArray finalDataArray = handelJsonArray(reqresData);
					return finalDataArray;
				} else if (reqresData instanceof JSONObject) {
					JSONArray finalDataArray = handelJsonObject(reqresData);
					return finalDataArray;
				} else {
					JSONArray finalDataArray = new JSONArray();
					if (reqresData.toString().startsWith("[[") || reqresData.toString().startsWith("[")) {
						finalDataArray = handelXMLArray(reqresData);
						return finalDataArray;
					} else {
						finalDataArray = handelXMLData(reqresData);
						return finalDataArray;
					}
				}
			}
		} catch (JSONException exp) {
			throw new LeapDataContextConfigException(exp.getMessage(), exp.getCause(),
					"Unable to build Json for Context Element", 400);
		}
		return null;
	}

	/**
	 * Method used to get the Leap Response Items array for a given leap Response.
	 * 
	 * @param leapResponse
	 * @return leap response Items array
	 */
	public static JSONArray getJsonData(String leapResponse) {
		JSONObject leapResponseObj = null;
		JSONObject leapData = null;
		JSONArray itemsArray = null;
		if (!leapResponse.isEmpty()) {
			leapResponseObj = new JSONObject(leapResponse);
			leapData = leapResponseObj.getJSONObject(LeapDataContextConstant.DATA);
			return leapData.getJSONArray(LeapDataContextConstant.ITEMS);
		}
		return itemsArray;
	}

	/**
	 * Method used to get either JSONObject or JSONArray depending on LeapResultSet
	 * instance coming inside LeapData
	 * 
	 * @param leapResponse
	 * @return Returns either JSONObject or JSONArray
	 */
	public static Object getJsonData(LeapDataElement leapResponse) {
		LeapData leapData = leapResponse.getData();
		JSONObject jsonObjectData = null;
		JSONArray jsonArrayData = null;

		if (leapData.getItems() instanceof LeapJSONResultSet) {
			LeapJSONResultSet resultSet = (LeapJSONResultSet) leapData.getItems();
			jsonObjectData = resultSet.getData();
			return jsonObjectData;
		} else if (leapData.getItems() instanceof LeapJSONArrayResultSet) {
			LeapJSONArrayResultSet resultSet = (LeapJSONArrayResultSet) leapData.getItems();
			jsonArrayData = resultSet.getData();
			return jsonArrayData;
		}
		return jsonObjectData;
	}

	/**
	 * Method used to know if the Leap response has Error type.
	 * 
	 * @param leapResponse
	 * @return true if the Leap response is having error type else false no error
	 *         response.
	 */
	public static boolean isLeapErrorResponse(String leapResponse) {
		boolean isError = false;
		JSONObject leapResponseObj = null;

		if (!leapResponse.isEmpty()) {
			leapResponseObj = new JSONObject(leapResponse);
			if (leapResponseObj.has("error")) {
				isError = true;
				return isError;
			}
		}
		return isError;

	}

	/**
	 * Method used to get Leap response Error Object for the given Leap Response.
	 * 
	 * @param leapResponse
	 * @return Leap Response Error JSON Object.
	 */
	public static JSONObject getLeapErrorData(String leapResponse) {

		JSONObject leapResponseObj = null;
		JSONObject leapErrorResponseObj = null;

		if (!leapResponse.isEmpty()) {
			leapResponseObj = new JSONObject(leapResponse);
			if (leapResponseObj.has("error")) {
				leapErrorResponseObj = leapResponseObj.getJSONObject("error");

				return leapErrorResponseObj;
			}
		}
		return leapErrorResponseObj;

	}

	/**
	 * Method used to convert a LeapResponse(leapDataElement) instance to JSON
	 * Object.
	 * 
	 * @param leapDataElement
	 * @return Returns a leapDataElement in JSON format.
	 */
	public static JSONObject toJson(LeapDataElement leapDataElement) {
		return LeapDataContext.getFormatLeapResponse(leapDataElement);
	}

	/**
	 * Method used to add new JSON item into items array of Leap Response where Leap
	 * Response as LeapDataElement.
	 * 
	 * @param leapDataElement
	 * @param newItem
	 * @return
	 */
	public static JSONObject addItems(LeapDataElement leapDataElement, JSONObject newItem) {

		JSONObject formatedLeapResponse = null;
		JSONObject newLeapResponse = null;

		formatedLeapResponse = LeapDataContext.getFormatLeapResponse(leapDataElement);
		newLeapResponse = addItems(formatedLeapResponse.toString(), newItem);

		return newLeapResponse;

	}

	/**
	 * Method used to add new JSON item into items array of Leap Response where Leap
	 * Response as String.
	 * 
	 * @param leapResponse
	 * @param newItem
	 * @return Returns Leap Response with new item added into the items Array
	 */
	public static JSONObject addItems(String leapResponse, JSONObject newItem) {
		JSONObject newLeapResponse = null;
		JSONObject leapData = null;
		JSONObject newleapData = null;
		JSONArray itemsArray = null;

		if (!leapResponse.isEmpty()) {
			newLeapResponse = new JSONObject(leapResponse);
			leapData = newLeapResponse.getJSONObject(LeapDataContextConstant.DATA);

			itemsArray = leapData.getJSONArray(LeapDataContextConstant.ITEMS);
			itemsArray.put(newItem);
			newleapData = LeapDataContext.getNewJsonLeapData(itemsArray,
					leapData.get(LeapDataContextConstant.KIND_VAL).toString(), itemsArray.length(),
					leapData.getJSONArray(LeapDataContextConstant.META_DATA));

			newLeapResponse.put(LeapDataContextConstant.DATA, newleapData);

		}
		return newLeapResponse;
	}

	/**
	 * Method used to remove all the Items in Leap Response.
	 * 
	 * @param leapResponse
	 * @return Returns the JSON Leap Response with items array as empty.
	 */
	public static JSONObject removeItems(String leapResponse) {
		JSONObject newLeapResponse = null;
		JSONObject leapData = null;
		JSONObject newleapData = null;
		JSONArray itemsArray = null;
		JSONArray metaData = null;
		System.out.println(leapResponse);
		if (!leapResponse.isEmpty()) {
			newLeapResponse = new JSONObject(leapResponse);
			leapData = newLeapResponse.getJSONObject(LeapDataContextConstant.DATA);

			itemsArray = new JSONArray();
			metaData = new JSONArray();

			newleapData = LeapDataContext.getNewJsonLeapData(itemsArray,
					leapData.get(LeapDataContextConstant.KIND_VAL).toString(), itemsArray.length(), metaData);
			newLeapResponse.put(LeapDataContextConstant.DATA, newleapData);

		}
		return newLeapResponse;
	}

	/**
	 * Method used to remove all the Items in Leap Response.
	 * 
	 * @param leapResponse
	 * @return Returns the JSON Leap Response with items array as empty.
	 */
	public static JSONObject removeItems(LeapDataElement leapResponse) {
		JSONObject formatedLeapResponse = null;
		formatedLeapResponse = LeapDataContext.getFormatLeapResponse(leapResponse);
		return removeItems(formatedLeapResponse.toString());
	}

	/**
	 * 
	 * @param reqresData
	 * @return {@link JSONArray}
	 */
	private static JSONArray handelJsonArray(Object reqresData) {
		JSONArray array = (JSONArray) reqresData;
		JSONArray finalDataArray = new JSONArray();
		JSONObject leapDataContextElementJson = new JSONObject();
		JSONObject responseData;
		for (Object eachJson : array) {
			if (eachJson instanceof JSONArray) {
				JSONArray jArray = (JSONArray) eachJson;
				for (Object eachObject : jArray) {
					responseData = new JSONObject(eachObject.toString());
					String rootTag = getRootTag(responseData);
					if (responseData.get(rootTag) instanceof JSONObject) {
						leapDataContextElementJson = (JSONObject) responseData.get(rootTag);
						finalDataArray.put(leapDataContextElementJson);
					} else {
						JSONArray jsonArray = (JSONArray) responseData.get(rootTag);
						for (Object eObject : jsonArray) {
							finalDataArray.put(eObject);
						}
					}
				}
			} else {
				responseData = new JSONObject(eachJson.toString());
				String rootTag = getRootTag(responseData);
				if (responseData.get(rootTag) instanceof JSONObject) {
					leapDataContextElementJson = (JSONObject) responseData.get(rootTag);
					finalDataArray.put(leapDataContextElementJson);
				} else if (responseData.get(rootTag) instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) responseData.get(rootTag);
					for (Object eObject : jsonArray) {
						finalDataArray.put(eObject);
					}
				} else if (responseData instanceof JSONObject) {
					finalDataArray.put(responseData);
				} else {
					logger.info("{} responseData : {}" ,LEAP_LOG_KEY, responseData);
					JSONArray jsonArray = new JSONArray(responseData.toString());
					finalDataArray = jsonArray;
				}
			}
		}
		return finalDataArray;
	}

	/**
	 * 
	 * @param reqresData
	 * @return {@link JSONArray}
	 */
	private static JSONArray handelJsonObject(Object reqresData) {
		JSONArray finalDataArray = new JSONArray();
		JSONObject responseData = new JSONObject(reqresData.toString());
		String rootTag = getRootTag(responseData);
		if (responseData.get(rootTag) instanceof JSONObject) {
			leapDataContextElementJson = (JSONObject) responseData.get(rootTag);
			finalDataArray.put(leapDataContextElementJson);
		} else {
			JSONArray jsonArray = (JSONArray) responseData.get(rootTag);
			for (Object eObject : jsonArray) {
				finalDataArray.put(eObject);
			}
		}
		return finalDataArray;
	}

	/**
	 * 
	 * @param reqresData
	 * @return {@link JSONArray}
	 */
	private static JSONArray handelXMLArray(Object reqresData) {
		JSONObject leapDataContextElementJson = new JSONObject();
		JSONArray finalDataArray = new JSONArray();
		if (reqresData.toString().startsWith("[[")) {
			JSONArray outerArray = new JSONArray(reqresData.toString());
			for (Object eachObject : outerArray) {
				JSONArray innerArray = new JSONArray(eachObject.toString());
				for (Object innerObject : innerArray) {
					JSONObject eachJson = XML.toJSONObject(innerObject.toString());
					String rootTag = getRootTag(eachJson);
					leapDataContextElementJson = (JSONObject) eachJson.get(rootTag);
					finalDataArray.put(leapDataContextElementJson);
				}
			}
			return finalDataArray;
		} else if (reqresData.toString().startsWith("[")) {
			JSONArray outerArray = new JSONArray(reqresData.toString());
			for (Object eachObject : outerArray) {
				JSONObject eachJson = XML.toJSONObject(eachObject.toString());
				String rootTag = getRootTag(eachJson);
				leapDataContextElementJson = (JSONObject) eachJson.get(rootTag);
				finalDataArray.put(leapDataContextElementJson);
			}
			return finalDataArray;
		}
		return null;
	}

	/**
	 * 
	 * @param reqresData
	 * @return {@link JSONArray}
	 */
	private static JSONArray handelXMLData(Object reqresData) {
		JSONObject jsonObject = XML.toJSONObject(reqresData.toString());
		JSONArray finalDataArray = new JSONArray();
		JSONObject leapDataContextElementJson = new JSONObject();
		String outerRootTag = getRootTag(jsonObject);
		if (outerRootTag != null) {
			try {
				JSONArray outerArray = jsonObject.getJSONArray(outerRootTag);
				for (Object eachObject : outerArray) {
					JSONObject innerJson = new JSONObject(eachObject.toString());
					String rootTag = getRootTag(innerJson);
					if (rootTag != null && !(innerJson.get(rootTag) instanceof String)) {
						leapDataContextElementJson = (JSONObject) innerJson.get(rootTag);
						finalDataArray.put(leapDataContextElementJson);
					} else {
						finalDataArray.put(innerJson);
					}
				}
			} catch (JSONException exp) {
				JSONObject outerJsonObj = jsonObject.getJSONObject(outerRootTag);
				String rootTag = getRootTag(outerJsonObj);
				try {
					if (rootTag != null) {
						JSONObject innerJson = outerJsonObj.getJSONObject(rootTag);
						finalDataArray.put(innerJson);
					}
				} catch (JSONException innerExp) {
					try {
						if (rootTag != null) {
							JSONArray innerArray = outerJsonObj.getJSONArray(rootTag);
							for (Object eachObject : innerArray) {
								JSONObject eachJson = new JSONObject(eachObject.toString());
								String innerRootTag = getRootTag(eachJson);
								if (innerRootTag != null && !(eachJson.get(innerRootTag) instanceof String)) {
									leapDataContextElementJson = (JSONObject) eachJson.get(innerRootTag);
									finalDataArray.put(leapDataContextElementJson);
								} else {
									finalDataArray.put(eachJson);
								}
							}
						}
					} catch (JSONException innerMostExp) {
						finalDataArray.put(outerJsonObj);
					}
				}
			}
		}
		return finalDataArray;
	}

	/**
	 * Method to construct LeapData structure form the raw data
	 * 
	 * @param jsonObject
	 * @param kind
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElement(JSONObject jsonObject, String kind, String taxonomyId,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();
		leapJSONResultSet.setData(jsonObject);
		leapData.setItems(leapJSONResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonObject, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct LeapData structure form the raw data in
	 * {@link JSONObject} format
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromJSONObject(JSONObject jsonObject, String kind,
			String taxonomyId, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();

		leapJSONResultSet.setData(jsonObject);
		leapData.setItems(leapJSONResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonObject, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct LeapData structure form the raw data in
	 * {@link JSONObject} format without taxonomyId
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromJSONObject(JSONObject jsonObject, String kind,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		LeapDataElement initialLeapDataElement = leapDataContext
				.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		try {
			leapJSONResultSet.setData(jsonObject);
			leapData.setItems(leapJSONResultSet);
			List<MetaData> metaDataList = new ArrayList<>();
			metaDataList = createMetaData(jsonObject, leapDataContext);

			leapData.setKind(kind);
			leapData.setMetadata(metaDataList);
			leapData.setUpdated(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
					.toInstant(ZoneOffset.UTC).toString());
			// leapData.setTaxonomyId(LeapDataContextConstant.WHEREWORKS_TAXONOMY);

			String apiVersion = initialLeapDataElement.getApiVersion();
			String context = initialLeapDataElement.getContext();
			String lang = initialLeapDataElement.getLang();

			if (apiVersion != null) {
				leapDataElement.setApiVersion(apiVersion);
			} else {
				leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
			}

			if (context != null) {
				leapDataElement.setContext(context);
			} else {
				leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
			}

			if (context != null) {
				leapDataElement.setLang(lang);
			} else {
				leapDataElement.setLang(LeapDataContextConstant.LANG);
			}

			if (jsonObject.has(LeapDataContextConstant.ERROR)) {
				leapDataElement.setError(jsonObject.getJSONObject(LeapDataContextConstant.ERROR));
			} else {
				leapDataElement.setData(leapData);
			}
			leapDataElement.setId(id);
			leapDataElement.setSelfLink("/" + kind + "/" + id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return leapDataElement;
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * {@link JSONObject} format
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructResponseLeapDataElementFromJSONObject(JSONObject jsonObject, String kind,
			Map<String, String> metaDataMap, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		try {
			leapJSONResultSet.setData(jsonObject);
			leapData.setItems(leapJSONResultSet);
			List<MetaData> metaDataList = new ArrayList<>();
			metaDataList = createMetaDataForResponse(jsonObject, metaDataMap, leapDataContext);

			leapData.setKind(kind);
			leapData.setMetadata(metaDataList);
			// leapData.setTaxonomyId(taxonomyId);

			leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
			leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
			leapDataElement.setLang(LeapDataContextConstant.LANG);
			leapDataElement.setData(leapData);
			leapDataElement.setId(id);
			leapDataElement.setSelfLink("/" + kind + "/" + id);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return leapDataElement;
	}

	/**
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructImmutableLeapDataElementFromJSONObject(JSONObject jsonObject, String kind,
			String taxonomyId, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		leapJSONResultSet.setData(jsonObject);
		leapData.setItems(leapJSONResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonObject, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);
		leapDataElement.setId(id);
		leapDataElement.setSelfLink("/" + kind + "/" + id);

		return leapDataElement;
	}

	static String globalTaxonomyId;
	static long middlegetDataBasedOnTaxonomy = 0;

	public static Object getDataBasedOnTaxonomy(String taxonomyId, Object object, LeapDataContext leapDataContext) {
		String methodName = "getDataBasedOnTaxonomy";
		logger.debug("{} entered into the method {}, TaxonomyId={}, Object={} ", LEAP_LOG_KEY, methodName,taxonomyId,object);
		Object changedDataWithTaxonomy = null;
		String requestTaxonomyFileName = taxonomyId + "_Taxonomy.json";
		String templateStr = null;
		try {
			templateStr = getFile(requestTaxonomyFileName, LeapDataContextConstant.TAXONOMY_KEY, taxonomyId,
					leapDataContext);
		} catch (LeapDataContextInitialzerException | IOException e) {
			logger.error("{} Unable to get the required resource file : {} ",LEAP_LOG_KEY,e);
		}
		templateJson = new JSONObject(templateStr);
		logger.debug("{} templateJson inside getDataBasedOnTaxonomy  ::: {}",LEAP_LOG_KEY, templateJson);
		String parentkey = getRootKey(object);
		logger.trace("{} parentkey inside getDataBasedOnTaxonomy  ::: {}",LEAP_LOG_KEY, parentkey);
		JSONObject response = new JSONObject();
		changedDataWithTaxonomy = changeAccordingToTaxonomyModified(object, response, parentkey);

		return changedDataWithTaxonomy;
	}

	/**
	 * Method to construct LeapData structure form the raw data in {@link JSONArray}
	 * format
	 * 
	 * @param jsonArray
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromJSONArray(JSONArray jsonArray, String kind,
			String taxonomyId, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONArrayResultSet = new LeapJSONArrayResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		// leapJSONArrayResultSet.setData(getDataBasedOnTaxonomy(taxonomyId,
		// jsonArray, leapDataContext));
		leapJSONArrayResultSet.setData(jsonArray);
		leapData.setItems(leapJSONArrayResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonArray, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);
		leapData.setTotalItems(jsonArray.length());

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);
		leapDataElement.setId(id);
		leapDataElement.setSelfLink("/" + kind + "/" + id);

		return leapDataElement;
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * {@link JSONArray} format
	 * 
	 * @param jsonArray
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructResponseLeapDataElementFromJSONArray(JSONArray jsonArray, String kind,
			Map<String, String> metaDataMap, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONArrayResultSet = new LeapJSONArrayResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		// leapJSONArrayResultSet.setData(getDataBasedOnTaxonomy(taxonomyId,
		// jsonArray, leapDataContext));
		leapJSONArrayResultSet.setData(jsonArray);
		leapData.setItems(leapJSONArrayResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaDataForResponse(jsonArray, metaDataMap, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		// leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);
		leapDataElement.setId(id);
		leapDataElement.setSelfLink("/" + kind + "/" + id);

		return leapDataElement;
	}

	/**
	 * Method to construct response LeapData structure form the raw data in
	 * {@link JSONArray} format without taxonomyId
	 * 
	 * @param jsonArray
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromJSONArray(JSONArray jsonArray, String kind,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONArrayResultSet = new LeapJSONArrayResultSet();
		String id = RandomStringUtils.randomAlphanumeric(8);
		LeapDataElement initialLeapDataElement = leapDataContext
				.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		// leapJSONArrayResultSet.setData(getDataBasedOnTaxonomy(taxonomyId,
		// jsonArray, leapDataContext));
		leapJSONArrayResultSet.setData(jsonArray);
		leapData.setItems(leapJSONArrayResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonArray, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(LeapDataContextConstant.LEAPDEFAULT_TAXONOMY);

		String apiVersion = initialLeapDataElement.getApiVersion();
		String context = initialLeapDataElement.getContext();
		String lang = initialLeapDataElement.getLang();

		if (apiVersion != null) {
			leapDataElement.setApiVersion(apiVersion);
		} else {
			leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		}

		if (context != null) {
			leapDataElement.setContext(context);
		} else {
			leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		}

		if (context != null) {
			leapDataElement.setLang(lang);
		} else {
			leapDataElement.setLang(LeapDataContextConstant.LANG);
		}
		leapDataElement.setData(leapData);
		leapDataElement.setId(id);
		leapDataElement.setSelfLink("/" + kind + "/" + id);

		return leapDataElement;
	}

	/**
	 * 
	 * @param jsonArray
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructImmutableLeapDataElementFromJSONArray(JSONArray jsonArray, String kind,
			String taxonomyId, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONArrayResultSet = new LeapJSONArrayResultSet();
		leapJSONArrayResultSet.setData(jsonArray);
		leapData.setItems(leapJSONArrayResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(jsonArray, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct LeapData structure form the raw data in XML(string)
	 * format
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromXML(String xml, String kind, String taxonomyId,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapXMLResultSet = new LeapXMLResultSet();
		List<MetaData> metaDataList = new ArrayList<>();

		if (xml.startsWith("[[")) {
			JSONArray jsonArray = new JSONArray(xml);
			for (int x = 0; x < jsonArray.length(); x++) {
				Object object = jsonArray.get(x);
				metaDataList = createMetaDataForXMLData(object, taxonomyId, leapDataContext);
			}
			leapXMLResultSet.setData(jsonArray.toString());
		} else if (xml.startsWith("[")) {
			JSONArray jsonArray = new JSONArray(xml);
			leapXMLResultSet.setData(jsonArray.toString());
			metaDataList = createMetaDataForXMLData(jsonArray, taxonomyId, leapDataContext);
		} else {
			leapXMLResultSet.setData(xml);
			metaDataList = createMetaDataForXMLData(XML.toJSONObject(xml, true), taxonomyId, leapDataContext);
		}
		leapData.setItems(leapXMLResultSet);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct LeapData structure form the raw data in XML(string)
	 * format without taxonomyId
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructLeapDataElementFromXML(String xml, String kind,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapDataElement initialLeapDataElement = leapDataContext
				.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
		LeapData leapData = new LeapData();
		LeapResultSet leapXMLResultSet = new LeapXMLResultSet();
		List<MetaData> metaDataList = new ArrayList<>();

		if (xml.startsWith("[[")) {
			JSONArray jsonArray = new JSONArray(xml);
			for (int x = 0; x < jsonArray.length(); x++) {
				Object object = jsonArray.get(x);
				metaDataList = createMetaDataForXMLData(object, leapDataContext);
			}
			leapXMLResultSet.setData(jsonArray.toString());
		} else if (xml.startsWith("[")) {
			JSONArray jsonArray = new JSONArray(xml);
			leapXMLResultSet.setData(jsonArray.toString());
			metaDataList = createMetaDataForXMLData(jsonArray, leapDataContext);
		} else {
			leapXMLResultSet.setData(xml);
			metaDataList = createMetaDataForXMLData(XML.toJSONObject(xml), leapDataContext);
		}
		leapData.setItems(leapXMLResultSet);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(LeapDataContextConstant.LEAPDEFAULT_TAXONOMY);

		String apiVersion = initialLeapDataElement.getApiVersion();
		String context = initialLeapDataElement.getContext();
		String lang = initialLeapDataElement.getLang();

		if (apiVersion != null) {
			leapDataElement.setApiVersion(apiVersion);
		} else {
			leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		}

		if (context != null) {
			leapDataElement.setContext(context);
		} else {
			leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		}

		if (context != null) {
			leapDataElement.setLang(lang);
		} else {
			leapDataElement.setLang(LeapDataContextConstant.LANG);
		}
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * XML(string) format
	 * 
	 * @param jsonObject
	 * @param kind
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructResponseLeapDataElementFromXML(String xml, String kind,
			Map<String, String> metaDataMap, LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapXMLResultSet = new LeapXMLResultSet();
		List<MetaData> metaDataList = new ArrayList<>();

		if (xml.startsWith("[[")) {
			JSONArray jsonArray = new JSONArray(xml);
			for (int x = 0; x < jsonArray.length(); x++) {
				Object object = jsonArray.get(x);
				metaDataList = createMetaDataFormXMLDataForResponse(object, metaDataMap, leapDataContext);
			}
			leapXMLResultSet.setData(jsonArray.toString());
		} else if (xml.startsWith("[")) {
			JSONArray jsonArray = new JSONArray(xml);
			leapXMLResultSet.setData(jsonArray.toString());
			metaDataList = createMetaDataFormXMLDataForResponse(jsonArray, metaDataMap, leapDataContext);
		} else {
			leapXMLResultSet.setData(xml);
			metaDataList = createMetaDataFormXMLDataForResponse(XML.toJSONObject(xml), metaDataMap, leapDataContext);
		}
		leapData.setItems(leapXMLResultSet);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		// leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * 
	 * @param xml
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return {@link LeapDataElement}
	 * @throws LeapDataContextConfigException
	 */
	public static LeapDataElement constructImmutableLeapDataElementFromXML(String xml, String kind, String taxonomyId,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapXMLResultSet = new LeapXMLResultSet();
		List<MetaData> metaDataList = new ArrayList<>();

		if (xml.startsWith("[[")) {
			JSONArray jsonArray = new JSONArray(xml);
			for (int x = 0; x < jsonArray.length(); x++) {
				Object object = jsonArray.get(x);
				metaDataList = createMetaDataForXMLData(object, taxonomyId, leapDataContext);
			}
			leapXMLResultSet.setData(jsonArray.toString());
		} else if (xml.startsWith("[")) {
			JSONArray jsonArray = new JSONArray(xml);
			leapXMLResultSet.setData(jsonArray.toString());
			metaDataList = createMetaDataForXMLData(jsonArray, taxonomyId, leapDataContext);
		} else {
			leapXMLResultSet.setData(xml);
			metaDataList = createMetaDataForXMLData(XML.toJSONObject(xml), taxonomyId, leapDataContext);
		}
		leapData.setItems(leapXMLResultSet);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * Method to construct LeapData structure form the raw MM DataSet
	 * 
	 * @param jsonArray
	 * @param kind
	 * @param taxonomyId
	 * @param leapDataContext
	 * @param elementKey
	 * @return {@link LeapDataElement}
	 * @throws JSONException
	 */
	public static LeapDataElement constructLeapDataElementFromMMDataSet(DataSet dataSet, String kind, String taxonomyId,
			LeapDataContext leapDataContext) {
		LeapDataElement leapDataElement = new LeapDataElement();
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONArrayResultSet = new LeapMMDataSet();
		leapJSONArrayResultSet.setData(dataSet);
		leapData.setItems(leapJSONArrayResultSet);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaDataFromDataSet(dataSet, kind, taxonomyId, leapDataContext);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		leapData.setTaxonomyId(taxonomyId);

		leapDataElement.setApiVersion(LeapDataContextConstant.API_VERSION);
		leapDataElement.setContext(LeapDataContextConstant.CONTEXT);
		leapDataElement.setLang(LeapDataContextConstant.LANG);
		leapDataElement.setData(leapData);

		return leapDataElement;
	}

	/**
	 * 
	 * @param dataSet
	 * @param taxonomyId
	 * @return
	 */
	private static List<MetaData> createMetaDataFromDataSet(DataSet dataSet, String kind, String taxonomyId,
			LeapDataContext leapDataContext) {
		JSONObject taxonomyData = new JSONObject();
		if (taxonomyId != null && !taxonomyId.isEmpty()) {
			String taxonomyFileName = taxonomyId + "_Taxonomy.json";
			String taxonomyFile = null;
			try {
				taxonomyFile = getFile(taxonomyFileName, LeapDataContextConstant.TAXONOMY_KEY, taxonomyId,
						leapDataContext);
			} catch (LeapDataContextInitialzerException | IOException e) {
				e.printStackTrace();
			}
			taxonomyData = new JSONObject(taxonomyFile);
		} // ..end of if condition, checking the taxonomy value is not null and
			// empty then fetch the file from classpath

		List<MetaData> metaDataList = new ArrayList<>();
		MetaData metaData = new MetaData();
		metaData.setActualColumnName(kind);
		if (taxonomyData.has(kind))
			metaData.setEffectiveColumnName(taxonomyData.getString(kind));
		else
			metaData.setEffectiveColumnName(kind);

		metaData.setType(kind.getClass().toString());
		metaData.setByteLenth(kind.length());
		metaDataList.add(metaData);
		List<SelectItem> selectItems = dataSet.getSelectItems();
		for (SelectItem selectItem : selectItems) {
			metaData = new MetaData();
			Column column = selectItem.getColumn();
			metaData.setActualColumnName(column.getName());
			if (taxonomyData.has(column.getName()))
				metaData.setEffectiveColumnName(taxonomyData.getString(column.getName()));
			else
				metaData.setEffectiveColumnName(column.getName());
			metaData.setType(column.getType().toString());
			metaData.setByteLenth(column.getColumnSize());
			metaDataList.add(metaData);
		}

		return metaDataList;
	}

	/**
	 * 
	 * @param dataSet
	 * @param taxonomyId
	 * @return
	 */
	private static List<MetaData> createMetaDataFromDataSet(DataSet dataSet, String kind,
			LeapDataContext leapDataContext) {
		JSONObject taxonomyData = new JSONObject();
		List<MetaData> metaDataList = new ArrayList<>();
		MetaData metaData = new MetaData();
		metaData.setActualColumnName(kind);
		if (taxonomyData.has(kind))
			metaData.setEffectiveColumnName(taxonomyData.getString(kind));
		else
			metaData.setEffectiveColumnName(kind);

		metaData.setType(kind.getClass().toString());
		metaData.setByteLenth(kind.length());
		metaDataList.add(metaData);
		List<SelectItem> selectItems = dataSet.getSelectItems();
		for (SelectItem selectItem : selectItems) {
			metaData = new MetaData();
			Column column = selectItem.getColumn();
			metaData.setActualColumnName(column.getName());
			if (taxonomyData.has(column.getName()))
				metaData.setEffectiveColumnName(taxonomyData.getString(column.getName()));
			else
				metaData.setEffectiveColumnName(column.getName().toUpperCase());
			metaData.setType(column.getType().toString());
			metaData.setByteLenth(column.getColumnSize());
			metaDataList.add(metaData);
		}

		return metaDataList;
	}

	/**
	 * Method to get values based on JSONPath
	 * 
	 * @param json
	 * @param key
	 * @return List<String>
	 */
	public static List<String> getValuesFromJson(JSONObject json, String... key) {
		StringBuilder sb = new StringBuilder("$..");
		sb.append(key[0]);
		for (int i = 1; i < key.length; i++) {
			sb.append(".");
			sb.append(key[i]);
		}
		List<String> list = new ArrayList<>();
		Object read = JsonPath.read(json.toString(), sb.toString());
		if (read instanceof net.minidev.json.JSONArray) {
			net.minidev.json.JSONArray newArray = (net.minidev.json.JSONArray) read;
			for (int x = 0; x < newArray.size(); x++) {
				if (newArray.get(x).toString().startsWith("[{")) {
					JSONArray object = new JSONArray(newArray.get(x).toString());
					list.add(object.toString());
				} else if (newArray.get(x).toString().startsWith("{")) {
					java.util.LinkedHashMap map = (LinkedHashMap) newArray.get(x);
					JSONObject object = new JSONObject(map);
					list.add(object.toString());
				} else {
					Object object = newArray.get(x);
					list.add(object.toString());
				}
			}
		}
		return list;
	}

	/**
	 * Method to get values based on XPath
	 * 
	 * @param xml
	 * @param key
	 * @return List<String>
	 * @throws LeapDataContextConfigException
	 */
	public static List<String> getValuesFromXml(String xml, String key) throws LeapDataContextConfigException {
		Document xmlDocument;
		List<String> nodeValue = null;
		xmlDocument = getXmlDocument(xml);
		nodeValue = getNodeValue(xmlDocument, key);
		return nodeValue;
	}

	/**
	 * 
	 * @param json
	 * @param key
	 * @return {@link String}
	 */
	public static String getValueFromJson(JSONObject json, String... key) {
		return getValuesFromJson(json, key).get(0);
	}

	/**
	 * 
	 * @param xml
	 * @param key
	 * @return {@link String}
	 */
	public static String getValueFromXml(String xml, String key) {
		Document xmlDocument;
		List<String> nodeValue = null;
		try {
			xmlDocument = getXmlDocument(xml);
			nodeValue = getNodeValue(xmlDocument, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodeValue.get(0);
	}

	/**
	 * Method to get the exact Node Value
	 * 
	 * @param doc
	 * @param key
	 * @return List<String>
	 * @throws LeapDataContextConfigException
	 */
	public static List<String> getNodeValue(Document doc, String key) throws LeapDataContextConfigException {
		List<String> list = new ArrayList<String>();
		String startKey = "<" + key + ">";
		String endKey = "</" + key + ">";
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.compile("//" + key).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.hasChildNodes()) {
					list.add(getXmlString(node).replaceFirst(startKey, "").replaceFirst(endKey, ""));
				} else {
					list.add(nodeList.item(i).getTextContent());
				}
			}
		} catch (DOMException | LeapDataContextConfigException | XPathExpressionException exp) {
			throw new LeapDataContextConfigException(exp.getMessage(), exp.getCause(), "Failed to g", 400);
		}
		return list;
	}

	/**
	 * Method to build the XML String into Document
	 * 
	 * @param xml
	 * @return
	 * @throws LeapDataContextConfigException
	 */
	private static Document getXmlDocument(String xml) throws LeapDataContextConfigException {
		Document parsedDocument;
		try {
			if (builder == null) {
				builder = builderFactory.newDocumentBuilder();
			}
			InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
			parsedDocument = builder.parse(stream);
		} catch (ParserConfigurationException | SAXException | IOException exp) {
			throw new LeapDataContextConfigException(exp.getMessage(), exp.getCause(),
					"Failed to build XML Document from XML String", 400);
		}
		return parsedDocument;
	}

	/**
	 * 
	 * @param doc
	 * @return
	 * @throws LeapDataContextConfigException
	 */
	private static String getXmlString(Node doc) throws LeapDataContextConfigException {
		String output;
		try {
			if (transformer == null) {
				transformer = tf.newTransformer();
			}
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			output = writer.getBuffer().toString();
		} catch (TransformerException | IllegalArgumentException exp) {
			throw new LeapDataContextConfigException(exp.getMessage(), exp.getCause(),
					"Failed to perse XML Document into XML String", 400);
		}
		return output;
	}

	/**
	 * Method to get the Root Tag for a {@link JSONObject}
	 * 
	 * @param json
	 * @return String
	 */
	public static String getRootTag(JSONObject json) {
		Iterator<String> keys = json.keys();
		if (keys.hasNext()) {
			return keys.next();
		}
		return null;
	}

	/**
	 * Method to convert the complete LeapDataElement Object as a {@link JSONObject}
	 * 
	 * @param dataElement
	 * @param tag
	 * @return {@link JSONObject}
	 */
	public static JSONObject getListOfContextElementAsJson(LeapDataElement dataElement, String tag) {
		JSONObject headers = null;
		if (dataElement instanceof InitialLeapDataContextElement) {
			InitialLeapDataContextElement initialLeapDataContextElement = (InitialLeapDataContextElement) dataElement;
			if (initialLeapDataContextElement.getRequestHeaderElement() != null
					&& initialLeapDataContextElement.getPrivateHeaderElement() == null) {
				headers = new JSONObject(initialLeapDataContextElement.getRequestHeaderElement());
				leapDataContextElementJson.put(tag, headers);
			} else {
				headers = new JSONObject(initialLeapDataContextElement.getPrivateHeaderElement());
				leapDataContextElementJson.put(tag, headers);
			}
		} else {
			String apiversion = dataElement.getApiVersion();
			String context = dataElement.getContext();
			String lang = dataElement.getLang();

			LeapData data = dataElement.getData();
			String kind = data.getKind();
			List<MetaData> metadataList = data.getMetadata();
			LeapResultSet items = data.getItems();
			String dataType = items.getType();
			Object reqresData = items.getData();

			JSONArray metaDataArray = new JSONArray();
			metaDataArray = convertToJson(metadataList);

			JSONObject itemsJson = new JSONObject();
			itemsJson.put(LeapDataContextConstant.TYPE, dataType);
			itemsJson.put(LeapDataContextConstant.ELEMENT, reqresData);

			JSONObject leapDataJson = new JSONObject();
			leapDataJson.put(LeapDataContextConstant.KIND, kind);
			leapDataJson.put(LeapDataContextConstant.META_DATA, metaDataArray);
			leapDataJson.put(LeapDataContextConstant.ITEMS, itemsJson);

			JSONObject leapDataElementJson = new JSONObject();

			leapDataElementJson.put("apiversion", apiversion);
			leapDataElementJson.put("context", context);
			leapDataElementJson.put("lang", lang);
			leapDataElementJson.put("data", leapDataJson);

			leapDataContextElementJson.put(tag, leapDataElementJson);
		}
		return leapDataContextElementJson;
	}

	/**
	 * Method to convert the MetaData into {@link JSONObject}
	 * 
	 * @param list
	 * @return {@link JSONArray}
	 */
	public static JSONArray convertToJson(List<MetaData> list) {
		JSONArray jsonArray = new JSONArray();
		for (MetaData metaData : list) {
			JSONObject formDetailsJson = new JSONObject();
			formDetailsJson.put(LeapDataContextConstant.ACTUAL_COLUMNNAME, metaData.getActualColumnName());
			formDetailsJson.put(LeapDataContextConstant.BYTE_LENGTH, metaData.getByteLenth());
			formDetailsJson.put(LeapDataContextConstant.TYPE, metaData.getType());
			formDetailsJson.put(LeapDataContextConstant.EFFECTIVE_COLUMNNAME, metaData.getEffectiveColumnName());
			// formDetailsJson.put("I18nLangText", metaData.getI18nLangText());
			// formDetailsJson.put("I18nID", metaData.getI18nID());
			jsonArray.put(formDetailsJson);
		}
		return jsonArray;
	}

	/**
	 * Custom Method to get the specific value for the specified key under the
	 * JSONObject
	 * 
	 * @param json
	 * @param key
	 * @return List<String>
	 */
	public static List<String> getValueFromJsonCustom(JSONObject json, String key) {
		String req = json.toString();
		String[] split = req.split("\"" + key + "\":");
		List<String> list = new ArrayList<>();
		for (int i = 1; i < split.length; i++) {
			String str = split[i].trim();
			char charAt = str.charAt(0);
			String output = null;
			switch (charAt) {
			case '{':
				output = parseJsonObj(str);
				break;
			case '[':
				output = parseJsonArr(str);
				break;
			case '"':
				output = parseString(str);
				break;
			default:
				output = String.valueOf(parseNumeric(str));
				break;
			}
			if (!output.matches("\\{"))
				list.add(output);
		}

		return list;
	}

	/**
	 * 
	 * @param str
	 * @return double
	 */
	private static double parseNumeric(String str) {
		int length = str.length();
		int index = 0;

		for (int i = 0; i < length; i++) {
			if (!Character.isDigit(str.charAt(i))) {
				if (str.charAt(i) != '.') {
					index = i;
					break;
				}
			}
		}
		return Double.parseDouble(str.substring(0, index));
	}

	/**
	 * 
	 * @param str
	 * @return String
	 */
	private static String parseString(String str) {
		int length = str.length();
		int index = 0;
		for (int i = 1; i < length; i++) {
			if (str.charAt(i) == '"') {
				index = i;
				break;
			}
		}
		return str.substring(1, index);
	}

	/**
	 * 
	 * @param str
	 * @return String
	 */
	private static String parseJsonObj(String str) {
		int length = str.length();
		int count = 1;
		int index = 0;
		for (int i = 1; i < length; i++) {
			if (str.charAt(i) == '{') {
				count++;
			} else if (str.charAt(i) == '}') {
				count--;
			}
			if (count == 0) {
				index = i;
				break;
			}
		}
		return str.substring(0, ++index);
	}

	/**
	 * 
	 * @param str
	 * @return String
	 */
	private static String parseJsonArr(String str) {
		int length = str.length();
		int count = 1;
		int index = 0;
		for (int i = 1; i < length; i++) {
			if (str.charAt(i) == '[') {
				count++;
			} else if (str.charAt(i) == ']') {
				count--;
			}
			if (count == 0) {
				index = i;
				break;
			}
		}
		return str.substring(0, ++index);
	}

	/**
	 * 
	 * @param map
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return List<MetaData>
	 */
	private static List<MetaData> parserMapData(Map<String, String> map, String taxonomyId,
			LeapDataContext leapDataContext) {
		JSONObject taxonomyData = new JSONObject();
		if (taxonomyId != null && !taxonomyId.isEmpty()) {
			String taxonomyFileName = taxonomyId + "_Taxonomy.json";
			String taxonomyFile = null;
			try {
				taxonomyFile = getFile(taxonomyFileName, LeapDataContextConstant.TAXONOMY_KEY, taxonomyId,
						leapDataContext);
			} catch (LeapDataContextInitialzerException | IOException e) {
				e.printStackTrace();
				if (taxonomyFileName.equalsIgnoreCase(LEAP_DEFAULT_TAXONOMY_FILE)) {
					taxonomyFile = "{}";
					logger.warn("{} unable to get {} , putting as empty" ,LEAP_LOG_KEY, taxonomyFileName );
				}
			}
			taxonomyData = new JSONObject(taxonomyFile);
			logger.debug("{} taxonomyData :: {}",LEAP_LOG_KEY, taxonomyData);
		} // ..end of if condition, to check if the taxonomy value is not null
			// and empty then fetch the file

		List<MetaData> list = new ArrayList<>();
		MetaData data;
		for (Entry<String, String> entry : map.entrySet()) {
			data = parseMetaData(entry.getValue(), entry.getKey(), taxonomyData);
			list.add(data);
		}
		return list;
	}

	/**
	 * 
	 * @param map
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return List<MetaData>
	 */
	private static List<MetaData> parserMapData(Map<String, String> map, LeapDataContext leapDataContext) {

		JSONObject taxonomyData = new JSONObject();
		List<MetaData> list = new ArrayList<>();
		MetaData data;
		for (Entry<String, String> entry : map.entrySet()) {
			data = parseMetaData(entry.getValue(), entry.getKey(), taxonomyData);
			list.add(data);
		}
		return list;
	}

	/**
	 * 
	 * @param map
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return List<MetaData>
	 */
	private static List<MetaData> getMapData(Map<String, String> map, Map<String, String> metaDataMap,
			LeapDataContext leapDataContext) {
		logger.trace("{} metaDataMap is : {}",LEAP_LOG_KEY, metaDataMap);
		List<MetaData> list = new ArrayList<>();
		String key = null;
		MetaData data;
		for (Entry<String, String> entry : map.entrySet()) {
			key = entry.getKey();
			logger.trace("{} key is : {}",LEAP_LOG_KEY, key);
			data = getMetaData(entry.getValue(), key);
			data.setActualColumnName(metaDataMap.get(key));
			data.setEffectiveColumnName(key);

			list.add(data);
		}
		return list;
	}

	/**
	 * 
	 * @param val
	 * @param key
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return {@link MetaData}
	 */
	private static MetaData parseMetaData(String val, String key, JSONObject taxonomyData) {
		String replacedKey;
		MetaData data = new MetaData();
		data.setActualColumnName(key);
		data.setByteLenth(key.length());
		data.setType(val);
		try {

			replacedKey = taxonomyData.getString(key);
			data.setEffectiveColumnName(replacedKey);
		} catch (JSONException e) {
			data.setEffectiveColumnName(key);
		}
		// data.setI18nID("dd");
		// data.setI18nLangText("sd");
		return data;
	}

	/**
	 * 
	 * @param val
	 * @param key
	 * @param taxonomyId
	 * @param leapDataContext
	 * @return {@link MetaData}
	 */
	private static MetaData getMetaData(String val, String key) {
		MetaData data = new MetaData();
		data.setEffectiveColumnName(key);
		data.setByteLenth(key.length());
		data.setType(val);

		return data;
	}

	/**
	 * 
	 * @param jsonIn
	 * @param map
	 * @throws LeapDataContextConfigException
	 */
	private static void findKeysOfJsonArray(JSONArray jsonIn, Map<String, String> map)
			throws LeapDataContextConfigException {
		try {
			if (jsonIn != null && jsonIn.length() != 0) {
				for (int i = 0; i < jsonIn.length(); i++) {
					if (jsonIn.get(i) instanceof JSONArray) {
						JSONArray jsonObjArray = jsonIn.getJSONArray(i);
						findKeysOfJsonArray(jsonObjArray, map);
					} else if (jsonIn.get(i) instanceof JSONObject) {
						JSONObject jsonObjIn = jsonIn.getJSONObject(i);
						findKeysOfJsonObject(jsonObjIn, map);
					}
				}
			}
		} catch (JSONException exp) {
			throw new LeapDataContextConfigException(exp.getMessage(), exp.getCause(),
					"Unable to parse and find keys from JsonArray", 400);
		}

	}

	/**
	 * 
	 * @param jsonIn
	 * @param map
	 * @throws LeapDataContextConfigException
	 */
	private static void findKeysOfJsonObject(JSONObject jsonIn, Map<String, String> map)
			throws LeapDataContextConfigException {
		Iterator<String> itr = jsonIn.keys();
		itr = jsonIn.keys();
		while (itr.hasNext()) {
			String itrStr = itr.next();
			Object obj = jsonIn.get(itrStr);
			JSONObject jsout = null;
			JSONArray jsArr = null;
			if (obj instanceof JSONObject) {
				map.put(itrStr, "Object");
				jsout = (JSONObject) obj;
				findKeysOfJsonObject(jsout, map);
			} else if (obj instanceof JSONArray) {
				map.put(itrStr, "Object");
				jsArr = (JSONArray) obj;
				findKeysOfJsonArray(jsArr, map);
			} else {
				if (obj instanceof String) {
					map.put(itrStr, "String");
				} else if (obj instanceof Integer) {
					map.put(itrStr, "Integer");
				} else if (obj instanceof Boolean) {
					map.put(itrStr, "Boolean");
				} else {
					map.put(itrStr, "Double");
				}
			}
		}
	}

	/**
	 * Method to construct MetaData
	 * 
	 * @param obj
	 * @param taxonomyId
	 * @return List<MetaData>
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaData(Object obj, String taxonomyId, LeapDataContext leapDataContext)
			throws LeapDataContextConfigException {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = (JSONArray) obj;
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = (JSONObject) obj;
			findKeysOfJsonObject(json, map);
		}
		return parserMapData(map, taxonomyId, leapDataContext);
	}

	/**
	 * Method to construct MetaData without taxonomyId
	 * 
	 * @param obj
	 * @param taxonomyId
	 * @return List<MetaData>
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaData(Object obj, LeapDataContext leapDataContext)
			throws LeapDataContextConfigException {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = (JSONArray) obj;
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = (JSONObject) obj;
			findKeysOfJsonObject(json, map);
		}
		return parserMapData(map, leapDataContext);
	}

	/**
	 * Method to construct MetaData
	 * 
	 * @param obj
	 * @param taxonomyId
	 * @return List<MetaData>
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaDataForResponse(Object obj, Map<String, String> metaDataMap,
			LeapDataContext leapDataContext) throws LeapDataContextConfigException {
		String methodName = "createMetaDataForResponse";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = (JSONArray) obj;
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = (JSONObject) obj;
			findKeysOfJsonObject(json, map);
		}
		return getMapData(map, metaDataMap, leapDataContext);
	}

	/**
	 * Method to construct MetaData from XML input data
	 * 
	 * @param obj
	 * @return List<MetaData>
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaDataForXMLData(Object obj, String taxonomyId,
			LeapDataContext leapDataContext) throws JSONException, LeapDataContextConfigException {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = convertXMLToJSONArray(obj);
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = convertXMLToJSONObject(obj);
			findKeysOfJsonObject(json, map);
		}
		return parserMapData(map, taxonomyId, leapDataContext);
	}

	/**
	 * Method to construct MetaData from XML input data
	 * 
	 * @param obj
	 * @return List<MetaData>
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaDataFormXMLDataForResponse(Object obj, Map<String, String> metaDataMap,
			LeapDataContext leapDataContext) throws JSONException, LeapDataContextConfigException {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = convertXMLToJSONArray(obj);
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = convertXMLToJSONObject(obj);
			findKeysOfJsonObject(json, map);
		}
		return getMapData(map, metaDataMap, leapDataContext);
	}

	/**
	 * Method to construct MetaData from XML input data without taxonomyId
	 * 
	 * @param obj
	 * @return List<MetaData>
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	public static List<MetaData> createMetaDataForXMLData(Object obj, LeapDataContext leapDataContext)
			throws JSONException, LeapDataContextConfigException {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = convertXMLToJSONArray(obj);
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = convertXMLToJSONObject(obj);
			findKeysOfJsonObject(json, map);
		}
		return parserMapData(map, leapDataContext);
	}

	/**
	 * 
	 * @param obj
	 * @return {@link JSONObject}
	 */
	private static JSONObject convertXMLToJSONObject(Object obj) {
		if (obj instanceof JSONObject) {
			return (JSONObject) obj;
		}
		String xml = obj.toString();
		JSONObject json = XML.toJSONObject(xml);
		return json;
	}

	/**
	 * 
	 * @param obj
	 * @return {@link JSONArray}
	 */
	private static JSONArray convertXMLToJSONArray(Object obj) {
		JSONArray newArray = new JSONArray();
		JSONArray array = (JSONArray) obj;
		for (Object eachObject : array) {
			JSONObject eachJson = XML.toJSONObject(eachObject.toString());
			newArray.put(eachJson);
		}
		return newArray;
	}

	/**
	 * For Transforming the intermediate data
	 * 
	 * @param jsArr
	 * @return {@link List}
	 */
	public static List<JSONObject> transform(JSONArray jsArr) {
		List<JSONObject> finalJson = new ArrayList<>();
		// JSONArray result = new JSONArray();
		for (int i = 0; i < jsArr.length(); i++) {
			JSONObject jsonObject = new JSONObject();
			jsonObject = transform(jsArr.getJSONObject(i));
			// result.put(jsonObject);
			finalJson.add(jsonObject);
		}
		// System.out.println("finalJson : " + finalJson);
		return finalJson;
	}

	/**
	 * 
	 * @param jsonObject
	 * @return {@link JSONObject}
	 */
	private static JSONObject transform(JSONObject jsonObject) {
		Iterator<String> keys = jsonObject.keys();
		String key;
		Object val;
		JSONArray modifyArray;
		while (keys.hasNext()) {
			key = keys.next();
			val = jsonObject.get(key);
			if (val instanceof JSONObject) {
				transform((JSONObject) val);
			} else if (val instanceof JSONArray) {
				modifyArray = modifyArray((JSONArray) val);
				jsonObject.put(key, modifyArray);
				transform(jsonObject.getJSONArray(key));
			}
		}
		return jsonObject;
	}

	/**
	 * 
	 * @param jsonArr
	 * @return {@link JSONArray}
	 */
	private static JSONArray modifyArray(JSONArray jsonArr) {
		int length = jsonArr.length();
		if (length == 1 || checkForDataCompression(jsonArr)) {
			return jsonArr;
		}
		JSONArray resultArr = new JSONArray();
		JSONObject js, temp;
		String key;
		JSONObject template = jsonArr.getJSONObject(0);
		for (int i = 1; i < length; i++) {
			js = new JSONObject(template.toString());
			temp = jsonArr.getJSONObject(i);
			if (temp.length() != 0) {
				key = getRootTag(temp);
				js.put(key, temp.get(key));
				resultArr.put(js);
			}
		}
		return resultArr;
	}

	/**
	 * 
	 * @param jsonArray
	 * @return {@link Boolean}
	 */
	public static boolean checkForDataCompression(JSONArray jsonArray) {
		boolean flag = false;
		JSONObject jsonObject = jsonArray.getJSONObject(0);
		for (int x = 1; x < jsonArray.length(); x++) {
			JSONObject innerJson = new JSONObject();
			innerJson = jsonArray.getJSONObject(x);
			// if (!(innerJson.isEmpty())) {
			String rootTag = getRootTag(innerJson);
			if (jsonObject.has(rootTag)) {
				flag = true;
			} else {
				flag = false;
			}
			// }
		}
		return flag;
	}

	/**
	 * Method to fetch file as resource using ResourceManagemant
	 * 
	 * @param fileName
	 * @param leapDataContext
	 * @return {@link String}
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 * @throws PermaStoreConfigRequestException
	 */
	public static String getFile(String fileName, String type, String vendorTaxonomyId, LeapDataContext leapDataContext)
			throws LeapDataContextInitialzerException, IOException {
		if (fileName.equalsIgnoreCase(LEAP_DEFAULT_TAXONOMY_FILE)) {
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			Object leapDefaultFile = leapServiceContext
					.getPermastoreByNameFromServiceContext(LEAP_DEFAULT_TAXONOMY_FILE);
			if (leapDefaultFile == null) {
				try {
					leapServiceContext.storePermastoreConfigToServiceContext(fileName);
					leapDefaultFile = leapServiceContext
							.getPermastoreByNameFromServiceContext(LEAP_DEFAULT_TAXONOMY_FILE);
				} catch (PermaStoreConfigRequestException e) {
					logger.warn("{} Unable to Store  Leap Default Taxonomy file in PermaStore",LEAP_LOG_KEY);
				}
			}
			if (leapDefaultFile == null) {
				leapDefaultFile = "{}";
				logger.warn("{} Not able to found LeapDefault_Taxonomy.json so setting as Empty json ",LEAP_LOG_KEY);
				// throw new LeapDataContextInitialzerException("Unable to find
				// Leap Default Taxonomy file ",
				// new Throwable(), "Resource Not Found!", 500);
			}
			return leapDefaultFile.toString();
		}
		String fileData = leapDataContext.getResourceFile(fileName, type);
		if (fileData != null) {
			return fileData;
		} else {
				fileData = sendGET(fileName, vendorTaxonomyId, leapDataContext);
				if (fileData != null && !fileData.isEmpty()) {
					JSONObject jsonRes = new JSONObject(fileData);
					Object object = jsonRes.getJSONObject("data").getJSONObject("items").getJSONObject("data")
							.get("Resources");
					JSONObject jsonData = new JSONObject();
					if (object instanceof JSONObject)
						jsonData = (JSONObject) object;
					else
						jsonData = ((JSONArray) object).getJSONObject(0);
					String encodedData = jsonData.getString("content");
					byte[] valueDecoded = Base64.decodeBase64(encodedData.getBytes());
					String fileNameFnd = jsonData.getString(RESOURCE_NAME);
					String responseData = new String(valueDecoded, Charset.forName("UTF-8"));
					storeLocally(fileNameFnd, responseData);
					return responseData;
				}
		}
		return null;
	}

	/**
	 * 
	 * @param fileNameFnd
	 * @param responseData
	 * @throws IOException
	 */
	private static void storeLocally(String fileNameFnd, String responseData) throws IOException {
		ClassPathHelper.addFile(fileNameFnd);
		Path path = Paths.get("src/main/resources/" + fileNameFnd);
		byte[] strToBytes = responseData.getBytes();
		Files.write(path, strToBytes);
	}

	/**
	 * 
	 * @param fileName
	 * @param leapDataContext
	 * @return
	 * @throws LeapDataContextInitialzerException
	 */
	private static String sendGET(String fileName, String vendorTaxonomyId, LeapDataContext leapDataContext)
			throws LeapDataContextInitialzerException {
		try {
			String url = constructResourceManagementURL(fileName, vendorTaxonomyId, leapDataContext);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			JSONObject privateHeaders = leapDataContext.getPrivateHeaders(LeapDataContextConstant.PRIVATE_HEADERS);
			con.setRequestProperty(LeapDataContextConstant.ACCOUNTID,
					privateHeaders.getString(LeapDataContextConstant.ACCOUNTID));
			con.setRequestProperty(LeapDataContextConstant.SITEID,
					privateHeaders.getString(LeapDataContextConstant.SITEID));
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				// print result
				return response.toString();
			} else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
				throw new LeapDataContextInitialzerException("Unable to find  file...! " + fileName, null,
						"Resource Not Found!", HttpURLConnection.HTTP_BAD_REQUEST);
			} else
				logger.error("{} GET request failed",LEAP_LOG_KEY);
		} catch (Exception e) {
			logger.error("Resource Not Found! "+e);
			throw new LeapDataContextInitialzerException(e.getMessage(), null, "Resource Not Found!",
					HttpURLConnection.HTTP_NOT_FOUND);
		}
		return null;

	}

	/**
	 * construct the resource management url by getting the resource management
	 * configuration.
	 * 
	 * @param fileName
	 * @param vendorTaxonomyId
	 * @param leapDataContext
	 * @return
	 * @throws ResourceManagementConfigurationException
	 * @throws UnsupportedEncodingException
	 */
	private static String constructResourceManagementURL(String fileName, String vendorTaxonomyId,
			LeapDataContext leapDataContext)
			throws ResourceManagementConfigurationException, UnsupportedEncodingException {
		String methodName = "constructResourceManagementURL";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IResourceManagementConfigService resourceManagement = new ResourceManagementConfigService();
		ConnectionInfo connectionInfo = resourceManagement
				.getConnectionInfoConfiguration(leapDataContext.getServiceDataContext().getRequestContext());
		GetResourceContent getResourceContentConfiguration = resourceManagement.getGetResourceContentConfiguration(
				leapDataContext.getServiceDataContext().getRequestContext(), vendorTaxonomyId);
		String host = connectionInfo.getHost();
		String port = connectionInfo.getPort();
		String baseUrl = connectionInfo.getBaseURI();
		String serviceName = getResourceContentConfiguration.getServiceName();
		String resourceURI = getResourceContentConfiguration.getResourceURI();
		String resourceScope = getResourceContentConfiguration.getResourceScope();
		String type = getResourceContentConfiguration.getType();
		String url = host.trim() + ":" + port.trim() + baseUrl.trim() + "/" + serviceName.trim() + "?" + URI + "="
				+ resourceURI.trim() + "&" + RESOURCE_NAME + "=" + fileName.trim() + "&" + RESOURCE_SCOPE + "="
				+ resourceScope.trim() + "&" + TYPE + "=" + type.trim();
		Boolean isURLSecured = connectionInfo.getIsURLSecured();
		if (isURLSecured) {
			url = HTTPS + url;
		} else {
			url = HTTP + url;
		}
		logger.trace("{} resourceManagement url is : {}" ,LEAP_LOG_KEY, url);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return url;
	}

	/**
	 * 
	 * @param originalData
	 * @return {@link Object}
	 */
	private static Object changeAccordingToTaxonomy(Object originalData) {
		// logger.debug("inside .changeAccordingToTaxonomy().. ");
		String rootKey = getRootKey(originalData);

		// TODO have to change it later
		JSONObject loadJson = null;
		if (originalData instanceof JSONObject) {
			loadJson = new JSONObject(originalData.toString());
		} else if (originalData instanceof String) {
			loadJson = XML.toJSONObject(originalData.toString());
		}

		JSONObject original = new JSONObject();
		Object object;
		if (rootKey != null) {
			object = loadJson.get(rootKey);
		} else
			object = loadJson;

		if (object instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) object;
			JSONArray resultJsonArry = new JSONArray();
			JSONObject resultJsonObj = null;
			for (int i = 0; i < jsonArray.length(); i++) {
				resultJsonObj = new JSONObject();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Iterator<String> keys = jsonObject.keys();
				while (keys.hasNext()) {
					String currentKey = keys.next();
					String replaceKeyWithOriginalKey = replaceKeyWithOriginalKey(currentKey, templateJson);
					Object value = jsonObject.get(currentKey);
					if (replaceKeyWithOriginalKey != null)
						resultJsonObj.put(replaceKeyWithOriginalKey, value);
					else
						resultJsonObj.put(currentKey, value);
				}
			}
			resultJsonArry.put(resultJsonObj);
			if (rootKey != null) {
				original.put(replaceKeyWithOriginalKey(rootKey, templateJson), resultJsonArry);
			} else
				return resultJsonArry;
		} else if (object instanceof JSONObject) {
			JSONObject resultJsonObj = new JSONObject();
			JSONObject jsonObject = (JSONObject) object;
			Iterator<String> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String currentKey = keys.next();
				String replaceKeyWithOriginalKey = replaceKeyWithOriginalKey(currentKey, templateJson);
				Object value = jsonObject.get(currentKey);
				if (replaceKeyWithOriginalKey != null)
					resultJsonObj.put(replaceKeyWithOriginalKey, value);
				else
					resultJsonObj.put(currentKey, value);
			}
			if (rootKey != null) {
				original.put(replaceKeyWithOriginalKey(rootKey, templateJson), resultJsonObj);
			} else
				return resultJsonObj;
		} else {
			JSONObject resultJsonObj = new JSONObject();
			Iterator<String> keys = loadJson.keys();
			while (keys.hasNext()) {
				String currentKey = keys.next();
				String replaceKeyWithOriginalKey = replaceKeyWithOriginalKey(currentKey, templateJson);
				Object value = loadJson.get(currentKey);
				if (replaceKeyWithOriginalKey != null)
					resultJsonObj.put(replaceKeyWithOriginalKey, value);
				else
					resultJsonObj.put(currentKey, value);
			}
			if (rootKey != null) {
				original.put(replaceKeyWithOriginalKey(rootKey, templateJson), resultJsonObj);
			} else
				return resultJsonObj;
		}
		return original;
	}

	/**
	 * 
	 * @param currentKey
	 * @param replceJsonObject
	 * @return {@link String}
	 */
	private static String replaceKeyWithOriginalKey(String currentKey, JSONObject replceJsonObject) {
		Set<String> keySet = replceJsonObject.keySet();
		boolean flag = keySet.stream().anyMatch(key -> key.equalsIgnoreCase(currentKey));
		if (flag) {
			String value = replceJsonObject.getString(currentKey);
			return value;
		} else {
			return currentKey;
		}
	}

	/**
	 * 
	 * @param object
	 * @return {@link String}
	 */
	private static String getRootKey(Object object) {
		JSONObject loadJson = null;
		JSONArray loadArray;
		if (object instanceof JSONObject) {
			loadJson = (JSONObject) object;
		} else if (object instanceof String) {
			loadJson = XML.toJSONObject(object.toString());
		}
		// else {
		// loadArray = (JSONArray) object;
		// for() {
		// }
		Iterator<String> keys = loadJson.keys();
		String key = null;
		while (keys.hasNext()) {
			key = keys.next();
			Object object2 = loadJson.get(key);
			if (object2 != null) {
				break;
			}
			return key;
		}
		return null;
	}

	/**
	 * 
	 * @param originalData
	 * @param response
	 * @param parentkey
	 * @return {@link JSONObject}
	 */
	private static JSONObject changeAccordingToTaxonomyModified(Object originalData, JSONObject response,
			String parentkey) {

		JSONObject contextData = null;
		if (originalData instanceof JSONObject) {
			contextData = new JSONObject(originalData.toString());
		} else if (originalData instanceof String) {
			contextData = XML.toJSONObject(originalData.toString());
		}

		logger.debug("{} keys from original data to modify acording to taxonamy ::: {}" ,LEAP_LOG_KEY, contextData.keySet());
		Iterator<String> keys = contextData.keys();
		String key;
		Object value = null, actualVal;
		JSONArray valueFromContext, tempValues;
		JSONObject tempObj;
		boolean isNotJson = true;
		while (keys.hasNext()) {
			key = keys.next();
			value = contextData.get(key);
			actualVal = mergeValues(value);
			isNotJson = !actualVal.getClass().getName().contains("JSON");
			if (isNotJson) {
				String key1 = getVendorTaxonomy(key);
				response.put(key1, actualVal);
				// response.put(key, actualVal);
			} else {
				if (actualVal instanceof JSONObject) {
					tempObj = new JSONObject();
					changeAccordingToTaxonomyModified((JSONObject) actualVal, tempObj, key);
					if (tempObj.has(key)) {
						String key2 = getVendorTaxonomy(key);
						response.put(key2, tempObj.get(key));
						// response.put(key, tempObj.get(key));
					} else {
						String key3 = getVendorTaxonomy(key);
						response.put(key3, tempObj);
						// response.put(key, tempObj);
					}
				} else {
					valueFromContext = (JSONArray) actualVal;
					tempValues = new JSONArray();
					String key4 = getVendorTaxonomy(key);
					response.put(key4, tempValues);
					// response.put(key, tempValues);
					for (int i = 0; i < valueFromContext.length(); i++) {
						tempObj = new JSONObject();
						changeAccordingToTaxonomyModified(valueFromContext.getJSONObject(i), tempObj, key);
						if (tempObj.has(key)) {
							tempValues.put(tempObj.get(key));
						} else {
							tempValues.put(tempObj);
						}
					}
				}
			}
		}
		logger.trace("{} changeAccordingToTaxonomyModified response :{}",LEAP_LOG_KEY,response);
		return response;
	}

	/**
	 * 
	 * @param key
	 * @return {@link String}
	 */
	private static String getVendorTaxonomy(String key) {
		logger.trace("{} inside getVendorTaxonomy method ::: {} ",LEAP_LOG_KEY, key);
		String changedKey = null;
		if (templateJson.has(key)) {
			changedKey = templateJson.getString(key);
			return changedKey;
		} else {
			return key;
		}
	}

	/**
	 * 
	 * @param value
	 * @return {@link Object}
	 */
	private static Object mergeValues(Object value) {
		return value == null ? "" : value;
	}

	public static boolean checkForNullAndEmpty(String key) {
		if (key != null) {
			if (!key.isEmpty()) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}

	}

}
