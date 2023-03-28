package com.attunedlabs.leap.context.bean;

import java.util.List;

/**
 * 
 * @author Reactiveworks
 *
 */

public class LeapData extends LeapDataContextElement {

	private String kind;
	private List<MetaData> metadata;
	private LeapResultSet items;
	private String taxonomyId;
	private String updated;
	private Integer totalItems;

	/*
	 * this field is only for data service (Entity service)
	 */
	private List<DisplayMetaData> displayMetaData;

	public LeapData() {
		super();
	}

	public LeapData(String kind, List<MetaData> metadata, LeapResultSet items) {
		super();
		this.kind = kind;
		this.metadata = metadata;
		this.items = items;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public Integer getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public List<MetaData> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<MetaData> metaDataArray) {
		this.metadata = metaDataArray;
	}

	public LeapResultSet getItems() {
		return items;
	}

	public void setItems(LeapResultSet items) {
		this.items = items;
	}

	public String getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(String taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public List<DisplayMetaData> getDisplayMetaDatas() {
		return displayMetaData;
	}

	public void setDisplayMetaDatas(List<DisplayMetaData> displayMetaDatas) {
		this.displayMetaData = displayMetaDatas;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeapData [");
		if (kind != null) {
			builder.append("kind=");
			builder.append(kind);
			builder.append(", ");
		}
		if (metadata != null) {
			builder.append("metadata=");
			builder.append(metadata);
			builder.append(", ");
		}
		if (items != null) {
			builder.append("items=");
			builder.append(items);
			builder.append(", ");
		}
		if (taxonomyId != null) {
			builder.append("taxonomyId=");
			builder.append(taxonomyId);
		}
		if (totalItems != null) {
			builder.append("totalItems=");
			builder.append(totalItems);
		}
		if (updated != null) {
			builder.append("updated=");
			builder.append(updated);
		}
		builder.append("]");
		return builder.toString();
	}

}
