package serialApi.demo;

import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    private boolean running = true;

    private final BlockingQueue<String> inputQueue;

    private final SerialManager SerialMgm;

    public Writer (BlockingQueue<String> inputQueue, SerialManager SerialMgm){

        this.inputQueue = inputQueue;
        this.SerialMgm = SerialMgm;

    }

    @Override
    public void run() {

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
