package com.attunedlabs.feature.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;

public class FeatureConfigXMLParserTest {

	final Logger log = LoggerFactory.getLogger(FeatureConfigXMLParserTest.class);
	private final String file = "Feature/feature-service.xml";
	private FeatureConfigXMLParser configXMLParser;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void beforeTest() {
		if (configXMLParser == null)
			configXMLParser = new FeatureConfigXMLParser();
	}

	/**
	 * This method use for marshalling, xml to object like xmlstring to
	 * FeatureServiceInfo.
	 * 
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testMarshallConfigXMLtoObject() throws FeatureConfigParserException {
		String xmlString = getXMLFileToString();
		FeaturesServiceInfo featureServiceInfo = configXMLParser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("Feature Service info should not be null :: ", featureServiceInfo);
		Assert.assertEquals("Feature Group Name Should be same as featureService feature Group name ::",
				GenericTestConstant.TEST_FEATUREGROUP, featureServiceInfo.getFeatures().getFeatureGroup());
	}

	/**
	 * This method use for marshalling, xml to object like xmlstring to
	 * FeatureServiceInfo.
	 * 
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testMarshallXMLtoObject() throws FeatureConfigParserException {
		String xmlString = getXMLFileToString();
		FeaturesServiceInfo featureServiceInfo = configXMLParser.marshallXMLtoObject(xmlString);
		Assert.assertNotNull("Feature Service info should not be null :: ", featureServiceInfo);
		Assert.assertEquals("Feature Group Name Should be same as featureService feature Group name ::",
				GenericTestConstant.TEST_FEATUREGROUP, featureServiceInfo.getFeatures().getFeatureGroup());
	}

	/**
	 * This method use for unmarshaling, object to xml like feature to xml.
	 * 
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testUnmarshallObjecttoXML() throws FeatureConfigParserException {
		String xmlData = configXMLParser.unmarshallObjecttoXML(getFeature(), GenericTestConstant.TEST_FEATUREGROUP);
		Assert.assertNotNull("String value should not be null :: ", xmlData);
		Assert.assertTrue("In XmlString should be contains TestFeatureGroup ::",
				xmlData.contains(GenericTestConstant.TEST_FEATUREGROUP));
	}

	private Feature getFeature() {
		Feature feature = new Feature();
		feature.setEnabled(true);
		feature.setFeatureName(GenericTestConstant.TEST_FEATURE);
		feature.setImplementationName(GenericTestConstant.TEST_IMPL);
		feature.setInterfaceName("");
		feature.setVersion(GenericTestConstant.TEST_VERSION);
		return feature;
	}

	/**
	 * read file from test resources.
	 * 
	 * @return
	 */
	private String getXMLFileToString() {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(file);
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
