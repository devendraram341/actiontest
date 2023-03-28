package com.attunedlabs.featuredeployment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featuremaster.FeatureMaster;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;
import com.attunedlabs.leap.LeapServiceContext;

public class FeatureDeploymentServiceTest {

	final Logger log = LoggerFactory.getLogger(FeatureDeploymentServiceTest.class);

	private IFeatureDeployment featureDeployment;
	private IFeatureMasterService featureMasterService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws FeatureMasterServiceException
	 */
	@Before
	public void init() throws FeatureMasterServiceException {
		if (featureDeployment == null)
			featureDeployment = new FeatureDeploymentService();
		if (featureMasterService == null)
			featureMasterService = new FeatureMasterService();
		featureMasterService.insertFeatureDetailsIntoFeatureMaster(getFeatureMasterData());
	}

	

	/**
	 * this method used for adding feature deployment data into DB.
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testAddFeatureDeployement() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		featureDeployment.addFeatureDeployement(configurationContext, false, false, false);

		featureDeployment.CheckAndaddFeatureDeployementInCache(configurationContext, false, false, false);

		boolean isDeleted = testDeleteFeatureDeployed();
		Assert.assertTrue("Feature Deployment Data Should be deleted from DB ::", isDeleted);
	}

	/**
	 * this method used for featureDeployment Detail.
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testGetFeatureDeployedDeatils() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();

		featureDeployment.addFeatureDeployement(configurationContext, false, false, false);

		FeatureDeployment featureDeployData = featureDeployment.getFeatureDeployedDeatils(configurationContext);
		Assert.assertNotNull("Feature Deployment data should not be null ::", featureDeployData);
		Assert.assertEquals("In this test Case Feature Name Should be same as featureDeployment Featrue",
				GenericTestConstant.TEST_FEATURE, featureDeployData.getFeatureName());

		boolean isDeleted = testDeleteFeatureDeployed();
		Assert.assertTrue("Feature Deployment Data Should be deleted from DB ::", isDeleted);
	}

	/**
	 * this method used for get featuredeployment data from active status is true
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testGetActiveAndPrimaryFeatureDeployedFromCache() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		featureDeployment.addFeatureDeployement(configurationContext, true, true, true);

		FeatureDeployment featureDepData = featureDeployment.getActiveAndPrimaryFeatureDeployedFromCache(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATURE,
				getLeapServiceContext());
		Assert.assertNotNull("Feature Deployment Data Should not be null ::", featureDepData);
		Assert.assertEquals("In this test Case Feature Name Should be same as featureDeployment Featrue",
				GenericTestConstant.TEST_FEATURE, featureDepData.getFeatureName());
		boolean isDeleted = testDeleteFeatureDeployed();
		Assert.assertTrue("Feature Deployment Data Should be deleted from DB ::", isDeleted);
	}

	/**
	 * this method used for upadate featureDeployment Data.
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testUpdateFeatureDeployed() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		featureDeployment.addFeatureDeployement(configurationContext, true, true, true);

		boolean isUpdated = featureDeployment.updateFeatureDeployed(configurationContext, false, false);
		Assert.assertTrue("Feature Deployment Data Should be updated then Value Is True ::", isUpdated);

		boolean isDeleted = testDeleteFeatureDeployed();
		Assert.assertTrue("Feature Deployment Data Should be deleted from DB ::", isDeleted);
	}

	/**
	 * this method used for check feature deployed or not
	 * 
	 * @throws FeatureDeploymentServiceException
	 */
	@Test
	public void testCheckIfFeatureIsAlreadyDeployed() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		featureDeployment.addFeatureDeployement(configurationContext, true, true, true);

		boolean checkData = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		Assert.assertTrue("Feature Deployment Data Should have in DB then True ::", checkData);

		boolean isDeleted = testDeleteFeatureDeployed();
		Assert.assertTrue("Feature Deployment Data Should be deleted from DB ::", isDeleted);
	}

	/**
	 * this method use for delete feature master table from DB.
	 * 
	 * @throws FeatureMasterServiceException
	 */
	@After
	public void deleteFeatureMasterData() throws FeatureMasterServiceException {
		featureMasterService.deleteFeatureDetailsInFeatureMaster(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
	}

	private LeapServiceContext getLeapServiceContext() {
		LeapServiceContext leapServiceContext = new LeapServiceContext();
		leapServiceContext.setFeatureGroup(GenericTestConstant.TEST_FEATUREGROUP);
		leapServiceContext.setFeatureName(GenericTestConstant.TEST_FEATURE);
		leapServiceContext.setImplementationName(GenericTestConstant.TEST_IMPL);
		leapServiceContext.setRequestContext(ContextData.getRequestContext());
		leapServiceContext.setRequestUUID(GenericTestConstant.TEST_REQUEST_ID);
		leapServiceContext.setVendor(GenericTestConstant.TEST_VENDOR);
		leapServiceContext.setVersion(GenericTestConstant.TEST_VERSION);
		return leapServiceContext;
	}

	private boolean testDeleteFeatureDeployed() throws FeatureDeploymentServiceException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		return featureDeployment.deleteFeatureDeployed(configurationContext);
	}

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
