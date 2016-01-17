package com.test.environment.alex.environmenttesttask002;

import org.json.JSONException;
import org.json.JSONObject;
import org.netpicture.ImageService;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * 此demo用来展示如何进行地理编码搜索（用地址检索坐标）、反地理编码搜索（用坐标检索地址）
 */
public class GeoCoderDemo extends Activity implements
		OnGetGeoCoderResultListener, MobvoiApiClient.ConnectionCallbacks,
		MobvoiApiClient.OnConnectionFailedListener, MessageApi.MessageListener,
		NodeApi.NodeListener {
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private LocationClient mLocationClient;
	EditText lat;
	EditText lon;
	
	EditText editCity;
	EditText editGeoCodeKey;
	
	JSONObject jsonParams;

	String toStr = "";
	String fromStr = "";

	String toLatStr = "";
	String toLonStr = "";

	String fromLatStr = "";
	String fromLonStr = "";


	ImageView staticImage;

	private MobvoiApiClient mMobvoiApiClient;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geocoder);

		initMobvoiAPI();

		staticImage = (ImageView)findViewById( R.id.staticImage );


		mLocationClient = ((LocationApplication)getApplication()).mLocationClient;
		initLocation();
		mLocationClient.start();

		CharSequence titleLable = "地理编码功能";
		setTitle(titleLable);
		Log.i("geo", "oncreate");

		lat = (EditText) findViewById(R.id.lat);
		lon = (EditText) findViewById(R.id.lon);

		editCity = (EditText) findViewById(R.id.city);
		editGeoCodeKey = (EditText) findViewById(R.id.geocodekey);

		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

		getLocationMethod();

	}

	private void getLocationMethod() {

		String paramStr = getIntent().getStringExtra("param");
		if ( paramStr != null ) {
			if ( paramStr.startsWith("@") ){
				//没有来源地址,使用本地地址
				toStr = paramStr.substring(1);
				getPositionMe();
			} else {
				//有来源地址
				fromStr = paramStr.substring(0, paramStr.indexOf("@"));
				toStr   = paramStr.substring(paramStr.indexOf("@") + 1);
				searchForTwoPoint();
			}
		}
	}

	private void handleAllAddress(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean isLoop = true;
				while( isLoop ){

					try{
						Thread.sleep(1000);
					}catch( java.lang.InterruptedException e ) {
						e.printStackTrace();
					}
					Log.i("CEO", "fromStr:" + fromStr);
					Log.i("CEO", "fromLatStr:" + fromLatStr);
					Log.i("CEO", "fromLonStr:" + fromLonStr);
					Log.i("CEO", "toStr:" + toStr);
					Log.i("CEO", "toLatStr:" + toLatStr);
					Log.i("CEO", "toLonStr:" + toLonStr);
					if ( !fromLonStr.equals("") && !fromLatStr.equals("") &&
							!fromLonStr.equals("") && !toStr.equals("") &&
							!toLatStr.equals("") && !toLonStr.equals("")   ){
						isLoop = false;
						new sendAllTargetTask().execute();
					}

				}

			}
		}).start();
	}


	private void searchForOnePoint() {
		//查询目标地址的坐标
//		mSearch = GeoCoder.newInstance().newInstance();
		mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result != null) {
					toLatStr = result.getLocation().latitude + "";
					toLonStr = result.getLocation().longitude + "";
				}
			}

			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

			}
		});
		mSearch.geocode(new GeoCodeOption().city(
				"北京").address(
				toStr));
		handleAllAddress();
	}

	private void searchForTwoPoint() {
		//查询来回地址的坐标
//		mSearch = GeoCoder.newInstance().newInstance();
		mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result != null) {
					toLatStr = result.getLocation().latitude + "";
					toLonStr = result.getLocation().longitude + "";
				}
			}

			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

			}
		});
		mSearch.geocode(new GeoCodeOption().city(
				"北京").address(
				toStr));

//		mSearch = GeoCoder.newInstance().newInstance();
		mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result != null) {
					fromLatStr = result.getLocation().latitude + "";
					fromLonStr = result.getLocation().longitude + "";
				}
			}

			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

			}
		});
		mSearch.geocode(new GeoCodeOption().city(
				"北京").address(
				fromStr));

		handleAllAddress();
	}

	private void getPositionMe(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				jsonParams = ((LocationApplication)getApplication()).getLocationJson();
				Log.i("Geo", "going to while");
				while( jsonParams == null ) {
					try {
						Log.i("Geo", "null");
						Thread.sleep(1000);
						jsonParams = ((LocationApplication)getApplication()).getLocationJson();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Log.i("Geo", "Get One!!!!!!");
				try {
					fromStr = jsonParams.getString("locationdescribe");
					fromLatStr = jsonParams.getString("latitude");
					fromLonStr = jsonParams.getString("longtitude");
					searchForOnePoint();
				} catch ( JSONException e){
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 发起搜索
	 * 
	 * @param v
	 */
	public void SearchButtonProcess(View v) {
//		if (v.getId() == R.id.reversegeocode) {
//			LatLng ptCenter = new LatLng((Float.valueOf(lat.getText()
//					.toString())), (Float.valueOf(lon.getText().toString())));
//			// 反Geo搜索
//			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
//					.location(ptCenter));
//		} else if (v.getId() == R.id.geocode) {
//			// Geo搜索
//			mSearch.geocode(new GeoCodeOption().city(
//					editCity.getText().toString()).address(
//					editGeoCodeKey.getText().toString()));
//		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
		Wearable.NodeApi.   removeListener(mMobvoiApiClient, this);
		mMobvoiApiClient.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMobvoiApiClient.connect();
	}

	@Override
	protected void onDestroy() {
		mLocationClient.stop();
		mSearch.destroy();
		super.onDestroy();
	}
	
	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode( LocationMode.Hight_Accuracy );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType( "gcj02" );//可选，默认gcj02，设置返回的定位结果坐标系，
		option.setScanSpan( 1000 );//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setIsNeedAddress( true );//可选，设置是否需要地址信息，默认不需要
		option.setOpenGps(true);//可选，默认false,设置是否使用gps
		option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		mLocationClient.setLocOption(option);
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		String strInfo = String.format("纬度：%f 经度：%f",
				result.getLocation().latitude, result.getLocation().longitude);
		Toast.makeText(GeoCoderDemo.this, strInfo, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(GeoCoderDemo.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		Toast.makeText(GeoCoderDemo.this, result.getAddress(),
				Toast.LENGTH_LONG).show();

	}

	private void initMobvoiAPI() {
		mMobvoiApiClient = new MobvoiApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
	}

	@Override
	public void onConnected(Bundle bundle) {
		Wearable.MessageApi.addListener(mMobvoiApiClient, this);
		Wearable.NodeApi.   addListener(mMobvoiApiClient, this);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	private static final String TAG = "GeoCoderDemo";
	private static final String ALL_TARGET_PATH = "/all-target";
	private static final String GET_UBER_PATH = "/get-uber";
	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		Log.i( TAG, "onMessageReeived: " + messageEvent );

		// Check to see if the message is to start an activity
		if ( messageEvent.getPath().equals(GET_UBER_PATH) ) {
			//do something to get Uber Car
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder( GeoCoderDemo.this )
							.setTitle("Uber")
							.setMessage("Get the Car NOW!!!")
							.show();
				}
			});
		}
	}

	@Override
	public void onPeerConnected(Node node) {

	}

	@Override
	public void onPeerDisconnected(Node node) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	private Collection<String> getNodes() {
		HashSet<String> results = new HashSet<String>();
		NodeApi.GetConnectedNodesResult nodes =
				Wearable.NodeApi.getConnectedNodes( mMobvoiApiClient ).await();
		for ( Node node : nodes.getNodes() ) {
			results.add( node.getId() );
		}

		return results;
	}

	private void sendALL_TARGET_Message( String node ) {
		String tmp = fromStr + "@";
		tmp += toStr;
		Wearable.MessageApi.sendMessage(
				mMobvoiApiClient, node, ALL_TARGET_PATH, tmp.getBytes())
				.setResultCallback(
						new ResultCallback<MessageApi.SendMessageResult>() {
							@Override
							public void onResult(MessageApi.SendMessageResult sendMessageResult) {
								if (!sendMessageResult.getStatus().isSuccess()) {
									Log.e(TAG, "Fained to send message with status code: "
											+ sendMessageResult.getStatus().getStatusCode());
								}
							}
						}
				);
	}

	private class sendAllTargetTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			Collection<String> nodes = getNodes();
			for ( String node : nodes ) {
				sendALL_TARGET_Message( node );
			}
			return null;
		}
	}
}
