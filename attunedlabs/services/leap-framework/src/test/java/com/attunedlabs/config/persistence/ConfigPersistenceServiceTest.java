package com.attunedlabs.config.persistence;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuremaster.FeatureMaster;
import com.attunedlabs.scheduler.ScheduledJobData;

public class ConfigPersistenceServiceTest {
	final Logger logger = LoggerFactory.getLogger(ConfigPersistenceServiceTest.class);
	private IConfigPersistenceService perService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws ConfigPersistenceException
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Before
	public void init() throws ConfigPersistenceException, ConfigNodeDataConfigurationException {
		if (perService == null) {
			perService = new ConfigPersistenceServiceMySqlImpl();
			ConfigurationTreeNode configNodeTree = perService.getConfigPolicyNodeTree();
			TenantConfigTreeServiceImpl tenantConfigTreeService = TenantConfigTreeServiceImpl
					.getTenantConfigTreeServiceImpl();
			tenantConfigTreeService.initialize(configNodeTree);
		}
	}

	/**
	 * this method get config policy Node tree as json format .
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetConfigPolicyNodeTreeAsJson() throws ConfigPersistenceException {
		String json = perService.getConfigPolicyNodeTreeAsJson();
		Assert.assertNotNull("json value should not be Null", json);
	}

	// Config Node Table Testing

	/**
	 * this test case used for check successfully inserted configNode data in db.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testInsertConfigNode() throws ConfigPersistenceException {
		ConfigNode configNode = new ConfigNode();
		configNode.setNodeName("TEST_TENENT");
		configNode.setHasChildren(false);
		configNode.setLevel(1);
		configNode.setParentNodeId(0);
		configNode.setVersion("1.0");
		configNode.setDescription("JUNIT TESTING");
		configNode.setType("tenent");
		configNode.setParentNodeId(0);
		configNode.setRoot(false);

		Integer nodeIdValue = perService.insertConfigNode(configNode);
		Assert.assertNotNull("Node value should not be Null", nodeIdValue);

		boolean flag = perService.deleteNodeByNodeId(nodeIdValue);
		Assert.assertTrue("deleted node should be flag value true ", flag);
	}

	/**
	 * This test case for failed to insert configNode Data into DB.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testInsertConfigNodeFail() throws ConfigPersistenceException {
		perService.insertConfigNode(null);

	}

	/**
	 * this test case used for insert config node data with version into ConfigNode
	 * DB.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testInsertConfigNodeWithVersion() throws ConfigPersistenceException {
		ConfigurationTreeNode treeNode = new ConfigurationTreeNode();
		treeNode.setHasChildern(false);
		treeNode.setLevel(1);
		treeNode.setNodeName("TEST WITH VIERSION");
		treeNode.setType("TEST_TENENT");
		treeNode.setDescription("TESTING PURPOSE");
		treeNode.setParentNodeId(0);
		treeNode.setVersion("1.2");

		Assert.assertNotNull("ConfigurationTreeNode should not be Null", treeNode);

		int nodeId = perService.insertConfigNodeWithVersion(treeNode);
		Assert.assertNotEquals("Inserted Node should not have NodeId as 0", 0, nodeId);

		ConfigNode configNodeFromDB = perService.getNodeById(nodeId);
		Assert.assertEquals("node id and config node id should be match", (Integer) nodeId,
				configNodeFromDB.getNodeId());

		int updated = perService.updateConfigNodeWithPrimaryFeatureId(nodeId,
				GenericTestConstant.UPDATE_PRIMARY_FEATURE_ID);
		Assert.assertEquals("Updated Node should have 0 beacuse every timereturn 0", 0, updated);

		boolean flag = perService.deleteNodeByNodeId(nodeId);
		Assert.assertTrue("deleted node should be flag is true", flag);
	}

	/**
	 * This test case for failed to insert configNode Data with version into DB.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testInsertConfigNodeWithVersionFail() throws ConfigPersistenceException {

		perService.insertConfigNodeWithVersion(null);

	}

	/**
	 * this test case method use for get child node from parent node id.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetChildNodes() throws ConfigPersistenceException {
		List<ConfigNode> listOfConfigNode = perService.getChildNodes(GenericTestConstant.TEST_PARENT_NODEID);
		Assert.assertNotNull("list Of Config Node must exist and should not be Null", listOfConfigNode);
		Assert.assertNotEquals("list of config node should not be 0 ", 0, listOfConfigNode.size());
	}

	/**
	 * this test case method use check failed to get child node data.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testGetChildNodesFail() throws ConfigPersistenceException {
		perService.getChildNodes(null);
	}

	/**
	 * this test case method use for get Node Id according node name and type;
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetNodeIdByNodeNameAndByType() throws ConfigPersistenceException {
		Integer tenantNodeValue = perService.getNodeIdByNodeNameAndByType(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TENANT, GenericTestConstant.TEST_PARENT_NODEID);
		Assert.assertEquals("the tenant node id Should be 47", GenericTestConstant.TEST_TENANT_NODEID, tenantNodeValue);
	}

	/**
	 * this test case method use for failed to get Node Id according node name and
	 * type;
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testGetNodeIdByNodeNameAndByTypeFail() throws ConfigPersistenceException {
		perService.getNodeIdByNodeNameAndByType(null, null, GenericTestConstant.TEST_PARENT_NODEID);
	}

	/**
	 * this test case method use for get node id according to name and type not with
	 * given node id;
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetNodeIdByNodeNameAndByTypeNotWithGivenNodeId() throws ConfigPersistenceException {
		Integer nodeId = perService.getNodeIdByNodeNameAndByTypeNotWithGivenNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TENANT, GenericTestConstant.TEST_PARENT_NODEID, 0);
		Assert.assertEquals("the tenant node id Should be " + GenericTestConstant.TEST_TENANT_NODEID,
				GenericTestConstant.TEST_TENANT_NODEID, nodeId);
	}

	/**
	 * this test case method use for failed to get node id according to name and
	 * type not with given node id;
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testGetNodeIdByNodeNameAndByTypeNotWithGivenNodeIdFail() throws ConfigPersistenceException {
		perService.getNodeIdByNodeNameAndByTypeNotWithGivenNodeId(null, GenericTestConstant.TENANT,
				GenericTestConstant.TEST_PARENT_NODEID, 0);
	}

	/**
	 * this test case method used for getConfigtree Node for FeatureGroup.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetConfigTreeNodeForFeatureGroup() throws ConfigPersistenceException {
		ConfigurationTreeNode configTreeNode = perService.getConfigTreeNodeForFeatureGroup(
				GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP);
		Assert.assertNotNull("config Tree node should not be Null", configTreeNode);
		Assert.assertEquals("The feature Group node id Should be " + GenericTestConstant.TEST_FEATUREGROUP_NODEID,
				GenericTestConstant.TEST_FEATUREGROUP_NODEID, configTreeNode.getNodeId());
	}

	/**
	 * this test case method use for getConfigTreeNode for Tenant by id.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetConfigTreeNodeForTenantById() throws ConfigPersistenceException {
		logger.debug("Tenant Node Id :: " + GenericTestConstant.TEST_TENANT_NODEID);
		ConfigurationTreeNode configTreeNode = perService
				.getConfigTreeNodeForTenantById(GenericTestConstant.TEST_TENANT_NODEID);
		logger.debug("configTreeNode value in testGetConfigTreeNodeForTenantById :: " + configTreeNode);
		Assert.assertNotNull("config Tree node should not be Null", configTreeNode);
		Assert.assertEquals("The Tenant name should be TestTenant", GenericTestConstant.TEST_TENANT,
				configTreeNode.getNodeName());
	}

	/**
	 * This test method use for get Application Node Id.
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testGetApplicableNodeId() throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer vendorNodeId = perService.getApplicableNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertEquals("The Vendor node id Should be " + GenericTestConstant.TEST_VENDOR_NODEID,
				GenericTestConstant.TEST_VENDOR_NODEID, vendorNodeId);
	}

	/**
	 * This test method use for failed to get Application Node Id.
	 * 
	 * @throws ConfigPersistenceException
	 * @throws InvalidNodeTreeException
	 */
	@Test(expected = InvalidNodeTreeException.class)
	public void testGetApplicableNodeIdFail() throws InvalidNodeTreeException, ConfigPersistenceException {
		perService.getApplicableNodeId(null, null, null, null, null, null, null);
	}

	/**
	 * This test method use for get Application Node Id with tenant.
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetApplicableNodeIdWithTenant() throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer tenantNodeId = perService.getApplicableNodeId(GenericTestConstant.TEST_TENANT);
		Assert.assertEquals("the tenant node id Should be " + GenericTestConstant.TEST_TENANT_NODEID,
				GenericTestConstant.TEST_TENANT_NODEID, tenantNodeId);
	}

	/**
	 * This test method use for get Application Node Id with tenant and site
	 * 
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetApplicableNodeIdwithTeanatAndSite() throws InvalidNodeTreeException, ConfigPersistenceException {
		Integer SiteNodeId = perService.getApplicableNodeId(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE);
		Assert.assertEquals("the tenant node id Should be " + GenericTestConstant.TEST_SITE_NODEID,
				GenericTestConstant.TEST_SITE_NODEID, SiteNodeId);
	}

	/**
	 * this test method use for get node id by name and type.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetNodeIDByNameAndType() throws ConfigPersistenceException {
		Integer value = perService.getNodeIDByNameAndType(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.FEATURE);
		Assert.assertEquals("the feature node id Should be " + GenericTestConstant.TEST_FEATURE_NODEID,
				GenericTestConstant.TEST_FEATURE_NODEID, value);
	}

	/**
	 * this test method use for failed to get node id by name and type.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test(expected = ConfigPersistenceException.class)
	public void testGetNodeIDByNameAndTypeFail() throws ConfigPersistenceException {
		perService.getNodeIDByNameAndType(null, null);
	}

	// Config Node Data Testing

	/**
	 * this test method use for insert config node data into DB.
	 * 
	 * @throws ConfigPersistenceException
	 */

	@Test
	public void testInsertConfigNodeData() throws ConfigPersistenceException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull("config node data should not be null", nodeData);
		int nodeDataId = perService.insertConfigNodeData(nodeData);
		Assert.assertNotEquals("inserted Node value should not have 0", 0, nodeDataId);

		List<Map<String, Object>> requestContextList = testGetRequestContextList();
		Assert.assertNotNull("request Context list must have a data and should not be Null", requestContextList);

		List<ConfigNodeData> listOfConfigNodeData = perService
				.getConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		Assert.assertNotNull("list data should not be Null", listOfConfigNodeData);
		Assert.assertNotEquals("list should not have 0", 0, listOfConfigNodeData.size());
		Assert.assertEquals("from list vendor node id should be same ", GenericTestConstant.TEST_VENDOR_NODEID,
				listOfConfigNodeData.get(0).getParentConfigNodeId());

		List<ConfigNodeData> listOfConfigNodeDataByIdAndType = perService.getConfigNodeDataByNodeIdAndByType(
				GenericTestConstant.TEST_VENDOR_NODEID, GenericTestConstant.TEST_CONFIG_TYPE);
		Assert.assertNotNull("list should not be Null", listOfConfigNodeDataByIdAndType);
		Assert.assertNotEquals("list should not have 0", 0, listOfConfigNodeDataByIdAndType.size());

		boolean nodeDataIdFlag = listOfConfigNodeData.toString().contains(Integer.toString(nodeDataId));
		Assert.assertTrue("nodeDataid should be match in list of config node data :", nodeDataIdFlag);

		ConfigNodeData configNodeData = perService.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, GenericTestConstant.TEST_CONFIG_NAME,
				GenericTestConstant.TEST_CONFIG_TYPE);
		Assert.assertNotNull("configNodeData should not be Null", configNodeData);

		Assert.assertEquals(GenericTestConstant.TEST_VENDOR_NODEID, configNodeData.getParentConfigNodeId());
		boolean updateFlag = perService.updateConfigdataInConfigNodeData(GenericTestConstant.TEST_UPDATE_CONFIG_DATA,
				GenericTestConstant.TEST_VENDOR_NODEID, GenericTestConstant.TEST_CONFIG_NAME,
				GenericTestConstant.TEST_CONFIG_TYPE);
		Assert.assertTrue("Update flag should be true ", updateFlag);

		boolean deletedFlag = perService.deleteConfigNodeData(nodeDataId);
		Assert.assertTrue("deleted flag should be true ", deletedFlag);
	}

	/**
	 * this test method use for delete configNodeData by Id.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testDeleteConfigNodeDataByNodeId() throws ConfigPersistenceException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull("configNodeData should not be Null", nodeData);
		int nodeDataId = perService.insertConfigNodeData(nodeData);
		Assert.assertNotEquals("inserted Node should not have 0", 0, nodeDataId);

		perService.enableConfigNodeData(false, nodeDataId);

		int result = perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		Assert.assertNotEquals("deleted Node should not have 0", 0, result);
	}

	/**
	 * this test method used for delete config node data by id and config name.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testDeleteConfigNodeDataByNodeIdAndConfigName() throws ConfigPersistenceException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull("configNodeData should not be Null", nodeData);
		int nodeDataId = perService.insertConfigNodeData(nodeData);
		Assert.assertNotEquals("", 0, nodeDataId);

		int result = perService.deleteConfigNodeDataByNodeIdAndConfigName(GenericTestConstant.TEST_CONFIG_NAME,
				GenericTestConstant.TEST_VENDOR_NODEID);
		Assert.assertNotEquals("deleted Node should not have 0", 0, result);
	}

	// Feature Master and Feature Deployment Table Testing

	/**
	 * this test method used for insert feature master data in DB.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testInsertFeatureMasterDetails() throws ConfigPersistenceException {
		FeatureMaster featureMaster = new FeatureMaster();
		featureMaster.setFeature(GenericTestConstant.TEST_FEATURE);
		featureMaster.setFeatureGroup(GenericTestConstant.TEST_FEATUREGROUP);
		featureMaster.setSiteId(GenericTestConstant.TEST_SITE_NODEID);
		featureMaster.setVersion(GenericTestConstant.TEST_VERSION);
		featureMaster.setAllowMultipleImpl(true);
		featureMaster.setMultipleVendorSupport(true);
		featureMaster.setProduct("Junit Testing 1.0");
		featureMaster.setDescription("Testing purpose");

		Assert.assertNotNull("feature master data should not be Null", featureMaster);

		boolean masterData = perService.insertFeatureMasterDetails(featureMaster);
		Assert.assertTrue("inserted flag should be true ", masterData);

		int featureMasterId = perService.getFeatureMasterIdByFeatureAndFeaturegroup(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_FEATUREGROUP, GenericTestConstant.TEST_VERSION,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertNotEquals("Feature Master Node Id Should not be 0 ", 0, featureMasterId);

		testInsertFeatureDeploymentDetails(featureMasterId);

		boolean result = perService.deleteFeatureMasterDetails(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_SITE_NODEID);
		Assert.assertTrue("deleted flag should be true ", result);
	}

	// Feature Deployment table testing

	/**
	 * this method use for insert feature deployment detail into DB.
	 * 
	 * @param featureMaseterNodeId
	 * @throws ConfigPersistenceException
	 */
	private void testInsertFeatureDeploymentDetails(int featureMaseterNodeId) throws ConfigPersistenceException {
		FeatureDeployment featureDeployment = getFeatureDeploymentData(featureMaseterNodeId);
		Assert.assertNotNull("feature Deployment data should not be Null", featureDeployment);

		FeatureDeployment insertedData = perService.insertFeatureDeploymentDetails(featureDeployment);
		Assert.assertNotNull("Insertdata value should not be Null", insertedData);
		Assert.assertEquals("FeatureMasterId should be same in Both table  ", featureMaseterNodeId,
				insertedData.getFeatureMasterId());

		FeatureDeployment getFeatureData = perService.getFeatureDeploymentDetails(featureMaseterNodeId,
				GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR,
				GenericTestConstant.TEST_VERSION);
		Assert.assertNotNull("getFeatureData value should not be Null", getFeatureData);
		Assert.assertEquals("FeatureMasterId should be same in Both table  ", featureMaseterNodeId,
				getFeatureData.getFeatureMasterId());

		boolean updatedFlag = perService.updateFeatureDeployment(featureMaseterNodeId, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION, false,
				false);
		Assert.assertTrue("updated flag should be true ", updatedFlag);

		boolean deletedFlag = perService.deleteFeatureDeployment(featureMaseterNodeId, GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_IMPL, GenericTestConstant.TEST_VENDOR, GenericTestConstant.TEST_VERSION);
		Assert.assertTrue("deleted flag should be true ", deletedFlag);
	}

	// scheduled job tabel testing

	/**
	 * this test method used for insert scheduled jon data into db.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testInsertScheduledJobData() throws ConfigPersistenceException {
		ScheduledJobData jobData = new ScheduledJobData();
		jobData.setAllApplicableTenant(false);
		jobData.setAuthenticated(false);
		jobData.setConcurrent(false);
		jobData.setEnabled(false);
		jobData.setFeature(GenericTestConstant.TEST_FEATURE);
		jobData.setFeatureGroup(GenericTestConstant.TEST_FEATUREGROUP);
		jobData.setJobContextDetail("JUNIT TEST");
		jobData.setJobName("Unit Testing");
		jobData.setJobService("Testing service");
		jobData.setJobType("Test Type");

		int insert = perService.insertScheduledJobData(jobData);
		Assert.assertNotEquals("Intert data Should not be 0 ", 0, insert);

		int checkData = perService.checkSchedulerDatabyName("Unit Testing");
		Assert.assertNotEquals("check data value should be 1", 0, checkData);
	}

	/**
	 * this test method used for get all site from config node table.
	 * 
	 * @throws ConfigPersistenceException
	 */
	@Test
	public void testGetAllSites() throws ConfigPersistenceException {
		List<String> siteList = perService.getAllSites();
		Assert.assertNotNull("site list must have a data and should not be Null", siteList);
	}

	private List<Map<String, Object>> testGetRequestContextList() throws ConfigPersistenceException {
		List<Map<String, Object>> requestContextList = perService
				.getRequestContextList(GenericTestConstant.TEST_FEATURE, GenericTestConstant.TEST_FEATUREGROUP);
		return requestContextList;
	}

	private ConfigNodeData getConfigNodeData() {
		ConfigNodeData nodeData = new ConfigNodeData();
		nodeData.setConfigName(GenericTestConstant.TEST_CONFIG_NAME);
		nodeData.setConfigLoadStatus("success");
		nodeData.setConfigData(GenericTestConstant.TEST_CONFIG_DATA);
		nodeData.setConfigType(GenericTestConstant.TEST_CONFIG_TYPE);
		nodeData.setEnabled(true);
		nodeData.setParentConfigNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		return nodeData;
	}

	private FeatureDeployment getFeatureDeploymentData(int masterNodeId) {
		FeatureDeployment featureDeployment = new FeatureDeployment();
		featureDeployment.setFeatureMasterId(masterNodeId);
		featureDeployment.setActive(true);
		featureDeployment.setFeatureName(GenericTestConstant.TEST_FEATURE);
		featureDeployment.setFeatureVersion(GenericTestConstant.TEST_VERSION);
		featureDeployment.setImplementationName(GenericTestConstant.TEST_IMPL);
		featureDeployment.setVendorName(GenericTestConstant.TEST_VENDOR);
		featureDeployment.setPrimary(true);
		featureDeployment.setProvider("TEST PROVODER");
		featureDeployment.setVendorTaxonomyId("Test taxonomy");
		return featureDeployment;
	}
}
