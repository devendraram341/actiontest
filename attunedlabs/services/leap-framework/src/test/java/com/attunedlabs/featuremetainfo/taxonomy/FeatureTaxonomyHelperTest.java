package com.attunedlabs.featuremetainfo.taxonomy;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;

public class FeatureTaxonomyHelperTest {

	private FeatureTaxonomyHelper taxonomyHelper = new FeatureTaxonomyHelper();
	final Logger logger = LoggerFactory.getLogger(FeatureTaxonomyHelperTest.class);
	private final String featureMetaInfoFileName = "FeatureMetaInfo/featureMetaInfo.xml";

	/**
	 * this method use for Adding taxonomy Name
	 * 
	 * @throws TaxonomyConfigException
	 */
	@Test
	public void testAddTaxonomyName() throws TaxonomyConfigException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		taxonomyHelper.addTaxonomyName(configurationContext, featureMetaInfoFileName);
	}

	/**
	 * this method used for initialize configNodeid.
	 * @throws TaxonomyConfigException
	 */
	@Test
	public void testInitializeConfigNodeId() throws TaxonomyConfigException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		Integer nodeId = taxonomyHelper.initializeConfigNodeId(configurationContext);
		Assert.assertNotEquals("NodeId Should not be 0 ::", 0, (int) nodeId);
		Assert.assertEquals("Node Id Should be Same as Vendor Node id ::", GenericTestConstant.TEST_VENDOR_NODEID,
				nodeId);
	}

	/**
	 * this method use for get taxonomy id:
	 */
	@Test
	public void testGetTaxonomy() {
		String actualName = taxonomyHelper.getTaxonomy("testing");
		Assert.assertNotNull("Actual Name Should not be null ::", actualName);
		Assert.assertEquals("Actual Name Should be Same As testing in uppercase :: ", "TESTING", actualName);
	}
}
