package com.attunedlabs.leap.context.bean;

import org.apache.metamodel.data.DataSet;

/**
 * 
 * @author Reactiveworks
 *
 */
public class LeapMMDataSet implements LeapResultSet {

	private String type = "MetaModelDataSet";
	private DataSet data;

	public LeapMMDataSet() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public DataSet getData() {
		return this.data;
	}

	@Override
	public void setData(Object data) {
		this.data = (DataSet) data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeapMMDataSet [type=");
		builder.append(type);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}

}
