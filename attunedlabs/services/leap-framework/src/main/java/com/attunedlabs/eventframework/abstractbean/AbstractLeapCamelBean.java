package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.camel.EventBuilderInstantiationException;
import com.attunedlabs.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.attunedlabs.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.CamelEventBuilder;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventDispatchers;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.loggingfeature.bean.LoggingFeatureUtilitiesBean;

/**
 * This Class is responsible for generating events for beans
 * 
 * @author ubuntu
 *
 */
public abstract class AbstractLeapCamelBean {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractLeapCamelBean.class);

	/**
	 * This method is used to invoke preprocessing, process bean and postProcessing
	 * method
	 * 
	 * @param exch
	 * @throws Exception
	 */
	@Handler
	public void invokeBean(Exchange exch) throws Exception {
		preProcessing(exch);
		long processBeanStart;
		long processBeanEnd;
		long postProcessBeanStart;
		long postProcessBeanEnd;

		processBeanStart = System.currentTimeMillis();
		try {
			processBean(exch);
			processBeanEnd = System.currentTimeMillis();
		} catch (Exception exp) {
			processBeanEnd = System.currentTimeMillis();
			postProcessingForException(exch, exp);
			throw exp;
		}

		postProcessBeanStart = System.currentTimeMillis();
		try {
			postProcessing(exch);
			postProcessBeanEnd = System.currentTimeMillis();
		} catch (EventBuilderInstantiationException evtBuilderExp) {
			postProcessBeanEnd = System.currentTimeMillis();
			evtBuilderExp.printStackTrace();
		}
		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled)
			LoggingFeatureUtilitiesBean.addInternalComponentCalls(exch,
					new String[] { "processBean", "postProcessing" },
					new long[] { (processBeanEnd - processBeanStart), (postProcessBeanEnd - postProcessBeanStart) });
	}

	protected void preProcessing(Exchange exch) {
		logger.debug("PreProcessing Completed {}", LEAP_LOG_KEY);
	}

	abstract protected void processBean(Exchange exch) throws Exception;

	protected void postProcessing(Exchange exch) throws EventBuilderInstantiationException {
		logger.debug("PostProcessing Completed {}", LEAP_LOG_KEY);
		postProcessEventGeneration(exch);

	}

	protected void postProcessingForException(Exchange exch, Exception thrownExption) {
		logger.debug("postProcessingForException Completed {}", LEAP_LOG_KEY);
	}

	protected synchronized Connection getConnection(DataSource dataSource, Exchange camelExchange) throws SQLException {
		try {
			String methodName = "getConnection";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			logger.debug("{} leapServiceContext : {}", LEAP_LOG_KEY, leapServiceContext);
			Map<Object, Object> resourceHolderMap = leapServiceContext.getResourceHolder();
			Connection con = (Connection) resourceHolderMap.get(dataSource);
			if (con == null || con.isClosed()) {
				logger.debug("{} New Connection from dataSource {}", LEAP_LOG_KEY, dataSource);
				try {
					con = DataSourceUtils.getConnection(dataSource);
				} catch (CannotGetJdbcConnectionException e) {
					logger.warn("{} Error in getting the Connection from {}", LEAP_LOG_KEY, dataSource);
				}
				if (con.isClosed()) {
					con = DataSourceUtils.getConnection(dataSource);
				}
				resourceHolderMap.put(dataSource, con);
				logger.trace("{} connection and DS to HeaderMap {}", LEAP_LOG_KEY, dataSource);
				logger.trace("{} Added connection status :{} ", LEAP_LOG_KEY, con.isClosed());
			}
			// leapHeader.setResourceHolder(resourceHolderMap);
			camelExchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return con;
		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error("{} exception is : {}", LEAP_LOG_KEY, e.getMessage());
			throw e;
		}
	}

	/**
	 * This method is use to set the datasource and its connection object in the
	 * leap service context resource holder map
	 * 
	 * @param dataSource         : {@link DataSource}
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link Connection}
	 * @throws CannotGetJdbcConnectionException
	 * @throws SQLException
	 */
	protected synchronized Connection getConnection(DataSource dataSource, LeapServiceContext leapServiceContext)
			throws CannotGetJdbcConnectionException, SQLException {
		String methodName = "getConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<Object, Object> resourceHolderMap = leapServiceContext.getResourceHolder();
		Connection con = (Connection) resourceHolderMap.get(dataSource);
		if (con == null || con.isClosed()) {
			logger.debug("LeapDataAccessContext.Fetching New Connection from dataSource" + dataSource);
			con = DataSourceUtils.getConnection(dataSource);
			if (con.isClosed()) {
				con = DataSourceUtils.getConnection(dataSource);
			}
			resourceHolderMap.put(dataSource, con);
			logger.debug("LeapDataAccessContext.Added connection and DS to service context resource holder map"
					+ dataSource);
			logger.debug("Added connection status : " + con.isClosed());
		} else {
			logger.debug("connection object" + con + " already exist for ds " + dataSource);
		}
		return con;

	}// end of the method getConnection

	/**
	 * This method is used to generate events for bean
	 * 
	 * @param exch : camel exchange
	 * @throws EventBuilderInstantiationException
	 */
	private void postProcessEventGeneration(Exchange exch) throws EventBuilderInstantiationException {
		String methodName = "postProcessEventGeneration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exch.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		RequestContext reqContext = leapServiceContext.getRequestContext();
		String beanName = this.getClass().getName();
		String serviceName = leapServiceContext.getRunningContextServiceName();
		String featureName = leapServiceContext.getFeatureName();
		String requestId = leapServiceContext.getRequestUUID();
		String values = "bean name : " + beanName + ", featureName : " + featureName + ", request uuid : " + requestId
				+ ", service name : " + serviceName;
		logger.debug("{} {}", LEAP_LOG_KEY, values);
		ConfigurationContext configCtx1 = new ConfigurationContext(reqContext);
		EventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
		Event evtConfig;
		CamelEventProducer camelEvtProducer = null;
		try {
			evtConfig = evtConfigService.getEventConfigProducerForBean(configCtx1, serviceName, beanName);
			if (evtConfig != null) {
				camelEvtProducer = evtConfig.getCamelEventProducer();
			} // end of if(evtConfig!=null)
		} catch (EventFrameworkConfigurationException e) {
			throw new EventBuilderInstantiationException(
					"Exception while finding the Event to produce from configuration", e);
		}
		if (camelEvtProducer != null) {

			ICamelEventBuilder evtBuilder = null;
			CamelEventBuilder camelEvtBuilderConfig = camelEvtProducer.getCamelEventBuilder();

			if (camelEvtBuilderConfig.getType().equalsIgnoreCase(CamelEventProducerConstant.OGNL_EVENT_BUILDER)) {
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				// Event builder is of type Custom
				evtBuilder = getEventBuilderInstance(camelEvtBuilderConfig);

			}
			EventDispatchers eventDispatchers = evtConfig.getEventDispatchers();
			if (eventDispatchers != null) {
				for (EventDispatcher eventDispatcher : eventDispatchers.getEventDispatcher()) {
					String dispatchChannelId = eventDispatcher.getDispatchChannelId();
					LeapEvent leapevent = evtBuilder.buildEvent(exch, evtConfig);
					leapevent.setDispatchChannelId(dispatchChannelId);
					leapevent.addObject(LeapHeaderConstant.TENANT_KEY, leapServiceContext.getTenant());
					leapevent.addObject(LeapHeaderConstant.SITE_KEY, leapServiceContext.getSite());
					LeapEventContext.addLeapEvent(requestId, leapevent, leapServiceContext);
					logger.debug("{} Event for this Bean is EventId= {} created", LEAP_LOG_KEY, leapevent.toString());
				}
			} else {
				throw new EventBuilderInstantiationException("Unable to find EventDispatchers for Event");
			}
		} else {
			logger.debug("{} No Event found for this Bean", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to generate custom event builder for the bean events
	 * 
	 * @param cEvtbuilder : CamelEventBuilder object
	 * @return AbstractCamelEventBuilder
	 * @throws EventBuilderInstantiationException
	 */
	// #TODO THIS WILL NOT WORK IN osgi.TO CHANGED EITHER loaded spring or osgi
	// registry
	private ICamelEventBuilder getEventBuilderInstance(CamelEventBuilder cEvtbuilder)
			throws EventBuilderInstantiationException {
		String fqcn = cEvtbuilder.getEventBuilder().getFqcn();
		logger.debug("fcqn :{} {} ", LEAP_LOG_KEY, fqcn);
		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		try {
			ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) beanResolver
					.getBeanInstance(ICamelEventBuilder.class, fqcn);
			return evtBuilderInstance;
		} catch (BeanDependencyResolveException e) {
			logger.error("{} error in building custom eveint builder inside AbstractLeapCamelBean for :{} ",
					LEAP_LOG_KEY, fqcn, e);
			throw new EventBuilderInstantiationException("Failed to Load CustomEventBuilder", e);
		}
	}

	/**
	 * This method is to get OGNL Event Builder Instance
	 * 
	 * @return AbstractCamelEventBuilder
	 */
	private ICamelEventBuilder getOGNLEventBuilderInstance() {
		ICamelEventBuilder evtBuilderInstance = (ICamelEventBuilder) new OgnlEventBuilder();
		return evtBuilderInstance;
	}

}
