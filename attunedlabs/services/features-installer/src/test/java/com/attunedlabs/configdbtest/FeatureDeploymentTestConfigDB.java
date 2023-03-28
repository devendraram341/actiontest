package com.attunedlabs.configdbtest;

import static com.attunedlabs.FeatureInstallerTestConstant.*;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featuremaster.FeatureMaster;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;

public class FeatureDeploymentTestConfigDB {

	private IFeatureDeployment featureDeployment;
	private IFeatureMasterService featureMasterService;
	private ConfigurationContext configurationContext;

	/**
	 * this method use for adding featureDeployement data with also inserting
	 * configNode data into DB.
	 */
	public void addFeatureDeployement() {
		System.setProperty(PROFILE_ID, LOCAL);
		new InsertConfigNodeForTesting();
		if (featureDeployment == null)
			featureDeployment = new FeatureDeploymentService();
		if (featureMasterService == null)
			featureMasterService = new FeatureMasterService();
		try {
			featureMasterService.insertFeatureDetailsIntoFeatureMaster(getFeatureMasterData());
			configurationContext = new ConfigurationContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE,
					TEST_IMPL, TEST_VENDOR, TEST_VERSION, TEST_PROVIDER);
			featureDeployment.addFeatureDeployement(configurationContext, true, true, true);
		} catch (FeatureMasterServiceException | FeatureDeploymentServiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * this method use for deleting featureDeployement data and also Deleting
	 * configNode data from DB..
	 */
	public void deleteFeatureDeployement() {
		try {
			new DeleteConfigNodeForTesting();
			testDeleteFeatureDeployed();
			featureMasterService.deleteFeatureDetailsInFeatureMaster(TEST_FEATURE, 2);
		} catch (FeatureDeploymentServiceException | FeatureMasterServiceException e) {
			e.printStackTrace();
		}

	}

	private boolean testDeleteFeatureDeployed() throws FeatureDeploymentServiceException {
		return featureDeployment.deleteFeatureDeployed(configurationContext);
	}

	private FeatureMaster getFeatureMasterData() {
		FeatureMaster featureMaster = new FeatureMaster();
		featureMaster.setFeature(TEST_FEATURE);
		featureMaster.setFeatureGroup(TEST_FEATUREGROUP);
		featureMaster.setSiteId(TEST_SITE_NODEID);
		featureMaster.setVersion(TEST_VERSION);
		featureMaster.setAllowMultipleImpl(true);
		featureMaster.setMultipleVendorSupport(true);
		featureMaster.setProduct("Junit Testing 1.0");
		featureMaster.setDescription("Testing purpose");
		return featureMaster;
	}
}
