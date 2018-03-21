package com.harman.ovrsclient;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getName();

    private static final String SERVICE_TYPE = "_http._tcp.";

    NsdManager mNsdManager;

    ServerListAdapter mServerAdapter = null;

    RecyclerView mRecyclerView = null;

    Button mSearchServers = null;

    OvrsClientCmdClient mWebSocketClient = null;

    Button mCallJoinEndButton = null;

    FrameLayout mCallLayout = null;

    FrameLayout mSelfSurfaceView = null;

    private android.hardware.Camera mCamera;

    MyCameraView myCameraView = null;

    RecyclerView mParticioantsRCV = null;

    CallLayoutAdapter mCallAdapter = null;


    final int NEW_ENTRY = 0;
    final int EXISTING_ENTRY = 1;
    final int DEL_ENTRY = 2;
    final int VIDEO = 3;
    final int NO_ROOM = -1;

    final String CMD_KEY = "CMD";
    final String FROM_KEY = "FROM";
    final String VFRAME_KEY = "VFRAME";
    final String ADDR_KEY = "ADDR";
    final String DEL_ENTRY_KEY = "DEL_ENTRY";
    final String EXISTING_ENTRY_KEY = "EXISTING_ENTRY";
    final String ENTRIES_KEY = "ENTRIES";
    final String NEW_ENTRY_KEY = "NEW_ENTRY";

    SenderThread senderThread = null;

    HashMap<String, byte[]> mInComingDataHolder;
    HashMap<String, ReceiverThread> mReceiverThreads;


   Messenger mMessenger = new Messenger(new Handler(){

       @Override
       public void handleMessage(Message msg) {
           try {
               JSONObject data = null;
               String IP = null;
               ReceiverThread thread = null;
               switch (msg.what) {
                   case NEW_ENTRY:
                       data = (JSONObject) msg.obj;
                       IP = data.getString(ADDR_KEY);
                       mCallAdapter.addParticipant(IP);
                       mInComingDataHolder.put(IP, null);
                       thread = new ReceiverThread(IP);
                       mReceiverThreads.put(IP, thread);
                       thread.setName(IP);
                       thread.start();
                       break;
                   case EXISTING_ENTRY:
                       data = (JSONObject)msg.obj;
                       JSONArray entries = data.getJSONArray(ENTRIES_KEY);
                       for (int i = 0; i < entries.length(); i++){
                           IP = entries.getString(i);
                           mCallAdapter.addParticipant(IP);
                           mInComingDataHolder.put(IP, null);
                           thread = new ReceiverThread(IP);
                           mReceiverThreads.put(IP, thread);
                           thread.setName(IP);
                           thread.start();
                       }
                       break;
                   case DEL_ENTRY:
                       data = (JSONObject)msg.obj;
                       IP = data.getString(ADDR_KEY);
                       mCallAdapter.removeParticipant(IP);
                       mInComingDataHolder.remove(IP);
                       mReceiverThreads.get(IP).isClientOpen = false;
                       mReceiverThreads.remove(IP);
                       break;
                   case VIDEO:
                       data = (JSONObject)msg.obj;
                       String inData = data.getString(VFRAME_KEY);
                       if (null != inData && inData.length() > 0) {
                           mInComingDataHolder.put(data.getString(FROM_KEY),inData.getBytes());
                       }
                       break;
                   case NO_ROOM:
                       if (null != mWebSocketClient) {
                           Toast.makeText(getApplicationContext(), "NO ROOM", Toast.LENGTH_LONG).show();
                           mWebSocketClient.close();
                           mWebSocketClient = null;
                       }
                       break;

               }
           }
           catch (Exception ee){
               Log.e(TAG, ee.getMessage());
           }
       }
   });


    OvrsCmdInterfaceCallbacks mCallBackHandler = new OvrsCmdInterfaceCallbacks() {
        @Override
        public void onServerConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connected to server", Toast.LENGTH_LONG).show();
                    startCall();
                    mCamera = getCameraInstance();
                    if (null != mCamera){
                        myCameraView = new MyCameraView(getApplicationContext(), mCamera);
                        mSelfSurfaceView.addView(myCameraView);
                        senderThread = new SenderThread();
                        mInComingDataHolder= new HashMap<>();

                        mReceiverThreads = new HashMap<>();
                        senderThread.start();

                    }


                }
            });
        }

        @Override
        public void onServerClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connection Closed", Toast.LENGTH_LONG).show();
                    senderThread = null;
                    mInComingDataHolder.clear();
                    mReceiverThreads.clear();
                    mInComingDataHolder = null;
                    mReceiverThreads = null;

                }
            });
        }

        @Override
        public void onNewParticipantAdded(ParticipantHolder participant) {
            mCallAdapter.addParticipant(participant.gethostaddress());
        }

        @Override
        public void onParticipantExit(ParticipantHolder participant) {
            mCallAdapter.removeParticipant(participant.gethostaddress());
        }

        @Override
        public void onVideoFrameReceived(ParticipantHolder participant, VideoFrameHeader frame) {

        }

        @Override
        public void onMessage(String s) {
            //Log.d("OnMessage", s);
            try {
                Message msg = Message.obtain();
                JSONObject data = new JSONObject(s);
                msg.what = data.getInt(CMD_KEY);
                msg.obj = data;
                mMessenger.send(msg);
            }
            catch (Exception ee){
                Log.e(TAG, ee.getMessage());
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNsdManager = (NsdManager)getSystemService(Context.NSD_SERVICE);


        mRecyclerView = findViewById(R.id.rcv_server_hosts);
        mSearchServers = findViewById(R.id.btn_search_hosts);

        mServerAdapter = new ServerListAdapter(this, mServerSelectedInterface);

        LinearLayoutManager lytManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lytManager);
        mRecyclerView.setAdapter(mServerAdapter);

        mCallJoinEndButton = findViewById(R.id.btn_join_video_call);
        mCallLayout = findViewById(R.id.call_layout);

        mSelfSurfaceView = findViewById(R.id.surface_view_self);

        mCallJoinEndButton.setOnClickListener(this);

        mSearchServers.setOnClickListener(this);

        mParticioantsRCV = findViewById(R.id.rcv_call_layout);
        GridLayoutManager gridLytManager = new GridLayoutManager(this, 2);
        mParticioantsRCV.setHasFixedSize(true);
        mParticioantsRCV.setLayoutManager(gridLytManager);
        mCallAdapter = new CallLayoutAdapter(this);
        mParticioantsRCV.setAdapter(mCallAdapter);







    }

    @Override
    protected void onDestroy() {
        if (null != mWebSocketClient){
            mWebSocketClient.close();
            mNsdManager = null;
        }
        super.onDestroy();
    }

    ServerListAdapter.ServerSelected mServerSelectedInterface = new ServerListAdapter.ServerSelected() {
        @Override
        public void onServerSelected(int pos) {
            if (null == mWebSocketClient) {
                ParticipantHolder holder = mServerAdapter.getSelectedServerFromPos(pos);
                String uri = "ws://" + holder.gethostaddress() + ":" + holder.gethostport();
                mWebSocketClient = new OvrsClientCmdClient(URI.create(uri), mCallBackHandler);
                mWebSocketClient.connect();
                mCallAdapter.removeAll();
            }
            else{


            }

        }
    };

    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String s, int i) {
            Log.d(TAG, "onStartDiscoveryFailed"+s+" "+i);
            mSearchServers.setEnabled(true);
        }

        @Override
        public void onStopDiscoveryFailed(String s, int i) {
            Log.d(TAG, "onStopDiscoveryFailed");
            mSearchServers.setEnabled(true);
        }

        @Override
        public void onDiscoveryStarted(String s) {
            Log.d(TAG, "onDiscoveryStarted");
        }

        @Override
        public void onDiscoveryStopped(String s) {
            Log.d(TAG, "onDiscoveryStopped");
        }

        @Override
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            Log.d(TAG, "onServiceFound "+nsdServiceInfo.getServiceName());
            if(nsdServiceInfo.getServiceName().contains("OVRS_SERVER")){
                Log.d(TAG, "Network discovery service found the server "+nsdServiceInfo.getServiceName());
                mNsdManager.resolveService(nsdServiceInfo, resolver);
                mNsdManager.stopServiceDiscovery(discoveryListener);
            }

        }

        @Override
        public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
            mSearchServers.setEnabled(true);
        }
    };

    private NsdManager.ResolveListener resolver = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.d(TAG, "onResolveFailed");
            mSearchServers.setEnabled(true);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            Log.d(TAG, "onServiceResolved");
            final int port = nsdServiceInfo.getPort();
            final InetAddress host = nsdServiceInfo.getHost();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mServerAdapter.addNewFoundServer(""+port, host.getHostAddress());
                }
            });



        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_join_video_call:
                if (mCallJoinEndButton.getText().toString().equalsIgnoreCase("End Call")){
                    releaseCameraAndPreview();
                    closeServerConnection();
                    endCall();
                }
                break;
            case R.id.btn_search_hosts:
                mServerAdapter.clearAll();
                mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                mSearchServers.setEnabled(false);
                break;

        }
    }

    public void endCall(){
        mInComingDataHolder.clear();
        mReceiverThreads.clear();
        mCallAdapter.removeAll();
        mRecyclerView.setVisibility(View.VISIBLE);
        mServerAdapter.clearAll();
        mCallLayout.setVisibility(View.GONE);
        mCallJoinEndButton.setText("End Call");
        mSearchServers.setVisibility(View.VISIBLE);
        mSearchServers.setEnabled(true);
    }

    public void startCall(){
        mRecyclerView.setVisibility(View.GONE);
        mCallLayout.setVisibility(View.VISIBLE);
        mCallJoinEndButton.setText("End Call");
        mSearchServers.setVisibility(View.GONE);
    }

    public void closeServerConnection(){
        mWebSocketClient.close();
        mWebSocketClient = null;
    }

    public android.hardware.Camera getCameraInstance(){
        android.hardware.Camera cm = null;
        try{
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
            }
            releaseCameraAndPreview();
            cm = android.hardware.Camera.open();
        }
        catch (Exception ee){
            Log.e("getCameraInstance", ee.getMessage());
        }
        return cm;
    }

    public void releaseCameraAndPreview(){
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mSelfSurfaceView.removeView(myCameraView);
                myCameraView.getHolder().removeCallback(myCameraView);
                myCameraView = null;
                mCamera.release();
                mCamera = null;

            }
        }
        catch (Exception ee){
            Log.e("releaseCameraAndPreview", ee.getMessage());
        }
    }

    class SenderThread extends Thread{
        @Override
        public void run() {
            while(null != mWebSocketClient && mWebSocketClient.isOpen()){
                try {
                    ByteArrayOutputStream bos = myCameraView.getCameraView();
                    if (null != bos) {
                        mWebSocketClient.send(bos.toString());
                        sleep(1000 / 12);
                    }
                }
                catch (Exception ee){
                    Log.e("ServerThread", ee.getMessage());
                }
            }
        }
    }

    class ReceiverThread extends Thread{


        private String IPAddress;
        private ImageView mRemotePreview;
        private boolean isClientOpen = false;
        private BitmapFactory.Options bitmap_options = new BitmapFactory.Options();

        public ReceiverThread(String IP){
            IPAddress = IP;
            isClientOpen = true;
            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        }



        @Override
        public void run() {
            while (isClientOpen){
                try{
                    byte[] data = mInComingDataHolder.get(IPAddress);
                    if (null != data){
                        final Bitmap bData = BitmapFactory.decodeByteArray(data, 0, data.length, bitmap_options);
                        if (null == mRemotePreview){
                           mRemotePreview =  mCallAdapter.getParticipantImageView(IPAddress);
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRemotePreview.setImageBitmap(bData);
                                    mCallAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                }
                catch (Exception ee){
                    Log.e(TAG, ee.getMessage());
                }
            }
        }
    }
}
