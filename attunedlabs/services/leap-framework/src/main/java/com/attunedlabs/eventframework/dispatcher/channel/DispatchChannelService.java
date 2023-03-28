package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.event.LeapBaseConfigEvent;
import com.attunedlabs.config.event.LeapConfigurationListener;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;

public class DispatchChannelService extends LeapConfigurationListener {
	private final static Logger logger = LoggerFactory.getLogger(DispatchChannelService.class);

	private static DispatchChannelService dispatchService;
	EventFrameworkConfigService eventFwkConfigService;
	private HashMap<String, Map<String, AbstractDispatchChannel>> channelMap;

	/**
	 * Singleton to ensure only one DispatchChannelService Exist
	 * 
	 * @return
	 */
	public static DispatchChannelService getDispatchChannelService() {
		if (dispatchService == null) {
			dispatchService = new DispatchChannelService();
		}
		return dispatchService;
	}

	/**
	 * Private Constructor for Singleton
	 */
	private DispatchChannelService() {
		eventFwkConfigService = new EventFrameworkConfigService();
		channelMap = new HashMap<>();
	}

	/**
	 * This method is to get dispatcher channel
	 * 
	 * @param tenantId  :String
	 * @param channelId : String
	 * @return AbstractDispatchChannel
	 * @throws NonRetryableMessageDispatchingException
	 */
	public AbstractDispatchChannel getDispatchChannel(RequestContext reqContext, String channelId)
			throws NonRetryableMessageDispatchingException {
		String methodName = "getDispatchChannel";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String tenantId = reqContext.getTenantId();
		String siteId = reqContext.getSiteId();

		Map tenantChannelMap = channelMap.get(tenantId + "-" + siteId);
		AbstractDispatchChannel dispatchChannel = null;
		if (tenantChannelMap != null)
			dispatchChannel = (AbstractDispatchChannel) tenantChannelMap.get(channelId);
		// found in the local Map
		if (dispatchChannel != null) {
			logger.debug("{} channel already :{}  instance of service {}", LEAP_LOG_KEY, dispatchChannel, channelMap);
			return dispatchChannel;
		}
		logger.debug("{} Channel not found in local initialized Map. Getting it and initializing it ChannelId={}",
				LEAP_LOG_KEY, channelId);
		// Not found in the Map lookup the configuration and initialize the
		// Channel
		ConfigurationContext configCtx = getConfigurationContext(reqContext);
		DispatchChannel disChannel;
		try {
			disChannel = eventFwkConfigService.getDispatchChannelConfiguration(configCtx, channelId);
			dispatchChannel = getChannelInstance(disChannel.getChannelImplementation().getFqcn(),
					disChannel.getChannelConfiguration());
			addDispatchChannelToLocalMap(dispatchChannel, channelId, configCtx);
			logger.trace("{} Channel {} initialized and added to local Map",LEAP_LOG_KEY, channelId);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return dispatchChannel;
		} catch (EventFrameworkConfigurationException | DispatchChannelInstantiationException
				| DispatchChannelInitializationException e) {
			logger.error("{} Failure In getting DispatchChannel {}", LEAP_LOG_KEY, channelId, e);
			throw new NonRetryableMessageDispatchingException("Failure In getting DispatchChannel {" + channelId + "}",
					e);
		}

	}

	private void addDispatchChannelToLocalMap(AbstractDispatchChannel channel, String channelId,
			ConfigurationContext reqCtx) {
		Map<String, AbstractDispatchChannel> tenantChannelMap = channelMap
				.get(reqCtx.getTenantId() + "-" + reqCtx.getSiteId());
		if (tenantChannelMap == null) {
			tenantChannelMap = new HashMap();
		}
		tenantChannelMap.put(channelId, channel);
		channelMap.put(reqCtx.getTenantId() + "-" + reqCtx.getSiteId(), tenantChannelMap);
	}

	/**
	 * This method is to get the instance of channel
	 * 
	 * @param fqcn       : String
	 * @param jsonConfig : String
	 * @return AbstractDispatchChannel
	 * @throws DispatchChannelInstantiationException
	 * @throws DispatchChannelInitializationException
	 */
	private AbstractDispatchChannel getChannelInstance(String fqcn, String jsonConfig)
			throws DispatchChannelInstantiationException, DispatchChannelInitializationException {
		logger.debug("{} inside getChannelInstance : {}", LEAP_LOG_KEY, fqcn);
		AbstractDispatchChannel channel = null;
		IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
		try {

			channel = (AbstractDispatchChannel) beanResolver.getBeanInstance(AbstractDispatchChannel.class, fqcn);
			channel.setChanneljsonconfig(jsonConfig);
		} catch (IllegalArgumentException | BeanDependencyResolveException exp) {
			logger.error("{} Failed to initialize DispatchChannel {}", fqcn, exp);
			throw new DispatchChannelInstantiationException("Failed to initialize DispatchChannel {" + fqcn + "}", exp);
		}
		return channel;

	}

	public void configAdded(LeapBaseConfigEvent configAddedEvent) {
		// DO Nothing as we are not interested in ConfigAdded Event
	}

	public void configUpdated(LeapBaseConfigEvent configUpdatedEvent) {

	}

	public void configRemoved(LeapBaseConfigEvent configRemovedEvent) {
	}

	public void configStatusChanged(LeapBaseConfigEvent configStatusChangedEvent) {
		// #TODO Proper coding is required
		// logger.debug("configStatusChanged- configStatusChangedEvent--in
		// dispacher channel service" + configStatusChangedEvent);
		// String tenantid = configStatusChangedEvent.getTenantId();
		// String configGroup = configStatusChangedEvent.getConfigGroup();
		// String key = configStatusChangedEvent.getConfigName();
		// ConfigurationUnit statusChangeconfigunit =
		// configStatusChangedEvent.getNewConfigUnit();
		// logger.info("tenatid : " + tenantid + " configGroup : " + configGroup
		// + " key : " + key);
		// configunitmap.put(key, statusChangeconfigunit);
		// logger.debug("------------logic to see if status value updated :
		// ---------");
		// ConfigurationUnit getmaptest = configunitmap.get(key);
		// logger.info("config unit is enabled : " + getmaptest.getIsEnabled());
	}

	private ConfigurationContext getConfigurationContext(RequestContext rqCtx) {
		ConfigurationContext configContext = new ConfigurationContext(rqCtx.getTenantId(), rqCtx.getSiteId(),
				rqCtx.getFeatureGroup(), rqCtx.getFeatureName(), rqCtx.getImplementationName(), rqCtx.getVendor(),
				rqCtx.getVersion());
		return configContext;
	}

}
