package com.attunedlabs.config.util;


public class LeapDefaultConstants {

	// Database Default Value Constants
	public static final String DEFAULT_DB_URL = "jdbc:hsqldb:hsql://localhost/leap";
	public static final String DEFAULT_DB_DRIVER_CLASS = "org.hsqldb.jdbcDriver";
	public static final String DEFAULT_DB_USER = "sa";
	public static final String DEFAULT_DB_PASSWORD = "";

	// Kafka Default Value Constants
	public static final String DEFAULT_ZOOKEEPER_CONNECT = "localhost:2181";
	public static final String DEFAULT_ACKS = "all";
	public static final String DEFAULT_RETRIES = "300";
	public static final String DEFAULT_BATCH_SIZE = "16384";
	public static final String DEFAULT_LINGER_MS = "2";
	public static final String DEFAULT_BUFFER_MEMORY = "33554432000";
	public static final String DEFAULT_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	public static final String DEFAULT_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer ";
	public static final String DEFAULT_PRODUCER_TYPE = "sync";
	public static final String DEFAULT_BUFFER_SIZE = "102400";
	public static final String DEFAULT_RECONNECT_INTERVAL = "30000";
	public static final String DEFAULT_REQUEST_REQUIRED_ACKS = "1";
	public static final String DEFAULT_REBALANCE_RETRIES_MAX = "60";
	public static final String DEFAULT_MIRROR_CONSUMER_NUMTHREADS = "1";
	public static final String DEFAULT_METADATA_MAX_AGE_MS = "300000000";
	public static final String DEFAULT_SECURITY_PROTOCOL = null;
	public static final String DEFAULT_SSL_TRUSTSTORE_LOCATION = null;
	public static final String DEFAULT_SSL_TRUSTSTORE_PASSWORD = null;


	// Retry Policy Conastants
	public static final String DEFAULT_FAILED_RETRY_INTERVAL_KEY = "1";
	public static final String DEFAULT_FAILED_RETRY_INTERVAL_MULTIPLIER_KEY = "2";
	public static final String DEFAULT_FAILED_MAXIMUM_RETRY_COUNT_KEY = "5";
	public static final String DEFAULT_FAILED_MAXIMUM_RETRY_INTERVAL_KEY = "200";
	public static final String DEFAULT_FAILEDTIMEINTERVALUNIT = "minutes";
	public static final String DEFAULT_NORMAL_RETRY_COUNT_KEY = "2";
	public static final String DEFAULT_NORMAL_RETRY_INTERVAL_KEY = "1";
	public static final String DEFAULT_NORMAL_TIME_INTERVAL_UNIT_KEY = "minutes";
	public static final String DEFAULT_TOP_MAX_RETRY_RECORDS_COUNT_KEY = "15";
	public static final String DEFAULT_DEFAULT_RETRY_COUNT = "3";

	// Cassandra Default Value Constants
	public static final String DEFAULT_DRIVER_CLASS_KEY = "com.github.adejanovski.cassandra.jdbc.CassandraDriver";
	public static final String DEFAULT_URL_KEY = "jdbc:cassandra://192.168.1.150:9042/observationdb";
	public static final String DEFAULT_PORT_KEY = "9042";
	public static final String DEFAULT_HOST_KEY = "192.168.1.150";
	public static final String DEFAULT_KEYSPACE_KEY = "observationdb";

	// Global Tenant and site Default Value Constants
	public static final String DEFAULT_TENANT_KEY = "all";
	public static final String DEFAULT_SITE_KEY = "all";

	// Logging key Default Value Constant
	public static final String DEFAULT_LEAP_LOGGING_KEY = "LEAP_SYSTEM_LOG";
	public static final String DEFAULT_ISPERFORMANCELOGGINGENABLED = "false";

	// OSGIENV Default Value Constant
	public static final String DEFAULT_OSGIENV_ISENABLE_KEY = "true";

	// ZOOKEEPER Default Value Constant
	public static final String DEFAULT_ZOOKEEPER_HOST_KEY = "localhost:2181";
	public static final String DEFAULT_ZOOKEEPER_TIMEOUT_KEY = "5000";

	// Root Deployable Feature Default Value Constant
	public static final String DEFAULT_ROOT_DEPLOYABLE_FEATURES_KEY = "authorization-authorizationservice,elastic-ui,scheduler-schedulerManagement,parcel-parcelservice";

	// Authorization Caching Default Value Constant
	public static final String DEFAULT_IS_AUTHZ_ENABLED = "false";

	// Authentication Parameters  Default Value Constant
	public static final String DEFAULT_IS_TENANT_TOKEN_VALID = "false";
	public static final String DEFAULT_IS_ACCESS_TOKEN_VALID = "false";
	public static final String DEFAULT_DEPLOYMENT_ENVIRONMENT_KEY = "Local";
	public static final String DEFAULT_TENANT_TOKEN_SKIP_SERVICES = "[authentication-authenticationservice-login,authentication-authenticationservice-getDefaultAppAcessToken,authentication-authenticationservice-loginWithCookie,authentication-authenticationservice-encryptlogin,authentication-authenticationservice-encryptUserDataInfo]";
	public static final String DEFAULT_ACCESS_TOKEN_SKIP_SERVICES = "[authentication-authenticationservice-login,authentication-authenticationservice-getDefaultAppAcessToken,authentication-authenticationservice-loginWithCookie,authentication-authenticationservice-encryptlogin,authentication-authenticationservice-encryptUserDataInfo]";
	public static final String DEFAULT_OAUTH_VALIDATOR_SERVIE_URL = "https://localhost:9443/services/OAuth2TokenValidationService";
	
	
	
	public static final String DEFAULT_JMS_INVOCATION = "false";
	public static final String DEFAULT_STATICCONFIG_DIC_KEY = "D:/POC/xslt";
	public static final String DEFAULT_STATIC_FILECONFIG_IMPL_KEY = "com.attunedlabs.staticconfig";
	public static final String DEFAULT_ERASE_OLD_CONFIGURATION_PROPERTY = "true";

}
