package com.attunedlabs.leapentity.autoIncrement;

/**
 * this is Factory design pattern for {@link ILeapEntityAutoIncrement}
 * 
 * @author Reactiveworks42
 *
 */
public class LeapEntityAutoIncrementFactoryBean {

	private static final String SNOWFLAKE = "SNOWFLAKE";
	private static final String OTHER = "OTHER";
	private static final String NATIVE_TYPE = "NATIVE";
	private static final String MANUAL_TYPE = "MANUAL";
	private static final String MYSQL = "MYSQL";
	private static final String ORACLE = "ORACLE";
	private static final String SQLSERVER = "SQLSERVER";

	private LeapEntityAutoIncrementFactoryBean() {
	}

	/**
	 * based on the impl parameter ,it will create the instance of
	 * {@link ILeapEntityAutoIncrement}
	 * 
	 * @param type
	 * @param value
	 * @return {@link ILeapEntityAutoIncrement}
	 * @throws LeapEntityAutoIncrementException if unable to get the impl
	 */
	public static ILeapEntityAutoIncrement getInstance(String type, String value)
			throws LeapEntityAutoIncrementException {
		if (MANUAL_TYPE.equals(type))
			switch (value.toUpperCase()) {
			case SNOWFLAKE:
				return SnowFlakeLeapEntityAutoIncrement.getInstance(23);
			case OTHER:
				break;
			default:
				throw new LeapEntityAutoIncrementException(
						value.toUpperCase() + " doesn't exits in  AutoIncrement " + MANUAL_TYPE + " Type ");
			}
		if (NATIVE_TYPE.equals(type)) {
			switch (value) {
			case MYSQL:
				break;
			case ORACLE:
				break;
			case SQLSERVER:
				break;
			default:
				throw new LeapEntityAutoIncrementException(
						value.toUpperCase() + " doesn't exits in  AutoIncrement " + NATIVE_TYPE + " Type ");
			}
		}
		return null;
	}

}
