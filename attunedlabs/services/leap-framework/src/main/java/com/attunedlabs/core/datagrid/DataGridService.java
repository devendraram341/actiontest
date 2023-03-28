package com.attunedlabs.core.datagrid;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.config.ConfigurationConstant.BASE_CONFIG_PATH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.core.datagrid.listener.ClusterMigrationListener;
import com.attunedlabs.core.datagrid.listener.ConsoleLoggingPartitionLostListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.PartitionService;
import com.hazelcast.durableexecutor.DurableExecutorService;

/**
 * This class is just a wrapper around Hazelcast instance
 * 
 * @author Bizruntime
 *
 */
public class DataGridService {
	protected static final Logger logger = LoggerFactory.getLogger(DataGridService.class);
	public static final String PROCESSOR_EXECUTOR_KEY = "PROC";
	public static final String DISPATCHER_EXECUTOR_KEY = "DISP";
	public static final String BUILDER_EXECUTOR_KEY = "BUILDER";
	public static final String SYSEVENT_DISPATCHER_KEY = "SYSEVTDIS";
	public static final String HAZELCAST_LOCAL_CONFIG_XML_KEY = "hazelcast.xml";
	public static final String HAZELCAST_PAAS_CONFIG_XML_KEY = "hazelcast-paas.xml";

	private HazelcastInstance hazelcastInstance;
	private static DataGridService hcService;

	private DataGridService(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	/**
	 * This method is used to created single object of hazelcast
	 * 
	 * @return
	 * @throws PropertiesConfigException
	 * 
	 */
	public static DataGridService getDataGridInstance() {
		String methodName = "getDataGridInstance";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (hcService == null) {
			synchronized (DataGridService.class) {
				String hazelCastFile = System.getProperty(BASE_CONFIG_PATH).concat(File.separator)
						.concat(HAZELCAST_LOCAL_CONFIG_XML_KEY);
				try {
					Config config = new FileSystemXmlConfig(hazelCastFile);
					config.setClassLoader(ConfigurationTreeNode.class.getClassLoader());
					HazelcastInstance hcInstance = Hazelcast.newHazelcastInstance(config);
					hcInstance.getPartitionService()
							.addPartitionLostListener(new ConsoleLoggingPartitionLostListener());
					PartitionService partitionService = hcInstance.getPartitionService();
					partitionService.addMigrationListener(new ClusterMigrationListener());
					hcService = new DataGridService(hcInstance);
				} catch (FileNotFoundException e) {
					logger.warn("{} Please check the hazelcast.xml file location::{}", LEAP_LOG_KEY, e.getMessage());
					throw new RuntimeException("Please check the hazelcast.xml file location at::" + e.getMessage());
				}
			} // end of sync block
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return hcService;
	}

	/**
	 * This method is to return hazelcast Instance object
	 * 
	 * @return HazelcastInstance
	 */
	public HazelcastInstance getHazelcastInstance() {

		return hazelcastInstance;

	}

	public IExecutorService getDispatcherExecutor(String tenantId) {
		String verifiedTenant = verifyTenant(tenantId);
		IExecutorService despExecutor = hazelcastInstance
				.getExecutorService(verifiedTenant + "-" + DISPATCHER_EXECUTOR_KEY);
		return despExecutor;
	}

	public IExecutorService getEventBuilderExecutor(String tenantId) {
		String verifiedTenant = verifyTenant(tenantId);
		IExecutorService builderExecutor = hazelcastInstance
				.getExecutorService(verifiedTenant + "-" + BUILDER_EXECUTOR_KEY);
		return builderExecutor;
	}

	public IExecutorService getSystemEventDispatcherExecutor(String tenantId) {
		String verifiedTenant = verifyTenant(tenantId);
		IExecutorService builderExecutor = hazelcastInstance
				.getExecutorService(verifiedTenant + "-" + SYSEVENT_DISPATCHER_KEY);
		return builderExecutor;
	}

	public DurableExecutorService getDurableDispatcherExecutor(String tenantId) {
		String verifiedTenant = verifyTenant(tenantId);
		DurableExecutorService builderExecutor = hazelcastInstance
				.getDurableExecutorService(verifiedTenant + "-" + SYSEVENT_DISPATCHER_KEY);
		return builderExecutor;
	}

	/**
	 * This method is to add configuration Listener
	 * 
	 * @param tenantId    : String
	 * @param configGroup : String
	 * @param listener    : LeapConfigurationListener
	 */
	public void addConfigListener(String groupKey, ConfigurationTreeNodeListener listener) {
		IMap<String, Serializable> configGroupMap = hazelcastInstance.getMap(groupKey);
		if (configGroupMap != null) {
			configGroupMap.addEntryListener(listener, true);
		}
	}

	public Long getClusterUniqueId(String idTypeName) {
		IdGenerator idGen = hazelcastInstance.getIdGenerator(idTypeName);
		Long id = idGen.newId();
		return id;
	}

	private String verifyTenant(String tenantId) {
		if (tenantId == null || tenantId.isEmpty())
			return "default";
		return tenantId.trim();
	}

}
