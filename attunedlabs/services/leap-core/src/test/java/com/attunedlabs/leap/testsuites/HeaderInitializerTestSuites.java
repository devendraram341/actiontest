package com.attunedlabs.leap.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.leap.header.initializer.FeatureHeaderInitializerTest;
import com.attunedlabs.leap.header.initializer.RootDeployableConfigurationTest;

@RunWith(Suite.class)
@SuiteClasses({
		FeatureHeaderInitializerTest.class,
		RootDeployableConfigurationTest.class
	})
public class HeaderInitializerTestSuites {

}
