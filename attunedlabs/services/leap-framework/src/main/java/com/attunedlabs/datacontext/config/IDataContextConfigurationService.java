package com.attunedlabs.datacontext.config;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigParserException;

public interface IDataContextConfigurationService {
	public void addDataContext(ConfigurationContext configContext,FeatureDataContext featureDataContext) throws  DataContextConfigurationException;
	public DataContextConfigurationUnit getDataContextConfiguration(RequestContext requestContext) throws  DataContextConfigurationException;
	public boolean deleteDataContextConfiguration(ConfigurationContext configContext) throws DataContextConfigurationException;
	public boolean checkDataContextConfigExistOrNot(ConfigurationContext configurationContext,
			String configName) throws DataContextParserException, DataContextConfigurationException;
	
	public boolean reloadDataContextCacheObject(RequestContext requestContext, String configName)
			throws DataContextConfigurationException;
}
