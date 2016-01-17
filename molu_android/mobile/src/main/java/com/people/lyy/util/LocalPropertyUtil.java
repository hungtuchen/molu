package com.people.lyy.util;


import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class LocalPropertyUtil {
	
	private static SharedPreferences setting = null;
	
	public static String getProperty( Context context, String propName ){
		setting = context.getSharedPreferences(context.getPackageName(), 0);
		String str = setting.getString(propName, null);
		return str;
	}
	
	public static String getHistory( Context context, String propName ){
		setting = context.getSharedPreferences(context.getPackageName(), 0);
		String str = setting.getString(propName, "https://118.26.73.74:12345");
		return str;
	}
	
	public static boolean setProperty( Context context, String propName, String propValue ){
		setting = context.getSharedPreferences(context.getPackageName(), 0);
		Editor editor = setting.edit();
		editor.putString(propName, propValue);
		editor.commit();
		return true;
	}
	
	public static void saveHistory( Context context, String propName, String propValue ){
		setting = context.getSharedPreferences(context.getPackageName(), 0);
		String longhistory = setting.getString(propName, "https://118.26.73.74:12345");
		
		String str = getHistory(context, propName);
		String[] arry = str.split(",");
		for (String string : arry) {
			if( !propValue.equals(string) ){
				if( !longhistory.contains(propValue + ",")){
					StringBuffer sb = new StringBuffer(longhistory);
					sb.insert(0, propValue +",");
					setting.edit().putString(propName,sb.toString()).commit();
				}
			}else{
				Toast.makeText(context, "切换成功", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	public static boolean removeProperty( Context context, String propName ){
		setting = context.getSharedPreferences(context.getPackageName(), 0);
		Editor editor = setting.edit();
		editor.remove(propName);
		editor.commit();
		return true;
	}
	//获取屏幕宽
	public static int getWidth(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	
	//获取屏幕高
		public static int getHeight(Context context){
			DisplayMetrics dm = new DisplayMetrics();
			((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			return dm.heightPixels;
		}
	
}
