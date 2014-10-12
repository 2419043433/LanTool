package com.orange.system;

import java.util.HashMap;
import java.util.Map;

public class SystemInfo {
	public enum Keys
	{
		GUID,
	}
	Map<Keys, Object> mMap  = new HashMap<SystemInfo.Keys, Object>();
	
	private static SystemInfo sInstance;
	public static SystemInfo getInstance()
	{
		if(null == sInstance)
		{
			sInstance = new SystemInfo();
		}
		return sInstance;
	}
	
	private SystemInfo(){}
	
	public SystemInfo put(Keys k, Object v) {
		mMap.put(k, v);
		return this;
	}

	public Object get(Keys k) {
		return mMap.get(k);
	}

	public Boolean getBoolean(Keys k) {
		return (Boolean) get(k);
	}

	public String getString(Keys k) {
		return (String) get(k);
	}

	public Integer getInt(Keys k) {
		return (Integer) get(k);
	}

	public Long getLong(Keys k) {
		return (Long) get(k);
	}
}
