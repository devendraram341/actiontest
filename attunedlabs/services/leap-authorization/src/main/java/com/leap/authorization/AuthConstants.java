package com.leap.authorization;

/**
 * Constants defined
 * 
 * @author Getusleap
 *
 */
public class AuthConstants {

	/**
	 * To protect creating the constructor of this class
	 */
	private AuthConstants() {
	}

	public static final String OACC_DB_URL_KEY = "OAUTH_DB_URL";
	public static final String OACC_DB_USER_KEY = "DB_USER";
	public static final String OACC_DB_PASSWRD_KEY = "DB_PASSWORD";
	public static final String OACC_DB_SCHEMA_KEY = "dbSchema";
	public static final String OACC_DB_ROOT_PWD_KEY = "oaccRootPwd";
	public static final String RESOURCE_APPENDER = "_CHILD";

	/* RESOURCETYPE - Constants */
	public static final String RESOURCE_TYPE_ROLE = "ROLE";
	public static final String RESOURCE_TYPE_USERS = "USERS";
	public static final String BASE_TENANT = "All";

	public static final String REQUEST_DATA = "orignalData";
}
