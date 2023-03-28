package com.attunedlabs.featuremaster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceTest;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;

public class FeatureMasterServiceTest {

	final Logger log = LoggerFactory.getLogger(FeatureDeploymentServiceTest.class);
	private IFeatureMasterService featureMasterService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws FeatureMasterServiceException
	 */
	@Before
	public void init() throws FeatureMasterServiceException {
		if (featureMasterService == null)
			featureMasterService = new FeatureMasterService();
		boolean isInserted = testInsertFeatureDetailsIntoFeatureMaster();
		Assert.assertTrue("Successfully Inserted feature Master Data In DB then should be true :: ", isInserted);
	}

	/**
	 * in this method test feature exist in feature master or not.
	 * 
	 * @throws FeatureMasterServiceException
	 */
	@Test
	public void testCheckFeatureExistInFeatureMasterOrNot() throws FeatureMasterServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		boolean isTrueFound = featureMasterService.checkFeatureExistInFeatureMasterOrNot(configurationContext);
		Assert.assertTrue("Feature master Data Found In DB ::", isTrueFound);

		boolean isDeleted = featureMasterService.deleteFeatureDetailsInFeatureMaster(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertTrue("Feature master Data deleted From DB ::", isDeleted);

		boolean isFalseFound = featureMasterService.checkFeatureExistInFeatureMasterOrNot(configurationContext);
		Assert.assertFalse("Feature master Data Not Found In DB ::", isFalseFound);

	}

	/**
	 * In this method test delete feature Detail in Feature master
	 * 
	 * @throws FeatureMasterServiceException
	 */
	@Test
	public void testDeleteFeatureDetailsInFeatureMaster() throws FeatureMasterServiceException {
		boolean isDeleted = featureMasterService.deleteFeatureDetailsInFeatureMaster(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertTrue("Feature master Data deleted From DB ::", isDeleted);
	}

	/**
	 * In this method insert feature master data into DB.
	 * 
	 * @return
	 * @throws FeatureMasterServiceException
	 */
	private boolean testInsertFeatureDetailsIntoFeatureMaster() throws FeatureMasterServiceException {
		return featureMasterService.insertFeatureDetailsIntoFeatureMaster(getFeatureMasterData());
	}

	/**
	 * feature master Data for inserting into DB.
	 * 
	 * @return
	 */
	private FeatureMaster getFeatureMasterData() {
		FeatureMaster featureMaster = new FeatureMaster();
		featureMaster.setFeature(GenericTestConstant.TEST_FEATURE);
		featureMaster.setFeatureGroup(GenericTestConstant.TEST_FEATUREGROUP);
		featureMaster.setSiteId(GenericTestConstant.TEST_SITE_NODEID);
		featureMaster.setVersion(GenericTestConstant.TEST_VERSION);
		featureMaster.setAllowMultipleImpl(true);
		featureMaster.setMultipleVendorSupport(true);
		featureMaster.setProduct("Junit Testing 1.0");
		featureMaster.setDescription("Testing purpose");
		return featureMaster;
	}
}
