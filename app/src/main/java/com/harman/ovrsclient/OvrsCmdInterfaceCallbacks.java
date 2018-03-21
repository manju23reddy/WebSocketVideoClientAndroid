package com.harman.ovrsclient;

/**
 * Created by MReddy3 on 3/16/2018.
 */

public interface OvrsCmdInterfaceCallbacks {
    void onServerConnected();
    void onServerClosed();
    void onNewParticipantAdded(ParticipantHolder participant);
    void onParticipantExit(ParticipantHolder participant);
    void onVideoFrameReceived(ParticipantHolder participant, VideoFrameHeader frame);
    void onMessage(String s);
}
