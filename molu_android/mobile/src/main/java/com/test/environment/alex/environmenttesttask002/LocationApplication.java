package com.test.environment.alex.environmenttesttask002;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

public class LocationApplication extends android.support.multidex.MultiDexApplication {
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	
	public TextView mLocationResult,logMsg;
    public TextView trigger,exit;
    public Vibrator mVibrator;
    
    private JSONObject mJsonObject = null;


    
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
    	
    	SDKInitializer.initialize(this);
    	
    	mLocationClient = new LocationClient( this.getApplicationContext() );
    	mMyLocationListener = new MyLocationListener();
    	mLocationClient.registerLocationListener( mMyLocationListener );
    	mVibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//    	mJsonObject = new JSONObject();
    }
    
    public JSONObject getLocationJson() {
    	return this.mJsonObject;
    }
    
    /**
     * ʵ��ʵʱλ�ûص�����
     */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			JSONObject jsonParams = new JSONObject();
			try {
				jsonParams.put("time", 			location.getTime());
				jsonParams.put("error", 		location.getLocType());
				jsonParams.put("latitude", 		location.getLatitude());
				jsonParams.put("longtitude", 	location.getLongitude());
				jsonParams.put("radius", 		location.getRadius());
				
				if ( location.getLocType() == BDLocation.TypeGpsLocation ) {
					jsonParams.put("addr", location.getAddrStr());
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					jsonParams.put("addr", location.getAddrStr());
				}
				
				jsonParams.put("locationdescribe", location.getLocationDescribe());
				List<Poi> list = location.getPoiList();// POI��Ϣ
				if (list != null) {
					jsonParams.put("poilistSize", list.size() );
					for (int i = 0; i < list.size(); i++) {
						Poi p = list.get(i);
						jsonParams.put("poi"+i, p.getName());
						jsonParams.put("poiRank"+i, p.getRank());
	                }
				}
				Log.i("BaiduLocationApiDem", jsonParams.toString());
				mJsonObject = jsonParams;
//				putJson( jsonParams );
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			StringBuffer sb = new StringBuffer(256);
//			sb.append("time : ");
//			sb.append( location.getTime() );
//			sb.append("\nerror code : ");
//			sb.append( location.getLocType() );
//			sb.append("\nlatitude : ");
//			sb.append( location.getLatitude() );
//			sb.append("\nlongtitude : ");
//			sb.append( location.getLongitude() );
//			sb.append("\nradius : ");
//			sb.append( location.getRadius() );
//			if ( location.getLocType() == BDLocation.TypeGpsLocation ) {
//				sb.append("\nspeed : ");
//                sb.append(location.getSpeed());// ��λ������ÿСʱ
//                sb.append("\nsatellite : ");
//                sb.append(location.getSatelliteNumber());
//                sb.append("\nheight : ");
//                sb.append(location.getAltitude());// ��λ����
//                sb.append("\ndirection : ");
//                sb.append(location.getDirection());
//                sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                sb.append("\ndescribe : ");
//                sb.append("gps��λ�ɹ�");
//			} else if ( location.getLocType() == BDLocation.TypeNetWorkLocation ) {
//				sb.append("\naddr : ");
//                sb.append(location.getAddrStr());
//                //��Ӫ����Ϣ
//                sb.append("\noperationers : ");
//                sb.append(location.getOperators());
//                sb.append("\ndescribe : ");
//                sb.append("���綨λ�ɹ�");
//			} else if ( location.getLocType() == BDLocation.TypeOffLineLocation ) {
//				sb.append("\ndescribe : ");
//                sb.append("���߶�λ�ɹ������߶�λ���Ҳ����Ч��");
//			} else if ( location.getLocType() == BDLocation.TypeServerError ) {
//				sb.append("\ndescribe : ");
//                sb.append("��������綨λʧ�ܣ����Է���IMEI�źʹ��嶨λʱ�䵽loc-bugs@baidu.com��������׷��ԭ��");
//			} else if ( location.getLocType() == BDLocation.TypeNetWorkException ) {
//				sb.append("\ndescribe : ");
//                sb.append("���粻ͬ���¶�λʧ�ܣ����������Ƿ�ͨ��");
//			} else if ( location.getLocType() == BDLocation.TypeCriteriaException ) {
//				sb.append("\ndescribe : ");
//                sb.append("�޷���ȡ��Ч��λ���ݵ��¶�λʧ�ܣ�һ���������ֻ��ԭ�򣬴��ڷ���ģʽ��һ���������ֽ��������������ֻ�");
//			}
//			sb.append("\nlocationdescribe : ");// λ�����廯��Ϣ
//            sb.append(location.getLocationDescribe());
//            List<Poi> list = location.getPoiList();// POI��Ϣ
//            if (list != null) {
//                sb.append("\npoilist size = : ");
//                sb.append(list.size());
//                for (Poi p : list) {
//                    sb.append("\npoi= : ");
//                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//                }
//            }
//            logMsg(sb.toString());
//            Log.i("BaiduLocationApiDem", sb.toString());
		}// end onReceiveLocation()
		
	}
	
	public void putJson( JSONObject jsonParams ) {
		try {
			if ( mJsonObject != null ){
				mJsonObject = jsonParams;
			} else {
				Log.i("BaiduLocationApiDem", "inner null!!!!");
			}
		} catch (Exception e ) {
			e.printStackTrace();
		}
		
	}
	
	/**
     * ��ʾ�����ַ�
     * @param str
     */
    public void logMsg(String str) {
        try {
            if (mLocationResult != null){
                mLocationResult.setText(str);
            } else {
            	
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
