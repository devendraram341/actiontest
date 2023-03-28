package com.attunedlabs.permastore.config.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

public class SetData implements IPermaStoreCustomCacheObjectBuilder {

	@Override
	public Serializable loadDataForCache(CustomBuilder configBuilderConfig) {
		Set<Object> set = new HashSet<Object>();
		set.add(101);
		set.add(102);
		return (Serializable) set;
	}
}
