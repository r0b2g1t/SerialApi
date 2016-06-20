package serialApi.demo;

import serialApi.serial.SerialManager;

/**
 * Created by robert on 20.06.16.
 */
public class SyncWriter implements Runnable {

    private final SerialManager SerialMgm;

    public SyncWriter (SerialManager SerialMgm){
        this.SerialMgm = SerialMgm;
    }

    @Override
    public void run(){
        System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("1"));
    }
}
