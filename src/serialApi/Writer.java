package serialApi;

import serialApi.listener.ResponseListener;
import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;

/**
 * Created by robert on 09.05.16.
 */
public class Writer implements Runnable {
    private final BlockingQueue<String> inputQueue;
    private final SerialManager SerialMgm;
    private ResponseListener responseListener;
    private final Message response;
    public Writer (BlockingQueue<String> inputQueue, SerialManager SerialMgm){
        this.inputQueue = inputQueue;
        this.SerialMgm = SerialMgm;
        response = new Message("");


    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getId());
        responseListener = new ResponseListener(response);
        SerialMgm.addResponseListener(responseListener, Thread.currentThread().getId());
        //SerialMgm.addResponseListener(responseListener, Long.valueOf("1"));

        while(true){
            String request;
            try{
                request = inputQueue.take();
                SerialMgm.write(request);
                Thread.sleep(2000);
                System.out.println("Response in writerThread: " + response.getText());
                /*if(!request.equals(null)) {
                    SerialMgm.removeResponseListener(responseListener);
                    request = "";
                    SerialMgm.write("0");
                    SerialMgm.addResponseListener(responseListener, Thread.currentThread().getId());
                    Thread.sleep(3000);
                    System.out.println("Response in writerThread: " + response.getText());
                }*/
                //System.out.println("Response: " + SerialMgm.read());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
