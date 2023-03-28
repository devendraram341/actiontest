package com.attunedlabs.leap.context.bean.helper;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.exception.UnableToApplyTaxonomyException;
import com.attunedlabs.leap.context.exception.UnableToApplyTemplateException;

import static com.attunedlabs.LeapCoreTestConstant.*;

public class LeapDataContextConfigurationHelperTest {

	Logger logger = LoggerFactory.getLogger(LeapDataContextConfigurationHelperTest.class);
	private LeapDataContextConfigurationHelper configurationHelper;
	private FeatureDeploymentTestConfigDB configDB;
	private Exchange exchange;
	private LeapDataContext context;

	@Before
	public void setup() {
		if (configDB == null)
			configDB = new FeatureDeploymentTestConfigDB();
		if (configurationHelper == null)
			configurationHelper = new LeapDataContextConfigurationHelper();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (context == null)
			context = new LeapDataContext();
		configDB.addFeatureDeployement();
	}

	/**
	 * this method is used to test taxonomy with file name
	 * 
	 * @throws UnableToApplyTemplateException
	 */
	@Test
	public void testApplyTemplateWithTaxonomy() throws UnableToApplyTemplateException {
		LeapCoreTestUtils.setHeaders(context, exchange);
		Object applyTemplate = configurationHelper.applyTemplate(false, false, null, TEST_TAXONOMY, null, null,
				exchange);

		Assert.assertNotNull("Apply Templete data should not be null :", applyTemplate);
		Assert.assertEquals("applyTemplate instanceOf Should be match ::", JSONArray.class, applyTemplate.getClass());

		JSONArray jsonArray = (JSONArray) applyTemplate;
		Assert.assertNotNull("JsonArray Should not be null ::", jsonArray);

		String rootKey = jsonArray.getJSONObject(0).keys().next();
		Assert.assertNotNull("Root key must be exist ::", rootKey);
		Assert.assertEquals("rootKey Should be name is root ::", "root", rootKey);
		Assert.assertTrue("After Apply taxonomy should be contains data -> taxonomyData",
				jsonArray.toString().contains("TaxonomyData"));
	}

	/**
	 * this method is used to apply projection and taxonomy with file name
	 * 
	 * @throws UnableToApplyTemplateException
	 */
	@Test
	public void testApplyTemplateWithProjection() throws UnableToApplyTemplateException {
		LeapCoreTestUtils.setHeaders(context, exchange);
		Object applyTemplate = configurationHelper.applyTemplate(false, true, "swagger", TEST_TAXONOMY, null, null,
				exchange);

		Assert.assertNotNull("Apply Templete data should not be null :", applyTemplate);
		Assert.assertEquals("applyTemplate instanceOf Should be match ::", JSONArray.class, applyTemplate.getClass());

		JSONArray jsonArray = (JSONArray) applyTemplate;
		Assert.assertNotNull("JsonArray Should not be null ::", jsonArray);

		String rootKey = jsonArray.getJSONObject(0).keys().next();
		Assert.assertNotNull("Root key must be exist ::", rootKey);
		Assert.assertEquals("rootKey Should be name is root ::", "root", rootKey);
		Assert.assertTrue("After Apply taxonomy should be contains data -> taxonomyData",
				jsonArray.toString().contains("TaxonomyData"));
	}

	/**
	 * this method is used to get the vendorTaxonomyId. If vendor taxonomy is null,
	 * return taxonomyId present in request. If it is null return default
	 * taxonomyId.
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testGetVendorTaxonomyId() throws FeatureDeploymentServiceException {
		LeapCoreTestUtils.setHeaders(context, exchange);

		Object header = exchange.getIn().getHeader(TAXONOMY_ID);
		Assert.assertNull("Exchange header should be null because header not be set ::", header);

		configurationHelper.getVendorTaxonomyId(exchange);

		header = exchange.getIn().getHeader("taxonomyId");
		Assert.assertNotNull("Exchange header should not be null ::", header);
		Assert.assertEquals("Default taxonomy id of header should be leapDefault ::", LEAP_DEFAULT, header);
	}

	/**
	 * This helper method is used to generate the LDC response to the client
	 */
	@Test
	public void testGenerateResponse() {
		exchange.getIn().setBody(DEMO_JSON_DATA);
		Object generateResponse = configurationHelper.generateResponse(exchange);
		Assert.assertNotNull("generate response data should not be null :", generateResponse);
		Assert.assertEquals("generate Response Data should be same as expected data ::", DEMO_JSON_DATA,
				generateResponse);

		exchange.getIn().setBody(new JSONObject(DEMO_JSON_DATA));
		generateResponse = configurationHelper.generateResponse(exchange);
		Assert.assertNotNull("generate response data should not be null :", generateResponse);
		Assert.assertNotEquals("generateResponce data match but class is defferent then should not be equal",
				DEMO_JSON_DATA, generateResponse);
		Assert.assertEquals("generateResponse class should be same as jsonObejct class ::", JSONObject.class,
				generateResponse.getClass());
	}

	/**
	 * Method used to set LeapDataElement's data to exchange body for the given
	 * 'tagName'
	 */
	@Test
	public void testSetLeapDataElementIntoExchange() {
		LeapCoreTestUtils.setHeaders(context, exchange);

		Object body = exchange.getIn().getBody();
		Assert.assertNull("Exchange body should be null  because before Exchange body not be set here ::", body);

		configurationHelper.setLeapDataElementIntoExchange("initial", exchange);

		body = exchange.getIn().getBody();
		Assert.assertNotNull("Exchange body should not be null ::", body);
		Assert.assertEquals("exchange body data should be same as expected data ::", DEMO_JSON_DATA, body);
	}

	/**
	 * This method is used to apply taxonomy to exchange body and store it in
	 * exchange itself
	 * 
	 * @throws UnableToApplyTaxonomyException
	 */
	@Test
	public void testApplyTaxonomy() throws UnableToApplyTaxonomyException {
		LeapCoreTestUtils.setHeaders(context, exchange);
		exchange.getIn().setBody(DEMO_JSON_DATA);
		exchange.getIn().setHeader(TAXONOMY_ID, TEST_TAXONOMY);

		Object body = exchange.getIn().getBody();
		Assert.assertNotNull("Exchange body should not be null ::", body);
		Assert.assertEquals("Exchange body data should be same as expected data :", DEMO_JSON_DATA, body);

		configurationHelper.applyTaxonomy(exchange);

		body = exchange.getIn().getBody();
		Assert.assertNotNull("exchange body should not be null ::", body);
		Assert.assertNotEquals(
				"exchange body should not equal because after apply taxonomy key value have been changed ::",
				DEMO_JSON_DATA, body);
	}

	/**
	 * This method is used to apply taxonomy to exchange body and store it in
	 * exchange itself
	 * 
	 * @throws UnableToApplyTaxonomyException
	 */
	@Test
	public void testApplyTaxonomyToExchangeBody() throws UnableToApplyTaxonomyException {
		LeapCoreTestUtils.setHeaders(context, exchange);
		exchange.getIn().setBody(DEMO_JSON_DATA);

		Object body = exchange.getIn().getBody();
		Assert.assertNotNull("Exchange body should not be null ::", body);
		Assert.assertEquals("Exchange body data should be same as expected data :", DEMO_JSON_DATA, body);

		configurationHelper.applyTaxonomyToExchangeBody(TEST_TAXONOMY, exchange);

		body = exchange.getIn().getBody();
		Assert.assertNotNull("exchange body should not be null ::", body);
		Assert.assertNotEquals(
				"exchange body should not equal because after apply taxonomy key value have been changed ::",
				DEMO_JSON_DATA, body);
	}

	/**
	 * This method get request body from Ldc and apply taxonomy. After that it will
	 * store applied taxonomy body into LDC by given push Tag.
	 * 
	 * @throws UnableToApplyTaxonomyException
	 */
	@Test
	public void testApplyTaxonomyWithLDC() throws UnableToApplyTaxonomyException {

		LeapCoreTestUtils.setHeaders(context, exchange);

		LeapDataElement leapDataElement = context.getContextElement("testTagName");
		Assert.assertNull("LeapDataElement should be null becuase tagName not present in LDC ", leapDataElement);

		configurationHelper.applyTaxonomyWithLDC(TEST_TAXONOMY, TAG_NAME_LEAP_INITIAL, "testTagName", exchange);

		leapDataElement = context.getContextElement("testTagName");
		Assert.assertNotNull("LeapDataElement Data should not be null ::", leapDataElement);

		Object data = leapDataElement.getData().getItems().getData();
		Assert.assertNotNull("LeapDataElement item should not be null ::", data);
		Assert.assertTrue("data must be contain taxonomyTest because after apply taxonomy test -> taxonomyTest",
				data.toString().contains("TaxonomyTest"));

	}

	/**
	 * This method get request body from Exchange body and apply taxonomy. After
	 * that it will store applied taxonomy body into LDC by given push Tag.
	 * 
	 * @throws UnableToApplyTaxonomyException
	 */
	@Test
	public void testApplyTaxonomyToExchangeBodyAndPushToLDC() throws UnableToApplyTaxonomyException {
		LeapCoreTestUtils.setHeaders(context, exchange);

		exchange.getIn().setBody(DEMO_JSON_DATA);

		configurationHelper.applyTaxonomyToExchangeBodyAndPushToLDC(TEST_TAXONOMY, "testPushTag", exchange);

		LeapDataElement leapDataElement = context.getContextElement("testPushTag");

		Assert.assertNotNull("LeapDataElement Data should not be null ::", leapDataElement);

		Object data = leapDataElement.getData().getItems().getData();
		Assert.assertNotNull("LeapDataElement item should not be null ::", data);
		Assert.assertTrue("data must be contain taxonomyTest because after apply taxonomy test -> taxonomyTest",
				data.toString().contains("TaxonomyTest"));
		Assert.assertNotEquals("Data should not be equal expected data ::", DEMO_JSON_DATA, data);

	}

	@After
	public void cleanUp() {
		configDB.deleteFeatureDeployement();
	}

}
