package com.attunedlabs;

/**
 * This class is for Generic Test Constant used in all test class
 * 
 *
 */
public class LeapCoreTestConstant {

	public static final String PROFILE_ID = "profileId";
	public static final String USER = "user.dir";
	public static final String CONFIG = "config";
	public static final String LOCAL = "local";

	public static final String TENANT = "tenant";
	public static final String SITE = "site";
	public static final String IMPL = "implementation";
	public static final String VENDOR = "vendor";

	public static final String TEST_ACCOUNT="ALL";
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
	public static final String FEATURE_GROUP = "featureGroup";
	public static final String FEATUREGROUP = "featuregroup";
	public static final String FEATURE = "feature";
	public static final String SERVICENAME = "serviceName";
	public static final String IMPLEMENTATION_NAME = "implName";
	public static final String VERSION = "version";
	public static final String PROVIDER = "provider";

	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final String REQUEST_METHOD = "requestMethod";
	public static final String IS_SERVICE_COMPLETED = "isServiceCompleted";
	public static final String CAMEL_HTTP_METHOD = "CamelHttpMethod";
	public static final String CAMEL_RESPONSE_CODE = "CamelHttpResponseCode";
	public static final String CAMEL_HTTP_QUERT = "CamelHttpQuery";
	public static final String HTTP_POST = "POST";
	public static final String HTTP_GET = "GET";
	public static final String HTTP_PUT = "PUT";
	public static final String TAG_NAME_LEAP_INITIAL = "#leap_initial";
	public static final String TAG_NAME = "TestTag";
	public static final String TAG_NAME_HEADER = "#header";
	public static final String CONTENT_TYPE = "Content-Type";

	public static final String ACCESS_TOKEN_VALIDATION = "isAccessTokenValidationEnabled";
	public static final String TENANT_TOKEN_VALIDATION = "isTenantTokenValidationEnabled";
	public static final String TENANT_TOKEN_EXPIRATION = "tenant_token_expiration";
	public static final Long TENANT_TOKEN_EXPIRATION_DATA = 165762527454L;
	public static final String TENANT_TOKEN = "tenant_token";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String ACCESS_TOKEN_DATA = "TestingToken";
	public static final String LEAP_AUTH_TOKEN = "leap_auth_token";
	public static final String CAMEL_RESTLET_REQUEST = "CamelRestletRequest";
	public static final String CAMEL_RESTLET_RESPONSE = "CamelRestletResponse";

	public static final String CONTENT_TYPE_LEAP = "application/vnd.leap+json";
	public static final String CONTENT_TYPE_JSON = "application/json";

	public static final String BASE_CONFIG_PATH = "base_config_path";
	public static final String BASEROUTE_XML_NAME = "baseroute.xml";
	public static final String ACTUAL_URL = TEST_FEATUREGROUP + "/" + TEST_FEATURE + "/" + TEST_SERVICE;
	public static final String PERMSTORE_CONFIG_BEAN = "bean:leapConfigUtil?method=storePermastoreConfigurationInServiceContext('LeapDefault_Taxonomy.json')";

	public static final String DEMO_DATA = "Testing";
	public static final String DEMO_JSON_DATA = "{\"test\":{\"data\":\"demo\"}}";
	public static final String DEMO_LEAP_DATA = "{\"apiVersion\":\"1.0\",\"context\":\"contextString\",\"lang\":\"en\",\"data\":{\"test\":{\"data\":\"testing\"}}}";
	public static final String DEMO_XML_DATA = "<Demo>Testing</Demo>";
	public static final String DEMO_REST_GET_DATA = "{\"data\":[{\"demo\":\"testing\",\"demo1\":\"testing1\"}]}";
	public static final String DEMO_CAMEL_HTTP_QUERY_DATA = "demo=testing&demo1=testing1";

	public static final String ALL_DIRECT_URI = "direct:*";
	public static final String ALL_SEDA_URI = "seda:*";
	public static final String MOCK_FINISH = "mock:finish";
	public static final String REST_CALL_JSON_OPTIONS = "rest-call-json-options";
	public static final String REST_CALL_JSON = "rest-call-json";
	public static final String REST_CALL_OPTIONS = "rest-call-options";
	public static final String REST_CALL_XML_OPTIONS = "rest-call-xml-options";
	public static final String REST_CALL_XML = "rest-call-xml";
	public static final String REST_GET_POST = "rest-get-post";
	public static final String REST_PRETTY_URI = "rest-prettyUri";
	public static final String SUBSCRIPTION_EXECUTION = "subscriber-execution-route";
	public static final String REST_OPTIONS = "rest-options";
	public static final String EXIT_ROUTE = "exitRoute";
	public static final String IDENTITY_SERVICE = "identity-service";
	public static final String SUBSCRIBER_TRANSACTION = "subscriber-transaction-route";
	public static final String LEAP_GET_POST = "leap-get-post";
	public static final String BASE_TRANSACTION = "base-transaction";
	public static final String TENANT_TOKEN_VALIDATION_ROUTE = "tenant-token-validation-route";
	public static final String ACCESS_TOKEN_VALIDATION_ROUTE = "access-token-validation-route";
	public static final String ENTRY_ROUTE = "entryRoute";
	public static final String LEAP_RESOURCE_RETENTION = "leapResourceRetentionRoute";
	public static final String LEAP_EVENT_DISPATCHER_SERVICE = "leapEventDispatcherServiceRoute";
	public static final String BASE_ROUTE = "baseRoute";
	public static final String RESOURCE_CLOSING_AND_DISPATCHER = "resourceClosingAndDispatcherRoute";
	public static final String JMS_REQUEST_RESPONSE_QUEUE = "JMSRequestResponseQueue";

	public static final String PROPERTIES_FILE = "BaseRoute/BaseRoute.properties";
	
	public static final String JMS_RETRY_ROUTE="JMSRetryRoute";
	public static final String RETRY_ROUTE="RetryRoute";
	public static final String PRETTY_URI_TEST = "prettyUriTest";
	public static final String SUBSCRIPTION_ID="subscriptionId";

	public static final String EXECUTION_ROUTE = "exeroute";
	public static final String PIPELINE_SERVICE_ROUTE = "pipelineServiceRoute";

	public static final String TEST_TAXONOMY = "testTaxonomy";
	public static final String LEAP_DEFAULT = "LeapDefault";
	public static final String TAXONOMY_ID = "taxonomyId";
}
