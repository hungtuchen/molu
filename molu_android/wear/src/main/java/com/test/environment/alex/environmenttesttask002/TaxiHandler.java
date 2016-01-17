package com.test.environment.alex.environmenttesttask002;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.semantic.EntityTagValue;
import com.mobvoi.android.semantic.SemanticIntentApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.MobvoiApiClient.OnConnectionFailedListener;
import com.mobvoi.android.common.data.FreezableUtils;
import com.mobvoi.android.location.LocationServices;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.DataMapItem;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

public class TaxiHandler extends Activity implements MobvoiApiClient.ConnectionCallbacks,
        MobvoiApiClient.OnConnectionFailedListener, MessageApi.MessageListener,
        NodeApi.NodeListener, DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = "TaxiHandler";

    private TextView mTextView;

    private String toStr = "";
    private String fromStr = "";

    private MobvoiApiClient mMobvoiApiClient;

    DelayedConfirmationView delayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_handler);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener(){

    @Override
    public void onLayoutInflated(WatchViewStub stub){
            mTextView=(TextView)stub.findViewById(R.id.text);
            mTextView.setText("正在处理中,请稍后……");
        delayedConfirmationView = (DelayedConfirmationView)
                findViewById(R.id.timer);
        delayedConfirmationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaxiHandler.this.finish();
            }
        });
            getTexiInfo();
            }
        });

        initMobvoiAPI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMobvoiApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   removeListener(mMobvoiApiClient, this);
        mMobvoiApiClient.disconnect();
    }

    private void initMobvoiAPI() {
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void getTexiInfo() {

        EntityTagValue from = SemanticIntentApi.extractAsEntity(getIntent(), "from");
        EntityTagValue to = SemanticIntentApi.extractAsEntity(getIntent(), "to");

        if ( from != null ) {
            if ( from.normData != null ) {
                fromStr = from.normData;
                Log.i(TAG, "from: " + fromStr );
            }
        }

        if ( to != null ) {
            if ( to.normData != null ) {
                toStr = to.normData;
                Log.i(TAG, "to: " + toStr );
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   addListener(mMobvoiApiClient, this);

        new StartWearableActivityTask().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private static final String ALL_TARGET_PATH = "/all-target";
    private static final String GET_UBER_PATH = "/get-uber";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i( TAG, "onMessageReeived: " + messageEvent );

        // Check to see if the message is to start an activity
        if ( messageEvent.getPath().equals(ALL_TARGET_PATH) ) {
            String allTarget = new String(messageEvent.getData());
            final String[] targets = allTarget.split("@");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String tmp = "人民优步\n";
                    tmp += "从   " + targets[0] + "\n";
                    tmp += "到   " + targets[1] + "\n";
                    mTextView.setText(tmp);
                    onStartTimer();
;                }
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

    public void onStartTimer() {
//        DelayedConfirmationView delayedConfirmationView = (DelayedConfirmationView)
//                findViewById(R.id.timer);
//        delayedConfirmationView.setVisibility( View.VISIBLE );
        delayedConfirmationView.setTotalTimeMs(3 * 1000);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        new getUberTask().execute();
    }

    @Override
    public void onTimerSelected(View view) {
        TaxiHandler.this.finish();
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Collection<String> nodes = getNodes();
            for ( String node : nodes ) {
                sendStartActivityMessage( node );
            }
            return null;
        }
    }

    private static final String START_ACTIVITY_PATH = "/start-activity";

//    private static final String TAXI_PATH = "/";

    private void sendStartActivityMessage( String node ) {
        String tmp = fromStr + "@";
        tmp += toStr;
        Log.i(TAG, "sending tmp:!!!" + tmp);
        Wearable.MessageApi.sendMessage(
                mMobvoiApiClient, node, START_ACTIVITY_PATH, tmp.getBytes())
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

    private class getUberTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Collection<String> nodes = getNodes();
            for ( String node : nodes ) {
                sendTargetMessage( node );
            }
            TaxiHandler.this.finish();
            return null;
        }
    }

    private void sendTargetMessage( String node ) {

        Wearable.MessageApi.sendMessage(
                mMobvoiApiClient, node, GET_UBER_PATH, new byte[0])
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

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes( mMobvoiApiClient ).await();
        for ( Node node : nodes.getNodes() ) {
            results.add( node.getId() );
        }

        return results;
    }
}
