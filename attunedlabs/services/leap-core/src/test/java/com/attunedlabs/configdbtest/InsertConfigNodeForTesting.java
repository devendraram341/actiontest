package com.attunedlabs.configdbtest;

import static com.attunedlabs.LeapCoreTestConstant.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;

public class InsertConfigNodeForTesting {
	final Logger log = LoggerFactory.getLogger(InsertConfigNodeForTesting.class);
	private ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();

	/**
	 * Inserting configNode Data into DB.
	 */
	public InsertConfigNodeForTesting() {
		try {
			testInsertConfigNode();
		} catch (ConfigNodeConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void testInsertConfigNode() throws ConfigNodeConfigurationException {

		TEST_SITE_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(TEST_SITE, SITE);

		configNodeDAO.insertConfigNodeWithVersion(getFeatureGroup());
		TEST_FEATUREGROUP_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(TEST_FEATUREGROUP, FEATURE_GROUP);

		configNodeDAO.insertConfigNodeWithVersion(getFeature());
		TEST_FEATURE_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(TEST_FEATURE, FEATURE);

		configNodeDAO.insertConfigNodeWithVersion(getImpl());
		TEST_IMPL_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(TEST_IMPL, IMPL);

		configNodeDAO.insertConfigNodeWithVersion(getVendor());
		TEST_VENDOR_NODEID = configNodeDAO.getNodeIdByNodeNameAndByType(TEST_VENDOR, VENDOR);
	}

	private ConfigurationTreeNode getFeatureGroup() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(3);
		treeNode.setNodeName(TEST_FEATUREGROUP);
		treeNode.setType(FEATURE_GROUP);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(TEST_SITE_NODEID);
		treeNode.setVersion(TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getFeature() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(4);
		treeNode.setNodeName(TEST_FEATURE);
		treeNode.setType(FEATURE);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(TEST_FEATUREGROUP_NODEID);
		treeNode.setVersion(TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getImpl() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(5);
		treeNode.setNodeName(TEST_IMPL);
		treeNode.setType(IMPL);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(TEST_FEATURE_NODEID);
		treeNode.setVersion(TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}

	private ConfigurationTreeNode getVendor() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(true);
		treeNode.setLevel(6);
		treeNode.setNodeName(TEST_VENDOR);
		treeNode.setType(VENDOR);
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(TEST_IMPL_NODEID);
		treeNode.setVersion(TEST_VERSION);
		treeNode.setPrimaryFeatureId(0);
		return treeNode;
	}
}
