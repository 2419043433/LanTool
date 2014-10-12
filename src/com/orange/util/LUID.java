package com.orange.util;

/*
 * Local Unique Identifier
 * Only used in single thread context
 */
public class LUID {
	private static long mId = 0;
	public static String getLUID()
	{
		return String.valueOf(mId ++);
	}
}
