package serialApi.demo;

import serialApi.exceptions.NoSyncResponseException;
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
        try {
            System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("1"));
        }catch (NoSyncResponseException e){
            e.printStackTrace();
        }

    }
}
