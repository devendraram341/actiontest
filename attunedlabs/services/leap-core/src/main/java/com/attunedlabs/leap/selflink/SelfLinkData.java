package com.attunedlabs.leap.selflink;

import java.util.List;

public class SelfLinkData {

	private String kind;
	private String updated;
	private Integer totalItems;
	private List<ItemData> items;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
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

	public List<ItemData> getItems() {
		return items;
	}

	public void setItems(List<ItemData> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "SelfLinkData [kind=" + kind + ", updated=" + updated + ", totalItems=" + totalItems + ", items=" + items
				+ "]";
	}

}
