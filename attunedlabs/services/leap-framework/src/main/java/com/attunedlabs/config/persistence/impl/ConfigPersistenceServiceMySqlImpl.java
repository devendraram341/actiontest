package com.attunedlabs.config.persistence.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigNode;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.dao.ConfigFeatureMasterDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.dao.FeatureDeploymentDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.persistence.exception.FeatureDeploymentConfigurationException;
import com.attunedlabs.config.persistence.exception.FeatureMasterConfigurationException;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuremaster.FeatureMaster;
import com.attunedlabs.scheduler.ScheduledJobData;

public class ConfigPersistenceServiceMySqlImpl implements IConfigPersistenceService {
	final Logger logger = LoggerFactory.getLogger(ConfigPersistenceServiceMySqlImpl.class);
	private ConfigNodeDAO configNodeDAO;
	private ConfigNodeDataDAO configDataDao;
	private ITenantConfigTreeService tenantConfigTreeService;
	private ConfigFeatureMasterDAO configFeatureMasterDao;
	private FeatureDeploymentDAO featureDeploymentDAO;

	public ConfigPersistenceServiceMySqlImpl() {
		configNodeDAO = new ConfigNodeDAO();
		configDataDao = new ConfigNodeDataDAO();
		tenantConfigTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		configFeatureMasterDao = new ConfigFeatureMasterDAO();
		featureDeploymentDAO = new FeatureDeploymentDAO();
	}

	public ConfigurationTreeNode getConfigPolicyNodeTree() throws ConfigPersistenceException {
		String methodName = "#";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationTreeNode configTreeNode = tenantConfigTreeService.getAllConfigTreeNode();
		if (configTreeNode == null) {
			logger.debug("{} Configuration tree Node is null", LEAP_LOG_KEY);
			// DataGrid not initialized
			ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();
			try {
				configTreeNode = configNodeDAO.getNodeTree();
				logger.debug("{} configTreeNode :{} ", LEAP_LOG_KEY,configTreeNode);
				tenantConfigTreeService.initialize(configTreeNode);
			} catch (ConfigNodeConfigurationException sqlexp) {
				throw new ConfigPersistenceException("Failed to get node tree", sqlexp);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configTreeNode;
	}

	public String getConfigPolicyNodeTreeAsJson() throws ConfigPersistenceException {
		ConfigurationTreeNode treeNode = getConfigPolicyNodeTree();

		StringBuffer jsonBuffer = new StringBuffer();
		treeNode.getConfigTreeNodeAsJSONString(jsonBuffer);
		return jsonBuffer.toString();
	}

	public int insertConfigNode(ConfigNode node) throws ConfigPersistenceException {
		try {
			int genNodeId = configNodeDAO.insertConfigNode(node);
			node.setNodeId(genNodeId);
			ConfigurationTreeNode configTreeNode = buildConfigTreeNode(node);
			// Add to DataGrid Representation of this Tree
			tenantConfigTreeService.addConfigurationTreeNode(configTreeNode);
			logger.debug("{} insertConfigNode()--Generated NodeId is={}", LEAP_LOG_KEY, genNodeId);
			return genNodeId;
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNode with ConfigNode=" + node, sqlexp);
		}

	}

	public int insertConfigNodeWithVersion(ConfigurationTreeNode node) throws ConfigPersistenceException {
		try {
			int genNodeId = configNodeDAO.insertConfigNodeWithVersion(node);
			node.setNodeId(genNodeId);

			// Add to DataGrid Representation of this Tree
			tenantConfigTreeService.addConfigurationTreeNode(node);
			logger.debug("{} insertConfigNode()--Generated NodeId is={}" , LEAP_LOG_KEY, genNodeId);
			return genNodeId;
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNode with ConfigNode=" + node, sqlexp);
		}

	}

	@Override
	public int updateConfigNodeWithPrimaryFeatureId(int nodeId, int primaryFeatureId)
			throws ConfigPersistenceException {

		try {
			new ConfigNodeDAO().updateNodeWithPrimaryFeatureId(nodeId, primaryFeatureId);
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Error in Updating primaryFeatureId in configNode with nodeId : "
					+ nodeId + " primaryFeatureId : " + primaryFeatureId, sqlexp);

		}
		return 0;
	}

	public List<ConfigNode> getChildNodes(Integer parentNodeId) throws ConfigPersistenceException {
		List<ConfigNode> childNodeList = null;
		try {
			configNodeDAO = new ConfigNodeDAO();
			childNodeList = configNodeDAO.getChildNodes(parentNodeId);
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Child Nodes parentNodeId=" + parentNodeId, sqlexp);
		}

		return childNodeList;
	}

	public ConfigNode getNodeById(Integer nodeId) throws ConfigPersistenceException {
		// configNodeDAO = new ConfigNodeDAO();
		ConfigNode configNode = null;
		try {
			configNode = configNodeDAO.getNodeById(nodeId);
		} catch (ConfigNodeConfigurationException sqlexp) {

			throw new ConfigPersistenceException("Failed to get Node by Id=" + nodeId, sqlexp);
		}
		return configNode;
	}

	public boolean deleteNodeByNodeId(Integer nodeId) throws ConfigPersistenceException {
		// Delete the node Next
		try {
			ConfigNode configNode = configNodeDAO.getNodeById(nodeId);
			int numRows = configNodeDAO.deleteNodeByNodeId(nodeId);
			// Remove it from the cacahe
			ConfigurationTreeNode configTreeNode = buildConfigTreeNode(configNode);
			tenantConfigTreeService.deleteConfigurationTreeNode(configTreeNode);
			if (numRows > 0)
				return true;
		} catch (ConfigNodeConfigurationException sqlexp) {

			throw new ConfigPersistenceException("Failed to Delete ConfigNode by Id=" + nodeId, sqlexp);
		}
		return false;
	}

	private ConfigurationTreeNode buildConfigTreeNode(ConfigNode node) {
		ConfigurationTreeNode nodeTree = new ConfigurationTreeNode();
		nodeTree.setNodeId(node.getNodeId());
		nodeTree.setLevel(node.getLevel());
		nodeTree.setNodeName(node.getNodeName());
		nodeTree.setParentNodeId(node.getParentNodeId());
		nodeTree.setType(node.getType());
		return nodeTree;
	}

	// -----------------------------TenantConfigTree---
	public ConfigurationTreeNode getConfigTreeNodeForFeatureGroup(String tenantName, String siteName,
			String featureGroup) throws ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		ConfigurationTreeNode configtreeNode = tenantConfigTreeService.getConfigTreeNodeForFeatureGroup(tenantName,
				siteName, featureGroup);

		return configtreeNode;
	}

	public ConfigurationTreeNode getConfigTreeNodeForTenantById(Integer tenantId) throws ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		ConfigurationTreeNode tenantConfigNodeTree = tenantConfigTreeService.getConfigTreeNodeForTenantById(tenantId);
		return tenantConfigNodeTree;
	}

	public Integer getApplicableNodeId(String tenantId, String siteId, String featureGroup, String featureName,
			String implName, String vendorName, String version)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			logger.debug("{} config tree is not initialiazed", LEAP_LOG_KEY);
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId, siteId, featureGroup,
				featureName, implName, vendorName, version);
		logger.debug("{} applicable node Id ->: {}", LEAP_LOG_KEY, applicableNodeId);
		return applicableNodeId;
	}

	@Override
	public Integer getApplicableNodeId(String tenantId, String siteId)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId, siteId);

		return applicableNodeId;
	}

	@Override
	public Integer getApplicableNodeId(String tenantId) throws InvalidNodeTreeException, ConfigPersistenceException {
		if (!tenantConfigTreeService.isInitialized()) {
			getConfigPolicyNodeTree();
		}
		Integer applicableNodeId = tenantConfigTreeService.getApplicableNodeId(tenantId);

		return applicableNodeId;
	}

	// ------Node Data calls------
	public List<ConfigNodeData> getConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		List<ConfigNodeData> nodeDataList = null;
		try {
			nodeDataList = configDataDao.getConfigNodeDataByNodeId(nodeId);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Config Node data by  Node Id=" + nodeId, sqlexp);
		}
		return nodeDataList;
	}

	public ConfigNodeData getConfigNodeDatabyNameAndNodeId(Integer nodeId, String configName, String configType)
			throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.getConfigNodeDatabyNameAndNodeId(nodeId, configName, configType);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException(
					"Failed to get Config Node data by Name=" + configName + " and Node Id=" + nodeId, sqlexp);
		}
	}

	public boolean updateConfigdataInConfigNodeData(String xmlString, Integer nodeId, String configName,
			String configType) throws ConfigPersistenceException {
		boolean configdataSuccessUpdate;
		try {
			configDataDao.updateConfigDataByNameAndNodeId(xmlString, nodeId, configName, configType);
			configdataSuccessUpdate = true;
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Node by Id=" + nodeId, sqlexp);
		}

		return configdataSuccessUpdate;
	}

	public boolean deleteConfigNodeData(Integer configNodeDataId) throws ConfigPersistenceException {
		try {
			configDataDao = new ConfigNodeDataDAO();
			int numofRows = configDataDao.deleteConfigNodeData(configNodeDataId);
			if (numofRows > 0)
				return true;
			return false;
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException(
					"Failed to delete ConfigNodeData with configNodeDataId=" + configNodeDataId, sqlexp);
		}
	}

	public int deleteConfigNodeDataByNodeId(Integer nodeId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		int numofRows;
		try {
			numofRows = configDataDao.deleteConfigNodeDataByNodeId(nodeId);
			return numofRows;
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to delete ConfigNodeData with nodeId=" + nodeId, sqlexp);
		}

	}

	public int insertConfigNodeData(ConfigNodeData nodeData) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.insertConfigNodeData(nodeData);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeId="
					+ nodeData.getParentConfigNodeId() + ",configName=" + nodeData.getConfigName(), sqlexp);
		}
	}

	public void enableConfigNodeData(boolean setEnable, Integer nodeDataId) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			configDataDao.enableConfigNodeData(setEnable, nodeDataId);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to enable/disable nodeDataId=" + nodeDataId, sqlexp);
		}
	}

	public List<ConfigNodeData> getConfigNodeDataByNodeIdAndByType(Integer nodeId, String type)
			throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		List<ConfigNodeData> nodeDataList = null;
		try {
			nodeDataList = configDataDao.getConfigNodeDataByNodeIdByConfigType(nodeId, type);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Config Node data by  Node Id=" + nodeId, sqlexp);
		}
		return nodeDataList;
	}

	/**
	 * @param configName
	 * @param nodeId
	 * 
	 */
	public int deleteConfigNodeDataByNodeIdAndConfigName(String configName, int nodeId)
			throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		int numofRows = 0;

		try {
			numofRows = configDataDao.deleteConfigNodeDataByNodeIdAndByConfigName(configName, nodeId);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			// TODO Auto-generated catch block
			throw new ConfigPersistenceException(
					"Failed to delete ConfigNodeData with nodeId=" + nodeId + " and configName=" + configName, sqlexp);
		}
		return numofRows;
	}

	@Override
	public int getNodeIdByNodeNameAndByTypeNotWithGivenNodeId(String nodeName, String type, int parentNodeId,
			int updatingNodeId) throws ConfigPersistenceException {
		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByTypeNotNodeId(nodeName, type, parentNodeId, updatingNodeId);
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeName=" + nodeName + ",type="
					+ type + ", parentnode id= " + parentNodeId + ", updateNodeID= " + updatingNodeId, sqlexp);
		}

		return nodeId;
	}

	public int updateConfigNodeData(ConfigNodeData nodeData) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.updateConfigNodeData(nodeData);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeId="
					+ nodeData.getParentConfigNodeId() + ",configName=" + nodeData.getConfigName(), sqlexp);
		}
	}

	/**
	 * getting of nodeId from DB by nodeName , nodeType and parentNodeId
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @return int
	 * @throws ConfigPersistenceException
	 * @throws SQLException
	 */
	public int getNodeIdByNodeNameAndByType(String nodeName, String type, int parentNodeId)
			throws ConfigPersistenceException {

		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(nodeName, type, parentNodeId);
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert ConfigNodeData with nodeName=" + nodeName + ",type="
					+ type + ", parentnode id= " + parentNodeId, sqlexp);
		}
		return nodeId;
	}

	@Override
	public int getNodeIDByNameAndType(String nodename, String nodeType) throws ConfigPersistenceException {
		int nodeId = 0;
		try {
			nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(nodename, nodeType);
		} catch (ConfigNodeConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get Node by Name=" + nodename + " Type = " + nodeType,
					sqlexp);
		}
		return nodeId;
	}

	public int getFeatureMasterIdByFeatureAndFeaturegroup(String featureName, String featureGroup, String version,
			int siteId) throws ConfigPersistenceException {

		int featureMasterNodeId = 0;
		try {
			featureMasterNodeId = configFeatureMasterDao.getFeatureMasterIdByFeatureAndFeaturegroup(featureName,
					featureGroup, version, siteId);
			logger.debug("{} featureMasterNodeId : {}" , LEAP_LOG_KEY, featureMasterNodeId);
		} catch (FeatureMasterConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get featureMasterNodeId  by featureName=" + featureName
					+ " featureGroup = " + featureGroup, sqlexp);
		}
		return featureMasterNodeId;
	}

	@Override
	public boolean insertFeatureMasterDetails(FeatureMaster featureMaster) throws ConfigPersistenceException {
		try {
			configFeatureMasterDao.insertFeatureMasterDetails(featureMaster);
			return true;
		} catch (FeatureMasterConfigurationException e) {
			throw new ConfigPersistenceException(
					"Failed to insert featureMaster Details for give featureMaster : " + featureMaster, e);

		}
	}

	@Override
	public boolean deleteFeatureMasterDetails(String featureName, int siteId) throws ConfigPersistenceException {
		try {
			configFeatureMasterDao.deleteFeatureMasterDetails(featureName, siteId);
			return true;
		} catch (FeatureMasterConfigurationException e) {
			throw new ConfigPersistenceException("Failed to delete featureMaster Details From Db  : " + featureName, e);

		}

	}

	@Override
	public FeatureDeployment insertFeatureDeploymentDetails(FeatureDeployment featureDeployment)
			throws ConfigPersistenceException {
		String methodName = "insertFeatureDeploymentDetails";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			FeatureDeployment isInsertedFeatureDep = featureDeploymentDAO
					.insertFeatureDeploymentDetails(featureDeployment);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return isInsertedFeatureDep;
		} catch (FeatureDeploymentConfigurationException e) {
			throw new ConfigPersistenceException(
					"Failed to insert FeatureDeployment Details for give featureDeployment : " + featureDeployment, e);

		}
	}

	@Override
	public FeatureDeployment getFeatureDeploymentDetails(int featureMasterId, String featureName, String implName,
			String vendorName, String version) throws ConfigPersistenceException {
		String methodName = "getFeatureDeploymentDetails";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		FeatureDeployment featureDeployment;
		try {
			featureDeployment = featureDeploymentDAO.getFeatureDeploymentByFeatureAndImplName(featureMasterId,
					featureName, implName, vendorName, version);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureDeployment;
		} catch (FeatureDeploymentConfigurationException e) {
			throw new ConfigPersistenceException("Failed to get FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version, e);

		}
	}

	@Override
	public boolean updateFeatureDeployment(int featureMasterId, String featureName, String implName, String vendorName,
			String version, boolean isPrimary, boolean isActive) throws ConfigPersistenceException {
		String methodName = "updateFeatureDeployment";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean isUpdated = false;
		try {
			isUpdated = featureDeploymentDAO.updateFeatureDeployment(featureMasterId, featureName, implName, vendorName,
					version, isPrimary, isActive);
		} catch (FeatureDeploymentConfigurationException e) {
			throw new ConfigPersistenceException("Failed to update FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version + ", and isPrimary : " + isPrimary, e);

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isUpdated;
	}

	@Override
	public boolean deleteFeatureDeployment(int featureMasterId, String featureName, String implName, String vendorName,
			String version) throws ConfigPersistenceException {
		String methodName = "deleteFeatureDeployment";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			boolean isDeleted = featureDeploymentDAO.deleteFeatureDeployment(featureMasterId, featureName, implName,
					vendorName, version);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return isDeleted;
		} catch (FeatureDeploymentConfigurationException e) {
			throw new ConfigPersistenceException("Failed to get FeatureDeployment Details for give featureName : "
					+ featureName + ", implementaion name : " + implName + ", vendor name : " + vendorName
					+ ", version : " + version, e);

		}
	}

	@Override
	public int checkSchedulerDatabyName(String configName) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.checkSchedulerDatabyName(configName);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to get jobId against scheduler data by Name=" + configName,
					sqlexp);
		}
	}

	@Override
	public int insertScheduledJobData(ScheduledJobData jobData) throws ConfigPersistenceException {
		configDataDao = new ConfigNodeDataDAO();
		try {
			return configDataDao.insertScheduledJobData(jobData);
		} catch (ConfigNodeDataConfigurationException sqlexp) {
			throw new ConfigPersistenceException("Failed to insert jobData with name=" + jobData.getJobName(), sqlexp);
		}
	}

	@Override
	public List<Map<String, Object>> getRequestContextList(String feature, String featureGroupName)
			throws ConfigPersistenceException {
		String methodName = "getRequestContextList";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configDataDao.getRequestContextList(feature, featureGroupName);
		} catch (ConfigNodeDataConfigurationException e) {
			throw new ConfigPersistenceException(
					"Failed to get the List of RequestContext objects from ConfigNode Table ", e);
		}

	}

	@Override
	public List<String> getAllSites() throws ConfigPersistenceException {
		String methodName = "getAllSites";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return configNodeDAO.getAllSitesNodeName();
		} catch (ConfigNodeConfigurationException e) {
			throw new ConfigPersistenceException(
					"Failed to get the List of RequestContext objects from ConfigNode Table ", e);
		}
	}

}
