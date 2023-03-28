package com.attunedlabs.feature.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.Service;

public class FeatureConfigurationServiceTest {

	final Logger log = LoggerFactory.getLogger(FeatureConfigurationServiceTest.class);
	private IFeatureConfigurationService featureConfigurationService;
	private IConfigPersistenceService perService;

	private final String FEATURE_FILE = "FeatureService/testfeature.xml";

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 * @throws FeatureConfigParserException
	 * @throws FeatureConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Before
	public void init() throws FeatureConfigurationException, FeatureConfigParserException, ConfigPersistenceException {
		if (featureConfigurationService == null)
			featureConfigurationService = new FeatureConfigurationService();
		if (perService == null)
			perService = new ConfigPersistenceServiceMySqlImpl();

		perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		testAddFeatureConfiguration();
	}

	/**
	 * This method use for get featureConfigurationUnit data and tested.
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testGetFeatureConfiguration() throws FeatureConfigurationException {
		FeatureConfigRequestContext featureConfigContext = new FeatureConfigRequestContext(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("Feature Config Request Context should not be null :: ", featureConfigContext);
		Assert.assertEquals("In this case feature group should be same as TestFeature ",
				GenericTestConstant.TEST_FEATUREGROUP, featureConfigContext.getFeatureGroup());

		FeatureConfigurationUnit featureConfigUnit = featureConfigurationService
				.getFeatureConfiguration(featureConfigContext, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("Feature Config Unit should not be null ::", featureConfigUnit);
		Assert.assertEquals("feature config key should be same as feature Name", GenericTestConstant.TEST_FEATURE,
				featureConfigUnit.getKey());
	}

	/**
	 * This method use for adding new service in featrue Configuration
	 * 
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testAddNewServiceInFeatureConfiguration()
			throws FeatureConfigurationException, FeatureConfigParserException {
		ConfigurationContext context = ContextData.getConfigContext();

		Service service = getFeaturesServiceInfo().getFeatures().getFeature().getService().get(0);
		featureConfigurationService.addNewServiceInFeatureConfiguration(context, service);
	}

	/**
	 * This method use for update feature configuration detail.
	 * 
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testUpdateFeatureConfiguration()
			throws FeatureConfigurationException, FeatureConfigParserException, ConfigPersistenceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();

		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = perService.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		Feature feature = getFeaturesServiceInfo().getFeatures().getFeature();
		int update = featureConfigurationService.updateFeatureConfiguration(configurationContext, "GroupName", feature,
				insertedNodeId);
		Assert.assertNotEquals("Update Feature Config Should not be zero :: ", 0, update);
	}

	/**
	 * This method use for chenge status of featrueConfiguration.
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testChangeStatusOfFeatureConfig() throws FeatureConfigurationException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context data should not be null :: ", configurationContext);
		featureConfigurationService.changeStatusOfFeatureConfig(configurationContext, GenericTestConstant.TEST_FEATURE,
				false);
	}

	/**
	 * This method use for change status of featureService
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testChangeStatusOfFeatureService() throws FeatureConfigurationException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		featureConfigurationService.changeStatusOfFeatureService(configurationContext, GenericTestConstant.TEST_FEATURE,
				null);
	}

	/**
	 * This method use for Delete feature configuration.
	 * 
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testDeleteFeatureConfiguration() throws FeatureConfigurationException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		boolean isFlag = featureConfigurationService.deleteFeatureConfiguration(configurationContext,
				GenericTestConstant.TEST_FEATURE);
		Assert.assertTrue("Feature Config Should be deleted :: ", isFlag);
	}

	/**
	 * This method use for check feature exist in DB and cache.
	 * 
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigRequestException
	 */
	@Test
	public void testCheckFeatureExistInDBAndCache()
			throws FeatureConfigurationException, FeatureConfigRequestException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		boolean isFlag = featureConfigurationService.checkFeatureExistInDBAndCache(configurationContext,
				GenericTestConstant.TEST_FEATURE);
		Assert.assertTrue("Feature Config Should be find in DB :: ", isFlag);
	}

	/**
	 * This method use for reload to feature cache object.
	 * 
	 * @throws FeatureConfigRequestException
	 * @throws FeatureConfigurationException
	 */
	@Test
	public void testReloadFeatureCacheObject() throws FeatureConfigRequestException, FeatureConfigurationException {
		RequestContext requestContext = ContextData.getRequestContext();

		boolean isTrueFlag = featureConfigurationService.reloadFeatureCacheObject(requestContext,
				GenericTestConstant.TEST_FEATURE);
		Assert.assertTrue("Feature Config Should be find in DB :: ", isTrueFlag);
	}

	/**
	 * This method use for adding feature configuration data into DB.
	 * 
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 */
	private void testAddFeatureConfiguration() throws FeatureConfigurationException, FeatureConfigParserException {
		ConfigurationContext configContext = ContextData.getConfigContext();
		Feature feature = getFeaturesServiceInfo().getFeatures().getFeature();
		featureConfigurationService.addFeatureConfiguration(configContext, feature);
	}

	private ConfigNodeData getConfigNodeData() {
		ConfigNodeData nodeData = new ConfigNodeData();
		nodeData.setConfigName(GenericTestConstant.TEST_CONFIG_NAME);
		nodeData.setConfigLoadStatus("success");
		nodeData.setConfigData(GenericTestConstant.TEST_CONFIG_DATA);
		nodeData.setConfigType(GenericTestConstant.TEST_CONFIG_TYPE);
		nodeData.setEnabled(true);
		nodeData.setParentConfigNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		return nodeData;
	}

	/**
	 * Read feature service file from test resources folder.
	 * 
	 * @return
	 * @throws FeatureConfigParserException
	 */
	private FeaturesServiceInfo getFeaturesServiceInfo() throws FeatureConfigParserException {
		FeatureConfigXMLParser parser = new FeatureConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(FEATURE_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(FEATURE_FILE + "file doesnot exist in classpath", e);
		}
		String xmlString = out1.toString();
		FeaturesServiceInfo result = parser.marshallConfigXMLtoObject(xmlString);

		return result;
	}
}
