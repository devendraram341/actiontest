package com.attunedlabs.permastore.config;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;

public class PermaStoreConfigurationServiceTest {

	private IPermaStoreConfigurationService permastoreConfigService;
	private IConfigPersistenceService configPersistenceService;

	/**
	 * used for Initialization.
	 */
	@Before
	public void setUp() {
		if (permastoreConfigService == null)
			permastoreConfigService = new PermaStoreConfigurationService();
		if (configPersistenceService == null)
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
	}

	/**
	 * test for insert permastore config in ConfigNodeData table.
	 * 
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testAddPermaStoreConfiguration()
			throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context should not be null ::", configurationContext);

		PermaStoreConfiguration permaStoreConfiguration = PermastoreFileRead.getPermaStoreConfigurations().getPermaStoreConfiguration().get(0);
		Assert.assertNotNull("permastore configuration Should not be null ::", permaStoreConfiguration);

		permastoreConfigService.addPermaStoreConfiguration(configurationContext, permaStoreConfiguration);

		ConfigNodeData loadedConfigNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "TestName", "PermaStore");
		Assert.assertNotNull("configNodeData Data should not be null ::", loadedConfigNodeData);
		Assert.assertEquals("ConfigNodeData Name Should be same as 'TestName' :: ", "TestName",
				loadedConfigNodeData.getConfigName());
	}

	/**
	 * test for fetch the permastore configuration from configNodeData table.
	 * 
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetPermaStoreConfiguration() throws PermaStoreConfigRequestException,
			PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);
		PermaStoreConfigurationUnit perConfigUnit = permastoreConfigService.getPermaStoreConfiguration(requestContext,
				"TestName");
		Assert.assertNotNull("PermaStore Config Unit Should Not be null ::", perConfigUnit);
		Assert.assertEquals("Site Id Should be same as permastore site Id ::", GenericTestConstant.TEST_SITE,
				perConfigUnit.getSiteId());
	}

	/**
	 * test for get serializable data from inline permastore configuration.
	 * 
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetPermaStoreCachedObjectWithInline() throws PermaStoreConfigRequestException,
			PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);
		Serializable data = permastoreConfigService.getPermaStoreCachedObject(requestContext, "TestName");
		Assert.assertNotNull("Serializable Data should not be null ::", data);
		Assert.assertEquals("Simple jsonObject class should be same ::", JSONObject.class, data.getClass());
		Assert.assertEquals("{\"third\":101,\"four\":102}", data.toString());
	}

	/**
	 * if false status of permastore configuration
	 * 
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfPermaStoreConfigIfFalse()
			throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context should not be null ::", configurationContext);

		permastoreConfigService.changeStatusOfPermaStoreConfig(configurationContext, "TestName", false);

		ConfigNodeData configNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "TestName", "PermaStore");
		Assert.assertFalse("ConfigNode Data isEnabled Should Be False", configNodeData.isEnabled());
	}

	/**
	 * if true status of permastore configuration
	 * 
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testChangeStatusOfPermaStoreConfigIfTrue()
			throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context should not be null ::", configurationContext);

		permastoreConfigService.changeStatusOfPermaStoreConfig(configurationContext, "TestName", true);

		ConfigNodeData configNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "TestName", "PermaStore");
		Assert.assertTrue("ConfigNode Data isEnabled Should Be true", configNodeData.isEnabled());
	}

	/**
	 * verify permastore configuartion load with valid permastoreConfig Name.
	 * 
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testVerifyPermaStoreConfigLoadedIfValid() throws PermaStoreConfigRequestException,
			PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);

		permastoreConfigService.verifyPermaStoreConfigLoaded(requestContext, "TestName");

		ConfigNodeData configNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "TestName", "PermaStore");
		Assert.assertTrue("permastore Configuration loaded with testname Config ::",
				!configNodeData.toString().isEmpty());
	}

	/**
	 * verify permastore configuartion load with invalid permastoreConfig Name.
	 * 
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testVerifyPermaStoreConfigLoadedIfInvalid() throws PermaStoreConfigRequestException,
			PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);

		permastoreConfigService.verifyPermaStoreConfigLoaded(requestContext, "check");

		ConfigNodeData configNodeData = configPersistenceService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, "check", "PermaStore");
		Assert.assertTrue("permastore Configuration loaded with other configuration", configNodeData == null);
	}

	/**
	 * reload permatore from cache object if exist.
	 * 
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadPerStoreCacheObjectIfExist()
			throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);
		boolean isReload = permastoreConfigService.reloadPerStoreCacheObject(requestContext, "TestName");
		Assert.assertTrue("Relaod Value Should be True ::", isReload);
	}

	/**
	 * reload permatore from cache object if Not exist.
	 * 
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testReloadPerStoreCacheObjectIfNotExist()
			throws PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		testAddPermaStoreConfiguration();
		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("Request Context should not be null ::", requestContext);
		boolean isReload = permastoreConfigService.reloadPerStoreCacheObject(requestContext, "check");
		Assert.assertFalse("Relaod Value Should be True ::", isReload);
	}

	/**
	 * test for checking permastore config exist or not.
	 * 
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 * @throws PermaStoreConfigParserException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testCheckPermaStoreConfigarationExistOrNot() throws PermaStoreConfigRequestException,
			PermaStoreConfigurationException, PermaStoreConfigParserException, ConfigPersistenceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context should not be null ::", configurationContext);

		boolean isCheckFalseData = permastoreConfigService.checkPermaStoreConfigarationExistOrNot(configurationContext,
				"TestName");
		Assert.assertFalse("at a time permastore Configuration then Should be False ::", isCheckFalseData);

		testAddPermaStoreConfiguration();
		boolean isCheckTrueData = permastoreConfigService.checkPermaStoreConfigarationExistOrNot(configurationContext,
				"TestName");
		Assert.assertTrue("at a time permastore Configuration then Should be False ::", isCheckTrueData);
	}

	/**
	 * test for delete permastore configuration after every method call.
	 * 
	 * @throws PermaStoreConfigurationException
	 */
	@After
	@Test
	public void testDeletePermaStoreConfiguration() throws PermaStoreConfigurationException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration Context should not be null ::", configurationContext);

		boolean isDeleted = permastoreConfigService.deletePermaStoreConfiguration(configurationContext, "TestName");
		Assert.assertTrue("Permastore config Should be deleted from DB ::", isDeleted);
	}
}
