package com.attunedlabs.leap.header.initializer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

public class RootDeployableConfigurationTest {

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
	}

	/**
	 * This method check isRootDeployableFeature have featureGroup- parcel and
	 * featureName-parcelService in globleAppDeploymentConfig.properties.
	 */
	@Test
	public void testIsRootDeployableFeatureTrue() {

		boolean rootDeployableFeature = RootDeployableConfiguration.isRootDeployableFeature("parcel", "parcelservice");
		Assert.assertTrue(
				"rootDeployable Feature should be true because it is Present in globleAppDeploymentCong properties ::",
				rootDeployableFeature);
	}

	/**
	 * This method check isRootDeployableFeature not have
	 * featureGroup-TestFeatureGroup and featureName-TestFeature in
	 * globleAppDeploymentConfig.properties.
	 */
	@Test
	public void testIsRootDeployableFeatureFalse() {

		boolean rootDeployableFeature = RootDeployableConfiguration.isRootDeployableFeature(TEST_FEATUREGROUP,
				TEST_FEATURE);
		Assert.assertFalse(
				"rootDeployable Feature should be false because it is not Present in globleAppDeploymentCong properties ::",
				rootDeployableFeature);
	}

}
