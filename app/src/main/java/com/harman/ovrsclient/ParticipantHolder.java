package com.harman.ovrsclient;

/**
 * Created by mreddy3 on 3/12/2018.
 */

public class ParticipantHolder {

    private String hostport;
    private String hostaddress;
    private String hostFirstName;
    private String hostLastName;

    public ParticipantHolder(){

    }

    public ParticipantHolder(String hostaddress, String hostport){
        this.hostaddress = hostaddress;
        this.hostport = hostport;
    }

    public void sethostport(String hostport){
        this.hostport = hostport;
    }

    public void sethostaddress(String hostaddress){
        this.hostaddress = hostaddress;
    }

    public void sethostFirstName(String hostFirstName){
        this.hostFirstName = hostFirstName;
    }

    public void setHostLastName(String hostLastName){
        this.hostLastName = hostLastName;
    }

    public String gethostaddress(){
        return this.hostaddress;
    }

    public String gethostport(){
        return this.hostport;
    }

    public String gethostfname(){
        return this.hostFirstName;
    }

    public String gethostlname(){
        return this.hostLastName;
    }


}
