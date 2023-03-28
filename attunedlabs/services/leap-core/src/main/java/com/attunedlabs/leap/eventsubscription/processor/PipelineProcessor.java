package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Arrays;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.Pipeline;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.exception.PipelineInvokationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.integrationpipeline.InitializingPipelineException;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.IAccountRetrieveService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.attunedlabs.security.service.impl.AccountRetrieveServiceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class PipelineProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(PipelineProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public PipelineProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		try {
			if (exchange.getIn() != null) {

				// get the data from the exchange.
				JSONObject eventBody = subscriptionUtil.identifyContentType(exchange.getIn().getBody(String.class));
				String topicName = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
				String subscriptionId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY,
						String.class);

				Pipeline pipeline = exchange.getIn().getHeader(SubscriptionConstant.PIPELINE_KEY, Pipeline.class);

				List<String> configParams = Arrays
						.asList(subscriptionId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));

				if (configParams.size() - 1 == 5) {
					if (pipeline != null && pipeline.getIntegrationPipeName() != null) {
						String pipeLineRoute = SubscriptionConstant.ENTRY_ROUTE_FOR_PIPLINE;
						String inetegerationPipeline = pipeline.getIntegrationPipeName().get(0);

						String featureGroup = configParams.get(0);
						String featureName = configParams.get(1);
						String implementation = configParams.get(2);
						String vendor = configParams.get(3);
						String version = configParams.get(4);
						log.trace("{} camel endpoint need to verify: {} ", LEAP_LOG_KEY, pipeLineRoute);
						Endpoint endpoint = null;

						if (pipeLineRoute != null && !pipeLineRoute.isEmpty() && inetegerationPipeline != null)
							endpoint = exchange.getContext().hasEndpoint(pipeLineRoute);

						log.trace("{} camel endpoint verified to call : {}", LEAP_LOG_KEY, endpoint);

						// default attribute adding on every exchange for
						// the subscriber to identify topic.
						exchange.getOut().setHeader(KafkaConstants.TOPIC, topicName);
						exchange.getOut().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriptionId);
						exchange.getOut().setHeader(LeapHeaderConstant.FEATURE_GROUP_KEY, configParams.get(0));
						exchange.getOut().setHeader(LeapHeaderConstant.FEATURE_KEY, configParams.get(1));

						String tenant = serviceDataContext.getTenant();
						String site = serviceDataContext.getSite();

						if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
							throw new PipelineInvokationException(
									"TENANT/SITE DOESN'T EXISTS :tenantId and siteId not found in eventHeaders of "
											+ "event data pipeline configuration failed to load...");

						serviceDataContext.setFeatureGroup(featureGroup);
						serviceDataContext.setFeatureName(featureName);
						serviceDataContext.setImplementationName(implementation);
						serviceDataContext.setVendor(vendor);
						serviceDataContext.setVersion(version);
						serviceDataContext.setEndpointType("HTTP-JSON");

						// set time-zone for tenant
						IAccountRegistryService accountRegistryService = new AccountRegistryServiceImpl();
						String accountId = accountRegistryService.getAccountIdByTenant(tenant);
						AccountConfiguration configuration = getAccountConfiguration(accountId, site);
						String timeZoneId = TimeZone.getDefault().getID();
						try {
							timeZoneId = configuration.getTimezone();
							if (timeZoneId == null || timeZoneId.isEmpty()) {
								log.trace("{} timezone not found for tenant and site {} : {}", LEAP_LOG_KEY, tenant,
										site);
								timeZoneId = TimeZone.getDefault().getID();
							}
						} catch (Exception e) {
							log.error("{} timezone not found for tenant {} and site {} due to  :{}", LEAP_LOG_KEY,
									tenant, site, e.getMessage());
							e.printStackTrace();
						}
						exchange.getOut().setHeader(LeapHeaderConstant.TIMEZONE, timeZoneId);

						exchange.getOut().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
						exchange.getOut().setBody(eventBody.toString());

						// load pipe config
						loadPipeConfiguration(tenant.trim(), site.trim(), featureGroup.trim(), featureName.trim(),
								implementation.trim(), vendor.trim(), version.trim(), inetegerationPipeline.trim(),
								exchange);

						if (endpoint != null)
							exchange.getOut().setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, pipeLineRoute);
						else
							throw new PipelineInvokationException(
									"NO PIPELINE CONSUMER-ENDPOINT AVAILABLE:- the route endpoint mentioned  "
											+ pipeLineRoute + "   doesn't exist ==> " + pipeLineRoute);
					} else
						throw new PipelineInvokationException(
								"NO PIPELINE_INVOCATION  ACTION FOUND :- No pipeline to invoke either specify integerationPipeline or mention endpointConsumer correctly");
					log.info("{} CamelExchange before pipeline-call: Headers: {}", LEAP_LOG_KEY,
							exchange.getOut().getHeaders());
					log.info("{} CamelExchange before pipeline-call: Body: {}", LEAP_LOG_KEY,
							exchange.getOut().getBody());
				}
			} else
				throw new PipelineInvokationException(
						"REQUEST CONTEXT BUILD FAILED :- Pipeline invokation cannot be done request context to fetch configuration failed to build");
		} catch (

		Exception e) {
			e.printStackTrace();
			if (exchange.hasOut())
				exchange.getOut().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			else
				exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);

		}
	}

	private AccountConfiguration getAccountConfiguration(String accountName, String internalSiteId)
			throws AccountFetchException {
		String methodName = "getAccountConfiguration";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (accountName.isEmpty() && internalSiteId.isEmpty())
			throw new AccountFetchException("siteId or accountId is missing in the header.");
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, AccountConfiguration> configurations = hazelcastInstance
				.getMap(TenantSecurityConstant.EXTERNAL_ACCOUNT_CONFIG);
		AccountConfiguration configuration = configurations.get(accountName + "-" + internalSiteId);
		if (configuration == null) {
			IAccountRetrieveService retrieveService = new AccountRetrieveServiceImpl();
			AccountConfiguration detailsConfiguration = retrieveService.getAccountDetailConfiguration(accountName);
			if (detailsConfiguration != null) {
				configurations.put(accountName + "-" + internalSiteId, detailsConfiguration);
				return detailsConfiguration;
			} else
				throw new AccountFetchException(
						internalSiteId + " is not available under the given account : " + accountName);
		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configuration;
	}

	/**
	 * to initialize a few things, like LeapHeader params, before propagation
	 * 
	 * @param exchange
	 * @throws InitializingPipelineException
	 */
	public void loadPipeConfiguration(String tenant, String site, String featureGroup, String featureName, String impl,
			String vendor, String version, String piplineConfigName, Exchange exchange)
			throws InitializingPipelineException {
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode vendorTreeNode;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getOut().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();

		RequestContext reqcontext;
		try {
			vendorTreeNode = tenantTreeService.getPrimaryVendorForFeature(serviceDataContext.getTenant(),
					serviceDataContext.getSite(), serviceDataContext.getFeatureGroup(),
					serviceDataContext.getFeatureName());
			serviceDataContext.setVendor(vendor);
			serviceDataContext.setVersion(version);
			reqcontext = new RequestContext(serviceDataContext.getTenant(), serviceDataContext.getSite(),
					serviceDataContext.getFeatureGroup(), serviceDataContext.getFeatureName(),
					serviceDataContext.getImplementationName(), vendor, version);
			serviceDataContext.setRequestContext(reqcontext);
		} catch (UndefinedPrimaryVendorForFeature e1) {
			throw new InitializingPipelineException(
					"Unable to load the VendorTree for initializing PipeConfiguration..", e1);
		}
		log.trace("{} To load we are calling: {} {}", LEAP_LOG_KEY, piplineConfigName,
				serviceDataContext + " " + exchange.getExchangeId());
		try {
			if (piplineConfigName != null && !(piplineConfigName.isEmpty())) {
				getIntegrationPipelineConfiguration(piplineConfigName, reqcontext, serviceDataContext, exchange);
			} else {
				throw new InitializingPipelineException("pipeline configuration name is null");
			}
		} catch (IntegrationPipelineConfigException e) {
			throw new InitializingPipelineException("Unable to load the IntegrationPipeline from cache to leapHeader..",
					e);
		}
	}// ..end of the method

	/**
	 * the newly added integrationPipeline configuration object, retrieved from the
	 * leap header
	 * 
	 * @param configName
	 * @param exchange
	 * @throws IntegrationPipelineConfigException
	 */
	public void getIntegrationPipelineConfiguration(String configName, RequestContext reqcontext,
			LeapServiceContext serviceDataContext, Exchange exchange) throws IntegrationPipelineConfigException {
		String methodName = "getIntegrationPipelineConfiguration";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		log.debug("{} Request Context: {} ", LEAP_LOG_KEY, reqcontext);

		IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipeLineConfigurationService
				.getIntegrationPipeConfiguration(reqcontext, configName);
		RequestContext request = new RequestContext(reqcontext.getTenantId(), reqcontext.getSiteId(),
				reqcontext.getFeatureGroup(), reqcontext.getFeatureName(), reqcontext.getImplementationName(),
				reqcontext.getVendor(), reqcontext.getVersion());
		if (pipelineConfigUnit == null) {
			// tO GET PIPElINE dETAILS FROM DEFAULT TENANT GROUP, take it from
			// framework

			request.setTenantId(LeapHeaderConstant.tenant);
			request.setSiteId(LeapHeaderConstant.site);
			pipelineConfigUnit = pipeLineConfigurationService.getIntegrationPipeConfiguration(request, configName);
			log.trace("{} pipeLineConfiguration Unit After setting the tenant And Site as All :{} ", LEAP_LOG_KEY,
					pipelineConfigUnit.toString());
		}

		// ......TestSnipet....
		exchange.getOut().setHeader("pipeActivityKey", pipelineConfigUnit);
		// .......TestSniper-Ends......

		Map<String, Object> integrationCahedObject = serviceDataContext.getIntegrationPipelineFromServiceContext();
		if (integrationCahedObject == null)
			integrationCahedObject = new HashMap<String, Object>();
		integrationCahedObject.put(LeapHeaderConstant.PIPELINE_CONFIG_KEY, pipelineConfigUnit);
		log.debug("exiting from the {}", methodName);
	}// ..end of the method

}
