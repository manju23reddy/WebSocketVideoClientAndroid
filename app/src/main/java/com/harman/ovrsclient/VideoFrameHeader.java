package com.harman.ovrsclient;

/**
 * Created by MReddy3 on 3/16/2018.
 */

public class VideoFrameHeader {
    int length;
    byte[] mRawFrames;

    public VideoFrameHeader(int len, byte[] frames){
        this.length = len;
        this.mRawFrames = frames;
    }

    public int getLength(){
        return length;
    }

    public byte[] getFrames(){
        return mRawFrames;
    }

}
