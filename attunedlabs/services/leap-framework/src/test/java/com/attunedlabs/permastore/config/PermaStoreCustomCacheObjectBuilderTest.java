package com.attunedlabs.permastore.config;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;
import com.attunedlabs.permastore.config.util.ListData;
import com.attunedlabs.permastore.config.util.MapData;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;
import com.attunedlabs.permastore.config.util.SetData;

public class PermaStoreCustomCacheObjectBuilderTest {

	private IPermaStoreCustomCacheObjectBuilder cacheObjectBuilderMap;
	private IPermaStoreCustomCacheObjectBuilder cacheObjectBuilderSet;
	private IPermaStoreCustomCacheObjectBuilder cacheObjectBuilderList;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void setUp() {
		if (cacheObjectBuilderMap == null)
			cacheObjectBuilderMap = new MapData();
		if (cacheObjectBuilderSet == null)
			cacheObjectBuilderSet = new SetData();
		if (cacheObjectBuilderList == null)
			cacheObjectBuilderList = new ListData();
	}

	/**
	 * thi method test for get custom mapType Serializable data.
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testCustomMap() throws PermaStoreConfigParserException {
		ConfigurationBuilder configBulder = PermastoreFileRead.getPermaStoreMap().getPermaStoreConfiguration().get(0)
				.getConfigurationBuilder();
		CustomBuilder customBuilderConfig = configBulder.getCustomBuilder();
		Serializable check = cacheObjectBuilderMap.loadDataForCache(customBuilderConfig);
		Assert.assertNotNull("Data should not be null ::", check);
		Assert.assertEquals("Map Data Should be same as check value ::", "{third=101, four=102}", check.toString());
	}

	/**
	 * this method test for get custom setType Serializable data.
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testCustomSet() throws PermaStoreConfigParserException {
		ConfigurationBuilder configBulder = PermastoreFileRead.getPermaStoreSet().getPermaStoreConfiguration().get(0)
				.getConfigurationBuilder();
		CustomBuilder customBuilderConfig = configBulder.getCustomBuilder();
		Serializable check = cacheObjectBuilderSet.loadDataForCache(customBuilderConfig);
		Assert.assertNotNull("Data should not be null ::", check);
		Assert.assertEquals("Map Data Should be same as check value ::", "[101, 102]", check.toString());
	}

	/**
	 * this method test for get custom ListType Serializable data.
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testCustomList() throws PermaStoreConfigParserException {
		ConfigurationBuilder configBulder = PermastoreFileRead.getPermaStoreList().getPermaStoreConfiguration().get(0)
				.getConfigurationBuilder();
		CustomBuilder customBuilderConfig = configBulder.getCustomBuilder();
		Serializable check = cacheObjectBuilderList.loadDataForCache(customBuilderConfig);
		Assert.assertNotNull("Data should not be null ::", check);
		Assert.assertEquals("Map Data Should be same as check value ::", "[101, 102]", check.toString());
	}
}
