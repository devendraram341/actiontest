package com.attunedlabs.leapentity.autoIncrement;

/**
 * this interface contract to get the autoIncrementvalue based on the impl type
 * 
 * @author Nishant
 *
 */
public interface ILeapEntityAutoIncrement {

	/**
	 * it will generate the value of autoIncrement based on the columnType
	 * 
	 * @param columnType
	 * @return {@link Object}
	 */
	public Object getAutoIncrementValue(String columnType);
}
