package com.people.lyy.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.syxc.util.Logger;

public class ParseResponseJSON {
	public static Object parseJSON(int reqType, String responseStr) {
//		Logger.e("response", responseStr);
		
		try {
			switch (reqType) {
			case TransferRequestTag.Login:
				return parseResponse( responseStr );
			
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static HashMap<String, String> parseBindResponse(String responseStr) {
		// TODO Auto-generated method stub
		
		JSONObject jsonObject;
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			jsonObject = new JSONObject( responseStr );
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static HashMap<String, String> parseResponse(String str) {
		JSONObject jsonObject;
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			jsonObject = new JSONObject( str );
			for( Iterator<String> keys = jsonObject.keys(); keys.hasNext(); ){
				String key1 = keys.next();
				if( key1.equals("message") ){
					JSONObject jsonMsg = new JSONObject( jsonObject.optString("message") );
					map.put("errorMsg", jsonMsg.optString("errorMsg"));
					try {
						JSONObject jsonData = new JSONObject( jsonMsg.optString("data") );
						for( Iterator<String> dataKeys = jsonData.keys(); dataKeys.hasNext(); ){
							String key2 = dataKeys.next();
//							Logger.i("json data:", key2 +":"+ jsonData.get(key2).toString() );
							map.put(key2, jsonData.get( key2 ).toString() );
						}
					} catch ( JSONException e ) {
						map.put("data", jsonMsg.optString("data") );
					}
				}else{
					map.put( key1, jsonObject.get(key1).toString() );
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}
	
	private static String getJsonValue(String json,String key){
		String value="";
		try {
			if(key!=null&&!"".equals(key)){
				JSONObject obj=new JSONObject(json);
				value=obj.optString(key);//解析得到的值 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	//根据特定key值获取value的内容
	private static String getJsonValueHJH(String json,String key){
		String value="";
		try {
			if(key!=null&&!"".equals(key)){
				JSONObject obj=new JSONObject(json);
				value = obj.optString("message");//解析得到的值
				obj = new JSONObject(value);
				value = obj.optString(key);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	private static List<Map<String, String>> getJsonArray(String jsonstr, String key, List<String> params){
		List<Map<String, String>> listArray = new ArrayList<Map<String, String>>();
		try {
			JSONObject obj=new JSONObject(jsonstr);
			JSONArray ary = obj.getJSONArray(key);
			for (int i = 0; i < ary.length(); i++) {
				JSONObject objAry = ary.optJSONObject(i);
				HashMap<String, String> mapAry = new HashMap<String, String>();
				for(int j = 0; j < params.size(); j++){
					String param = params.get(j);
					mapAry.put(param,  objAry.getString(param) );
				}
				listArray.add(mapAry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return listArray;
	}
	
	private static List<Map<String, String>> getJsonArray(String jsonstr, String key){
		List<Map<String, String>> listArray = new ArrayList<Map<String, String>>();
		try {
			JSONObject obj=new JSONObject(jsonstr);
			JSONArray ary = obj.getJSONArray(key);
			for (int i = 0; i < ary.length(); i++) {
				JSONObject objAry = ary.optJSONObject(i);
				HashMap<String, String> mapAry = new HashMap<String, String>();
				mapAry.put("dev", objAry.getString("dev"));
				mapAry.put("time", objAry.getString("time"));
				listArray.add(mapAry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return listArray;
	}
}
