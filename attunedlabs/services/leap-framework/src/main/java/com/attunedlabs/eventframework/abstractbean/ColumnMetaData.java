package com.attunedlabs.eventframework.abstractbean;

public class ColumnMetaData {
	
	private String columnName;
	private String dataType;
	private int length;
	private String defaultValue;
	private boolean notNull;
	private boolean autoIncrement;
	
	public ColumnMetaData(String columnName, String dataType, int length, String defaultValue, boolean notNull, boolean autoIncrement) {
		this.columnName=columnName;
		this.dataType=dataType;
		this.length=length;
		this.defaultValue=defaultValue;
		this.notNull=notNull;
		this.autoIncrement=autoIncrement;
	}
	
	public boolean isNotNull() {
		return notNull;
	}
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}
	public boolean isAutoIncreament() {
		return autoIncrement;
	}
	public void setAutoIncreament(boolean autoIncreament) {
		this.autoIncrement = autoIncreament;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	

}
