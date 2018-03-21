package com.harman.ovrsclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mreddy3 on 3/12/2018.
 */

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.HolderAdapter> {


    public interface ServerSelected{
        public void onServerSelected(int pos);
    }

    ServerSelected mServerSelectListener = null;

    private ArrayList<ParticipantHolder> mServerList = null;


    class HolderAdapter extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mHostAddr;
        TextView mHostport;
        TextView mConnStatus;
        public HolderAdapter(View view){
            super(view);

            mHostAddr = view.findViewById(R.id.txtv_host_address);
            mHostport = view.findViewById(R.id.txtv_host_port);
            mConnStatus = view.findViewById(R.id.txtv_conn_status);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mServerSelectListener.onServerSelected(getAdapterPosition());
        }
    }


    public ServerListAdapter(Context context, ServerSelected serverSelected){
        mServerList = new ArrayList<>();
        mServerSelectListener = serverSelected;
    }

    @Override
    public int getItemCount() {
        if (null == mServerList){
            return 0;
        }
        else{
            return mServerList.size();
        }
    }

    @Override
    public HolderAdapter onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.discovered_hosts_item_view;
        LayoutInflater inflator = LayoutInflater.from(context);

        View view = inflator.inflate(layoutIdForListItem, parent, false);

        return new HolderAdapter(view);
    }

    @Override
    public void onBindViewHolder(HolderAdapter holder, int position) {
        ParticipantHolder curItem = mServerList.get(position);
        holder.mHostport.setText(curItem.gethostport());
        holder.mHostAddr.setText(curItem.gethostaddress());
        holder.mConnStatus.setText("Not Connected");
    }

    public ParticipantHolder getSelectedServerFromPos(int pos){
        return mServerList.get(pos);
    }



    public void addNewFoundServer(String port, String name){
        mServerList.add(new ParticipantHolder(name, port));
        notifyDataSetChanged();
    }

    public void clearAll(){
        mServerList.clear();
        notifyDataSetChanged();
    }
}
