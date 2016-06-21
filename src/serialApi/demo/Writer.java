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
    private ResponseListener notificationListener;

    private boolean running = true;

    private final Message response;
    private final Message notification;

    private final BlockingQueue<String> inputQueue;

    private final SerialManager SerialMgm;

    public Writer (BlockingQueue<String> inputQueue, SerialManager SerialMgm){
        this.inputQueue = inputQueue;
        this.SerialMgm = SerialMgm;

        response = new Message("");
        notification = new Message("");


    }

    @Override
    public void run() {
        responseListener = new ResponseListener(response);
        notificationListener = new ResponseListener(notification);

        SerialMgm.addResponseListener(responseListener, Thread.currentThread().getId());
        SerialMgm.addNotificationListener(notificationListener);

        try {
            Thread.sleep(1800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(running){
            String request;
            try{
                request = inputQueue.take();
                SerialMgm.write(request);
                Thread.sleep(10);

                if(!response.getText().equals("")) {
                    System.out.println("Response in writerThread: " + response.getText());
                    response.setText("");
                }
                if(!notification.getText().equals("")) {
                    System.out.println("Notification in writerThread: " + notification.getText());
                    notification.setText("");
                }

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
