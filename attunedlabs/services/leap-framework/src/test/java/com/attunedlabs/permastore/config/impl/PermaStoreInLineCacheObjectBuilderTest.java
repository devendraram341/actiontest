package com.attunedlabs.permastore.config.impl;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationBuilderException;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;

public class PermaStoreInLineCacheObjectBuilderTest {

	private PermaStoreInLineCacheObjectBuilder cacheObjectBuilder;
	private PermaStoreConfigurations permastoreConfigs;

	/**
	 * used for Object initialization
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Before
	public void setUp() throws PermaStoreConfigParserException {
		if (cacheObjectBuilder == null)
			cacheObjectBuilder = new PermaStoreInLineCacheObjectBuilder();
		if (permastoreConfigs == null)
			permastoreConfigs = PermastoreFileRead.getPermaStoreConfigurations();
	}

	/**
	 * 
	 * 
	 * Builds the Object to be cached from the InLine Configuration. It supports
	 * JSON string only as an inlineConfigurationbuilder
	 * 
	 * @throws PermaStoreConfigurationBuilderException
	 */
	@Test
	public void testLoadDataForCache() throws PermaStoreConfigurationBuilderException {
		Serializable data = cacheObjectBuilder.loadDataForCache(
				permastoreConfigs.getPermaStoreConfiguration().get(0).getConfigurationBuilder().getInlineBuilder());
		Assert.assertNotNull("Serializable data should not be null ::", data);
		Assert.assertFalse("Serializable data should not be empty ::", data.toString().isEmpty());
		Assert.assertEquals("{\"third\":101,\"four\":102}", data.toString());
	}
}
