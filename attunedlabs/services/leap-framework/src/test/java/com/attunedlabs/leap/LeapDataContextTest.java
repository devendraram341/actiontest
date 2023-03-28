package com.attunedlabs.leap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.bean.MetaData;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationUnit;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;

public class LeapDataContextTest {

	private LeapDataContext leapDataCtx;
	private final String jsonAsString = "{\"test\":{\"data\":\"demo\"}}";
	private final String jsonArrayAsString = "[{\"test\":{\"data\":\"demo\"}}]";
	final Logger log = LoggerFactory.getLogger(LeapDataContextTest.class);
	private final String TEST_TAG_NAME = "#leap_initial";
	private final String TEST_HEADER_TAG = "#header";
	private final String TEST_KIND = "TestKind";
	private final String CONFIG_NAME = "TestNameForLDC";
	private final String PERMASTORE_FILE = "LDC/PermaStoreConfig.xml";
	private FeatureDeploymentService featureDeploymentService;

	/**
	 * used for initialization
	 */
	@Before
	public void setUp() {
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		if (featureDeploymentService == null)
			featureDeploymentService = new FeatureDeploymentService();

	}

	/**
	 * Method to add LeapDataContextElement inside the Deque against a 'kind' &
	 * 'tagName'
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testAddContextElement() throws LeapDataContextInitialzerException {

		JSONObject jsonObject = new JSONObject(jsonAsString);
		leapDataCtx.addContextElement(jsonObject, TEST_KIND, TEST_TAG_NAME, null);

		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals(jsonAsString, leapDataElement.getData().getItems().getData().toString());

	}

	/**
	 * Method to add get LDC response format without taxonomyId
	 * 
	 * @throws LeapDataContextInitialzerException
	 */

	@Test
	public void testGetLDCResponseWithoutTaxo() throws LeapDataContextInitialzerException {

		testAddContextElement();
		JSONObject ldcResponse1 = leapDataCtx.getLDCResponse(new JSONObject(jsonAsString), TEST_KIND);
		Assert.assertNotNull("ldc response should not be null ::", ldcResponse1);
		Assert.assertTrue("ldc response should be contains as jsonAsString data ::",
				ldcResponse1.toString().contains(jsonAsString));
	}

	/**
	 * Method to add get LDC response format, the taxonomyId should be same as
	 * taxonomyId used to apply taxonomy. If taxonomyId is null, then vendor
	 * taxonomy is used.
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testGetLDCResposeWithTaxo() throws LeapDataContextInitialzerException {
		testGetNormalizedContextWithTaxo();
		JSONObject ldcResponse = leapDataCtx.getLDCResponse(new JSONObject(jsonAsString), TEST_KIND, "testTaxonomy");
		Assert.assertNotNull("ldc response should not be null ::", ldcResponse);
		Assert.assertTrue("ldc response should be contains as jsonAsString data ::",
				ldcResponse.toString().contains(jsonAsString));
	}

	/**
	 * Method to fetch LeapDataElement Object for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */

	@Test
	public void testGetFormatLeapResponse() throws LeapDataContextInitialzerException {
		LeapDataElement leapDataElementNull = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNull("Leap Data Element is null because data not push into ldc :: ", leapDataElementNull);

		testAddContextElement();
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals(jsonAsString, leapDataElement.getData().getItems().getData().toString());

		JSONObject leapFormatJsonResponse = LeapDataContext.getFormatLeapResponse(leapDataElement);
		Assert.assertNotNull(leapFormatJsonResponse);
		Assert.assertTrue(leapFormatJsonResponse.toString().contains(jsonAsString));
		Assert.assertEquals(TEST_KIND, leapFormatJsonResponse.getJSONObject("data").getString("kind"));
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'kind'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetNewJsonLeapDataWithListMetaData() throws LeapDataContextInitialzerException {
		List<MetaData> nullListOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNull("should be null because not add context element ::", nullListOfMetaData);

		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());

		JSONObject newJsonLeapData = LeapDataContext.getNewJsonLeapData(new JSONArray(jsonArrayAsString), TEST_KIND, 1,
				listOfMetaData);
		Assert.assertNotNull("newJsonLeapData Should not be null ::", newJsonLeapData);
		Assert.assertEquals("in this test case totalItem Should be 1 :: ", 1, newJsonLeapData.get("totalItems"));
	}

	/**
	 * Method used to build the Leap Response data structure.
	 */
	@Test
	public void testGetNewJsonLeapDataWithJsonArray() {
		JSONObject newJsonLeapData = LeapDataContext.getNewJsonLeapData(null, TEST_KIND, 1, new JSONArray());
		Assert.assertNotNull("newJsonLeapData Should not be null ::", newJsonLeapData);
		Assert.assertEquals("in this test case totalItem Should be 1 :: ", 1, newJsonLeapData.get("totalItems"));
	}

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service
	 */
	@Test
	public void testGetServiceDataContextWithTenantAndSite() {
		LeapServiceContext leapServiceContext = leapDataCtx.getServiceDataContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE);
		Assert.assertNotNull("Leap Service Context should not be null ::", leapServiceContext);
		Assert.assertEquals("tenant name should be same as leap service context tenant name ::",
				GenericTestConstant.TEST_TENANT, leapServiceContext.getTenant());
	}

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service.
	 */
	@Test
	public void testGetServiceDataContext() {
		LeapServiceContext leapServiceContext = leapDataCtx.getServiceDataContext();
		Assert.assertNotNull("Leap Service Context should not be null ::", leapServiceContext);
		Assert.assertEquals("tenant name should be same as leap service context tenant name ::", null,
				leapServiceContext.getTenant());
	}

	/**
	 * This method is used to get the leap service context. It is going to be a
	 * single object instance in LDC. This object contain the information related to
	 * executing service.
	 */
	@Test
	public void testGetServiceDataContextWithFeatureGroupAndFeature() {
		LeapServiceContext leapServiceContext = leapDataCtx.getServiceDataContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("Leap Service Context should not be null ::", leapServiceContext);
		Assert.assertEquals("tenant name should be same as leap service context tenant name ::",
				GenericTestConstant.TEST_TENANT, leapServiceContext.getTenant());
		Assert.assertEquals("FeatureGroup name should be same as leap service context featureGroup name ::",
				GenericTestConstant.TEST_FEATUREGROUP, leapServiceContext.getFeatureGroup());
		Assert.assertEquals("featureName name should be same as leap service context Feature name ::",
				GenericTestConstant.TEST_FEATURE, leapServiceContext.getFeatureName());
	}

	/**
	 * Method to get the List of context element on list of 'tagName'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetRawContextElementForTags() throws LeapDataContextInitialzerException {
		List<String> listOftags = new ArrayList<>();
		listOftags.add(TEST_TAG_NAME);
		List<LeapDataElement> emptyLDEList = leapDataCtx.getRawContextElementForTags(listOftags);
		Assert.assertFalse("list of leapDataElement should be zero ::", emptyLDEList.size() > 0);

		testAddContextElement();
		List<LeapDataElement> listOfLDE = leapDataCtx.getRawContextElementForTags(listOftags);
		Assert.assertTrue("list of leapDataElement should be greater then zero ::", listOfLDE.size() > 0);
		Assert.assertTrue("Should be contain json data in list of leapDataElement ::",
				listOfLDE.toString().contains(jsonAsString));
	}

	/**
	 * Method to fetch Context Element for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetContextElementsForTags() throws LeapDataContextInitialzerException {

		JSONObject nullData = leapDataCtx.getContextElementForTag(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullData);

		testAddContextElement();
		JSONObject jsonData = leapDataCtx.getContextElementForTag(TEST_TAG_NAME);
		Assert.assertNotNull("Josn Data Should not be null ::", jsonData);
		Assert.assertTrue(jsonData.toString().contains(jsonAsString));
	}

	/**
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetDataByTags() throws LeapDataContextInitialzerException {
		LeapResultSet nullLeapResult = leapDataCtx.getDataByTag(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullLeapResult);

		testAddContextElement();
		LeapResultSet leapResultSet = leapDataCtx.getDataByTag(TEST_TAG_NAME);
		Assert.assertNotNull("leap result set Should not be null ::", leapResultSet);
		Assert.assertEquals("json Data Should be Same ::", jsonAsString, leapResultSet.getData().toString());

	}

	/**
	 * Method to fetch LeapDataElement Object for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetContextElement() throws LeapDataContextInitialzerException {
		LeapDataElement nullLeapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullLeapDataElement);

		testAddContextElement();
		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals(jsonAsString, leapDataElement.getData().getItems().getData().toString());
	}

	/**
	 * Method to remove LeapDataElement from deque
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testRemoveContextElement() throws LeapDataContextInitialzerException {
		LeapDataElement nullLeapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullLeapDataElement);

		testAddContextElement();
		LeapDataElement beforeLeapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", beforeLeapDataElement);
		Assert.assertEquals(jsonAsString, beforeLeapDataElement.getData().getItems().getData().toString());

		leapDataCtx.removeContextElement(TEST_TAG_NAME);
		LeapDataElement afterRemoveLDE = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNull("Leap Data Context Should be null beacuse already remove:: ", afterRemoveLDE);
	}

	/**
	 * Method to fetch Context Element for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetContextElementForTag() throws LeapDataContextInitialzerException {
		JSONObject nullJsonData = leapDataCtx.getContextElementForTag(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullJsonData);

		testAddContextElement();
		JSONObject jsonData = leapDataCtx.getContextElementForTag(TEST_TAG_NAME);
		Assert.assertNotNull("Josn Data Should not be null ::", jsonData);
		Assert.assertTrue(jsonData.toString().contains(jsonAsString));
	}

	/**
	 * Method to fetch List of Context Elements for the same 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetContextElementsForTag() throws LeapDataContextInitialzerException {
		List<JSONObject> emptyListOfJson = leapDataCtx.getContextElementsForTag(TEST_TAG_NAME);
		Assert.assertFalse("should be empty because not add context element ::", emptyListOfJson.size() > 0);

		testAddContextElement();
		List<JSONObject> listOfJson = leapDataCtx.getContextElementsForTag(TEST_TAG_NAME);
		Assert.assertTrue("json data list should not be empty ::", listOfJson.size() > 0);
		Assert.assertTrue(listOfJson.get(0).toString().contains(jsonAsString));
	}

	/**
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws LeapDataContextInitialzerException 
	 */
	@Test
	public void testGetContextElementForKinds() throws LeapDataContextConfigException, LeapDataContextInitialzerException {
		JSONObject jsonObject = new JSONObject(jsonAsString);

		List<String> kinds = new ArrayList<>();
		kinds.add(TEST_KIND);

		JSONArray nullJsonArray = leapDataCtx.getContextElementForKinds(kinds);
		Assert.assertNull("should be null because not add context element ::", nullJsonArray);

		testAddContextElement();
		JSONArray jsonArray = leapDataCtx.getContextElementForKinds(kinds);
		Assert.assertNotNull("json Array Should not be null ::", jsonArray);
		Assert.assertEquals(jsonObject.getJSONObject("test").toString(), jsonArray.get(0).toString());
	}

	/**
	 * Method to fetch the Context Elements for the same 'kind'
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws LeapDataContextInitialzerException 
	 */
	@Test
	public void testGetContextElementForKind() throws LeapDataContextConfigException, LeapDataContextInitialzerException {
		JSONObject jsonObject = new JSONObject(jsonAsString);

		JSONArray nullJsonArray = leapDataCtx.getContextElementForKind(TEST_KIND);
		Assert.assertFalse("should be empty because not add context element ::", nullJsonArray.length() > 0);

		testAddContextElement();
		JSONArray jsonArray = leapDataCtx.getContextElementForKind(TEST_KIND);
		Assert.assertNotNull("json Array Should not be null ::", jsonArray);
		Assert.assertEquals(jsonObject.getJSONObject("test").toString(), jsonArray.get(0).toString());
	}

	/**
	 * Method to get the related MetaData of a Context Element by
	 * EffectiveColumnName
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetMetaDataByEffectiveName() throws LeapDataContextInitialzerException {
		MetaData nullMetaData = leapDataCtx.getMetaDataByEffectiveName("test");
		Assert.assertNull("should be empty because not add context element ::", nullMetaData);

		testAddContextElement();
		MetaData metaData = leapDataCtx.getMetaDataByEffectiveName("test");
		Assert.assertNotNull("Metadata Should not be null ::", metaData);
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, metaData.getByteLenth());

	}

	/**
	 * Method to get the related MetaData of a Context Element by ActualColumnName
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */

	@Test
	public void testGetMetaDataByActualName() throws LeapDataContextInitialzerException {
		MetaData nullMetaData = leapDataCtx.getMetaDataByActualName("data");
		Assert.assertNull("should be empty because not add context element ::", nullMetaData);

		testAddContextElement();
		MetaData metaData = leapDataCtx.getMetaDataByActualName("data");
		Assert.assertNotNull("Metadata Should not be null ::", metaData);
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, metaData.getByteLenth());
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'tagName'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetMetaDataOfContextElementByTag() throws LeapDataContextInitialzerException {
		List<MetaData> nullListOfMetaData = leapDataCtx.getMetaDataOfContextElementByTag(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullListOfMetaData);

		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByTag(TEST_TAG_NAME);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'tagName'
	 * and 'kind'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */

	@Test
	public void testGetMetaDataOfContextElementByTagKind() throws LeapDataContextInitialzerException {
		List<MetaData> nullListOfMetaData = leapDataCtx.getMetaDataOfContextElementByTagKind(TEST_TAG_NAME, TEST_KIND);
		Assert.assertNull("should be null because not add context element ::", nullListOfMetaData);

		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByTagKind(TEST_TAG_NAME, TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());
	}

	/**
	 * Method to get the related List of MetaData of a Context Element by 'kind'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetMetaDataOfContextElementByKind() throws LeapDataContextInitialzerException {
		List<MetaData> nullListOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNull("should be null because not add context element ::", nullListOfMetaData);

		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());
	}

	/**
	 * Method to fetch the Initial Raw Request Data for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetInitialRequestData() throws LeapDataContextInitialzerException {
		LeapResultSet nullLeapResult = leapDataCtx.getInitialRequestData();
		Assert.assertNull("should be null because not add context element ::", nullLeapResult);

		testAddContextElement();
		LeapResultSet leapResultSet = leapDataCtx.getInitialRequestData();
		Assert.assertNotNull("leap result set Should not be null ::", leapResultSet);
		Assert.assertEquals("json Data Should be Same ::", jsonAsString, leapResultSet.getData().toString());
	}

	/**
	 * Method to fetch the Initial Raw Request Data for a 'tag'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetDataByTag() throws LeapDataContextInitialzerException {
		LeapResultSet nullLeapResult = leapDataCtx.getDataByTag(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullLeapResult);

		testAddContextElement();
		LeapResultSet leapResultSet = leapDataCtx.getDataByTag(TEST_TAG_NAME);
		Assert.assertNotNull("leap result set Should not be null ::", leapResultSet);
		Assert.assertEquals("json Data Should be Same ::", jsonAsString, leapResultSet.getData().toString());
	}

	/**
	 * Method to fetch All Context Elements Stored
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetAllContextElement() throws LeapDataContextInitialzerException {
		List<LeapDataElement> emptyLDEList = leapDataCtx.getAllContextElement();
		Assert.assertFalse("list of leapDataElement should be zero ::", emptyLDEList.size() > 0);

		testAddContextElement();
		List<LeapDataElement> listOfLDE = leapDataCtx.getAllContextElement();
		Assert.assertTrue("list of leapDataElement should be greater then zero ::", listOfLDE.size() > 0);
		Assert.assertTrue("Should be contain json data in list of leapDataElement ::",
				listOfLDE.toString().contains(jsonAsString));
	}

	/**
	 * Method to fetch All Context Elements Stored with Mapped 'tags'
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetAllContextElementWithMappedTag() throws LeapDataContextInitialzerException {
		List<Map<String, LeapDataElement>> emptyLDEList = leapDataCtx.getAllContextElementWithMappedTag();
		Assert.assertFalse("list of leapDataElement should be zero ::", emptyLDEList.size() > 0);

		testAddContextElement();

		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_TAG_NAME);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
		Assert.assertEquals(jsonAsString, leapDataElement.getData().getItems().getData().toString());

		List<Map<String, LeapDataElement>> listOfLDE = leapDataCtx.getAllContextElementWithMappedTag();
		Assert.assertTrue("list of leapDataElement should be greater then zero ::", listOfLDE.size() > 0);
		Assert.assertEquals(listOfLDE.get(0).get(TEST_TAG_NAME), leapDataElement);
	}

	/**
	 * Method to get all recent/latest context elements from the Deque
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetNormalizedContext() throws LeapDataContextInitialzerException {
		List<JSONObject> emptyListOfJson = leapDataCtx.getNormalizedContext();
		Assert.assertFalse("should be empty because not add context element ::", emptyListOfJson.size() > 0);

		testAddContextElement();
		List<JSONObject> listOfJson = leapDataCtx.getNormalizedContext();
		Assert.assertTrue("json data list should not be empty ::", listOfJson.size() > 0);
		Assert.assertTrue(listOfJson.get(0).toString().contains(jsonAsString));
	}

	/**
	 * Method to get the count of row data present inside the DataSet Object stored
	 * inside the context element
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testGetRowCountForResponseDataSet() throws LeapDataContextInitialzerException {
		JSONArray jsonObject = new JSONArray(jsonArrayAsString);
		leapDataCtx.addContextElement(jsonObject, TEST_KIND, "TEST", null);

		int check = leapDataCtx.getRowCountForResponseDataSet("TEST");
		Assert.assertNotEquals("Row count response should be greater then 0 ::", 0, check);
		Assert.assertEquals("In this test case only one data inside jsonArray ::", 1, check);
	}

	/**
	 * Method to determine the instance of the request/response data stored inside
	 * the context element
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetContextElementDataType() throws LeapDataContextInitialzerException {
		Class<? extends Object> nullInstanceOfData = leapDataCtx.getContextElementDataType(TEST_TAG_NAME);
		Assert.assertNull("should be null because not add context element ::", nullInstanceOfData);

		testAddContextElement();
		Class<? extends Object> instanceOfData = leapDataCtx.getContextElementDataType(TEST_TAG_NAME);
		Assert.assertNotNull("insatnce of data should not be null ::", instanceOfData);
		Assert.assertEquals("both class should be same as jsonObject ::", JSONObject.class, instanceOfData);
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetDataElementFromResponse() throws LeapDataContextInitialzerException {
		Object nullData = leapDataCtx.getDataElementFromResponse(TEST_TAG_NAME, "test");
		Assert.assertNull("should be null because not add context element ::", nullData);

		testAddContextElement();
		Object data = leapDataCtx.getDataElementFromResponse(TEST_TAG_NAME, "test");
		Assert.assertNotNull("data Should not be null ::", data);
		Assert.assertEquals("Data should be same as getdataElementfromResponse ::", "[{\"data\":\"demo\"}]",
				data.toString());
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element using JSONPath
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetDataElementFromResponseUsingJSONPath() throws LeapDataContextInitialzerException {
		Object nullData = leapDataCtx.getDataElementFromResponseUsingJSONPath(TEST_TAG_NAME, "test");
		Assert.assertNull("should be null because not add context element ::", nullData);

		testAddContextElement();
		Object data = leapDataCtx.getDataElementFromResponseUsingJSONPath(TEST_TAG_NAME, "test");
		Assert.assertNotNull("data Should not be null ::", data);
		Assert.assertEquals("Data should be same as getdataElementfromResponse ::", "[{\"data\":\"demo\"}]",
				data.toString());
	}

	/**
	 * Method to get specific value based on 'key' from the request/response object
	 * stored inside context element using XPath
	 * 
	 * @throws LeapDataContextConfigException
	 * @throws LeapDataContextInitialzerException 
	 */
	@Test
	public void testGetDataElementFromResponseUsingXPath() throws LeapDataContextConfigException, LeapDataContextInitialzerException {
		Object nullData = leapDataCtx.getDataElementFromResponseUsingXPath(TEST_TAG_NAME, "test");
		Assert.assertNull("should be null because not add context element ::", nullData);

		testAddContextElement();
		Object data = leapDataCtx.getDataElementFromResponseUsingXPath(TEST_TAG_NAME, "test");
		Assert.assertNotNull("data Should not be null ::", data);
		Assert.assertEquals("Data should be same as getdataElementfromResponse ::", "{\"data\":\"demo\"}",
				data.toString());
	}

	/**
	 * Method to get the list of data elements from the response
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetListOfDataElementFromResponse() throws LeapDataContextInitialzerException {
		Object nullData = leapDataCtx.getListOfDataElementFromResponse(TEST_TAG_NAME, "test");
		Assert.assertNull("should be null because not add context element ::", nullData);

		testAddContextElement();
		Object data = leapDataCtx.getListOfDataElementFromResponse(TEST_TAG_NAME, "test");
		Assert.assertNotNull("data Should not be null ::", data);
		Assert.assertEquals("Data should be same as getdataElementfromResponse ::", "[{\"data\":\"demo\"}]",
				data.toString());
	}

	/**
	 * Method to fetch ActulaColumnName from the MetaData based on the
	 * EffectiveColumnName
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetActualColumnName() throws LeapDataContextInitialzerException {
		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());

		String data = leapDataCtx.getActualColumnName("test", listOfMetaData);
		Assert.assertNotNull("String data should not be null ::", data);
		Assert.assertEquals("Actual Data And Expected data should be same :: ", "test", data);
	}

	@Test(expected = NullPointerException.class)
	public void testGetActualColumnNameFail() {
		List<MetaData> listOfMetaData = null;
		leapDataCtx.getActualColumnName(null, listOfMetaData);

	}

	/**
	 * Method to fetch EffectiveColumnName from the MetaData based on the
	 * ActulaColumnName
	 * @throws LeapDataContextInitialzerException 
	 * 
	 */
	@Test
	public void testGetEffectiveColumnName() throws LeapDataContextInitialzerException {
		testAddContextElement();
		List<MetaData> listOfMetaData = leapDataCtx.getMetaDataOfContextElementByKind(TEST_KIND);
		Assert.assertNotNull("list of metadata  Should not be null ::", listOfMetaData);
		Assert.assertEquals("EffectiveColumnName Should be same ::", "test",
				listOfMetaData.get(0).getEffectiveColumnName());
		Assert.assertEquals("metaData byte length should be same as 4 ::", 4, listOfMetaData.get(0).getByteLenth());

		String data = leapDataCtx.getEffectiveColumnName("test", listOfMetaData);
		Assert.assertNotNull("String data should not be null ::", data);
		Assert.assertEquals("Actual Data And Expected data should be same :: ", "test", data);
	}

	@Test(expected = NullPointerException.class)
	public void testGetEffectiveColumnNameFail() {
		List<MetaData> listOfMetaData = null;
		leapDataCtx.getEffectiveColumnName(null, listOfMetaData);
	}

	/**
	 * Method apply projection but no taxonomyId
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testGetNormalizedContextWithProjectionFileWithSwagger() throws LeapDataContextInitialzerException {
		setServiceName();
		addContextElementWithSetRequestHeader();

		leapDataCtx.addContextElement(jsonAsString, TEST_KIND, TEST_TAG_NAME, null);

		Object normalizedContextWithSwagger = leapDataCtx.getNormalizedContext("swagger", "test_Swagger.json");
		Assert.assertNotNull("normalized context should not be null ::", normalizedContextWithSwagger);
		Assert.assertTrue("normalizedContext should be contains with root ::",
				normalizedContextWithSwagger.toString().contains("root"));

	}

	/**
	 * Method fail to apply projection but no taxonomyId
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test(expected = LeapDataContextInitialzerException.class)
	public void testGetNormalizedContextWithProjectionFileWithSwaggerFail() throws LeapDataContextInitialzerException {
		leapDataCtx.getNormalizedContext("swagger", "test_Swagger.json");
		Assert.fail("failed because not initilaization leapServiceContext :");
	}

	/**
	 * Apply only taxonomy not projection . If taxonomyId is null, vendor taxonomyId
	 * is applied
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testGetNormalizedContextWithTaxo() throws LeapDataContextInitialzerException {
		setServiceName();
		testAddContextElement();
		Object normalizedContext = leapDataCtx.getNormalizedContext("testTaxonomy");
		Assert.assertNotNull("normalizedContext should not be null ::", normalizedContext);
		Assert.assertTrue("normalizedContext should be contains with root ::",
				normalizedContext.toString().contains("root"));
	}

	/**
	 * failed to Apply only taxonomy
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test(expected = LeapDataContextInitialzerException.class)
	public void testGetNormalizedContextWithTaxoFail() throws LeapDataContextInitialzerException {
		leapDataCtx.getNormalizedContext("testTaxonomy");
		Assert.fail("failed because not initilaization leapServiceContext :");

	}

	/**
	 * both projection and taxonomy is applied . All arguments should not be null or
	 * empty.
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testGetNormalizedContextWithProjectionAndTaxonomy() throws LeapDataContextInitialzerException {
		setServiceName();
		addContextElementWithSetRequestHeader();
		testAddContextElement();
		Object normalizedContext = leapDataCtx.getNormalizedContext("swagger", "test_Swagger.json", "testTaxonomy");
		Assert.assertEquals("both class type should be same as jsonArray ::", JSONArray.class,
				normalizedContext.getClass());
		Assert.assertTrue("normalized Context should be contain as root ::",
				normalizedContext.toString().contains("root"));
		Assert.assertTrue("normalized Context should be apply taxonomy 'test:TaxonomyTest' ::",
				normalizedContext.toString().contains("TaxonomyTest"));
	}

	/**
	 * failed to apply projection and taxonomy.
	 * 
	 * @throws LeapDataContextInitialzerException
	 */
	@Test(expected = LeapDataContextInitialzerException.class)
	public void testGetNormalizedContextWithProjectionAndTaxonomyFail() throws LeapDataContextInitialzerException {
		leapDataCtx.getNormalizedContext("swagger", "test_Swagger.json", "testTaxonomy");
		Assert.fail("failed because not initilaization leapServiceContext :");
	}

	/**
	 * this method is used to apply template with taxonomy
	 * 
	 * @throws UnableToApplyTemplateException
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testApplyTemplateWithTaxonomy()
			throws UnableToApplyTemplateException, LeapDataContextInitialzerException {
		setServiceName();
		addContextElementWithSetRequestHeader();
		testAddContextElement();
		Object applyTemplate = leapDataCtx.applyTemplate(false, false, "testTaxonomy", "swagger");
		Assert.assertEquals("both class type should be same as jsonArray ::", JSONArray.class,
				applyTemplate.getClass());
		Assert.assertTrue("applyTemplate should be contain as root ::", applyTemplate.toString().contains("root"));
		Assert.assertTrue("ApplyTemplate should be apply taxonomy 'test:TaxonomyTest' ::",
				applyTemplate.toString().contains("TaxonomyTest"));

	}

	/**
	 * this method failed to apply template with taxonomy
	 * 
	 * @throws UnableToApplyTemplateException
	 */
	@Test(expected = UnableToApplyTemplateException.class)
	public void testApplyTemplateWithTaxonomyFail() throws UnableToApplyTemplateException {
		leapDataCtx.applyTemplate(false, false, "testTaxonomy", "swagger");
		Assert.fail("failed because not initilaization leapServiceContext :");

	}

	/**
	 * this method is used to apply Template, projection and taxonomy
	 * 
	 * @throws UnableToApplyTemplateException
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testApplyTemplateWithProjectionSourceAndTaxonomyWithProjectionTrue()
			throws UnableToApplyTemplateException, LeapDataContextInitialzerException {
		leapDataCtx.getServiceDataContext(GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		setServiceName();
		addContextElementWithSetRequestHeader();
		testAddContextElement();
		Object applyTemplate = leapDataCtx.applyTemplate(false, true, "testTaxonomy", "swagger");
		Assert.assertEquals("both class type should be same as jsonArray ::", JSONArray.class,
				applyTemplate.getClass());
		Assert.assertTrue("applyTemplate should be contain as root ::", applyTemplate.toString().contains("root"));
		Assert.assertTrue("ApplyTemplate should be apply taxonomy 'test:TaxonomyTest' ::",
				applyTemplate.toString().contains("TaxonomyTest"));

	}

	/**
	 * failed to apply template with projection and taxonomy ::
	 * 
	 * @throws UnableToApplyTemplateException
	 */
	@Test(expected = UnableToApplyTemplateException.class)
	public void testApplyTemplateWithProjectionSourceAndTaxonomyWithProjectionFail()
			throws UnableToApplyTemplateException {
		leapDataCtx.applyTemplate(false, true, "testTaxonomy", "swagger");
		Assert.fail("failed because not initilaization leapServiceContext :");
	}

	/**
	 * this method is used to apply projection and taxonomy
	 * 
	 * @throws UnableToApplyTemplateException
	 * @throws LeapDataContextInitialzerException
	 */
	@Test
	public void testApplyTemplateWithProjectionFileAndTaxonomy()
			throws UnableToApplyTemplateException, LeapDataContextInitialzerException {
		leapDataCtx.getServiceDataContext(GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		setServiceName();
		addContextElementWithSetRequestHeader();
		testAddContextElement();
		Object applyTemplate = leapDataCtx.applyTemplate(false, true, null, "test_Swagger.json", "swagger",
				"testTaxonomy");
		Assert.assertEquals("both class type should be same as jsonArray ::", JSONArray.class,
				applyTemplate.getClass());
		Assert.assertTrue("applyTemplate should be contain as root ::", applyTemplate.toString().contains("root"));
		Assert.assertTrue("ApplyTemplate should be apply taxonomy 'test:TaxonomyTest' ::",
				applyTemplate.toString().contains("TaxonomyTest"));
	}

	/**
	 * this method failed to apply projection and taxonomy
	 * 
	 * @throws UnableToApplyTemplateException
	 */
	@Test(expected = UnableToApplyTemplateException.class)
	public void testApplyTemplateWithProjectionFileAndTaxonomyFail() throws UnableToApplyTemplateException {

		leapDataCtx.applyTemplate(false, true, null, "test_Swagger.json", "swagger", "testTaxonomy");
		Assert.fail("failed because not initilaization leapServiceContext :");

	}

	/**
	 * Method to get the LDC Response as Normalized Context based on the Template
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws PermaStoreConfigRequestException
	 * @throws InvalidNodeTreeException
	 */
	@Test
	public void testGetNormalizedContextUsingTemplate()
			throws LeapDataContextInitialzerException, InvalidNodeTreeException, PermaStoreConfigRequestException {

		String expectedData = "[{\"root\":{\"TaxonomyTest\":[{\"TaxonomyData\":\"demo\",\"checkData\":{\"third\":101,\"four\":102}}]}}]";

		JSONObject jsonObject = new JSONObject(jsonAsString);
		leapDataCtx.addContextElement(jsonObject, "test", "test", null);

		addContextElementWithSetRequestHeader();
		setServiceName();
		permastoreDataCheck();

		Object normalizedContextUsingTemplate = leapDataCtx.getNormalizedContextUsingTemplate("testTemp_Template.json",
				"swagger", "test_Swagger.json", "testTaxonomy");

		Assert.assertNotNull("normalizedContextUsingTemplate data should not be null ::",
				normalizedContextUsingTemplate);
		Assert.assertEquals("normalizedContextUsingTemplate class will be same as expected class ::", JSONArray.class,
				normalizedContextUsingTemplate.getClass());

		JSONArray jsonArray = (JSONArray) normalizedContextUsingTemplate;

		Assert.assertEquals("normalizedContextUsingTemplate rootTag should be same as 'root':: ", "root",
				jsonArray.getJSONObject(0).keys().next());
		Assert.assertEquals("normalizedContextUsingTemplate Data must be same as expected data :", expectedData,
				normalizedContextUsingTemplate.toString());
	}

	/**
	 * Method fail to get the LDC Response as Normalized Context based on the
	 * Template
	 * 
	 * @throws LeapDataContextInitialzerException
	 */

	@Test(expected = LeapDataContextInitialzerException.class)
	public void testgetNormalizedContextUsingTemplateFail() throws LeapDataContextInitialzerException {
		leapDataCtx.getNormalizedContextUsingTemplate("testTemp_Template.json", "swagger", "test_Swagger.json",
				"testTaxonomy");
		Assert.fail("failed because not initilaization leapServiceContext :");

	}

	/**
	 * this method is used to apply Template, projection and taxonomy
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws JSONException
	 * @throws UnableToApplyTemplateException
	 * @throws InvalidNodeTreeException
	 * @throws PermaStoreConfigRequestException
	 */
	@Test
	public void testApplyTemplate() throws JSONException, UnableToApplyTemplateException, InvalidNodeTreeException,
			PermaStoreConfigRequestException, LeapDataContextInitialzerException {

		JSONObject jsonObject = new JSONObject(jsonAsString);
		leapDataCtx.addContextElement(jsonObject, "test", "test", null);

		addContextElementWithSetRequestHeader();
		setServiceName();
		permastoreDataCheck();

		Object applyTemplate = leapDataCtx.applyTemplate(true, true, "testTemp_Template.json", "test_Swagger.json",
				"swagger", "testTaxonomy");

		Assert.assertNotNull("apply template data should not be null ::", applyTemplate);
		Assert.assertEquals("Apply template class will be same as expected class ::", JSONArray.class,
				applyTemplate.getClass());

		JSONArray jsonArray = (JSONArray) applyTemplate;

		Assert.assertEquals("applyTemplate rootTag should be same as 'root':: ", "root",
				jsonArray.getJSONObject(0).keys().next());
	}

	/**
	 * this method is used failed to apply Template, projection and taxonomy
	 */
	@Test
	public void testApplyTemplateFail() {
		try {
			leapDataCtx.applyTemplate(true, true, "testTemp_Template.json", "test_Swagger.json", "swagger",
					"testTaxonomy");
			Assert.fail("failed because not set service Name, Request Header and Not adding permastore Data");
		} catch (Exception e) {
			Assert.assertEquals(UnableToApplyTemplateException.class, e.getClass());
		}
	}

	/**
	 * Method to apply taxonomy
	 * 
	 * @throws LeapDataContextInitialzerException
	 * @throws IOException
	 */
	@Test
	public void testApplyTaxonomy() throws LeapDataContextInitialzerException, IOException {
		try {
			leapDataCtx.applyTaxonomy(new JSONObject(jsonAsString), new JSONObject());
		} catch (NullPointerException e) {
			Assert.assertEquals(NullPointerException.class, e.getClass());
		}

		testGetNormalizedContextWithTaxo();
		JSONObject applyTaxonomy = leapDataCtx.applyTaxonomy(new JSONObject(jsonAsString), new JSONObject());
		Assert.assertNotNull("JsonObject should not be null :: ", applyTaxonomy);
		Assert.assertTrue(applyTaxonomy.toString().contains("TaxonomyTest"));
	}

	/**
	 * Method failed to apply taxonomy
	 */
	@Test
	public void testApplyTaxonomyFail() {
		try {
			leapDataCtx.applyTaxonomy(new JSONObject(jsonAsString), new JSONObject());
			Assert.fail("failed because not set service Name and Not add data into LDC.");
		} catch (NullPointerException e) {
			Assert.assertEquals(NullPointerException.class, e.getClass());
		}
	}

	/**
	 * Method to build the Netsed Structure for the response data based on
	 * projection
	 */
	@Test
	public void testBuildNestedStructure() {
		String json = "{\"root\":{\"data\":\"demo\"}}";
		JSONArray jsonArray = new JSONArray();
		JSONArray buildNestedStructure = leapDataCtx.buildNestedStructure(jsonArray, new JSONObject(json));
		Assert.assertNotNull("build nested Structure should not be null ::", buildNestedStructure);
		Assert.assertTrue("", buildNestedStructure.toString().contains(json.toString()));
	}

	/**
	 * Method to get the resource files
	 */
	@Test
	public void testGetResourceFile() {
		setServiceName();
		String resourceFile = leapDataCtx.getResourceFile("test.txt", null);
		Assert.assertNotNull("resource file data should not be null ::", resourceFile);
	}

	/**
	 * Method to get the request headers based on the 'tagName'
	 */
	@Test
	public void testGetRequestHeaders() {
		addContextElementWithSetRequestHeader();
		JSONObject requestHeaders = leapDataCtx.getRequestHeaders(TEST_HEADER_TAG);
		Assert.assertNotNull("Request Header Should not be null ::", requestHeaders);
		Assert.assertEquals("Request Header Should be have feature data ::", GenericTestConstant.TEST_FEATURE,
				requestHeaders.getString(GenericTestConstant.FEATURE));
	}

	/**
	 * Method to get the request headers based on the 'tagName'
	 */
	@Test
	public void testGetRequestMapHeaders() {
		addContextElementWithSetRequestHeader();
		Map<String, Object> requestMapHeaders = leapDataCtx.getRequestMapHeaders(TEST_HEADER_TAG);
		Assert.assertNotNull("Request Map Header Should not be null ::", requestMapHeaders);
		Assert.assertEquals("Request map Header Should be have feature data ::", GenericTestConstant.TEST_FEATURE,
				requestMapHeaders.get(GenericTestConstant.FEATURE));
	}

	/**
	 * Method to get the private headers based on the 'tagName'
	 */
	@Test
	public void testGetPrivateHeaders() {
		addContextElementWithSetRequestHeader();
		JSONObject privateHeaders = leapDataCtx.getPrivateHeaders(TEST_HEADER_TAG);
		Assert.assertNotNull("private header data should not be null ::", privateHeaders);
		Assert.assertEquals("private data Should be equla as expected and actual Data ::", "PrivateTestData",
				privateHeaders.getString("privateTest"));
	}

	/**
	 * Instance check of Integer
	 */
	@Test
	public void testInstanceOfInteger() {
		boolean isIntegerFalse = LeapDataContext.instanceOfInteger("1");
		Assert.assertFalse("Should be false because object is String", isIntegerFalse);

		boolean isIntegerTrue = LeapDataContext.instanceOfInteger(1);
		Assert.assertTrue("Should be true because object is Integer", isIntegerTrue);
	}

	/**
	 * Instance check of json object
	 */
	@Test
	public void testInstanceOfJSONObject() {
		boolean isJSONObjectFalse = LeapDataContext.instanceOfJSONObject(jsonArrayAsString);
		Assert.assertFalse("Should be false because object is String", isJSONObjectFalse);

		boolean isJSONObjectTrue = LeapDataContext.instanceOfJSONObject(new JSONObject(jsonAsString));
		Assert.assertTrue("Should be true because object is JsonObject", isJSONObjectTrue);
	}

	/**
	 * Method to add LeapDataContextElement inside the Deque against a 'tagName'
	 */
	@Test
	public void addContextElementWithSetRequestHeader() {
		Map<String, Object> reaquiredHeadersMap = new HashMap<String, Object>();
		reaquiredHeadersMap.put("featureGroup", "TestFeatureGroup");
		reaquiredHeadersMap.put("feature", "TestFeature");
		reaquiredHeadersMap.put("serviceName", "TestService");
		reaquiredHeadersMap.put("requestMethod", "post");

		Map<String, Object> privateHeadersMap = new HashMap<String, Object>();
		privateHeadersMap.put("privateTest", "PrivateTestData");

		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setRequestHeaderElement(reaquiredHeadersMap);
		initialRequestHeaderElement.setPrivateHeaderElement(privateHeadersMap);
		LeapDataContextElement createShipHeaderElement = new LeapDataContextElement(TEST_HEADER_TAG,
				initialRequestHeaderElement);
		leapDataCtx.addContextElement(createShipHeaderElement, TEST_HEADER_TAG);

		LeapDataElement leapDataElement = leapDataCtx.getContextElement(TEST_HEADER_TAG);
		Assert.assertNotNull("Leap Data Context Should not be null :: ", leapDataElement);
	}

	/**
	 * check permstore data exist or not:
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws PermaStoreConfigRequestException
	 */
	private void permastoreDataCheck() throws InvalidNodeTreeException, PermaStoreConfigRequestException {
		IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);
		PermaStoreConfigurationUnit perConfigUnit = permastoreConfigService.getPermaStoreConfiguration(requestContext,
				CONFIG_NAME);
		if (perConfigUnit == null)
			addPermastoreConfig();
	}

	/**
	 * set service name and initialization
	 */
	private void setServiceName() {
		leapDataCtx.getServiceDataContext().setRequestContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		leapDataCtx.getServiceDataContext().initializeLeapRuntimeServiceContext("TestService");
		leapDataCtx.getServiceDataContext().SetRunningContextServiceName("TestService");
	}

	/**
	 * read permastore File from resource folder.
	 * 
	 * @return
	 * @throws PermaStoreConfigParserException
	 */
	private PermaStoreConfigurations getPermaStoreConfigurations() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();
		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * Adding permastore configuration into configNodeData Table.
	 */
	private void addPermastoreConfig() {
		PermaStoreConfiguration permaStoreConfiguration;
		try {
			permaStoreConfiguration = getPermaStoreConfigurations().getPermaStoreConfiguration().get(0);
			Assert.assertNotNull("permastore configuration Should not be null ::", permaStoreConfiguration);

			IPermaStoreConfigurationService permastoreConfigService = new PermaStoreConfigurationService();
			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("configuration context should not be null ::", configurationContext);

			permastoreConfigService.addPermaStoreConfiguration(configurationContext, permaStoreConfiguration);

			RequestContext context = ContextData.getRequestContext();
			leapDataCtx.getServiceDataContext().setRequestContext(context);
			leapDataCtx.getServiceDataContext().storePermastoreConfigToServiceContext(CONFIG_NAME);

			Object permastoreData = leapDataCtx.getServiceDataContext()
					.getPermastoreByNameFromServiceContext(CONFIG_NAME);
			leapDataCtx.addContextElement(new JSONObject(permastoreData.toString()), "checkData", "checkData", null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
