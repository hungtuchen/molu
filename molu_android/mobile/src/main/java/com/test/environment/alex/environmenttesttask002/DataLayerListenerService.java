package com.test.environment.alex.environmenttesttask002;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.common.data.FreezableUtils;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;
import com.mobvoi.android.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;



/**
 * Created by Alex on 16/1/10.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListenerService";

    private static final String ALL_TARGET_PATH = "/all-target";
    private static final String GET_UBER_PATH = "/get-uber";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";
    private static final String COUNT_KEY = "count";
    private static final int MAX_LOG_TAG_LENGTH = 23;
    MobvoiApiClient mMobvoiApiClient;

    String toStr = "";
    String fromStr = "";

    String toLatStr = "";
    String toLonStr = "";

    String fromLatStr = "";
    String fromLonStr = "";

    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private LocationClient mLocationClient;
    JSONObject jsonParams;


    @Override
    public void onCreate() {
        super.onCreate();
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mMobvoiApiClient.connect();

        mLocationClient = ((LocationApplication)getApplication()).mLocationClient;
        initLocation();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        LOGI(TAG, "onDataChanged: " + dataEventBuffer);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        dataEventBuffer.close();
        if ( !mMobvoiApiClient.isConnected() ) {
            ConnectionResult connectionResult = mMobvoiApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if ( !connectionResult.isSuccess() ) {
                return;
            }
        }

        // Loop through the events and send a message back to the node that
        // create the data item
        for ( DataEvent event : events ) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if ( COUNT_PATH.equals(path) ) {
                // Get the node id of the node that created the data item
                // from the host portion of the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                Wearable.MessageApi.sendMessage(mMobvoiApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
                        payload);
            }
        }//end for
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//        super.onMessageReceived(messageEvent);
        LOGI(TAG, "onMessageReeived: " + messageEvent);

        // Check to see if the message is to start an activity
        if ( messageEvent.getPath().equals(START_ACTIVITY_PATH) ) {
//            Intent startIntent = new Intent(this, GeoCoderDemo.class);
//            startIntent.putExtra("param", new String(messageEvent.getData()));
//            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity( startIntent );

            String paramStr = new String ( messageEvent.getData() );

            if ( paramStr != null ) {
                Log.i("MEMEME", "paramStr:" + paramStr );
                if ( paramStr.startsWith("@") ){
                    //没有来源地址,使用本地地址
                    mLocationClient.start();
                    toStr = paramStr.substring(1);
                    Log.i("MEMEME", "toPosition:" + toStr );
                    getPositionMe();
                } else {
                    //有来源地址
                    fromStr = paramStr.substring(0, paramStr.indexOf("@"));
                    toStr   = paramStr.substring(paramStr.indexOf("@") + 1);
                    searchForTwoPoint();
                }
            }
        } else if ( messageEvent.getPath().equals(GET_UBER_PATH) ) {
            //do something to get Uber Car
            Intent startIntent = new Intent(this, GetUberActivity.class);
            startIntent.putExtra("end_latitude", toLatStr );
            startIntent.putExtra("end_longitude", toLonStr );
            startIntent.putExtra("start_latitude", fromLatStr );
            startIntent.putExtra("start_longitude", fromLonStr );
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity( startIntent );

            toStr = "";
            toLatStr = "";
            toLonStr = "";
            fromStr = "";
            fromLatStr = "";
            fromLonStr = "";
        }
    }

    @Override
    public void onPeerConnected(Node node) {
//        super.onPeerConnected(node);
        LOGI(TAG, "onPeerConnected: " + node);
    }

    @Override
    public void onPeerDisconnected(Node node) {
//        super.onPeerDisconnected(node);
        LOGI(TAG, "onPeeerDisconnected: " + node);
    }

    public static void LOGI( final  String tag, String message ) {
        Log.i(tag, message);
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
//                                    Log.e(TAG, "Fained to send message with status code: "
//                                            + sendMessageResult.getStatus().getStatusCode());
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

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes( mMobvoiApiClient ).await();
        for ( Node node : nodes.getNodes() ) {
            results.add( node.getId() );
        }

        return results;
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
                    if ( toStr == null ){
                        toStr = "西直门";
                    }
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
                    if ( result.getLocation() != null ) {
                        toLatStr = result.getLocation().latitude + "";
                        toLonStr = result.getLocation().longitude + "";
                    } else {
                        toStr    = "三里屯";
                        toLatStr = "39.941740";
                        toLonStr = "116.457916";
                    }

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

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
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


}
