package serialApi.demo;

import serialApi.helper.Message;
import serialApi.listener.ResponseListener;
import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;

/**
 * Created by robert on 09.05.16.
 */
public class Writer implements Runnable {


    private ResponseListener responseListener;
    private ResponseListener responseListenerAll;
    private ResponseListener notificationListener;

    private boolean running = true;

    private final Message response;
    private final Message responseAll;
    private final Message notification;
    private final BlockingQueue<String> inputQueue;
    private final SerialManager SerialMgm;

    public Writer (BlockingQueue<String> inputQueue, SerialManager SerialMgm){
        this.inputQueue = inputQueue;
        this.SerialMgm = SerialMgm;
        response = new Message("");
        responseAll = new Message("");
        notification = new Message("");


    }

    @Override
    public void run() {
        responseListener = new ResponseListener(response);
        responseListenerAll = new ResponseListener(responseAll);
        notificationListener = new ResponseListener(notification);
        SerialMgm.addResponseListener(responseListener, Thread.currentThread().getId());
        SerialMgm.addResponseListener(responseListenerAll, Long.parseLong("all", 36));
        SerialMgm.addNotificationListener(notificationListener);

        System.out.println("ThreadID: " + Thread.currentThread().getId());
        //SerialMgm.addResponseListener(responseListener, Long.valueOf("1"));

        while(running){
            String request;
            try{
                request = inputQueue.take();
                SerialMgm.write(request);
                Thread.sleep(1800);
                System.out.println("Response in writerThread: " + response.getText());
                System.out.println("Response in writerThread all: " + responseAll.getText());
                System.out.println("Notification in writerThread: " + notification.getText());
                SerialMgm.removeResponseListener(notificationListener);
                //SerialMgm.removeResponseListener(responseListener);
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
    public void terminate()
    {
        running = false;
        System.out.println("Thread terminating!!!!!");
    }
}
