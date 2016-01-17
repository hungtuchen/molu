package com.test.environment.alex.environmenttesttask002;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class DataInfo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataInfo.initData();
		String d = "司机名:"+DataInfo.Driver.GetDriver().name + "\n"
				+"手机号:"+DataInfo.Driver.GetDriver().phone + "\n"
				+"预计时间:"+ DataInfo.Driver.GetDriver().time+"\n"
				+"车牌号:"+DataInfo.Driver.GetDriver().cartNo;

		String u = "车友昵称:"+DataInfo.MoLuFriends.GetMoLuFriends().name +"\n"
				//		+"手机号:"+DataInfo.MoLuFriends.GetMoLuFriends().phone+"\n"
				+"性别:"+DataInfo.MoLuFriends.GetMoLuFriends().sex;
		System.out.println(d+"\n"+u);
	}
	public static List<Driver> driverTable = new ArrayList<Driver>();
	public static List<MoLuFriends> moluFrindsTable = new ArrayList<MoLuFriends>();

	String str = "我飞女厕所巧克力多次买了的免息免代码王李孙红组主朱魏明帝瞑目了密码轻松的但其文凭阿德系车蜜么莫额外明年送妈丹器道需哦面对" ;
	public static void initData(){
		driverTable.add(new Driver("王师傅", "18200093841", "京P-BS3**", "3"));
		driverTable.add(new Driver("王师傅", "18200093321", "京P-BS2**", "6"));
		driverTable.add(new Driver("李师傅", "18200093843", "京P-BB3**", "8"));
		driverTable.add(new Driver("哈师傅", "18200093845", "京P-BS8**", "7"));
		driverTable.add(new Driver("马师傅", "18200093834", "京P-BS4**", "4"));
		driverTable.add(new Driver("代师傅", "18200093878", "京P-BS7**", "6"));
		driverTable.add(new Driver("都师傅", "18200093899", "京P-BS9**", "5"));
		driverTable.add(new Driver("李师傅", "18200093866", "京P-BY3**", "2"));
		driverTable.add(new Driver("王师傅", "18200093843", "京P-BY0**", "7"));
		driverTable.add(new Driver("李师傅", "18200093840", "京P-BY1**", "3"));


		moluFrindsTable.add(new MoLuFriends("语蝶", "男", "18200092841"));
		moluFrindsTable.add(new MoLuFriends("晓旋", "女", "18200092841"));
		moluFrindsTable.add(new MoLuFriends("盼芙", "女", "18200094841"));
		moluFrindsTable.add(new MoLuFriends("采珊", "女", "18200095841"));
		moluFrindsTable.add(new MoLuFriends("迎天", "女", "18200096841"));
		moluFrindsTable.add(new MoLuFriends("南珍", "男", "18200094841"));
		moluFrindsTable.add(new MoLuFriends("妙芙", "男", "18200042844"));
		moluFrindsTable.add(new MoLuFriends("语柳", "女", "18200092844"));
		moluFrindsTable.add(new MoLuFriends("含莲", "男", "18200092843"));
		moluFrindsTable.add(new MoLuFriends("夏山", "女", "18200092842"));
		moluFrindsTable.add(new MoLuFriends("尔容", "男", "18200092846"));
		moluFrindsTable.add(new MoLuFriends("采春", "女", "18200092847"));
		moluFrindsTable.add(new MoLuFriends("念梦", "男", "18200092848"));
		moluFrindsTable.add(new MoLuFriends("傲南", "女", "18200082842"));
	}

	public static class Driver{
		public String name;
		public String phone;
		public String cartNo;
		public String time;

		public Driver(String name, String phone, String cartNo, String time)
		{
			this.name = name;
			this.phone = phone;
			this.cartNo = cartNo;
			this.time = time;
		}
		public void AddDriver(Driver d){
			DataInfo.driverTable.add(d);
		}
		public static Driver GetDriver()
		{
			int x = new Random().nextInt(10);
			return DataInfo.driverTable.get(x);
		}
	}

	public static class MoLuFriends{
		public String name;
		public String sex;
		public String phone;

		public MoLuFriends(String name, String sex, String phone)
		{
			this.name = name;
			this.phone = phone;
			this.sex = sex;
		}
		public void AddMoLuFriends(MoLuFriends molu){
			DataInfo.moluFrindsTable.add(molu);
		}
		public static MoLuFriends GetMoLuFriends()
		{
			int x = new Random().nextInt(10);
			return DataInfo.moluFrindsTable.get(x);
		}
	}
}
