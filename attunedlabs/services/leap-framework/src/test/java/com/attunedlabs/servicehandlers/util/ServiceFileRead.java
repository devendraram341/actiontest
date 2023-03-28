package com.attunedlabs.servicehandlers.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigParserException;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParser;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;

public class ServiceFileRead {

	final static Logger log = LoggerFactory.getLogger(ServiceFileRead.class);
	private static ServiceHandlerConfigXMLParser handlerConfigXMLParser = new ServiceHandlerConfigXMLParser();

	private static final String FEATURE_FILE = "ServiceHandler/feature-service.xml";
	private static final String SERVICE_HANDLER_FILE = "ServiceHandler/servicehandler.xml";
	private static final String APPLICATION_SERVICE_HANDLER_FILE = "ServiceHandler/app_servicehandlerconfig.xml";

	/**
	 * Read feature service file from test resources folder.
	 * 
	 * @return
	 * @throws FeatureConfigParserException
	 */
	public static FeaturesServiceInfo getFeaturesServiceInfo() throws FeatureConfigParserException {
		FeatureConfigXMLParser parser = new FeatureConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(FEATURE_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(FEATURE_FILE + "file doesnot exist in classpath", e);
			throw new FeatureConfigParserException(FEATURE_FILE + "file doesnot exist in classpath", e);
		}
		String xmlString = out1.toString();
		FeaturesServiceInfo result = parser.marshallConfigXMLtoObject(xmlString);

		return result;
	}

	public static ServiceHandlerConfiguration getServiceHandlerFile() throws ServiceHandlerConfigParserException {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(SERVICE_HANDLER_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
			throw new ServiceHandlerConfigParserException(APPLICATION_SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
		}
		String xmlString = out1.toString();
		ServiceHandlerConfiguration result = handlerConfigXMLParser.marshallConfigXMLtoObject(xmlString);

		return result;
	}
	
	public static String getServiceHandlerAsString() throws ServiceHandlerConfigParserException {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(SERVICE_HANDLER_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
			throw new ServiceHandlerConfigParserException(SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
	
	
	
	public static ApplicationServiceHandlerConfiguration getApplicationServiceHandler() throws ServiceHandlerConfigParserException {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(APPLICATION_SERVICE_HANDLER_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(APPLICATION_SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
			throw new ServiceHandlerConfigParserException(APPLICATION_SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
		}
		String xmlString = out1.toString();
		ApplicationServiceHandlerConfiguration result = handlerConfigXMLParser.marshallConfigXMLtoAppObject(xmlString);

		return result;
	}
	
	public static String getApplicationServiceHandlerAsString() throws ServiceHandlerConfigParserException {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader()
				.getResourceAsStream(APPLICATION_SERVICE_HANDLER_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(APPLICATION_SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
			throw new ServiceHandlerConfigParserException(APPLICATION_SERVICE_HANDLER_FILE + "file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
}
