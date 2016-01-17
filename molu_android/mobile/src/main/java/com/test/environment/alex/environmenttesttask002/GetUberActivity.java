package com.test.environment.alex.environmenttesttask002;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;
import com.people.lyy.activity.BaseActivity;
import com.people.lyy.client.TransferRequestTag;
import com.people.lyy.util.PhoneUtil;
import com.people.network.LKAsyncHttpResponseHandler;
import com.people.network.LKHttpRequest;
import com.people.network.LKHttpRequestQueue;
import com.people.network.LKHttpRequestQueueDone;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class GetUberActivity extends BaseActivity implements MobvoiApiClient.ConnectionCallbacks,
        MobvoiApiClient.OnConnectionFailedListener, MessageApi.MessageListener,
        NodeApi.NodeListener {

    LKHttpRequestQueue requestQueue;

    String start_latitude = "";
    String start_longitude = "";
    String end_latitude = "";
    String end_longitude = "";

    private MobvoiApiClient mMobvoiApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_uber);
        initMobvoiAPI();

        start_latitude = getIntent().getStringExtra("start_latitude");
        start_longitude = getIntent().getStringExtra("start_longitude");
        end_latitude = getIntent().getStringExtra("end_latitude");
        end_longitude = getIntent().getStringExtra("end_longitude");

        requestQueue = new LKHttpRequestQueue();

//        sendTest();

    }

    private void sendTest(){
        HashMap<String, Object> tmpMap = new HashMap<String, Object>();
        tmpMap.put("token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZXMiOlsicmVxdWVzdCJdLCJzdWIiOiJlYTZiNDkyMy1jMGQ3LTQ2ODktOTlkNS0wMTYyZDQzYWZjNWQiLCJpc3MiOiJ1YmVyLWNuMSIsImp0aSI6IjNjZjNkODY3LTI4ZWYtNGYxZi04YzFkLTYyYzBkYzVjNWZjOSIsImV4cCI6MTQ1NTUyNTg5OSwiaWF0IjoxNDUyOTMzODk4LCJ1YWN0IjoiS09KVUFwYUZOVDViSWRMa1F5cE1jZzRMRUp4U0R2IiwibmJmIjoxNDUyOTMzODA4LCJhdWQiOiJxUTVhYW5VQ2hpb3ZaUUlhTlBnX3c0aVZLa1l4eThoZiJ9.hQmRy4RuyXIkbEz2pMmMfckxRk2-tfleAmkx8hoirvUQ1t3GYiJN82sXHauypSBf5qoSI8tAhFN-8ufHLIfYI1PttiEAIgJP5O4Jxlg7BcqG4rIcdKxxmmLKdKuk0sW7KA5A3MydChAkhyXdZ8iHYaw4ncjB7Sf97g2-2p-8sP0RfSwVcZ_FSMt9p7FHbUN5Ri7ktK_kv6sAYIq7Mi63d78M9mKi9rG-aPHeq5PYYKsw1KS8AawYQ_M5jX23i5f3ke6yx4vxUcUH-h5_zW9frvYzcaYo5jMVIJvYasbwlE9bUAUVSVWqhI4thvDb37iJ0cPAS4PocXnWiDyD-sOtNQ");
        tmpMap.put("hardId", PhoneUtil.getIMEI(this));
        tmpMap.put("start_latitude", "40.05215");
        tmpMap.put("start_longitude", "116.293571");
        tmpMap.put("end_latitude", "39.947458");
        tmpMap.put("end_longitude", "116.359758");
        LKHttpRequest loginReq = new LKHttpRequest(TransferRequestTag.Login, tmpMap, getLoginHandler() );
        requestQueue.addHttpRequest( loginReq ).executeQueue("正在登录请稍候...", new LKHttpRequestQueueDone() {
            @Override
            public void onComplete() {
                // TODO Auto-generated method stub
                super.onComplete();
            }
        });
    }

    private LKAsyncHttpResponseHandler getLoginHandler() {
        return new LKAsyncHttpResponseHandler() {

            @Override
            public void successAction(Object obj) {
                // TODO Auto-generated method stub
                HashMap<String, String> map = (HashMap<String, String>) obj;
            }
        };
    }

    private void initMobvoiAPI() {
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   addListener(mMobvoiApiClient, this);

        new sendAllTargetTask().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

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

    private static final String DRIVER_DETAIL_PATH = "driver-detail";
    private static final String TAG = "GetUberActivity";

    private void sendDRIVER_DETAIL_Message( String node ) {
        Wearable.MessageApi.sendMessage(
                mMobvoiApiClient, node, DRIVER_DETAIL_PATH, new byte[0])
                .setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Fained to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                }
                                GetUberActivity.this.finish();
                            }
                        }
                );
    }

    private class sendAllTargetTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Collection<String> nodes = getNodes();
            for ( String node : nodes ) {
                sendDRIVER_DETAIL_Message( node );
            }
            return null;
        }
    }
}
