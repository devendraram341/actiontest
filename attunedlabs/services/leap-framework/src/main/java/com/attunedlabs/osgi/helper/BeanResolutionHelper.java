package com.attunedlabs.osgi.helper;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BundleContextSingleton;

public class BeanResolutionHelper {
	final static Logger logger = LoggerFactory.getLogger(BeanResolutionHelper.class);

	public Object resolveBean(String interfaceName,String fqcnBuilder) throws InvalidSyntaxException{
		String methodName = "resolveBean";
		logger.debug("{} entered into the method {}, BeanResolutionHelper : {},  fqcn builder : {} ", LEAP_LOG_KEY, methodName,interfaceName,fqcnBuilder);
		//Object cacheObjectBuilder1=null;
		Object cacheObjectBuilder=null;

		BundleContext context=BundleContextSingleton.getBundleContext();
		logger.debug("{} bundle context : {} ",LEAP_LOG_KEY,context);
		/*ServiceReference serRef=context.getServiceReference(interfaceName);
		System.out.println(serRef.getPropertyKeys());
		System.out.println("context object of policystore : "+serRef);
		cacheObjectBuilder1=(Object)context.getService(serRef);*/
		String filter = "(&(fqcnBuilder=" + fqcnBuilder + "))";
		ServiceReference[] serviceReferneces=context.getServiceReferences(interfaceName, filter);
		logger.debug("{} services avaliable : {} ",LEAP_LOG_KEY,serviceReferneces);
		logger.trace("{} size of services avaliable : {} ",LEAP_LOG_KEY,serviceReferneces.length);
		for(ServiceReference serviceReference:serviceReferneces){
			cacheObjectBuilder=(Object)context.getService(serviceReference);
			logger.debug("{} cache object in BeanResolutionHelper : {} ",LEAP_LOG_KEY,cacheObjectBuilder);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

		return cacheObjectBuilder;
	}

}
