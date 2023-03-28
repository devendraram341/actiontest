package com.attunedlabs.leap.util;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.util.LeapConfigUtilException;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.eventframework.config.EventRequestContext;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;

@SuppressWarnings("static-access")
public class LeapConfigurationUtilTest {

	private Exchange exchange;
	private LeapConfigurationUtil configurationUtil;
	private Message message;
	private LeapDataContext leapDataCtx;
	private FeatureDeploymentTestConfigDB configDB;
	private String jsonString = "{\"root\":{\"name\":\"jai Prakash\",\"phone\":\"7004397645\",\"city\":\"Bettiah\",\"state\":\"Bihar\",\"pin\":\"845438\"}}";
	private String xmlString = "<root><name>Jai Prakash Sah</name><phone>7004397645</phone><city>Bettiah</city><state>Bihar</state><pin>845438</pin></root>";

	private Logger logger = LoggerFactory.getLogger(LeapConfigurationUtilTest.class);

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (configurationUtil == null)
			configurationUtil = new LeapConfigurationUtil();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		if (configDB == null)
			configDB = new FeatureDeploymentTestConfigDB();
		message = exchange.getIn();
	}

	/**
	 * This method is used to get LDC from exchange
	 */
	@Test
	public void testGetLDCFromExchange() {
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		LeapDataContext ldcFromExchange = configurationUtil.getLDCFromExchange(exchange);

		Assert.assertNotNull("LeapDataContext should not be null ::", ldcFromExchange);
		Assert.assertEquals(leapDataCtx, ldcFromExchange);
	}

	/**
	 * This method help us to get RequestBody from LDC either without tag name or
	 * from initialRequest without addContextElemet.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetRequestBodyFromLDCWithoutAddContextElement() {
		configurationUtil.getRequestBodyFromLDC(leapDataCtx, null);
	}

	/**
	 * This method help us to get RequestBody from LDC without tag name or from
	 * initialRequest with addContextElemet.
	 */
	@Test
	public void testGetRequestBodyFromLDCWithAddContextElementAndWithoutTag() {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		String requestBodyFromLDC = configurationUtil.getRequestBodyFromLDC(leapDataCtx, null);
		Assert.assertNotNull("Request Body From LDC should not be null ::", requestBodyFromLDC);
		Assert.assertEquals("Request Body From LDC data Should be Same as Expected Data ::", DEMO_JSON_DATA,
				requestBodyFromLDC);
	}

	/**
	 * This method help us to get RequestBody from LDC by tag name with
	 * addContextElemet.
	 */
	@Test
	public void testGetRequestBodyFromLDCWithAddContextElementAndTag() {
		LeapCoreTestUtils.addContextElementWithTag(leapDataCtx);
		JSONObject jsonObject = new JSONObject(DEMO_LEAP_DATA);

		String requestBodyFromLDC = configurationUtil.getRequestBodyFromLDC(leapDataCtx, TAG_NAME);
		Assert.assertNotNull("Request Body From LDC should not be null ::", requestBodyFromLDC);
		Assert.assertEquals("Request Body From LDC data Should be Same as Expected Data ::",
				new JSONObject(requestBodyFromLDC).toString(), jsonObject.toString());
	}

	/**
	 * This method is used to set the response code for the exchange
	 */
	@Test
	public void testSetResponseCode() {

		Object body = message.getBody();
		Assert.assertNull("Exchange Body Should be null ::", body);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertEquals("Exchange Header Should be Zero (0) ::", 0, headers.size());

		configurationUtil.setResponseCode(404, exchange, "page not found");

		body = message.getBody();
		Assert.assertNotNull("after setResponse then Exchange Body Should not be null ::", body);

		headers = message.getHeaders();
		Assert.assertEquals("after setResponse then Exchange Header Should be Four (4) ::", 4, headers.size());
	}

	@Test
	public void testStoreAllEntitiesConfigurationInServiceContext() throws LeapDataServiceConfigurationException {

		configDB.addFeatureDeployement();

		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		Map<String, Object> entitesConfig = leapDataCtx.getServiceDataContext().getEntitesConfigFromServiceContext();
		logger.debug("entitesConfigentitesConfig 1 " + entitesConfig);

		configurationUtil.storeAllEntitiesConfigurationInServiceContext(exchange);

		entitesConfig = leapDataCtx.getServiceDataContext().getEntitesConfigFromServiceContext();
		logger.debug("entitesConfigentitesConfig 2 " + entitesConfig);

		configDB.deleteFeatureDeployement();
	}

	@Test
	public void testStoreEntityConfigurationInServiceContext() throws LeapDataServiceConfigurationException {
		configDB.addFeatureDeployement();
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.storeEntityConfigurationInServiceContext("TestConfig", exchange);

		configDB.deleteFeatureDeployement();
	}

	@Test
	public void testStorePermastoreConfigurationInServiceContext()
			throws JSONException, PermaStoreConfigRequestException {
		configDB.addFeatureDeployement();
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.storePermastoreConfigurationInServiceContext("TestConfig", exchange);

		configDB.deleteFeatureDeployement();
	}

	// TODO
	@Test
	public void testGetPolicyConfiguration() {
//		Utils.addContextElementWithLeapTag(leapDataCtx);
//		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
//		configurationUtil.getPolicyConfiguration("TestConfig", exchange);
	}

	/**
	 * This method is used to initialize EventRequestContext object with data store
	 * in leapHeader
	 */
	@Test
	public void testInitializeEventRequestContext() {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		EventRequestContext initializeEventRequestContext = configurationUtil.initializeEventRequestContext(exchange);
		Assert.assertNotNull("Initialize event request context should not be null ::", initializeEventRequestContext);
		Assert.assertEquals("Initialize event request context feature group should be same as TestFeatureGroup ::",
				TEST_FEATUREGROUP, initializeEventRequestContext.getFeatureGroup());
		Assert.assertEquals("Initialize event request context feature should be same as TestFeature ::", TEST_FEATURE,
				initializeEventRequestContext.getFeatureName());
		Assert.assertEquals("Initialize event request context siteId should be same as TestSite ::", TEST_SITE,
				initializeEventRequestContext.getSite());
	}

	// TODO
	@Test
	public void testCompareDataContext() {

	}

	/**
	 * this method get Integration PipeContext data with set IntegrationPipeContext.
	 */
	@Test
	public void testGetIntegrationPipeContextWithSet() {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		Object body = message.getBody();
		Assert.assertNull("Exchange Body should be null ::", body);

		configurationUtil.setIntegrationPipeContext(DEMO_DATA, exchange);

		configurationUtil.getIntegrationPipeContext(exchange);

		body = message.getBody();
		Assert.assertNotNull("Exchange Body should be null ::", body);
		Assert.assertEquals("Exchange Body should be same as set IntegrationPipeContext Data ::", DEMO_DATA, body);
	}

	/**
	 * this method get Integration PipeContext data without set
	 * IntegrationPipeContext.
	 */

	@Test
	public void testGetIntegrationPipeContextWithoutSet() {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		Object body = message.getBody();
		Assert.assertNull("Exchange Body should be null ::", body);

		configurationUtil.getIntegrationPipeContext(exchange);

		body = message.getBody();
		Assert.assertNull("Exchange Body should be null ::", body);
	}

	// TODO
	@Test
	public void testGetIntegrationPipelineConfiguration() {

	}

	// TODO
	@Test
	public void testGetDataContext() {
//		DataContext dataContext = configurationUtil.getDataContext(null, null);
//		logger.debug("dataContextdataContextdataContextdataContext "+dataContext);
	}

	/**
	 * This method is used to get the request body json from LDC by tag
	 * 
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 */
	@Test
	public void testJsonToXMLFormLDCGetTag()
			throws LeapDataServiceConfigurationException, LeapDataContextInitialzerException, JSONException {
		leapDataCtx.addContextElement(new JSONObject(DEMO_JSON_DATA), "TestJsonTag", "TestJsonTag", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		String jsonToXMLFormLDCGetTag = configurationUtil.jsonToXMLFormLDCGetTag("TestJsonTag", exchange);
		Assert.assertNotNull("JsonToXML data should not be null ::", jsonToXMLFormLDCGetTag);

		JSONObject jsonObject = XML.toJSONObject(jsonToXMLFormLDCGetTag);
		Assert.assertNotNull("jsonObject should not be null ::", jsonObject);
		Assert.assertTrue("Inside jsonObject Should have atleast one jsonObject is required ::",
				jsonObject.length() > 0);
		Assert.assertEquals("jsonObject data should be same as AddContextElemnt Data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to get the request body json from LDC without tag
	 * 
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 */
	@Test
	public void testJsonToXMLFormLDCWithoutGetTag()
			throws LeapDataServiceConfigurationException, LeapDataContextInitialzerException, JSONException {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		String jsonToXMLFormLDCGetTag = configurationUtil.jsonToXMLFormLDCGetTag(null, exchange);
		Assert.assertNotNull("JsonToXML data should not be null ::", jsonToXMLFormLDCGetTag);

		JSONObject jsonObject = XML.toJSONObject(jsonToXMLFormLDCGetTag);
		Assert.assertNotNull("jsonObject should not be null ::", jsonObject);
		Assert.assertTrue("Inside jsonObject Should have atleast one jsonObject is required ::",
				jsonObject.length() > 0);
		Assert.assertEquals("jsonObject data should be same as AddContextElemnt Data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to get the request body xml from LDC by tag
	 * 
	 * @throws LeapDataServiceConfigurationException
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 */
	@Test(expected = JSONException.class)
	public void testJsonToXMLFormLDCGetTagWithXML()
			throws LeapDataServiceConfigurationException, LeapDataContextInitialzerException, JSONException {
		leapDataCtx.addContextElement(new JSONObject(DEMO_XML_DATA), "TestJsonTag", "TestJsonTag", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.jsonToXMLFormLDCGetTag("TestJSonTag", exchange);

	}

	/**
	 * This method is used to get the request body JSON from LDC by tag, and convert
	 * it into xml string and Push it to LDC using PushTag.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testJsonToXMLWithLDC()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		leapDataCtx.addContextElement(new JSONObject(DEMO_JSON_DATA), "TestJsonGetTag", "TestJsonGetTag", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.jsonToXMLWithLDC("TestJsonGetTag", "TestJsonPushTag", exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("TestJsonPushTag");
		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should not XML  ::", "XML", type);

		Object data = contextElement.getData().getItems().getData();
		Assert.assertNotNull("data should not be null :", data);

		JSONObject jsonObject = XML.toJSONObject(data.toString());
		Assert.assertNotNull("jsonObject data should not be null ::", jsonObject);
		Assert.assertEquals("JSonObject data should be same as expected data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to get the request body JSON from LDC by without tag, and
	 * convert it into xml string and Push it to LDC using PushTag.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testJsonToXMLWithLDCWithoutGetTag()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.jsonToXMLWithLDC(null, "TestJsonPushTag", exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("TestJsonPushTag");
		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be XML  ::", "XML", type);

		Object data = contextElement.getData().getItems().getData();
		Assert.assertNotNull("data should not be null :", data);

		JSONObject jsonObject = XML.toJSONObject(data.toString());
		Assert.assertNotNull("jsonObject data should not be null ::", jsonObject);
		Assert.assertEquals("JSonObject data should be same as expected data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to get the request body XML from LDC by with tag, and
	 * convert it into xml string and Push it to LDC using PushTag.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test(expected = JSONException.class)
	public void testJsonToXMLWithLDCWithXML()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		leapDataCtx.addContextElement(new JSONObject(DEMO_XML_DATA), "TestJsonGetTag", "TestJsonGetTag", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.jsonToXMLWithLDC("TestJsonGetTag", "TestJsonPushTag", exchange);

		leapDataCtx.getContextElement("TestJsonPushTag");
	}

	/**
	 * This method is used to convert json Object String to XML
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testJsonToXMLFromObjectWithJson() throws LeapDataServiceConfigurationException {
		String jsonToXMLFromObject = configurationUtil.jsonToXMLFromObject(DEMO_JSON_DATA);

		Assert.assertNotNull("JsonToXML data should not be null ::", jsonToXMLFromObject);

		JSONObject jsonObject = XML.toJSONObject(jsonToXMLFromObject);
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);
		Assert.assertEquals("jsonObject data Should be same as Expected data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to convert XML Object String
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test(expected = LeapDataServiceConfigurationException.class)
	public void testJsonToXMLFromObjectWithXML() throws LeapDataServiceConfigurationException {
		configurationUtil.jsonToXMLFromObject(DEMO_XML_DATA);
	}

	/**
	 * This method is used to get body from exchange and convert JSON into xml
	 * string
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testJsonToXMLFromExchangeWithJSON() throws LeapDataServiceConfigurationException {
		message.setBody(DEMO_JSON_DATA);
		String jsonToXMLFromExchange = configurationUtil.jsonToXMLFromExchange(exchange);

		Assert.assertNotNull("JsonToXML data should not be null ::", jsonToXMLFromExchange);

		JSONObject jsonObject = XML.toJSONObject(jsonToXMLFromExchange);
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);
		Assert.assertEquals("jsonObject data Should be same as Expected data ::", DEMO_JSON_DATA,
				jsonObject.toString());
	}

	/**
	 * This method is used to get body from exchange XML String
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */

	@Test(expected = LeapDataServiceConfigurationException.class)
	public void testJsonToXMLFromExchangeWithXML() throws LeapDataServiceConfigurationException {
		message.setBody(DEMO_XML_DATA);
		configurationUtil.jsonToXMLFromExchange(exchange);
	}

	/**
	 * This method get xml body from LDC by without tag and Convert xml into json
	 * string
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJsonWithoutTagName() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_XML_DATA, TAG_NAME_LEAP_INITIAL, TAG_NAME_LEAP_INITIAL, null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String xmlToJson = configurationUtil.xmlToJson(null, Collections.emptySet(),exchange);
		Assert.assertNotNull("XML to JSON data Should not be null ::", xmlToJson);

		JSONObject jsonObject = new JSONObject(xmlToJson);
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);

		String xml = XML.toString(jsonObject);
		Assert.assertNotNull("xml Data Should not be null ::", xml);
		Assert.assertEquals("XML Data Should be Same as Expected Data ::", DEMO_XML_DATA, xml);
	}

	/**
	 * This method get xml body from LDC by tag and Convert xml into json string
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJsonWithTagName() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_XML_DATA, "TestTagName", "TestTagName", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String xmlToJson = configurationUtil.xmlToJson("TestTagName",Collections.emptySet(), exchange);
		Assert.assertNotNull("XML to JSON data Should not be null ::", xmlToJson);

		JSONObject jsonObject = new JSONObject(xmlToJson);
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);

		String xml = XML.toString(jsonObject);
		Assert.assertNotNull("xml Data Should not be null ::", xml);
		Assert.assertEquals("XML Data Should be Same as Expected Data ::", DEMO_XML_DATA, xml);
	}

	/**
	 * This method get xml body from LDC by tag and try to Convert json into json
	 * string
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJSonWithJSONDataFailed() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_JSON_DATA, "TestTagName", "TestTagName", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String xmlToJson = configurationUtil.xmlToJson("TestTagName",Collections.emptySet(), exchange);
		Assert.assertNotNull("xml Data should not be null ::", xmlToJson);
		Assert.assertEquals("xmlToJson Data Should be Empty json ::", "{}", xmlToJson);
	}

	/**
	 * This method get xml body from Exchange and save it in Exchange as well
	 */
	@Test
	public void testXmlToJsonWithExchangeWithXml() {
		message.setBody(DEMO_XML_DATA);

		configurationUtil.xmlToJsonWithExchange(Collections.emptySet(),exchange);
		Object body = message.getBody();
		Assert.assertNotNull("XML to JSON data Should not be null ::", body);

		JSONObject jsonObject = new JSONObject(body.toString());
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);

		String xml = XML.toString(jsonObject);
		Assert.assertNotNull("xml Data Should not be null ::", xml);
		Assert.assertEquals("XML Data Should be Same as Expected Data ::", DEMO_XML_DATA, xml);
	}

	/**
	 * This method get JSON body from Exchange and save it in Exchange as well
	 */
	@Test
	public void testXmlToJsonWithExchangeWithJsonFailed() {
		message.setBody(DEMO_JSON_DATA);

		configurationUtil.xmlToJsonWithExchange(Collections.emptySet(),exchange);
		Object body = message.getBody();
		Assert.assertNotNull("xml Data should not be null ::", body);
		Assert.assertEquals("xmlToJson Data Should be Empty json ::", "{}", body.toString());
	}

	/**
	 * This method is used to get xml body from LDC by without tag and xml Convert
	 * into json string and Push it to LDC using push tag
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJsonWithLDCWithXMLDataWithoutTag() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_XML_DATA, TAG_NAME_LEAP_INITIAL, TAG_NAME_LEAP_INITIAL, null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.xmlToJsonWithLDC(null, "TestPushTag",Collections.emptySet(), exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("TestPushTag");

		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be JOSN  ::", "JSON", type);

		Object data = contextElement.getData().getItems().getData();
		logger.debug("datadatadata " + data.getClass());
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", new JSONObject().getClass(), data.getClass());

		String string = XML.toString(data);
		Assert.assertNotNull("String data Should not be null :", string);
		Assert.assertEquals("Actual Data Should be Same as Expected Data ::", DEMO_XML_DATA, string);
	}

	/**
	 * This method is used to get xml body from LDC by tag and xml Convert into json
	 * string and Push it to LDC using push tag
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJsonWithLDCWithXMLDataWithTag() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_XML_DATA, "TestGetTag", "TestGetTag", null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.xmlToJsonWithLDC("TestGetTag", "TestPushTag",Collections.emptySet(), exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("TestPushTag");

		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be JOSN  ::", "JSON", type);

		Object data = contextElement.getData().getItems().getData();
		logger.debug("datadatadata " + data.getClass());
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", new JSONObject().getClass(), data.getClass());

		String string = XML.toString(data);
		Assert.assertNotNull("String data Should not be null :", string);
		Assert.assertEquals("Actual Data Should be Same as Expected Data ::", DEMO_XML_DATA, string);
	}

	/**
	 * This method is used to get xml body from LDC without tag and tried json
	 * Convert into json string and Push it to LDC using push tag
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testXmlToJsonWithLDCWithJSONDataFailed() throws LeapDataContextInitialzerException {
		leapDataCtx.addContextElement(DEMO_JSON_DATA, TAG_NAME_LEAP_INITIAL, TAG_NAME_LEAP_INITIAL, null);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.xmlToJsonWithLDC(null, "TestPushTag", Collections.emptySet(),exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("TestPushTag");

		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be JOSN  ::", "JSON", type);

		Object data = contextElement.getData().getItems().getData();
		logger.debug("datadatadata " + data.getClass());
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", "{}", data.toString());
	}

	/**
	 * This method is used to convert Document object into json string
	 * 
	 * @throws IOException
	 * @throws TransformerException
	 * @throws LeapConfigUtilException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testXmlToJsonWithDocument()
			throws IOException, TransformerException, LeapConfigUtilException, LeapDataServiceConfigurationException {

		String xmlToJsonWithDocument = configurationUtil.xmlToJsonWithDocument(getDocFile(),Collections.emptySet());
		Assert.assertNotNull("XML to JSon data should not be null ::", xmlToJsonWithDocument);

		JSONObject jsonObject = new JSONObject(xmlToJsonWithDocument.toString());
		Assert.assertNotNull("JsonObject Should not be null ::", jsonObject);

		String xml = XML.toString(jsonObject);
		Assert.assertNotNull("xml Data Should not be null ::", xml);
		Assert.assertEquals("XML Data Should be Same as Expected Data ::", DEMO_XML_DATA, xml);
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * pushing back to LDC with xml
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataWithLDCWithXML()
			throws LeapDataContextInitialzerException, LeapDataServiceConfigurationException {

		leapDataCtx.addContextElement(xmlString, "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.transformDataWithLDC("test-XSLT.xsl", "testGetTag", "testPushTag", exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("testPushTag");
		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be XML  ::", "XML", type);

		Object data = contextElement.getData().getItems().getData();
		logger.debug("datadatadata " + data.getClass());
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", String.class, data.getClass());
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * pushing back to LDC with JSON
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataWithLDCWithJSON()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {

		leapDataCtx.addContextElement(new JSONObject(jsonString), "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.transformDataWithLDC("test-XSLT.xsl", "testGetTag", "testPushTag", exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("testPushTag");
		Assert.assertNotNull("Context Element data should not be null ::", contextElement);

		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be XML  ::", "XML", type);

		Object data = contextElement.getData().getItems().getData();
		logger.debug("datadatadata " + data.getClass());
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", String.class, data.getClass());
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * pushing back to LDC with Wrong xslt file Name.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test(expected = LeapDataServiceConfigurationException.class)
	public void testTransformDataWithLDCWithWrongFileName()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {

		leapDataCtx.addContextElement(new JSONObject(jsonString), "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		configurationUtil.transformDataWithLDC("WrongFileName", "testGetTag", "testPushTag", exchange);
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * return it as String with JSON Data
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataFromLdcGetTagWithJson()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		leapDataCtx.addContextElement(new JSONObject(jsonString), "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String transformData = configurationUtil.transformDataFromLdcGetTag("test-XSLT.xsl", "testGetTag", exchange);
		Assert.assertNotNull("Transform Data should not be null ::", transformData);
		Assert.assertTrue("Transform Data Should not be empty ::", !transformData.isEmpty());
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * return it as String with XML data.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataFromLdcGetTagWithXML()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		leapDataCtx.addContextElement(xmlString, "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String transformData = configurationUtil.transformDataFromLdcGetTag("test-XSLT.xsl", "testGetTag", exchange);
		Assert.assertNotNull("Transform Data should not be null ::", transformData);
		Assert.assertTrue("Transform Data Should not be empty ::", !transformData.isEmpty());
	}

	/**
	 * This method is used to transform the body from LDC ,applying the XSLT and
	 * return it as String with wrong xslt File NAme.
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test(expected = LeapDataServiceConfigurationException.class)
	public void testTransformDataFromLdcGetTagWithWrongFileName()
			throws LeapDataContextInitialzerException, JSONException, LeapDataServiceConfigurationException {
		leapDataCtx.addContextElement(xmlString, "testGetTag", "testGetTag", null);
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		configurationUtil.transformDataFromLdcGetTag("WrongFileName", "testGetTag", exchange);
	}

	/**
	 * This method is used to transform the body from Exchange ,applying the XSLT
	 * and store it in exchange with json data
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataWithExchangeWithJSON() throws LeapDataServiceConfigurationException {
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setBody(jsonString);
		configurationUtil.transformDataWithExchange("test-XSLT.xsl", exchange);

		Object transformData = message.getBody();
		Assert.assertNotNull("Transform Data should not be null ::", transformData);
		Assert.assertTrue("Transform Data Should not be empty ::", !transformData.toString().isEmpty());
	}

	/**
	 * This method is used to transform the body from Exchange ,applying the XSLT
	 * and store it in exchange with xml Data.
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test
	public void testTransformDataWithExchangeWithXML() throws LeapDataServiceConfigurationException {
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setBody(xmlString);
		configurationUtil.transformDataWithExchange("test-XSLT.xsl", exchange);

		Object transformData = message.getBody();
		Assert.assertNotNull("Transform Data should not be null ::", transformData);
		Assert.assertTrue("Transform Data Should not be empty ::", !transformData.toString().isEmpty());
	}

	/**
	 * This method is used to transform the body from Exchange ,applying the XSLT
	 * and store it in exchange with wrong xslt file name.
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	@Test(expected = LeapDataServiceConfigurationException.class)
	public void testTransformDataWithExchangeWithWrongFileName() throws LeapDataServiceConfigurationException {
		leapDataCtx.getServiceDataContext().setRequestContext(LeapCoreTestUtils.getRequestContext());
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setBody(xmlString);
		configurationUtil.transformDataWithExchange("WrongFileName", exchange);
	}

	/**
	 * This method is used to store request body from exchange to LDC using pushTag
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testPushBodyToLDCFromExchange() throws LeapDataContextInitialzerException {
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setBody(DEMO_XML_DATA);

		configurationUtil.pushBodyToLDCFromExchange("testPushTag", exchange);

		LeapDataElement contextElement = leapDataCtx.getContextElement("testPushTag");

		Assert.assertNotNull("Context Element data should not be null ::", contextElement);
		String type = contextElement.getData().getItems().getType();
		Assert.assertNotNull("Context elemnt item of data type should not be null  ::", type);
		Assert.assertEquals("Context elemnt item of data type should be XML  ::", "XML", type);

		Object data = contextElement.getData().getItems().getData();
		Assert.assertNotNull("data should not be null :", data);
		Assert.assertEquals("data Should be jsonObject Instance ::", String.class, data.getClass());
		Assert.assertEquals(DEMO_XML_DATA, data.toString());
	}

	private Document getDocFile() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(DEMO_XML_DATA)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

}
