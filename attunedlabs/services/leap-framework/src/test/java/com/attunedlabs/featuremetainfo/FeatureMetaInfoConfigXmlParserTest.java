package com.attunedlabs.featuremetainfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;

public class FeatureMetaInfoConfigXmlParserTest {

	final Logger log = LoggerFactory.getLogger(FeatureMetaInfoConfigXmlParserTest.class);
	private final String featureMetaInfoFile = "FeatureMetaInfo/featureMetaInfo.xml";

	private FeatureMetaInfoConfigXmlParser configXmlParser = new FeatureMetaInfoConfigXmlParser();

	/**
	 * this method used for featureMetaInfo Config xml to object.
	 * 
	 * @throws FeatureMetaInfoConfigParserException
	 */
	@Test
	public void testMarshallConfigXMLtoObject() throws FeatureMetaInfoConfigParserException {
		FeatureMetainfo featureMasterInfoData = configXmlParser.marshallConfigXMLtoObject(getXMLFileToString());
		Assert.assertNotNull("Feature master Data Should not be null ::", featureMasterInfoData);
		Assert.assertEquals(GenericTestConstant.TEST_FEATUREGROUP,
				featureMasterInfoData.getFeatureGroup().get(0).getName());
	}

	/**
	 * this method use for featureMetaInfo xml to object
	 * 
	 * @throws FeatureMetaInfoConfigParserException
	 */
	@Test
	public void testMarshallXMLtoObject() throws FeatureMetaInfoConfigParserException {
		FeatureMetainfo featureMasterInfoData = configXmlParser.marshallXMLtoObject(getXMLFileToString());
		Assert.assertNotNull("Feature master Data Should not be null ::", featureMasterInfoData);
		Assert.assertEquals(GenericTestConstant.TEST_FEATUREGROUP,
				featureMasterInfoData.getFeatureGroup().get(0).getName());
	}

	/**
	 * read file from test resource folder.
	 * 
	 * @return {@link String}
	 */
	private String getXMLFileToString() {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(featureMetaInfoFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("eventFramework file doesnot exist in classpath", e);
			e.printStackTrace();
		}
		return out1.toString();
	}

}
