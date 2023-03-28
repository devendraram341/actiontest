package com.attunedlabs.featureinstaller.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.config.ConfigurationConstant.BASE_CONFIG_PATH;
import static com.attunedlabs.config.ConfigurationConstant.IS_EVENT_EXIST;
import static com.attunedlabs.config.ConfigurationConstant.IS_SYSTEM_EVENT_EXIST;
import static com.attunedlabs.featureinstaller.FeatureMetaInfoConstant.ERASE_OLD_CONFIGURATION_PROPERTY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateSummary;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.schema.Table;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.applicationmetainfo.config.ApplicationMetaInfoConfigXMLParser;
import com.attunedlabs.applicationmetainfo.config.ApplicationMetaInfoParsingException;
import com.attunedlabs.applicationmetainfo.jaxb.AppMetainfo;
import com.attunedlabs.applicationmetainfo.jaxb.AppMetainfo.ApplicationServiceHandlers;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.persistence.exception.PrettyUrlMappingDaoException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingService;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingServiceException;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.datacontext.config.DataContextConfigurationUnit;
import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigXMLParser;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.dynastore.config.DynaStoreConfigParserException;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationException;
import com.attunedlabs.dynastore.config.IDynaStoreConfigurationService;
import com.attunedlabs.dynastore.config.impl.DynaStoreConfigXmlParser;
import com.attunedlabs.dynastore.config.impl.DynaStoreConfigurationService;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfigurations;
import com.attunedlabs.eventframework.camel.eventproducer.ServicePerformanceLoggingEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigParserException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkXmlHandler;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.DispatchChannels;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventDispatchers;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.EventSubscription;
import com.attunedlabs.eventframework.jaxb.Events;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.eventframework.jaxb.SystemEvents;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.IFeatureConfigurationService;
import com.attunedlabs.feature.config.impl.FeatureConfigXMLParser;
import com.attunedlabs.feature.config.impl.FeatureConfigurationService;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.GenericRestEndpoint;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featureinstaller.FeatureMetaInfoConfigInstaller;
import com.attunedlabs.featureinstaller.FeatureMetaInfoConstant;
import com.attunedlabs.featureinstaller.customloader.CustomLoaderFromJar;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;
import com.attunedlabs.featuremetainfo.FeatureMetaInfoConfigParserException;
import com.attunedlabs.featuremetainfo.impl.FeatureMetaInfoConfigXmlParser;
import com.attunedlabs.featuremetainfo.jaxb.ConfigFile;
import com.attunedlabs.featuremetainfo.jaxb.DBConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.DataContexts;
import com.attunedlabs.featuremetainfo.jaxb.Database;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.DynaStoreConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.EventResource;
import com.attunedlabs.featuremetainfo.jaxb.EventResources;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.featuremetainfo.jaxb.FeatureDataContexts;
import com.attunedlabs.featuremetainfo.jaxb.FeatureGroup;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementation;
import com.attunedlabs.featuremetainfo.jaxb.FeatureImplementations;
import com.attunedlabs.featuremetainfo.jaxb.FeatureMetainfo;
import com.attunedlabs.featuremetainfo.jaxb.HandlerConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.IntegrationPipeLineConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.LeapEntityConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.LeapEntityConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PipeConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.PolicyConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.ResourceManager;
import com.attunedlabs.featuremetainfo.jaxb.ResourceManagerConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.ScheduledJobConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.ScheduledJobConfigurations;
import com.attunedlabs.featuremetainfo.jaxb.ServiceHandlerConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.StaticFileConfiguration;
import com.attunedlabs.featuremetainfo.jaxb.UIConfig;
import com.attunedlabs.featuremetainfo.util.FeatureMetaInfoUtil;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigParserException;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigXmlParser;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipe;
import com.attunedlabs.integrationfwk.config.jaxb.IntegrationPipes;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.i18n.service.ILeapI18nSetup;
import com.attunedlabs.leap.i18n.service.ILeapResourceBundleResolver;
import com.attunedlabs.leap.i18n.service.LeapI18nSetupImpl;
import com.attunedlabs.leap.i18n.service.LeapResourceBundleResolverImpl;
import com.attunedlabs.leapentity.config.ILeapDataServiceConfiguration;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.leapentity.config.impl.LeapDataServiceConfiguration;
import com.attunedlabs.leapentity.config.impl.LeapEntityConfigParserException;
import com.attunedlabs.leapentity.config.impl.LeapEntityConfigXMLParser;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.LeapDataServices;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigXMLParser;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilder;
import com.attunedlabs.permastore.config.jaxb.ConfigurationBuilderType;
import com.attunedlabs.permastore.config.jaxb.FeatureInfo;
import com.attunedlabs.permastore.config.jaxb.InlineBuilder;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;
import com.attunedlabs.permastore.config.jaxb.PublishPermaStoreEvent;
import com.attunedlabs.permastore.config.jaxb.SubscribePermaStoreEvents;
import com.attunedlabs.policy.config.IPolicyConfigurationService;
import com.attunedlabs.policy.config.PolicyConfigXMLParser;
import com.attunedlabs.policy.config.PolicyConfigXMLParserException;
import com.attunedlabs.policy.config.PolicyConfigurationException;
import com.attunedlabs.policy.config.PolicyRequestContext;
import com.attunedlabs.policy.config.PolicyRequestException;
import com.attunedlabs.policy.config.impl.PolicyConfigurationService;
import com.attunedlabs.policy.jaxb.Policies;
import com.attunedlabs.policy.jaxb.Policy;
import com.attunedlabs.resourcemanagement.config.IResourceManagementConfigService;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigParserException;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigurationException;
import com.attunedlabs.resourcemanagement.config.impl.ResourceManagementConfigService;
import com.attunedlabs.resourcemanagement.config.impl.ResourceManagementConfigXmlParser;
import com.attunedlabs.resourcemanagement.jaxb.ConnectionInfo;
import com.attunedlabs.resourcemanagement.jaxb.GetResourceContent;
import com.attunedlabs.resourcemanagement.jaxb.ResourceManagerConfig;
import com.attunedlabs.scheduler.ScheduledJobConfigParserException;
import com.attunedlabs.scheduler.ScheduledJobConfigurationException;
import com.attunedlabs.scheduler.config.IScheduledJobConfigurationService;
import com.attunedlabs.scheduler.config.ScheduledJobConfigRequestException;
import com.attunedlabs.scheduler.config.impl.ScheduledJobConfigXMLParser;
import com.attunedlabs.scheduler.config.impl.ScheduledJobConfigurationService;
import com.attunedlabs.security.config.AccountConfigurationService;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.service.IAccountRetrieveService;
import com.attunedlabs.security.service.impl.AccountRetrieveServiceImpl;
import com.attunedlabs.servicehandlers.config.IServiceHandlerConfigurationService;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigParserException;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParser;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigurationException;
import com.attunedlabs.servicehandlers.config.impl.ServiceHandlerConfigurationService;
import com.attunedlabs.staticconfig.AddStaticConfigException;
import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigDuplicateNameofFileException;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.factory.StaticConfigurationFactory;
import com.attunedlabs.staticconfig.impl.AccessProtectionException;
import com.attunedlabs.staticconfig.util.LocalfileUtil;
import com.attunedlabs.zookeeper.staticconfig.service.impl.InvalidFilePathException;
import com.attunedlabs.zookeeper.staticconfig.service.impl.ZookeeperFilemanagementServiceImpl;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

import oracle.jdbc.pool.OracleDataSource;

/**
 * This class is responsible for loading all feature related configuration in
 * non-osgi environment
 * 
 * @author bizruntime
 * 
 */
@SuppressWarnings({ "unused", "rawtypes", "resource" })
public class FeatureMetaInfoResourceUtil {
	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoResourceUtil.class);
	private PrettyUrlMappingService prettyService = new PrettyUrlMappingService();

	private static final String JAVA_CLASSPATH_KEY = "java.class.path";
	private static final String PATH_SEPERATOR_KEY = "path.separator";
	static final String MYSQL_TYPE = "mysql";
	static final String ORACLE_TYPE = "oracle";
	static final String MSSQL_TYPE = "mssql";
	static final String POSTGRESSQL_TYPE = "postgressql";
	static final String POSTGRES_TYPE = "postgres";
	static final String DDLUITLS_XMLDOCTYPE = "<!DOCTYPE database SYSTEM \"http://db.apache.org/torque/dtd/database.dtd\"> ";
	static String path = "";
	private static final String APP_META_INFO_XSD = "applicationMetaInfo.xsd";
	private static final String APP_META_INFO_XML = "applicationMetaInfo.xml";
	private static final String APP_SERVICE_HANDLERS_CONFIG = "app_servicehandlerconfig.xml";
	private static final String STATICCONFIG_PROPS = "globalAppDeploymentConfig.properties";
	private static final String STATICCONFIG_DIC_KEY = "staticConfigDirectory";
	private static final Class[] parameters = new Class[] { URL.class };
	private static final String ADD_URL = "addURL";
	private static final String RESOURCE_FILE_PATH = "src/main/resources";
	private static final String AUTHORIZATION = "authorization";
	private static final String LEAP_DEFAULT = "LeapDefault_Taxonomy.json";
	private String LEAP_DEFAULAT_PERMASTORE_STRING = "";

	private static Properties propsStaticConfig = new Properties();

	static {
		try {
			propsStaticConfig = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
			path = LeapConfigUtil.getGlobalPropertyValue(STATICCONFIG_DIC_KEY,LeapDefaultConstants.DEFAULT_STATICCONFIG_DIC_KEY);
		} catch (Exception e) {
			logger.error("{} unable to get GlobalAppDeploymentConfigProperties {}", LEAP_LOG_KEY, e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to get all elements of java.class.path get a Collection
	 * of resources Pattern pattern = Pattern.compile("featureMetaInfo.xml"); gets
	 * all resources
	 * 
	 * @param pattern the pattern to match
	 * @return the resources if they are found
	 * @throws FeatureMetaInfoResourceException
	 * @throws PropertiesConfigException
	 * @throws IOException
	 */

	public Collection<String> getClassPathResources(Pattern pattern)
			throws FeatureMetaInfoResourceException, PropertiesConfigException {
		String methodName = "getClassPathResources";
		logger.debug("{} entered into the method {}, pattern: {}", LEAP_LOG_KEY, methodName, pattern);
		final ArrayList<String> resource = new ArrayList<String>();
		final String javaclassPath = System.getProperty(JAVA_CLASSPATH_KEY, ".");
		final String[] javaclassPathElements = javaclassPath.split(System.getProperty(PATH_SEPERATOR_KEY));
		String oldConfigurationProperty = LeapConfigUtil.getGlobalPropertyValue(ERASE_OLD_CONFIGURATION_PROPERTY,LeapDefaultConstants.DEFAULT_ERASE_OLD_CONFIGURATION_PROPERTY);
		eraseOldConfigurations(oldConfigurationProperty);
		for (final String element : javaclassPathElements) {
			try {
				boolean flag = true;
				if (element.contains("leap-framework") && element.endsWith(".jar") && flag) {
					loadApplicationMetaInfo(element);
					flag = false;
				}
				resource.addAll(getResources(element, pattern));
			} catch (Exception e) {
				e.printStackTrace();
				throw new FeatureMetaInfoResourceException("Unable to get ClassPathResources: ", e);
			}
		}
		logger.trace("{} resource :: {}", LEAP_LOG_KEY, resource);
		logger.debug("{} exiting the method {}", LEAP_LOG_KEY, methodName);
		return resource;
	}

	private void eraseOldConfigurations(String oldConfigurationProperty) {
		String profileId = System.getProperty(FeatureMetaInfoConfigInstaller.PROFILE_ID_PROPERTY);
		if (oldConfigurationProperty == null) {
			logger.warn(
					"{} unable to find [erasePreviousLoadedConfiguration] property in globalAppDeploymentConfigProperties. So, erasing old configurations set to true",
					LEAP_LOG_KEY);
			oldConfigurationProperty = "true";
		}
		if (profileId.equalsIgnoreCase("prod") && Boolean.valueOf(oldConfigurationProperty)) {
			logger.warn("{} erasing old configurations in prod environment is  not recommended", LEAP_LOG_KEY);
		}
		if (Boolean.valueOf(oldConfigurationProperty)) {
			erasePreviousLoadedConfiguration();
		}
	}

	/**
	 * This method is to search pattern in directory or jar available in classpath
	 * 
	 * @param element : Jar/Directory to be searched
	 * @param pattern : pattern to be matched
	 * @return Collection object
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 */
	private Collection<String> getResources(String element, Pattern pattern)
			throws FeatureMetaInfoResourceException, IOException {
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		final File file = new File(element);
		if (file.isDirectory()) {
			resourcevalue.addAll(getResourcesFromDirectory(file, pattern));
		} else {
			resourcevalue.addAll(getResourcesFromJar(file, pattern, element));
		}
		return resourcevalue;
	}

	/**
	 * This method is to searched the matching pattern in jar
	 * 
	 * @param file    : name of the file to be searched
	 * @param pattern : pattern to be searched
	 * @param element : jar to be searched
	 * @return Colection Object
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 */
	private Collection<String> getResourcesFromJar(File file, Pattern pattern, String element)
			throws FeatureMetaInfoResourceException, IOException {
		String methodName = "getResourcesFromJar";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(file);
		} catch (ZipException e) {
			throw new FeatureMetaInfoResourceException(
					"Unable to opens a ZIP file for reading given the specified File object.", e);
		} catch (IOException e) {
			throw new FeatureMetaInfoResourceException("Unable to read from given the File object. = " + file.getName(),
					e);
		}
		final Enumeration enumerator = zipfile.entries();
		while (enumerator.hasMoreElements()) {
			final ZipEntry zipentry = (ZipEntry) enumerator.nextElement();
			String fileName = zipentry.getName();
			String featureMetaFileInfoLoc = null;
			if (!fileName.equalsIgnoreCase(FeatureMetaInfoConfigInstaller.PATTERN_SEARCH_KEY)) {
				String[] fileNameSplit = fileName.split("/");
				if (fileNameSplit.length >= 2)
					if (fileNameSplit[1] != null
							&& fileNameSplit[1].equalsIgnoreCase(FeatureMetaInfoConfigInstaller.PATTERN_SEARCH_KEY)) {
						fileName = fileNameSplit[1];
						featureMetaFileInfoLoc = fileNameSplit[0];
						logger.debug("{} inside file name:{}, featureMetaInfoLoc:{}", LEAP_LOG_KEY, fileName,
								featureMetaFileInfoLoc);
					}
			}
			final boolean acceptedFile = pattern.matcher(fileName).matches();
			if (acceptedFile) {
				logger.trace("{} acceptedFile ::{}", LEAP_LOG_KEY, fileName);
				logger.trace("{} element ::{}", LEAP_LOG_KEY, element);
				loadFeatureDefaultTaxonomyJson(zipfile, element);
				List<FeatureGroup> featureGroupList = parseAndGetFeatureMetaInfo(fileName, element,
						featureMetaFileInfoLoc);
				loadAllResourceFromFeatureLevel(featureGroupList);
				resourcevalue.add(fileName);
			}

		}
		try {
			zipfile.close();
		} catch (IOException e1) {
			throw new FeatureMetaInfoResourceException("Unable to close a ZIP file object for specified File object.",
					e1);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return resourcevalue;
	}

	private void loadFeatureDefaultTaxonomyJson(ZipFile zipfile, String element) {
		String methodName = "loadFeatureDefaultTaxonomyJson";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Pattern pattern = Pattern.compile(LEAP_DEFAULT);
		final Enumeration enumerator = zipfile.entries();
		while (enumerator.hasMoreElements()) {
			final ZipEntry zipentry = (ZipEntry) enumerator.nextElement();
			final String fileName = zipentry.getName();
			final boolean acceptedFile = pattern.matcher(fileName).matches();
			if (acceptedFile) {
				URL url = null;
				try {
					url = new URL("file:" + element);
				} catch (MalformedURLException e1) {
					logger.error("{} Unable to create URL from element path : {} errorMsg:{}", LEAP_LOG_KEY, element,
							e1.getMessage());
					return;
				}
				try {
					CustomLoaderFromJar customJarLoader = new CustomLoaderFromJar(new URL[] { url });
					URL xmlURL = customJarLoader.getResource(fileName);
					LEAP_DEFAULAT_PERMASTORE_STRING = convertJsonToString(xmlURL, fileName);
					logger.debug("{} LEAP_DEFAULAT_PERMASTORE_STRING ::{}", LEAP_DEFAULT,
							LEAP_DEFAULAT_PERMASTORE_STRING);
				} catch (UnableToParseJsonException e) {
					logger.error("{} Unable to parse {}", LEAP_DEFAULT, e.getMessage());
				}
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void loadApplicationMetaInfo(String element) throws ApplicationMetaInfoParsingException {
		Pattern pattern = Pattern.compile(APP_META_INFO_XML);
		final File file = new File(element);
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(file);
		} catch (ZipException e) {
			throw new ApplicationMetaInfoParsingException(
					"Unable to opens a ZIP file for reading given the specified File object.", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApplicationMetaInfoParsingException(
					"Unable to read from given the File object. = " + file.getName(), e);
		}
		final Enumeration enumerator = zipfile.entries();
		while (enumerator.hasMoreElements()) {
			final ZipEntry zipentry = (ZipEntry) enumerator.nextElement();
			final String fileName = zipentry.getName();
			final boolean acceptedFile = pattern.matcher(fileName).matches();
			if (acceptedFile) {
				logger.trace("{} .parseAndgetApplicationMetaInfo of ServiceHandlers {}, element :{}", LEAP_LOG_KEY,
						fileName, element);
				URL url;
				try {
					url = new URL("file:" + element);
				} catch (MalformedURLException e1) {
					throw new ApplicationMetaInfoParsingException("Unable to create URL from element path : " + element,
							e1);
				}
				CustomLoaderFromJar customJarLoader = new CustomLoaderFromJar(new URL[] { url });
				URL handlerResourceUrl = customJarLoader.getResource(fileName);
				logger.trace("{} app_metainfo url : {}", LEAP_LOG_KEY, handlerResourceUrl);

				String handlerConfigurationXML;
				try {
					handlerConfigurationXML = convertXmlToString(handlerResourceUrl, fileName);
				} catch (FeatureMetaInfoResourceException e) {
					throw new ApplicationMetaInfoParsingException(e.getMessage());
				}
				if (handlerConfigurationXML != null) {
					ApplicationMetaInfoConfigXMLParser applicationMetaInfoConfigXMLParser = new ApplicationMetaInfoConfigXMLParser();
					AppMetainfo appMetaInfo = applicationMetaInfoConfigXMLParser
							.marshallConfigXMLtoObject(handlerConfigurationXML);
					loadAllResources(appMetaInfo);
				}

			}
		}
		try {
			zipfile.close();
		} catch (IOException e1) {
			throw new ApplicationMetaInfoParsingException(
					"Unable to close a ZIP file object for specified File object.", e1);
		}

	}

	private void loadAllResources(AppMetainfo appMetaInfo) throws ApplicationMetaInfoParsingException {
		ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, "applicationGrp", "application", "attunedlabs", "attunedlabs", "1.0");
		ApplicationServiceHandlers applicationServiceHandlers = appMetaInfo.getApplicationServiceHandlers();
		checkAndLoadAppServiceHandlers(applicationServiceHandlers, configContext);

	}

	private void checkAndLoadAppServiceHandlers(ApplicationServiceHandlers applicationServiceHandlers,
			ConfigurationContext configContext) throws ApplicationMetaInfoParsingException {
		String methodName = "checkAndLoadAppServiceHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (applicationServiceHandlers != null) {
			if (applicationServiceHandlers.getServiceHandlerConfig() != null) {
				String resourceName = applicationServiceHandlers.getServiceHandlerConfig().getResourceName();
				URL resourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(resourceName);
				if (resourceUrl != null) {
					logger.trace("{} resourceUrl : {}", LEAP_LOG_KEY, resourceUrl);
					try {
						String appResourceServicehandlers = convertXmlToString(resourceUrl, resourceName);
						IServiceHandlerConfigurationService serviceHandlerConfigurationService = new ServiceHandlerConfigurationService();
						ServiceHandlerConfigXMLParser serviceHandlerConfigXMLParser = new ServiceHandlerConfigXMLParser();
						ApplicationServiceHandlerConfiguration serviceHandlerConfiguration = serviceHandlerConfigXMLParser
								.marshallConfigXMLtoAppObject(appResourceServicehandlers);
						if (serviceHandlerConfiguration.getCommonServiceHandler() != null)
							serviceHandlerConfigurationService.addApplicationCommonServiceHandlersConfiguration(
									serviceHandlerConfiguration.getCommonServiceHandler(), configContext);

						if (serviceHandlerConfiguration.getFeatureServiceHandler() != null)
							serviceHandlerConfigurationService.addApplicationFeatureServiceHandlersConfiguration(
									serviceHandlerConfiguration.getFeatureServiceHandler(), configContext);
					} catch (ServiceHandlerConfigParserException | BeanDependencyResolveException
							| ServiceHandlerConfigurationException | FeatureMetaInfoResourceException e) {
						throw new ApplicationMetaInfoParsingException(
								"Unable to parse service handler file : " + resourceName, e);
					}
				} else
					throw new ApplicationMetaInfoParsingException(
							"Unable to parse service handler file : " + resourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is to searched the matching pattern in directory
	 * 
	 * @param file    : name of the file to be searched
	 * @param pattern : pattern to be searched
	 * @return Colection Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private Collection<String> getResourcesFromDirectory(File directory, Pattern pattern)
			throws FeatureMetaInfoResourceException {
		final ArrayList<String> resourcevalue = new ArrayList<String>();
		final File[] fileList = directory.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				resourcevalue.addAll(getResourcesFromDirectory(file, pattern));
			} else {
				try {
					final String fileName = file.getCanonicalPath();
					final boolean acceptedFile = pattern.matcher(fileName).matches();
					if (acceptedFile) {
						// parseAndGetFeatureMetaInfo(fileName);
						resourcevalue.add(fileName);
					}
				} catch (final IOException e) {
					throw new FeatureMetaInfoResourceException(
							"Unable to get the canonical path of file : " + file.getName(), e);
				}
			}
		}
		return resourcevalue;
	}

	/**
	 * This method is used to parse the featureMetaInfo
	 * 
	 * @param featureMetaInfo        : filename to be parsed
	 * @param element                : jar from where it has to be loaded
	 * @param featureMetaFileInfoLoc
	 * @return List : list of FeatureGroup Object
	 * @throws FeatureMetaInfoResourceException
	 */
	public List<FeatureGroup> parseAndGetFeatureMetaInfo(String featureMetaInfo, String element,
			String featureMetaFileInfoLoc) throws FeatureMetaInfoResourceException {
		String methodName = "parseAndGetFeatureMetaInfo";
		logger.debug("{} entered into the method {}, featureMetaInfo{}, featureMetaFileInfoLoc{} element {}",
				LEAP_LOG_KEY, methodName, featureMetaInfo, featureMetaFileInfoLoc, element);
		URL url;
		try {
			url = new URL("file:" + element);
		} catch (MalformedURLException e1) {
			throw new FeatureMetaInfoResourceException("Unable to create URL from element path : " + element, e1);
		}
		CustomLoaderFromJar customJarLoader = new CustomLoaderFromJar(new URL[] { url });
		if (featureMetaFileInfoLoc != null) {
			featureMetaInfo = featureMetaFileInfoLoc + "/" + featureMetaInfo;
		}
		URL featureMetaInfoXmlUrl = customJarLoader.getResource(featureMetaInfo);
		logger.trace("{} featureMetaInfoXmlUrl :{} ", LEAP_LOG_KEY, featureMetaInfoXmlUrl);
		String featurexmlAsString = convertXmlToString(featureMetaInfoXmlUrl, featureMetaInfo);
		List<FeatureGroup> featureGroupList = null;
		if (featurexmlAsString != null) {
			FeatureMetaInfoConfigXmlParser featureMetaInfoParser = new FeatureMetaInfoConfigXmlParser();
			try {
				FeatureMetainfo featureMetaInfo1 = featureMetaInfoParser.marshallConfigXMLtoObject(featurexmlAsString);
				featureGroupList = featureMetaInfo1.getFeatureGroup();
			} catch (FeatureMetaInfoConfigParserException e) {
				throw new FeatureMetaInfoResourceException("Unable to parse featureMetaInfo xml string into object ");
			}
		} // end of if(featurexmlAsString!=null)
		logger.debug("{} exiting parseAndgetFeatureMetaInfo of FeatureMetaInfoExtender ", LEAP_LOG_KEY);
		return featureGroupList;
	}

	/**
	 * This method is used to xml format to string format
	 * 
	 * @param featureMetaInfoXmlUrl : URL Object of resource file
	 * @param featureMetaInfo       : name of xml file to be converted into String
	 * @return String
	 * @throws FeatureMetaInfoResourceException
	 */
	private String convertXmlToString(URL featureMetaInfoXmlUrl, String featureMetaInfo)
			throws FeatureMetaInfoResourceException {
		String methodName = "convertXmlToString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		InputStream featureMetaInfoXmlInput = null;
		String featurexmlAsString = null;
		StringBuilder out1 = new StringBuilder();
		if (featureMetaInfoXmlUrl != null) {
			try {
				featureMetaInfoXmlInput = featureMetaInfoXmlUrl.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(featureMetaInfoXmlInput));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						out1.append(line + "\n");
					}
				} catch (IOException e) {
					throw new FeatureMetaInfoResourceException(
							"Unable to open the read for the BufferedReader for the file : " + featureMetaInfo, e);
				}
				// logger.debug(out1.toString()); // Prints the string content
				// read
				// from input stream
				try {
					reader.close();
				} catch (IOException e) {
					throw new FeatureMetaInfoResourceException(
							"Unable to close the read for the BufferedReader for the file : " + featureMetaInfo, e);
				}
				featurexmlAsString = out1.toString();
			} catch (IOException e) {
				throw new FeatureMetaInfoResourceException(
						"Unable to open the input stream for the file : " + featureMetaInfo, e);
			}
		} else {
			logger.debug("{} FeatureMetaInfo.xml file doesn't exist ", LEAP_LOG_KEY);
		}
		logger.trace("{} String content is : {}", LEAP_LOG_KEY, featurexmlAsString);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return featurexmlAsString;
	}// end of method

	/**
	 * This method is used to Json format to string format
	 * 
	 * @param featureMetaInfoXmlUrl : URL Object of resource file
	 * @param featureMetaInfo       : name of xml file to be converted into String
	 * @return String : Jsonfile as String
	 * @throws UnableToParseJsonException
	 */
	private String convertJsonToString(URL jsonURL, String jsonFile) throws UnableToParseJsonException {
		String methodName = "convertJsonToString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		InputStream jsonInput = null;
		String jsonAsString = null;
		StringBuilder out1 = new StringBuilder();
		if (jsonURL != null) {
			try {
				jsonInput = jsonURL.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(jsonInput));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						out1.append(line + "\n");
					}
				} catch (IOException e) {
					throw new UnableToParseJsonException(
							"Unable to open the read for the BufferedReader for the file : " + jsonInput, e);
				}
				// logger.debug(out1.toString()); // Prints the string content
				// read
				// from input stream
				try {
					reader.close();
				} catch (IOException e) {
					throw new UnableToParseJsonException(
							"Unable to close the read for the BufferedReader for the file : " + jsonInput, e);
				}
				jsonAsString = out1.toString();
			} catch (IOException e) {
				throw new UnableToParseJsonException("Unable to open the input stream for the file : " + jsonInput, e);
			}
		} else {
			logger.debug("{} file doesn't exist ", LEAP_LOG_KEY);
		}
		logger.trace("{} String content is :{} ", LEAP_LOG_KEY, jsonAsString);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return jsonAsString;
	}// end of method

	/**
	 * This method will load resource from feature level
	 * 
	 * @param featureGroupList : List of FeatureGroup available in feature
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 */
	public void loadAllResourceFromFeatureLevel(List<FeatureGroup> featureGroupList)
			throws FeatureMetaInfoResourceException, IOException {
		if (featureGroupList != null && !featureGroupList.isEmpty()) {
			for (FeatureGroup featureGroup : featureGroupList) {
				String featureGroupName = featureGroup.getName();
				List<Feature> featureList = featureGroup.getFeatures().getFeature();
				logger.trace("{} featureList : {}", LEAP_LOG_KEY, featureList.size());
				for (Feature feature : featureList) {
					logger.debug(
							"{} Feature group Name :{}, Feature Name :{}, implementationName :{}, vendor name :{}, version :{}, provider :{}",
							LEAP_LOG_KEY, featureGroupName, feature.getName(), feature.getImplementationName(),
							feature.getVendorName(), feature.getVendorVersion(), feature.getProvider());
					boolean isAvailable = checkFeatureExitInFeatureMaster(featureGroupName, feature.getName(),
							feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
					logger.trace("{} is avalable : {}", LEAP_LOG_KEY, isAvailable);
					if (isAvailable) {
						checkResourceAvailableAndload(feature, featureGroupName);
					} else {
						logger.debug(
								"{} feature doesn't avaliable in master table with feature group name :{} and feature name : {} ",
								LEAP_LOG_KEY, featureGroupName, feature.getName());
					}
				}
			}
		} // end of if(featureGroupList != null && !featureGroupList.isEmpty())
	}// end of method

	/**
	 * Method is used to erase previously loaded configurations for freshly updating
	 * the database with new records.
	 */

	private void erasePreviousLoadedConfiguration() {
		String methodName = "erasePreviousLoadedConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table confignode = dataContext.getTableByQualifiedLabel(LeapConstants.CONFIG_NODE_DATA_TABLE);
			final Table featureDeploymentTable = dataContext
					.getTableByQualifiedLabel(LeapConstants.TABLE_FEATUREDEPLOYMENT);
			final Table prettyUrlMapping = dataContext
					.getTableByQualifiedLabel(LeapConstants.PRETTY_URL_MAPPING_TABLE_NAME);

			DefaultUpdateSummary configNodeDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(confignode);
							delete.execute();

						}
					});
			DefaultUpdateSummary featureDeploymentTableDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(featureDeploymentTable);
							delete.execute();

						}
					});
			UpdateSummary prettyurlMappingDelete = dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(prettyUrlMapping);
					delete.execute();

				}
			});
			if (configNodeDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) configNodeDelete.getDeletedRows().get();
				logger.info("{} confignodedata total deleted record's {}", LEAP_LOG_KEY, totalRowsDeleted);
			} else {
				logger.info("{} confignodedata already empty!", LEAP_LOG_KEY);
			}
			totalRowsDeleted = 0;
			if (featureDeploymentTableDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) featureDeploymentTableDelete.getDeletedRows().get();
				logger.info("{} featureDeploymentTable total deleted record's {}", LEAP_LOG_KEY, totalRowsDeleted);
			} else {
				logger.info("{} featureDeploymentTable already empty!", LEAP_LOG_KEY);
			}
			totalRowsDeleted = 0;
			if (prettyurlMappingDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) prettyurlMappingDelete.getDeletedRows().get();
				logger.info("{} prettyurlMapping table total deleted record's {}", LEAP_LOG_KEY, totalRowsDeleted);
			} else {
				logger.info("{} prettyurlMapping table already empty!", LEAP_LOG_KEY);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to table confignodedata and featureDeploymentTable --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}

		// TODO Auto-generated method stub
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to check if feature exist in FeatureMaster Table or not
	 * 
	 * @param featureGroup : feature group name
	 * @param featureName  : feature name
	 * @return boolean : True/False
	 * @throws FeatureMetaInfoResourceException
	 */
	private boolean checkFeatureExitInFeatureMaster(String featureGroup, String featureName, String implName,
			String vendorName, String version) throws FeatureMetaInfoResourceException {
		String methodName = "checkFeatureExitInFeatureMaster";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IFeatureMasterService featureMasterServie = new FeatureMasterService();
		boolean isAvailable;
		try {
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroup, featureName, implName, vendorName, version);
			isAvailable = featureMasterServie.checkFeatureExistInFeatureMasterOrNot(configContext);
		} catch (FeatureMasterServiceException e) {
			throw new FeatureMetaInfoResourceException(
					"Unable  find out Feature with feature group name : " + featureGroup + " and feature name : "
							+ featureName + "impl name : " + implName + " in Feature master ",
					e);

		}
		return isAvailable;

	}

	/**
	 * This method is used to check the resource available in featureMetaInfo.xml
	 * and call method to load them
	 * 
	 * @param feature          : feature name
	 * @param featureGroupName : feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 */
	public void checkResourceAvailableAndload(Feature feature, String featureGroupName)
			throws FeatureMetaInfoResourceException, IOException {
		String methodName = "checkResourceAvailableAndload";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// check if event resource configured and then load
		logger.trace("{} feature MetaInfo:- {}", LEAP_LOG_KEY, feature);
		EventResources eventResources = feature.getEventResources();
		checkEventResourceAndLoad(eventResources, feature, featureGroupName);

		// load service handlers
		ServiceHandlerConfiguration serviceHandlerConfiguration = feature.getServiceHandlerConfiguration();
		checkServiceHandlerConfigurationAndLoad(serviceHandlerConfiguration, feature, featureGroupName);

		// check if permastore resource configured and then load
		com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration = feature
				.getPermaStoreConfigurations();
		checkPermastoreResourceAndLoad(permastoreConfiguration, feature, featureGroupName);

		// check if dynastore resource configured and then load
		DynaStoreConfigurations dynastoreConfiguration = feature.getDynaStoreConfigurations();
		checkDynastoreResourceAndLoad(dynastoreConfiguration, feature, featureGroupName);

		// check if policy resource configured and then load
		PolicyConfigurations policyConfiguration = feature.getPolicyConfigurations();
		checkPolicyResourceAndLoad(policyConfiguration, feature, featureGroupName);

		// check if featureImpl resource configured and then load
		FeatureImplementations featureImplementation = feature.getFeatureImplementations();
		checkFeatureImplResourceAndLoad(featureImplementation, feature, featureGroupName);

		// check if featureDataContext is defined or not
		FeatureDataContexts featureDataContexts = feature.getFeatureDataContexts();
		checkFeatureDataContextsResourceAndLoad(featureDataContexts, feature, featureGroupName);

		// check if integrationpipelineConfig is defined or not
		IntegrationPipeLineConfigurations integrationPipes = feature.getIntegrationPipeLineConfigurations();
		checkIntegrationPipeCongigurationsResourceAndLoad(integrationPipes, feature, featureGroupName);

		// load the Static file configurations..
		StaticFileConfiguration staticFileConfiguration = feature.getStaticFileConfiguration();
		storeStaticFileConfigs(staticFileConfiguration, feature, featureGroupName);

		// loads the DBConfiguration from FeatureMetaInfo
		DBConfiguration dbConfiguration = feature.getDBConfiguration();
		configureDatabase(dbConfiguration, feature, featureGroupName);

		// loads the UIConfig from FeatureMetaInfo
		UIConfig uiConfig = feature.getUIConfig();
		configureUI(uiConfig, feature, featureGroupName);

		ScheduledJobConfigurations schedulerConfigurations = feature.getScheduledJobConfigurations();
		checkSchedulerResourceAndLoad(schedulerConfigurations, feature, featureGroupName);

		// check if resourcemanagement resource configured and then load
		ResourceManagerConfiguration resourceManagerConfiguration = feature.getResourceManagerConfiguration();
		checkResourceMangementResourceAndLoad(resourceManagerConfiguration, feature, featureGroupName);

		// check if leapEntity resource configured and then load
		com.attunedlabs.featuremetainfo.jaxb.LeapEntityConfigurations leapEntityConfigurations = feature
				.getLeapEntityConfigurations();
		checkLeapEntityResourceAndLoad(leapEntityConfigurations, feature, featureGroupName);

		// Pre-loading tenant specific data
		checkAndReloadResource(feature.getName(), featureGroupName);

		// loads the account detail from Database
		loadAccountConfigurations();

		// load LeapDefault_Taxonomy.json as Permastore
		loadFeatureDefaultTaxonomyAsPermastore(featureGroupName, feature.getName(), feature.getImplementationName(),
				feature.getVendorName(), feature.getVendorVersion());

	}

	private void checkAndReloadResource(String feature, String featureGroupName)
			throws FeatureMetaInfoResourceException {
		String methodName = "erasePreviousLoadedConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
		IPermaStoreConfigurationService permaStoreConfigurationService = new PermaStoreConfigurationService();
		IPolicyConfigurationService policyConfigurationService = new PolicyConfigurationService();
		IFeatureConfigurationService featureConfigurationService = new FeatureConfigurationService();
		IDataContextConfigurationService dataContextConfigurationService = new DataContextConfigurationService();
		IIntegrationPipeLineConfigurationService integrationPipelineConfigurationService = new IntegrationPipelineConfigurationService();
		String configType = null;
		try {
			List<Map<String, Object>> requestContextList = configPersistenceService.getRequestContextList(feature,
					featureGroupName);
			for (Map<String, Object> map : requestContextList) {
				configType = (String) map.get("configType");
				configType = configType.trim().toLowerCase();
				switch (configType) {
				case "event":
					eventFrameworkConfigService.reloadEventCacheObject((RequestContext) map.get("requestContext"),
							(String) map.get("configName"));
					break;
				case "systemevent":
					eventFrameworkConfigService.reloadSystemEventCacheObject((RequestContext) map.get("requestContext"),
							(String) map.get("configName"));
					break;
				case "dispatchchanel":
					eventFrameworkConfigService.reloadDispatchChannelCacheObject(
							(RequestContext) map.get("requestContext"), (String) map.get("configName"));
					break;
				case "eventsubscription":
					eventFrameworkConfigService.reloadSubscriptionEventCacheObject(
							(RequestContext) map.get("requestContext"), (String) map.get("configName"));
					break;
				case "permastore":
					permaStoreConfigurationService.reloadPerStoreCacheObject((RequestContext) map.get("requestContext"),
							(String) map.get("configName"));
					break;
				case "policy":
					policyConfigurationService.reloadPolicyCacheObject((RequestContext) map.get("requestContext"),
							(String) map.get("configName"));
					break;
				case "feature":
					featureConfigurationService.reloadFeatureCacheObject((RequestContext) map.get("requestContext"),
							(String) map.get("configName"));
					break;
				case "datacontext":
					dataContextConfigurationService.reloadDataContextCacheObject(
							(RequestContext) map.get("requestContext"), (String) map.get("configName"));
					break;
				case "integrationpipeline":
					integrationPipelineConfigurationService.reloadIntegrationPipelineCacheObject(
							(RequestContext) map.get("requestContext"), (String) map.get("configName"));
					break;

				}

			}
		} catch (EventFrameworkConfigurationException | PermaStoreConfigurationException | PolicyConfigurationException
				| FeatureConfigRequestException | DataContextConfigurationException | IntegrationPipelineConfigException
				| ConfigPersistenceException e) {

			throw new FeatureMetaInfoResourceException("Unable to reload the resources from DB ", e);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void checkSchedulerResourceAndLoad(ScheduledJobConfigurations schedulerConfigurations, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "checkSchedulerResourceAndLoad";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (schedulerConfigurations != null) {
			List<ScheduledJobConfiguration> schedulerConfigList = schedulerConfigurations
					.getScheduledJobConfiguration();
			if (schedulerConfigList != null) {
				loadSchedulerResourceInFeatureMetaInfo(schedulerConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.debug(
						"{} No SchedulerResource is defined in FeatureMetaInfo for feature Group : {} feature name : {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature);
			}
		} else {
			logger.debug(
					"{} No SchedulerResource configured in FeatureMetaInfo for feature Group :{} , feature name : {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void loadSchedulerResourceInFeatureMetaInfo(List<ScheduledJobConfiguration> schedulerConfigList,
			String featureGroupName, String featureName, String implementationName, String vendorName,
			String vendorVersion) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadSchedulerResourceInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (ScheduledJobConfiguration schedulerConfig : schedulerConfigList) {
			logger.trace("{} schedulerConfig :{} ", LEAP_LOG_KEY, schedulerConfig.toString());
			String schedulerResourceName = schedulerConfig.getResourceName();
			logger.trace("{} schedulerResourceName : {}", LEAP_LOG_KEY, schedulerResourceName);
			ConfigurationContext configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implementationName, vendorName,
					vendorVersion);
			URL schedulerResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(schedulerResourceName,
					configurationContext);
			logger.trace("{} schedulerResourceUrl : {}", LEAP_LOG_KEY, schedulerResourceUrl);
			if (schedulerResourceUrl != null) {
				String schedulerSourceAsString = convertXmlToString(schedulerResourceUrl,
						schedulerConfig.getResourceName());
				logger.trace("{} schedulerSourceAsString : {}", LEAP_LOG_KEY, schedulerSourceAsString);
				if (schedulerSourceAsString != null) {
					ScheduledJobConfigXMLParser schedulerConfigParser = new ScheduledJobConfigXMLParser();
					com.attunedlabs.scheduler.jaxb.ScheduledJobConfigurations scheduler = null;
					try {
						scheduler = schedulerConfigParser.marshallConfigXMLtoObject(schedulerSourceAsString);
						List<com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration> schedulerConfigList1 = scheduler
								.getScheduledJobConfiguration();
						for (com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration schedulerConfiguration : schedulerConfigList1) {
							logger.trace("{} schedulerConfiguration : {}", LEAP_LOG_KEY,
									schedulerConfiguration.toString());
							String configname = schedulerConfiguration.getName();
							logger.trace("{} configname :{} ", LEAP_LOG_KEY, configname);
							IScheduledJobConfigurationService schedulerConfigService = new ScheduledJobConfigurationService();

							RequestContext requestContext = null;
							boolean isEnabled = schedulerConfiguration.getIsEnabled();
							logger.trace("{} isEnabled : {}", LEAP_LOG_KEY, isEnabled);
							// String response =
							// schedulerConfigParser.unmarshallConfigXMLtoObject(schedulerConfiguration);
							// logger.debug("{} response :"+response);
							if (isEnabled) {
								logger.trace("{} scheduler config is marked as global", LEAP_LOG_KEY);
								requestContext = new RequestContext(FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implementationName, vendorName, vendorVersion);
								configurationContext = new ConfigurationContext(
										FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implementationName, vendorName, vendorVersion);
							} else {
								logger.trace("{} scheduler config is not global", LEAP_LOG_KEY);
								requestContext = new RequestContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
										featureGroupName, featureName, implementationName, vendorName, vendorVersion);
								configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
										LeapHeaderConstant.site, featureGroupName, featureName, implementationName,
										vendorName, vendorVersion);
							}
							logger.trace("{} before isExist in loadSchedulerResourceInFeatureMetaInfo", LEAP_LOG_KEY);
							boolean isExist = schedulerConfigService.checkScheduledJobConfigarationExistOrNot(
									configurationContext, schedulerConfiguration.getName());
							logger.trace("{} after isExist in loadSchedulerResourceInFeatureMetaInfo isExist :{} ",
									LEAP_LOG_KEY, isExist);
							if (!isExist) {
								schedulerConfigService.addScheduledJobConfiguration(configurationContext,
										schedulerConfiguration);

							} else {
								logger.trace("{} Scheduler configuration for :{} already exist", LEAP_LOG_KEY,
										configname);
							}
						} // end of if(builderType.equalsIgnoreCase("CUSTOM"))
					} catch (ScheduledJobConfigParserException | ScheduledJobConfigRequestException
							| ScheduledJobConfigurationException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse scheduler file : " + schedulerConfig.getResourceName());
					}
				} // end of for
			} else {
				logger.debug("{} No scheduler config xml defined for : {}", LEAP_LOG_KEY, schedulerResourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Configures the database form DBConfiguration, from DBConfiguration - consumes
	 * the ddlUtil-database structure
	 * 
	 * @param dbConfiguration
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void configureDatabase(DBConfiguration dbConfiguration, Feature feature, String featureGroupName)
			throws FeatureMetaInfoResourceException {
		String methodName = "configureDatabase";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (dbConfiguration != null) {
			String databaseXml = "";
			try {
				databaseXml = getDbConfigasString(dbConfiguration);
				logger.trace("{} .configureDatabase(), the DatabaseConfigurations: {}", LEAP_LOG_KEY, databaseXml);
				RequestContext requestContext = getRequestContextFromConfigurationContext(feature, featureGroupName);
				DataSource dataSource = getDataSourceFromDataContext(requestContext);
				/*
				 * api-ddlUtils requires xml-hence converted from String to InputStream, which
				 * is read from FeatureMetaInfo
				 */
				InputStream input = IOUtils.toInputStream(databaseXml.trim(), "UTF-8");
				Reader reader = new InputStreamReader(input, "UTF-8");
				org.apache.ddlutils.model.Database desiredDB = new DatabaseIO().read(reader);

				configureTables(dataSource, desiredDB);// ..call to api-ddlutils

			} catch (JAXBException | IOException | DataContextConfigurationException | DataContextParserException
					| ParserConfigurationException | SAXException e) {
				throw new FeatureMetaInfoResourceException("Unable to process model for the DatabaseConfigured ", e);
			}
			logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		}
	}// ..end of the method

	/**
	 * 
	 * @param dataSource
	 * @param desiredDB
	 */
	private void configureTables(DataSource dataSource, org.apache.ddlutils.model.Database desiredDB) {
		String dbName = desiredDB.getName();
		logger.debug("{} Configured - dbName :{} ", LEAP_LOG_KEY, dbName);
		Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
		org.apache.ddlutils.model.Database currentDB = platform.readModelFromDatabase(dbName);
		platform.alterModelExtended(false, currentDB, desiredDB, false);

	}// ..end of the method

	/**
	 * 
	 * @param feature
	 * @param featureGroupName
	 * @return
	 * @throws FeatureMetaInfoResourceException
	 */
	private RequestContext getRequestContextFromConfigurationContext(Feature feature, String featureGroupName)
			throws FeatureMetaInfoResourceException {
		ConfigurationContext configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, featureGroupName, feature.getName(), feature.getImplementationName(),
				feature.getVendorName(), feature.getVendorVersion());
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		return requestContext;
	}

	/**
	 * method to create dataSource from the dataContext
	 * 
	 * @return dataSource
	 * @throws DataContextConfigurationException
	 * @throws DataContextParserException
	 * @throws ParserConfigurationException
	 * @throws FeatureMetaInfoResourceException
	 * @throws IOException
	 * @throws SAXException
	 */
	private DataSource getDataSourceFromDataContext(RequestContext requestContext)
			throws DataContextConfigurationException, DataContextParserException, FeatureMetaInfoResourceException,
			ParserConfigurationException, SAXException, IOException {

		DataContextConfigurationService configService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfig = configService.getDataContextConfiguration(requestContext);
		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfig.getConfigData();

		DataSource dataSource = null;
		try {
			dataSource = getDataSource(featureDataContext);
		} catch (FeatureConfigurationException e) {
			throw new FeatureMetaInfoResourceException("Unable to get the DataSource for the configured dataBase: ", e);
		}
		return dataSource;
	}// ..end of the method

	/**
	 * configures the dataSource object based on the dbType
	 * 
	 * @param featureDataContext
	 * @return javax.sql DataSource
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigRequestException
	 */
	private DataSource getDataSource(FeatureDataContext featureDataContext) throws FeatureConfigurationException {
		String methodName = "getDataSource";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		DataContext dataConetxt = featureDataContext.getDataContexts().getDataContext().get(0);
		String dbType = dataConetxt.getDbType();
		String dbHost = dataConetxt.getDbHost();
		String dbPort = dataConetxt.getDbPort();
		String dbSchema = dataConetxt.getDbSchema();
		String user = dataConetxt.getDbUser();
		String password = dataConetxt.getDbPassword();
		DataSource dataSource = null;
		switch (dbType.toLowerCase()) {
		case MYSQL_TYPE:
			dataSource = getMySqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		case ORACLE_TYPE:
			dataSource = getOracleDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			break;
		case MSSQL_TYPE:
			dataSource = getMSSqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		case POSTGRESSQL_TYPE:
			dataSource = getPostgresSqlDataSource(dbType, dbHost, dbPort, dbSchema, user, password);
			return dataSource;
		default:
			throw new FeatureConfigurationException(
					"Invalid dbType configured, configure any one of the following ['postgressql','mssql','oracle','mysql'].");
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return dataSource;
	}// ..end of the method

	/**
	 * prepares the datasource in java.sql.Datasource, from the PGPoolingDatasource
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return postgresql DataSource in java.sql casted
	 */
	private DataSource getPostgresSqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		PGPoolingDataSource source = new PGPoolingDataSource();
		source.setDataSourceName("postgresDatasource");
		source.setServerName(dbHost);
		source.setDatabaseName(dbSchema);
		source.setUser(user);
		source.setPassword(password);
		source.setMaxConnections(10);
		return source;
	}// ..end of the method

	/**
	 * constructs the SQLServer datasource, if called to perform the ddlutils
	 * operation
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return SQLServerDataSource
	 */
	private SQLServerDataSource getMSSqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		SQLServerDataSource ds = new SQLServerDataSource();
		ds.setUser(user);
		ds.setPassword(password);
		ds.setServerName(dbHost);
		ds.setPortNumber(Integer.valueOf(dbPort));
		// jdbc:sqlserver://HOSP_SQL1.company.com;user=name;password=abcdefg;database=Test
		String url = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";database=" + dbSchema + ";" + "user=" + user
				+ ";password=" + password + ";";
		ds.setURL(url);
		ds.setDatabaseName(dbSchema);
		return ds;
	}// ..end of the method

	/**
	 * Builds a new OracleDataSource to perform the ddlUtils DBConfiguration
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @param user
	 * @param password
	 * @return
	 * @throws FeatureConfigurationException
	 */
	private OracleDataSource getOracleDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) throws FeatureConfigurationException {
		OracleDataSource oracleDS = null;
		try {
			oracleDS = new OracleDataSource();
			oracleDS.setDatabaseName(dbSchema);
			oracleDS.setPortNumber(Integer.valueOf(dbPort));
			oracleDS.setUser(user);
			oracleDS.setPassword(password);
			// jdbc:oracle:thin:@192.168.1.78:1521:XE
			String url = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + "/" + dbSchema;
			oracleDS.setURL(url);
		} catch (SQLException e) {
			throw new FeatureConfigurationException("Unable to connect to OracleDataSource: ", e);
		}
		return oracleDS;
	}// ..end of the method

	/**
	 * Private accessory, returns the mysql DS for the type = MySql
	 * 
	 * @param dbType
	 * @param dbHost
	 * @param dbPort
	 * @param dbSchema
	 * @return mysqlDS
	 */
	private MysqlDataSource getMySqlDataSource(String dbType, String dbHost, String dbPort, String dbSchema,
			String user, String password) {
		MysqlDataSource mysqlDS = new MysqlDataSource();
		mysqlDS.setURL("jdbc:" + dbType + ":" + "//" + dbHost + ":" + dbPort + "/" + dbSchema);
		mysqlDS.setUser(user);
		mysqlDS.setPassword(password);

		return mysqlDS;
	}// ..end of the method

	/**
	 * Gets the object defined in FeatureMetaInfoa and returns the string of the
	 * databases-needed by ddlUtils
	 * 
	 * @param dbConfiguration
	 * @return xmlString which hold the Database
	 * @throws JAXBException
	 */
	private String getDbConfigasString(DBConfiguration dbConfiguration) throws JAXBException {
		StringWriter xmlOut = new StringWriter();
		JAXBContext contextObj = JAXBContext.newInstance(Database.class);
		Marshaller marshallerObj = contextObj.createMarshaller();
		marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshallerObj.marshal(dbConfiguration.getDatabase(), xmlOut);

		StringBuffer sb = new StringBuffer(xmlOut.toString());
		sb.insert(56, DDLUITLS_XMLDOCTYPE);

		logger.trace("{} .getDbConfigasString(), the DatabaseConfigurations: {}", LEAP_LOG_KEY, sb);

		return sb.toString();
	}// ..end of the method

	/**
	 * to store the StaticFileConfigs into the zookeeperNode
	 * 
	 * @param staticFileConfiguration
	 * @param feature
	 * @param featureGroupName
	 * @throws IOException
	 * @throws FeatureMetaInfoResourceException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void storeStaticFileConfigs(StaticFileConfiguration staticFileConfiguration, Feature feature,
			String featureGroupName) throws IOException, FeatureMetaInfoResourceException {
		String methodName = "storeStaticFileConfigs";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (staticFileConfiguration != null) {
			logger.trace("{} StaticConfigFileNames...{}", LEAP_LOG_KEY, staticFileConfiguration.getConfigFile());
			List<ConfigFile> statConfigs = staticFileConfiguration.getConfigFile();
			String tenantID = LeapHeaderConstant.tenant;
			String siteID = LeapHeaderConstant.site;
			ConfigurationContext configurationContext = new ConfigurationContext(tenantID, siteID, featureGroupName,
					feature.getName(), feature.getImplementationName(), feature.getVendorName(),
					feature.getVendorVersion());
			try {
				IStaticConfigurationService staticConfigurationService = StaticConfigurationFactory
						.getFilemanagerInstance();
				if (staticConfigurationService instanceof ZookeeperFilemanagementServiceImpl) {
					for (ConfigFile configFile : statConfigs) {
						String localPath = configFile.getFilePath();
						int index = localPath.length() - 1;
						if (!(localPath.charAt(index) == '/')) {
							localPath = localPath + "/";
						} // ..checking and appending '/' at the end of the
							// directory
						logger.trace("{} ConfigNames...{} ---: Path specified is...", LEAP_LOG_KEY,
								configFile.getFileName(), localPath);
						try {
							String contents = null;
							contents = new String(Files.readAllBytes(Paths.get(localPath + configFile.getFileName())));
							staticConfigurationService.addStaticConfiguration(configurationContext,
									configFile.getFileName(), contents);
						} catch (StaticConfigDuplicateNameofFileException | AddStaticConfigException
								| AccessProtectionException | InvalidFilePathException e) {
							// This step is to skip the redundancy of file
							logger.trace("{} File Not saved in zookeeper nodes.. {}", LEAP_LOG_KEY,
									configFile.getFileName());
						}
					}
				}
			} catch (InstantiationException | IllegalAccessException | StaticConfigInitializationException e) {
				throw new FeatureMetaInfoResourceException("Unable to instantiate the StaticFileConfiguration", e);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	/**
	 * this is the method which is called to load, validate and add to the database
	 * 
	 * @param integrationPipes
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void checkIntegrationPipeCongigurationsResourceAndLoad(IntegrationPipeLineConfigurations integrationPipes,
			Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "checkIntegrationPipeCongigurationsResourceAndLoad";
		logger.debug("{} entered into the method {}, integrationPipes: {}", LEAP_LOG_KEY, methodName, integrationPipes);
		if (integrationPipes != null) {
			List<PipeConfiguration> listOfPipeConfigs = integrationPipes.getPipeConfiguration();
			if (listOfPipeConfigs != null) {
				loadPipeConfigResourcesInFeatureMetaInfo(listOfPipeConfigs, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No IntegrationPipeline is defined in FeatureMetaInfo for feature Group : {} feature name : {}, impl name : {}but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace(
					"{} No IntegrationPipeline configured in FeatureMetaInfo for feature Group :{}, feature name :{} ",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	/**
	 * This method is used to load validate and add integration configuration in
	 * database and cache
	 * 
	 * @param featureDataContexts
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkFeatureDataContextsResourceAndLoad(FeatureDataContexts featureDataContexts, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "checkFeatureDataContextsResourceAndLoad";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace("{} featureDataContexts : {}", LEAP_LOG_KEY, featureDataContexts);
		if (featureDataContexts != null) {
			List<DataContexts> dataContextsList = featureDataContexts.getDataContexts();
			if (dataContextsList != null) {
				logger.trace("{} calling method to  load datacontext", LEAP_LOG_KEY);
				loadDataContextResourcesInFeatureMetaInfo(dataContextsList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No DataContext is defined in FeatureMetaInfo for feature Group :{} , feature name :{} , impl name :{}  but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace("{} No DataContext configured in FeatureMetaInfo for feature Group :{}, feature name :{} ",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method checkFeatureDataContextsResourceAndLoad

	private void loadDataContextResourcesInFeatureMetaInfo(List<DataContexts> dataContextsList, String featureGroupName,
			String name, String implName, String vendorName, String vendorVersion)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadDataContextResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (DataContexts dataContexts : dataContextsList) {
			String dataContextResourceName = dataContexts.getResourceName();
			logger.trace("{} dataContextResourceName : {}", LEAP_LOG_KEY, dataContextResourceName);
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, name, implName, vendorName, vendorVersion);
			URL dataContextResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(dataContextResourceName,
					configContext);
			if (dataContextResourceUrl != null) {
				String dataContextAsSourceAsString = convertXmlToString(dataContextResourceUrl,
						dataContextResourceName);
				if (dataContextAsSourceAsString != null) {
					DataContextConfigXMLParser dataContextXmlParser = new DataContextConfigXMLParser();
					IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();

					try {
						FeatureDataContext featureDataContext = dataContextXmlParser
								.marshallConfigXMLtoObject(dataContextAsSourceAsString);

						try {
							boolean isExist = dataContextConfigService.checkDataContextConfigExistOrNot(configContext,
									featureDataContext.getDataContexts().getContextName());
							if (!isExist) {
								dataContextConfigService.addDataContext(configContext, featureDataContext);
							} else {
								logger.trace(
										"{} pipeline configuration for : {} already exist for featuregroup : {} and feature : {} , impl name : {} in db",
										LEAP_LOG_KEY, dataContextResourceName, featureGroupName, name, implName);
							}
						} catch (DataContextConfigurationException e) {
							throw new FeatureMetaInfoResourceException(
									"Unable to add configuration file for feature group :  " + featureGroupName
											+ ", feature name : " + name + ", with context data : " + configContext);

						}

					} catch (DataContextParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse datacontext configuration file for feature group :  "
										+ featureGroupName + ", feature name : " + name);

					}

				}
			} else {
				logger.trace("{} No datacontext config xml exist with name : {}", LEAP_LOG_KEY,
						dataContextResourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method loadDataContextResourcesInFeatureMetaInfo

	/**
	 * This method is used to check and load event resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param eventResources   : EventResources Object of FeatureMetaInfo
	 * @param feature          : feature Name
	 * @param featureGroupName : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkEventResourceAndLoad(EventResources eventResources, Feature feature, String featureGroupName)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		if (eventResources != null) {
			List<EventResource> eventResourceList = eventResources.getEventResource();
			if (eventResourceList != null) {
				loadEventResourcesInFeatureMetaInfo(eventResourceList, featureGroupName, feature);

			} else {
				logger.trace(
						"{} No EventResource is defined in FeatureMetaInfo for feature Group :  {}, feature name :  {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature);
			}
		} else {
			logger.trace("{} No EventResource configured in FeatureMetaInfo for feature Group : {}, feature name :  {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
	}

	/**
	 * This method is used to load handler configuration defined in
	 * featureMetaInfo.xml
	 * 
	 * @param serviceHandlerConfiguration
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkServiceHandlerConfigurationAndLoad(ServiceHandlerConfiguration serviceHandlerConfiguration,
			Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "checkServiceHandlerConfigurationAndLoad";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (serviceHandlerConfiguration != null) {
			HandlerConfiguration handlerConfiguration = serviceHandlerConfiguration.getHandlerConfiguration();
			if (handlerConfiguration != null) {
				loadHandlerConfigurationInFeatureMetaInfo(handlerConfiguration, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No HandlerConfiguration is defined in FeatureMetaInfo for feature Group :  {}, feature name :  {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature);
			}
		} else {
			logger.trace(
					"{} No HandlerConfiguration configured in FeatureMetaInfo for feature Group :  {}, feature name :  {} ",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to check and load permastore resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param permastoreConfiguration : PermaStoreConfigurations Object of
	 *                                FeatureMetaInfo
	 * @param feature                 : feature Name
	 * @param featureGroupName        : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkPermastoreResourceAndLoad(
			com.attunedlabs.featuremetainfo.jaxb.PermaStoreConfigurations permastoreConfiguration, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		if (permastoreConfiguration != null) {
			List<PermaStoreConfiguration> permastoreConfigList = permastoreConfiguration.getPermaStoreConfiguration();
			if (permastoreConfigList != null) {
				loadPermastoreResourceInFeatureMetaInfo(permastoreConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No PermastoreResource is defined in FeatureMetaInfo for feature Group :  {}, feature name :  {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature);
			}
		} else {
			logger.trace(
					"{} No PermastoreResource configured in FeatureMetaInfo for feature Group :  {}, feature name :  {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
	}

	/**
	 * This method is used to check and load LeapEntity resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param leapEntityConfigurations : LeapEntityConfigurations Object of
	 *                                 FeatureMetaInfo
	 * @param feature                  : feature Name
	 * @param featureGroupName         : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkLeapEntityResourceAndLoad(LeapEntityConfigurations leapEntityConfigurations, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "checkLeapEntityResourceAndLoad";
		logger.debug("{} entered into the method {}, leapEntityConfigurations : {}", LEAP_LOG_KEY, methodName,
				leapEntityConfigurations);
		if (leapEntityConfigurations != null) {
			List<LeapEntityConfiguration> leapEntityConfigList = leapEntityConfigurations.getLeapEntityConfigurations();
			logger.trace("{} leapEntityConfigList is : {} ", LEAP_LOG_KEY, leapEntityConfigList);
			if (leapEntityConfigList != null) {
				loadLeapEntityResourceInFeatureMetaInfo(leapEntityConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No LeapEntityResource is defined in FeatureMetaInfo for feature Group :  {}, feature name :  {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature);
			}
		} else {
			logger.trace(
					"{} No LeapEntityResource configured in FeatureMetaInfo for feature Group :  {}, feature name :  {} ",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to check and load PolicyResource resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param policyConfiguration : PolicyConfigurations Object of FeatureMetaInfo
	 * @param feature             : feature Name
	 * @param featureGroupName    : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void checkPolicyResourceAndLoad(PolicyConfigurations policyConfiguration, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		if (policyConfiguration != null) {
			List<PolicyConfiguration> policyConfigList = policyConfiguration.getPolicyConfiguration();
			if (policyConfigList != null) {
				loadPolicyResourceInFeatureMetaInfo(policyConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No PolicyResource is defined in FeatureMetaInfo for feature Group : {}, feature name : {} impl name : {}  but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace("{} No PolicyResource configured in FeatureMetaInfo for feature Group : {}, feature name : {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
	}

	/**
	 * This method is used to check and load FeatureImplResource resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param FeatureImplementations : FeatureImplementations Object of
	 *                               FeatureMetaInfo
	 * @param feature                : feature Name
	 * @param featureGroupName       : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 * @throws PrettyUrlMappingDaoException
	 */
	private void checkFeatureImplResourceAndLoad(FeatureImplementations featureImplementations, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		if (featureImplementations != null) {
			List<FeatureImplementation> featureImplList = featureImplementations.getFeatureImplementation();
			if (featureImplList != null) {
				loadFeatureResourceInFeatureMetaInfo(featureImplList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion(),
						feature.getProvider(), feature.getVendorTaxonomyId());
			} else {
				logger.trace(
						"{} No FeatureImplResource is defined in FeatureMetaInfo for feature Group :{}, feature name : {} , impl name : {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace(
					"{} No FeatureImplResource configured in FeatureMetaInfo for feature Group : {} , feature name {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
	}

	/**
	 * This method is used to check and load DynastoreResource resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param dynastoreConfiguration : DynaStoreConfigurations Object of
	 *                               FeatureMetaInfo
	 * @param feature                : feature Name
	 * @param featureGroupName       : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void checkDynastoreResourceAndLoad(DynaStoreConfigurations dynastoreConfiguration, Feature feature,
			String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		if (dynastoreConfiguration != null) {
			List<DynaStoreConfiguration> dynaStoreConfigList = feature.getDynaStoreConfigurations()
					.getDynaStoreConfiguration();
			if (dynaStoreConfigList != null) {
				loadDynastoreResourceInFeatureMetaInfo(dynaStoreConfigList, featureGroupName, feature.getName(),
						feature.getImplementationName(), feature.getVendorName(), feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No DynastoreResource is defined in FeatureMetaInfo for feature Group :{}, feature name : {} , impl name : {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace(
					"{} No DynastoreResource configured in FeatureMetaInfo for feature Group : {} , feature name {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}
	}

	/**
	 * This method is used to check and load ResourceManagement resource defined in
	 * featureMetaInfo.xml
	 * 
	 * @param resourceManagerConfiguration : ResourceManagerConfiguration Object of
	 *                                     FeatureMetaInfo
	 * @param feature                      : feature Name
	 * @param featureGroupName             : Feature group name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void checkResourceMangementResourceAndLoad(ResourceManagerConfiguration resourceManagerConfiguration,
			Feature feature, String featureGroupName) throws FeatureMetaInfoResourceException, MalformedURLException {
		if (resourceManagerConfiguration != null) {
			List<ResourceManager> resourceManagerList = resourceManagerConfiguration.getResourceManager();
			if (resourceManagerList != null && resourceManagerList.size() != 0) {
				loadResourceManagementResourceInFeatureMetaInfo(resourceManagerList, featureGroupName,
						feature.getName(), feature.getImplementationName(), feature.getVendorName(),
						feature.getVendorVersion());
			} else {
				logger.trace(
						"{} No ResourceManagement is defined in FeatureMetaInfo for feature Group :{}, feature name : {} , impl name : {} but empty",
						LEAP_LOG_KEY, featureGroupName, feature, feature.getImplementationName());
			}
		} else {
			logger.trace(
					"{} No ResourceManagement Resource configured in FeatureMetaInfo for feature Group : {} , feature name {}",
					LEAP_LOG_KEY, featureGroupName, feature);
		}

	}

	private void loadResourceManagementResourceInFeatureMetaInfo(List<ResourceManager> resourceManagerList,
			String featureGroupName, String name, String implementationName, String vendorName, String vendorVersion)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadResourceManagementResourceInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace("{} .loadResourceManagementResourceInFeatureMetaInfo of FeatureMetaInfoResourceUtil",
				LEAP_LOG_KEY);
		for (ResourceManager resourceManagerConfiguration : resourceManagerList) {
			String resourceManagerResourceName = resourceManagerConfiguration.getResourceName();
			logger.trace("{} resourceManagerResourceName :{} ", LEAP_LOG_KEY, resourceManagerResourceName);
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, name, implementationName, vendorName, vendorName);
			URL ResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(resourceManagerResourceName, configContext);
			if (ResourceUrl != null) {
				String resourcemanagerAsSourceAsString = convertXmlToString(ResourceUrl, resourceManagerResourceName);
				if (resourcemanagerAsSourceAsString != null) {
					ResourceManagementConfigXmlParser parser = new ResourceManagementConfigXmlParser();
					IResourceManagementConfigService iResourceManagementConfigurationService = null;
					try {
						ResourceManagerConfig resourceManagerConfig = parser
								.marshallConfigXMLtoObject(resourcemanagerAsSourceAsString);

						iResourceManagementConfigurationService = new ResourceManagementConfigService();
						ConnectionInfo connectionInfo = resourceManagerConfig.getConnectionInfo();
						try {
							iResourceManagementConfigurationService.addConnectionInfoConfiguration(configContext,
									connectionInfo);
						} catch (ResourceManagementConfigurationException e) {
							throw new FeatureMetaInfoResourceException(
									"Unable to add ressourceManagement connectionInfo configuration file for feature group :  "
											+ featureGroupName + ", feature name : " + name + ", with context data : "
											+ configContext);
						}

						List<GetResourceContent> getResourceContentList = resourceManagerConfig.getGetResourceContent();
						for (GetResourceContent getResourceContent : getResourceContentList) {

							try {
								iResourceManagementConfigurationService
										.addGetResourceContentConfiguration(configContext, getResourceContent);
							} catch (ResourceManagementConfigurationException e) {
								throw new FeatureMetaInfoResourceException(
										"Unable to add ressourceManagement getResourceContent configuration file for feature group :  "
												+ featureGroupName + ", feature name : " + name
												+ ", with context data : " + configContext);
							}
						}

					} catch (ResourceManagementConfigParserException e) {
						e.printStackTrace();
						throw new FeatureMetaInfoResourceException(
								"Unable to parse resource management configuration file for feature group :  "
										+ featureGroupName + ", feature name : " + name);
					}

				} // end of if(dynastoreAsSourceAsString !=null)
			} else {
				logger.trace("{} No resource management config xml exist with name :{} ", LEAP_LOG_KEY,
						resourceManagerResourceName);
			}
		} // end of for
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void loadDynastoreResourceInFeatureMetaInfo(List<DynaStoreConfiguration> dynaStoreConfigList,
			String featureGroupName, String name, String implName, String vendorName, String version)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadDynastoreResourceInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (DynaStoreConfiguration dynastoreconfig : dynaStoreConfigList) {
			String dynaResourceName = dynastoreconfig.getResourceName();
			logger.trace("{} dynaResourceName :{} ", LEAP_LOG_KEY, dynaResourceName);
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, name, implName, vendorName, version);
			URL dynaResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(dynaResourceName, configContext);
			if (dynaResourceUrl != null) {
				String dynastoreAsSourceAsString = convertXmlToString(dynaResourceUrl, dynaResourceName);
				if (dynastoreAsSourceAsString != null) {
					DynaStoreConfigXmlParser parser = new DynaStoreConfigXmlParser();
					IDynaStoreConfigurationService iDynaStoreConfigurationService = null;

					try {
						DynastoreConfigurations dynastoreConfigurations = parser
								.marshallConfigXMLtoObject(dynastoreAsSourceAsString);
						iDynaStoreConfigurationService = new DynaStoreConfigurationService();
						List<DynastoreConfiguration> dynastoreConfigList = dynastoreConfigurations
								.getDynastoreConfiguration();
						for (DynastoreConfiguration dynaConfig : dynastoreConfigList) {
							try {
								iDynaStoreConfigurationService.addDynaStoreConfiguration(configContext, dynaConfig);
							} catch (DynaStoreConfigurationException e) {
								throw new FeatureMetaInfoResourceException(
										"Unable to add configuration file for feature group :  " + featureGroupName
												+ ", feature name : " + name + ", with context data : "
												+ configContext);
							}
						} // end of for
					} catch (DynaStoreConfigParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse dynastore configuration file for feature group :  " + featureGroupName
										+ ", feature name : " + name);
					}

				} // end of if(dynastoreAsSourceAsString !=null)
			} else {
				logger.trace("{} No dynastore config xml exist with name :{} ", LEAP_LOG_KEY, dynaResourceName);
			}
		} // end of for
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	private void loadEventResourcesInFeatureMetaInfo(List<EventResource> eventResourceList, String featureGroupName,
			Feature feature) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadEventResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		String featureName = feature.getName();
		String implName = feature.getImplementationName();
		String vendorName = feature.getVendorName();
		String version = feature.getVendorVersion();

		for (EventResource eventresource : eventResourceList) {
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implName, vendorName, version);
			String eventResourceName = eventresource.getResourceName();
			URL eventResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(eventResourceName, configContext);
			if (eventResourceUrl != null) {
				logger.trace("{} eventResourceUrl :{} ", LEAP_LOG_KEY, eventResourceUrl);
				String eventSourceAsString = convertXmlToString(eventResourceUrl, eventresource.getResourceName());
				if (eventresource != null) {
					EventFrameworkXmlHandler parser = new EventFrameworkXmlHandler();
					EventFramework eventFrameworkConfig = null;
					IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
					try {
						eventFrameworkConfig = parser.marshallConfigXMLtoObject(eventSourceAsString);
						// prepare the configcontext for eventing

						// check dispatcher defined or not and then load
						// configuration
						DispatchChannels dispatcherChanel = eventFrameworkConfig.getDispatchChannels();
						List<String> dispatchChannelIds = loadEventChanelConfigurationGetDispatchChannelIds(
								dispatcherChanel, eventConfigService, configContext);

						// check SystemEvents defined or not and then load
						// configuration
						SystemEvents systemEvent = eventFrameworkConfig.getSystemEvents();

						// checking dispatchers is valid for systemEvents
						checkSystemEvenDispatchersIsValid(dispatchChannelIds, systemEvent);

						boolean isLoggingEvent = loadSystemEventConfiguration(systemEvent, eventConfigService,
								configContext);
						if (isLoggingEvent) {
							LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer
									.getConfigurationService();
							leapConfigurationServer.loadLoggingFeatureInDataGrid(feature, featureGroupName);
						}

						// check Events defined or not and then load
						// configuration
						Events events = eventFrameworkConfig.getEvents();

						// checking dispatchers is valid for LeapEvents
						checkEventDispatchersIsValid(dispatchChannelIds, events);

						loadEventConfiguration(events, eventConfigService, configContext);

						// check event subscription defined or not and then load
						// configuration
						EventSubscription eventSusbscriptions = eventFrameworkConfig.getEventSubscription();
						loadEventSubscriptionConfiguration(eventSusbscriptions, eventConfigService, configContext,
								featureGroupName, feature);
					} catch (EventFrameworkConfigParserException | ConfigServerInitializationException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse event file : " + eventresource.getResourceName());
					}
				} // end of if(eventresource !=null)
				logger.trace("{} exiting getEventResourcesInFeatureMetaInfo of FeatureMetaInfoResourceUtil",
						LEAP_LOG_KEY);
			} else {
				logger.trace("{} no such event source defined : {}", LEAP_LOG_KEY, eventResourceName);
			}
		} // end of for
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	private void checkSystemEvenDispatchersIsValid(List<String> dispatchChannelIds, SystemEvents systemEvents)
			throws FeatureMetaInfoResourceException {
		String methodName = "checkSystemEvenDispatchersIsValid";
		logger.debug("{} entered into the method {} dispatchChannelIds {} systemEvents {},", LEAP_LOG_KEY, methodName,
				dispatchChannelIds, systemEvents);
		boolean isConfigValid = true;
		if (systemEvents != null) {
			List<SystemEvent> systemEventList = systemEvents.getSystemEvent();
			mainLoop: for (SystemEvent systemEvent : systemEventList) {
				EventDispatchers eventDispatchers = systemEvent.getEventDispatchers();
				List<EventDispatcher> dispatchers = eventDispatchers.getEventDispatcher();
				for (EventDispatcher dispatcher : dispatchers) {
					isConfigValid = dispatchChannelIds.contains(dispatcher.getDispatchChannelId());
					if (!isConfigValid) {
						logger.error("{} invalid dispatch channelId :{}", LEAP_LOG_KEY,
								dispatcher.getDispatchChannelId());
						throw new FeatureMetaInfoResourceException(
								"Dispatcher Channel configuration doesn't exits for dispatchChannelId :: "
										+ dispatcher.getDispatchChannelId(),
								new Throwable());
					}
				}
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void checkEventDispatchersIsValid(List<String> dispatchChannelIds, Events events)
			throws FeatureMetaInfoResourceException {
		String methodName = "loadDataContextResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean isConfigValid = true;
		if (events != null) {
			List<Event> eventsList = events.getEvent();
			mainLoop: for (Event event : eventsList) {
				EventDispatchers eventDispatchers = event.getEventDispatchers();
				List<EventDispatcher> dispatchers = eventDispatchers.getEventDispatcher();
				for (EventDispatcher dispatcher : dispatchers) {
					isConfigValid = dispatchChannelIds.contains(dispatcher.getDispatchChannelId());
					if (!isConfigValid) {
						logger.error("{} invalid dispatch channelId :{}", LEAP_LOG_KEY,
								dispatcher.getDispatchChannelId());
						throw new FeatureMetaInfoResourceException(
								"Dispatcher Channel configuration doesn't exits for dispatchChannelId :: "
										+ dispatcher.getDispatchChannelId(),
								new Throwable());
					}
				}
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to add event susbscription for the event in cache and in
	 * db
	 * 
	 * @param eventSusbscriptions : EventSubscriptions Object
	 * @param eventConfigService  : EventFrameworkConfigService Object
	 * @param configContext       : ConfigurationContext Object
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadEventSubscriptionConfiguration(EventSubscription eventSusbscriptions,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext,
			String featureGroupName, Feature feature) throws FeatureMetaInfoResourceException {
		String methodName = "loadEventSubscriptionConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (eventSusbscriptions != null) {
			List<SubscribeEvent> eventSubscriptionList = eventSusbscriptions.getSubscribeEvent();
			List<JMSSubscribeEvent> jmsSubscribeEventList = eventSusbscriptions.getJmsSubscribeEvent();
			for (SubscribeEvent eventSubscription : eventSubscriptionList) {
				try {
					// repace character with '-' to '_' for subscriptionId
					eventSubscription.setSubscriptionId(eventSubscription.getSubscriptionId().replaceAll(
							SubscriptionConstant.SUBSCRIPTION_ID_REPLACEMENT_CHARACTER,
							SubscriptionConstant.SUBSCRIPTION_ID_NEW_CHARACTER));
					eventConfigService.addEventFrameworkConfiguration(configContext, eventSubscription);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in adding eventSubscription configuration ", e);

				}
			} // end of for loop

			if (jmsSubscribeEventList != null && !jmsSubscribeEventList.isEmpty()) {
				for (JMSSubscribeEvent jmsEventSubscription : jmsSubscribeEventList) {
					try {
						// repace character with '-' to '_' for subscriptionId
						jmsEventSubscription.setSubscriptionId(jmsEventSubscription.getSubscriptionId().replaceAll(
								SubscriptionConstant.SUBSCRIPTION_ID_REPLACEMENT_CHARACTER,
								SubscriptionConstant.SUBSCRIPTION_ID_NEW_CHARACTER));
						eventConfigService.addEventFrameworkConfiguration(configContext, jmsEventSubscription);
					} catch (EventFrameworkConfigurationException e) {
						throw new FeatureMetaInfoResourceException(
								"Error in adding jmsEventSubscription configuration ", e);

					}
				} // end of for loop
			}

			// add subscription features in dataGrid
			try {
				LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
				leapConfigurationServer.loadFeatureInDataGrid(feature, featureGroupName);// ALL-CONFIG-CONTEXT-KEY
																							// as
																							// the
																							// listName
																							// in
																							// datagrid
			} catch (ConfigServerInitializationException e) {
				logger.warn("{} Unable to add request context for subscription in datagrid...{}", LEAP_LOG_KEY,
						e.getMessage());
			}
		} else {
			logger.trace("{} Event subscription is undefined", LEAP_LOG_KEY);

		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used to add channel configuration of event
	 * 
	 * @param dispatcherChanel   : DispatchChanels Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext      : ConfigurationContext Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private List<String> loadEventChanelConfigurationGetDispatchChannelIds(DispatchChannels dispatcherChanel,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext)
			throws FeatureMetaInfoResourceException {
		String methodName = "loadEventChanelConfigurationGetDispatchChannelIds";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<String> dispatchChannnelIds = new ArrayList<String>();
		if (dispatcherChanel != null) {
			List<DispatchChannel> disChanelList = dispatcherChanel.getDispatchChannel();
			// addchanel init cache
			for (DispatchChannel disChanelConfig : disChanelList) {
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, disChanelConfig);
					dispatchChannnelIds.add(disChanelConfig.getId());
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in adding channel configuration ", e);

				}
			} // end of for loop
		} else {
			logger.trace("{} dispatcher channel is not defined for the event configuration", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return dispatchChannnelIds;
	}// end of method

	/**
	 * This method is used to add system event configuration of event
	 * 
	 * @param systemEvent        : SystemEvents Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext      : ConfigurationContext Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private boolean loadSystemEventConfiguration(SystemEvents systemEvents,
			IEventFrameworkConfigService eventConfigService, ConfigurationContext configContext)
			throws FeatureMetaInfoResourceException {
		String methodName = "loadSystemEventConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean hasLoggingSystemEvt = false;
		if (systemEvents != null) {
			List<SystemEvent> systemEventList = systemEvents.getSystemEvent();
			// add system events
			for (SystemEvent systemEvent : systemEventList) {
				List<EventDispatcher> eventDispacherList = systemEvent.getEventDispatchers().getEventDispatcher();
				if (!hasLoggingSystemEvt)
					hasLoggingSystemEvt = systemEvent.getCamelSystemEventBuilder().getFqcn().trim()
							.equals(ServicePerformanceLoggingEventBuilder.class.getName());
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("XML-XSLT")) {
						logger.trace("{} event for whom xslt defined : {}", LEAP_LOG_KEY, systemEvent);
						String xslName = eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(xslName);
						logger.trace("{} xsl url :{}  for xslt name : {}", LEAP_LOG_KEY, xslUrl, xslName);
						String xslAsString = convertXmlToString(xslUrl, xslName);
						logger.trace("{} xslt As String : {}", LEAP_LOG_KEY, xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, systemEvent);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in adding System event configuration ", e);
				}
			} // end of for(SystemEvent systemEvent : systemEventList)
		} else {
			System.setProperty(IS_SYSTEM_EVENT_EXIST, "false");
			logger.debug("{} System event is not defined for the event configuration", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return hasLoggingSystemEvt;
	}// end of method

	/**
	 * This method is used to add event configuration of event
	 * 
	 * @param events             : Events Object
	 * @param eventConfigService : EventFrameworkConfigService Object
	 * @param configContext      : ConfigurationContext Object
	 * @throws FeatureMetaInfoResourceException
	 */
	private void loadEventConfiguration(Events events, IEventFrameworkConfigService eventConfigService,
			ConfigurationContext configContext) throws FeatureMetaInfoResourceException {
		String methodName = "loadEventConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (events != null) {
			List<Event> eventList = events.getEvent();
			// add events
			for (Event event : eventList) {
				List<EventDispatcher> eventDispacherList = event.getEventDispatchers().getEventDispatcher();
				for (EventDispatcher eventDispacher : eventDispacherList) {
					String transformationtype = eventDispacher.getEventTransformation().getType();
					if (transformationtype.equalsIgnoreCase("XML-XSLT")) {
						logger.trace("{} event for which xslt defined :{} ", LEAP_LOG_KEY, event.getId());
						String xslName = eventDispacher.getEventTransformation().getXSLTName();
						URL xslUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(xslName);
						logger.trace("{} xsl url : {}, for xslt name :{}", LEAP_LOG_KEY, xslUrl, xslName);
						String xslAsString = convertXmlToString(xslUrl, xslName);
						logger.trace("{} xslt As String :{} ", LEAP_LOG_KEY, xslAsString);
						eventDispacher.getEventTransformation().setXsltAsString(xslAsString);
					}
				}
				try {
					eventConfigService.addEventFrameworkConfiguration(configContext, event);
				} catch (EventFrameworkConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Error in adding  event configuration ", e);

				}

			}
		} else {
			System.setProperty(IS_EVENT_EXIST, "false");
			logger.debug("{} events is not defined for the event configuration", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	private void loadHandlerConfigurationInFeatureMetaInfo(HandlerConfiguration handlerConfiguration,
			String featureGroupName, String featureName, String implementationName, String vendorName,
			String vendorVersion) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadHandlerConfigurationInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String resourceName = handlerConfiguration.getResourceName();

		ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, featureGroupName, featureName, implementationName, vendorName, vendorVersion);
		URL handlerResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(resourceName, configContext);
		IServiceHandlerConfigurationService serviceHandlerConfigurationService = new ServiceHandlerConfigurationService();
		if (handlerResourceUrl != null) {
			String handlerConfigurationXML = convertXmlToString(handlerResourceUrl, resourceName);
			if (handlerConfigurationXML != null) {
				ServiceHandlerConfigXMLParser serviceHandlerConfigXMLParser = new ServiceHandlerConfigXMLParser();
				try {
					com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration serviceHandlerConfiguration = serviceHandlerConfigXMLParser
							.marshallConfigXMLtoObject(handlerConfigurationXML);
					if (serviceHandlerConfiguration.getFeatureServiceHandler() != null)
						serviceHandlerConfigurationService.addFeatureServiceHandlerConfiguration(configContext,
								serviceHandlerConfiguration.getFeatureServiceHandler());
				} catch (ServiceHandlerConfigParserException | BeanDependencyResolveException
						| ServiceHandlerConfigurationException e) {
					throw new FeatureMetaInfoResourceException("Unable to parse service handler file : " + resourceName,
							e);
				}
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void loadPermastoreResourceInFeatureMetaInfo(List<PermaStoreConfiguration> permastoreConfigList,
			String featureGroupName, String featureName, String implName, String vendorName, String version)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadPermastoreResourceInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationContext configurationContext = null;
		for (PermaStoreConfiguration permastore : permastoreConfigList) {
			String permastoreResourceName = permastore.getResourceName();
			configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
					featureGroupName, featureName, implName, vendorName, version);
			URL permastoreResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(permastoreResourceName,
					configurationContext);
			if (permastoreResourceUrl != null) {
				String permastoreSourceAsString = convertXmlToString(permastoreResourceUrl,
						permastore.getResourceName());
				if (permastoreSourceAsString != null) {
					PermaStoreConfigXMLParser parmastoreConfigParser = new PermaStoreConfigXMLParser();
					PermaStoreConfigurations permastorConfig = null;
					try {
						permastorConfig = parmastoreConfigParser.marshallConfigXMLtoObject(permastoreSourceAsString);
						List<com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration> permastoreConfigList1 = permastorConfig
								.getPermaStoreConfiguration();
						for (com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration : permastoreConfigList1) {
							String configname = permaStoreConfiguration.getName();
							IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
							RequestContext requestContext = null;
							boolean isGlobal = permaStoreConfiguration.isIsGlobal();
							if (isGlobal) {
								logger.trace("{} permastore config is marked as global", LEAP_LOG_KEY);
								requestContext = new RequestContext(FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implName, vendorName, version);
								configurationContext = new ConfigurationContext(
										FeatureMetaInfoConstant.DEFAULT_TENANT_KEY,
										FeatureMetaInfoConstant.DEFAULT_SITE_KEY, featureGroupName, featureName,
										implName, vendorName, version);
							} else {
								logger.trace("{} permastore config is not global", LEAP_LOG_KEY);
								requestContext = new RequestContext(LeapHeaderConstant.tenant, LeapHeaderConstant.site,
										featureGroupName, featureName, implName, vendorName, version);
								configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
										LeapHeaderConstant.site, featureGroupName, featureName, implName, vendorName,
										version);
							}
							try {
								boolean isExist = psConfigService.checkPermaStoreConfigarationExistOrNot(
										configurationContext, permaStoreConfiguration.getName());
								if (!isExist) {
									psConfigService.addPermaStoreConfiguration(configurationContext,
											permaStoreConfiguration);

								} else {
									logger.trace(
											"{} Permastore configuration for :{} already exist for featuregroup :{}  and feature :{}  in db",
											LEAP_LOG_KEY, configname, featureGroupName, featureName);
								}

							} catch (PermaStoreConfigurationException | PermaStoreConfigRequestException e) {
								throw new FeatureMetaInfoResourceException(
										"error in loading the PermastoreConfiguration ", e);

							}
						} // end of if(builderType.equalsIgnoreCase("CUSTOM"))
					} catch (PermaStoreConfigParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse permastore file : " + permastore.getResourceName());
					}
				} // end of for
			} else {
				logger.trace("{} No permastore config xml defined for : {}", LEAP_LOG_KEY, permastoreResourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	private void loadFeatureDefaultTaxonomyAsPermastore(String featureGroupName, String featureName, String implName,
			String vendorName, String version) throws FeatureMetaInfoResourceException {
		String methodName = "loadFeatureDefaultTaxonomyAsPermastore";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (featureGroupName != null && featureName != null && implName != null && vendorName != null
				&& version != null) {
			if (!LEAP_DEFAULAT_PERMASTORE_STRING.isEmpty()) {
				com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration permaStoreConfiguration = new com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration();
				permaStoreConfiguration.setIsEnabled(true);
				permaStoreConfiguration.setIsGlobal(true);
				permaStoreConfiguration.setName(LEAP_DEFAULT);
				permaStoreConfiguration.setDataType("Map");
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setType(ConfigurationBuilderType.INLINE);
				InlineBuilder inlineBuilder = new InlineBuilder();
				inlineBuilder.setType("JSON-TO-Map");
				inlineBuilder.setValue(LEAP_DEFAULAT_PERMASTORE_STRING);
				builder.setInlineBuilder(inlineBuilder);
				permaStoreConfiguration.setConfigurationBuilder(builder);
				FeatureInfo featureInfo = new FeatureInfo();
				featureInfo.setFeatureGroup(featureGroupName);
				featureInfo.setFeatureName(featureName);
				permaStoreConfiguration.setFeatureInfo(featureInfo);
				permaStoreConfiguration.setPublishPermaStoreEvent(new PublishPermaStoreEvent());
				permaStoreConfiguration.setSubscribePermaStoreEvents(new SubscribePermaStoreEvents());
				ConfigurationContext configurationContext = new ConfigurationContext(
						FeatureMetaInfoConstant.DEFAULT_TENANT_KEY, FeatureMetaInfoConstant.DEFAULT_SITE_KEY,
						featureGroupName, featureName, implName, vendorName, version);
				IPermaStoreConfigurationService psConfigService = new PermaStoreConfigurationService();
				try {
					boolean isExist = psConfigService.checkPermaStoreConfigarationExistOrNot(configurationContext,
							permaStoreConfiguration.getName());
					if (!isExist) {
						psConfigService.addPermaStoreConfiguration(configurationContext, permaStoreConfiguration);
						LEAP_DEFAULAT_PERMASTORE_STRING = "";
					} else {
						logger.trace(
								"{} Permastore configuration for : {} already exist for featuregroup :{}  and feature :{} in db",
								LEAP_DEFAULT, featureGroupName, featureName);
					}

				} catch (PermaStoreConfigurationException | PermaStoreConfigRequestException e) {
					throw new FeatureMetaInfoResourceException("error in loading the PermastoreConfiguration ", e);

				}

			}
		} else {
			throw new FeatureMetaInfoResourceException("Unable to load LeapDefault_Taxonomy.json ");
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	private void loadLeapEntityResourceInFeatureMetaInfo(List<LeapEntityConfiguration> leapEntityConfigList,
			String featureGroupName, String featureName, String implementationName, String vendorName,
			String vendorVersion) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadLeapEntityResourceInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (LeapEntityConfiguration leapEntity : leapEntityConfigList) {
			String leapEntityResourceName = leapEntity.getResourceName();
			logger.trace("{} leapEntityResourceName :{} ", LEAP_LOG_KEY, leapEntityResourceName);
			ConfigurationContext configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implementationName, vendorName,
					vendorVersion);
			URL leapEntityResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(leapEntityResourceName,
					configurationContext);
			if (leapEntityResourceUrl != null) {
				String leapEntitySourceAsString = convertXmlToString(leapEntityResourceUrl, leapEntityResourceName);
				if (leapEntitySourceAsString != null) {
					LeapEntityConfigXMLParser entityConfigParser = new LeapEntityConfigXMLParser();
					LeapDataServices leapDataServices = null;
					try {
						leapDataServices = entityConfigParser.marshallConfigXMLtoObject(leapEntitySourceAsString);
						List<Entity> entityList = leapDataServices.getEntity();
						for (Entity entity : entityList) {
							String entityName = entity.getName();
							logger.trace("{} entityName ::{}  : IsEnable {}", LEAP_LOG_KEY, entityName,
									entity.isIsEnable());
							ILeapDataServiceConfiguration leapEntityConfigService = new LeapDataServiceConfiguration();

							try {
								boolean isExist = leapEntityConfigService
										.checkEntityConfigarationExistOrNot(configurationContext, entityName);
								if (!isExist) {
									leapEntityConfigService.addEntityConfiguration(configurationContext, entity);

								} else {
									logger.trace(
											"{} Entity configuration for : {} already exist for featuregroup : {}  and feature : {} in db",
											LEAP_LOG_KEY, entityName, featureGroupName, featureName);
								}

							} catch (LeapDataServiceConfigurationException e) {
								throw new FeatureMetaInfoResourceException("error in loading the EntityConfiguration ",
										e);

							}
						} // end of if(builderType.equalsIgnoreCase("CUSTOM"))
					} catch (LeapEntityConfigParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse leapDataService file : " + leapEntityResourceName);
					}
				} // end of for
			} else {
				logger.trace("{} No leapDataService config xml defined for :{} ", LEAP_LOG_KEY, leapEntityResourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to load the the policy configuration in db and cache
	 * 
	 * @param policyConfigList : List<PolicyConfiguration> Object
	 * @param featureGroupName : feature group name
	 * @param featureName      : feature name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void loadPolicyResourceInFeatureMetaInfo(List<PolicyConfiguration> policyConfigList,
			String featureGroupName, String featureName, String implName, String vendorName, String version)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadDataContextResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (PolicyConfiguration policyconfig : policyConfigList) {
			String policyconfigResourceName = policyconfig.getResourceName();
			ConfigurationContext configurationContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implName, vendorName, version);
			URL policyResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(policyconfigResourceName,
					configurationContext);
			if (policyResourceUrl != null) {
				String policyconfigSourceAsString = convertXmlToString(policyResourceUrl,
						policyconfig.getResourceName());
				if (policyconfigSourceAsString != null) {
					PolicyConfigXMLParser policyParser = new PolicyConfigXMLParser();
					try {
						Policies policies = policyParser.marshallConfigXMLtoObject(policyconfigSourceAsString);
						List<Policy> policyList = policies.getPolicy();
						if (!(policyList.isEmpty()) || policyList != null) {
							for (Policy policy : policyList) {
								logger.trace("{} policy related info :{} ", LEAP_LOG_KEY, policy.getPolicyName());
								IPolicyConfigurationService policyConfigService = new PolicyConfigurationService();
								PolicyRequestContext policyRequestContext = new PolicyRequestContext(
										LeapHeaderConstant.tenant, LeapHeaderConstant.site, featureGroupName,
										featureName, implName, vendorName, version);
								try {
									boolean isExist = policyConfigService
											.checkPolicyExistInDbAndCache(configurationContext, policy.getPolicyName());
									if (!isExist) {
										policyConfigService.addPolicyConfiguration(configurationContext, policy);
									} else {
										logger.trace(
												"{} Policy configuration for :{} already exist for featuregroup :{} and feature :{}  in db",
												LEAP_LOG_KEY, policy.getPolicyName(), featureGroupName, featureName);
									}
								} catch (PolicyConfigurationException | PolicyRequestException e) {
									throw new FeatureMetaInfoResourceException(
											"error in loading the policyConfiguration for policy = "
													+ policy.getPolicyName(),
											e);
								}
							} // end of for loop
						}
					} catch (PolicyConfigXMLParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse policy file : " + policyconfig.getResourceName());
					}

				} // end of if(policyconfigSourceAsString !=null)
			} else {
				logger.trace("{} No policy config defined for policy : {}", LEAP_LOG_KEY, policyconfigResourceName);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used to load the the feature configuration in db and cache
	 * 
	 * @param featureImplList  : List<FeatureImplementation> Object
	 * @param featureGroupName : feature group name
	 * @param featureName      : feature Name
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 * @throws PrettyUrlMappingDaoException
	 */
	private void loadFeatureResourceInFeatureMetaInfo(List<FeatureImplementation> featureImplList,
			String featureGroupName, String featureName, String implName, String vendor, String version,
			String provider, String vendorTaxonomyId) throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadDataContextResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		/*
		 * boolean isPrimary=false; if(featureImplList.size()==1){ isPrimary=true; }
		 */
		IServiceHandlerConfigurationService serviceHandlerConfigurationService = new ServiceHandlerConfigurationService();
		for (FeatureImplementation featureImpl : featureImplList) {
			String featureImplResourceName = featureImpl.getResourceName();
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor, version);
			URL featureImplResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(featureImplResourceName,
					configContext);
			if (featureImplResourceUrl != null) {
				String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl,
						featureImpl.getResourceName());
				logger.trace("{} feature as String : {}", LEAP_LOG_KEY, featureImplSourceAsString);
				if (featureImplSourceAsString != null) {
					logger.trace("{} .feature as string is not null", LEAP_LOG_KEY);
					FeatureConfigXMLParser featureparser = new FeatureConfigXMLParser();
					FeaturesServiceInfo feaureServiceInfo = null;
					try {
						feaureServiceInfo = featureparser.marshallConfigXMLtoObject(featureImplSourceAsString);
					} catch (FeatureConfigParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to parse feature file : " + featureImpl.getResourceName());
					}
					// #TODO we have to convert it to a List implementation
					com.attunedlabs.feature.jaxb.Feature feature1 = feaureServiceInfo.getFeatures().getFeature();
					feature1.setImplementationName(implName);
					if (provider == null) {
						provider = "NA";
					} else {
						feature1.setProvider(provider);
					}
					if (vendorTaxonomyId == null) {
						vendorTaxonomyId = "NA";
					} else {
						feature1.setVendorTaxonomyId(vendorTaxonomyId);
					}
					List<Service> service = feature1.getService();

					try {
						prettyService.checkAndLoadeTableDataIntoHC();
					} catch (PrettyUrlMappingServiceException e1) {
						logger.error("{} {}", LEAP_LOG_KEY, e1.getMessage());
						throw new FeatureMetaInfoResourceException(e1.getMessage());
					}
					for (Service singleService : service) {
						GenericRestEndpoint genericRestEndpoint = singleService.getGenericRestEndpoint();
						String prettyuri = genericRestEndpoint.getPrettyuri();
						if (prettyuri != null) {
							logger.debug("{} prettyUri:{} {}", LEAP_LOG_KEY, prettyuri,
									genericRestEndpoint.getUrlMappingScheme());
							PrettyUrlMapping prettyUrlMapping = buildPrettyUrlMappingObject(genericRestEndpoint);
							logger.debug("{} .prettyUrl mapping {}", LEAP_LOG_KEY, prettyUrlMapping);
							addPrettyUrlMappingIntoDBAndCache(prettyUrlMapping);
						}
					}
					logger.trace("{} feature related info : ", LEAP_LOG_KEY, feature1.getFeatureName());
					IFeatureConfigurationService featureConfigService = new FeatureConfigurationService();
					FeatureConfigRequestContext requestContext;
					if (feature1.getProvider() != null) {
						if (feature1.getVendorTaxonomyId() != null) {
							logger.trace("{} inside if-if loadFeatureResourceInFeatureMetaInfo", LEAP_LOG_KEY);
							requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
									LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor, version,
									feature1.getProvider(), feature1.getVendorTaxonomyId());

						} else {
							logger.trace("{} inside if-else loadFeatureResourceInFeatureMetaInfo", LEAP_LOG_KEY);
							requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
									LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor, version,
									feature1.getProvider());
						}
					} else {
						if (feature1.getVendorTaxonomyId() != null) {
							logger.trace("{} inside else-if loadFeatureResourceInFeatureMetaInfo", LEAP_LOG_KEY);
							requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
									LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor, version,
									null, feature1.getVendorTaxonomyId());

						} else {
							logger.trace("{} inside else-else loadFeatureResourceInFeatureMetaInfo", LEAP_LOG_KEY);
							requestContext = new FeatureConfigRequestContext(LeapHeaderConstant.tenant,
									LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor, version);
						}
					}
					logger.trace("{} requestContext inside loadFeatureResourceInFeatureMetaInfo : {}", LEAP_LOG_KEY,
							requestContext);
					ConfigurationContext configurationContext = requestContext.getConfigurationContext();
					logger.trace("{} configurationContext inside loadFeatureResourceInFeatureMetaInfo : {}",
							LEAP_LOG_KEY, configurationContext);
					try {
						boolean isExist = featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
								feature1.getFeatureName());
						if (!isExist) {
							addFeatureInFeatureDeployment(configurationContext);

							featureConfigService.addFeatureConfiguration(configurationContext, feature1);
						} else {
							addFeatureInFeatureDeploymentForCache(configurationContext);
							logger.trace(
									"{} feature configuration for : {} already exist for featuregroup : {} and feature : {}, impl name :{} in db ",
									LEAP_LOG_KEY, feature1.getFeatureName(), featureGroupName, featureName, implName);
						}
						serviceHandlerConfigurationService.cacheHandlersForEachService(configurationContext,
								feaureServiceInfo);
					} catch (FeatureConfigurationException | FeatureConfigRequestException
							| FeatureDeploymentServiceException | ServiceHandlerConfigurationException e) {
						throw new FeatureMetaInfoResourceException(
								"error in loading the feature Configuration for feature = " + feature1.getFeatureName(),
								e);
					}
				} // end of for loop
			} // end of if(featureImplSourceAsString!=null)
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method

	private void addPrettyUrlMappingIntoDBAndCache(PrettyUrlMapping prettyUrlMapping)
			throws FeatureMetaInfoResourceException {
		try {
			prettyService.addPrettyUrlMappingInDBAndCache(prettyUrlMapping);
		} catch (PrettyUrlMappingServiceException e) {
			throw new FeatureMetaInfoResourceException(
					"unable to add pretty url mapping into table  --> " + e.getMessage(), e);
		}
	}

	private PrettyUrlMapping buildPrettyUrlMappingObject(GenericRestEndpoint genericRestEndpoint) {
		PrettyUrlMapping prettyUrlMapping = new PrettyUrlMapping();
		prettyUrlMapping.setTenantId(FeatureMetaInfoConstant.DEFAULT_TENANT_KEY);
		prettyUrlMapping.setSiteId(FeatureMetaInfoConstant.DEFAULT_SITE_KEY);
		prettyUrlMapping.setPrettyString(genericRestEndpoint.getPrettyuri());
		prettyUrlMapping.setActualString(genericRestEndpoint.getUrlMappingScheme());
		return prettyUrlMapping;
	}

	private void addFeatureInFeatureDeployment(ConfigurationContext configurationContext)
			throws FeatureDeploymentServiceException {
		String methodName = "addFeatureInFeatureDeployment";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IFeatureDeployment featureDeployment = new FeatureDeploymentService();
		ILeapI18nSetup localeRegistryService = new LeapI18nSetupImpl();
		ILeapResourceBundleResolver bundleResolver = new LeapResourceBundleResolverImpl();
		boolean isAlreadyDeployed = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		if (isAlreadyDeployed) {
			logger.trace("{} configurationContext in addFeatureInFeatureDeployment :{} ", LEAP_LOG_KEY,
					configurationContext);
			featureDeployment.addFeatureDeployement(configurationContext, true, false, true);
		} else {
			featureDeployment.addFeatureDeployement(configurationContext, true, true, true);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void addFeatureInFeatureDeploymentForCache(ConfigurationContext configurationContext)
			throws FeatureDeploymentServiceException {
		String methodName = "addFeatureInFeatureDeploymentForCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IFeatureDeployment featureDeployment = new FeatureDeploymentService();
		ILeapI18nSetup localeRegistryService = new LeapI18nSetupImpl();
		ILeapResourceBundleResolver bundleResolver = new LeapResourceBundleResolverImpl();
		boolean isAlreadyDeployed = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		if (isAlreadyDeployed) {
			logger.trace("{} configurationContext in addFeatureInFeatureDeployment : {}", LEAP_LOG_KEY,
					configurationContext);
			featureDeployment.CheckAndaddFeatureDeployementInCache(configurationContext, true, false, true);

		} else {
			featureDeployment.CheckAndaddFeatureDeployementInCache(configurationContext, true, true, true);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * this is to load the pipeConfigurationfrom the resources
	 * 
	 * @param listOfPipeConfigs
	 * @param featureGroupName
	 * @param name
	 * @param vendorName
	 * @param vendorVersion
	 * @throws FeatureMetaInfoResourceException
	 * @throws MalformedURLException
	 */
	private void loadPipeConfigResourcesInFeatureMetaInfo(List<PipeConfiguration> listOfPipeConfigs,
			String featureGroupName, String featureName, String implName, String vendorName, String vendorVersion)
			throws FeatureMetaInfoResourceException, MalformedURLException {
		String methodName = "loadDataContextResourcesInFeatureMetaInfo";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		for (PipeConfiguration pipeConfiguration : listOfPipeConfigs) {
			String pipeConfigResourceName = pipeConfiguration.getResourceName();
			logger.trace("{} Loading IntegrationpipeConfiguration with name:{} ", LEAP_LOG_KEY, pipeConfigResourceName);
			ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, featureGroupName, featureName, implName, vendorName, vendorVersion);
			URL pipeConfigResourceUrl = FeatureMetaInfoUtil.checkAndGetResourceUrl(pipeConfigResourceName,
					configContext);
			if (pipeConfigResourceUrl != null) {
				String pipeConfigasSring = convertXmlToString(pipeConfigResourceUrl, pipeConfigResourceName);
				if (pipeConfigasSring != null) {
					IntegrationPipelineConfigXmlParser pipelineConfigXmlParser = new IntegrationPipelineConfigXmlParser();
					IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();

					try {
						IntegrationPipes pipes = pipelineConfigXmlParser.unmarshallConfigXMLtoObject(pipeConfigasSring);
						for (IntegrationPipe pipe : pipes.getIntegrationPipe()) {
							try {
								boolean isExist = pipeLineConfigurationService
										.checkIntegrationPipelineConfigExistOrNot(configContext, pipe.getName());
								if (!isExist) {
									pipeLineConfigurationService.addIntegrationPipelineConfiguration(configContext,
											pipe);
								} else {
									logger.trace(
											"{} pipeline configuration for : {} already exist for featuregroup : {} and feature : {}, impl name : {} in db",
											LEAP_LOG_KEY, pipeConfiguration.getResourceName(), featureGroupName,
											featureName, implName);
								}
							} catch (IntegrationPipelineConfigException e) {
								throw new FeatureMetaInfoResourceException(
										"Unable to load IntegrationPipeConfiguration to the database..", e);
							}
						}
					} catch (IntegrationPipelineConfigParserException e) {
						throw new FeatureMetaInfoResourceException(
								"Unable to add Integrationconfiguration file for feature group :  " + featureGroupName
										+ ", featureName : " + featureName + ", with pipe data : " + configContext,
								e);
					}
				}
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Configures the from and to url paths form UIConfig object
	 * 
	 * @param uiConfig
	 * @param feature
	 * @param featureGroupName
	 * @throws FeatureMetaInfoResourceException
	 */
	// TODO in future release when moved to camel 3 we have to check and understood
	// about the functionality if we
	// need otherwise we will remove
	// this functionality
	private void configureUI(UIConfig uiConfig, Feature feature, String featureGroupName)
			throws FeatureMetaInfoResourceException {

		if (uiConfig != null) {
			String uiConfigXml = "";
			if (uiConfig != null) {
				try {
					uiConfigXml = getUIConfigAsString(uiConfig);
					logger.trace("{} .configureUI(), the UIConfig:{} ", LEAP_LOG_KEY, uiConfigXml);

					String tenantID = LeapHeaderConstant.tenant;
					String siteID = LeapHeaderConstant.site;

					ConfigurationContext configurationContext = new ConfigurationContext(tenantID, siteID,
							featureGroupName, feature.getName(), feature.getImplementationName(),
							feature.getVendorName(), feature.getVendorVersion());

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					InputSource is = new InputSource(new StringReader(uiConfigXml));
					Document doc = builder.parse(is);

					String filePath = "";
					NodeList nList = doc.getElementsByTagName("FormFlow");
					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						logger.trace("{} Current Element :{}", LEAP_LOG_KEY, nNode.getNodeName());
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							logger.trace("{} path :{}", LEAP_LOG_KEY, eElement.getAttribute("path"));
							filePath = eElement.getAttribute("path");
							addAllFilesFromLocalToZNode(configurationContext, path, filePath);
						}
					}
				} catch (JAXBException | IOException | ParserConfigurationException | SAXException e) {
					throw new FeatureMetaInfoResourceException("Unable to configure uiConfig as string ", e);
				}
			}
		}
	}

	/**
	 * method to get the defined UIConfig in featureMetaInfo.xml as string
	 * 
	 * @param uiConfig
	 * @return
	 * @throws JAXBException
	 */
	private String getUIConfigAsString(UIConfig uiConfig) throws JAXBException {

		StringWriter xmlOut = new StringWriter();
		JAXBContext contextObj = JAXBContext.newInstance(UIConfig.class);
		Marshaller formMarshallerObj = contextObj.createMarshaller();
		formMarshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		formMarshallerObj.marshal(uiConfig, xmlOut);

		logger.trace("{} .getUIConfigAsString(), the UIConfig : {}", LEAP_LOG_KEY, xmlOut);

		return xmlOut.toString();
	}

	/**
	 * 
	 * @param configCtx
	 * @param baseDir
	 * @throws FeatureMetaInfoResourceException
	 * @throws UtilityException
	 */
	public void addAllFilesFromLocalToZNode(ConfigurationContext configCtx, String baseDir, String filePath)
			throws FeatureMetaInfoResourceException {
		String methodName = "addAllFilesFromLocalToZNode";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		nullValueChecking(configCtx);
		String dirPath = baseDir + "/" + obtainPath(configCtx);
		logger.trace("{} dirPath : {}", LEAP_LOG_KEY, dirPath);
		try {
			addingAllFilesFromLocal(configCtx, dirPath, filePath);
		} catch (StaticConfigDuplicateNameofFileException | InvalidFilePathException e) {
			e.printStackTrace();
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * adding the files present in local present in directory formed based upon
	 * context object and baseDir.
	 * 
	 * @param configCtx
	 * @param dirPath
	 * @throws FeatureMetaInfoResourceException
	 * @throws StaticConfigDuplicateNameofFileException
	 * @throws InvalidFilePathException
	 */
	private void addingAllFilesFromLocal(ConfigurationContext configCtx, String dirPath, String filepath)
			throws FeatureMetaInfoResourceException, StaticConfigDuplicateNameofFileException,
			InvalidFilePathException {
		String methodName = "addingAllFilesFromLocal";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IStaticConfigurationService instance = null;
		try {
			instance = StaticConfigurationFactory.getFilemanagerInstance();
		} catch (InstantiationException | IllegalAccessException | StaticConfigInitializationException e1) {
			e1.printStackTrace();
		}
		if (instance instanceof ZookeeperFilemanagementServiceImpl) {

			LocalfileUtil localfileUtil = new LocalfileUtil();
			File file = new File(dirPath + "/" + filepath);
			logger.trace("{} file : {}", LEAP_LOG_KEY, file.toString().replaceAll("\\\\", "/"));
			String filePath = null;
			String fileData = null;
			try {
				FileReader freader = new FileReader(file.toString().replaceAll("\\\\", "/"));
				BufferedReader br = new BufferedReader(freader);
				String content;
				StringBuffer buffer = new StringBuffer();
				while ((content = br.readLine()) != null) {
					buffer.append(content);
					fileData = buffer.toString();
					// logger.debug("{} fileData : " + fileData);
				}
				filePath = removeExtraPath(file.getPath());
				logger.trace("{} removed filePath :{} ", LEAP_LOG_KEY, filePath);
				boolean existsInlocal = localfileUtil.checkFileExists(dirPath + "/" + filepath);
				instance.addStaticConfiguration(configCtx, filePath, fileData);
				br.close();
			} catch (NullPointerException | IOException | StaticConfigInitializationException | AddStaticConfigException
					| AccessProtectionException e) {
				throw new FeatureMetaInfoResourceException("cannot find the file in local no such directory present "
						+ file + " or templates folder might be empty", e);
			}
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * removes the extra and unneccesary path from the provided path and takes the
	 * path only above the template also replaces the backslash in the file by
	 * forward slash
	 * 
	 * @param fileName
	 * @return filename
	 */
	private static String removeExtraPath(String fileName) {
		if (fileName.contains("templates")) {
			int tempIndex = (fileName.indexOf("templates"));
			fileName = fileName.substring(tempIndex).trim();
		}
		if (fileName.contains("/")) {
			logger.trace("{} fileName contains before : {} ", LEAP_LOG_KEY, fileName);
			fileName = fileName.replace("/", "\\");
			logger.trace("{} fileName contains after  :{} ", LEAP_LOG_KEY, fileName);
		}
		return fileName;
	}

	/**
	 * obtains the path based upon the cTx provided. returns the same path.
	 * 
	 * @param ctx
	 * @return pathObtained
	 */
	public static String obtainPath(Object ctx) {
		String pathObtained = null;
		if (ctx instanceof ConfigurationContext) {
			ConfigurationContext configCtx = (ConfigurationContext) ctx;
			pathObtained = "/" + configCtx.getTenantId() + "/" + configCtx.getSiteId() + "/"
					+ configCtx.getFeatureGroup() + "/" + configCtx.getFeatureName() + "/"
					+ configCtx.getImplementationName() + "/" + configCtx.getVendorName() + "/"
					+ configCtx.getVersion();
		} else if (ctx instanceof RequestContext) {
			RequestContext requestCtx = (RequestContext) ctx;
			pathObtained = "/" + requestCtx.getTenantId() + "/" + requestCtx.getSiteId() + "/"
					+ requestCtx.getFeatureGroup() + "/" + requestCtx.getFeatureName() + "/"
					+ requestCtx.getImplementationName() + "/" + requestCtx.getVendor() + "/" + requestCtx.getVersion();
		}
		return pathObtained;
	}

	/**
	 * this method checks the null value is present in any field of object. throws
	 * Exception if present
	 * 
	 * @param ctx
	 * @throws UtilityException
	 * @throws FeatureMetaInfoResourceException
	 * @throws NullValueException
	 */
	private static void nullValueChecking(Object ctx) throws FeatureMetaInfoResourceException {
		if (ctx instanceof ConfigurationContext) {
			ConfigurationContext configCtx = (ConfigurationContext) ctx;
			if (configCtx.getTenantId().equals(null) && configCtx.getSiteId().equals(null)
					&& configCtx.getFeatureGroup().equals(null) && configCtx.getFeatureName().equals(null)
					&& configCtx.getImplementationName().equals(null) && configCtx.getVendorName().equals(null)
					&& configCtx.getVersion().equals(null))
				throw new FeatureMetaInfoResourceException(
						"Unable to create the path  because null value found  while creating Nodes ... ");
			else
				return;
		} else if (ctx instanceof RequestContext) {
			RequestContext requestCtx = (RequestContext) ctx;
			if (requestCtx.getTenantId().equals(null) && requestCtx.getSiteId().equals(null)
					&& requestCtx.getFeatureGroup().equals(null) && requestCtx.getFeatureName().equals(null)
					&& requestCtx.getImplementationName().equals(null) && requestCtx.getVendor().equals(null)
					&& requestCtx.getVersion().equals(null))
				throw new FeatureMetaInfoResourceException(
						"Unable to create the path  because null value found  while creating Nodes ... ");
			else
				return;
		}
	}

	private void loadAccountConfigurations() throws FeatureMetaInfoResourceException {
		IAccountRetrieveService accountRetrieveService = new AccountRetrieveServiceImpl();
		try {
			List<AccountConfiguration> configurations = accountRetrieveService.getAllAccountDetailConfiguration();
			AccountConfigurationService service = new AccountConfigurationService();
			service.addAccountConfigurations(configurations);

		} catch (AccountFetchException | ConfigServerInitializationException e) {
			throw new FeatureMetaInfoResourceException(e.getMessage(), e.getCause());
		}

	}

}
/*
 * for (FeatureImplementation featureImpl : featureImplList) { String
 * featureImplResourceName = featureImpl.getResourceName(); URL
 * featureImplResourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader()
 * .getResource(featureImplResourceName); if (featureImplResourceUrl != null) {
 * String featureImplSourceAsString = convertXmlToString(featureImplResourceUrl,
 * featureImpl.getResourceName()); logger.debug("{} feature as String : " +
 * featureImplSourceAsString); if (featureImplSourceAsString != null) {
 * logger.debug("{} .feature as string is not null"); FeatureConfigXMLParser
 * featureparser = new FeatureConfigXMLParser(); FeaturesServiceInfo
 * feaureServiceInfo = null; try { feaureServiceInfo =
 * featureparser.marshallConfigXMLtoObject(featureImplSourceAsString); } catch
 * (FeatureConfigParserException e) { throw new
 * FeatureMetaInfoResourceException( "Unable to parse feature file : " +
 * featureImpl.getResourceName()); } // #TODO we have to convert it to a List
 * implementation com.attunedlabs.feature.jaxb.Feature feature1 =
 * feaureServiceInfo.getFeatures().getFeature();
 * feature1.setImplementationName(implName); logger.debug(
 * "feature related info : " + feature1.getFeatureName());
 * IFeatureConfigurationService featureConfigService = new
 * FeatureConfigurationService(); FeatureConfigRequestContext requestContext =
 * new FeatureConfigRequestContext( LeapHeaderConstant.tenant,
 * LeapHeaderConstant.site, featureGroupName, featureName, implName, vendor,
 * version); ConfigurationContext configurationContext =
 * requestContext.getConfigurationContext(); try { boolean isExist =
 * featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
 * feature1.getFeatureName()); if (!isExist) {
 * addFeatureInFeatureDeployment(configurationContext);
 * featureConfigService.addFeatureConfiguration(configurationContext, feature1);
 * } else { logger.debug("{} feature configuration for : " +
 * feature1.getFeatureName() + "already exist for featuregroup : " +
 * featureGroupName + " and feature : " + featureName + ", impl name : " +
 * implName + " in db"); } } catch (FeatureConfigurationException |
 * FeatureConfigRequestException | FeatureDeploymentServiceException e) { throw
 * new FeatureMetaInfoResourceException(
 * "error in loading the feature Configuration for feature = " +
 * feature1.getFeatureName(), e); } } // end of for loop } // end of
 * if(featureImplSourceAsString!=null) }// ..end of the method
 */

/*
 * try { boolean isExist =
 * featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
 * feature1.getFeatureName()); if (!isExist) {
 * addFeatureInFeatureDeployment(configurationContext);
 * featureConfigService.addFeatureConfiguration(configurationContext, feature1);
 * } else { logger.debug("{} feature configuration for : " +
 * feature1.getFeatureName() + "already exist for featuregroup : " +
 * featureGroupName + " and feature : " + featureName + ", impl name : " +
 * implName + " in db"); } } catch (FeatureConfigurationException |
 * FeatureConfigRequestException | FeatureDeploymentServiceException e) { throw
 * new FeatureMetaInfoResourceException(
 * "error in loading the feature Configuration for feature = " +
 * feature1.getFeatureName(), e); }
 */