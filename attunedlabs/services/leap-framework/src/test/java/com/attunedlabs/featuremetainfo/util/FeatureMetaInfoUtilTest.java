package com.attunedlabs.featuremetainfo.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.featuremetainfo.taxonomy.FeatureTaxonomyHelperTest;

public class FeatureMetaInfoUtilTest {

	final Logger logger = LoggerFactory.getLogger(FeatureTaxonomyHelperTest.class);
	private final String featureMetaInfoFileName = "FeatureMetaInfo/featureMetaInfo.xml";

	/**
	 * this method used for check resource file url.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testCheckAndGetResourceUrl() throws MalformedURLException {
		ConfigurationContext configurationContext = ContextData.getConfigContext();
		URL url = FeatureMetaInfoUtil.checkAndGetResourceUrl(featureMetaInfoFileName, configurationContext);
		Assert.assertNotNull("URL Should not be null ::", url);
		Assert.assertTrue("url should be contain besic file path :: ",
				url.toString().contains(featureMetaInfoFileName));
	}

}
