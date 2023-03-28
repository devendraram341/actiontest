package com.attunedlabs.servicehandlers.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.core.JavaBeanDependencyResolver;
import com.attunedlabs.servicehandlers.AbstractServiceHandler;

/**
 * <code>HandlerCacheSerivce</code> Cache service for getting cached instance of
 * handler class.
 * 
 * @author Reactiveworks42
 *
 */
public class HandlerCacheSerivce {
	static Map<String, HashMap<String, AbstractServiceHandler>> handlerStore = new HashMap<>();
	static IBeanDependencyResolver beanDependencyResolver = new JavaBeanDependencyResolver();
	final static Logger logger = LoggerFactory.getLogger(HandlerCacheSerivce.class);

	/**
	 * storing handler instance : </br>
	 * {tenant}-{site}-ACSH/AFSH/FSH --> {hanlder0-> instanceOfHanlder}
	 * 
	 * @param handlerConfig
	 * @param string
	 * @return
	 * @throws BeanDependencyResolveException
	 */
	public static AbstractServiceHandler initializeHandler(String hanlderfqcn, String hanlderkey, String handlerConfig)
			throws BeanDependencyResolveException {
		String methodName = "initializeHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		HashMap<String, AbstractServiceHandler> hanlderMapData = handlerStore.get(hanlderkey);
		if (hanlderMapData == null)
			hanlderMapData = new HashMap<>();
		AbstractServiceHandler iServiceHandler = (AbstractServiceHandler) beanDependencyResolver
				.getBeanInstance(AbstractServiceHandler.class.getName(), hanlderfqcn);
		if (handlerConfig != null && !handlerConfig.trim().isEmpty())
			try {
				iServiceHandler.initializeConfiguration((JSONObject) new JSONParser().parse(handlerConfig));
			} catch (Exception e) {
				logger.error("{}, {}:{} -> failed to initialize the configuration while intantiation...{} ",
						LEAP_LOG_KEY, hanlderfqcn, handlerConfig, e.getMessage(), e);
			}
		hanlderMapData.put(hanlderfqcn, iServiceHandler);
		handlerStore.put(hanlderkey, hanlderMapData);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return iServiceHandler;
	}

	/**
	 * get the cached instance of handler class.
	 * 
	 * @param hanlderfqcn
	 * @param hanlderkey
	 * @param handlerConfig
	 * @return handler
	 */
	public static AbstractServiceHandler getHandler(String hanlderfqcn, String hanlderkey, String handlerConfig) {
		String methodName = "getHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		HashMap<String, AbstractServiceHandler> hanlderMapData = handlerStore.get(hanlderkey);
		AbstractServiceHandler abstractServiceHandler;
		if (hanlderMapData == null || hanlderMapData.get(hanlderfqcn) == null)
			try {
				logger.trace(" {} handler class instance not found in cache....",LEAP_LOG_KEY);
				abstractServiceHandler = initializeHandler(hanlderfqcn, hanlderkey, handlerConfig);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return abstractServiceHandler;
			} catch (BeanDependencyResolveException e) {
				logger.error("{}, {}:{} ->  failed to initialize the instace of handler...{} ",LEAP_LOG_KEY, hanlderfqcn, handlerConfig, e.getMessage(), e);
			}
		else {
			logger.trace("{} returning the cached instance of handler class....",LEAP_LOG_KEY);
			abstractServiceHandler=hanlderMapData.get(hanlderfqcn);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return abstractServiceHandler;
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;

	}

}
