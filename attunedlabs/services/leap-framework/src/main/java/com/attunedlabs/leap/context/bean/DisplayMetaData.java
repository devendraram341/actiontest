package com.attunedlabs.leap.context.bean;

/**
 * This metaData section is only available for data service (Entity service)
 * 
 * @author Reactiveworks42
 *
 */
public class DisplayMetaData {

	private String entityFieldNameRef;
	private Boolean readOnly;
	private Boolean searchable;
	private String pattern;
	private String maxLength;
	private String minLength;
	private String i18N;

	public String getEntityFieldNameRef() {
		return entityFieldNameRef;
	}

	public void setEntityFieldNameRef(String entityFieldNameRef) {
		this.entityFieldNameRef = entityFieldNameRef;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getMinLength() {
		return minLength;
	}

	public void setMinLength(String minLength) {
		this.minLength = minLength;
	}

	public String getI18N() {
		return i18N;
	}

	public void setI18N(String entityName) {
		this.i18N = entityName + "." + entityFieldNameRef;
	}

	public void updateI18NFieldName(String fieldNameRef) {
		this.i18N = this.i18N.substring(0, this.i18N.lastIndexOf(".") + 1);
		this.i18N = this.i18N + fieldNameRef;
	}

	@Override
	public String toString() {
		return "DisplayMetaData [entityFieldNameRef=" + entityFieldNameRef + ", readOnly=" + readOnly + ", searchable="
				+ searchable + ", pattern=" + pattern + ", maxLength=" + maxLength + ", minLength=" + minLength
				+ ", i18N=" + i18N + "]";
	}

}
