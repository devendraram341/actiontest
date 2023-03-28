package com.attunedlabs.config.persistence;

import java.util.List;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;

public class TenantConfigTreeServiceTest {
	final Logger logger = LoggerFactory.getLogger(TenantConfigTreeServiceTest.class);
	private ITenantConfigTreeService tenantConfigTreeService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Before
	public void initializeTheDataGrid() throws ConfigPersistenceException {
		if (tenantConfigTreeService == null) {
			IConfigPersistenceService perService = new ConfigPersistenceServiceMySqlImpl();
			ConfigurationTreeNode configNodeTree = perService.getConfigPolicyNodeTree();
			tenantConfigTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
			tenantConfigTreeService.isInitialized();
			tenantConfigTreeService.initialize(configNodeTree);
		}
	}

	/**
	 * this test case used for get configTreeNode using tenant name
	 */
	@Test
	public void testGetConfigTreeNodeForTenantByName() {
		ConfigurationTreeNode configNode = tenantConfigTreeService
				.getConfigTreeNodeForTenantByName(GenericTestConstant.TEST_TENANT);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		Assert.assertEquals("The tenent node id should be " + GenericTestConstant.TEST_TENANT_NODEID,
				GenericTestConstant.TEST_TENANT_NODEID, configNode.getNodeId());
	}

	/**
	 * this test case used for get configTreeNode using tenant Node Id.
	 */
	@Test
	public void testGetConfigTreeNodeForTenantById() {
		ConfigurationTreeNode configNode = tenantConfigTreeService
				.getConfigTreeNodeForTenantById(GenericTestConstant.TEST_TENANT_NODEID);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		Assert.assertEquals("The tenent name should be TestTenant", GenericTestConstant.TEST_TENANT,
				configNode.getNodeName());
	}

	/**
	 * this test case used for get ConfigTreeNode for Feature.
	 */
	@Test
	public void testGetConfigTreeNodeForFeature() {
		ConfigurationTreeNode configNode = tenantConfigTreeService.getConfigTreeNodeForFeature(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		Assert.assertEquals("The feature node id should be " + GenericTestConstant.TEST_FEATURE_NODEID,
				GenericTestConstant.TEST_FEATURE_NODEID, configNode.getNodeId());
		Assert.assertEquals("The feature node name should be TestFeature", GenericTestConstant.TEST_FEATURE,
				configNode.getNodeName());
	}

	/**
	 * this test case used for get primary vendor for Feature.
	 * 
	 * @throws UndefinedPrimaryVendorForFeature
	 */
	@Test
	public void testGetPrimaryVendorForFeature() throws UndefinedPrimaryVendorForFeature {
		ConfigurationTreeNode configNode = tenantConfigTreeService.getPrimaryVendorForFeature(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		List<ConfigurationTreeNode> configNodeList = configNode.getChildNodes();
		Assert.assertNotNull("configNodeList should not be Null", configNodeList);
		Assert.assertEquals(1, configNodeList.size());
		String vendor = configNodeList.get(0).getNodeName();
		Assert.assertEquals(GenericTestConstant.TEST_VENDOR, vendor);
	}

	/**
	 * this test case used for get primary vendor for feature with impl.
	 * 
	 * @throws UndefinedPrimaryVendorForFeature
	 */
	@Test
	public void testGetPrimaryVendorForFeatureWithImpl() throws UndefinedPrimaryVendorForFeature {
		ConfigurationTreeNode configNode = tenantConfigTreeService.getPrimaryVendorForFeature(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP,
				GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		Assert.assertEquals("The vendor node Name should be TestVendor ", GenericTestConstant.TEST_VENDOR,
				configNode.getNodeName());
		Assert.assertEquals("the vendor node id should be " + GenericTestConstant.TEST_VENDOR_NODEID,
				GenericTestConstant.TEST_VENDOR_NODEID, configNode.getNodeId());
	}

	/**
	 * this test case used for get configTreeNode For feature Group.
	 */
	@Test
	public void testGetConfigTreeNodeForFeatureGroup() {
		ConfigurationTreeNode configNode = tenantConfigTreeService.getConfigTreeNodeForFeatureGroup(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP);
		Assert.assertNotNull("ConfigurationTreeNode should not be Null", configNode);
		Assert.assertEquals("The feature group node id should be " + GenericTestConstant.TEST_FEATUREGROUP_NODEID,
				GenericTestConstant.TEST_FEATUREGROUP_NODEID, configNode.getNodeId());
		Assert.assertEquals("the feature group node name should be TestFeatureGroup",
				GenericTestConstant.TEST_FEATUREGROUP, configNode.getNodeName());
	}

	/**
	 * this test case used for get configTreeNode In json.
	 */
	@Test
	public void testGetConfigTreeNodeAsJson() {
		String configNode = tenantConfigTreeService.getConfigTreeNodeAsJson();
		Assert.assertNotNull("configNode value should not be Null", configNode);
		JSONParser parser = new JSONParser();
		try {
			parser.parse(configNode);

		} catch (ParseException pe) {
			Assert.fail("ConfigTreeNode as JSON String failed to Parse as Json");
		}
	}

	/**
	 * this test case used for get application node id .
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetApplicableNodeId() throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer vendorNode = tenantConfigTreeService.getApplicableNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("vendorNode value should not be Null", vendorNode);
		Assert.assertEquals("The vendor node id should be " + GenericTestConstant.TEST_VENDOR_NODEID,
				GenericTestConstant.TEST_VENDOR_NODEID, vendorNode);
	}

	/**
	 * this test case used for get application Node id using tenant Id and site id.
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetApplicableNodeIdWithTenentAndSiteId()
			throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer siteNode = tenantConfigTreeService.getApplicableNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE);
		Assert.assertNotNull("siteNode value should not be Null", siteNode);
		Assert.assertEquals("The Site node id should be " + GenericTestConstant.TEST_SITE_NODEID,
				GenericTestConstant.TEST_SITE_NODEID, siteNode);
	}

	/**
	 * this test case used for get Application Node id using Tenant Id.
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testGetApplicableNodeIdWithTenetId() throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer tenantNode = tenantConfigTreeService.getApplicableNodeId(GenericTestConstant.TEST_TENANT);
		Assert.assertNotNull("tenant Node value should not be Null", tenantNode);
		Assert.assertEquals("The Tenant node id should be " + GenericTestConstant.TEST_TENANT_NODEID,
				GenericTestConstant.TEST_TENANT_NODEID, tenantNode);
	}

	/**
	 * this test case used for adding configuration tree node.
	 */
	@Test
	public void testAddConfigurationTreeNode() {
		ConfigurationTreeNode treeNode = getConfigTreeNodeData();
		Assert.assertNotNull("ConfigurationTreeNode must exist and should not be Null", treeNode);
		tenantConfigTreeService.deleteConfigurationTreeNode(treeNode);
	}

	@Test
	public void testDeleteConfigurationTreeNode() {
		ConfigurationTreeNode treeNode = getConfigTreeNodeData();
		Assert.assertNotNull("ConfigurationTreeNode must exist and should not be Null", treeNode);
		tenantConfigTreeService.deleteConfigurationTreeNode(treeNode);
	}

	private ConfigurationTreeNode getConfigTreeNodeData() {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(false);
		treeNode.setLevel(1);
		treeNode.setNodeName("TEST");
		treeNode.setType("TEST_TENENT");
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(0);
		treeNode.setVersion("1.0");
		treeNode.setNodeId(0);
		return treeNode;
	}
}
