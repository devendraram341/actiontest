package com.attunedlabs.leap.servicehandlers.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.ALL_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.ASYNC_EXECUTE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.ASYNC_ROUTE_NAME_KEY;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.ASYNC_ROUTE_NAME_VALUE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.INSTANCE_HANDLER_INVOCATION;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.INVOKE_TYPE_KEY;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.LOOKUP_HANDLER_INVOCATION;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_EXEC_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_EXEC_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_EXEC_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_EXEC_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_SERVICE_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_SERVICE_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_SERVICE_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.POST_SERVICE_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_ENRICHMENT_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_ENRICHMENT_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_ENRICHMENT_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_ENRICHMENT_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_EXEC_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_ENRICHMENT_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_ENRICHMENT_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_ENRICHMENT_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_ENRICHMENT_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_SELECTION_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_SELECTION_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_SELECTION_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_SELECTION_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_IMPL_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_SERVICE_ASYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_SERVICE_INVOCATION_ASYNC_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_SERVICE_INVOCATION_TYPE;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.PRE_SERVICE_SYNC_HANDLERS;
import static com.attunedlabs.servicehandlers.constant.SeviceHandlerConstant.SYNC_EXECUTE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.servicehandlers.AbstractServiceHandler;
import com.attunedlabs.servicehandlers.config.IServiceHandlerConfigurationService;
import com.attunedlabs.servicehandlers.config.impl.HandlerCacheSerivce;
import com.attunedlabs.servicehandlers.config.impl.ServiceHandlerConfigurationHelper;
import com.attunedlabs.servicehandlers.config.impl.ServiceHandlerConfigurationService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * <code>ServiceHandlersBean</code> is bean to fetch the handlers associated
 * with particular service and store in the respective list of handlers.
 * 
 * <xs:enumeration value="pre-service" /> <xs:enumeration value="pre-exec" />
 * <xs:enumeration value="pre-exec-enrichment" />
 * <xs:enumeration value="pre-impl-selection" />
 * <xs:enumeration value="pre-impl-enrichment" />
 * <xs:enumeration value="pre-impl" /></b> <xs:enumeration value="post-exec" />
 * <xs:enumeration value="post-service" /> </n> <xs:enumeration value="*" />
 * 
 * @author Reactiveworks42
 *
 */
public class ServiceHandlersBean {
	final static Logger logger = LoggerFactory.getLogger(ServiceHandlersBean.class);
	IServiceHandlerConfigurationService iServiceHandlerConfigurationService = new ServiceHandlerConfigurationService();
	private HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
	private IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();

	/**
	 * get the pre and post handlers and add the handlers configuration into
	 * particular list of handlers.
	 * 
	 * @param exchange
	 * @param handlerType
	 * @throws Exception
	 */
	public void fetchHandlers(Exchange exchange) throws Exception {
		String methodName = "fetchHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// LeapHeader leapHeader = (LeapHeader)
		// exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		// get the leapDataContext, leapServiceContext and
		// leapServiceRuntimeContext
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		LeapServiceRuntimeContext leapServiceRuntimeContext = leapServiceContext.getCurrentLeapServiceRuntimeContext();

		RequestContext requestContext = leapServiceContext.getRequestContext();
		long start = System.currentTimeMillis();
		// getting the global level service handlers CSH
		int globalNodeId = iServiceHandlerConfigurationService.getConfigNodeId(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, requestContext.getVendor(), requestContext.getImplementationName(),
				requestContext.getVersion(), requestContext.getFeatureGroup(), requestContext.getFeatureName());
		logger.debug("{} globalNodeId : {}", LEAP_LOG_KEY, globalNodeId);
		List<String> globalhandlersForService = iServiceHandlerConfigurationService
				.getApplicationLevelHandlersForService(globalNodeId, leapServiceRuntimeContext.getServiceName());

		// get tenant specific service handlers AFSH and FFSH
		if (!(leapServiceContext.getTenant().equals(LeapHeaderConstant.tenant)
				&& leapServiceContext.getSite().equals(LeapHeaderConstant.site))) {
			logger.debug("{} Now fetching handlers for actual tenant:site : {}", LEAP_LOG_KEY,
					leapServiceContext.getTenant() + " : " + leapServiceContext.getSite());
			int specificNodeId = iServiceHandlerConfigurationService.getConfigNodeId(requestContext.getTenantId(),
					requestContext.getSiteId(), requestContext.getVendor(), requestContext.getImplementationName(),
					requestContext.getVersion(), requestContext.getFeatureGroup(), requestContext.getFeatureName());
			List<String> appSpecifichandlersForService = iServiceHandlerConfigurationService
					.getApplicationLevelHandlersForService(specificNodeId, leapServiceRuntimeContext.getServiceName());
			List<String> featureSpecifichandlersForService = iServiceHandlerConfigurationService
					.getFeatureLevelHandlersForService(specificNodeId, leapServiceRuntimeContext.getServiceName());
			globalhandlersForService.addAll(appSpecifichandlersForService);
			globalhandlersForService.addAll(featureSpecifichandlersForService);
		} else {
			// getting global level AFSH
			List<String> featureSpecifichandlersForService = iServiceHandlerConfigurationService
					.getFeatureLevelHandlersForService(globalNodeId, leapServiceRuntimeContext.getServiceName());
			globalhandlersForService.addAll(featureSpecifichandlersForService);
		}
		logger.debug("{} globalhandlersForService : {}", LEAP_LOG_KEY, globalhandlersForService);
		getPreAndPostHandlers(globalhandlersForService, leapServiceContext, exchange);
		long end = System.currentTimeMillis();
		logger.debug("{} Total time taken(fetchHandlers): {} ", LEAP_LOG_KEY, (end - start));
	}

	/**
	 * The method is use to decide whether to invoke handler for pre-service sync or
	 * async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preServiceSyncHandler><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preServiceAsyncHandler><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreService(String syncServiceInvocationKey, String asyncServiceInvocationKey,
			Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPreService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} Service Name is ::{}", serviceName);
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPreService: {}", syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPreService: {}", asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_SERVICE_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_SERVICE_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-handler service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			logger.debug("{} {}", PRE_SERVICE_SYNC_HANDLERS, serviceName);
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_SERVICE_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_SERVICE_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreService

	/**
	 * The method is use to decide whether to invoke handler for post-service sync
	 * or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <postServiceSyncHandler><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <postServiceAsyncHandler><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPostService(String syncServiceInvocationKey, String asyncServiceInvocationKey,
			Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPostService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPostService: {}", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPostService: {}", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(POST_SERVICE_ASYNC_HANDLERS + serviceName)) {
				logger.debug("{} post service async invoker", LEAP_LOG_KEY);
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, POST_SERVICE_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async post-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(POST_SERVICE_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(POST_SERVICE_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync post-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPostService

	/**
	 * The method is use to decide whether to invoke handler for pre-exec-service
	 * sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like <preExecSyncHandler><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like <preExecAsyncHandler><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreExecService(String syncServiceInvocationKey, String asyncServiceInvocationKey,
			Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPreExecService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPreExecService: {}", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPreExecService: {}", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_EXEC_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_EXEC_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-exec-service handler is defined for the service: {}", serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_EXEC_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_EXEC_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-exec-service handler is defined for the service: {}", serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreExecService

	/**
	 * The method is use to decide whether to invoke handler for post-exec-service
	 * sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like <postExecSyncHandler><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like <postExecAsyncHandler><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPostExecService(String syncServiceInvocationKey, String asyncServiceInvocationKey,
			Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPostExecService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPostExecService: {}", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("asyncServiceInvocationKey  in syncOrAsyncDeciderForPostExecService:{} ", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(POST_EXEC_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, POST_EXEC_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async post-exec-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(POST_EXEC_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(POST_EXEC_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync post-exec-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPostExecService

	/**
	 * The method is use to decide whether to invoke handler for
	 * pre-exec-enrichment-service sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preExecEnricherSyncHandlers><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preExecEnricherAsyncHandlers><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreExecEnrichService(String syncServiceInvocationKey,
			String asyncServiceInvocationKey, Exchange exchange) {
		String methodName = "fetchHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPreExecEnrichService: {}", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPreExecEnrichService: ", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_EXEC_ENRICHMENT_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_EXEC_ENRICHMENT_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-exec-enrich-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_EXEC_ENRICHMENT_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_EXEC_ENRICHMENT_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-exec-enrich-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreExecEnrichService

	/**
	 * The method is use to decide whether to invoke handler for
	 * pre-impl-selection-service sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preImplSelectionSyncHandlers><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preImplSelectionAsyncHandlers><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreImplSelectionService(String syncServiceInvocationKey,
			String asyncServiceInvocationKey, Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPreImplSelectionService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPostService:{} ", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPostService:{} ", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_SELECTION_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_IMPL_SELECTION_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-impl-selection-service handler is defined for the service:{} ",
						LEAP_LOG_KEY, serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_SELECTION_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_IMPL_SELECTION_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-impl-selection-service handler is defined for the service:{} ",
						LEAP_LOG_KEY, serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreImplSelectionService

	/**
	 * The method is use to decide whether to invoke handler for
	 * pre-impl-enrichment-service sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preImplEnricherSyncHandlers><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like
	 *                                  <preImplEnricherAsyncHandlers><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreImplEnrichService(String syncServiceInvocationKey,
			String asyncServiceInvocationKey, Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPreImplEnrichService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPreImplEnrichService: {}", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPreImplEnrichService: ", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_ENRICHMENT_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_IMPL_ENRICHMENT_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-impl-enrich-service handler is defined for the service:{} ", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_ENRICHMENT_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_IMPL_ENRICHMENT_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-impl-enrich-service handler is defined for the service: {}", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreImplEnrichService

	/**
	 * The method is use to decide whether to invoke handler for pre-impl-service
	 * sync or async way
	 * 
	 * @param syncServiceInvocationKey  : call the handlers in sync way [value will
	 *                                  be like <preImplSyncHandlers><servicename>]
	 * @param asyncServiceInvocationKey : call the handlers in sync way [value will
	 *                                  be like <preImplAsyncHandlers><servicename>]
	 * @param exchange                  : Camel Exchange Object
	 */
	public void syncOrAsyncDeciderForPreImplService(String syncServiceInvocationKey, String asyncServiceInvocationKey,
			Exchange exchange) {
		String methodName = "syncOrAsyncDeciderForPreImplService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} syncServiceInvocationKey  in syncOrAsyncDeciderForPreImplService:{} ", LEAP_LOG_KEY,
				syncServiceInvocationKey);
		logger.debug("{} asyncServiceInvocationKey  in syncOrAsyncDeciderForPreImplService:{} ", LEAP_LOG_KEY,
				asyncServiceInvocationKey);
		Object asyncHandlerRoute = exchange.getIn().getHeader(ASYNC_ROUTE_NAME_KEY);
		if (asyncHandlerRoute != null)
			exchange.getIn().removeHeader(ASYNC_ROUTE_NAME_KEY);

		if ((asyncServiceInvocationKey != null && !(asyncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (asyncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_ASYNC_HANDLERS + serviceName)) {
				exchange.getIn().setHeader(ASYNC_ROUTE_NAME_KEY, ASYNC_ROUTE_NAME_VALUE);
				exchange.getIn().setHeader(INVOKE_TYPE_KEY, PRE_IMPL_INVOCATION_TYPE);
			} else {
				logger.debug("{} No async pre-impl-service handler is defined for the service:{} ", LEAP_LOG_KEY,
						serviceName);
			}
		}
		if ((syncServiceInvocationKey != null && !(syncServiceInvocationKey.isEmpty()))
				&& (serviceName != null && !(serviceName.isEmpty()))) {
			if (syncServiceInvocationKey.equalsIgnoreCase(PRE_IMPL_SYNC_HANDLERS + serviceName))
				invokeSyncHandlers(PRE_IMPL_INVOCATION_TYPE, serviceName, leapServiceContext, exchange);
			else
				logger.debug("{} No sync pre-impl-service handler is defined for the service:{} ", LEAP_LOG_KEY,
						serviceName);
		}

	}// end of method syncOrAsyncDeciderForPreImplService

	@SuppressWarnings("unchecked")
	private void invokeSyncHandlers(String invoke, String serviceName, LeapServiceContext leapServiceContext,
			Exchange exchange) {
		String methodName = "invokeSyncHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> leapServiceHandlerMap = leapServiceContext.getServiceHandlerFromServiceContext();
		switch (invoke) {
		case PRE_SERVICE_INVOCATION_TYPE:
			List<AbstractServiceHandler> preHandlers = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_SERVICE_SYNC_HANDLERS + serviceName);
			if (preHandlers != null)
				for (AbstractServiceHandler iServiceHandler : preHandlers) {
					iServiceHandler.preService(exchange);
				}
			break;
		case POST_SERVICE_INVOCATION_TYPE:
			List<AbstractServiceHandler> postHandlers = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(POST_SERVICE_SYNC_HANDLERS + serviceName);
			if (postHandlers != null)
				for (AbstractServiceHandler iServiceHandler : postHandlers) {
					iServiceHandler.postService(exchange);
				}
			break;
		case PRE_EXEC_INVOCATION_TYPE:
			List<AbstractServiceHandler> preExecHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_EXEC_SYNC_HANDLERS + serviceName);
			if (preExecHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : preExecHandlerConfig) {
					iServiceHandler.preExec(exchange);
				}
			break;
		case POST_EXEC_INVOCATION_TYPE:
			List<AbstractServiceHandler> postExecHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(POST_EXEC_SYNC_HANDLERS + serviceName);
			if (postExecHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : postExecHandlerConfig) {
					iServiceHandler.postExec(exchange);
				}
			break;
		case PRE_EXEC_ENRICHMENT_INVOCATION_TYPE:
			List<AbstractServiceHandler> preExecEnrichHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_EXEC_ENRICHMENT_SYNC_HANDLERS + serviceName);
			if (preExecEnrichHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : preExecEnrichHandlerConfig) {
					iServiceHandler.preExecEnrichment(exchange);
				}
			break;
		case PRE_IMPL_SELECTION_INVOCATION_TYPE:
			List<AbstractServiceHandler> preImplSelectionHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_IMPL_SELECTION_SYNC_HANDLERS + serviceName);
			if (preImplSelectionHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : preImplSelectionHandlerConfig) {
					iServiceHandler.preImplSelection(exchange);
				}
			break;
		case PRE_IMPL_ENRICHMENT_INVOCATION_TYPE:
			List<AbstractServiceHandler> preImplEnrichHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_IMPL_ENRICHMENT_SYNC_HANDLERS + serviceName);
			if (preImplEnrichHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : preImplEnrichHandlerConfig) {
					iServiceHandler.preImplEnrichment(exchange);
				}
			break;
		case PRE_IMPL_INVOCATION_TYPE:
			List<AbstractServiceHandler> preImpHandlerConfig = (List<AbstractServiceHandler>) leapServiceHandlerMap
					.get(PRE_IMPL_SYNC_HANDLERS + serviceName);
			if (preImpHandlerConfig != null)
				for (AbstractServiceHandler iServiceHandler : preImpHandlerConfig) {
					iServiceHandler.preImpl(exchange);
				}
			break;
		}

	}

	@SuppressWarnings("unchecked")
	public void invokeAsyncHandlers(String invoke, Exchange exchange) {
		String methodName = "invokeAsyncHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext dataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = dataContext.getServiceDataContext();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		logger.debug("{} inside invokeAsyncHandlers...{} for the sevice :{} ", LEAP_LOG_KEY, invoke, serviceName);
		Map<String, Object> leapServiceHandlerMap = leapServiceContext.getServiceHandlerFromServiceContext();
		logger.debug("{} leapServiceHandlerMap ::{} ", LEAP_LOG_KEY, leapServiceHandlerMap);
		switch (invoke) {
		case PRE_SERVICE_INVOCATION_TYPE:
			List<String[]> preHandlersConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_SERVICE_ASYNC_HANDLERS + serviceName);
			logger.debug("{} Pre-Handler Map ::{}", LEAP_LOG_KEY, preHandlersConfig);
			if (preHandlersConfig != null)
				for (String[] iServiceconfig : preHandlersConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preService(exchange);
				}
			break;
		case POST_SERVICE_INVOCATION_TYPE:
			List<String[]> postHandlersConfig = (List<String[]>) leapServiceHandlerMap
					.get(POST_SERVICE_ASYNC_HANDLERS + serviceName);
			if (postHandlersConfig != null)
				for (String[] iServiceconfig : postHandlersConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.postService(exchange);
				}
			break;
		case PRE_EXEC_INVOCATION_TYPE:
			List<String[]> preExecHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_EXEC_ASYNC_HANDLERS + serviceName);
			if (preExecHandlerConfig != null)
				for (String[] iServiceconfig : preExecHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preExec(exchange);
				}
			break;
		case POST_EXEC_INVOCATION_TYPE:
			List<String[]> postExecHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(POST_EXEC_ASYNC_HANDLERS + serviceName);
			if (postExecHandlerConfig != null)
				for (String[] iServiceconfig : postExecHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.postExec(exchange);
				}
			break;
		case PRE_EXEC_ENRICHMENT_INVOCATION_TYPE:
			List<String[]> preExecEnrichHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_EXEC_ENRICHMENT_ASYNC_HANDLERS + serviceName);
			if (preExecEnrichHandlerConfig != null)
				for (String[] iServiceconfig : preExecEnrichHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preExecEnrichment(exchange);
				}
			break;
		case PRE_IMPL_SELECTION_INVOCATION_TYPE:
			List<String[]> preImplSelectionHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_IMPL_SELECTION_ASYNC_HANDLERS + serviceName);
			if (preImplSelectionHandlerConfig != null)
				for (String[] iServiceconfig : preImplSelectionHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preImplSelection(exchange);
				}
			break;
		case PRE_IMPL_ENRICHMENT_INVOCATION_TYPE:
			List<String[]> preImplEnrichHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_IMPL_ENRICHMENT_ASYNC_HANDLERS + serviceName);
			if (preImplEnrichHandlerConfig != null)
				for (String[] iServiceconfig : preImplEnrichHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preImplEnrichment(exchange);
				}
			break;
		case PRE_IMPL_INVOCATION_TYPE:
			List<String[]> preImpHandlerConfig = (List<String[]>) leapServiceHandlerMap
					.get(PRE_IMPL_ASYNC_HANDLERS + serviceName);
			if (preImpHandlerConfig != null)
				for (String[] iServiceconfig : preImpHandlerConfig) {
					AbstractServiceHandler handlerImpl = getHandlerImpl(iServiceconfig, exchange);
					if (handlerImpl != null)
						handlerImpl.preImpl(exchange);
				}
			break;
		}
	}

	/**
	 * parse the configuration and get the respective handlers in exchange.</br>
	 * handlerId|#ACSH/AFSH/FSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig},</br>
	 * handlerId|#ACSH/AFSH/FSH|#lookup|#{beanId}|#{per/post/both}|#{sync/async}|#{handlerConfig},...]}
	 * 
	 * 
	 * @param exchange
	 * @param globalhandlersForService
	 * @param leapHeader
	 * @param handlerType
	 */
	private void getPreAndPostHandlers(List<String> globalhandlersForService, LeapServiceContext leapServiceContext,
			Exchange exchange) {
		CamelContext camelContext = exchange.getContext();
		String runningContextServiceName = leapServiceContext.getRunningContextServiceName();
		for (String handlers : getServiceHandlersData(globalhandlersForService)) {
			String[] handlerConfig = handlers.split(
					ServiceHandlerConfigurationHelper.ESCAPE_CHAR + ServiceHandlerConfigurationHelper.VALUE_SEPERATOR);
			if (handlerConfig.length >= 6) {
				String handlerId = handlerConfig[0]; // handler Id -unique name
				String handlerKeyType = handlerConfig[1]; // ACSH or AFSH or
															// FFSH
				String getInstanceBy = handlerConfig[2]; // instance or lookup
				String classNameOrBeanId = handlerConfig[3]; // fqcn or beanId
				String invocation = handlerConfig[4]; // pre or post or both
				String processing = handlerConfig[5]; // sync or async
				String handlerConfigJSONStr = "";
				if (handlerConfig.length == 7)
					handlerConfigJSONStr = handlerConfig[6];
				switch (processing.toLowerCase()) {
				case SYNC_EXECUTE:
					switch (getInstanceBy) {
					case INSTANCE_HANDLER_INVOCATION:
						String tenant = LeapHeaderConstant.tenant;
						String site = LeapHeaderConstant.site;
						// handlerKey=<tenant>-<site>-<ACSH||AFSH||FFSH),
						// searching under global tenant
						String handlerKey = tenant + ServiceHandlerConfigurationHelper.KEY_SEPERATOR + site
								+ ServiceHandlerConfigurationHelper.KEY_SEPERATOR + handlerKeyType;
						AbstractServiceHandler serviceHandlerObj = HandlerCacheSerivce.getHandler(classNameOrBeanId,
								handlerKey, handlerConfigJSONStr);
						// need to look for logic leapHeaderConstant.tenant, as
						// we are going to replace leapHeader
						if (serviceHandlerObj == null
								&& (!(leapServiceContext.getTenant().equals(LeapHeaderConstant.tenant)
										&& leapServiceContext.getSite().equals(LeapHeaderConstant.site)))) {
							// searching under tenant specific
							tenant = leapServiceContext.getTenant();
							site = leapServiceContext.getSite();
							// handlerKey=<tenant>-<site>-<ACSH||AFSH||FFSH)
							handlerKey = tenant + ServiceHandlerConfigurationHelper.KEY_SEPERATOR + site
									+ ServiceHandlerConfigurationHelper.KEY_SEPERATOR + handlerKeyType;
							serviceHandlerObj = HandlerCacheSerivce.getHandler(classNameOrBeanId, handlerKey,
									handlerConfigJSONStr);
						}
						if (serviceHandlerObj == null)
							logger.error(LEAP_LOG_KEY
									+ "Unable to find handler with specifed FQCN in registry! Hence,particular handler with id "
									+ handlerId + " wont be invoked.");
						else
							updateExtensionPointForSyncHandlers(invocation, runningContextServiceName,
									serviceHandlerObj, leapServiceContext, exchange);
						break;
					case LOOKUP_HANDLER_INVOCATION:
						AbstractServiceHandler iServiceHandler = (AbstractServiceHandler) camelContext.getRegistry()
								.lookupByName(classNameOrBeanId);
						if (iServiceHandler != null) {
							if (!handlerConfigJSONStr.isEmpty())
								try {
									iServiceHandler.initializeConfiguration(
											(JSONObject) new JSONParser().parse(handlerConfigJSONStr));
								} catch (Exception e) {
									logger.error(LEAP_LOG_KEY + handlerConfig
											+ " -> failed to initialize the configuration while intantiation..."
											+ e.getMessage(), e);
								}
							updateExtensionPointForSyncHandlers(invocation, runningContextServiceName, iServiceHandler,
									leapServiceContext, exchange);
						} else
							logger.error(LEAP_LOG_KEY + " Unable to find Bean ref " + classNameOrBeanId
									+ " in camel registry by lookup...! Hence,particular handler with id " + handlerId
									+ " wont be invoked.");
						break;
					}
					break;
				case ASYNC_EXECUTE:
					updateExtensionPointForAsyncHandlers(invocation, runningContextServiceName, handlerConfig,
							leapServiceContext, exchange);
					break;
				}
			}
		}
	}

	/**
	 * updating the pre post list stored in exchange properites for each extention
	 * point the sync handler needs to be invoked.
	 * 
	 * <xs:enumeration value="pre-service" /> <xs:enumeration value="pre-exec" />
	 * <xs:enumeration value="pre-exec-enrichment" />
	 * <xs:enumeration value="pre-impl-selection" />
	 * <xs:enumeration value="pre-impl-enrichment" />
	 * <xs:enumeration value="pre-impl" /></b> <xs:enumeration value="post-exec" />
	 * <xs:enumeration value="post-service" /> </n> <xs:enumeration value="*" />
	 * 
	 * @param invocation
	 * @param serviceHandlerObj
	 * @param exchange
	 */
	private void updateExtensionPointForSyncHandlers(String invocation, String serviceName,
			AbstractServiceHandler serviceHandlerObj, LeapServiceContext leapServiceContext, Exchange exchange) {
		logger.debug("{} inside updateExtensionPointForSyncHandlers with invocation:{}, servicename : {}  ",
				LEAP_LOG_KEY, invocation, serviceName);
		if (invocation.equalsIgnoreCase(PRE_SERVICE_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_SERVICE_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_SERVICE_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(POST_SERVICE_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(POST_SERVICE_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					POST_SERVICE_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_EXEC_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_EXEC_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_EXEC_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(POST_EXEC_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(POST_EXEC_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					POST_EXEC_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_EXEC_ENRICHMENT_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_EXEC_ENRICHMENT_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_EXEC_ENRICHMENT_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_SELECTION_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_IMPL_SELECTION_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_IMPL_SELECTION_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_ENRICHMENT_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_IMPL_ENRICHMENT_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_IMPL_ENRICHMENT_INVOCATION_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateSyncProperties(PRE_IMPL_SYNC_HANDLERS + serviceName, serviceHandlerObj, leapServiceContext,
					PRE_IMPL_INVOCATION_TYPE, exchange);

	}

	/**
	 * updating the pre post list stored in exchange properties for each extention
	 * point the async handler needs to be invoked.
	 * 
	 * @param invocation
	 * @param handlerConfig
	 * @param exchange
	 */
	private void updateExtensionPointForAsyncHandlers(String invocation, String serviceName, String[] handlerConfig,
			LeapServiceContext leapServiceContext, Exchange exchange) {
		if (invocation.equalsIgnoreCase(PRE_SERVICE_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_SERVICE_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_SERVICE_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(POST_SERVICE_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(POST_SERVICE_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					POST_SERVICE_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_EXEC_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_EXEC_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_EXEC_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(POST_EXEC_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(POST_EXEC_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					POST_EXEC_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_EXEC_ENRICHMENT_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_EXEC_ENRICHMENT_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_EXEC_ENRICHMENT_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_SELECTION_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_IMPL_SELECTION_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_IMPL_SELECTION_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_ENRICHMENT_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_IMPL_ENRICHMENT_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_IMPL_ENRICHMENT_INVOCATION_ASYNC_TYPE, exchange);
		if (invocation.equalsIgnoreCase(PRE_IMPL_INVOCATION_TYPE) || invocation.equals(ALL_INVOCATION_TYPE))
			updateAsyncProperties(PRE_IMPL_ASYNC_HANDLERS + serviceName, handlerConfig, leapServiceContext,
					PRE_IMPL_INVOCATION_ASYNC_TYPE, exchange);

	}

	@SuppressWarnings("unchecked")
	private void updateSyncProperties(String type, AbstractServiceHandler serviceHandlerObj,
			LeapServiceContext leapServiceContext, String invokeType, Exchange exchange) {
		Map<String, Object> serviceHandlerMap = leapServiceContext.getServiceHandlerFromServiceContext();
		List<AbstractServiceHandler> syncHandlers = (List<AbstractServiceHandler>) serviceHandlerMap.get(type);
		if (syncHandlers == null) {
			syncHandlers = new ArrayList<>();
			serviceHandlerMap.put(type, syncHandlers);
			exchange.getIn().setHeader(invokeType, type);
		}
		syncHandlers.add(serviceHandlerObj);
	}

	@SuppressWarnings("unchecked")
	private void updateAsyncProperties(String type, String[] handlerConfig, LeapServiceContext leapServiceContext,
			String invokeType, Exchange exchange) {
		Map<String, Object> serviceHandlerMap = leapServiceContext.getServiceHandlerFromServiceContext();
		List<String[]> syncHandlers = (List<String[]>) serviceHandlerMap.get(type);
		if (syncHandlers == null) {
			syncHandlers = new ArrayList<>();
			serviceHandlerMap.put(type, syncHandlers);
			exchange.getIn().setHeader(invokeType, type);
		}
		syncHandlers.add(handlerConfig);
	}

	/**
	 * gets the handler data for each service.
	 * 
	 * @param globalhandlersForService
	 * @return serviceHandlers.
	 */
	private List<String> getServiceHandlersData(List<String> globalhandlersForService) {
		IMap<String, String> hanldersStoreACSHBeanRef = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.AppCommonServiceHanldersLookup);
		IMap<String, String> hanldersStoreACSHFQCN = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.AppCommonServiceHanldersFQCN);
		IMap<String, String> hanldersStoreAFSHBeanRef = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.AppFeatureServiceHanldersLookup);
		IMap<String, String> hanldersStoreAFSHFQCN = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.AppFeatureServiceHanldersFQCN);
		IMap<String, String> hanldersStoreFFSHBeanRef = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.FeatureServiceHanldersLookup);
		IMap<String, String> hanldersStoreFFSHFQCN = hazelcastInstance
				.getMap(ServiceHandlerConfigurationHelper.FeatureServiceHanldersFQCN);
		List<String> handlersDataForService = new ArrayList<>();
		for (String serviceHandlerId : globalhandlersForService) {
			// AFSH/ACSH/FFSH-{handlerId}
			String handlerType = serviceHandlerId.substring(0, 4);// AFSH/ACSH/FFSH
			String handlerId = serviceHandlerId.substring(5);// {handlerId}
			switch (handlerType) {
			case ServiceHandlerConfigurationHelper.AppCommonServiceHanlders:
				if (hanldersStoreACSHBeanRef.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreACSHBeanRef.get(handlerId));
				if (hanldersStoreACSHFQCN.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreACSHFQCN.get(handlerId));
				break;
			case ServiceHandlerConfigurationHelper.AppFeatureServiceHanlders:
				if (hanldersStoreAFSHBeanRef.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreAFSHBeanRef.get(handlerId));
				if (hanldersStoreAFSHFQCN.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreAFSHFQCN.get(handlerId));
				break;
			case ServiceHandlerConfigurationHelper.FeatureServiceHanlders:
				if (hanldersStoreFFSHBeanRef.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreFFSHBeanRef.get(handlerId));
				if (hanldersStoreFFSHFQCN.containsKey(handlerId))
					handlersDataForService.add(hanldersStoreFFSHFQCN.get(handlerId));
				break;
			}
		}
		return handlersDataForService;

	}

	private AbstractServiceHandler getHandlerImpl(String[] handlerConfig, Exchange exchange) {
		LeapHeader leapHeader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		CamelContext camelContext = exchange.getContext();

		if (handlerConfig.length >= 6) {
			String handlerId = handlerConfig[0];
			String handlerKeyType = handlerConfig[1];
			String getInstanceBy = handlerConfig[2];
			String classNameOrBeanId = handlerConfig[3];
			String invocation = handlerConfig[4];
			String handlerConfigJSONStr = "";
			if (handlerConfig.length == 7)
				handlerConfigJSONStr = handlerConfig[6];
			switch (getInstanceBy) {
			case INSTANCE_HANDLER_INVOCATION:
				String tenant = LeapHeaderConstant.tenant;
				String site = LeapHeaderConstant.site;
				String handlerKey = tenant + ServiceHandlerConfigurationHelper.KEY_SEPERATOR + site
						+ ServiceHandlerConfigurationHelper.KEY_SEPERATOR + handlerKeyType;
				AbstractServiceHandler serviceHandlerObj = HandlerCacheSerivce.getHandler(classNameOrBeanId, handlerKey,
						handlerConfigJSONStr);
				if (serviceHandlerObj == null && (!(leapHeader.getTenant().equals(LeapHeaderConstant.tenant)
						&& leapHeader.getSite().equals(LeapHeaderConstant.site)))) {
					tenant = leapHeader.getTenant();
					site = leapHeader.getSite();
					handlerKey = tenant + ServiceHandlerConfigurationHelper.KEY_SEPERATOR + site
							+ ServiceHandlerConfigurationHelper.KEY_SEPERATOR + handlerKeyType;
					serviceHandlerObj = HandlerCacheSerivce.getHandler(classNameOrBeanId, handlerKey,
							handlerConfigJSONStr);
				}
				if (serviceHandlerObj == null)
					logger.error(LEAP_LOG_KEY
							+ "Unable to find handler with specifed FQCN in registry! Hence,particular handler with id "
							+ handlerId + "  wont be invoked.");
				else
					return ifInvocationContains(invocation, serviceHandlerObj);
				break;
			case LOOKUP_HANDLER_INVOCATION:
				AbstractServiceHandler iServiceHandler = (AbstractServiceHandler) camelContext.getRegistry()
						.lookupByName(classNameOrBeanId);
				if (iServiceHandler != null) {
					if (!handlerConfigJSONStr.isEmpty())
						try {
							iServiceHandler
									.initializeConfiguration((JSONObject) new JSONParser().parse(handlerConfigJSONStr));
						} catch (Exception e) {
							logger.error(LEAP_LOG_KEY + handlerConfig
									+ " -> failed to initialize the configuration while intantiation..."
									+ e.getMessage(), e);
						}
					return ifInvocationContains(invocation, iServiceHandler);
				} else
					logger.error(LEAP_LOG_KEY + "Unable to find Bean ref " + classNameOrBeanId
							+ " in camel registry by lookup...! Hence,particular handler with id " + handlerId
							+ " wont be invoked.");
				break;
			}

		}
		return null;
	}

	/**
	 * just checking whether invocation is from one of the type.
	 * 
	 * @param invocation
	 * @param serviceHandlerObj
	 * @return
	 */
	private AbstractServiceHandler ifInvocationContains(String invocation, AbstractServiceHandler serviceHandlerObj) {
		switch (invocation.toLowerCase()) {
		case PRE_SERVICE_INVOCATION_TYPE:
			return serviceHandlerObj;
		case POST_SERVICE_INVOCATION_TYPE:
			return serviceHandlerObj;
		case PRE_EXEC_INVOCATION_TYPE:
			return serviceHandlerObj;
		case POST_EXEC_INVOCATION_TYPE:
			return serviceHandlerObj;
		case PRE_EXEC_ENRICHMENT_INVOCATION_TYPE:
			return serviceHandlerObj;
		case PRE_IMPL_SELECTION_INVOCATION_TYPE:
			return serviceHandlerObj;
		case PRE_IMPL_ENRICHMENT_INVOCATION_TYPE:
			return serviceHandlerObj;
		case PRE_IMPL_INVOCATION_TYPE:
			return serviceHandlerObj;
		case ALL_INVOCATION_TYPE:
			return serviceHandlerObj;
		}
		return null;
	}
}
