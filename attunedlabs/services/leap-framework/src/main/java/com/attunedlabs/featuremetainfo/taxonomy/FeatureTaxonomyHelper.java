package com.attunedlabs.featuremetainfo.taxonomy;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.GenericApplicableNode;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;

public class FeatureTaxonomyHelper extends GenericApplicableNode {
	final Logger logger = LoggerFactory.getLogger(FeatureTaxonomyHelper.class);
	private Integer configNodeId = null;
	private JSONObject taxonomyNames = null;

	public JSONObject getTaxonomyNames() {
		return taxonomyNames;
	}

	public void setTaxonomyNames(JSONObject taxonomyNames) {
		this.taxonomyNames = taxonomyNames;
	}

	public Integer getConfigNodeId() {
		return configNodeId;
	}

	public void setConfigNodeId(Integer configNodeId) {
		this.configNodeId = configNodeId;
	}

	public void addTaxonomyName(ConfigurationContext configContext, String fileName) throws TaxonomyConfigException {
		String methodName = "addTaxonomyName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId;
		try {

			String tenantId = configContext.getTenantId();
			String siteId = configContext.getSiteId();
			String vendorName = configContext.getVendorName();
			String version = configContext.getVersion();
			String featureGroup = configContext.getFeatureGroup();
			String featureName = configContext.getFeatureName();
			String implementation = configContext.getImplementationName();

//			logger.debug("ConfigurationContext-Object: tenantId-" + tenantId + ", siteId-" + siteId + ", vendorName-"
//					+ vendorName + ", version-" + version + ", featureGroup-" + featureGroup + ", featureName-"
//					+ featureName + ", impl name : " + implementation);
			configNodeId = getConfigNodeId(tenantId, siteId, vendorName, implementation, version, featureGroup,
					featureName);
			loadConfigurationInDataGrid(generateTaxonomyStoreKey(configNodeId), fileName);
		} catch (Exception e) {
			throw new TaxonomyConfigException("Failed to add taxonomy config file name for feature " + configContext,
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public Integer initializeConfigNodeId(ConfigurationContext configContext) throws TaxonomyConfigException {
		String methodName = "initializeConfigNodeId";
		logger.debug("{} entered into the method {}, configContext={} ", LEAP_LOG_KEY, methodName,configContext);
		try {
			if (configNodeId == null) {
				if (!configContext.getVendorName().isEmpty()) {
					configNodeId = getApplicableNodeIdVendorName(configContext.getTenantId(), configContext.getSiteId(),
							configContext.getFeatureGroup(), configContext.getFeatureName(),
							configContext.getImplementationName(), configContext.getVendorName(),
							configContext.getVersion());
				} else {
					configNodeId = getApplicableNodeIdFeatureName(configContext.getTenantId(),
							configContext.getSiteId(), configContext.getFeatureGroup(), configContext.getFeatureName(),
							configContext.getImplementationName());
				}
			}
		} catch (Exception e) {
			throw new TaxonomyConfigException("Failed to get taxonomy config file name for feature " + configContext,
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configNodeId;
	}

	/**
	 * Method to get the Root tag for a JSONObject
	 * 
	 * @param fileName
	 * @return
	 */
	private String getTaxonomyFileFromClassPath(String fileName) {
		String result = "";
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
		} catch (IOException e) {
			//logger.error("Taxonomy file not Found ! " + fileName + " -> " + e.getMessage(), e);
		}
		return result;
	}

	public String getTaxonomy(String actualName) {
		try {
			if (getTaxonomyNames() == null) {
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				String fileName = configServer.getConfiguration(generateTaxonomyStoreKey(configNodeId));
				taxonomyNames = new JSONObject(getTaxonomyFileFromClassPath(fileName));
			}
		} catch (Exception e) {
			//logger.warn("Failed to get taxonomy config file name for feature with nodeId" + configNodeId);
		}
		if (getTaxonomyNames() != null && taxonomyNames.has(actualName))
			return taxonomyNames.getString(actualName.trim());
		else
			return actualName.toUpperCase();

	}

	private void loadConfigurationInDataGrid(String taxCofigKey, String filename) throws TaxonomyConfigException {
		//logger.debug(".loadConfigurationInDataGrid() EventFrameworkConfigurationUnit=" + taxCofigKey);
		try {
			// we upload in cache only when enabled
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.addConfiguration(taxCofigKey, filename);

		} catch (ConfigServerInitializationException e) {
			throw new TaxonomyConfigException("Failed to Upload Taxonomy Config in DataGrid configName=" + taxCofigKey,
					e);
		}
	}
	
	/**
	 * locally invoked to get the configurationNodeId , once insertion is
	 * success full, checks for the version availability and when not available
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param vendorName
	 * @param version
	 * @param featureGroup
	 * @param featureName
	 * @return ConfigurationNodeId, after inserting the data
	 * @throws TaxonomyConfigException
	 */
	private int getConfigNodeId(String tenantId, String siteId, String vendorName, String implName, String version,
			String featureGroup, String featureName) throws TaxonomyConfigException {
		String methodName = "getConfigNodeId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int configNodeId = 0;
		try {
			if (!vendorName.isEmpty() && !version.isEmpty()) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName, implName,
						vendorName, version);
				//logger.debug("Applicable nodeId is.." + configNodeId);
			} else if (vendorName.isEmpty() && version.isEmpty()) {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName, implName);
				//logger.debug("Applicable nodeId is.." + configNodeId);
			} // ..end of if-else, conditional check with vendor-version support
		} catch (InvalidNodeTreeException | ConfigPersistenceException persistanceException) {
			throw new TaxonomyConfigException(
					"Failed loading nodeId, when version and vendor is empty for tenantId-" + tenantId + ", siteId-"
							+ siteId + ", vendorName-" + vendorName + ", version-" + version + ", featureGroup-"
							+ featureGroup + ", featureName-" + featureName + ", impl name : " + implName,
					persistanceException);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configNodeId;
	}// ..end of the method

	private String generateTaxonomyStoreKey(Integer configNodeId) {
		return configNodeId + "_TX";
	}

}
