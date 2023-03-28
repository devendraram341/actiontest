package com.attunedlabs.config.persistence.dao;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.ConfigNode;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.util.DataSourceInstance;

/**
 * 
 * @author bizruntime #TODO Proper Exception Handling is Pending
 */
public class ConfigNodeDAO {
	final Logger logger = LoggerFactory.getLogger(ConfigNodeDAO.class);

	/**
	 * Inserts ConfigNode in the DB with Version
	 * 
	 * @param treeNode = set the values from POJO to MySQL.
	 * @return true
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 * 
	 */
	public int insertConfigNodeWithVersion(final ConfigurationTreeNode treeNode)
			throws ConfigNodeConfigurationException {
		String methodName = "insertConfigNodeWithVersion";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer generatedNodeId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.CONFIG_NODE_NAME, treeNode.getNodeName())
							.value(LeapConstants.IS_ROOT, false)
							.value(LeapConstants.HAS_CHILDERN, treeNode.isHasChildern());

					if (treeNode.getParentNodeId() != null)
						insert.value(LeapConstants.PARENT_NODE_ID, treeNode.getParentNodeId());
					else
						insert.value(LeapConstants.PARENT_NODE_ID, java.sql.Types.NULL);

					insert.value(LeapConstants.DESCRIPTION, treeNode.getDescription())
							.value(LeapConstants.TYPE, treeNode.getType())
							.value(LeapConstants.LEVEL, treeNode.getLevel())
							.value(LeapConstants.VERSION, treeNode.getVersion())
							.value(LeapConstants.PRIMARY_FEATURE_ID, treeNode.getPrimaryFeatureId()).execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedNodeId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("{} insertConfigNode-generatedKey:{} ", LEAP_LOG_KEY, generatedNodeId);
			} else
				logger.debug("{} insertConfigNode-generatedKey  not found", LEAP_LOG_KEY);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to insert config node: " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return generatedNodeId;

	}

	/**
	 * Inserts ConfigNode in the DB
	 * 
	 * @param node set the values from POJO to MySQL.
	 * @return true
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 * 
	 */
	public int insertConfigNode(final ConfigNode node) throws ConfigNodeConfigurationException {
		String methodName = "insertConfigNode";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer generatedNodeId = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.CONFIG_NODE_NAME, node.getNodeName())
							.value(LeapConstants.IS_ROOT, node.isRoot())
							.value(LeapConstants.HAS_CHILDERN, node.isHasChildren());

					if (node.getParentNodeId() != null)
						insert.value(LeapConstants.PARENT_NODE_ID, node.getParentNodeId());
					else
						insert.value(LeapConstants.PARENT_NODE_ID, java.sql.Types.NULL);

					if (node.getVersion() != null)
						insert.value(LeapConstants.VERSION, node.getVersion());

					insert.value(LeapConstants.DESCRIPTION, node.getDescription())
							.value(LeapConstants.TYPE, node.getType()).value(LeapConstants.LEVEL, node.getLevel())
							.execute();

				}
			});

			if (insertSummary.getGeneratedKeys().isPresent()) {
				generatedNodeId = Integer
						.parseInt(insertSummary.getGeneratedKeys().get().iterator().next().toString().trim());
				logger.debug("generatedKey:{} {} ", LEAP_LOG_KEY, generatedNodeId);
			} else
				logger.debug("generatedKey not found {}", LEAP_LOG_KEY);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to insert config node: " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return generatedNodeId;
	}

	/**
	 * updateNode With PrimaryFeatureId.
	 * 
	 * @param nodeId
	 * @param primaryFeatureId
	 * @return
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	public boolean updateNodeWithPrimaryFeatureId(final int nodeId, final int primaryFeatureId)
			throws ConfigNodeConfigurationException {
		String methodName = "updateNodeWithPrimaryFeatureId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
							.value(LeapConstants.PRIMARY_FEATURE_ID, primaryFeatureId).execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("total updated node:{} {}", LEAP_LOG_KEY, totalRowsUpdated);
				if (totalRowsUpdated > 0)
					return true;
			} else
				logger.debug("total updated node:{} {}", LEAP_LOG_KEY, totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to update config node: " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;
	}

	/**
	 * getting node data by specific node id.
	 * 
	 * @param nodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 * 
	 */
	public ConfigNode getNodeById(Integer nodeId) throws ConfigNodeConfigurationException {
		String methodName = "getNodeById";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigNode config = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				config = new ConfigNode();
				Row row = itr.next();
				config.setNodeId(Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString().trim()));
				config.setNodeName(
						row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_NAME)).toString().trim());
				config.setRoot(Boolean
						.getBoolean(row.getValue(table.getColumnByName(LeapConstants.IS_ROOT)).toString().trim()));
				config.setHasChildren(Boolean
						.getBoolean(row.getValue(table.getColumnByName(LeapConstants.HAS_CHILDERN)).toString().trim()));
				config.setParentNodeId(Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.PARENT_NODE_ID)).toString().trim()));
				config.setDescription(row.getValue(table.getColumnByName(LeapConstants.DESCRIPTION)).toString().trim());
				config.setType(row.getValue(table.getColumnByName(LeapConstants.TYPE)).toString().trim());
				config.setLevel(
						Integer.parseInt(row.getValue(table.getColumnByName(LeapConstants.LEVEL)).toString().trim()));
				config.setVersion(row.getValue(table.getColumnByName(LeapConstants.VERSION)).toString().trim());
			}
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to get config-nodeId: " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} confignode info for nodeid {} is  {}", LEAP_LOG_KEY, nodeId, config);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return config;
	}

	/**
	 * update node name where node id is matched.
	 * 
	 * @param nodeId
	 * @param nodeName
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	public int updateNodeByName(final Integer nodeId, final String nodeName) throws ConfigNodeConfigurationException {
		String methodName = "updateNodeByName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.CONFIG_NODE_ID).eq(nodeId)
							.value(LeapConstants.CONFIG_NODE_NAME, nodeName).execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("{} total updated node:{}", LEAP_LOG_KEY, totalRowsUpdated);
				if (totalRowsUpdated > 0)
					return totalRowsUpdated;
			} else
				logger.debug("{} total updated node:{}", LEAP_LOG_KEY, totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException(
					"failed to update config-nodename with nodeId:" + nodeId + " ---> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return totalRowsUpdated;
	}

	/**
	 * TO search node exsist with given nodename and type
	 * 
	 * @param nodeName
	 * @param type
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	public int getNodeIdByNodeNameAndByType(String nodeName, String type) throws ConfigNodeConfigurationException {
		String methodName = "getNodeIdByNodeNameAndByType";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table)
					.select(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).where(LeapConstants.CONFIG_NODE_NAME)
					.eq(nodeName).and(LeapConstants.TYPE).eq(type).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				int nodeId = Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString());
				logger.info("{} fetched node id:{} ", LEAP_LOG_KEY, nodeId);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return nodeId;
			} else {
				logger.warn("getNodeIdByNodeNameAndByType is null {}", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to get nodeId --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
	}

	/**
	 * TO search nodeId of given nodename , type and ParnetNodeId not with
	 * updateNodeId
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @param updatingNodeId
	 * @return
	 * @throws ConfigNodeConfigurationException
	 */
	public int getNodeIdByNodeNameAndByTypeNotNodeId(String nodeName, String type, int parentNodeId, int updatingNodeId)
			throws ConfigNodeConfigurationException {
		String args = "nodeName: " + nodeName + " type: " + type + " parentNodeId: " + parentNodeId
				+ " updatingNodeId: " + updatingNodeId;
		String methodName = "getNodeIdByNodeNameAndByTypeNotNodeId";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, args);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table)
					.select(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).where(LeapConstants.CONFIG_NODE_NAME)
					.eq(nodeName).and(LeapConstants.TYPE).eq(type).and(LeapConstants.PARENT_NODE_ID).eq(parentNodeId)
					.and(LeapConstants.CONFIG_NODE_ID).ne(updatingNodeId).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				int nodeId = Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString());
				logger.info("{} fetched node id :{} ", LEAP_LOG_KEY, nodeId);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return nodeId;
			} else {
				logger.warn("null {}", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to get nodeId --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * getting the child nodes based on parent node id.
	 * 
	 * @param parentNodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	public List<ConfigNode> getChildNodes(Integer parentNodeId) throws ConfigNodeConfigurationException {
		String methodName = "getChildNodes";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<ConfigNode> list = new ArrayList<ConfigNode>();
		ConfigNode config = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.PARENT_NODE_ID)
					.eq(parentNodeId).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				config = new ConfigNode();
				Row row = itr.next();
				config.setNodeId(Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString().trim()));
				config.setNodeName(
						row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_NAME)).toString().trim());
				config.setRoot(Boolean
						.getBoolean(row.getValue(table.getColumnByName(LeapConstants.IS_ROOT)).toString().trim()));
				config.setHasChildren(Boolean
						.getBoolean(row.getValue(table.getColumnByName(LeapConstants.HAS_CHILDERN)).toString().trim()));
				config.setParentNodeId(Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.PARENT_NODE_ID)).toString().trim()));
				config.setDescription(row.getValue(table.getColumnByName(LeapConstants.DESCRIPTION)).toString().trim());
				config.setType(row.getValue(table.getColumnByName(LeapConstants.TYPE)).toString().trim());
				config.setLevel(
						Integer.parseInt(row.getValue(table.getColumnByName(LeapConstants.LEVEL)).toString().trim()));
				config.setVersion(row.getValue(table.getColumnByName(LeapConstants.VERSION)).toString().trim());
				list.add(config);
			}
			logger.debug("{} confignode info for parentNodeId {} is {}", LEAP_LOG_KEY, parentNodeId, list);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to get childnodes --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		return list;
	}

	/**
	 * get the whole configuration tree node.
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	public ConfigurationTreeNode getNodeTree() throws ConfigNodeConfigurationException {
		String methodName = "getNodeTree";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationTreeNode rootNode = new ConfigurationTreeNode(new Integer(0), "Root", "Root", 0, "1.0", 0);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table).select(LeapConstants.CONFIG_NODE_ID)
					.and(LeapConstants.CONFIG_NODE_NAME).and(LeapConstants.TYPE).and(LeapConstants.PARENT_NODE_ID)
					.and(LeapConstants.LEVEL).and(LeapConstants.VERSION).and(LeapConstants.PRIMARY_FEATURE_ID)
					.orderBy(LeapConstants.PARENT_NODE_ID).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				ConfigurationTreeNode node = new ConfigurationTreeNode();
				parseDSNode(node, row, table);
				rootNode.addChildren(node);
				logger.debug("Got the Root Node {}", LEAP_LOG_KEY);
			}

			// int currentLevel = 1;
			while (itr.hasNext()) {
				Row row = itr.next();
				ConfigurationTreeNode node = new ConfigurationTreeNode();
				parseDSNode(node, row, table);
				logger.debug("Node Data is {} {} ", LEAP_LOG_KEY, node);
				rootNode.addChildren(node);
				// addToParentNode(node,rootNode);
			}

			logger.debug("{} all child nodes:{} ", LEAP_LOG_KEY, rootNode.getChildNodes());
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to getNodeTree --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		return rootNode;

	}

	/**
	 * getting node id by name, type, parentNodeId.
	 * 
	 * @param nodeName
	 * @param type
	 * @param parentNodeId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	// TO search node exist with given nodename , type and ParnetNodeId
	public int getNodeIdByNodeNameAndByType(String nodeName, String type, int parentNodeId)
			throws ConfigNodeConfigurationException {
		String arg = "nodeName: " + nodeName + " type: " + type + " parentNodeId: " + parentNodeId;
		String methodName = "getNodeIdByNodeNameAndByType";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, arg);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DataSet dataSet = dataContext.query().from(table)
					.select(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).where(LeapConstants.CONFIG_NODE_NAME)
					.eq(nodeName).and(LeapConstants.TYPE).eq(type).and(LeapConstants.PARENT_NODE_ID).eq(parentNodeId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				int nodeId = Integer
						.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString());
				logger.info("fetched node id :{} {}", LEAP_LOG_KEY, nodeId);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return nodeId;
			} else {
				logger.warn("getNodeIdByNodeNameAndByType is null {}", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return 0;
			}
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException("failed to getNodeId --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

	}

	/**
	 * Deleting the config node with the specified node id.
	 * 
	 * @param nodeID
	 * @return
	 * @throws IOException
	 * @throws ConfigNodeConfigurationException
	 */
	public int deleteNodeByNodeId(final int nodeID) throws ConfigNodeConfigurationException {
		String methodName = "deleteNodeByNodeId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.where(LeapConstants.CONFIG_NODE_ID).eq(nodeID).execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("total deleted node:{} {}", LEAP_LOG_KEY, totalRowsDeleted);
				return totalRowsDeleted;
			} else
				logger.info("total deleted node:{} {}", LEAP_LOG_KEY, totalRowsDeleted);
		} catch (Exception e) {
			throw new ConfigNodeConfigurationException(
					"failed to deletenode with nodeId:" + nodeID + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return totalRowsDeleted;

	}

	public List<String> getAllSitesNodeName() throws ConfigNodeConfigurationException {
		String methodName = "getAllSitesNodeName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		DataSet dataSet = null;
		List<String> availableSitesList = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_TABLE);
			Column nodeNameCol = table.getColumnByName(LeapConstants.CONFIG_NODE_NAME);
			Column typeCol = table.getColumnByName(LeapConstants.TYPE);

			dataSet = dataContext.query().from(table).select(nodeNameCol).where(typeCol).eq(LeapConstants.SITE_KEY)
					.execute();

			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				String nodeName = row.getValue(nodeNameCol).toString().trim().toLowerCase();
				availableSitesList.add(nodeName);
			}
		} catch (Exception e) {
			logger.error("Failed to get Site Information {} {}", LEAP_LOG_KEY, e.getMessage());
			throw new ConfigNodeConfigurationException("Failed to get Site Information" + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("All the available Sites are ::{} {}", LEAP_LOG_KEY, availableSitesList);
		return availableSitesList;
	}

	private void parseDSNode(ConfigurationTreeNode config, Row row, Table table) {
		config.setNodeId(
				Integer.parseInt(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_ID)).toString().trim()));
		config.setNodeName(row.getValue(table.getColumnByName(LeapConstants.CONFIG_NODE_NAME)).toString().trim());
		config.setType(row.getValue(table.getColumnByName(LeapConstants.TYPE)).toString().trim());
		config.setParentNodeId(
				Integer.parseInt(row.getValue(table.getColumnByName(LeapConstants.PARENT_NODE_ID)).toString().trim()));
		config.setLevel(Integer.parseInt(row.getValue(table.getColumnByName(LeapConstants.LEVEL)).toString().trim()));
		if (row.getValue(table.getColumnByName(LeapConstants.VERSION)) != null)
			config.setVersion(row.getValue(table.getColumnByName(LeapConstants.VERSION)).toString().trim());
		else
			config.setVersion("0");

		if (row.getValue(table.getColumnByName(LeapConstants.PRIMARY_FEATURE_ID)) != null)
			config.setPrimaryFeatureId(Integer
					.parseInt(row.getValue(table.getColumnByName(LeapConstants.PRIMARY_FEATURE_ID)).toString().trim()));
		else
			config.setPrimaryFeatureId(0);
	}

}
