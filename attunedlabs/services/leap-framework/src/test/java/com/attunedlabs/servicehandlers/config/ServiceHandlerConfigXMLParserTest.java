package com.attunedlabs.servicehandlers.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;
import com.attunedlabs.servicehandlers.util.ServiceFileRead;

public class ServiceHandlerConfigXMLParserTest {

	private ServiceHandlerConfigXMLParser configXMLParser;
	final Logger log = LoggerFactory.getLogger(ServiceHandlerConfigXMLParserTest.class);

	@Before
	public void setUp() {
		if (configXMLParser == null) {
			configXMLParser = new ServiceHandlerConfigXMLParser();
		}
	}

	@Test
	public void testMarshallConfigXMLtoObject() throws ServiceHandlerConfigParserException {
		String xmlFile = ServiceFileRead.getServiceHandlerAsString();
		Assert.assertNotNull("Service Handler String should not be null ::", xmlFile);

		ServiceHandlerConfiguration serviceHandlerConfig = configXMLParser.marshallConfigXMLtoObject(xmlFile);
		Assert.assertNotNull("ServiceHandlerConfiguration data should not be null ::", serviceHandlerConfig);
		Assert.assertEquals("testFeatureHandler1",
				serviceHandlerConfig.getFeatureServiceHandler().getServiceHandler().get(0).getHandlerId());
	}

	@Test
	public void testMarshallConfigXMLtoAppObject() throws ServiceHandlerConfigParserException {
		String xmlFile = ServiceFileRead.getApplicationServiceHandlerAsString();
		Assert.assertNotNull("Application Service Handler String should not be null ::", xmlFile);

		ApplicationServiceHandlerConfiguration appServiceHandlerConfig = configXMLParser
				.marshallConfigXMLtoAppObject(xmlFile);
		Assert.assertNotNull("ApplicationServiceHandlerConfiguration data should not be null ::",
				appServiceHandlerConfig);
		Assert.assertEquals("testAppFeatureHandler1",
				appServiceHandlerConfig.getFeatureServiceHandler().getServiceHandler().get(0).getHandlerId());
		Assert.assertEquals("testAppCommonHandler1",
				appServiceHandlerConfig.getCommonServiceHandler().getServiceHandler().get(0).getHandlerId());
	}

	@Test
	public void testUnmarshallCommonServiceHandlerObjecttoXML() throws ServiceHandlerConfigParserException {
		ApplicationServiceHandlerConfiguration appServiceHandlerConfig = ServiceFileRead.getApplicationServiceHandler();
		Assert.assertNotNull("ApplicationServiceHandlerConfiguration data should not be null ::",
				appServiceHandlerConfig);

		CommonServiceHandler commonServiceHandler = appServiceHandlerConfig.getCommonServiceHandler();
		Assert.assertNotNull("CommonServiceHandler data should not be null ::", commonServiceHandler);

		String xmlFile = configXMLParser.unmarshallCommonServiceHandlerObjecttoXML(commonServiceHandler);
		Assert.assertNotNull("Xml File Should not be null ::", xmlFile);
		Assert.assertTrue(xmlFile.contains("testAppCommonHandler1"));
	}

	@Test
	public void testUnmarshallFeatureServiceHandlerObjecttoXML() throws ServiceHandlerConfigParserException {
		ApplicationServiceHandlerConfiguration appServiceHandlerConfig = ServiceFileRead.getApplicationServiceHandler();
		Assert.assertNotNull("ApplicationServiceHandlerConfiguration data should not be null ::",
				appServiceHandlerConfig);

		FeatureServiceHandler featureServiceHandler = appServiceHandlerConfig.getFeatureServiceHandler();
		Assert.assertNotNull("FeatureServiceHandler data should not be null ::", featureServiceHandler);

		String xmlFile = configXMLParser.unmarshallFeatureServiceHandlerObjecttoXML(featureServiceHandler);
		Assert.assertNotNull("Xml File Should not be null ::", xmlFile);
		Assert.assertTrue(xmlFile.contains("testAppFeatureHandler1"));
	}

	@Test
	public void testUnmarshallFeatureLevelFeatureServiceHandlerObjecttoXML()
			throws ServiceHandlerConfigParserException {
		ServiceHandlerConfiguration serviceHandler = ServiceFileRead.getServiceHandlerFile();
		Assert.assertNotNull("ServiceHandlerConfiguration data should not be null ::", serviceHandler);

		com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler = serviceHandler
				.getFeatureServiceHandler();
		Assert.assertNotNull("FeatureServiceHandler data should not be null ::", featureServiceHandler);
		
		String xmlFile = configXMLParser.unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML(featureServiceHandler);
		Assert.assertNotNull("Xml File Should not be null ::", xmlFile);
		Assert.assertTrue(xmlFile.contains("testFeatureHandler1"));
	}
}
