package com.attunedlabs.datacontext.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;

public class DataContextConfigXMLParserTest {

	final Logger log = LoggerFactory.getLogger(DataContextConfigXMLParserTest.class);
	private DataContextConfigXMLParser dataContextXmlParser;
	private final static String CONFIG_FILE_TO_PARSE = "FeatureDataContext/testFeatureDataContext.xml";

	static String xmlString;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void init() {
		if (xmlString == null)
			xmlString = getDataContextConfiguration();
		if (dataContextXmlParser == null)
			dataContextXmlParser = new DataContextConfigXMLParser();
	}

	/**
	 * this method used for read test resources file.
	 * 
	 * @throws DataContextParserException
	 */
	@Test
	public void testFeatureConfigReading() throws DataContextParserException {
		FeatureDataContext featureDataContext = dataContextXmlParser.marshallConfigXMLtoObject(xmlString);
		Assert.assertNotNull("data context config must not be Null", featureDataContext);
		String dataContextName = featureDataContext.getDataContexts().getContextName();
		Assert.assertEquals("data context config name must be test-TestService-DataContext", dataContextName,
				"test-TestService-DataContext");

	}

	/**
	 * this method used for object to xml format.
	 * 
	 * @throws DataContextParserException
	 */
	@Test
	public void testUnmarshallObjecttoXML() throws DataContextParserException {
		FeatureDataContext featureDataContext = dataContextXmlParser.marshallConfigXMLtoObject(xmlString);
		String configXml = dataContextXmlParser.unmarshallObjecttoXML(featureDataContext);
		Assert.assertNotNull("Configuration xml should not be null and should be valid xml String", configXml);

	}

	/**
	 * this method used for xml to object format.
	 * 
	 * @throws DataContextParserException
	 */
	@Test
	public void testMarshallXMLtoObject() throws DataContextParserException {
		FeatureDataContext featureDataContext = dataContextXmlParser.marshallConfigXMLtoObject(xmlString);
		String configXml = dataContextXmlParser.unmarshallObjecttoXML(featureDataContext);
		FeatureDataContext featureDataContext1 = dataContextXmlParser.marshallXMLtoObject(configXml);
		String name1 = featureDataContext.getDataContexts().getContextName();
		String name2 = featureDataContext1.getDataContexts().getContextName();
		Assert.assertEquals(name1, name2);

	}

	/**
	 * this method used for load file from test resources folder.
	 * 
	 * @return
	 */
	private String getDataContextConfiguration() {
		InputStream inputstream = DataContextConfigurationServiceTest.class.getClassLoader()
				.getResourceAsStream(CONFIG_FILE_TO_PARSE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);

			}
			reader.close();
		} catch (IOException e) {
			log.error("datacontext file doesnot exist in classpath", e);
		}

		return out1.toString();
	}
}
