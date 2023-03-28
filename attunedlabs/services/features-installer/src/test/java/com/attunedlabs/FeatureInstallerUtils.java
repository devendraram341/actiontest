package com.attunedlabs;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.jaxb.DispatchChannels;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.EventSubscription;
import com.attunedlabs.eventframework.jaxb.Events;
import com.attunedlabs.eventframework.jaxb.SystemEvents;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParser;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;

import static com.attunedlabs.FeatureInstallerTestConstant.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FeatureInstallerUtils {

	public static ConfigurationContext getConfigContext() {
		return new ConfigurationContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR,
				TEST_VERSION);
	}
	
	public static RequestContext getRequestContext() {
		return new RequestContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR,
				TEST_VERSION);
	}

	/**
	 * feature meta info read file from test resources.
	 * 
	 * @return
	 */
	public static String getFeatureMetaInfoXMLFileToString() {
		InputStream inputstream = FeatureInstallerUtils.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.FEATURE_META_INFO);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out1.toString();
	}

	/**
	 * get Permsatore Data from test resources folder.
	 * 
	 * @return
	 */
	public static PermaStoreConfigurations getPermastore() {
		PermaStoreConfigXMLParser configXMLParser = new PermaStoreConfigXMLParser();
		InputStream inputstream = FeatureInstallerUtils.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.PERMASTORE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		PermaStoreConfigurations marshallConfigXMLtoObject = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			marshallConfigXMLtoObject = configXMLParser.marshallConfigXMLtoObject(out1.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return marshallConfigXMLtoObject;
	}

	/**
	 * get ServiceHandler Data from test resources folder.
	 * 
	 * @return
	 */
	public static ServiceHandlerConfiguration getServiceHandler() {
		ServiceHandlerConfigXMLParser configXMLParser = new ServiceHandlerConfigXMLParser();
		InputStream inputstream = FeatureInstallerUtils.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.SERVICE_HANDLER);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		ServiceHandlerConfiguration marshallConfigXMLtoAppObject = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			marshallConfigXMLtoAppObject = configXMLParser.marshallConfigXMLtoObject(out1.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return marshallConfigXMLtoAppObject;
	}

	/**
	 * get Feature Data Context Data from test resources folder.
	 * 
	 * @return
	 */
	public static FeatureDataContext getFeatureDataContext() {
		DataContextConfigXMLParser configXMLParser = new DataContextConfigXMLParser();
		InputStream inputstream = FeatureInstallerUtils.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.FEATURE_DATA_CONTEXT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		FeatureDataContext marshallConfigXMLtoObject = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			marshallConfigXMLtoObject = configXMLParser.marshallConfigXMLtoObject(out1.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return marshallConfigXMLtoObject;
	}

	/**
	 * get Feature Service Info Data from test resources folder.
	 * 
	 * @return
	 */
	public static FeaturesServiceInfo getFeatureServiceInfo() {
		FeatureConfigXMLParser configXMLParser = new FeatureConfigXMLParser();
		InputStream inputstream = FeatureInstallerUtils.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.FEATURE_SERVICE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		FeaturesServiceInfo marshallConfigXMLtoObject = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			marshallConfigXMLtoObject = configXMLParser.marshallConfigXMLtoObject(out1.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return marshallConfigXMLtoObject;
	}

	/**
	 * get Dispatch Channels data from eventFramework test resources folder.
	 * 
	 * @return
	 */
	public static DispatchChannels getEventDispatchChannels() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.EVENT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			String evtFrmeworkXmlStr = out1.toString();
			evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return evtFramework.getDispatchChannels();
	}

	/**
	 * get events data from eventFramework test resources folder.
	 * 
	 * @return
	 */
	public static Events getEvents() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.EVENT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			String evtFrmeworkXmlStr = out1.toString();
			evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return evtFramework.getEvents();
	}

	/**
	 * get SystemEvents data from eventFramework test resources folder.
	 * 
	 * @return
	 */
	public static SystemEvents getSystemEvents() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.EVENT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			String evtFrmeworkXmlStr = out1.toString();
			evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return evtFramework.getSystemEvents();
	}
	
	/**
	 * get Dispatch Channels data from eventFramework test resources folder.
	 * 
	 * @return
	 */
	public static EventSubscription getEventSubscription() {
		EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(FeatureInstallerTestConstant.EVENT);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		EventFramework evtFramework = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
			String evtFrmeworkXmlStr = out1.toString();
			evtFramework = parser.marshallConfigXMLtoObject(evtFrmeworkXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return evtFramework.getEventSubscription();
	}

	/**
	 * set Global tenant and site :
	 */
	public static void setTenantAndSite() {
		LeapHeaderConstant.tenant = "all";
		LeapHeaderConstant.site = "all";
	}
}
