package com.attunedlabs.permastore.config.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

public class ListData implements IPermaStoreCustomCacheObjectBuilder {

	@Override
	public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
		List<Object> list = new ArrayList<>();
		list.add(101);
		list.add(102);
		return (Serializable) list;
	}

}