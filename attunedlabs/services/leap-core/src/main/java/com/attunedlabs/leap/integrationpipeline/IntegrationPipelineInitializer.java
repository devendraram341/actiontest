package com.attunedlabs.leap.integrationpipeline;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

public class IntegrationPipelineInitializer {
	final org.slf4j.Logger logger = LoggerFactory.getLogger(IntegrationPipelineInitializer.class);

	/**
	 * to initialize a few things, like LeapHeader params, before propagation
	 * 
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void loadPipeConfiguration(String piplineConfigName, Exchange exchange)
			throws InitializingPipelineException {
		String methodName = "loadPipeConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode vendorTreeNode;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		LeapConfigurationUtil leapConfigurationUtil = new LeapConfigurationUtil();
		RequestContext reqcontext;
		try {
			vendorTreeNode = tenantTreeService.getPrimaryVendorForFeature(serviceDataContext.getTenant(),
					serviceDataContext.getSite(), serviceDataContext.getFeatureGroup(),
					serviceDataContext.getFeatureName(), serviceDataContext.getImplementationName());
			String vendor = vendorTreeNode.getNodeName();
			String version = vendorTreeNode.getVersion();
			serviceDataContext.setVendor(vendor);
			serviceDataContext.setVersion(version);
			reqcontext = serviceDataContext.getRequestContext();

		} catch (UndefinedPrimaryVendorForFeature e1) {
			throw new InitializingPipelineException(
					"Unable to load the VendorTree for initializing PipeConfiguration..", e1);
		}
		logger.debug("{} To load we are calling:{}:{}:{} ", LEAP_LOG_KEY, piplineConfigName, serviceDataContext,
				exchange.getExchangeId());
		try {
			if (piplineConfigName != null && !(piplineConfigName.isEmpty())) {
				leapConfigurationUtil.getIntegrationPipelineConfiguration(piplineConfigName, reqcontext,
						serviceDataContext, exchange);
			} else {
				throw new InitializingPipelineException("pipeline configuration name is null");
			}
		} catch (IntegrationPipelineConfigException e) {
			throw new InitializingPipelineException("Unable to load the IntegrationPipeline from cache to leapHeader..",
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method
}