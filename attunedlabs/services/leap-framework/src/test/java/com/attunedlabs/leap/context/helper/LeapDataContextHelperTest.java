package com.attunedlabs.leap.context.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapDataContextConfigException;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.MetaData;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;

public class LeapDataContextHelperTest {

	final Logger log = LoggerFactory.getLogger(LeapDataContextHelperTest.class);

	private LeapDataContext leapDataCtx;
	private final String TEST_TAXONOMY = "testTaxonomy";
	private final String TEST_TAG_NAME = "#leap_initial";
	private final String TEST_KIND = "TestKind";
	private final String xml = "<test><data>demo</data></test>";
	private final String jsonAsString = "{\"test\":{\"data\":\"demo\"}}";
	private final String jsonArrayAsString = "[{\"test\":{\"data\":\"demo\"}}]";
	private final String leapResponseForTest = "{\"apiVersion\":\"apiVersion\",\"data\":{\"totalItems\":1,\"metadata\":[{\"actualColumnName\":\"test\",\"byteLenth\":4,\"type\":\"Object\",\"effectiveColumnName\":\"test\"},{\"actualColumnName\":\"data\",\"byteLenth\":4,\"type\":\"String\",\"effectiveColumnName\":\"data\"}],\"kind\":\"TestKind\",\"items\":[{\"test\":{\"data\":\"demo\"}}]},\"context\":\"context\",\"lang\":\"lang\"}";
	private final String leapResponseForErrorTest = "{\"data\":{\"items\":[]},\"error\":{\"ErrorMassage\":\"DataNotFoundException\"}}";
	private final String jsonNewData = "{\"new\":\"test\"}";

	@Before
	public void setUp() throws LeapDataContextInitialzerException {
		if (leapDataCtx == null) {
			leapDataCtx = new LeapDataContext();
			leapDataCtx.getServiceDataContext().setRequestContext(GenericTestConstant.TEST_TENANT,
					GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP,
					GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR,
					GenericTestConstant.TEST_VERSION);
			JSONObject jsonObject = new JSONObject(jsonAsString);
			leapDataCtx.addContextElement(jsonObject, TEST_KIND, TEST_TAG_NAME, null);
			leapDataCtx.getServiceDataContext().initializeLeapRuntimeServiceContext("TestService");
			leapDataCtx.getServiceDataContext().SetRunningContextServiceName("TestService");
		}
	}

	/**
	 * Method to convert LeapDataElement Object as a {@link JSONObject}
	 */
	@Test
	public void testGetContextElementAsJson() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement data should be same as expected Data ::", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		JSONObject contextElementAsJson = LeapDataContextHelper.getContextElementAsJson(leapDataElement, TEST_TAG_NAME);
		Assert.assertNotNull("json Obejct data should not be null ::", contextElementAsJson);
		Assert.assertTrue("jsonAsString data should be contain inside jsonObject::",
				contextElementAsJson.toString().contains(jsonAsString));
	}

	@Test
	public void testGetContextElementAsJsonFail() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(null);
		Assert.assertNull("Leap Data Context Should not be null :: ", leapDataElement);
	}

	/**
	 * Method to convert the 'element' data of LeapDataElement Object as a
	 * {@link JSONObject}
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testGetContextElementAsJsonForKind() throws LeapDataContextConfigException {

		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement item data Should be same as Expected Data ::", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		Object contextElementAsJsonForKind = LeapDataContextHelper.getContextElementAsJsonForKind(leapDataElement,
				TEST_KIND);
		Assert.assertNotNull("Context Element As json for kind data Should not be null ::",
				contextElementAsJsonForKind);

		JSONArray jsonArray = (JSONArray) contextElementAsJsonForKind;
		Assert.assertNotNull("jsonArray Should not be null ::", jsonArray);
		Assert.assertEquals("jsonArray Data Should be same as expected ::",
				new JSONObject(jsonAsString).getJSONObject("test").toString(), jsonArray.get(0).toString());
	}

	@Test
	public void testGetContextElementAsJsonForKindFail() throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(null);
		Assert.assertNull("Leap Data Context Should not be null :: ", leapDataElement);
	}

	/**
	 * Method used to get either JSONObject or JSONArray depending on LeapResultSet
	 * instance coming inside LeapData
	 */
	@Test
	public void testGetJsonDataWithLDE() {

		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement item data should be same as Expected data :", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		Object jsonData = LeapDataContextHelper.getJsonData(leapDataElement);
		Assert.assertNotNull("json Data Should not be null ::", jsonData);
		Assert.assertEquals("json Data Should be same as expected data ::", jsonAsString, jsonData.toString());
	}

	/**
	 * Method used to get the Leap Response Items array for a given leap Response.
	 */
	@Test
	public void testGetJsonDataWithString() {

		JSONArray jsonData = LeapDataContextHelper.getJsonData(leapResponseForTest);
		Assert.assertNotNull("json Data Should not be null ::", jsonData);
		Assert.assertEquals("json Data Should be same as expected data ::", jsonAsString, jsonData.get(0).toString());
	}

	/**
	 * Method used to know if the Leap response has Error type.
	 */
	@Test
	public void testIsLeapErrorResponse() {
		boolean leapErrorResponseFalse = LeapDataContextHelper.isLeapErrorResponse(leapResponseForTest);
		Assert.assertFalse("leap response should be false becuase leap Response have a no error ::",
				leapErrorResponseFalse);

		boolean leapErrorResponseTrue = LeapDataContextHelper.isLeapErrorResponse(leapResponseForErrorTest);
		Assert.assertTrue("leap response should be false becuase leap Response have a error ::", leapErrorResponseTrue);
	}

	/**
	 * Method used to get Leap response Error Object for the given Leap Response.
	 */
	@Test
	public void testGetLeapErrorData() {
		JSONObject leapErrorData = LeapDataContextHelper.getLeapErrorData(leapResponseForErrorTest);
		log.debug("leapErrorDataleapErrorData " + leapErrorData);
		Assert.assertNotNull("leap Error Data Should not be null ::", leapErrorData);
		Assert.assertEquals("leapErrorData should be same as expected ::",
				"{\"ErrorMassage\":\"DataNotFoundException\"}", leapErrorData.toString());
	}

	/**
	 * Method used to convert a LeapResponse(leapDataElement) instance to JSON
	 * Object.
	 */
	@Test
	public void testToJson() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement item data should be same as Expected data :", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		JSONObject json = LeapDataContextHelper.toJson(leapDataElement);
		Assert.assertNotNull("json Data Should not be null ::", json);
		Assert.assertTrue("json should be contain jsonAsString Data::", json.toString().contains(jsonAsString));
	}

	@Test(expected = NullPointerException.class)
	public void testToJsonFail() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(null);
		Assert.assertNull("Leap Data Context Should not be null :: ", leapDataElement);

		LeapDataContextHelper.toJson(leapDataElement);
	}

	/**
	 * Method used to add new JSON item into items array of Leap Response where Leap
	 * Response as LeapDataElement.
	 */
	@Test
	public void testAddItemsInLDE() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement item data should be same as Expected data :", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		JSONObject addItems = LeapDataContextHelper.addItems(leapDataElement, new JSONObject(jsonNewData));
		Assert.assertNotNull("add item json object should not be null ::", addItems);
		Assert.assertTrue("AddItems should be contains JsonNew Data ::", addItems.toString().contains(jsonNewData));
		Assert.assertTrue("AddItems Should be contains JsonAsString Data ::",
				addItems.toString().contains(jsonAsString));
	}

	@Test(expected = NullPointerException.class)
	public void testAddItemsInLDEFail() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(null);
		Assert.assertNull("Leap Data Context Should not be null :: ", leapDataElement);

		LeapDataContextHelper.addItems(leapDataElement, new JSONObject(jsonNewData));

	}

	/**
	 * Method used to add new JSON item into items array of Leap Response where Leap
	 * Response as String.
	 */
	@Test
	public void testAddItemsInLeapResponse() {
		JSONObject addItems = LeapDataContextHelper.addItems(leapResponseForTest, new JSONObject(jsonNewData));
		Assert.assertNotNull("add item json object should not be null ::", addItems);
		Assert.assertTrue("AddItems should be contains JsonNew Data ::", addItems.toString().contains(jsonNewData));
		Assert.assertTrue("AddItems Should be contains JsonAsString Data ::",
				addItems.toString().contains(jsonAsString));
	}

	/**
	 * Method used to remove all the Items in Leap Response.
	 */
	@Test
	public void testRemoveItemsFromLDE() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("leapDataElement item data should be same as Expected data :", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		JSONObject removeItems = LeapDataContextHelper.removeItems(leapDataElement);
		Assert.assertNotNull("add item json object should not be null ::", removeItems);
		Assert.assertFalse("", removeItems.toString().contains(jsonAsString));
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveItemsFromLDEFail() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(null);
		Assert.assertNull("Leap Data Context Should not be null :: ", leapDataElement);

		LeapDataContextHelper.removeItems(leapDataElement);

	}

	/**
	 * Method used to remove all the Items in Leap Response.
	 */
	@Test
	public void testRemoveItemsFromLeapResponse() {
		Assert.assertTrue(leapResponseForTest.toString().contains(jsonAsString));

		JSONObject removeItems = LeapDataContextHelper.removeItems(leapResponseForTest);
		Assert.assertNotNull("add item json object should not be null ::", removeItems);
		Assert.assertFalse(removeItems.toString().contains(jsonAsString));
	}

	/**
	 * Method to construct LeapData structure form the raw data
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapDataElement() throws LeapDataContextConfigException {
		LeapDataElement constructLeapDataElement = LeapDataContextHelper
				.constructLeapDataElement(new JSONObject(jsonAsString), TEST_KIND, TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("LeapDataElement should not be null ::", constructLeapDataElement);
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", TEST_TAXONOMY,
				constructLeapDataElement.getData().getTaxonomyId());
		Assert.assertTrue("leapDataElement data should be contains jsonAsString data ::",
				constructLeapDataElement.toString().contains(jsonAsString));
	}

	/**
	 * Method to construct LeapData structure form the raw data in
	 * {@link JSONObject} format without taxonomyId
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapDataElementFromJSONObjectWithoutTaxonomy() throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = LeapDataContextHelper
				.constructLeapDataElementFromJSONObject(new JSONObject(jsonAsString), TEST_KIND, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", leapDataElement);
		Assert.assertTrue("leapDataContext should be contain jsonAsString Data ::",
				leapDataElement.toString().contains(jsonAsString));
		Assert.assertNull("taxonomy id should be null :", leapDataElement.getData().getTaxonomyId());
	}

	/**
	 * Method to construct LeapData structure form the raw data in
	 * {@link JSONObject} format
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapDataElementFromJSONObjectWithTaxonomy() throws LeapDataContextConfigException {
		LeapDataElement leapDataElement = LeapDataContextHelper.constructLeapDataElementFromJSONObject(
				new JSONObject(jsonAsString), TEST_KIND, TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", leapDataElement);
		Assert.assertTrue("leapDataContext should be contain jsonAsString Data ::",
				leapDataElement.toString().contains(jsonAsString));
		Assert.assertNotNull("taxonomy id should be null :", leapDataElement.getData().getTaxonomyId());
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", TEST_TAXONOMY,
				leapDataElement.getData().getTaxonomyId());
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * {@link JSONObject} format
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructResponseLeapDataElementFromJSONObject()
			throws LeapDataContextConfigException, JSONException {
		Map<String, String> metaDataMap = new HashMap<>();
		LeapDataElement leapDataElement = LeapDataContextHelper.constructResponseLeapDataElementFromJSONObject(
				new JSONObject(jsonAsString), TEST_KIND, metaDataMap, leapDataCtx);
		Assert.assertNotNull("Leap Data Element should not be null :", leapDataElement);
		Assert.assertTrue(leapDataElement.toString().contains(jsonAsString));
	}

	/**
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructImmutableLeapDataElementFromJSONObject()
			throws LeapDataContextConfigException, JSONException {
		LeapDataElement immutableLeapDataElement = LeapDataContextHelper
				.constructImmutableLeapDataElementFromJSONObject(new JSONObject(jsonAsString), TEST_KIND, TEST_TAXONOMY,
						leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", immutableLeapDataElement);
		Assert.assertTrue("leapDataContext should be contain jsonAsString Data ::",
				immutableLeapDataElement.toString().contains(jsonAsString));
		Assert.assertNotNull("taxonomy id should be null :", immutableLeapDataElement.getData().getTaxonomyId());
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", TEST_TAXONOMY,
				immutableLeapDataElement.getData().getTaxonomyId());
	}

	/**
	 * Method get data on Taxonomy
	 */
	@Test
	public void testGetDataBasedOnTaxonomy() {
		Object dataBasedOnTaxonomy = LeapDataContextHelper.getDataBasedOnTaxonomy(TEST_TAXONOMY,
				new JSONObject(jsonAsString), leapDataCtx);
		Assert.assertNotNull("Data based on taxonomy should not be null ::", dataBasedOnTaxonomy);
		Assert.assertEquals("jsonObject class should be same as get object data class ::", JSONObject.class,
				dataBasedOnTaxonomy.getClass());
		Assert.assertTrue("TaxonomyTest must be contain in object data because taxonomy applied",
				dataBasedOnTaxonomy.toString().contains("TaxonomyTest"));
		Assert.assertTrue("TaxonomyData must be contain in object data because taxonomy applied",
				dataBasedOnTaxonomy.toString().contains("TaxonomyData"));
	}

	/**
	 * Method to construct response LeapData structure form the raw data in
	 * {@link JSONArray} format without taxonomyId
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructLeapDataElementFromJSONArrayWithoutTaxonomy()
			throws LeapDataContextConfigException, JSONException {
		LeapDataElement leapDataElementFromJSONArray = LeapDataContextHelper
				.constructLeapDataElementFromJSONArray(new JSONArray(jsonArrayAsString), TEST_KIND, leapDataCtx);
		Assert.assertNotNull("LeapDataElement data Should not be null ::", leapDataElementFromJSONArray);
		Assert.assertTrue("leapDataContext should be contain jsonArrayAsString Data ::",
				leapDataElementFromJSONArray.toString().contains(jsonArrayAsString));
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", "LeapDefault",
				leapDataElementFromJSONArray.getData().getTaxonomyId());
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * {@link JSONArray} format
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructResponseLeapDataElementFromJSONArray()
			throws LeapDataContextConfigException, JSONException {
		Map<String, String> metaDataMap = new HashMap<>();
		LeapDataElement responseLeapDataElement = LeapDataContextHelper.constructResponseLeapDataElementFromJSONArray(
				new JSONArray(jsonArrayAsString), TEST_KIND, metaDataMap, leapDataCtx);
		Assert.assertNotNull("LeapDataContext Should not be null ::", responseLeapDataElement);
		Assert.assertTrue("leapDataContext should be contain jsonArrayAsString Data ::",
				responseLeapDataElement.toString().contains(jsonArrayAsString));
		Assert.assertEquals("jsonArrayAsString should be same as leapDataElement item data ::", jsonArrayAsString,
				responseLeapDataElement.getData().getItems().getData().toString());
	}

	/**
	 * Method to construct LeapData structure form the raw data in {@link JSONArray}
	 * format
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructLeapDataElementFromJSONArrayWithTaxonomy()
			throws LeapDataContextConfigException, JSONException {
		LeapDataElement leapDataElementFromJSONArray = LeapDataContextHelper.constructLeapDataElementFromJSONArray(
				new JSONArray(jsonArrayAsString), TEST_KIND, TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("LeapDataElement data Should not be null ::", leapDataElementFromJSONArray);
		Assert.assertTrue("leapDataContext should be contain jsonArrayAsString Data ::",
				leapDataElementFromJSONArray.toString().contains(jsonArrayAsString));
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", TEST_TAXONOMY,
				leapDataElementFromJSONArray.getData().getTaxonomyId());
	}

	/**
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testConstructImmutableLeapDataElementFromJSONArray()
			throws LeapDataContextConfigException, JSONException {
		LeapDataElement immutableLeapDataElement = LeapDataContextHelper.constructImmutableLeapDataElementFromJSONArray(
				new JSONArray(jsonArrayAsString), TEST_KIND, TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", immutableLeapDataElement);
		Assert.assertTrue("leapDataContext should be contain jsonAsString Data ::",
				immutableLeapDataElement.toString().contains(jsonAsString));
		Assert.assertNotNull("taxonomy id should be null :", immutableLeapDataElement.getData().getTaxonomyId());
		Assert.assertEquals("leapDataElement texonomyid should be same as expected data ::", TEST_TAXONOMY,
				immutableLeapDataElement.getData().getTaxonomyId());
	}

	/**
	 * Method to construct LeapData structure form the raw data in XML(string)
	 * format without taxonomyId
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapDataElementFromXMLWithoutTaxonomy() throws LeapDataContextConfigException {
		LeapDataElement leapDataElementFromXML = LeapDataContextHelper.constructLeapDataElementFromXML(xml, TEST_KIND,
				leapDataCtx);
		Assert.assertNotNull("leapDataElement should not be null ::", leapDataElementFromXML);
		Assert.assertEquals("LDE item type should be XML ::", "XML",
				leapDataElementFromXML.getData().getItems().getType());
		Assert.assertEquals("LDE item data Should be same as xml Data :", xml,
				leapDataElementFromXML.getData().getItems().getData());
	}

	/**
	 * Method to construct LeapData structure form the raw data in XML(string)
	 * format
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapDataElementFromXMLWithTaxonomy() throws LeapDataContextConfigException {
		LeapDataElement leapDataElementFromXML = LeapDataContextHelper.constructLeapDataElementFromXML(xml, TEST_KIND,
				TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", leapDataElementFromXML);
		Assert.assertTrue("leapDataContext should be contain xml Data ::",
				leapDataElementFromXML.toString().contains(xml));
		Assert.assertNotNull("taxonomy id should be null :", leapDataElementFromXML.getData().getTaxonomyId());
		Assert.assertEquals("texonomyid should be same as leapDataElement texonomyId ::", TEST_TAXONOMY,
				leapDataElementFromXML.getData().getTaxonomyId());
	}

	/**
	 * Method to construct Response LeapData structure form the raw data in
	 * XML(string) format
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructResponseLeapDataElementFromXML() throws LeapDataContextConfigException {
		Map<String, String> metaData = new HashMap<>();
		LeapDataElement responseLDEFromXML = LeapDataContextHelper.constructResponseLeapDataElementFromXML(xml,
				TEST_KIND, metaData, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", responseLDEFromXML);
		Assert.assertTrue("leapDataContext should be contain xml Data ::", responseLDEFromXML.toString().contains(xml));
		Assert.assertEquals("xml data should be same as leapDataElement item data ::", xml,
				responseLDEFromXML.getData().getItems().getData().toString());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructResponseLeapDataElementFromXMLFail() throws LeapDataContextConfigException {
		Map<String, String> metaData = new HashMap<>();
		LeapDataContextHelper.constructResponseLeapDataElementFromXML(null, TEST_KIND, metaData, leapDataCtx);

	}

	/**
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructImmutableLeapDataElementFromXML() throws LeapDataContextConfigException {
		LeapDataElement immutableLDE = LeapDataContextHelper.constructImmutableLeapDataElementFromXML(xml, TEST_KIND,
				TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("leap data context should not be null ::", immutableLDE);
		Assert.assertTrue("leapDataContext should be contain xml Data ::", immutableLDE.toString().contains(xml));
		Assert.assertEquals("xml data should be same as leapDataElement item data ::", xml,
				immutableLDE.getData().getItems().getData().toString());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructImmutableLeapDataElementFromXMLFail() throws LeapDataContextConfigException {
		LeapDataContextHelper.constructImmutableLeapDataElementFromXML(null, TEST_KIND, TEST_TAXONOMY, leapDataCtx);
	}

	/**
	 * Method to get values based on JSONPath
	 */
	@Test
	public void testGetValuesFromJson() {
		List<String> valuesFromJson = LeapDataContextHelper.getValuesFromJson(new JSONObject(jsonAsString), "test");
		Assert.assertTrue("value should be greater then 0 ::", valuesFromJson.size() > 0);
		Assert.assertEquals("value of test should be same as expected data ::", "[{\"data\":\"demo\"}]",
				valuesFromJson.toString());

		List<String> valuesFromJson2 = LeapDataContextHelper.getValuesFromJson(new JSONObject(jsonAsString),
				"test.data");
		Assert.assertTrue("value should be greater then 0 ::", valuesFromJson2.size() > 0);
		Assert.assertEquals("value of data should be same as expected data ::", "demo", valuesFromJson2.get(0));
	}

	/**
	 * Method to get values based on XPath
	 * 
	 * @throws LeapDataContextConfigException
	 */

	@Test
	public void testGetValuesFromXml() throws LeapDataContextConfigException {
		List<String> valuesFromXml = LeapDataContextHelper.getValuesFromXml(xml, "test");
		Assert.assertTrue("value should be greater then 0 ::", valuesFromXml.size() > 0);
		Assert.assertEquals("value of test should be same as expected data ::", "[<data>demo</data>]",
				valuesFromXml.toString());

		List<String> valuesFromXml2 = LeapDataContextHelper.getValuesFromXml(xml, "data");
		Assert.assertTrue("value should be greater then 0 ::", valuesFromXml2.size() > 0);
		Assert.assertEquals("value of data should be same as expected data ::", "[<data>demo</data>]",
				valuesFromXml.toString());
	}

	/**
	 * Method to get value based on JSONPath
	 */

	@Test
	public void testGetValueFromJson() {
		String valueFromJson = LeapDataContextHelper.getValueFromJson(new JSONObject(jsonAsString), "test");
		Assert.assertNotNull("value from json should not be null ::", valueFromJson);
		Assert.assertEquals("value of test should be same as expected data ::", "{\"data\":\"demo\"}", valueFromJson);

		String valueFromJson2 = LeapDataContextHelper.getValueFromJson(new JSONObject(jsonAsString), "test.data");
		Assert.assertNotNull("value from json should not be null ::", valueFromJson2);
		Assert.assertEquals("value of data should be same as expected data ::", "demo", valueFromJson2);
	}

	/**
	 * Method to get value based on XPath
	 */
	@Test
	public void testGetValueFromXml() {
		String valueFromXml = LeapDataContextHelper.getValueFromXml(xml, "test");
		Assert.assertNotNull("value from xml should not be null ::", valueFromXml);
		Assert.assertEquals("value of test should be same as expected data ::", "<data>demo</data>", valueFromXml);

		String valueFromXml2 = LeapDataContextHelper.getValueFromXml(xml, "data");
		Assert.assertNotNull("value from xml should not be null ::", valueFromXml2);
		Assert.assertEquals("value of data should be same as expected data ::", "demo", valueFromXml2);
	}

	/**
	 * 
	 * Method to get the exact Node Value
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testGetNodeValue()
			throws ParserConfigurationException, SAXException, IOException, LeapDataContextConfigException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		Document doc = builder.parse(stream);

		List<String> nodeValue = LeapDataContextHelper.getNodeValue(doc, "test");
		Assert.assertTrue("node value should be greater then 0 ::", nodeValue.size() > 0);
		Assert.assertEquals("node value of test should be same as expected data ::", "<data>demo</data>",
				nodeValue.get(0));

		List<String> nodeValue2 = LeapDataContextHelper.getNodeValue(doc, "data");
		Assert.assertTrue("node value should be greater then 0 ::", nodeValue2.size() > 0);
		Assert.assertEquals("node value of  data should be same as expected data ::", "demo", nodeValue2.get(0));

	}

	/**
	 * Method to get the Root Tag for a {@link JSONObject}
	 */
	@Test
	public void testGetRootTag() {
		String rootTag = LeapDataContextHelper.getRootTag(new JSONObject(jsonAsString));
		Assert.assertNotNull("root tag should not be null ::", rootTag);
		Assert.assertEquals("root element should be test::", "test", rootTag);
	}

	/**
	 * Method to convert the complete LeapDataElement Object as a {@link JSONObject}
	 */
	@Test
	public void testGetListOfContextElementAsJson() {
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals("jsonAsString data must be same as leapDataelement item data::", jsonAsString,
				leapDataElement.getData().getItems().getData().toString());

		JSONObject listOfContextElementAsJson = LeapDataContextHelper.getListOfContextElementAsJson(leapDataElement,
				TEST_TAG_NAME);
		Assert.assertNotNull("listOfContextElement should not be null ::", listOfContextElementAsJson);
		Assert.assertTrue("jsonObject data should be contain as jsonAsString value :",
				listOfContextElementAsJson.toString().contains(jsonAsString));
	}

	/**
	 * Method to convert the MetaData into {@link JSONObject}
	 */
	@Test
	public void testConvertToJson() {
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());

		JSONArray convertToJson = LeapDataContextHelper.convertToJson(listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				convertToJson.getJSONObject(0).get("effectiveColumnName"));
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4,
				convertToJson.getJSONObject(0).get("byteLenth"));

	}

	/**
	 * Custom Method to get the specific value for the specified key under the
	 * JSONObject
	 */
	@Test
	public void testGetValueFromJsonCustom() {
		List<String> valueFromJsonCustom = LeapDataContextHelper.getValueFromJsonCustom(new JSONObject(jsonAsString),
				"test");
		Assert.assertTrue("value of json custom list should be more then 0 ::", valueFromJsonCustom.size() > 0);
		Assert.assertEquals("list of index 0 must be data same as expected ::", "{\"data\":\"demo\"}",
				valueFromJsonCustom.get(0));
	}

	/**
	 * Method to construct MetaData
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testCreateMetaDataWithTaxonomy() throws LeapDataContextConfigException, JSONException {
		List<MetaData> createMetaData = LeapDataContextHelper.createMetaData(new JSONObject(jsonAsString),
				TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "TaxonomyTest",
				createMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, createMetaData.get(0).getByteLenth());
	}

	/**
	 * Method to construct MetaData without taxonomyId
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testCreateMetaDataWithoutTaxonomy() throws LeapDataContextConfigException, JSONException {
		List<MetaData> createMetaData = LeapDataContextHelper.createMetaData(new JSONObject(jsonAsString), leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				createMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, createMetaData.get(0).getByteLenth());
	}

	/**
	 * Method to construct MetaData
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws JSONException
	 */
	@Test
	public void testCreateMetaDataForResponse() throws LeapDataContextConfigException, JSONException {
		Map<String, String> metaData = new HashMap<String, String>();
		List<MetaData> createMetaDataForResponse = LeapDataContextHelper
				.createMetaDataForResponse(new JSONObject(jsonAsString), metaData, leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaDataForResponse);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				createMetaDataForResponse.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4,
				createMetaDataForResponse.get(0).getByteLenth());
	}

	@Test(expected = NullPointerException.class)
	public void testCreateMetaDataForResponseFail() throws LeapDataContextConfigException, JSONException {
		Map<String, String> metaData = new HashMap<String, String>();
		LeapDataContextHelper.createMetaDataForResponse(null, metaData, leapDataCtx);

	}

	/**
	 * Method to construct MetaData from XML input data without taxonomyId
	 * 
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testCreateMetaDataForXMLDataWithoutTaxonomy() throws JSONException, LeapDataContextConfigException {
		List<MetaData> createMetaDataForXMLData = LeapDataContextHelper.createMetaDataForXMLData(xml, leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaDataForXMLData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				createMetaDataForXMLData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4,
				createMetaDataForXMLData.get(0).getByteLenth());
	}

	@Test(expected = NullPointerException.class)
	public void testCreateMetaDataForXMLDataWithoutTaxonomyFail() throws JSONException, LeapDataContextConfigException {
		LeapDataContextHelper.createMetaDataForXMLData(null, leapDataCtx);
	}

	/**
	 * Method to construct MetaData from XML input data
	 * 
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testCreateMetaDataFormXMLDataForResponse() throws JSONException, LeapDataContextConfigException {
		Map<String, String> metaData = new HashMap<String, String>();
		List<MetaData> createMetaData = LeapDataContextHelper.createMetaDataFormXMLDataForResponse(xml, metaData,
				leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				createMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, createMetaData.get(0).getByteLenth());
	}

	@Test(expected = NullPointerException.class)
	public void testCreateMetaDataFormXMLDataForResponseFail() throws JSONException, LeapDataContextConfigException {
		Map<String, String> metaData = new HashMap<String, String>();
		LeapDataContextHelper.createMetaDataFormXMLDataForResponse(null, metaData, leapDataCtx);
	}

	/**
	 * Method to construct MetaData from XML input data without taxonomyId
	 * 
	 * @throws JSONException
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testCreateMetaDataForXMLData() throws JSONException, LeapDataContextConfigException {
		List<MetaData> createMetaDataForXMLData = LeapDataContextHelper.createMetaDataForXMLData(xml, leapDataCtx);
		Assert.assertNotNull("list of metadata  Should not be null ::", createMetaDataForXMLData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				createMetaDataForXMLData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4,
				createMetaDataForXMLData.get(0).getByteLenth());
	}

	@Test(expected = NullPointerException.class)
	public void testCreateMetaDataForXMLDataFail() throws JSONException, LeapDataContextConfigException {
		LeapDataContextHelper.createMetaDataForXMLData(null, leapDataCtx);

	}

	/**
	 * For Transforming the intermediate data
	 */
	@Test
	public void testTransform() {
		List<JSONObject> transform = LeapDataContextHelper.transform(new JSONArray(jsonArrayAsString));
		Assert.assertTrue("transform list must be greater then 0:: ", transform.size() > 0);
		Assert.assertEquals("jsonArray Data Should be same as transform data:: ", jsonAsString,
				transform.get(0).toString());
	}

	/**
	 * Check For Data Compression
	 */
	@Test
	public void testCheckForDataCompression() {
		boolean checkForDataCompression = LeapDataContextHelper
				.checkForDataCompression(new JSONArray(jsonArrayAsString));
		Assert.assertFalse("boolean value should be false because in jsonArray have single jsonObject Data ::",
				checkForDataCompression);

		String jsonArrayAsString = "[{\"test\":{\"data\":{\"demo\":\"test\"}}},{\"test\":{\"data\":{\"demo\":\"test\"}}}]";
		boolean checkForDataCompression1 = LeapDataContextHelper
				.checkForDataCompression(new JSONArray(jsonArrayAsString));
		Assert.assertTrue("boolean value should be true because in jsonArray more then 2 jsonObject Data ::",
				checkForDataCompression1);
	}

	/**
	 * Method to fetch file as resource using ResourceManagemant
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 */
	@Test
	public void testGetFile() throws LeapDataContextInitialzerException, IOException {

		String taxonomy = TEST_TAXONOMY + "_Taxonomy.json";
		String file = LeapDataContextHelper.getFile(taxonomy, "taxonomy", TEST_TAXONOMY, leapDataCtx);
		Assert.assertNotNull("file should not be null ::", file);
		Assert.assertTrue("taxonomy file should be contains 'TaxonomyTest' :: ", file.contains("TaxonomyTest"));
		Assert.assertTrue("taxonomy file should be contains 'TaxonomyData' :: ", file.contains("TaxonomyData"));
	}
}
