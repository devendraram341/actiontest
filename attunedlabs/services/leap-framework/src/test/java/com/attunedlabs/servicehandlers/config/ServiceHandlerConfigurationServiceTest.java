package com.attunedlabs.servicehandlers.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.attunedlabs.servicehandlers.config.impl.ServiceHandlerConfigurationHelper.FeatureServiceHanldersFQCN;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.servicehandlers.config.impl.ServiceHandlerConfigurationService;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;
import com.attunedlabs.servicehandlers.util.ServiceFileRead;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ServiceHandlerConfigurationServiceTest {

	final Logger log = LoggerFactory.getLogger(ServiceHandlerConfigurationServiceTest.class);
	private IServiceHandlerConfigurationService service;
	private IConfigPersistenceService configPersistenceService;

	/**
	 * for Initialization
	 */
	@Before
	public void setUp() {
		if (service == null)
			service = new ServiceHandlerConfigurationService();
		if (configPersistenceService == null)
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
	}

	/**
	 * test for adding application level common service handlers.
	 * 
	 * @throws ServiceHandlerConfigurationException
	 */
	@Test
	public void testAddApplicationCommonServiceHandlersConfiguration() throws ServiceHandlerConfigurationException {
		try {
			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("Configuration Context data Should not be null ::", configurationContext);

			ApplicationServiceHandlerConfiguration applicationHandler = ServiceFileRead.getApplicationServiceHandler();
			Assert.assertNotNull("ApplicationServiceHandler should not be null ::", applicationHandler);
			Assert.assertTrue(!applicationHandler.getCommonServiceHandler().toString().isEmpty());

			CommonServiceHandler commonService = applicationHandler.getCommonServiceHandler();
			Assert.assertNotNull("CommonServiceHandler should not be null ::", commonService);

			service.addApplicationCommonServiceHandlersConfiguration(commonService, configurationContext);

			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "ACSH", "AppHandlers");
			Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
			Assert.assertEquals("ApplicationCommonServiceHandler Id [ACSH] should be match as configNodeData Name ",
					"ACSH", loadedConfigNodeData.getConfigName());
		} catch (BeanDependencyResolveException | ServiceHandlerConfigurationException
				| ServiceHandlerConfigParserException | ConfigPersistenceException e) {
			log.error("Exception occure :: " + e.getMessage(), e);
			throw new ServiceHandlerConfigurationException("Exception occure :: " + e.getMessage(), e);
		}
	}

	/**
	 * test for adding the application level feature service handlers .
	 * 
	 * @throws ServiceHandlerConfigurationException
	 */
	@Test
	public void testAddApplicationFeatureServiceHandlersConfiguration() throws ServiceHandlerConfigurationException {
		try {
			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("Configuration Context data Should not be null ::", configurationContext);

			ApplicationServiceHandlerConfiguration applicationHandler = ServiceFileRead.getApplicationServiceHandler();
			Assert.assertNotNull("ApplicationServiceHandler should not be null ::", applicationHandler);
			Assert.assertTrue(!applicationHandler.getCommonServiceHandler().toString().isEmpty());

			FeatureServiceHandler featureService = applicationHandler.getFeatureServiceHandler();
			Assert.assertNotNull("FeatureServiceHandler should not be null ::", featureService);

			service.addApplicationFeatureServiceHandlersConfiguration(featureService, configurationContext);

			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "AFSH", "AppHandlers");
			Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
			Assert.assertEquals("ApplicationFeatureServiceHandler Id [AFSH] should be match as configNodeData Name ",
					"AFSH", loadedConfigNodeData.getConfigName());
		} catch (BeanDependencyResolveException | ServiceHandlerConfigurationException
				| ServiceHandlerConfigParserException | ConfigPersistenceException e) {
			log.error("Exception occure :: " + e.getMessage(), e);
			throw new ServiceHandlerConfigurationException("Exception occure :: " + e.getMessage(), e);
		}

	}

	/**
	 * test for adding the feature service handlers
	 * 
	 * @throws ServiceHandlerConfigParserException
	 */
	@Test
	public void testAddFeatureServiceHandlerConfiguration() throws ServiceHandlerConfigurationException {
		try {
			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("Configuration Context data Should not be null ::", configurationContext);

			ServiceHandlerConfiguration serviceHandler = ServiceFileRead.getServiceHandlerFile();
			Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", serviceHandler);
			Assert.assertTrue(!serviceHandler.getFeatureServiceHandler().toString().isEmpty());

			com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler featureService = serviceHandler
					.getFeatureServiceHandler();
			Assert.assertNotNull("FeatureServiceHandler should not be null ::", featureService);

			service.addFeatureServiceHandlerConfiguration(configurationContext, featureService);

			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					GenericTestConstant.TEST_VENDOR_NODEID, "FFSH", "FeatureHandlers");
			Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
			Assert.assertEquals("FeatureServiceHandler Id [FFSH] should be match as configNodeData Name ", "FFSH",
					loadedConfigNodeData.getConfigName());
		} catch (ServiceHandlerConfigParserException | BeanDependencyResolveException
				| ServiceHandlerConfigurationException | ConfigPersistenceException e) {
			log.error("Exception occure :: " + e.getMessage(), e);
			throw new ServiceHandlerConfigurationException("Exception occure :: " + e.getMessage(), e);
		}

	}

	/**
	 * test for Maintaining the handlers to be invoked for services in
	 * feature-service.
	 * 
	 * @throws ServiceHandlerConfigurationException
	 */
	@Test
	public void testCacheHandlersForEachService() throws ServiceHandlerConfigurationException {
		try {
			testAddApplicationCommonServiceHandlersConfiguration();
			testAddApplicationFeatureServiceHandlersConfiguration();
			testAddFeatureServiceHandlerConfiguration();

			ConfigurationContext configurationContext = ContextData.getConfigContext();
			Assert.assertNotNull("Configuration Context data Should not be null ::", configurationContext);

			FeaturesServiceInfo featureServiceInfo = ServiceFileRead.getFeaturesServiceInfo();
			Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", featureServiceInfo);

			service.cacheHandlersForEachService(configurationContext, featureServiceInfo);

			for (Service services : featureServiceInfo.getFeatures().getFeature().getService()) {
				List<String> applicationLevelHandlersForService = service.getApplicationLevelHandlersForService(
						GenericTestConstant.TEST_VENDOR_NODEID, services.getName());
				applicationLevelHandlersForService.addAll(service
						.getFeatureLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, services.getName()));
				if (services.getName().equals("testService1")) {
					Assert.assertFalse(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler1"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler2"));
					Assert.assertFalse(applicationLevelHandlersForService.contains("AFSH-testAppFeatureHandler1"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("FFSH-testFeatureHandler1"));
				}
				if (services.getName().equals("testService2")) {
					Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler1"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler2"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("AFSH-testAppFeatureHandler1"));
					Assert.assertFalse(applicationLevelHandlersForService.contains("FFSH-testFeatureHandler1"));
				}
				if (services.getName().equals("testService3")) {
					Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler1"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler2"));
					Assert.assertFalse(applicationLevelHandlersForService.contains("AFSH-testAppFeatureHandler1"));
					Assert.assertTrue(applicationLevelHandlersForService.contains("FFSH-testFeatureHandler1"));
				}
			}
		} catch (ServiceHandlerConfigurationException | FeatureConfigParserException e) {
			log.error("Exception occure :: " + e.getMessage(), e);
			throw new ServiceHandlerConfigurationException("Exception occure :: " + e.getMessage(), e);
		}

	}

	/**
	 * Test for get all Application level handler service
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * 
	 */
	@Test
	public void testGetApplicationLevelHandlersForService()
			throws ServiceHandlerConfigurationException {
		testCacheHandlersForEachService();
		List<String> listOfAppHandler = service
				.getApplicationLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, "TestService2");
		Assert.assertNotNull("ApplicationServiceHandler List Should not be null :", listOfAppHandler);
		Assert.assertTrue("ApplicationServiceHandler list should not be zero ::", listOfAppHandler.size() > 0);
	}


	/**
	 * Test for get all feature level handler service 
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * 
	 */
	@Test
	public void testGetFeatureLevelHandlersForService() throws ServiceHandlerConfigurationException {
		testCacheHandlersForEachService();
		List<String> listOfServiceHandler = service
				.getFeatureLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, "TestService1");
		Assert.assertNotNull("ServiceHandler List Should not be null :", listOfServiceHandler);
		Assert.assertTrue("ServiceHandler list should not be zero ::", listOfServiceHandler.size() > 0);
	}

	/**
	 * test for get configNodeId.
	 * 
	 * @throws ServiceHandlerConfigurationException
	 */
	@Test
	public void testGetConfigNodeId() throws ServiceHandlerConfigurationException {
		Integer configNodeId = service.getConfigNodeId(GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VERSION,
				GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotEquals("ConfigNodeid Should not be zero ::", 0, (int) configNodeId);
		Assert.assertEquals("Vendor Node id Should be same as configNodeId ::", GenericTestConstant.TEST_VENDOR_NODEID,
				configNodeId);
	}

	/**
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigParserException 
	 */
	@Test
	public void testUpdateFSHandlersConfiguration()
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException, ServiceHandlerConfigParserException {
		testCacheHandlersForEachService();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request Context should not be null ::", requestContext);
		
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, String> hanldersStorefqcn = hazelcastInstance.getMap(FeatureServiceHanldersFQCN);
		String preConfig = hanldersStorefqcn.get("testFeatureHandler1");
		Assert.assertNotNull("Pre configuration Not be null ::",preConfig);
		Assert.assertTrue(preConfig.contains("*"));
		Assert.assertTrue(preConfig.contains("sync"));
		Assert.assertTrue(preConfig.contains("com.attunedlabs.servicehandlers.util.LoggingHandler"));
	
		service.updateFSHandlersConfiguration(requestContext, "testFeatureHandler1", "async", "post-service", "com.attunedlabs.servicehandlers.util.LoggingHandlerForUpdate", null, null);
		
		String postConfig = hanldersStorefqcn.get("testFeatureHandler1");
		Assert.assertNotNull("Post configuration Not be null ::",postConfig);
		Assert.assertTrue(postConfig.contains("post-service"));
		Assert.assertTrue(postConfig.contains("com.attunedlabs.servicehandlers.util.LoggingHandlerForUpdate"));
		Assert.assertTrue(postConfig.contains("async"));
		
	}

	/**
	 * Test for Adding new feature service
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testincludeNewService()
			throws ServiceHandlerConfigurationException, ConfigPersistenceException, FeatureConfigParserException {
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request Context should not be null ::", requestContext);
		testCacheHandlersForEachService();
		service.includeNewService("testFeatureHandler1", requestContext, "TestService2");

		FeaturesServiceInfo featureServiceInfo = ServiceFileRead.getFeaturesServiceInfo();
		Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", featureServiceInfo);

		for (Service services : featureServiceInfo.getFeatures().getFeature().getService()) {
			List<String> applicationLevelHandlersForService = service
					.getApplicationLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, services.getName());
			applicationLevelHandlersForService.addAll(service
					.getFeatureLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, services.getName()));
			if (services.getName().equals("TestService2")) {
				Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler1"));
				Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler2"));
				Assert.assertTrue(applicationLevelHandlersForService.contains("AFSH-testAppFeatureHandler1"));
				Assert.assertTrue(applicationLevelHandlersForService.contains("FFSH-testFeatureHandler1"));
			}
		}
	}

	/**
	 * Test for remove service level handler
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testRemoveServicesForHandler()
			throws ServiceHandlerConfigurationException, ConfigPersistenceException, FeatureConfigParserException {
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request Context should not be null ::", requestContext);
		testCacheHandlersForEachService();
		service.removeServicesForHandler("testFeatureHandler1", requestContext, "TestService3");

		FeaturesServiceInfo featureServiceInfo = ServiceFileRead.getFeaturesServiceInfo();
		Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", featureServiceInfo);

		for (Service services : featureServiceInfo.getFeatures().getFeature().getService()) {
			List<String> applicationLevelHandlersForService = service
					.getApplicationLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, services.getName());
			applicationLevelHandlersForService.addAll(service
					.getFeatureLevelHandlersForService(GenericTestConstant.TEST_VENDOR_NODEID, services.getName()));
			log.debug("applicationLevelHandlersForServiceapplicationLevelHandlersForService "
					+ applicationLevelHandlersForService);
			if (services.getName().equals("testService3")) {
				Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler1"));
				Assert.assertTrue(applicationLevelHandlersForService.contains("ACSH-testAppCommonHandler2"));
				Assert.assertFalse(applicationLevelHandlersForService.contains("AFSH-testAppFeatureHandler1"));
				Assert.assertFalse(applicationLevelHandlersForService.contains("FFSH-testFeatureHandler1"));
			}
		}
	}

	/**
	 * test for Disable all feature level handler
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testDisableAllFeatureLevelHandlers()
			throws ServiceHandlerConfigurationException, ConfigPersistenceException {
		testAddFeatureServiceHandlerConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request Context should not be null ::", requestContext);
		service.disableAllFeatureLevelHandlers(requestContext);

		ConfigNodeData loadedConfigNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "FFSH", "FeatureHandlers");
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertFalse("FeatureServiceHandler Should be false ::", loadedConfigNodeData.isEnabled());
	}

	/**
	 * test for enable all feature level handler
	 * 
	 * @throws ServiceHandlerConfigurationException
	 * @throws BeanDependencyResolveException
	 * @throws FeatureConfigParserException
	 * @throws ServiceHandlerConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testEnableAllFeatureLevelHandlers()
			throws ServiceHandlerConfigurationException, FeatureConfigParserException,
			ServiceHandlerConfigParserException, BeanDependencyResolveException, ConfigPersistenceException {
		testAddFeatureServiceHandlerConfiguration();

		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request Context should not be null ::", requestContext);

		FeaturesServiceInfo featureServiceInfo = ServiceFileRead.getFeaturesServiceInfo();
		Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", featureServiceInfo);

		ServiceHandlerConfiguration serviceHandler = ServiceFileRead.getServiceHandlerFile();
		Assert.assertNotNull("ServiceHandlerConfiguration should not be null ::", serviceHandler);
		Assert.assertTrue(!serviceHandler.getFeatureServiceHandler().toString().isEmpty());

		com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler featureService = serviceHandler
				.getFeatureServiceHandler();
		Assert.assertNotNull("FeatureServiceHandler should not be null ::", featureService);

		service.enableAllFeatureLevelHandlers(requestContext, featureServiceInfo, featureService);

		ConfigNodeData loadedConfigNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "FFSH", "FeatureHandlers");
		Assert.assertNotNull("configNodeData Data should notg be null ::", loadedConfigNodeData);
		Assert.assertTrue("FeatureServiceHandler Should be true ::", loadedConfigNodeData.isEnabled());
	}
}
