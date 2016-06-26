/**
 * MIT-license: https://opensource.org/licenses/MIT
 * Copyright (c) 2016 Robert Neumann
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

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
