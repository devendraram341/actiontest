package com.attunedlabs.eventframework.config.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationUnit;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.CamelProducerConfig;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class EventFrameworkConfigHelperCopy{
	final Logger logger = LoggerFactory.getLogger(IEventFrameworkConfigService.class);
	private EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, Event evtFwkConfig)
			throws EventFrameworkConfigurationException {
		// Check and get ConfigNodeId for this
		Integer configNodeId;
		try {
			ConfigurationContext context = new ConfigurationContext(configContext.getTenantId(),
					configContext.getSiteId(), null, null, null);
			configNodeId = getApplicableNodeId(context);
			String evtFwkXMLStr = parser.unmarshallObjecttoXML(evtFwkConfig);
			logger.debug(".addEventFrameworkConfiguration  Applicable Config Node Id is =" + configNodeId);
			logger.debug(".addEventFrameworkConfiguration -XmlStr=" + evtFwkXMLStr);
			logger.debug(".addEventFrameworkConfiguration -EventId-" + evtFwkConfig.getId());

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, evtFwkConfig.getId(), evtFwkXMLStr,
					EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);// ConfigNodeData(Integer
																	// parentConfigNodeId,
																	// String
																	// configName,
																	// String
																	// configData,
																	// String
																	// configType)
			configNodeData.setEnabled(evtFwkConfig.isIsEnabled());
			configNodeData.setConfigLoadStatus("Sucess");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, evtFwkConfig.getId(), EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				// build configuration unit to cache.
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configContext.getTenantId(), configContext.getSiteId(), configNodeId,
						evtFwkConfig.isIsEnabled(), evtFwkConfig);
				evtConfigUnit.setDbconfigId(configDataId);
				loadConfigurationInDataGrid(evtConfigUnit);
				// updatagrid for EventProducer Mapping
				updateDataGridForEventProducer(evtConfigUnit);
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException | EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to add EventConfiguration for Event with eventId" + evtFwkConfig.getId(), e);
		}
	}// end of method

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, SystemEvent sysevtFwkConfig)
			throws EventFrameworkConfigurationException {
		Integer configNodeId;
		try {
			ConfigurationContext context = new ConfigurationContext(configContext.getTenantId(),
					configContext.getSiteId(), null, null, null);
			configNodeId = getApplicableNodeId(context);
			logger.debug("Applicable Config Node Id is =" + configNodeId);
			String evtFwkXMLStr = parser.unmarshallObjecttoXML(sysevtFwkConfig);

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, sysevtFwkConfig.getId(), evtFwkXMLStr,
					EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
			configNodeData.setEnabled(sysevtFwkConfig.isIsEnabled());
			configNodeData.setConfigLoadStatus("Sucess");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, sysevtFwkConfig.getId(), EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				// build configuration unit to cache.
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configContext.getTenantId(), configContext.getSiteId(), configNodeId,
						sysevtFwkConfig.isIsEnabled(), sysevtFwkConfig);
				evtConfigUnit.setDbconfigId(configDataId);
				loadConfigurationInDataGrid(evtConfigUnit);
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException | EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to add EventConfiguration for SystemEvent with eventId" + sysevtFwkConfig.getId(), e);
		}
	}

	public void addEventFrameworkConfiguration(ConfigurationContext configContext, DispatchChannel dispatchChannelConfig)
			throws EventFrameworkConfigurationException {
		Integer configNodeId;
		try {
			ConfigurationContext context = new ConfigurationContext(configContext.getTenantId(),
					configContext.getSiteId(), null, null, null);
			configNodeId = getApplicableNodeId(context);
			logger.debug("Applicable Config Node Id is =" + configNodeId);
			String evtFwkXMLStr = parser.unmarshallObjecttoXML(dispatchChannelConfig);

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, dispatchChannelConfig.getId(), evtFwkXMLStr,
					EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
			configNodeData.setEnabled(dispatchChannelConfig.isIsEnabled());
			configNodeData.setConfigLoadStatus("Sucess");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, dispatchChannelConfig.getId(), EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				// build configuration unit to cache.
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configContext.getTenantId(), configContext.getSiteId(), configNodeId,
						dispatchChannelConfig.isIsEnabled(), dispatchChannelConfig);
				evtConfigUnit.setDbconfigId(configDataId);
				loadConfigurationInDataGrid(evtConfigUnit);
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException | EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to add EventConfiguration for DispatchChannel with ChannelId" + dispatchChannelConfig.getId(),
					e);
		}

	}

	public Event getEventConfiguration(ConfigurationContext configContext, String forEventId)
			throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			Event event = null;
			logger.debug(
					"configcontext in EventFrameworkConfigHelper : " + configContext + ", event id : " + forEventId);
			int searchStartLevel = getContextLevel(configContext);
			logger.debug(".getEventConfiguration() Search Level is =" + searchStartLevel);
			for (int i = searchStartLevel; i > 1; i--) {
				prepareConfigContextForSearchLevel(configContext, i);
				Integer nodeId = getApplicableNodeId(configContext);
				String eventGroupKey = EventFrameworkConfigurationUnit.getEventConfigGroupKey(nodeId);
				logger.debug("nodeId : " + nodeId + ", event group key : " + eventGroupKey);
				EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) configServer
						.getConfiguration(configContext.getTenantId(), eventGroupKey, forEventId.trim());
				logger.debug(".getEventConfiguration() searching at level " + i + " Event" + evtFwkConfigUnit);
				if (evtFwkConfigUnit != null) {
					event = (Event) evtFwkConfigUnit.getConfigData();
					return event;
				} // if no event Config is found return null;
			}

		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to getEventConfiguration for eventId {" + forEventId + "}", e);
		}

		return null;
	}

	public CamelEventProducer getEventProducerForBean(ConfigurationContext configContext, String serviceName,
			String beanFQCN) throws EventFrameworkConfigurationException {
		try {
			logger.debug("configContext : " + configContext.toString() + " Service Name : " + serviceName + "bean : "
					+ beanFQCN);
			Integer nodeId = getApplicableNodeId(configContext);
			logger.debug("nodeId : " + nodeId);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(nodeId));
			// Getting "fqcnCompName+serviceName" as key and EventId as value
			String eventId = (String) map.get(beanFQCN + "-" + serviceName);

			if (eventId == null)
				return null;
			Event evtConfig = getEventConfiguration(configContext, eventId);
			CamelEventProducer camelEventProducer = evtConfig.getCamelEventProducer();
			return camelEventProducer;
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException();
		}
	}

	public List<CamelEventProducer> getEventProducerForServiceSuccessCompletion(ConfigurationContext configContext,
			String serviceName, String completionCase) throws EventFrameworkConfigurationException {
		try {
			Integer nodeId = getApplicableNodeId(configContext);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForServiceGroupKey(nodeId));
			// Getting "serviceName-sucess|failure" as key and EventId as value
			String eventIdListStr = (String) map.get(serviceName + "-" + completionCase);
			if (eventIdListStr == null)
				return null;
			List<CamelEventProducer> camelEvtProdList = new ArrayList(3);

			List<String> eventIdList = Arrays.asList(eventIdListStr.split(","));
			int listsize = eventIdList.size();
			for (String eventId : eventIdList) {
				Event evtConfig = getEventConfiguration(configContext, eventId);
				CamelEventProducer camelEventProducer = evtConfig.getCamelEventProducer();
				camelEvtProdList.add(camelEventProducer);
			}

			return camelEvtProdList;
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException();
		}
	}

	private static List<String> getEventListFromCommaSeperatedString(String eventIdListStr) {
		List<String> eventIdList = Arrays.asList(eventIdListStr.split(","));
		// System.out.println("List is "+eventIdList);
		return eventIdList;
	}

	public DispatchChannel getDispatchChannelConfiguration(ConfigurationContext configContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			DispatchChannel disChannel = null;
			int searchStartLevel = getContextLevel(configContext);
			logger.debug(".getDispatchChannelConfiguration() Search Level is =" + searchStartLevel);
			for (int i = searchStartLevel; i > 1; i--) {
				prepareConfigContextForSearchLevel(configContext, i);
				Integer nodeId = getApplicableNodeId(configContext);
				String eventGroupKey = EventFrameworkConfigurationUnit.getDispatchChannelConfigGroupKey(nodeId);
				EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) configServer
						.getConfiguration(configContext.getTenantId(), eventGroupKey, dispatchChannelId);
				logger.debug(".getDispatchChannelConfiguration() searching at level " + i + " ConfigUnit="
						+ evtFwkConfigUnit);
				if (evtFwkConfigUnit != null) {
					disChannel = (DispatchChannel) evtFwkConfigUnit.getConfigData();
					return disChannel;
				} // return null if not found
			}

		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to getDispatchChannelConfiguration for channelId{" + dispatchChannelId + "}", e);
		}

		return null;
	}

	public SystemEvent getSystemEventConfiguration(ConfigurationContext configContext, String systemEventId)
			throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			SystemEvent systemEvent = null;
			int searchStartLevel = getContextLevel(configContext);
			logger.debug(".getDispatchChannelConfiguration() Search Level is =" + searchStartLevel);
			for (int i = searchStartLevel; i > 1; i--) {
				prepareConfigContextForSearchLevel(configContext, i);
				Integer nodeId = getApplicableNodeId(configContext);
				String eventGroupKey = EventFrameworkConfigurationUnit.getSystemEventConfigGroupKey(nodeId);
				EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) configServer
						.getConfiguration(configContext.getTenantId(), eventGroupKey, systemEventId);
				logger.debug(
						".getSystemEventConfiguration() searching at level " + i + " ConfigUnit=" + evtFwkConfigUnit);
				if (evtFwkConfigUnit != null) {
					systemEvent = (SystemEvent) evtFwkConfigUnit.getConfigData();
					return systemEvent;
				} // return null if not found
			}

		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to getSystemEventConfiguration for systemEventid{" + systemEventId + "}", e);
		}

		return null;
	}

	private void loadConfigurationInDataGrid(EventFrameworkConfigurationUnit evfwkConfigUnit)
			throws EventFrameworkConfigurationException {
		logger.debug(".loadConfigurationInDataGrid() EventFrameworkConfigurationUnit=" + evfwkConfigUnit);
		try {
			// we upload in cache only when enabled
			if (evfwkConfigUnit.getIsEnabled()) {
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				configServer.addConfiguration(evfwkConfigUnit);
			}

		} catch (ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to Upload EventFramework Config in DataGrid configName=" + evfwkConfigUnit.getKey(), e);
		}
	}

	private void updateDataGridForEventProducer(EventFrameworkConfigurationUnit evfwkConfigUnit) {
		logger.debug("inside updateDataGridForEventProducer method with " + evfwkConfigUnit.getConfigData());
		// if eventConfiguration is Enabled the only data will be stored to Data
		if (evfwkConfigUnit.getIsEnabled()) {

			Event evtFwkConfig = (Event) evfwkConfigUnit.getConfigData();
			Integer attachedNodeId = evfwkConfigUnit.getAttachedNodeId();
			CamelEventProducer evtProducer = evtFwkConfig.getCamelEventProducer();
			if (evtProducer != null) {
				CamelProducerConfig producerConfig = evtProducer.getCamelProducerConfig();
				String beanName = producerConfig.getComponent();
				String serviceName = producerConfig.getServiceName();
				String eventId = evtFwkConfig.getId();
				String raiseoN = producerConfig.getRaiseOn();// "success";

				logger.debug("raiseoN = " + raiseoN + " , eventId= " + eventId + " , beanName= " + beanName
						+ " , serviceName=  " + serviceName);
				boolean isBeanEvent = false;
				if (beanName != null && !beanName.isEmpty())
					isBeanEvent = true;
				// else its service event
				HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
				if (isBeanEvent) {
					IMap<String, String> map = hazelcastInstance
							.getMap(EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(attachedNodeId));
					// Putting "fqcnCompName+serviceName" as key and EventId as
					// value
					map.put(producerConfig.getComponent() + "-" + serviceName, eventId);
				} else {
					if (raiseoN == null)// In xsd can't make raise on as
										// mandatory as for bean event typr it
										// has to be null
						raiseoN = "success";
					// Its service completion Event on sucess failure
					IMap<String, String> map = hazelcastInstance
							.getMap(EventFrameworkConfigurationUnit.getEventProcucerForServiceGroupKey(attachedNodeId));
					String key = serviceName + "-" + raiseoN;
					String eventListing = map.get(key);
					if (eventListing == null) {
						// Putting "serviceName-sucess|failure" as key and
						// EventId as value
						map.put(serviceName + "-" + raiseoN, eventId);
					} else {
						map.put(serviceName + "-" + raiseoN, eventListing + "," + eventId);
					}
					logger.debug("final eventIdlist = " + eventListing);
				} // end of outer else
			}
		} // end of if(evtProducer!=null)
	}

	public Event getEventConfigProducerForBean(ConfigurationContext configContext, String serviceName, String beanFQCN)
			throws EventFrameworkConfigurationException {
		try {
			Integer nodeId = getApplicableNodeId(configContext);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(nodeId));
			// Getting "fqcnCompName+serviceName" as key and EventId as value
			String eventId = (String) map.get(beanFQCN + "-" + serviceName);
			if (eventId == null)
				return null;
			Event evtConfig = getEventConfiguration(configContext, eventId);

			return evtConfig;
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException();
		}
	}

	public List<Event> getEventConfigProducerForServiceSuccessCompletion(ConfigurationContext configContext,
			String serviceName, String completionCase) throws EventFrameworkConfigurationException {
		logger.debug(".getEventConfigProducerForServiceSuccessCompletion of EventFrameworkConfigHelper");
		try {
			Integer nodeId = getApplicableNodeId(configContext);
			logger.debug("nodeId : " + nodeId);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForServiceGroupKey(nodeId));
			// Getting "serviceName-sucess|failure" as key and EventId as value
			String eventIdListStr = (String) map.get(serviceName + "-" + completionCase);
			if (eventIdListStr == null)
				return null;
			List<Event> camelEvtProdList = new ArrayList(3);

			List<String> eventIdList = Arrays.asList(eventIdListStr.split(","));
			int listsize = eventIdList.size();
			for (String eventId : eventIdList) {
				Event evtConfig = getEventConfiguration(configContext, eventId);
				// CamelEventProducer
				// camelEventProducer=evtConfig.getCamelEventProducer();
				camelEvtProdList.add(evtConfig);
			}

			return camelEvtProdList;
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException();
		}
	}

	/**
	 * To changeStatusOfDispactherChannelConfiguration based on the given status
	 * input ,if status input is true change the status in DB to true and load
	 * the configuration into Data Grid else change the status in DB to false
	 * and delete the configuration from DB
	 * 
	 * @param configurationContext
	 * @param dispatchChannelId
	 * @param isEnable
	 * @return
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean changeStatusOfDispactherChannelConfiguration(ConfigurationContext configurationContext,
			String dispatchChannelId, boolean isEnable) throws EventFrameworkConfigurationException {

		Integer applicableNodeId;
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();

			applicableNodeId = getApplicableNodeId(configurationContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					dispatchChannelId, EventFrameworkConstants.EF_DISPATCHCHANNEL_CONFIG_TYPE);
			if (configNodeData == null) {
				// config not exist in DB
				throw new EventFrameworkConfigurationException(
						"EventFrameworkConfiguration with eventId=" + dispatchChannelId + " Doesnt Exist in DB ");
			}

			if (!isEnable) {
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				String eventGroupKey = EventFrameworkConfigurationUnit
						.getDispatchChannelConfigGroupKey(configNodeData.getParentConfigNodeId());
				configServer.deleteConfiguration(configurationContext.getTenantId(), eventGroupKey, dispatchChannelId);
				return true;
			} else {
				// build configuration unit to cache.
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());

				EventFrameworkXmlHandler eventFrameworkXmlHandler = new EventFrameworkXmlHandler();
				EventFramework eventFramework = eventFrameworkXmlHandler
						.marshallConfigXMLtoObject(configNodeData.getConfigData());
				DispatchChannel dispatchChannel = eventFramework.getDispatchChannels().getDispatchChannel().get(0);
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configurationContext.getTenantId(), configurationContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), isEnable, dispatchChannel);
				evtConfigUnit.setDbconfigId(configNodeData.getNodeDataId());

				loadConfigurationInDataGrid(evtConfigUnit);
				return true;
			}
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException
				| EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Error in loading DB data to cache with dispachannelId=" + dispatchChannelId);
		}

	}

	/**
	 * To StatusOfSystemEventConfiguration based on given status , if Enable
	 * ,change the status of DB and load to Data Grid else disabled change the
	 * status to false and delete the configuration from Data Grid
	 * 
	 * @param configurationContext
	 * @param systemEventId
	 * @param isEnable
	 * @return boolean value true|false
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean changeStatusOfSystemEventConfiguration(ConfigurationContext configurationContext,
			String systemEventId, boolean isEnable) throws EventFrameworkConfigurationException {

		Integer applicableNodeId;
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();

			applicableNodeId = getApplicableNodeId(configurationContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					systemEventId, EventFrameworkConstants.EF_SYSEVENT_CONFIG_TYPE);
			if (configNodeData == null) {
				// config not exist in DB
				throw new EventFrameworkConfigurationException(
						"EventFrameworkConfiguration with eventId=" + systemEventId + " Doesnt Exist in DB ");
			}

			if (!isEnable) {
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				String eventGroupKey = EventFrameworkConfigurationUnit
						.getSystemEventConfigGroupKey(configNodeData.getParentConfigNodeId());
				configServer.deleteConfiguration(configurationContext.getTenantId(), eventGroupKey, systemEventId);
				return true;
			} else {
				// build configuration unit to cache.
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				EventFrameworkXmlHandler eventFrameworkXmlHandler = new EventFrameworkXmlHandler();
				EventFramework eventFramework = eventFrameworkXmlHandler
						.marshallConfigXMLtoObject(configNodeData.getConfigData());
				SystemEvent systemEvent = eventFramework.getSystemEvents().getSystemEvent().get(0);
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configurationContext.getTenantId(), configurationContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), isEnable, systemEvent);
				evtConfigUnit.setDbconfigId(configNodeData.getNodeDataId());

				loadConfigurationInDataGrid(evtConfigUnit);
				return true;
			}
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException
				| EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Error in loading DB data to cache with systemEventId=" + systemEventId);
		}

	}

	/**
	 * To deleteDispatcherChannelConfigaration by checking in DG(data Grid) if
	 * exist delete in both in DB and Cache else delete in DB Only
	 * 
	 * @param configContext
	 * @param dispatchChannelId
	 * @return boolean value true | false
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean deleteDipatcherChannelConfiguration(ConfigurationContext configContext, String dispatchChannelId)
			throws EventFrameworkConfigurationException {
		boolean isDeleted = false;
		try {
			DispatchChannel dispatchChannel = getDispatchChannelConfiguration(configContext, dispatchChannelId);
			int nodeId = getApplicableNodeId(configContext);
			if (dispatchChannel == null) {
				// delete from DB
				isDeleted = deleteEventFrameworkConfigurationFromDB(configContext, dispatchChannelId, nodeId);
				return isDeleted;

			}

			LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
			String eventGroupKey = EventFrameworkConfigurationUnit.getDispatchChannelConfigGroupKey(nodeId);
			IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) leapConfigurationServer
					.getConfiguration(configContext.getTenantId(), eventGroupKey, dispatchChannelId);

			isDeleted = iConfigPersistenceService.deleteConfigNodeData(evtFwkConfigUnit.getDbconfigId());
			leapConfigurationServer.deleteConfiguration(configContext.getTenantId(), eventGroupKey, dispatchChannelId);
		} catch (EventFrameworkConfigurationException | InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Error in deleting DipatcherChannelConfiguration with event ID=" + dispatchChannelId, e);
		}

		return isDeleted;
	}

	/**
	 * To deleteSystemEventConfiguration by checking in Data Grid if Exist
	 * delete in DB and data grid both else delete in DB only
	 * 
	 * @param configContext
	 * @param systemEventId
	 * @return boolean value True|false
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean deleteSystemEventConfiguration(ConfigurationContext configContext, String systemEventId)
			throws EventFrameworkConfigurationException {

		boolean isDeleted = false;
		logger.debug("inside deleteSystemEventConfiguration method with systemEventId = " + systemEventId);
		try {
			SystemEvent systemEvent = getSystemEventConfiguration(configContext, systemEventId);
			int nodeId = getApplicableNodeId(configContext);
			logger.debug("nodeId found is === " + nodeId + " and systemEvent configData in cache = " + systemEvent);
			if (systemEvent == null) {
				// delete from DB
				isDeleted = deleteEventFrameworkConfigurationFromDB(configContext, systemEventId, nodeId);
				return isDeleted;

			}

			LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
			String eventGroupKey = EventFrameworkConfigurationUnit.getSystemEventConfigGroupKey(nodeId);
			IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) leapConfigurationServer
					.getConfiguration(configContext.getTenantId(), eventGroupKey, systemEventId);
			isDeleted = iConfigPersistenceService.deleteConfigNodeData(evtFwkConfigUnit.getDbconfigId());
			leapConfigurationServer.deleteConfiguration(configContext.getTenantId(), eventGroupKey, systemEventId);

		} catch (EventFrameworkConfigurationException | InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Error in deleting SystemEventConfiguration  with event ID=" + systemEventId, e);
		}

		return isDeleted;
	}

	/**
	 * to change the statusOfEventConfigaration to enabele or Disable, if Enable
	 * load the data to both Data Grid(configuration,ForEventProducer) by
	 * setting in DB as Enabled else change the status Disable by deleting data
	 * from both Data Grid and setting configuration DB value to false
	 * 
	 * @param configContext
	 * @param eventId
	 * @param isEnable
	 * @return
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean changeStatusOfEventConfiguration(ConfigurationContext configContext, String eventId,
			boolean isEnable) throws EventFrameworkConfigurationException {

		int applicableNodeId = 0;

		try {
			applicableNodeId = getApplicableNodeId(configContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					eventId, EventFrameworkConstants.EF_EVENT_CONFIG_TYPE);

			if (configNodeData == null) {
				throw new EventFrameworkConfigurationException(
						"EventFrameworkConfiguration with eventId=" + eventId + " Doesnt Exist in DB ");
			}
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();

			EventFrameworkXmlHandler eventFrameworkXmlHandler = new EventFrameworkXmlHandler();
			EventFramework eventFramework = eventFrameworkXmlHandler
					.marshallConfigXMLtoObject(configNodeData.getConfigData());
			Event event = eventFramework.getEvents().getEvent().get(0);
			EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
					configContext.getTenantId(), configContext.getSiteId(), configNodeData.getParentConfigNodeId(),
					isEnable, event);
			evtConfigUnit.setDbconfigId(configNodeData.getNodeDataId());

			if (isEnable) {
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(evtConfigUnit);
				removeOrUpdateDataGOfEventProducerForBeanConfig(evtConfigUnit);

			} else {
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				String eventGroupKey = EventFrameworkConfigurationUnit
						.getEventConfigGroupKey(configNodeData.getParentConfigNodeId());

				configServer.deleteConfiguration(configContext.getTenantId(), eventGroupKey, eventId);

				removeOrUpdateDataGOfEventProducerForBeanConfig(evtConfigUnit);

			}

		} catch (InvalidNodeTreeException | ConfigPersistenceException | EventFrameworkConfigParserException
				| ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Error in changing the status EventConfiguration with eventId=" + eventId, e);
		}

		return true;
	}

	/**
	 * to delete event configuration from both DB and Cache , first Check in
	 * Data Gird if not exist delete in Db only else delete in both Data Grids
	 * and Db
	 * 
	 * @param configContext
	 * @param eventId
	 * @return
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean deleteEventConfiguration(ConfigurationContext configContext, String eventId)
			throws EventFrameworkConfigurationException {
		boolean isDeleted = false;
		logger.debug("inside deleteEventConfiguration method with EventId = " + eventId);
		try {
			Event evnt = getEventConfiguration(configContext, eventId);
			int nodeId = getApplicableNodeId(configContext);
			logger.debug("nodeId found is === " + nodeId + " and evnt configData in cache = " + evnt);
			if (evnt == null) {
				// delete from DB
				isDeleted = deleteEventFrameworkConfigurationFromDB(configContext, eventId, nodeId);
				return isDeleted;

			}

			LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
			String eventGroupKey = EventFrameworkConfigurationUnit.getEventConfigGroupKey(nodeId);
			IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) leapConfigurationServer
					.getConfiguration(configContext.getTenantId(), eventGroupKey, eventId);
			isDeleted = iConfigPersistenceService.deleteConfigNodeData(evtFwkConfigUnit.getDbconfigId());
			leapConfigurationServer.deleteConfiguration(configContext.getTenantId(), eventGroupKey, eventId);
			removeEventProducerDataFromDataGrid(evtFwkConfigUnit);
		} catch (EventFrameworkConfigurationException | InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Error in deleting EventConfiguration  with event ID=" + eventId, e);
		}

		return isDeleted;
	}

	private void removeOrUpdateDataGOfEventProducerForBeanConfig(EventFrameworkConfigurationUnit evfwkConfigUnit) {

		logger.debug(
				"inside removeOrUpdateDataGOfEventProducerForBeanConfig method with EventFrameworkConfigurationUnit="
						+ evfwkConfigUnit);
		if (evfwkConfigUnit.getIsEnabled()) {
			updateDataGridForEventProducer(evfwkConfigUnit);
		} else {
			removeEventProducerDataFromDataGrid(evfwkConfigUnit);
		}

	}

	private void removeEventProducerDataFromDataGrid(EventFrameworkConfigurationUnit evfwkConfigUnit) {
		Event evtFwkConfig = (Event) evfwkConfigUnit.getConfigData();
		Integer attachedNodeId = evfwkConfigUnit.getAttachedNodeId();
		CamelEventProducer evtProducer = evtFwkConfig.getCamelEventProducer();
		CamelProducerConfig producerConfig = evtProducer.getCamelProducerConfig();
		String beanName = producerConfig.getComponent();
		String serviceName = producerConfig.getServiceName();
		String eventId = evtFwkConfig.getId();
		String raiseoN = producerConfig.getRaiseOn();// "success";

		boolean isBeanEvent = false;
		if (beanName != null)
			isBeanEvent = true;
		// else its service event
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		if (isBeanEvent) {
			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForBeanGroupKey(attachedNodeId));

			// remove beanCompennt service event from Data Grid
			if (map != null) {
				map.remove(producerConfig.getComponent() + "-" + serviceName);
			}
		} else {

			IMap<String, String> map = hazelcastInstance
					.getMap(EventFrameworkConfigurationUnit.getEventProcucerForServiceGroupKey(attachedNodeId));
			String key = serviceName + "-" + raiseoN;
			String eventListing = map.get(key);
			// remove events from service map of Data Grid
			if (eventListing != null) {
				StringBuilder stringBuilder = new StringBuilder(eventListing);
				if (eventListing.contains(eventId)) {
					int startIndex = eventListing.indexOf(eventId);
					int endIndex = eventListing.lastIndexOf(eventId);

					if (startIndex == 0) {
						stringBuilder.delete(startIndex, endIndex);
					} else {
						stringBuilder.delete(startIndex - 1, endIndex);

					}
					eventListing = stringBuilder.toString();
					logger.debug("list of service event ids after removing eventId=" + eventId, eventListing);

					if (eventListing.isEmpty()) {
						map.put(key, eventListing);

					} else {
						map.remove(key);

					}
				}

			}

		} // end of out

	}

	/**
	 * delete EventFrameworkConfigurationFromDB from DB
	 * 
	 * @param configContext
	 * @param eventConfigName
	 * @param nodeId
	 * @return boolean value true|false
	 * @throws EventFrameworkConfigurationException
	 */
	private boolean deleteEventFrameworkConfigurationFromDB(ConfigurationContext configContext, String eventConfigName,
			int nodeId) throws EventFrameworkConfigurationException {

		IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		try {

			int isDeleted = iConfigPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(eventConfigName,
					nodeId);
			if (isDeleted == 1)
				return true;
		} catch (ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException(
					"Error in deleting EventFrameworkconfigaration in DB with event ID=" + eventConfigName, e);
		}

		return false;
	}

	/**
	 * This method is used to add configuration for event subscriber
	 * 
	 * @param configContext
	 *            : ConfigurationContext object
	 * @param eventSubscriptionConfig
	 *            : EventSubscription object
	 * @throws EventFrameworkConfigurationException
	 */
	public void addEventFrameworkConfiguration(ConfigurationContext configContext,
			SubscribeEvent eventSubscriptionConfig) throws EventFrameworkConfigurationException {
		logger.debug(".addEventFrameworkConfiguration method for EventSubscription ");
		Integer configNodeId;
		try {
			// subscription at tenant and site level
			ConfigurationContext context = new ConfigurationContext(configContext.getTenantId(),
					configContext.getSiteId(), null, null, null);
			configNodeId = getApplicableNodeId(context);
			String evtSubscriptionFwkXMLStr = parser.unmarshallObjecttoXML(eventSubscriptionConfig);
			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId,
					eventSubscriptionConfig.getSubscriptionId(), evtSubscriptionFwkXMLStr,
					EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
			configNodeData.setEnabled(eventSubscriptionConfig.isIsEnabled());
			configNodeData.setConfigLoadStatus("Success");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, eventSubscriptionConfig.getSubscriptionId(),
					EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				// build configuration unit to cache.
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configContext.getTenantId(), configContext.getSiteId(), configNodeId,
						eventSubscriptionConfig.isIsEnabled(), eventSubscriptionConfig);
				evtConfigUnit.setDbconfigId(configDataId);
				loadConfigurationInDataGrid(evtConfigUnit);
				loadTopicNamesInDataGrid(eventSubscriptionConfig);
				loadSubscribersByTopicNamesInDataGrid(eventSubscriptionConfig);
			} else {
				logger.debug("event subscrition with subscription Id : " + eventSubscriptionConfig.getSubscriptionId()
						+ " failed ! ");
				throw new EventFrameworkConfigurationException("event subscrition for the event : "
						+ eventSubscriptionConfig.getSubscriptionId() + " failed because event doesnot exist ");
			}
			// }
		} catch (InvalidNodeTreeException | ConfigPersistenceException | EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to add EventSubscriptionConfiguration for subscription Id "
							+ eventSubscriptionConfig.getSubscriptionId(),
					e);
		}

	}

	private void loadSubscribersByTopicNamesInDataGrid(SubscribeEvent eventSubscriptionConfig) {
		logger.debug(".loadSubscribersByTopicNamesInDataGrid() SubscribeEvent=" + eventSubscriptionConfig);
		try {
			// we upload in cache only when enabled
			if (eventSubscriptionConfig.isIsEnabled()) {
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				String subscribeTopics = eventSubscriptionConfig.getSubscribeTo();
				if (subscribeTopics != null && !subscribeTopics.isEmpty())
					configServer.loadSubscribersByTopicName(eventSubscriptionConfig.getSubscriptionId(),
							subscribeTopics);
			}
		} catch (ConfigServerInitializationException e) {
			logger.error("subscription failed to load in datagrid  topic names for subscribeId :"
					+ eventSubscriptionConfig.getSubscriptionId());
		}

	}

	private void loadTopicNamesInDataGrid(SubscribeEvent eventSubscriptionConfig) {
		logger.debug(".loadTopicNamesInDataGrid() SubscribeEvent=" + eventSubscriptionConfig);
		try {
			// we upload in cache only when enabled
			if (eventSubscriptionConfig.isIsEnabled()) {
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				String subscribeTopics = eventSubscriptionConfig.getSubscribeTo();
				if (subscribeTopics != null && !subscribeTopics.isEmpty())
					configServer.loadSubscriberTopicBySubscribeId(eventSubscriptionConfig.getSubscriptionId(),
							subscribeTopics);
			}
		} catch (ConfigServerInitializationException e) {
			logger.error("subscription failed to load in datagrid  topic names for subscribeId :"
					+ eventSubscriptionConfig.getSubscriptionId());
		}

	}

	/**
	 * This is the method used to get the event subscription configuration
	 * 
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @param eventSubscriptionId
	 *            :eventSubscriptionId
	 * @return EventSubscription Object
	 * @throws EventFrameworkConfigurationException
	 */
	public SubscribeEvent getEventSubscriptionConfiguration(ConfigurationContext configContext,
			String eventSubscriptionId) throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			SubscribeEvent eventSubscription = null;
			int searchStartLevel = getContextLevel(configContext);
			logger.debug(".getEventSubscriptionConfiguration() Search Level is =" + searchStartLevel);
			for (int i = searchStartLevel; i > 1; i--) {
				prepareConfigContextForSearchLevel(configContext, i);
				Integer nodeId = getApplicableNodeId(configContext);
				logger.debug("nodeId of event subscription : " + nodeId);
				String eventSubscriptionGroupKey = EventFrameworkConfigurationUnit
						.getEventSubscriptionConfigGroupKey(nodeId);
				EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) configServer
						.getConfiguration(configContext.getTenantId(), eventSubscriptionGroupKey, eventSubscriptionId);
				logger.debug(".getEventSubscriptionConfiguration() searching at level " + i + " ConfigUnit="
						+ evtFwkConfigUnit);
				if (evtFwkConfigUnit != null) {
					eventSubscription = (SubscribeEvent) evtFwkConfigUnit.getConfigData();
					return eventSubscription;
				} // return null if not found
			}

		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to getEventSubscriptionConfiguration for eventId{" + eventSubscriptionId + "}", e);
		}

		return null;

	}

	/**
	 * gives you all the enabled subscribers for the particular topic.
	 * 
	 * @param topicName
	 * @return ListofSubscribers
	 * @throws EventFrameworkConfigurationException
	 */
	public Set<String> getAllTopicSubscribersbyTopicName(String topicName) throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			return configServer.getAllTopicSubscribers(topicName);
		} catch (ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Failed to getAllTopicSubscribersbyTopicName for topicName{" + topicName + "}", e);
		} // TODO Auto-generated method stub
	}

	/**
	 * gets you all the topics to be subscribed.
	 * 
	 * @return topicNames
	 * @throws EventFrameworkConfigurationException
	 */
	public String getAllSubscriberTopicNames() throws EventFrameworkConfigurationException {
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			return configServer.getAllSubscriberTopic();
		} catch (ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException("Failed to getAllSubscriberTopicNames ", e);
		} // TODO Auto-generated method stub
	}

	/**
	 * This method is used to change the status of specific event subscription
	 * 
	 * @param configurationContext
	 *            : ConfigurationContext Object
	 * @param subscriptionEventId
	 *            : event id in string for which it is subscribing
	 * @param isEnable
	 *            : boolean value
	 * @return boolean
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean changeStatusOfEventSubscriptionConfiguraion(ConfigurationContext configurationContext,
			String subscriptionEventId, boolean isEnable) throws EventFrameworkConfigurationException {
		logger.debug(".changeStatusOfEventSubscriptionConfiguraion method of EventFramewrkHelper");
		Integer applicableNodeId;
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();

			applicableNodeId = getApplicableNodeId(configurationContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					subscriptionEventId, EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
			if (configNodeData == null) {
				// config doesn't exist in DB
				throw new EventFrameworkConfigurationException("EventFrameworkConfiguration with subscriptionEventId="
						+ subscriptionEventId + " Doesnt Exist in DB ");
			}

			configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
			EventFramework eventFramework = convertEventFrameworkXmlStringToObject(configNodeData.getConfigData());
			SubscribeEvent eventSubscription = eventFramework.getEventSubscription().getSubscribeEvent().get(0);
			eventSubscription.setIsEnabled(isEnable);
			String updatedEventFrameworkString = convertEventFrameworkObjectToString(eventSubscription);
			boolean updateConfiData = configPersistenceService.updateConfigdataInConfigNodeData(
					updatedEventFrameworkString, applicableNodeId, eventSubscription.getSubscriptionId(),
					EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);

			logger.debug("subscription updated in db for SubscriptionId : " + eventSubscription.getSubscriptionId()
					+ updateConfiData);

			if (!isEnable) {
				logger.debug("isEnabled value is false ");
				String eventSubscriptionGroupKey = EventFrameworkConfigurationUnit
						.getEventSubscriptionConfigGroupKey(configNodeData.getParentConfigNodeId());
				configServer.deleteConfiguration(configurationContext.getTenantId(), eventSubscriptionGroupKey,
						subscriptionEventId);
				return true;
			} else {
				logger.debug("isEnabled value is true ");
				// build configuration unit to cache.
				EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
						configurationContext.getTenantId(), configurationContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), isEnable, eventSubscription);
				evtConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(evtConfigUnit);
				return true;
			}
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException
				| EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Error in loading DB data to cache with subscriptionId=" + subscriptionEventId);
		}
	}

	/**
	 * This method is used to change the status of specific subscriber
	 * 
	 * @param configurationContext
	 *            : ConfigurationContext Object
	 * @param subscriptionEventId
	 *            : subscriptionEventId in string
	 * @param subsciberId
	 *            : subsciberId id whose status need to change
	 * @param isEnable
	 *            : boolean value
	 * @return boolean
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean changeStatusOfEventSubscriber(ConfigurationContext configurationContext, String subscriptionEventId,
			String subsciberId, boolean isEnable) throws EventFrameworkConfigurationException {
		Integer applicableNodeId;
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();

			applicableNodeId = getApplicableNodeId(configurationContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					subscriptionEventId, EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
			if (configNodeData == null) {
				// config not exist in DB
				throw new EventFrameworkConfigurationException("EventFrameworkConfiguration with subscriptionEventId="
						+ subscriptionEventId + " Doesnt Exist in DB ");
			}
			// build configuration unit to cache.
			configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
			EventFramework eventFramework = convertEventFrameworkXmlStringToObject(configNodeData.getConfigData());
			SubscribeEvent subscriberEvent = eventFramework.getEventSubscription().getSubscribeEvent().get(0);
			if (subscriberEvent.getSubscriptionId().equalsIgnoreCase(subsciberId)) {
				subscriberEvent.setIsEnabled(isEnable);
			}
			String updatedEventFrameworkString = convertEventFrameworkObjectToString(subscriberEvent);
			boolean updateConfiData = configPersistenceService.updateConfigdataInConfigNodeData(
					updatedEventFrameworkString, applicableNodeId, subscriptionEventId,
					EventFrameworkConstants.EF_EVENTSUBSCRIPTION_CONFIG_TYPE);
			EventFrameworkConfigurationUnit evtConfigUnit = new EventFrameworkConfigurationUnit(
					configurationContext.getTenantId(), configurationContext.getSiteId(),
					configNodeData.getParentConfigNodeId(), isEnable, subscriberEvent);
			evtConfigUnit.setDbconfigId(configNodeData.getNodeDataId());

			loadConfigurationInDataGrid(evtConfigUnit);
			return true;

		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException
				| EventFrameworkConfigParserException e) {
			throw new EventFrameworkConfigurationException(
					"Error in loading DB data to cache with subscriptionEventId=" + subscriptionEventId);
		}
	}

	/**
	 * To deleteEventSubscriptionConfiguration by checking in Data Grid if Exist
	 * delete in DB and data grid both else delete in DB only
	 * 
	 * @param configContext
	 *            : ConfigurationContext Object
	 * @param eventSubscriptionId
	 *            : subscription Id need to delete
	 * @return boolean value True|false
	 * @throws EventFrameworkConfigurationException
	 */
	public boolean deleteEventSubscriptionConfiguration(ConfigurationContext configContext, String eventSubscriptionId)
			throws EventFrameworkConfigurationException {

		boolean isDeleted = false;
		logger.debug(
				"inside deleteEventSubscriptionConfiguration method with eventSubscriptionId = " + eventSubscriptionId);
		try {
			SubscribeEvent eventSubscription = getEventSubscriptionConfiguration(configContext, eventSubscriptionId);
			int nodeId = getApplicableNodeId(configContext);
			logger.debug("nodeId found is === " + nodeId + " and EventSubscription configData in cache = "
					+ eventSubscriptionId);
			if (eventSubscription == null) {
				// delete from DB
				isDeleted = deleteEventFrameworkConfigurationFromDB(configContext, eventSubscriptionId, nodeId);
				return isDeleted;

			}

			LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
			String eventGroupKey = EventFrameworkConfigurationUnit.getEventSubscriptionConfigGroupKey(nodeId);
			IConfigPersistenceService iConfigPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			EventFrameworkConfigurationUnit evtFwkConfigUnit = (EventFrameworkConfigurationUnit) leapConfigurationServer
					.getConfiguration(configContext.getTenantId(), eventGroupKey, eventSubscriptionId);
			isDeleted = iConfigPersistenceService.deleteConfigNodeData(evtFwkConfigUnit.getDbconfigId());
			leapConfigurationServer.deleteConfiguration(configContext.getTenantId(), eventGroupKey,
					eventSubscriptionId);

		} catch (EventFrameworkConfigurationException | InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException e) {
			throw new EventFrameworkConfigurationException(
					"Error in deleting EventSubscriptionConfiguration  with event ID=" + eventSubscriptionId, e);
		}

		return isDeleted;
	}

	/**
	 * /** Based on Tenant,Site,FeatureGroup,Feature finds the applicable NodeId
	 * to Tag PermaStoreConfiguration <BR>
	 * Note :- 1.) Does not support tagging of Event above Site<br>
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @param featureName
	 * @return
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	public Integer getApplicableNodeId(ConfigurationContext configContext)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();
		String implname = configContext.getImplementationName();
		logger.debug("Finding ParentNodeId for Tenant=" + tenantId + "-siteId=" + siteId + "-featureGroup="
				+ featureGroup + "-featureName=" + featureName + " -implName : " + implname);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		if (featureName == null && featureGroup == null) {
			// if featureName and feature group are null than we want to tag it
			// to a Site
			return configPersistenceService.getApplicableNodeId(tenantId, siteId);
		}
		return configPersistenceService.getApplicableNodeId(tenantId, siteId, featureGroup, featureName, implname, null,
				null);
	}

	/**
	 * This method is used to convert EventFramework xml string into Object
	 * 
	 * @param eventxmlString
	 *            : eventframework in String type
	 * @return EventFramework
	 * @throws EventFrameworkConfigParserException
	 */
	private EventFramework convertEventFrameworkXmlStringToObject(String eventxmlString)
			throws EventFrameworkConfigParserException {
		logger.debug("inside convertEventFrameworkXmlStringToObject of EventFrameworkConfigHelper");
		EventFrameworkXmlHandler eventFrameworkXmlHandler = new EventFrameworkXmlHandler();
		EventFramework evkfkConfigs = eventFrameworkXmlHandler.marshallXMLtoObject(eventxmlString);
		return evkfkConfigs;

	}

	/**
	 * This method is used to convert EventFramework xml string into Object
	 * 
	 * @param eventxmlString
	 *            : eventframework in String type
	 * @return EventFramework
	 * @throws EventFrameworkConfigParserException
	 */
	private String convertEventFrameworkObjectToString(SubscribeEvent eventSubscription)
			throws EventFrameworkConfigParserException {
		logger.debug("inside convertEventFrameworkXmlStringToObject of EventFrameworkConfigHelper");
		EventFrameworkXmlHandler eventFrameworkXmlHandler = new EventFrameworkXmlHandler();
		String evkfkConfigs = eventFrameworkXmlHandler.unmarshallObjecttoXML(eventSubscription);
		return evkfkConfigs;

	}

	private int getContextLevel(ConfigurationContext configContext) {
		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();

		if (tenantId == null || siteId == null) {
			return 0;
		} else if (featureGroup == null && featureName == null) {
			return 2;
		} else if (featureGroup != null && featureName == null) {
			return 3;
		} else {
			return 4;
		}
	}

	private void prepareConfigContextForSearchLevel(ConfigurationContext configContext, int level) {
		if (level == 3) {
			configContext.setFeatureName(null);
		} else if (level == 2) {
			configContext.setFeatureName(null);
			configContext.setFeatureGroup(null);
		}
	}

}
