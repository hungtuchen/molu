package com.test.environment.alex.environmenttesttask002;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


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

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener{


    private static final String TAG = "MainActivity";

    private MobvoiApiClient mMobvoiApiClient;
    private DataItemAdapter mDataItemListAdapter;
    private ListView mDataItemList;
    private TextView mIntroText;
    private View     mLayout;
    private Handler  mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mDataItemList = (ListView) findViewById( R.id.dataItem_list );
                mIntroText = (TextView) findViewById( R.id.intro );
                mLayout = findViewById( R.id.layout );
                mDataItemListAdapter = new DataItemAdapter(MainActivity.this, android.R.layout.simple_list_item_1);
                mDataItemList.setAdapter( mDataItemListAdapter );
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
        Wearable.DataApi.   removeListener(mMobvoiApiClient, this);
        Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   removeListener(mMobvoiApiClient, this);
        mMobvoiApiClient.disconnect();
    }

    private void initMobvoiAPI(){
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.   addListener(mMobvoiApiClient, this);
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   addListener(mMobvoiApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // do nothing?? should do something???
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + connectionResult);
    }

    private void generateEvent( final String title, final String text ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIntroText.setVisibility(View.INVISIBLE);
                mDataItemListAdapter.add(new Event(title, text));
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.i(TAG, "onDataChanged(): " + dataEventBuffer);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        dataEventBuffer.close();
        for ( DataEvent event : events ) {
            if ( event.getType() == DataEvent.TYPE_CHANGED ) {
                String path = event.getDataItem().getUri().getPath();
                if ( DataLayerListenerService.IMAGE_PATH.equals(path) ) {
                    //recvive photo
                    Log.i(TAG, "Data changed for IMAGE_PATH");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem( event.getDataItem() );
                    Asset photo = dataMapItem.getDataMap()
                            .getAsset(DataLayerListenerService.IMAGE_KEY);
                    final Bitmap bitmap = loadBitmapFromAsset(mMobvoiApiClient, photo);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Setting background image..");
                            mLayout.setBackground( new BitmapDrawable(getResources(), bitmap));
                        }
                    });
                } else if ( DataLayerListenerService.COUNT_PATH.equals(path) ) {
                    Log.i(TAG, "Data changed for COUNT_PATH");
                    generateEvent("DataItem Changed", event.getDataItem().toString());
                } else {
                    Log.i(TAG, "Unrecognized path: " + path );
                }
            } else if ( event.getType() == DataEvent.TYPE_DELETED ) {
                generateEvent("DataItem Deleted", event.getDataItem().toString());
            } else {
                generateEvent("Unknown data event type", "Type = " + event.getType());
            }
        }
    }

    private Bitmap loadBitmapFromAsset( MobvoiApiClient apiClient, Asset asset ) {
        if ( asset == null ) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                apiClient, asset
        ).await().getInputStream();

        if ( assetInputStream == null ) {
            Log.w(TAG, "Requested an unknow Asset");
            return null;
        }
        return BitmapFactory.decodeStream( assetInputStream );
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i( TAG, "onMessageReceived: " + messageEvent );
        generateEvent("Message", messageEvent.toString() );
    }

    @Override
    public void onPeerConnected(Node node) {
        generateEvent("Node Connected", node.getId());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        generateEvent("Node Disconnected", node.getId());
    }

    private static class DataItemAdapter extends ArrayAdapter<Event> {

        private final Context mContext;

        public DataItemAdapter(Context context, int unusedResource) {
            super(context, unusedResource);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.two_line_list_item, null);
                convertView.setTag(holder);
                holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
                holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Event event = getItem(position);
            holder.text1.setText(event.title);
            holder.text2.setText(event.text);
            return convertView;
        }

        private class ViewHolder {

            TextView text1;
            TextView text2;
        }
    }

    private class Event {

        String title;
        String text;

        public Event(String title, String text) {
            this.title = title;
            this.text = text;
        }
    }

}
