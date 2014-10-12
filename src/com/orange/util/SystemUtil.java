package com.orange.util;


public class SystemUtil {

	public static long getPID() {
	    String processName =
	        java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	    return Long.parseLong(processName.split("@")[0]);

	}
	
	public static String getGUID()
	{
		 return java.util.UUID.randomUUID().toString();
	}
	
	public static String getLUID()
	{
		return LUID.getLUID();
	}

}
