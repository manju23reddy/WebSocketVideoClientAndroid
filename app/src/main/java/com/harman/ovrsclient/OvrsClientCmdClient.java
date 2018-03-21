package com.harman.ovrsclient;

import android.util.Log;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by MReddy3 on 3/16/2018.
 */

public class OvrsClientCmdClient extends WebSocketClient {

    OvrsCmdInterfaceCallbacks mCallbacks;

    public OvrsClientCmdClient(URI serverUri, OvrsCmdInterfaceCallbacks callbacks){
        super(serverUri, new Draft_6455());

        mCallbacks = callbacks;
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        mCallbacks.onServerClosed();

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

            mCallbacks.onServerConnected();
    }

    @Override
    public void onMessage(String s) {
        //Log.d("From Server ", s);
        mCallbacks.onMessage(s);
    }



}
