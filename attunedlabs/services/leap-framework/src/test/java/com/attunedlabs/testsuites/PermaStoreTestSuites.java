package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.permastore.ConfigPolicyJSONParserTest;
import com.attunedlabs.permastore.config.PermaStoreConfigurationServiceTest;
import com.attunedlabs.permastore.config.PermaStoreCustomCacheObjectBuilderTest;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigBuilderHelperTest;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParserTest;
import com.attunedlabs.permastore.config.impl.PermaStoreInLineCacheObjectBuilderTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	PermaStoreConfigurationServiceTest.class,
	PermaStoreCustomCacheObjectBuilderTest.class,
	PermaStoreConfigBuilderHelperTest.class,
	PermaStoreConfigXMLParserTest.class,
	PermaStoreInLineCacheObjectBuilderTest.class,
	ConfigPolicyJSONParserTest.class
	})
public class PermaStoreTestSuites {
	
}
