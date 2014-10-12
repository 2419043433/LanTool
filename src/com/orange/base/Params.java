package com.orange.base;

import java.util.HashMap;
import java.util.Map;

public class Params {
	private Map<ParamKeys, Object> mMap = new HashMap<ParamKeys, Object>();

	public Params put(ParamKeys k, Object v) {
		mMap.put(k, v);
		return this;
	}

	public Object get(ParamKeys k) {
		return mMap.get(k);
	}

	public Boolean getBoolean(ParamKeys k) {
		return (Boolean) get(k);
	}

	public String getString(ParamKeys k) {
		return (String) get(k);
	}

	public Integer getInt(ParamKeys k) {
		return (Integer) get(k);
	}

	public Long getLong(ParamKeys k) {
		return (Long) get(k);
	}

	public static Params obtain() {
		return new Params();
	}

}
