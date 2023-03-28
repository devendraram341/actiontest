package com.attunedlabs;

/**
 * This class is for Generic Test Constant used in all test class
 * 
 *
 */
public class FeatureInstallerTestConstant {

	public static final String PROFILE_ID = "profileId";
	public static final String LOCAL = "local";

	public static final String TENANT = "tenant";
	public static final String SITE = "site";
	public static final String IMPL = "implementation";
	public static final String VENDOR = "vendor";

	public static final String TEST_TENANT = "all";
	public static final String TEST_SITE = "all";
	public static final String TEST_FEATUREGROUP = "TestFeatureGroup";
	public static final String TEST_FEATURE = "TestFeature";
	public static final String TEST_IMPL = "TestImpl";
	public static final String TEST_VENDOR = "TestVendor";
	public static final String TEST_VERSION = "1.0";

	public static int TEST_SITE_NODEID;
	public static int TEST_FEATUREGROUP_NODEID;
	public static int TEST_FEATURE_NODEID;
	public static int TEST_IMPL_NODEID;
	public static int TEST_VENDOR_NODEID;

	public static final String TEST_SERVICE = "TestService";
	public static final String LEAP_DATA_CONTEXT = "leapDataContext";
	public static final String TEST_PROVIDER = "TestProvider";

	public static final String ACCOUNTID = "accountId";
	public static final String TENANTID = "tenantId";
	public static final String SITEID = "siteId";
	public static final String FEATURE_GROUP = "feature_group";
	public static final String FEATUREGROUP = "featuregroup";
	public static final String FEATURE = "feature";
	public static final String SERVICENAME = "serviceName";
	public static final String IMPLEMENTATION_NAME = "implName";
	public static final String VERSION = "version";
	public static final String PROVIDER = "provider";

	public static final String FEATURE_META_INFO = "featureMetaInfo.xml";
	public static final String BAD_FEATURE_META_INFO = "badFeatureMetaInfo.xml";
	
	public static final String PERMASTORE="PermaStore-PermaStoreService-permastoreconfig.xml";
	public static final String SERVICE_HANDLER="servicehandler-impl.xml";
	public static final String EVENT="TestFeatureGroup-TestFeature-eventframework.xml";
	public static final String FEATURE_DATA_CONTEXT="TestFeatureGroup-TestFeature-featureDataContext.xml";
	public static final String FEATURE_SERVICE="TestFeatureGroup-TestFeature-TestImpl-TestVendor-1.0-featureservice.xml";
	
	public static String permastoreConfigData;
	public static String serviceHandlerConfigData;
	public static String eventsConfigData;
	public static String dispatchChannelConfigData;
	public static String systemEventConfigData;
	public static String featureDataContextConfigData;
	public static String featureServiceConfigData;
	public static String jmsEventSubscriptionConfigData;
}
