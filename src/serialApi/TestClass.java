package serialApi;

import serialApi.serial.SerialConfig;
import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by robert on 22.04.16.
 */
public class TestClass {

    private static serialApi.serial.SerialManager SerialMgm;

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        // set path to the properties file
        String file = "/Users/robert/Documents/serial.properties";

        SerialConfig CONFIGURATION = new SerialConfig(file);

        // Set the COM-Port
        CONFIGURATION.setPort("/dev/cu.wchusbserial1420");

        SerialMgm = new SerialManager(CONFIGURATION);

        SerialMgm.connect();

        // add data to the WriterThreads via inputQueues for asynchronous writing
        inputQueue.add("0");
        inputQueue.add("1");
        inputQueue.add("0");
        inputQueue.add("1");
        inputQueue.add("0");
        inputQueue.add("1");


        /*inputQueue2.add("1");
        inputQueue2.add("0");*/


        Writer writer = new Writer(inputQueue, SerialMgm);
        /*Writer writer2 = new Writer(inputQueue2, SerialMgm);
        final Thread writerThread2 = new Thread(writer2);*/
        final Thread writerThread = new Thread(writer);

        writerThread.start();

        //writerThread2.start();

        // Synchronous writing
        //System.out.println("Write sync:");
        System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("1"));
    }
}
