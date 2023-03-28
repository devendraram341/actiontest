package com.leap.authentication;

public class AuthNConstants {

	/** Local properties name **/
	public static final String SSO_CONFIG_PROPS = "ssoconfig.properties";
	/** Default value namespace for SAML2SSO-OAuth2 with WSO2 **/
	public static final String WSO2_CONFIG_PROPS = "basewso2-config.properties";
	public static final String CONFIG_PROPS = "ldap-config.properties";
	public static final String SAML_OAuth2ClientID = "SAML.OAuth2ClientID";
	public static final String SAML_OAuth2ClientSecret = "SAML.OAuth2ClientSecret";
	public static final String EnableSAMLSSOLogin = "EnableSAMLSSOLogin";
	public static final String EnableOpenIDLogin = "EnableOpenIDLogin";
	public static final String EnableSAML2Grant = "EnableSAML2Grant";
	public static final String LoginUrl = "LoginUrl";
	public static final String SAMLSSOUrl = "SAMLSSOUrl";
	public static final String SAML2GrantUrl = "SAML2GrantUrl";
	public static final String SAML_IssuerID = "SAML.IssuerID";
	public static final String SAML_ConsumerUrl = "SAML.ConsumerUrl";
	public static final String KeyStore = "KeyStore";
	public static final String SAML_IdPUrl = "SAML.IdPUrl";
	public static final String SSOAgentSessionBeanName = "SSOAgentSessionBeanName";
	public static final String SAML_EnableSLO = "SAML.EnableSLO";
	public static final String SAML_LogoutUrl = "SAML.LogoutUrl";
	public static final String SAML_EnableResponseSigning = "SAML.EnableResponseSigning";
	public static final String SAML_EnableAssertionSigning = "SAML.EnableAssertionSigning";
	public static final String SAML_EnableAssertionEncryption = "SAML.EnableAssertionEncryption";
	public static final String SAML_EnableRequestSigning = "SAML.EnableRequestSigning";
	public static final String SAML_EnableForceAuthentication = "SAML.EnableForceAuthentication";
	public static final String SAML_SSOAgentCredentialImplClass = "SAML.SSOAgentCredentialImplClass";
	public static final String KeyStorePassword = "KeyStorePassword";
	public static final String SAML_IdPCertAlias = "SAML.IdPCertAlias";
	public static final String SAML_PrivateKeyAlias = "SAML.PrivateKeyAlias";
	public static final String SAML_PrivateKeyPassword = "SAML.PrivateKeyPassword";
	public static final String SAML_OAuth2TokenEndpoint = "SAML.OAuth2TokenEndpoint";

	public static final String SAML_DESTINATION = "https://localhost:9443/samlsso";
	public static final String ISSUER_URN = "urn:oasis:names:tc:SAML:2.0:assertion";
	public static final String ISSUER = "Issuer";
	public static final String SAML_ELEMENT = "samlp";
	public static final String ISSUER_PROTOCOL_URN = "urn:oasis:names:tc:SAML:2.0:protocol";
	public static final String AUTHN_REQ = "AuthnRequest";
	public static final String AUTH_REQ_PROTOCOL_BIND_URN = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";

	public static final String REQUEST_URL = "requestURL";
	public static final String AUTHERIZATION = "Authorization";

	// Oauth costants

	public static final String OAUTH_ADMIN_SERVICE_URL = "OAuthAdminService";
	public static final String GET_OAUTH_APP_DATA_BY_APP_NAME = "getOAuthApplicationDataByAppName";
	public static final String APP_NAME = "appName";
	public static final String CERTIFICATE_FILE_NAME = "CertificateFileName";
	public static final String DEFAULT_CERTIFICATE_FILE_NAME = "wso2carbon.jks";
	public static final String TRUST_STORE_PASSWORD = "trustStorePassword";
	public static final String SERVER_URL = "identityServerUrl";
	public static final String SERVER_USER_NAME = "identityServerUsername";
	public static final String SERVER_PASSWORD = "identityServerPassword";
	public static final String OAUTH_APP_NAME = "oauthAppName";

	public static final String PART_REMOTE_USERREPO = "services/RemoteUserStoreManagerService?wsdl";
	public static final String PART_USER_IDENTITYREPO = "services/UserIdentityManagementAdminService?wsdl";
	public static final String PART_USER_RECOVERYREPO = "services/UserInformationRecoveryService?wsdl";
	public static final String PART_REMOTE_TENANTREPO = "services/TenantMgtAdminService?wsdl";
	public static final String PART_USER_INFO_REPO = "services/UserInformationRecoveryService?wsdl";
	public static final String PART_REMOTE_TENANT_MANAGERREPO = "services/RemoteTenantManagerService?wsdl";

	public static final String OACC_PROP_FILE = "oacc-service-url.properties";
	public static final String USERS = "users";
	public static final String DOMAIN = "domain";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String TITLE = "title";
	public static final String PASSWORD = "password";
	public static final String ROLE_LIST = "Roles";
	public static final String TENANT_ID = "tenantId";
	public static final String SITE_ID = "siteId";
	public static final String ACCOUNT_ID = "accountId";
	public static final String USER_CLAIM = "userClaim";
	public static final String COUNTRY = "country";
	public static final String EMAIL_ADDRESS = "emailaddress";
	public static final String MOBILE = "mobile";
	public static final String ORGANIZATION = "organization";
	public static final String STREETADDRESS = "streetaddress";
	public static final String TELEPHONE = "telephone";
	public static final String IS_ACTIVE = "isactive";
	public static final String USER_NAME = "username";
	public static final String OLD_PASSWORD = "oldpassword";
	public static final String NEW_PASSWORD = "newpassword";
	public static final String INVALID_JSON_FORMAT = "Invalid JSON Format";
	public static final String INVALID_JSON_ERR_CODE = "400";
	public static final String ERR_MESSAGE_KEY = "Error Message";
	public static final String ERR_CODE_KEY = "Error Code";
	public static final String INVALID_REQUEST_KEY = "Invalid Request";
	public static final String INVALID_REQUEST_ERR_KEY = "400";
	public static final String COMPANY = "company";
	public static final String COMAPNY_NAME = "name";
	public static final String ADDRESS = "address";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String POSTAL_CODE = "postalcode";
	public static final String REGION = "region";
	public static final String CONTACT_NUMBER = "contactnumber";
	public static final String RESOURCE = "Resource";
	public static final String RESOURCE_TYPE = "ResourceType";
	public static final String CREATE_TENANT_URL = "createTenantUrl";
	public static final String CREATE_RESOURCE_CLASS_URL = "createResourceClassUrl";
	public static final String REGISTER_URSER_URL = "registerUsersUrl";
	public static final String CREATE_RESOURCE_URL = "createResourceUrl";
	public static final String REGISTER_ROLES_URL = "registerRolesUrl";
	public static final String ROLES = "Roles";
	public static final String TENANT = "Tenant";
	public static final String USER = "User";
	public static final String EMAIL = "email";

	// ...ValidationConstants
	public static final String IS_AUTH = "isAuthenticated";
	public static final String SKIP_SERVICES = "skipServices";

	public static final String VALIDATION_REQUEST_DTO = "validationReqDTO";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String TOKEN_TYPE = "tokenType";
	public static final String IDENTIFIER = "identifier";
	public static final String TOKEN_TYPE_VALUE = "bearer";
	public static final String OAUTH_VALIDATOR_SERVIE_URL = "oauthValidatorServiceURL";
	public static final String XSD_NAMESPACE1 = "xmlns:xsd";
	public static final String XSD_NAMESPACE2 = "xmlns:xsd1";
	public static final String TRUE_KEY = "true";
	public static final String FALSE_KEY = "false";

	public static final int INT_SRVR_CODE = 500;
	public static final String DEFAULT_ROLE = "Internal/everyone";

	public static final int BAD_REQ_CODE = 400;
	public static final String MSG_KEY = "message";
	public static final String SUCCESS = "success";
	public static final String CONTENT_TYPE = "Content-type";
	public static final String APP_JSON = "application/json";
	public static final String EXPIERS_IN = "expires_in";
	public static final String LOCALITY = "locality";
	public static final String CHARSET_UTF_8 = "UTF-8";
	public static final String BASE_URL = "baseurl";
	public static final String TRUST_STORE_KEY = "javax.net.ssl.trustStore";
	public static final String TRUST_STORE_PASS_KEY = "javax.net.ssl.trustStorePassword";
	public static final String IS_LOCKED = "islocked";
	public static final String AT_SIGN = "@";
	public static final String ADDERSS_1 = "address1";
	public static final String ADMIN = "admin";
	public static final String UID = "uid";
	public static final String GIVEN_NAME = "givenName";
	public static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
	public static final String POSTAL_ADDRESS = "postalAddress";
	public static final String CREATED_DATE = "createdDate";
	public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
	public static final String STREET = "street";
	public static final int PASSWORD_MIN_LENGTH = 5;
	public static final String DEFAULT_APP_USER = "DEFAULT_APP_NOT_REGISTERED";
	public static final String CHARACTERS_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";

	public static final String JWT_TYPE_KEY = "type";
	public static final String JWT_TYPE_ALGO = "algo";
	public static final String JWT_TYPE_VALUE = "jwt";
	public static final String JWT_TYPE_ALGO_VALUE = "HS256";
	public static final String AUTH_DATA_KEY = "authData";
	public static final String USER_DATA_KEY = "userData";
	public static final String TENANT_SITE_INFO_KEY = "tenantSiteInfo";
	public static final String JWT_TOKEN = "leap_auth_token";

	public static final String ENV_CONFIG_FOLDER = "config";
	// public static final String ENV_VARIABLE = "LEAP_HOME";
	public static final String BASE_CONFIG_PATH = "base_config_path";
	public static final String RUNTIME_CONFIG_FILES = "runtimeConfigFiles";

	public static final String SITE = "site";
}
