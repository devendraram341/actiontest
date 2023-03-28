package com.attunedlabs.ddlutils.config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.ContextData;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.ddlutils.config.impl.DbConfigurationServiceImpl;

public class DbConfigurationServiceTest {

	private IDbConfigurationService configurationService;
	private ConfigurationContext configurationContext;

	@Before
	public void setUp() {
		if (configurationService == null) {
			configurationService = new DbConfigurationServiceImpl();
		}
		if (configurationContext == null) {
			configurationContext = ContextData.getConfigContext();
		}

		try {
			configurationService.addDbConfiguration(configurationContext, null);
		} catch (InvalidDbConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetDbConfiguration() throws DbConfigNotfoundException {
		RequestContext context = ContextData.getRequestContext();
		String dbConfigData = configurationService.getDbConfiguration(context, null);
		Assert.assertNull("Should be null beacuse no operation done ::", dbConfigData);
	}

	@Test
	public void testUpdateDbConfiguration() throws DbConfigNotfoundException {
		String updateDbConfigData = configurationService.updateDbConfiguration(configurationContext, 0, null);
		Assert.assertNull("Should be null beacuse no operation done ::", updateDbConfigData);
	}

	@After
	@Test
	public void testDeleteDbConfiguration() throws DbConfigNotfoundException {
		String deletedDbConfigData = configurationService.deleteDbConfiguration(configurationContext, null);
		Assert.assertNull("Should be null beacuse no operation done ::", deletedDbConfigData);
	}

}
