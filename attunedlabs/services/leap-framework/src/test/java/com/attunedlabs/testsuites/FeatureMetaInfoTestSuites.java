package com.attunedlabs.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigXmlParserTest;
import com.attunedlabs.featuremetainfo.taxonomy.FeatureTaxonomyHelperTest;
import com.attunedlabs.featuremetainfo.util.FeatureMetaInfoUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
	FeatureMetaInfoConfigXmlParserTest.class,
	FeatureTaxonomyHelperTest.class,
	FeatureMetaInfoUtilTest.class,
})
public class FeatureMetaInfoTestSuites {
	
}
