package com.attunedlabs.config.persistence.confignode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;

public class InsertConfigNode {
	final Logger log = LoggerFactory.getLogger(InsertConfigNode.class);
	private ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();

	public InsertConfigNode() {
		try {
			testInsertConfigNode();
		} catch (ConfigNodeConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	public void testInsertConfigNode() throws ConfigNodeConfigurationException {
		configNodeDAO.insertConfigNodeWithVersion(getTenant());
		GenericTestConstant.TEST_TENANT_NODEID=configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TENANT);
		
		configNodeDAO.insertConfigNodeWithVersion(getSite());
		GenericTestConstant.TEST_SITE_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_SITE,
				GenericTestConstant.SITE);
		
		configNodeDAO.insertConfigNodeWithVersion(getFeatureGroup());
		GenericTestConstant.TEST_FEATUREGROUP_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.FEATURE_GROUP);
		
		configNodeDAO.insertConfigNodeWithVersion(getFeature());
		GenericTestConstant.TEST_FEATURE_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.FEATURE);
		
		configNodeDAO.insertConfigNodeWithVersion(getImpl());
		GenericTestConstant.TEST_IMPL_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_IMPL,
				GenericTestConstant.IMPL);
		
		configNodeDAO.insertConfigNodeWithVersion(getVendor());
		GenericTestConstant.TEST_VENDOR_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.VENDOR);
	}

	private ConfigurationTreeNode getTenant() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(1);
		treeNode.setNodeName(GenericTestConstant.TEST_TENANT);
		treeNode.setType(GenericTestConstant.TENANT);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(0);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getSite() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(2);
		treeNode.setNodeName(GenericTestConstant.TEST_SITE);
		treeNode.setType(GenericTestConstant.SITE);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(GenericTestConstant.TEST_TENANT_NODEID);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getFeatureGroup() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(3);
		treeNode.setNodeName(GenericTestConstant.TEST_FEATUREGROUP);
		treeNode.setType(GenericTestConstant.FEATURE_GROUP);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(GenericTestConstant.TEST_SITE_NODEID);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getFeature() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(4);
		treeNode.setNodeName(GenericTestConstant.TEST_FEATURE);
		treeNode.setType(GenericTestConstant.FEATURE);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(GenericTestConstant.TEST_FEATUREGROUP_NODEID);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getImpl() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(5);
		treeNode.setNodeName(GenericTestConstant.TEST_IMPL);
		treeNode.setType(GenericTestConstant.IMPL);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(GenericTestConstant.TEST_FEATURE_NODEID);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getVendor() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(6);
		treeNode.setNodeName(GenericTestConstant.TEST_VENDOR);
		treeNode.setType(GenericTestConstant.VENDOR);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(GenericTestConstant.TEST_IMPL_NODEID);
		treeNode.setVersion(GenericTestConstant.TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}
}
