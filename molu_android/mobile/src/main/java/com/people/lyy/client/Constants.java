package com.people.lyy.client;

import java.util.ArrayList;
import java.util.List;

import com.people.lyy.util.LocalPropertyUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
	// 当前系统的版本号
	public static final int VERSION = 1;

	public static final String AESKEY = "dynamicode";

	public static final String APPFILEPATH = "/data/data/"
			+ ApplicationEnvironment.getInstance().getApplication().getPackageName();

	// assets下的文件保存路径
	public static final String ASSETSPATH = APPFILEPATH + "/assets/";

	public static final String kUSERNAME = "kUSERNAME";
	public static final String kPASSWORD = "kPASSWORD";
	public static final String kLOCKKEY = "LockKey";
	public static final String kGESTRUECLOSE = "GestureClose";
	

	public static String IP="http://192.168.253.2:8080";
	
	
	
    
	public static final String kVERSION = "VERSION";

	public static final String kACCOUNTLIST = "kACCOUNTLIST";

	// public static final String IP = "https://192.168.1.205:6443";
	// public static final String IP = "https://192.168.1.234:6443";
//	public static String IP = "https://118.26.73.74:12345";
	// public static final String IP = "https://192.168.1.197:6443";
//	public static String IP ="https://118.26.73.74:12345";

	
	public static boolean GENTOKEN_ONLINE = true;
	public static boolean SHOP_ONLINE = true;

	public static final int OVERTIME = 20;// 超时时间

	public static boolean HASSETBLUETOOTH = false;

	public static String LOGGED = "Logged";

	public static String SOTPPACKET = "com.people.sotp.service";

	public static String resultString = "";

	public static String URL;
	public static String VERSION2 = "";

	public static String resultCode;
	public static String resultBalance;

	// 判断用户是否登录的全局状态
	public static boolean isLogin = false;
	// 用户账户对应的银行卡号
	public static String cardNum = "";
	// 全局存在的用户手机号
	//
	public static String phoneNumGobal = "";

	// 安全插件的文件名
	public static String pluginName = "sotpCore";

	// sotp保护服务开通状态
	public static String sotpLoginService = "0";
	public static String sotpOnepayService = "0";
	public static String sotpEpayService = "0";

	// 一键支付业务开通状态
	public static String onepayStatus = "0";

	// 以下变量用于在执行两种支付业务时，发现没有插件去下载插件，下载插件完成后再跳回到支付页面
	public static String epayDownloadComeback = "0";
	public static String onekeypayDownloadComeback = "0";
	public static String loginDownloadComeback = "0";

	// 这个是临时添加的，用于在下载插件的时候防止重复下载，作死的用法，
	public static boolean isSendingMsg = false;

}
