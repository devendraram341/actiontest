package com.attunedlabs.configdbtest;

import static com.attunedlabs.FeatureInstallerTestConstant.*;

import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;

public class DeleteConfigNodeForTesting {

	private ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();
	private static ConfigNodeDataDAO nodeDataDAO = new ConfigNodeDataDAO();

	/**
	 * Delete configNode Data after testing.
	 */
	public DeleteConfigNodeForTesting() {
		try {
			nodeDataDAO.deleteConfigNodeDataByNodeId(TEST_VENDOR_NODEID);
			deleteConfigNode();
		} catch (ConfigNodeDataConfigurationException | ConfigNodeConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void deleteConfigNode() throws ConfigNodeConfigurationException {
		configNodeDAO.deleteNodeByNodeId(TEST_FEATUREGROUP_NODEID);
		configNodeDAO.deleteNodeByNodeId(TEST_FEATURE_NODEID);
		configNodeDAO.deleteNodeByNodeId(TEST_IMPL_NODEID);
		configNodeDAO.deleteNodeByNodeId(TEST_VENDOR_NODEID);

		TEST_FEATUREGROUP_NODEID = 0;
		TEST_FEATURE_NODEID = 0;
		TEST_IMPL_NODEID = 0;
		TEST_VENDOR_NODEID = 0;
	}

}
