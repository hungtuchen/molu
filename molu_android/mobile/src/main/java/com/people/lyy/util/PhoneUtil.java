package com.people.lyy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.syxc.util.Logger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellLocation;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class PhoneUtil {
	public static String getPhoneNum(Context context){
		String phoneNum = (String)((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		
		if (null != phoneNum){
			if (phoneNum.startsWith("+86")){
				return phoneNum.replace("+86", "");
			} else if (phoneNum.startsWith("+086"))
				return phoneNum.replace("+086", "");
			
			return phoneNum;
		}
		
		return "";
		
	}
	
	public static String getIMEI(Context context){
		String IMEI = (String)((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if (null == IMEI)
			return "000000000000000";
		return IMEI;
	}
	
	public static String getLocation(Context context){
		CellLocation location = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getCellLocation();
		return location.toString();
	}
	
	public static String getPhoneModel(){
		return android.os.Build.MODEL;
	}
	
	public static String getLocalMacAddress(Context context) {  
		WifiManager wifi = (WifiManager) (context.getSystemService(Context.WIFI_SERVICE));  
        WifiInfo info = wifi.getConnectionInfo();  
        String MAC = info.getMacAddress();
        if( null == MAC )
        	return "90:18:7C:1C:1B:B9";
        return MAC;
    }  
	
	public static String getIMSI(Context context){
		String IMSI = (String)((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
		if (null == IMSI)
			return "";
		return IMSI;
	}
	
	public static String SHA(String decript) {
		try {
			MessageDigest digest = MessageDigest
					.getInstance("SHA");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
//	public static void sendSMS(String phoneNum, String smsCnt) {
//		try{
//			SmsManager smsManager = SmsManager.getDefault();
//			smsManager.sendTextMessage(phoneNum, null, smsCnt, null, null);
//		} catch(Exception e){
//			e.printStackTrace();
//			BaseActivity.getTopActivity().runOnUiThread(new Runnable(){
//				@Override
//				public void run() {
//					Toast.makeText(BaseActivity.getTopActivity(), "发送短信失败", Toast.LENGTH_SHORT).show();
//				}
//				
//			});
//		}
		
//	}
	
//	public static void sendMMS(String phoneNum){
//		Intent sendIntent = new Intent(Intent.ACTION_SEND,  Uri.parse("mms://"));  
//	    sendIntent.setType("image/jpeg");  
//	    sendIntent.putExtra("subject", "交易签单");
//	    sendIntent.putExtra("sms_body", "已成功完成交易，附上信息签名，请注意查收保存");
//	    sendIntent.putExtra("address", phoneNum);
//	    String url = "file://mnt//sdcard//image//123456.JPEG";  
//	    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));  
//	    ApplicationEnvironment.getInstance().getApplication().startActivity(Intent.createChooser(sendIntent, "MMS:"));
//	    
//	}
	
}
