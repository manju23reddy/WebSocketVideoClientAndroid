package com.harman.ovrsclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mreddy3 on 3/17/2018.
 */

public class CallLayoutAdapter extends RecyclerView.Adapter<CallLayoutAdapter.RemoteViewHolder> {



    ArrayList<String> mParticipantsList = null;
    HashMap<String, ImageView>mParticipantImageViews = null;
    Context mContext = null;

    public class RemoteViewHolder extends RecyclerView.ViewHolder{

        ImageView mRemotePreview;
        TextView mRemoteAddress;

        public RemoteViewHolder(View view){
            super(view);

            mRemotePreview = view.findViewById(R.id.imgv_remote_view);
            mRemoteAddress = view.findViewById(R.id.txtv_ip);

        }
    }

    public CallLayoutAdapter(Context context){
        mContext = context;
        mParticipantsList = new ArrayList<>();
        mParticipantImageViews = new HashMap<>();
    }


    @Override
    public int getItemCount() {
        if (mParticipantsList == null){
            return 0;
        }
        return mParticipantsList.size();
    }


    @Override
    public void onBindViewHolder(RemoteViewHolder holder, int position) {
        String participantIP = mParticipantsList.get(position);
        holder.mRemoteAddress.setText(participantIP);
        mParticipantImageViews.put(participantIP, holder.mRemotePreview);
    }

    public void addParticipant(String ipAddress){
        mParticipantsList.add(ipAddress);
        notifyDataSetChanged();
    }

    public void removeParticipant(String ipAddr){
        if(mParticipantsList.contains(ipAddr)){
            mParticipantsList.remove(ipAddr);
            mParticipantImageViews.remove(ipAddr);
            notifyDataSetChanged();
        }
    }

    public void removeAll(){
        mParticipantsList.clear();
        mParticipantImageViews.clear();
    }

    @Override
    public RemoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.participant_layout;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(layout, parent, false);
        return new RemoteViewHolder(view);
    }


    public ImageView getParticipantImageView(String ipAddr){
        if(null != mParticipantImageViews && null != mParticipantsList){
            ImageView preview = mParticipantImageViews.get(ipAddr);
            return preview;
        }
        return null;
    }
}
