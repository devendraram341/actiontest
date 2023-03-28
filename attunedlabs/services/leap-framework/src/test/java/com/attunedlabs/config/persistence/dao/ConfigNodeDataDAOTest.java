package com.attunedlabs.config.persistence.dao;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;
import com.attunedlabs.scheduler.ScheduledJobData;

public class ConfigNodeDataDAOTest {

	final Logger log = LoggerFactory.getLogger(ConfigNodeDataDAOTest.class);
	private ConfigNodeDataDAO nodeDataDAO;

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void initialization() {
		if (nodeDataDAO == null)
			nodeDataDAO = new ConfigNodeDataDAO();

	}

	/**
	 * this method used for insert config node data into DB.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testInsertConfigNodeData() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		nodeDataDAO.enableConfigNodeData(false, insertedNodeId);

		nodeDataDAO.updateConfigDataByNameAndNodeId(GenericTestConstant.TEST_UPDATE_CONFIG_DATA, insertedNodeId,
				GenericTestConstant.TEST_CONFIG_NAME, GenericTestConstant.TEST_CONFIG_TYPE);

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for fail insert config node data into DB.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test(expected = ConfigNodeDataConfigurationException.class)
	public void testInsertConfigNodeDataFail() throws ConfigNodeDataConfigurationException {
		nodeDataDAO.insertConfigNodeData(null);
	}

	/**
	 * this method used for get confignodedata using config Name and node id.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testGetConfigNodeDatabyNameAndNodeId() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		ConfigNodeData confignodeData = nodeDataDAO.getConfigNodeDatabyNameAndNodeId(
				GenericTestConstant.TEST_VENDOR_NODEID, GenericTestConstant.TEST_CONFIG_NAME,
				GenericTestConstant.TEST_CONFIG_TYPE);
		Assert.assertNotNull(confignodeData);
		Assert.assertEquals(GenericTestConstant.TEST_CONFIG_NAME, confignodeData.getConfigName());

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for fail get confignodedata using config Name and node id.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test(expected = ConfigNodeDataConfigurationException.class)
	public void testGetConfigNodeDatabyNameAndNodeIdFail() throws ConfigNodeDataConfigurationException {
		nodeDataDAO.getConfigNodeDatabyNameAndNodeId(null, null, null);
	}

	/**
	 * this method used for get ConfigNodeData using node ID.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testGetConfigNodeDatabyId() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		ConfigNodeData confiNodeData = nodeDataDAO.getConfigNodeDatabyId(insertedNodeId);
		Assert.assertNotNull(confiNodeData);
		Assert.assertEquals(GenericTestConstant.TEST_CONFIG_NAME, confiNodeData.getConfigName());
		Assert.assertEquals((Integer) insertedNodeId, confiNodeData.getNodeDataId());

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for fail get ConfigNodeData using node ID.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 * 
	 */
	@Test(expected = ConfigNodeDataConfigurationException.class)
	public void testGetConfigNodeDatabyIdFail() throws ConfigNodeDataConfigurationException {
		nodeDataDAO.getConfigNodeDatabyId(null);
	}

	/**
	 * this method used for get configNodeData using vendore node id.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testGetConfigNodeDataByNodeId() throws ConfigNodeDataConfigurationException {

		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		List<ConfigNodeData> listOfConfigNode = nodeDataDAO
				.getConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		Assert.assertTrue(listOfConfigNode.size() > 0);
		Assert.assertEquals(GenericTestConstant.TEST_VENDOR_NODEID, listOfConfigNode.get(0).getParentConfigNodeId());

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for fail get configNodeData using vendore node id.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test(expected = ConfigNodeDataConfigurationException.class)
	public void testGetConfigNodeDataByNodeIdFail() throws ConfigNodeDataConfigurationException {
		nodeDataDAO.getConfigNodeDataByNodeId(null);
	}

	/**
	 * this method used for delete configNodeData From DB.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testDeleteConfigNodeData() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		int result = nodeDataDAO.deleteConfigNodeData(insertedNodeId);
		Assert.assertTrue(result > 0);
		Assert.assertNotEquals(0, result);

	}

	/**
	 * this method used for get configNodeData using Nodeid And ConfigType.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testGetConfigNodeDataByNodeIdByConfigType() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		List<ConfigNodeData> listOfConfigNode = nodeDataDAO.getConfigNodeDataByNodeIdByConfigType(
				GenericTestConstant.TEST_VENDOR_NODEID, GenericTestConstant.TEST_CONFIG_TYPE);
		Assert.assertTrue(listOfConfigNode.size() > 0);
		Assert.assertEquals(GenericTestConstant.TEST_CONFIG_TYPE, listOfConfigNode.get(0).getConfigType());

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);

	}

	/**
	 * this method used for fail get configNodeData using Nodeid And ConfigType.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test(expected = ConfigNodeDataConfigurationException.class)
	public void testGetConfigNodeDataByNodeIdByConfigTypeFail() throws ConfigNodeDataConfigurationException {
		nodeDataDAO.getConfigNodeDataByNodeIdByConfigType(null, GenericTestConstant.TEST_CONFIG_TYPE);
	}

	/**
	 * this method used for update configNodeData table in DB.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testUpdateConfigNodeData() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		int updated = nodeDataDAO.updateConfigNodeData(getForUpdateConfigNodeData(insertedNodeId));
		Assert.assertNotEquals(0, updated);
		Assert.assertTrue(updated > 0);

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for delete configNodeData from Db using NodeID
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testDeleteConfigNodeDataByNodeId() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for delete configNodeData using configName and NodeId.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testDeleteConfigNodeDataByNodeIdAndByConfigName() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		int deleteStatus = nodeDataDAO.deleteConfigNodeDataByNodeIdAndByConfigName(GenericTestConstant.TEST_CONFIG_NAME,
				GenericTestConstant.TEST_VENDOR_NODEID);
		Assert.assertTrue(deleteStatus > 0);
		Assert.assertNotEquals(0, deleteStatus);

	}

	/**
	 * this method used for insert scheduledJob data into DB.
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testInsertScheduledJobData() throws ConfigNodeDataConfigurationException {
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

		int insert = nodeDataDAO.insertScheduledJobData(jobData);
		Assert.assertTrue(insert > 0);
		Assert.assertNotEquals("Intert data Should not be 0 ", 0, insert);

		int checkData = testCheckSchedulerDatabyName();
		Assert.assertTrue(checkData > 0);
		Assert.assertNotEquals("check data value should Not be 0", 0, checkData);
	}

	/**
	 * this method used for get all request Context list
	 * 
	 * @throws ConfigNodeDataConfigurationException
	 */
	@Test
	public void testGetRequestContextList() throws ConfigNodeDataConfigurationException {
		ConfigNodeData nodeData = getConfigNodeData();
		Assert.assertNotNull(nodeData);

		int insertedNodeId = nodeDataDAO.insertConfigNodeData(nodeData);
		Assert.assertNotEquals(0, insertedNodeId);

		List<Map<String, Object>> listOfMap = nodeDataDAO.getRequestContextList(GenericTestConstant.TEST_FEATURE,
				GenericTestConstant.TEST_FEATUREGROUP);
		Assert.assertNotNull(listOfMap);
		Assert.assertTrue(listOfMap.size() > 0);

		int deleteStatus = deleteConfigNodeData();
		Assert.assertNotEquals(0, deleteStatus);
		Assert.assertTrue(deleteStatus > 0);
	}

	/**
	 * this method used for check scheduler data by name.
	 * 
	 * @return
	 * @throws ConfigNodeDataConfigurationException
	 */
	private int testCheckSchedulerDatabyName() throws ConfigNodeDataConfigurationException {
		int checkData = nodeDataDAO.checkSchedulerDatabyName("Unit Testing");
		return checkData;
	}

	/**
	 * this method used for delete config node data from DB.
	 * 
	 * @return
	 * @throws ConfigNodeDataConfigurationException
	 */
	private int deleteConfigNodeData() throws ConfigNodeDataConfigurationException {
		int deleteStatus = nodeDataDAO.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		return deleteStatus;
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

	private ConfigNodeData getForUpdateConfigNodeData(int nodeDataId) {
		ConfigNodeData nodeData = new ConfigNodeData();
		nodeData.setConfigName("UPDATE CONFIG NAME");
		nodeData.setConfigLoadStatus("success");
		nodeData.setConfigData("UPDATE CONFIG DATA");
		nodeData.setConfigType("UPDATE CONFIG TYPE");
		nodeData.setEnabled(true);
		nodeData.setNodeDataId(nodeDataId);
		nodeData.setParentConfigNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		return nodeData;
	}
}
