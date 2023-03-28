package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.featuredeployment.FeatureDeploymentServiceTest;
import com.attunedlabs.featuremaster.FeatureMasterServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
	FeatureDeploymentServiceTest.class, 
	FeatureMasterServiceTest.class
})
public class FeatureMasterAndDeploymentTestSuites {
	
}
