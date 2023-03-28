package com.attunedlabs.config.persistence.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.exception.FeatureMasterConfigurationException;
import com.attunedlabs.featuremaster.FeatureMaster;

public class ConfigFeatureMasterDAOTest {

	private ConfigFeatureMasterDAO featureMasterDAO;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void initializeTheDataGrid() {
		if (featureMasterDAO == null)
			featureMasterDAO = new ConfigFeatureMasterDAO();
	}

	/**
	 * this test method used for insert feature master data into DB.
	 * 
	 * @throws FeatureMasterConfigurationException
	 */
	@Test
	public void testInsertFeatureMasterDetails() throws FeatureMasterConfigurationException {
		FeatureMaster featureMaster = getFeatureMasterData();
		Assert.assertNotNull(featureMaster);

		boolean insertDataStatus = featureMasterDAO.insertFeatureMasterDetails(featureMaster);
		Assert.assertTrue(insertDataStatus);

	}

	/**
	 * this test method used for failed to insert feature master data into DB.
	 * 
	 * @throws FeatureMasterConfigurationException
	 */
	@Test(expected = FeatureMasterConfigurationException.class)
	public void testInsertFeatureMasterDetailsFail() throws FeatureMasterConfigurationException {
		featureMasterDAO.insertFeatureMasterDetails(null);
	}

	/**
	 * this test method used for get Feature Master Data using feature and feature
	 * group
	 * 
	 * @throws FeatureMasterConfigurationException
	 */
	@Test
	public void testGetFeatureMasterIdByFeatureAndFeaturegroup() throws FeatureMasterConfigurationException {

		FeatureMaster featureMaster = getFeatureMasterData();
		Assert.assertNotNull(featureMaster);

		boolean insertDataStatus = featureMasterDAO.insertFeatureMasterDetails(featureMaster);
		Assert.assertTrue(insertDataStatus);

		int getData = featureMasterDAO.getFeatureMasterIdByFeatureAndFeaturegroup(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_VERSION,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertNotEquals(0, getData);
	}

	/**
	 * this test method used for failed to get Feature Master Data using feature and
	 * feature
	 * 
	 * @throws FeatureMasterConfigurationException
	 */
	@Test(expected = FeatureMasterConfigurationException.class)
	public void testGetFeatureMasterIdByFeatureAndFeaturegroupFail() throws FeatureMasterConfigurationException {
		featureMasterDAO.getFeatureMasterIdByFeatureAndFeaturegroup(null, "xyz", "xyz", 0);
	}

	/**
	 * this method used for delete feature master data from DB.
	 * 
	 * @return
	 * @throws FeatureMasterConfigurationException
	 */
	@After
	@Test
	public void testDeleteFeatureMasterDetails() throws FeatureMasterConfigurationException {
		featureMasterDAO.deleteFeatureMasterDetails(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
	}

	/**
	 * this method use for creating data for feature master data.
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
