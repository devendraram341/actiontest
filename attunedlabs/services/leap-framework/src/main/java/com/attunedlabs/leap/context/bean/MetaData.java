package com.attunedlabs.leap.context.bean;

/**
 * 
 * @author Reactiveworks
 *
 */
public class MetaData {

	private String type;
	private int byteLenth;
	private String effectiveColumnName;
	private String actualColumnName;

	public MetaData() {
		super();
	}

	public MetaData(String type, int byteLenth, String i18nID, String i18nLangText) {
		super();
		this.type = type;
		this.byteLenth = byteLenth;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/*public String getI18nID() {
		return i18nID;
	}

	public void setI18nID(String i18nID) {
		this.i18nID = i18nID;
	}*/

	public int getByteLenth() {
		return byteLenth;
	}

	public void setByteLenth(int byteLenth) {
		this.byteLenth = byteLenth;
	}

	public String getEffectiveColumnName() {
		return effectiveColumnName;
	}

	public void setEffectiveColumnName(String effectiveColumnName) {
		this.effectiveColumnName = effectiveColumnName;
	}

	public String getActualColumnName() {
		return actualColumnName;
	}

	public void setActualColumnName(String actualColumnName) {
		this.actualColumnName = actualColumnName;
	}

	/*public String getI18nLangText() {
		return i18nLangText;
	}

	public void setI18nLangText(String i18nLangText) {
		this.i18nLangText = i18nLangText;
	}*/
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetaData [type=");
		builder.append(type);
		builder.append(", byteLenth=");
		builder.append(byteLenth);
		builder.append(", effectiveColumnName=");
		builder.append(effectiveColumnName);
		builder.append(", actualColumnName=");
		builder.append(actualColumnName);
		builder.append("]");
		return builder.toString();
	}

}
