package com.attunedlabs.leap.feature.routing;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.generic.UnableToLoadPropertiesException;

public class ExecutionFeatureDynamicTest {

	private ExecutionFeatureDynamic featureDynamic;
	private Exchange exchange;
	private LeapDataContext leapDataCtx;
	private Message message;
	private FeatureDeploymentTestConfigDB configDB;

	Logger logger = LoggerFactory.getLogger(ExecutionFeatureDynamicTest.class);

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (featureDynamic == null)
			featureDynamic = new ExecutionFeatureDynamic();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		if (configDB == null)
			configDB = new FeatureDeploymentTestConfigDB();

		message = exchange.getIn();
		configDB.addFeatureDeployement();

	}

	/**
	 * This method is for dynamically routing to Implementation route with provider.
	 * 
	 * @throws JSONException
	 * @throws UnableToLoadPropertiesException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws ConfigServerInitializationException
	 */
	@Test
	public void testRouteWithProvider() throws JSONException, UnableToLoadPropertiesException,
			DynamicallyImplRoutingFailedException, ConfigServerInitializationException {

		LeapHeaderConstant.site = TEST_SITE;
		LeapHeaderConstant.tenant = TEST_TENANT;
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setHeader(PROVIDER, "TestProvider");

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertNull("Exchnage header not have implRoute key then should be null ::", headers.get("implroute"));

		addConfigurationUnit();
		featureDynamic.route(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header have ImplRoute key thet should not be null ::", headers.get("implroute"));

		String value = getFeature().getService().get(0).getGenericRestEndpoint().getValue().trim();
		Assert.assertNotNull("GenericRestEndpint should not be null :: ", value);
		Assert.assertFalse("GenericRestEndpint should not be empty :: ", value.isEmpty());

		Assert.assertEquals("exchange header implroute should have same as GenericRestEndpint value ::", value,
				headers.get("implroute"));

	}

	/**
	 * This method is for dynamically routing to Implementation route without
	 * provider.
	 * 
	 * @throws JSONException
	 * @throws UnableToLoadPropertiesException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws ConfigServerInitializationException
	 */
	@Test
	public void testRouteWithoutProvider() throws JSONException, UnableToLoadPropertiesException,
			DynamicallyImplRoutingFailedException, ConfigServerInitializationException {

		LeapHeaderConstant.site = TEST_SITE;
		LeapHeaderConstant.tenant = TEST_TENANT;
		
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setHeader("FeatureDeployment", setFeatureDeployment());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertNull("Exchnage header not have implRoute key then should be null ::", headers.get("implroute"));

		addConfigurationUnit();
		featureDynamic.route(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header have ImplRoute key thet should not be null ::", headers.get("implroute"));

		String value = getFeature().getService().get(0).getGenericRestEndpoint().getValue().trim();
		Assert.assertNotNull("GenericRestEndpint should not be null :: ", value);
		Assert.assertFalse("GenericRestEndpint should not be empty :: ", value.isEmpty());

		Assert.assertEquals("exchange header implroute should have same as GenericRestEndpint value ::", value,
				headers.get("implroute"));
	}

	/**
	 * This method is for dynamically routing to Implementation route without
	 * provider and FeatureDeployment.
	 * 
	 * @throws JSONException
	 * @throws UnableToLoadPropertiesException
	 * @throws DynamicallyImplRoutingFailedException
	 * @throws ConfigServerInitializationException
	 */
	@Test(expected = DynamicallyImplRoutingFailedException.class)
	public void testRouteWithoutProvicerAndFeatureDeployment() throws JSONException, UnableToLoadPropertiesException,
			DynamicallyImplRoutingFailedException, ConfigServerInitializationException {

		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertNull("Exchnage header not have implRoute key then should be null ::", headers.get("implroute"));

		featureDynamic.route(exchange);
	}

	@After
	public void cleanUp() {
		configDB.deleteFeatureDeployement();
	}

	private FeatureDeployment setFeatureDeployment() {
		FeatureDeployment deployment = new FeatureDeployment();
		deployment.setImplementationName(TEST_IMPL);
		deployment.setVendorName(TEST_VENDOR);
		deployment.setFeatureVersion(TEST_VERSION);
		return deployment;
	}

	private void addConfigurationUnit() throws ConfigServerInitializationException {
		FeatureConfigurationUnit configurationUnit = new FeatureConfigurationUnit("all", "all", TEST_VENDOR_NODEID,
				true, getFeature());
		LeapConfigurationServer.getConfigurationService().addConfiguration(configurationUnit);
	}

	private Feature getFeature() {
		String xmlString = LeapCoreTestFileRead.getFeatureServiceXMLFileToString();
		FeaturesServiceInfo featureServiceInfo = null;
		try {
			featureServiceInfo = new  FeatureConfigXMLParser().marshallXMLtoObject(xmlString);
		} catch (FeatureConfigParserException e) {
			e.printStackTrace();
		}
		return featureServiceInfo.getFeatures().getFeature();
	}

}
