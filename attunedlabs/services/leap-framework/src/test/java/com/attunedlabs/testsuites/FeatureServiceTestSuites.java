package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.feature.config.FeatureConfigXMLParserTest;
import com.attunedlabs.feature.config.FeatureConfigurationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
	FeatureConfigurationServiceTest.class, 
	FeatureConfigXMLParserTest.class
})
public class FeatureServiceTestSuites {
	
}
