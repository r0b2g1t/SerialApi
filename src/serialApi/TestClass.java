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
        String file = "/Users/robert/IdeaProjects/SerialApi/src/serialApi/serial/serial.properties";

        SerialConfig CONFIGURATION = new SerialConfig(file);

        // Set the COM-Port
        CONFIGURATION.setPort("/dev/cu.wchusbserial142");

        SerialMgm = new SerialManager(CONFIGURATION);

        SerialMgm.init();

        // add data to the WriterThreads via inputQueues for asynchronous writing
        inputQueue.add("1");
        inputQueue.add("0");
        inputQueue.add("1");
        inputQueue.add("0");
        /*inputQueue.add("1");
        inputQueue.add("0");*/

        Writer writer = new Writer(inputQueue, SerialMgm);
        final Thread writerThread = new Thread(writer);

        writerThread.start();

        // Synchronous writing
        //System.out.println("Write sync:");
        //System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("1"));
    }
}
