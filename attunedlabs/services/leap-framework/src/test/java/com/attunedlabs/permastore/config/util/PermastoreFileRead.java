package com.attunedlabs.permastore.config.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;

public class PermastoreFileRead {

	final static Logger log = LoggerFactory.getLogger(PermastoreFileRead.class);
	private final static String PERMASTORE_FILE = "PermaStore/PermaStoreConfig.xml";
	private final static String PERMASTORE_MAP = "PermaStore/PermastoreMap.xml";
	private final static String PERMASTORE_SET = "PermaStore/PermastoreSet.xml";
	private final static String PERMASTORE_LIST = "PermaStore/PermastoreList.xml";
	private final static String BAD_PERMASTORE = "PermaStore/BadPermaStoreConfig.xml";

	/**
	 * read persmastoreConfig.xml file from resourse and parse to
	 * permastoreconfigurations.
	 * 
	 * @return {@link PermaStoreConfigurations}
	 * @throws PermaStoreConfigParserException
	 */
	public static PermaStoreConfigurations getPermaStoreConfigurations() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();
		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * read BadPersmastoreConfig.xml file from resourse and parse to
	 * permastoreconfigurations.
	 * 
	 * @return {@link PermaStoreConfigurations}
	 * @throws PermaStoreConfigParserException
	 */
	public static PermaStoreConfigurations getBadPermaStoreConfigurations() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(BAD_PERMASTORE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();
		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * read PermastoreMap.xml file from resourse and parse to
	 * permastoreconfigurations.
	 * 
	 * @return {@link PermaStoreConfigurations}
	 * @throws PermaStoreConfigParserException
	 */
	public static PermaStoreConfigurations getPermaStoreMap() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_MAP);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();

		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * read PermastoreList.xml file from resourse and parse to
	 * permastoreconfigurations.
	 * 
	 * @return {@link PermaStoreConfigurations}
	 * @throws PermaStoreConfigParserException
	 */
	public static PermaStoreConfigurations getPermaStoreList() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_LIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();
		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * read PermastoreSet.xml file from resourse and parse to
	 * permastoreconfigurations.
	 * 
	 * @return {@link PermaStoreConfigurations}
	 * @throws PermaStoreConfigParserException
	 */
	public static PermaStoreConfigurations getPermaStoreSet() throws PermaStoreConfigParserException {
		PermaStoreConfigXMLParser parser = new PermaStoreConfigXMLParser();
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_SET);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		String xmlfile = out1.toString();
		PermaStoreConfigurations permastoreConfig = parser.marshallConfigXMLtoObject(xmlfile);

		return permastoreConfig;
	}

	/**
	 * Read permastoreConfig.xml file and convert as String.
	 * 
	 * @return {@link String}
	 * @throws PermaStoreConfigParserException
	 */
	public static String getPermaStoreConfiAsString() throws PermaStoreConfigParserException {
		InputStream inputstream = EventFrameworkXmlHandler.class.getClassLoader().getResourceAsStream(PERMASTORE_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		StringBuilder out1 = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				out1.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error("Permastore file doesnot exist in classpath", e);
		}
		return out1.toString();
	}
}
