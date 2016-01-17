package com.test.environment.alex.environmenttesttask002;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.data.FreezableUtils;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.Wearable;
import com.mobvoi.android.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alex on 16/1/10.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListenerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    private static final String DRIVER_DETAIL_PATH = "driver-detail";
    public static final String COUNT_PATH = "/count";
    public static final String IMAGE_PATH = "/image";
    public static final String IMAGE_KEY = "photo";
    private static final String COUNT_KEY = "count";
    private static final int MAX_LOG_TAG_LENGTH = 23;
    MobvoiApiClient mMobvoiApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mMobvoiApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        LOGI( TAG, "onDataChanged: " + dataEventBuffer );
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
        LOGI( TAG, "onMessageReeived: " + messageEvent );

        // Check to see if the message is to start an activity
        if ( messageEvent.getPath().equals(START_ACTIVITY_PATH) ) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity( startIntent );
        } else if ( messageEvent.getPath().equals(DRIVER_DETAIL_PATH) ) {
            Intent startIntent = new Intent(this, TrueDetail.class);
            startIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity( startIntent );
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
        LOGI(TAG, "onPeeerDisconnected: " + node );
    }

    public static void LOGI( final  String tag, String message ) {
        Log.i( tag, message );
    }
}
