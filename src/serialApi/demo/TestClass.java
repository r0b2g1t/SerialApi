package serialApi.demo;

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
        CONFIGURATION.setPort("/dev/cu.wchusbserial1420");

        SerialMgm = new SerialManager(CONFIGURATION);

        SerialMgm.init();

        // add data to the WriterThreads via inputQueues for asynchronous writing
        inputQueue.add("1");
        inputQueue.add("0");
        inputQueue.add("1");
        /*inputQueue.add("0");
        /*inputQueue.add("1");
        inputQueue.add("0");*/

        Writer writer = new Writer(inputQueue, SerialMgm);
        final Thread writerThread = new Thread(writer);

        writerThread.start();

        // Synchronous writing

        writer.terminate();
        writerThread.interrupt();



        SyncWriter syncWriter = new SyncWriter(SerialMgm);
        final Thread syncWriterThread = new Thread(syncWriter);

        syncWriterThread.start();

        //System.out.println("Write sync:");
        System.out.println("SyncWrite answer: " + SerialMgm.syncWrite("0"));

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++)
            System.out.println("Thread No:" + i + " (ID:" + lstThreads[i].getId() + ") = "
                    + lstThreads[i].getName());

        try{
            SerialMgm.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
