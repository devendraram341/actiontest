package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.datacontext.config.DataContextConfigXMLParserTest;
import com.attunedlabs.datacontext.config.DataContextConfigurationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	DataContextConfigurationServiceTest.class, 
	DataContextConfigXMLParserTest.class 
	})
public class DataContextTestSuites {
	
}
