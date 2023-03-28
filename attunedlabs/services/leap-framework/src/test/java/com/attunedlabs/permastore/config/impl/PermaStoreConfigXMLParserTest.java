package com.attunedlabs.permastore.config.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.permastore.config.util.PermastoreFileRead;

public class PermaStoreConfigXMLParserTest {

	private PermaStoreConfigXMLParser configXMLParser;
	private String permastoreStringFile;
	private PermaStoreConfigurations persmatoreConfigs;

	/**
	 * used for object initialization
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Before
	public void setUp() throws PermaStoreConfigParserException {
		if (configXMLParser == null)
			configXMLParser = new PermaStoreConfigXMLParser();
		if (permastoreStringFile == null)
			permastoreStringFile = PermastoreFileRead.getPermaStoreConfiAsString();
		if (persmatoreConfigs == null)
			persmatoreConfigs = PermastoreFileRead.getPermaStoreConfigurations();
	}

	/**
	 * Marshall String xml to permastore Config Object
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testMarshallConfigXMLtoObject() throws PermaStoreConfigParserException {
		PermaStoreConfigurations permaStoreConfig = configXMLParser.marshallConfigXMLtoObject(permastoreStringFile);
		Assert.assertNotNull("Permastore Configuration Should Not be null ::", permaStoreConfig);
		Assert.assertEquals("permastore Configuration Name Should be same as 'TestName' ::", "TestName",
				permaStoreConfig.getPermaStoreConfiguration().get(0).getName());
	}

	/**
	 * Marshall String xml to permastore Config Object
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testMarshallXMLtoObject() throws PermaStoreConfigParserException {
		PermaStoreConfigurations permaStoreConfig = configXMLParser.marshallXMLtoObject(permastoreStringFile);
		Assert.assertNotNull("Permastore Configuration Should Not be null ::", permaStoreConfig);
		Assert.assertEquals("permastore Configuration Name Should be same as 'TestName' ::", "TestName",
				permaStoreConfig.getPermaStoreConfiguration().get(0).getName());
	}

	/**
	 * unmarshall permastore Config object to String Xml.
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testunmarshallObjecttoXML() throws PermaStoreConfigParserException {
		String xmlFile = configXMLParser.unmarshallObjecttoXML(persmatoreConfigs.getPermaStoreConfiguration().get(0));
		Assert.assertNotNull("XML file should not be null ::", xmlFile);
		Assert.assertTrue("XML file should have permastore Name :: ", xmlFile.contains("TestName"));
	}
}
