package com.attunedlabs.leap.identityservice;

public class IdentityServiceConstant {
	public static final String IS_TENANT_TOKEN_VALID = "isTenantTokenValidationEnabled";
	public static final String IS_ACCESS_TOKEN_VALID = "isAccessTokenValidationEnabled";
	public static final String AUTHERIZATION = "Authorization";
	public static final String TENANT_TOKEN_SKIP_SERVICES = "tenantTokenSkipServices";
	public static final String ACCESS_TOKEN_SKIP_SERVICES = "accessTokenSkipServices";
	public static final String IS_AUTHZ_ENABLED = "authorization";
	public static final String VALIDATION_REQUEST_DTO = "validationReqDTO";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String TOKEN_TYPE = "tokenType";
	public static final String IDENTIFIER = "identifier";
	public static final String TOKEN_TYPE_VALUE = "bearer";
	// public static final String OAUTH_VALIDATOR_SERVIE_URL =
	// "https://52.201.18.79:9443/services/OAuth2TokenValidationService";
	public static final String OAUTH_VALIDATOR_SERVIE_URL = "oauthValidatorServiceURL";
	public static final String XSD_NAMESPACE1 = "xmlns:xsd";
	public static final String XSD_NAMESPACE2 = "xmlns:xsd1";
	public static final String TRUE_KEY = "true";
	public static final String FALSE_KEY = "false";
	public static final String XML_VALID_TAG = "ax2395:valid";
	public static final String LOGIN_SERVICE = "login";
	public static final String DEFAULT_APP_ACCESS_TOKEN_SERVICE = "getDefaultAppAcessToken";
	public static final String LOGIN_WITH_COOKIE_SERVICE = "loginWithCookie";

	// constants related to the JWT Cookie
	public static final String RES_COOKIE_HEADER_VALUE = "jwtTokenlogin";
	public static final String REQ_COOKIE_HEADER_KEY = "leap_auth_token";
	public static final String JWT_HEADER_DATA_KEY = "headerData";
	public static final String JWT_PAYLOAD_DATA_KEY = "payloadData";
	public static final String JWT_SECURITY_DATA_KEY = "securityData";
	public static final String JWT_TOKEN_KEY = "leap_auth_token";
	public static final String PAYLOAD_AUTH_DATA_KEY = "authData";
	public static final String TENANT_SITE_INFO_KEY = "tenantSiteInfo";

	// constants realted to the JWT Type and ALGO
	public static final String JWT_TYPE_KEY = "type";
	public static final String JWT_TYPE_ALGO = "algo";
	public static final String JWT_TYPE_VALUE = "jwt";
	public static final String JWT_TYPE_ALGO_VALUE = "HS256";

	public static final String DOMAIN = "domain";

	//constants related to Okta implementation

	public static final String AUTH_PROVIDER_KEY = "authProvider";
	public static final String OKTA_AUTH_PROVIDER = "okta";
	public static final String PING_AUTH_PROVIDER = "ping";

	public final static String HTTP_POST_MTD = "POST";
	public final static String HTTP_CONTENT_TYPE_KEY = "Content-Type";
	public final static String HTTP_CONTENT_TYPE_APP_JSON = "application/json";
	public final static String HTTP_CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
	public final static String HTTP_AUTHZ_KEY = "Authorization";
	public final static String HTTP_BASIC_AUTHZ = "Basic";
	public final static String HTTP_UTF_8_ENCODING = "UTF-8";
	public final static String OKTA_ENCODED_CLIENT_CREDENTIALS = "";

}
