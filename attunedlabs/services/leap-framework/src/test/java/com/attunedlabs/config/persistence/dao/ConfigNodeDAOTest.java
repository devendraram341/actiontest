package com.attunedlabs.config.persistence.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.ConfigNode;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;

public class ConfigNodeDAOTest {

	private ConfigNodeDAO configNodeDAO;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void initializeTheDataGrid() {
		if (configNodeDAO == null)
			configNodeDAO = new ConfigNodeDAO();
	}

	/**
	 * this method used for insert configNode into DB.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testInsertConfigNode() throws ConfigNodeConfigurationException {
		ConfigNode configNode = new ConfigNode();
		configNode.setNodeName("TEST_TENENT");
		configNode.setHasChildren(false);
		configNode.setLevel(1);
		configNode.setParentNodeId(0);
		configNode.setDescription("TEST CONFIG NODE");
		configNode.setType("tenent");
		configNode.setParentNodeId(0);
		configNode.setRoot(false);

		int insertData = configNodeDAO.insertConfigNode(configNode);
		Assert.assertNotEquals("config node id should not be 0 :", 0, insertData);

		int deletedNode = testDeleteNodeByNodeId(insertData);
		Assert.assertNotEquals("deleted node should not be 0 :", 0, deletedNode);
	}

	/**
	 * this method used for failed to insert configNode into DB.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test(expected = ConfigNodeConfigurationException.class)
	public void testInsertConfigNodeFail() throws ConfigNodeConfigurationException {
		configNodeDAO.insertConfigNode(null);
	}

	/**
	 * this method used for insert config node data with version in DB.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testInsertConfigNodeWithVersion() throws ConfigNodeConfigurationException {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(false);
		treeNode.setLevel(1);
		treeNode.setNodeName("TEST WITH VIERSION");
		treeNode.setType("TEST_TENENT");
		treeNode.setDescription("TEST CONFIG TREE");
		treeNode.setParentNodeId(0);
		treeNode.setVersion("1.2");

		int NodeIdWithVersion = configNodeDAO.insertConfigNodeWithVersion(treeNode);
		Assert.assertNotEquals(" tree node id should not be 0 :", 0, NodeIdWithVersion);

		boolean updatedFeatureId = testUpdateNodeWithPrimaryFeatureId(NodeIdWithVersion);
		Assert.assertTrue(updatedFeatureId);

		ConfigNode configData = testGetNodeById(NodeIdWithVersion);
		Assert.assertNotNull(configData);
		Assert.assertEquals("TEST WITH VIERSION", configData.getNodeName());

		int updateNode = testUpdateNodeByName(NodeIdWithVersion);
		Assert.assertNotEquals("update Node  should not be 0 :", 0, updateNode);

		int deletedNode = testDeleteNodeByNodeId(NodeIdWithVersion);
		Assert.assertNotEquals("deleted node should not be 0 :", 0, deletedNode);

	}

	/**
	 * this method used for fail insert config node data with version in DB.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test(expected = ConfigNodeConfigurationException.class)
	public void testInsertConfigNodeWithVersionFail() throws ConfigNodeConfigurationException {
		configNodeDAO.insertConfigNodeWithVersion(null);
	}

	/**
	 * this method used for get node id using node name and type
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetNodeIdByNodeNameAndByType() throws ConfigNodeConfigurationException {
		Integer nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.FEATURE);
		Assert.assertEquals(GenericTestConstant.TEST_FEATURE_NODEID, nodeId);
	}

	/**
	 * this method used for get node id using node name and type not using node id.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetNodeIdByNodeNameAndByTypeNotNodeId() throws ConfigNodeConfigurationException {
		Integer nodeId = configNodeDAO.getNodeIdByNodeNameAndByTypeNotNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TENANT, 0, 0);
		Assert.assertEquals(GenericTestConstant.TEST_TENANT_NODEID, nodeId);
	}

	/**
	 * this method used for get child node from parent nodes.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetChildNodes() throws ConfigNodeConfigurationException {
		List<ConfigNode> listOfConfigNode = configNodeDAO.getChildNodes(0);
		Assert.assertTrue(listOfConfigNode.size() > 0);
	}

	/**
	 * this method used for get config Tree node
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetNodeTree() throws ConfigNodeConfigurationException {
		ConfigurationTreeNode configTree = configNodeDAO.getNodeTree();
		Assert.assertNotNull(configTree);
		List<ConfigurationTreeNode> list = configTree.getChildNodes();
		boolean flag = true;
		for (ConfigurationTreeNode configTreeNode : list) {
			if (configTreeNode.getNodeName().equalsIgnoreCase(GenericTestConstant.TEST_TENANT)) {
				flag = false;
			}
		}
		if (flag) {
			Assert.fail("Tenant not found in db");
		}
	}

	/**
	 * this method used for get node id using config name and type
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetNodeIdByNodeNameAndByTypeWithParentNode() throws ConfigNodeConfigurationException {
		Integer nodeId = configNodeDAO.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TENANT, 0);
		Assert.assertEquals(GenericTestConstant.TEST_TENANT_NODEID, nodeId);

	}

	/**
	 * get all site Node name from configNode table.
	 * 
	 * @throws ConfigNodeConfigurationException
	 */
	@Test
	public void testGetAllSitesNodeName() throws ConfigNodeConfigurationException {
		List<String> listOfSite = configNodeDAO.getAllSitesNodeName();
		Assert.assertTrue(listOfSite.size() > 0);
		boolean flag = true;
		for (String site : listOfSite) {
			if (site.equalsIgnoreCase(GenericTestConstant.TEST_SITE)) {
				flag = false;
			}
		}
		if (flag) {
			Assert.fail(GenericTestConstant.TEST_SITE + " Not found in DB");
		}
	}

	/**
	 * this method used for delete confignode from db
	 * 
	 * @param nodeId
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	private int testDeleteNodeByNodeId(int nodeId) throws ConfigNodeConfigurationException {
		int deletedNode = configNodeDAO.deleteNodeByNodeId(nodeId);
		return deletedNode;

	}

	/**
	 * this method used for get confignode by id.
	 * 
	 * @param nodeIdWithVersion
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	private ConfigNode testGetNodeById(int nodeIdWithVersion) throws ConfigNodeConfigurationException {
		ConfigNode configData = configNodeDAO.getNodeById(nodeIdWithVersion);
		return configData;
	}

	/**
	 * this method used for update confignode with primary featureId.
	 * 
	 * @param nodeId
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	private boolean testUpdateNodeWithPrimaryFeatureId(int nodeId) throws ConfigNodeConfigurationException {
		boolean updated = configNodeDAO.updateNodeWithPrimaryFeatureId(nodeId,
				GenericTestConstant.UPDATE_PRIMARY_FEATURE_ID);
		return updated;
	}

	/**
	 * this method used for update config node name.
	 * 
	 * @param nodeId
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	private int testUpdateNodeByName(int nodeId) throws ConfigNodeConfigurationException {
		int updateData = configNodeDAO.updateNodeByName(nodeId, "UPDATED ");
		return updateData;
	}
}
