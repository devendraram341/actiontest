package com.attunedlabs.permastore.config.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;
import com.attunedlabs.permastore.config.jaxb.CustomBuilder;

public class MapData implements IPermaStoreCustomCacheObjectBuilder {

	@Override
	public Serializable loadDataForCache(CustomBuilder arg0) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("third", 101);
		map.put("four", 102);
		return (Serializable) map;
	}
}
