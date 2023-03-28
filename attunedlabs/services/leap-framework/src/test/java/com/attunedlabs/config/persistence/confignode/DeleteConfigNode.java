package com.attunedlabs.config.persistence.confignode;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.persistence.dao.ConfigNodeDAO;
import com.attunedlabs.config.persistence.dao.ConfigNodeDataDAO;
import com.attunedlabs.config.persistence.exception.ConfigNodeConfigurationException;
import com.attunedlabs.config.persistence.exception.ConfigNodeDataConfigurationException;

public class DeleteConfigNode {

	private ConfigNodeDAO configNodeDAO = new ConfigNodeDAO();
	private static ConfigNodeDataDAO nodeDataDAO = new ConfigNodeDataDAO();

	public DeleteConfigNode() {
		try {
			nodeDataDAO.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
			deleteConfigNode();
		} catch (ConfigNodeDataConfigurationException | ConfigNodeConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void deleteConfigNode() throws ConfigNodeConfigurationException {
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_TENANT_NODEID);
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_SITE_NODEID);
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_FEATUREGROUP_NODEID);
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_FEATURE_NODEID);
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_IMPL_NODEID);
		configNodeDAO.deleteNodeByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);

		GenericTestConstant.TEST_TENANT_NODEID = 0;
		GenericTestConstant.TEST_SITE_NODEID = 0;
		GenericTestConstant.TEST_FEATUREGROUP_NODEID = 0;
		GenericTestConstant.TEST_FEATURE_NODEID = 0;
		GenericTestConstant.TEST_IMPL_NODEID = 0;
		GenericTestConstant.TEST_VENDOR_NODEID = 0;
	}

}
