package com.attunedlabs.config.persistence.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.persistence.exception.FeatureDeploymentConfigurationException;
import com.attunedlabs.config.persistence.exception.FeatureMasterConfigurationException;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuremaster.FeatureMaster;

public class FeatureDeploymentDAOTest {

	private FeatureDeploymentDAO deploymentDAO;
	private ConfigFeatureMasterDAO featureMasterDAO;
	private int featureMasterid;

	/**
	 * In this method used for Initialization.
	 * @throws FeatureMasterConfigurationException 
	 */
	@Before
	public void initialization() throws FeatureMasterConfigurationException {
		if(deploymentDAO==null)
		deploymentDAO = new FeatureDeploymentDAO();
		if(featureMasterDAO==null)
		featureMasterDAO = new ConfigFeatureMasterDAO();
		featureMasterDAO.insertFeatureMasterDetails(getFeatureMasterData());
		featureMasterid = getFeatureMasterId();
	}



	/**
	 * this method used for insert featureDeployment data into DB.
	 * 
	 * @throws FeatureMasterConfigurationException
	 * @throws FeatureDeploymentConfigurationException
	 */
	@Test
	public void testInsertFeatureDeploymentDetails()
			throws FeatureMasterConfigurationException, FeatureDeploymentConfigurationException {

		FeatureDeployment featureDeplo = deploymentDAO
				.insertFeatureDeploymentDetails(getFeatureDeploymentData(featureMasterid));
		Assert.assertNotNull(featureDeplo);
		Assert.assertEquals(GenericTestConstant.TEST_FEATURE, featureDeplo.getFeatureName());

		boolean deleteStatus = deploymentDAO.deleteFeatureDeployment(featureMasterid, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertTrue(deleteStatus);
	}

	/**
	 * this method used for get FeatureDeployment Data using featureu and impl name.
	 * 
	 * @throws FeatureDeploymentConfigurationException
	 */
	@Test
	public void testGetFeatureDeploymentByFeatureAndImplName() throws FeatureDeploymentConfigurationException {
		FeatureDeployment featureDeplo = deploymentDAO
				.insertFeatureDeploymentDetails(getFeatureDeploymentData(featureMasterid));
		Assert.assertNotNull(featureDeplo);
		Assert.assertEquals(GenericTestConstant.TEST_FEATURE, featureDeplo.getFeatureName());

		FeatureDeployment getFeatureDeplyData = deploymentDAO.getFeatureDeploymentByFeatureAndImplName(featureMasterid,
				GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull(getFeatureDeplyData);
		Assert.assertEquals(GenericTestConstant.TEST_FEATURE, getFeatureDeplyData.getFeatureName());

		boolean isDelete = testDeleteFeatureDeployment();
		Assert.assertTrue(isDelete);
	}

	/**
	 * This method call after every method.
	 * 
	 * @throws FeatureMasterConfigurationException
	 * @throws ConfigNodeDataConfigurationException
	 * @throws ConfigNodeConfigurationException
	 */
	@After
	public void AfterDeleteMasterData() throws FeatureMasterConfigurationException,
			ConfigNodeDataConfigurationException, ConfigNodeConfigurationException {
		boolean deleteStatus = featureMasterDAO.deleteFeatureMasterDetails(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertTrue(deleteStatus);

	}

	/**
	 * this method used for delete featrueFeployment Data from DB.
	 * 
	 * @return
	 * @throws FeatureDeploymentConfigurationException
	 */
	private boolean testDeleteFeatureDeployment() throws FeatureDeploymentConfigurationException {
		boolean deleteStatus = deploymentDAO.deleteFeatureDeployment(featureMasterid, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);

		return deleteStatus;
	}

	/**
	 * this method used for get feature master id from DB.
	 * 
	 * @return
	 * @throws FeatureMasterConfigurationException
	 */
	private int getFeatureMasterId() throws FeatureMasterConfigurationException {
		int featureMasterId = featureMasterDAO.getFeatureMasterIdByFeatureAndFeaturegroup(
				GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_VERSION, GenericTestConstant.TEST_SITE_NODEID);
		return featureMasterId;
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

	private FeatureDeployment getFeatureDeploymentData(int masterNodeId) {
		FeatureDeployment featureDeployment = new FeatureDeployment();
		featureDeployment.setFeatureMasterId(masterNodeId);
		featureDeployment.setActive(true);
		featureDeployment.setFeatureName(GenericTestConstant.TEST_FEATURE);
		featureDeployment.setFeatureVersion(GenericTestConstant.TEST_VERSION);
		featureDeployment.setImplementationName(GenericTestConstant.TEST_IMPL);
		featureDeployment.setVendorName(GenericTestConstant.TEST_VENDOR);
		featureDeployment.setPrimary(true);
		featureDeployment.setProvider("TEST PROVIDER");
		featureDeployment.setVendorTaxonomyId("Test taxonomy");
		return featureDeployment;
	}
}
