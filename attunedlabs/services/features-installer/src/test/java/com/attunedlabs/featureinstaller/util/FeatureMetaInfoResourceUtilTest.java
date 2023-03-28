package com.attunedlabs.featureinstaller.util;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.attunedlabs.FeatureInstallerTestConstant.*;

import com.attunedlabs.FeatureInstallerUtils;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.util.EmbeddedHSQLDBConnectionUtil;
import com.attunedlabs.config.util.HSQLDBConnectionException;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.FeatureGroup;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigParserException;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParser;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FeatureMetaInfoResourceUtilTest {

	private static FeatureDeploymentTestConfigDB testConfigDB = new FeatureDeploymentTestConfigDB();
	private static FeatureMetaInfoResourceUtil resourceUtil;

	@BeforeClass
	public static void setUp() throws HSQLDBConnectionException {
		System.setProperty(PROFILE_ID, LOCAL);
		EmbeddedHSQLDBConnectionUtil.readFileAndExecute(FeatureInstallerUtils.getRequestContext(), null);
		resourceUtil = new FeatureMetaInfoResourceUtil();
		testConfigDB.addFeatureDeployement();
	}

	/**
	 * This method will load resource from feature level
	 * 
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testALoadAllResourceFromFeatureLevel()
			throws FeatureMetaInfoResourceException, IOException, ConfigPersistenceException {
		FeatureInstallerUtils.setTenantAndSite();
		resourceUtil.loadAllResourceFromFeatureLevel(getFeatureMetaInfo());

		List<ConfigNodeData> listOfConfigNode = findConfigNodeData();

		Assert.assertNotNull("list of configNodeData should not be null ::", listOfConfigNode);
		Assert.assertTrue("list of config node data should be greater then zero ::", listOfConfigNode.size() > 0);
		Assert.assertEquals("config node data perent id should be same as TestVendor id ::", TEST_VENDOR_NODEID,
				(int) listOfConfigNode.get(0).getParentConfigNodeId());
	}

	/**
	 * In this method load required data from ConfigNodeData table.
	 */
	@Test
	public void testCheckDataFromConfigNodeData() {
		List<ConfigNodeData> listOfConfigNode = findConfigNodeData();
		int sizeOfList = listOfConfigNode.size();
		for (int i = 0; i < sizeOfList; i++) {
			if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("PermaStore")) {
				permastoreConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("Event")
					&& listOfConfigNode.get(i).getConfigName().equalsIgnoreCase("Success_Event_Test")) {
				eventsConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("DispatchChannel")) {
				dispatchChannelConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("SystemEvent")
					&& listOfConfigNode.get(i).getConfigName().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
				systemEventConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("DataContext")) {
				featureDataContextConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("Feature")) {
				featureServiceConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("FeatureHandlers")) {
				serviceHandlerConfigData = listOfConfigNode.get(i).getConfigData();
			} else if (listOfConfigNode.get(i).getConfigType().equalsIgnoreCase("JMSEventSubscription")) {
				jmsEventSubscriptionConfigData = listOfConfigNode.get(i).getConfigData();
			}
		}
		Assert.assertNotNull("PermastoreConfigData Should not be null :: ", permastoreConfigData);
		Assert.assertNotNull("eventsConfigData Should not be null :: ", eventsConfigData);
		Assert.assertNotNull("dispatchChannelConfigData Should not be null :: ", dispatchChannelConfigData);
		Assert.assertNotNull("systemEventConfigData Should not be null :: ", systemEventConfigData);
		Assert.assertNotNull("featureDataContextConfigData Should not be null :: ", featureDataContextConfigData);
		Assert.assertNotNull("featureServiceConfigData Should not be null :: ", featureServiceConfigData);
		Assert.assertNotNull("serviceHandlerConfigData Should not be null :: ", serviceHandlerConfigData);
		Assert.assertNotNull("jmsEventSubscriptionConfigData Should not be null :: ", jmsEventSubscriptionConfigData);
	}

	/**
	 * In this method check PermaStore feature successfully loaded or not.
	 * 
	 * @throws PermaStoreConfigParserException
	 */
	@Test
	public void testPermastoreData() throws PermaStoreConfigParserException {

		PermaStoreConfigXMLParser configXMLParser = new PermaStoreConfigXMLParser();
		PermaStoreConfigurations permastoreConfig = configXMLParser.marshallConfigXMLtoObject(permastoreConfigData);
		Assert.assertNotNull("Permastore Configuration data should not be null:: ", permastoreConfig);
		List<PermaStoreConfiguration> permaStoreConfiguration = permastoreConfig.getPermaStoreConfiguration();

		Assert.assertFalse("List of PermastoreConfig should not be empty ::", permaStoreConfiguration.isEmpty());
		Assert.assertTrue("List of PersmastoreConfig should be greater then zero ::",
				permaStoreConfiguration.size() > 0);

		int size = FeatureInstallerUtils.getPermastore().getPermaStoreConfiguration().size();
		String name = FeatureInstallerUtils.getPermastore().getPermaStoreConfiguration().get(0).getName();
		Assert.assertEquals("permastore Configuration size should be same as expected ::", size,
				permaStoreConfiguration.size());
		Assert.assertEquals("permastore Configuration name should be same as expected ::", name,
				permaStoreConfiguration.get(0).getName());
	}

	/**
	 * In this method check Service Handler feature successfully loaded or not.
	 * 
	 * @throws ServiceHandlerConfigParserException
	 */
	@Test
	public void testServiceHandler() throws ServiceHandlerConfigParserException {

		ServiceHandlerConfigXMLParser configXMLParser = new ServiceHandlerConfigXMLParser();
		ServiceHandlerConfiguration serviceHandler = configXMLParser
				.marshallConfigXMLtoObject(serviceHandlerConfigData);

		Assert.assertNotNull("ServiceHandler Data should not be null :", serviceHandler);

		int size = FeatureInstallerUtils.getServiceHandler().getFeatureServiceHandler().getServiceHandler().size();
		Assert.assertEquals("Service handler name should be same as Expected :", size,
				serviceHandler.getFeatureServiceHandler().getServiceHandler().size());
	}

	/**
	 * In this method check Event feature successfully loaded or not.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testEvents() throws EventFrameworkConfigParserException {

		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework event = parser.marshallConfigXMLtoObject(eventsConfigData);
		List<Event> getEventsFromConfigNode = event.getEvents().getEvent();
		Assert.assertFalse("list of Event Data from configNodeData should not be empty ::",
				getEventsFromConfigNode.isEmpty());
		Assert.assertTrue("list of Event Data should be greater then zero ::", getEventsFromConfigNode.size() > 0);

		List<Event> getEventFromUtil = FeatureInstallerUtils.getEvents().getEvent();
		Assert.assertEquals("Event Id should be same as Local Event Id ::", getEventFromUtil.get(0).getId(),
				getEventsFromConfigNode.get(0).getId());
	}

	/**
	 * In this method check Dispatch Channel feature successfully loaded or not.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testDispatchChannel() throws EventFrameworkConfigParserException {

		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework event = parser.marshallConfigXMLtoObject(dispatchChannelConfigData);
		List<DispatchChannel> getDispatchFromConfigNode = event.getDispatchChannels().getDispatchChannel();
		Assert.assertFalse("list of Dispatch Channel should not be empty ::", getDispatchFromConfigNode.isEmpty());
		Assert.assertTrue("list of Dispatch channel should be greater then zero :: ",
				getDispatchFromConfigNode.size() > 0);

		List<DispatchChannel> getDispatchFromUtil = FeatureInstallerUtils.getEventDispatchChannels()
				.getDispatchChannel();
		Assert.assertEquals("Dispatch channel id should be same as expected ::", getDispatchFromUtil.get(0).getId(),
				getDispatchFromConfigNode.get(0).getId());
	}

	/**
	 * In this method check System Event feature successfully loaded or not.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testSystemEvent() throws EventFrameworkConfigParserException {

		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework event = parser.marshallConfigXMLtoObject(systemEventConfigData);
		List<SystemEvent> getSystemEventFromConfigNode = event.getSystemEvents().getSystemEvent();
		Assert.assertFalse("list of system Event should not be empty ::", getSystemEventFromConfigNode.isEmpty());
		Assert.assertTrue("list of system event should be greater then zero ::",
				getSystemEventFromConfigNode.size() > 0);

		List<SystemEvent> getSystemEventFromUtil = FeatureInstallerUtils.getSystemEvents().getSystemEvent();
		Assert.assertEquals("System Event Id should be same as expected Data ::", getSystemEventFromUtil.get(0).getId(),
				getSystemEventFromConfigNode.get(0).getId());
	}

	/**
	 * In this method check Event subscription feature successfully loaded or not.
	 * 
	 * @throws EventFrameworkConfigParserException
	 */
	@Test
	public void testEventSubscription() throws EventFrameworkConfigParserException {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		EventFramework event = parser.marshallConfigXMLtoObject(jmsEventSubscriptionConfigData);
		List<JMSSubscribeEvent> getJMSSubFromConfigNode = event.getEventSubscription().getJmsSubscribeEvent();
		Assert.assertFalse("list of jms subscription should not be empty ::", getJMSSubFromConfigNode.isEmpty());
		Assert.assertTrue("list of jms subscription should be greater then zero ::",
				getJMSSubFromConfigNode.size() > 0);

		List<JMSSubscribeEvent> getJMSSubFromUtil = FeatureInstallerUtils.getEventSubscription().getJmsSubscribeEvent();
		Assert.assertEquals("jms Event sub Id should be same as expected Data ::",
				getJMSSubFromUtil.get(0).getSubscriptionId(), getJMSSubFromConfigNode.get(0).getSubscriptionId());
	}

	/**
	 * In this method check Feature Data Context feature successfully loaded or not.
	 * 
	 * @throws DataContextParserException
	 */
	@Test
	public void testFeatureDataContext() throws DataContextParserException {

		DataContextConfigXMLParser configXMLParser = new DataContextConfigXMLParser();
		FeatureDataContext featureDataContext = configXMLParser.marshallConfigXMLtoObject(featureDataContextConfigData);

		List<DataContext> geDataContextFromConfigNodeData = featureDataContext.getDataContexts().getDataContext();
		Assert.assertFalse("list of feature data context should not be empty ::",
				geDataContextFromConfigNodeData.isEmpty());
		Assert.assertTrue("list of feature data context should be greater then zero ::",
				geDataContextFromConfigNodeData.size() > 0);

		List<DataContext> geDataContextFromUtil = FeatureInstallerUtils.getFeatureDataContext().getDataContexts()
				.getDataContext();
		Assert.assertEquals("dataContext bean ref Name should be same as expected data ::",
				geDataContextFromUtil.get(0).getDbBeanRefName(),
				geDataContextFromConfigNodeData.get(0).getDbBeanRefName());
	}

	/**
	 * In this method check Feature Service feature successfully loaded or not.
	 * 
	 * @throws FeatureConfigParserException
	 */
	@Test
	public void testFeatureService() throws FeatureConfigParserException {

		FeatureConfigXMLParser configXMLParser = new FeatureConfigXMLParser();
		FeaturesServiceInfo featureServiceInfoFromConfigNodeData = configXMLParser
				.marshallConfigXMLtoObject(featureServiceConfigData);
		Assert.assertNotNull("Feature Service data should not be null ::", featureServiceInfoFromConfigNodeData);

		List<Service> serviceFromConfigNodeData = featureServiceInfoFromConfigNodeData.getFeatures().getFeature()
				.getService();
		Assert.assertFalse("service of FeatureServiceInfo should not be empty :", serviceFromConfigNodeData.isEmpty());
		Assert.assertTrue("Service data should be greater then 0 ::", serviceFromConfigNodeData.size() > 0);

		List<Service> serviceFromUtil = FeatureInstallerUtils.getFeatureServiceInfo().getFeatures().getFeature()
				.getService();
		Assert.assertEquals("Service Name should be same as expected data:: ", serviceFromUtil.get(0).getName(),
				serviceFromConfigNodeData.get(0).getName());
	}

	/**
	 * this method get ConfigNodeData data from DB:
	 * 
	 * @return
	 */
	private List<ConfigNodeData> findConfigNodeData() {

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		List<ConfigNodeData> listOfConfigNode = null;
		try {
			listOfConfigNode = configPersistenceService.getConfigNodeDataByNodeId(TEST_VENDOR_NODEID);
		} catch (ConfigPersistenceException e) {
			e.printStackTrace();
		}
		return listOfConfigNode;
	}

	/**
	 * get list of featureGroup for loading resource from feature level.
	 * 
	 * @return
	 */
	private List<FeatureGroup> getFeatureMetaInfo() {
		List<FeatureGroup> featureGroupList = null;
		FeatureMetaInfoConfigXmlParser featureMetaInfoParser = new FeatureMetaInfoConfigXmlParser();
		try {
			FeatureMetainfo featureMetaInfo1 = featureMetaInfoParser
					.marshallConfigXMLtoObject(FeatureInstallerUtils.getFeatureMetaInfoXMLFileToString());
			featureGroupList = featureMetaInfo1.getFeatureGroup();
		} catch (FeatureMetaInfoConfigParserException e) {
			e.printStackTrace();
		}
		return featureGroupList;
	}

	@AfterClass
	public static void cleanUp() throws HSQLDBConnectionException {
		testConfigDB.deleteFeatureDeployement();
		EmbeddedHSQLDBConnectionUtil.revertGlobalAppDeploymentProperties();
	}

}
