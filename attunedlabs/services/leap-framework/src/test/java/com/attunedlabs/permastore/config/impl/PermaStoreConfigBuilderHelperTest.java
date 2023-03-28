package com.attunedlabs.permastore.config.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationBuilderException;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilder;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;

public class PermaStoreConfigBuilderHelperTest {

	private PermaStoreConfigBuilderHelper helper;
	private PermaStoreConfigurations permaStoreConfigurations;

	/**
	 * used for initialization
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Before
	public void setUp() {
		if (helper == null)
			helper = new PermaStoreConfigBuilderHelper();
	}

	/**
	 * success Handles the Building of Cacheable Object based on the configuration
	 * given in the xml config.
	 * 
	 * @throws PermaStoreConfigurationBuilderException
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testHandleConfigurationBuilder()
			throws PermaStoreConfigurationBuilderException, PermaStoreConfigParserException {
		permaStoreConfigurations = PermastoreFileRead.getPermaStoreConfigurations();
		ConfigurationBuilder configBuilder = permaStoreConfigurations.getPermaStoreConfiguration().get(0)
				.getConfigurationBuilder();
		Serializable data = helper.handleConfigurationBuilder(configBuilder);
		Assert.assertNotNull("Serializable Data should not be null ::", data);
		Assert.assertEquals("Simple jsonObject class should be same ::", JSONObject.class, data.getClass());
		Assert.assertEquals("{\"third\":101,\"four\":102}", data.toString());
	}

	/**
	 * Failed to Handles the Building of Cacheable Object based on the configuration
	 * given in the xml config.
	 * 
	 * @throws PermaStoreConfigurationBuilderException
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testHandleConfigurationBuilderFail()
			throws PermaStoreConfigurationBuilderException, PermaStoreConfigParserException {
		permaStoreConfigurations = PermastoreFileRead.getBadPermaStoreConfigurations();
		ConfigurationBuilder configBuilder = permaStoreConfigurations.getPermaStoreConfiguration().get(0)
				.getConfigurationBuilder();
		try {
			helper.handleConfigurationBuilder(configBuilder);
		} catch (Exception e) {
			Assert.assertEquals("Unsupported InLine-ConfigurationBuilder InLine-Type=CUSTOM", e.getMessage());
		}
	}
}
