package com.attunedlabs.datacontext.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
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
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;

public class DataContextConfigurationServiceTest {

	final Logger log = LoggerFactory.getLogger(DataContextConfigurationServiceTest.class);
	private IDataContextConfigurationService configurationService;
	private IConfigPersistenceService perService;
	private final String CONFIG_FILE_TO_PARSE = "FeatureDataContext/testFeatureDataContext.xml";

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws DataContextConfigurationException
	 * @throws DataContextParserException
	 * @throws ConfigPersistenceException 
	 */
	@Before
	public void beforeClass()
			throws DataContextParserException, DataContextConfigurationException, ConfigPersistenceException {
		if (configurationService == null)
			configurationService = new DataContextConfigurationService();
		if (perService == null)
			perService = new ConfigPersistenceServiceMySqlImpl();

		perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		
	}

	/**
	 * this method used for add data context.
	 * 
	 * @throws DataContextParserException
	 * @throws DataContextConfigurationException
	 * @throws ConfigPersistenceException 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testAddDataContext()
			throws DataContextParserException, DataContextConfigurationException, ConfigPersistenceException {
		ConfigurationContext confContext = ContextData.getConfigContext();
		Assert.assertNotNull("Configuration Context should not be null ::", confContext);
		configurationService.addDataContext(confContext, getDataContextConfiguration());

		String configName = getConfigname(GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		ConfigNodeData configNodeData = perService
				.getConfigNodeDatabyNameAndNodeId(GenericTestConstant.TEST_VENDOR_NODEID, configName, "DataContext");
		Assert.assertEquals("ConfigNodeData CoonfigName Should Be Same ::", configName, configNodeData.getConfigName());
	}

	/**
	 * this method used for get featureDataconfiguration data.
	 * 
	 * @throws DataContextConfigurationException
	 * @throws ConfigNodeDataConfigurationException 
	 * @throws DataContextParserException 
	 * @throws ConfigPersistenceException 
	 */
	@Test
	public void testGetDataContextConfiguration() throws DataContextConfigurationException, DataContextParserException, ConfigPersistenceException {
		testAddDataContext();
		String configName = getConfigname(GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("Config name should not be null", configName);

		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request context should not be null", requestContext);

		DataContextConfigurationUnit contextConfigUnit = configurationService
				.getDataContextConfiguration(requestContext);
		Assert.assertNotNull("context config unit data should not be null", contextConfigUnit);
		Assert.assertEquals("context config unit key should be same as config name :", configName,
				contextConfigUnit.getKey());
	}

	/**
	 * this method used for check data context exist or not
	 * 
	 * @throws DataContextConfigurationException
	 * @throws DataContextParserException
	 * @throws ConfigPersistenceException 
	 * @throws ConfigNodeDataConfigurationException 
	 */
	@Test
	public void testCheckDataContextConfigExistOrNot()
			throws DataContextConfigurationException, DataContextParserException, ConfigPersistenceException {
		testAddDataContext();
		String configName = getConfigname(GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("Config name should not be null", configName);

		ConfigurationContext configContext = ContextData.getConfigContext();
		Assert.assertNotNull("configuration context should not be null", configContext);

		boolean checkData = configurationService.checkDataContextConfigExistOrNot(configContext, configName);
		Assert.assertTrue("config name should be find in DB", checkData);
	}

	/**
	 * this method used for reload dataContxt in cache object.
	 * 
	 * @throws DataContextConfigurationException
	 * @throws ConfigNodeDataConfigurationException 
	 * @throws DataContextParserException 
	 * @throws ConfigPersistenceException 
	 */
	@Test
	public void testReloadDataContextCacheObject() throws DataContextConfigurationException, DataContextParserException, ConfigPersistenceException {
		testAddDataContext();
		String configName = getConfigname(GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("Config name should not be null", configName);

		RequestContext requestContext = ContextData.getRequestContext();
		Assert.assertNotNull("request context should not be null", requestContext);

		boolean reloadData = configurationService.reloadDataContextCacheObject(requestContext, configName);
		Assert.assertTrue("successfully data reload and should be true", reloadData);

	}

	/**
	 * this method used for delete datacontext configuration after call each method.
	 * 
	 * @throws DataContextConfigurationException
	 */
	@After
	@Test
	public void testDeleteDataContextConfiguration() throws DataContextConfigurationException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		boolean isDeleted = configurationService.deleteDataContextConfiguration(configurationContext);
		Assert.assertTrue("should be deleted data context configurartion :: ", isDeleted);
	}

	/**
	 * this method used for get FeaturedataContext configuration data.
	 * 
	 * @return
	 * @throws DataContextParserException
	 */
	private FeatureDataContext getDataContextConfiguration() throws DataContextParserException {
		DataContextConfigXMLParser parser = new DataContextConfigXMLParser();
		InputStream inputstream = DataContextConfigurationServiceTest.class.getClassLoader()
				.getResourceAsStream(CONFIG_FILE_TO_PARSE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			throw new DataContextParserException("datacontext file doesnot exist in classpath", e);
		}

		String featureConfigxml = out1.toString();
		FeatureDataContext featureDataContext = parser.marshallConfigXMLtoObject(featureConfigxml);
		return featureDataContext;
	}

	/**
	 * this method used for arrage FeatureDataContext configName.
	 * 
	 * @param featureGroup
	 * @param featureName
	 * @return
	 */
	private String getConfigname(String featureGroup, String featureName) {
		String configName = featureGroup + "-" + featureName + "-" + DataContextConstant.DATACONTEXT_SUFFIX_KEY;
		return configName;
	}
}
