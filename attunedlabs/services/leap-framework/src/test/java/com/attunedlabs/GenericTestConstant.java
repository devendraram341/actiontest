package com.attunedlabs;

/**
 * This class is for Generic Test Constant used in all test class
 * 
 * @author bizruntime
 *
 */
public class GenericTestConstant {
	
	public static final String PROFILE_ID="profileId";
	public static final String LOCAL="local";

	public static final String TENANT = "tenant";
	public static final String SITE = "site";
	public static final String FEATURE_GROUP = "feature_group";
	public static final String FEATURE = "feature";
	public static final String IMPL = "implementation";
	public static final String VENDOR = "vendor";

	public static final String TEST_TENANT = "TestTenant";
	public static final String TEST_SITE = "TestSite";
	public static final String TEST_FEATUREGROUP = "TestFeatureGroup";
	public static final String TEST_FEATURE = "TestFeature";
	public static final String TEST_IMPL = "TestImpl";
	public static final String TEST_VENDOR = "TestVendor";
	public static final String TEST_VERSION = "1.0";
	public static final String TEST_SERVICE="TestService";

	public static Integer TEST_TENANT_NODEID;
	public static Integer TEST_SITE_NODEID;
	public static Integer TEST_FEATUREGROUP_NODEID;
	public static Integer TEST_FEATURE_NODEID ;
	public static Integer TEST_IMPL_NODEID;
	public static Integer TEST_VENDOR_NODEID;

	public static final int TEST_TENANT_LEVEL = 1;
	public static final int TEST_SITE_LEVEL = 2;
	public static final int TEST_FEATURE_GROUP_LEVEL = 3;
	public static final int TEST_FEATURE_LEVEL = 4;
	public static final int TEST_IMPL_LEVEL = 5;
	public static final int TEST_VENDOR_LEVEL = 6;

	public static final String TEST_CONFIG_NAME = "Demo For Testing";
	public static final String TEST_CONFIG_TYPE = "Permastore";
	public static final String TEST_UPDATE_CONFIG_DATA = "<Demo> Updated </Demo>";
	public static final String TEST_CONFIG_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	public static final int UPDATE_PRIMARY_FEATURE_ID = 1;

	public static final Integer TEST_PARENT_NODEID = 0;
	public static final String LEAP_DUMP="leapdump.sql";

	public static final String TEST_REQUEST_ID = "123456789";

	public static final String SERVICE_NAME = "TestService";
	public static final String TEST_JSON = "{\"demo\":\"test\"}";

	public static final String EVENT_DATA = "{\"metadata\":{\"accountId\":\"ALL\",\"implementationName\":\"TestImpl\",\"featureName\":\"TestFeature\",\"featureGroup\":\"TestFeatureGroup\",\"requestId\":\"123456789\",\"siteId\":\"all\"}}";

	public static final int PORT=5673;
}
