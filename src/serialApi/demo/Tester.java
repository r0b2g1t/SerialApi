package serialApi.demo;

import serialApi.exceptions.NoSyncResponseException;
import serialApi.serial.SerialConfig;
import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Tester {

    private static serialApi.serial.SerialManager SerialMgm;

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        // set path to the properties file
        String file = "/Users/robert/IdeaProjects/SerialApi/src/resources/serial.properties";

        SerialConfig CONFIGURATION = new SerialConfig(file);

        // Set the COM-Port
        CONFIGURATION.setPort("/dev/cu.wchusbserial1420");

        SerialMgm = new SerialManager(CONFIGURATION);

        try {
            SerialMgm.init();
        }catch (Exception e){
            e.printStackTrace();
        }

        // add data to the WriterThreads via inputQueues for asynchronous writing
        inputQueue.add("1");
        inputQueue.add("0");
        inputQueue.add("1");
        /*inputQueue.add("0");
        /*inputQueue.add("1");
        inputQueue.add("0");*/

        MessageReceiver receiver = new MessageReceiver(SerialMgm);
        final Thread receiverThread = new Thread(receiver);

        receiverThread.start();

        Writer writer = new Writer(inputQueue, SerialMgm);
        final Thread writerThread = new Thread(writer);

        writerThread.start();

        // Synchronous writing
        SyncWriter syncWriter = new SyncWriter(SerialMgm);
        final Thread syncWriterThread = new Thread(syncWriter);

        syncWriterThread.start();

        try {
            System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("0"));
        }catch (NoSyncResponseException e){
            e.printStackTrace();
        }

        /*try{
            SerialMgm.close();
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }
}
