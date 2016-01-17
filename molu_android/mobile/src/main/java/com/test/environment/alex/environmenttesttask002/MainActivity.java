package com.test.environment.alex.environmenttesttask002;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.MobvoiApiClient.OnConnectionFailedListener;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.common.data.FreezableUtils;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi;
import com.mobvoi.android.wearable.DataApi.DataItemResult;
import com.mobvoi.android.wearable.DataEvent;
import com.mobvoi.android.wearable.DataEventBuffer;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageApi.SendMessageResult;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.PutDataMapRequest;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener,
        ConnectionCallbacks, OnConnectionFailedListener, MessageApi.MessageListener, NodeApi.NodeListener
{
    private static final String TAG = "MainActivity";

    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String COUNT_PATH = "/count";
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_KEY = "photo";
    private static final String COUNT_KEY = "count";

    private MobvoiApiClient mMobvoiApiClient;

    //用于表示当前错误处理的状态
    private boolean mResolvingError = false;
    private boolean mCameraSupported = false;

    private ListView mDataItemList;
    private Button mTakePhotoBtn;
    private Button mSendPhotoBtn;
    private ImageView mThumbView;
    private View mStartActivityBtn;
    private Bitmap mImageBitmap;

    private DataItemAdapter mDataItemListAdapter;
    private android.os.Handler mHandler;

    private ScheduledExecutorService mGeneratorExecutor;
    private ScheduledFuture<?>       mDataItemGeneratorFuture;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mCameraSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        setContentView(R.layout.activity_main);
        setupView();
        mDataItemListAdapter = new DataItemAdapter(this, android.R.layout.simple_list_item_1);
        mDataItemList.setAdapter( mDataItemListAdapter);

        mGeneratorExecutor = new ScheduledThreadPoolExecutor(1);

        initMobvoiAPI();
    }

    private void setupView() {
        mDataItemList = (ListView) findViewById(R.id.data_item_list);
        mTakePhotoBtn = (Button) findViewById(R.id.takePhoto);
        mSendPhotoBtn = (Button) findViewById(R.id.sendPhoto);
        mThumbView    = (ImageView) findViewById( R.id.imageView);
        mStartActivityBtn = findViewById(R.id.start_wearable_activity);

        String paramStr = getIntent().getStringExtra("param");
        if ( paramStr != null ) {
            mSendPhotoBtn.setText( paramStr );
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if ( !mResolvingError ) {
            mMobvoiApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataItemGeneratorFuture = mGeneratorExecutor.scheduleWithFixedDelay(
                new DataItemGenerator(), 1, 5, TimeUnit.SECONDS
        );

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataItemGeneratorFuture.cancel(true );
    }

    @Override
    protected void onStop() {
        if ( !mResolvingError ) {
            Wearable.DataApi.   removeListener( mMobvoiApiClient, this );
            Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
            Wearable.NodeApi.   removeListener(mMobvoiApiClient, this);
            mMobvoiApiClient.disconnect();
        }
        super.onStop();
    }

    private void initMobvoiAPI(){
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        LOGD(TAG, "onDataChanged: " + dataEventBuffer );
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        dataEventBuffer.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DataEvent event : events) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        mDataItemListAdapter.add(
                                new Event("DataItem Changed", event.getDataItem().toString())
                        );
                    } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                        mDataItemListAdapter.add(
                                new Event("DataItem Deleted", event.getDataItem().toString())
                        );
                    }
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        mResolvingError = false;
        mStartActivityBtn.setEnabled(true);
        mSendPhotoBtn.setEnabled( mCameraSupported );
        Wearable.DataApi.   addListener(mMobvoiApiClient, this);
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.   addListener(mMobvoiApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //手表与手机之间的连接被打断,在这个时候应该在UI上做某些处理
        //比如Disable某些控件等
        mStartActivityBtn.setEnabled(false);
        mSendPhotoBtn.setEnabled( false );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if ( mResolvingError ) {
            // Already attempting to resolve an error.
            return;
        } else if ( connectionResult.hasResolution() ) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR );
            } catch ( IntentSender.SendIntentException e ) {
                mMobvoiApiClient.connect();
            }
        } else {
            mResolvingError = false;
            //disable views
            mStartActivityBtn.setEnabled( false );
            mSendPhotoBtn.setEnabled( false );
            Wearable.DataApi.   removeListener(mMobvoiApiClient, this);
            Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
            Wearable.NodeApi.   removeListener(mMobvoiApiClient, this);
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        LOGD( TAG, "onMessageReceived() A message from watch was received:" +
                messageEvent.getRequestId() + " " + messageEvent.getPath() );
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataItemListAdapter.add(new Event("Message from watch", messageEvent.toString()));
            }
        });
    }

    /**
     * 发送Message需要Nodeid,所以在创建Message前首先要实现nodesAPI
     * @param view
     */
    public void onStartWearableActivityClick( View view ) {
        LOGD( TAG, "Generating RPC" );
        new StartWearableActivityTask().execute();
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Collection<String> nodes = getNodes();
            for ( String node : nodes ) {
                sendStartActivityMessage( node );
            }
            return null;
        }
    }

    private void sendStartActivityMessage( String node ) {
        Wearable.MessageApi.sendMessage(
                mMobvoiApiClient, node, START_ACTIVITY_PATH, new byte[0])
                .setResultCallback(
                        new ResultCallback<SendMessageResult>() {
                            @Override
                            public void onResult(SendMessageResult sendMessageResult) {
                                if ( !sendMessageResult.getStatus().isSuccess() ) {
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

    @Override
    public void onPeerConnected(final Node node) {
        LOGD( TAG, "onPeerConnected: " + node );
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataItemListAdapter.add(
                        new Event("Connected", node.toString())
                );
            }
        });
    }

    @Override
    public void onPeerDisconnected(final Node node) {
        LOGD( TAG, "onPeerDisconnected: " + node );
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataItemListAdapter.add(
                        new Event("Disconnected", node.toString())
                );
            }
        });
    }

    /**
     * Dispatches an {@link Intent} to take a photo. Result will be returned back
     * in onActivityResult().
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ( takePictureIntent.resolveActivity(getPackageManager()) != null ) {
            startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mThumbView.setImageBitmap(mImageBitmap);
        }
    }

    private static Asset toAsset( Bitmap bitmap ) {
        ByteArrayOutputStream byteScream = null;
        try{
            byteScream = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.PNG, 100, byteScream );
            return Asset.createFromBytes( byteScream.toByteArray() );
        } finally {
            if ( null != byteScream ){
                try {
                    byteScream.close();;
                } catch ( IOException e ) {
                    //ignor
                }
            }
        }
    }

    private void sendPhoto( Asset asset ) {
        PutDataMapRequest dataMap = PutDataMapRequest.create( IMAGE_PATH );
        dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
        dataMap.getDataMap().putLong("time", new java.util.Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem( mMobvoiApiClient, request )
                .setResultCallback(
                        new ResultCallback<DataItemResult>() {
                            @Override
                            public void onResult(DataItemResult dataItemResult) {
                                LOGD(TAG, "Sending image was successful: "
                                        + dataItemResult.getStatus().isSuccess());
                            }
                        }
                );
    }

    public void onTakePhotoClick( View view ) {
        dispatchTakePictureIntent();
    }

    public void onSendPhotoClick( View view ) {
        if ( null != mImageBitmap && mMobvoiApiClient.isConnected() ) {
            sendPhoto( toAsset(mImageBitmap) );
        }
    }

    private static class DataItemAdapter extends ArrayAdapter<Event> {
        private final Context mContext;

        public DataItemAdapter( Context context, int unusedResource ) {
            super( context, unusedResource );
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if ( convertView == null ) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE
                );
                convertView = inflater.inflate( android.R.layout.two_line_list_item, null );
                convertView.setTag( holder );
                holder.text1 = (TextView) convertView.findViewById( android.R.id.text1 );
                holder.text2 = (TextView) convertView.findViewById( android.R.id.text2 );
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Event event = getItem( position );
            holder.text1.setText( event.title );
            holder.text2.setText( event.text  );
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

    private class DataItemGenerator implements  Runnable {
        private int count = 0;

        @Override
        public void run() {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create( COUNT_PATH );
            putDataMapRequest.getDataMap().putInt( COUNT_KEY, count++ );
            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            LOGD( TAG, "Generating DataItem: " + request);
            if ( !mMobvoiApiClient.isConnected() ) {
                return;
            }
            Wearable.DataApi.putDataItem( mMobvoiApiClient, request)
                    .setResultCallback(new ResultCallback<DataItemResult>() {
                        @Override
                        public void onResult(DataItemResult dataItemResult) {
                            if ( !dataItemResult.getStatus().isSuccess() ) {
                                Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                            + dataItemResult.getStatus().getStatusCode());
                            }
                        }
                    });
        }
    }

    private static void LOGD( final String tag, String message ) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d( tag, message );
        }
    }
}
