package com.attunedlabs.leap.transform;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingService;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingServiceException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TransformDataTest {

	private Exchange exchange;
	private TransformData transformData;
	private Message message;
	private final String CAMEL_HTTP_URI = "http://0.0.0.0:9070/"+PRETTY_URI_TEST;

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (transformData == null)
			transformData = new TransformData();
		message = exchange.getIn();
	}

	/**
	 * This method is used to convert xml request data into JSON format with Content
	 * Type application/json
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testMarshalXmltoJson() throws JSONException, RequestDataTransformationException {
		message.setHeader(CONTENT_TYPE, "application/json");
		message.setHeader(SERVICENAME, TEST_SERVICE);
		message.setBody(DEMO_XML_DATA);

		Object body = message.getBody();
		Assert.assertEquals("exchnage body should be xml String data ::", DEMO_XML_DATA, body);

		transformData.marshalXmltoJson(exchange);

		body = message.getBody();
		Assert.assertNotEquals("exchnage body should not be xml data ::", DEMO_XML_DATA, body);
		Assert.assertEquals("exchnage body data should be json Type ::", JSONObject.class, body.getClass());
	}

	/**
	 * This method is used to convert xml request data into JSON format with Content
	 * Type application/x-www-form-urlencoded and with exchange body.
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testMarshalXmltoJsonWithContentTypeUrlencodedAndSetBody()
			throws JSONException, RequestDataTransformationException {
		message.setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
		message.setHeader(SERVICENAME, TEST_SERVICE);
		message.setBody(DEMO_XML_DATA);

		Object body = message.getBody();
		Assert.assertEquals("exchnage body should be xml String data ::", DEMO_XML_DATA, body);

		transformData.marshalXmltoJson(exchange);

		body = message.getBody();
		Assert.assertNotEquals("exchnage body should not be xml data ::", DEMO_XML_DATA, body);
		Assert.assertEquals("exchnage body data should be json Type ::", JSONObject.class, body.getClass());
	}

	/**
	 * This method is used to convert xml request data into JSON format with Content
	 * Type application/x-www-form-urlencoded and withOut exchange body.
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testMarshalXmltoJsonWithContentTypeUrlencoded()
			throws JSONException, RequestDataTransformationException {
		message.setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
		message.setHeader("msg", DEMO_XML_DATA);
		message.setHeader(SERVICENAME, TEST_SERVICE);

		transformData.marshalXmltoJson(exchange);

		Object body = message.getBody();
		Assert.assertNotEquals("exchnage body should not be xml data ::", DEMO_XML_DATA, body);
		Assert.assertEquals("exchnage body data should be json Type ::", JSONObject.class, body.getClass());
	}

	/**
	 * Custom bean to unmarshal the JSON string to XML
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testUnmarshalJsonToXML() throws JSONException, RequestDataTransformationException {
		message.setBody(DEMO_JSON_DATA);

		transformData.unmarshalJsonToXML(exchange);

		Object body = message.getBody();
		Assert.assertNotNull("Body Exchange Should not be null ", body);
		Assert.assertNotEquals("body Data Should not be match of set exchange body :", DEMO_JSON_DATA, body);
		Assert.assertEquals(String.class, body.getClass());

	}

	/**
	 * Custom bean to unmarshal the JSON string with Data root tag to XML
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testUnmarshalJsonToXMLWithJsonWithData() throws JSONException, RequestDataTransformationException {
		message.setBody("{\"data\":[{\"demo\":\"testing\"}]}");

		transformData.unmarshalJsonToXML(exchange);

		Object body = message.getBody();
		Assert.assertNotNull("Body Exchange Should not be null ", body);
		Assert.assertEquals(String.class, body.getClass());

	}

	/**
	 * Custom bean to unmarshal the JSON string to XML for XML ENDPOINT
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testUnmarshalJsonToXMLForXmlEndpointWithJson()
			throws JSONException, RequestDataTransformationException {

		message.setBody(DEMO_JSON_DATA);

		transformData.unmarshalJsonToXMLForXmlEndpoint(exchange);

		Object body = message.getBody();

		Assert.assertNotNull("Body Exchange Should not be null ", body);
		Assert.assertNotEquals("body Data Should not be match of set exchange body :", DEMO_JSON_DATA, body);
		Assert.assertEquals(String.class, body.getClass());
	}

	/**
	 * Custom bean to unmarshal the XML string to XML for XML ENDPOINT
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testUnmarshalJsonToXMLForXmlEndpointWithXML() throws JSONException, RequestDataTransformationException {

		message.setBody(DEMO_XML_DATA);

		transformData.unmarshalJsonToXMLForXmlEndpoint(exchange);

		Object body = message.getBody();

		Assert.assertNotNull("Body Exchange Should not be null ", body);
		Assert.assertEquals("body Data Should not be match of set exchange body :", DEMO_XML_DATA, body);
		Assert.assertEquals(String.class, body.getClass());
	}

	@Test
	public void testTransformRequestData() throws JSONException, RequestDataTransformationException {

		message.setBody(DEMO_JSON_DATA);

		transformData.transformRequestData(exchange);

		Object body = message.getBody();

		Assert.assertNotNull("Body Exchange Should not be null ", body);
		Assert.assertEquals("body Data Should not be match of set exchange body :", DEMO_JSON_DATA, body);
		Assert.assertEquals(String.class, body.getClass());

		Object header = message.getHeader(SERVICENAME);
		Assert.assertNotNull("Body Exchange Should not be null ", body);

		JSONObject jsonObject = new JSONObject(DEMO_JSON_DATA);
		Assert.assertEquals("header serviceName Data Should not be match as expected :", jsonObject.keys().next(),
				header);

	}

	/**
	 * This method used for transform POST request data.
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testTransformRestRequestDataWithPost() throws JSONException, RequestDataTransformationException {
		message.setHeader(CAMEL_HTTP_METHOD, HTTP_POST);
		message.setBody(DEMO_DATA);

		String transformRestRequestData = transformData.transformRestRequestData(exchange);
		Assert.assertNotNull("TransformRestRequest Data Should not be null :", transformRestRequestData);
		Assert.assertEquals("TransformRestRequest data should be same as exchnage body ::", DEMO_DATA,
				transformRestRequestData);
	}

	/**
	 * This method used for transform GET request data.
	 * 
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	@Test
	public void testTransformRestRequestDataWithGet() throws JSONException, RequestDataTransformationException {
		message.setHeader(CAMEL_HTTP_METHOD, HTTP_GET);
		message.setHeader(CAMEL_HTTP_QUERT, DEMO_CAMEL_HTTP_QUERY_DATA);

		String transformRestRequestData = transformData.transformRestRequestData(exchange);
		Assert.assertNotNull("TransformRestRequest Data Should not be null :", transformRestRequestData);

		JSONObject jsonObject = new JSONObject(transformRestRequestData);
		Assert.assertNotNull("jsonObject should not be null ::", jsonObject);
		Assert.assertEquals("jsonobject root key should be sama as 'data' :", "data", jsonObject.keys().next());
	}

	/**
	 * this method use for loadActualUri.
	 * 
	 * @throws RequestDataTransformationException
	 * @throws PrettyUrlMappingServiceException
	 */
	@Test
	public void testLoadActualUri() throws RequestDataTransformationException, PrettyUrlMappingServiceException {
		addPrettyUrl();
		message.setHeader(ACCOUNTID, TEST_ACCOUNT);
		message.setHeader(SITEID, TEST_SITE);
		message.setHeader("CamelHttpUri", CAMEL_HTTP_URI);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange header not have featureGroup data then it should be null ::",
				headers.get(FEATURE_GROUP));
		Assert.assertNull("Exchange header not have feature data then it should be null ::", headers.get(FEATURE));
		Assert.assertNull("Exchange header not have ServiceName data then it should be null ::",
				headers.get(SERVICENAME));

		transformData.loadActualUri(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNotNull("After Exchange header have featureGroup data then it should be Not null ::",
				headers.get(FEATURE_GROUP));
		Assert.assertEquals("header FeatureGroup Data should be same as TestFeatureGroup :: ", TEST_FEATUREGROUP,
				headers.get(FEATURE_GROUP));

		Assert.assertNotNull("After Exchange header have feature data then it should be Not null ::",
				headers.get(FEATURE));
		Assert.assertEquals("header Feature Data should be same as TestFeature :: ", TEST_FEATURE,
				headers.get(FEATURE));

		Assert.assertNotNull("After Exchange header have ServiceName data then it should be Not null ::",
				headers.get(SERVICENAME));
		Assert.assertEquals("header ServiceName Data should be same as TestService :: ", TEST_SERVICE,
				headers.get(SERVICENAME));
	}


	/**
	 * this method use for loadActualUri without adding header of camelHttpURi.
	 * 
	 * @throws RequestDataTransformationException
	 * @throws PrettyUrlMappingServiceException
	 */
	@Test(expected = NullPointerException.class)
	public void testLoadActualUriWithoutCamelHttpUri()
			throws RequestDataTransformationException, PrettyUrlMappingServiceException {

		addPrettyUrl();
		message.setHeader(ACCOUNTID, TEST_TENANT);
		message.setHeader(SITEID, TEST_SITE);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange header not have featureGroup data then it should be null ::",
				headers.get(FEATURE_GROUP));
		Assert.assertNull("Exchange header not have feature data then it should be null ::", headers.get(FEATURE));
		Assert.assertNull("Exchange header not have ServiceName data then it should be null ::",
				headers.get(SERVICENAME));

		transformData.loadActualUri(exchange);
	}

	private void addPrettyUrl() throws PrettyUrlMappingServiceException {
		PrettyUrlMapping mapping = new PrettyUrlMapping(TEST_TENANT, TEST_SITE, PRETTY_URI_TEST, ACTUAL_URL, 1);
		PrettyUrlMappingService service = new PrettyUrlMappingService();
		service.addPrettyUrlMappingInDBAndCache(mapping);
		addPrettyUrlMappingInCache(mapping);

	}

	private void addPrettyUrlMappingInCache(PrettyUrlMapping prettyUrlMapping) {
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureServiceKey());
		Map<String, Serializable> siteMap = new HashMap<>();
		Map<String, String> prettyUriMap = new HashMap<>();
		prettyUriMap.put(prettyUrlMapping.getPrettyString(), prettyUrlMapping.getActualString());
		siteMap.put(prettyUrlMapping.getSiteId(), (Serializable) prettyUriMap);
		map.put(prettyUrlMapping.getTenantId(), (Serializable) siteMap);

	}

	private static String getGlobalFeatureServiceKey() {
		return "GlobalFeatureService".trim();
	}
}
