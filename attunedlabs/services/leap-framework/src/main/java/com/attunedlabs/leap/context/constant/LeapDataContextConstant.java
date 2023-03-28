package com.attunedlabs.leap.context.constant;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapDataContextConstant {

	// leap data context constant
	public static final String LEAP_DATA_CONTEXT = "leapDataContext";
	public static final String OLD_LEAP_DATA_CONTEXT = "oldLeapDataContext";

	public static final String ACCOUNTID = "accountId";
	public static final String TENANTID = "tenantId";
	public static final String SITEID = "siteId";
	public static final String FEATUREGROUP = "featureGroup";
	public static final String FEATURENAME = "feature";
	public static final String SERVICENAME = "serviceName";
	public static final String REQUEST_METHOD = "requestMethod";
	public static final String IMPLEMENTATION_NAME = "implName";
	public static final String VENDOR = "vendor";
	public static final String VERSION = "version";
	public static final String PROVIDER = "provider";

	// Data key constant
	public static final String DATA_KEY = "data";

	// request unique Id constant
	public static final String REQUEST_ID_KEY = "requestUUID";

	// constants for storing data in leap data context
	public static final String PRIVATE_HEADERS = "#leap_private";
	public static final String HEADER = "#header";
	public static final String INITIAL_CONTEXT = "#leap_initial";
	public static final String RESPONSE_CONTEXT = "#responsecontext";

	public static final String MEDIA_TYPE = "mediaType";
	public static final String API_VERSION = "apiVersion";
	public static final String CONTEXT = "context";
	public static final String LANG = "lang";
	public static final String KIND = "#context";

	public static final String ACTUAL_COLUMNNAME = "actualColumnName";
	public static final String BYTE_LENGTH = "byteLenth";
	public static final String TYPE = "type";
	public static final String EFFECTIVE_COLUMNNAME = "effectiveColumnName";

	public static final String ELEMENT = "element";
	public static final String META_DATA = "metadata";
	public static final String ITEMS = "items";
	public static final String DATA = "data";
	public static final String TOTAL_ITEMS = "totalItems";
	public static final String UPDATED = "updated";

	public static final String API_VERSION_VAL = "1.0";
	public static final String LANG_VAL = "en";
	public static final String KIND_VAL = "kind";
	public static final String ERROR = "error";

	public static final String TAXONOMY_ID = "taxonomy_id";
	public static final String VENDORTAXONOMYID = "vendorTaxonomyId";


	public static final String HTTP_POST = "post";

	// Feature Deployment constant
	public static final String FEATURE_DEPLOYMENT = "FeatureDeployment";

	// Pipeline constant
	public static final String PIPELINE_CONFIG_KEY = "pipelineConfig";

	// Authetication related constants
	public static final String AUTHENTICATION_FEATURE_GROUP = "authentication";
	public static final String AUTHENTICATION_FEATURE = "authenticationservice";
	public static final String LOGIN_SERVICE = "login";
	public static final String TRUE_KEY = "true";
	public static final String FALSE_KEY = "false";

	public static final String ACCESS_TOKEN = "access_token";
	public static final String TENANT_TOKEN = "tenant_token";
	public static final String TENANT_TOKEN_EXPIRATION = "tenant_token_expiration";

	// constants to handle cookie for login service
	public static final String CAMEL_RESTLET_RESPONSE = "CamelRestletResponse";
	public static final String CAMEL_RESTLET_REQUEST = "CamelRestletRequest";
	public static final String COOKIE = "cookieLogin";

	// constants for endpoints supported by leap
	public static final String ENDPOINT_TYPE_KEY = "endpointType";
	public static final String HTTP_JSON_KEY = "HTTP-JSON";
	public static final String HTTP_XML_KEY = "HTTP-XML";
	public static final String CXF_ENDPOINT_KEY = "CXF-ENDPOINT";

	// constants for camel http method and usage
	public static final String CAMEL_HTTP_METHOD = "CamelHttpMethod";
	public static final String GET_KEY = "GET";
	public static final String POST_KEY = "POST";
	public static final String AND_KEY = "&";
	public static final String ISEQUAL_KEY = "=";

	// constant for timezone
	public static final String TIMEZONE = "timeZone";

	// global tenant and site
	public static final String GLOBAL_TENANT_ID = "all";
	public static final String GLOBAL_SITE_ID = "all";

	// default taxonomy
	// public static final String WHEREWORKS_TAXONOMY = "Whereworks";
	public static final String LEAPDEFAULT_TAXONOMY = "LeapDefault";

	public static final String TEMPLATE_FILE_PATTERN = "Template.json";
	public static final String SWAGGER_FILE_PATTERN = "Swagger.json";
	public static final String SWAGGER = "swagger";
	public static final String JSON_SCHEMA = "jsonSchema";

	// Request data
	public static final String REQUEST_LEAP_lOCAL = "leapLocal";
	public static final String REQUEST_TAXONOMY = "taxonomy";
	public static final String REQUEST_TAXONOMY_ID = "taxonomy_id";

	// taxonomyId set in exchange header
	public static final String TAXONOMY_ID_INHEADER = "taxonomyId";
	
	public static final String LDC_DATA_MAP = "LDCDataMap";
	
	public static final String TAXONOMY_KEY = "taxonomy";
	public static final String PROJECTION_KEY = "projection";
	public static final String TEMPLATE_KEY = "template";

}
