package com.people.lyy.client;

import android.annotation.SuppressLint;
import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class TransferRequestTag {

	public static final int Login = 1; // 登录
	
	private static HashMap<Integer, String> requestTagMap = null;

	public static HashMap<Integer, String> getRequestTagMap() {
		if (null == requestTagMap) {
			requestTagMap = new HashMap<Integer, String>();

			requestTagMap.put(Login, Constants.IP + "/UberServer/Request");
			
			
		}

		return requestTagMap;
	}
	
	public static void resetTagMap() {
		requestTagMap = null;
		getRequestTagMap();
	}

}
