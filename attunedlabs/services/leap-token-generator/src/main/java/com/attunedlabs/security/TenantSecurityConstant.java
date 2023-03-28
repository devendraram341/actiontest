package com.attunedlabs.security;

public class TenantSecurityConstant {

	private TenantSecurityConstant() {
	}

	public static final String DB_CONFIG = "globalAppDeploymentConfig.properties";
	public static final String DRIVER_CLASS = "DB_DRIVER_CLASS";
	public static final String DB_URL = "DB_URL";
	public static final String DB_USER = "DB_USER";
	public static final String DB_PASS = "DB_PASSWORD";

	public static final String SEC_ALG = "SHA1PRNG";
	public static final String SEC_DIGEST = "MD5";
	public static final int DEFAULT_INTERVAL = 1200;

	public static final String ACCOUNT_ID = "accountId";
	public static final String TENANT_ID = "tenantId";
	public static final String SITE_ID = "siteId";
	public static final String TENANT_TOKEN = "tenant_token";
	public static final String TENANT_TOKEN_EXPIRATION_TIME = "tenant_token_expiration";
	public static final String TENANT_TOKEN_LOGIN = "tenant_token_login";
	public static final String TENANT_TOKEN_EXPIRATION_TIME_LOGIN = "tenant_token_expiration_login";
	public static final String PARTITION_KEY = "partitionKey";

	public static final String TABLE_CUSTOMER_SITE = "customersite";
	public static final String TABLE_CUSTOMER_ACCOUNT = "customeraccount";
	public static final String ACCOUNT_NAME = "accountName";
	public static final String INTERNAL_TENANT = "internalTenantId";

	public static final String TENANT_TOKEN_EXPIRATION = "tenantTokenExpiration";
	public static final String SALT_SECRET_KEY = "saltSecretKey";
	public static final String DOMAIN = "domain";
	public static final String USER_NAME = "username";
	public static final String DOMAIN_ID = "domainId";
	public static final String ADMIN_USERNAME = "adminusername";
	public static final String ADMIN_PASSWORD = "adminpassword";
	public static final String DESCRIPTION = "description";
	public static final String EXTERNAL_ACCOUNT_CONFIG = "externalAccountConfig";
	public static final String INTERNAL_ACCOUNT_CONFIG = "internalAccountConfig";
	public static final String FEATURE_SEPERATOR = "-";
	
	
	// Cofiguration files path
	public static final String ENV_CONFIG_FOLDER = "config";
	public static final String ENV_VARIABLE = "LEAP_HOME";
	public static final String RUNTIME_CONFIG_FILES = "runtimeConfigFiles";
	public static final String APPS_DEPLOYMENT_ENV_CONFIG = "globalAppDeploymentConfig.properties";
	
	public static final String PROFILE_ID_PROPERTY = "profileId";
	public static final String BASE_CONFIG_PATH = "base_config_path";
}
