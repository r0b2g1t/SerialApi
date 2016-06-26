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

package serialApi.serial;

import serialApi.helper.LoggerCollector;
import serialApi.helper.SerialProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SerialOutputProcessing implements Runnable{

    private static LoggerCollector logger;

    private boolean running = true;

    private final LinkedBlockingQueue<SerialProtocol> SERIAL_OUTPUT_QUEUE;
    private final OutputStream SERIAL_OUT;
    private final SerialProtocol transferElement;
    private final AtomicInteger sendSignal;

    /**
     * @param SERIAL_OUTPUT_QUEUE   Request queue for data transmission to the rxtx-library.
     * @param SERIAL_OUT            Serial output stream of the serial device via the rxtx-library.
     * @param transferElement       Shared object witch carries the information of the last written request to.
     * @param sendSignal            Signal object indicat the possibility to send request to device.
     */
    public SerialOutputProcessing(final LinkedBlockingQueue<SerialProtocol> SERIAL_OUTPUT_QUEUE,
                                  final OutputStream SERIAL_OUT,
                                  final SerialProtocol transferElement, AtomicInteger sendSignal)
    {
        this.SERIAL_OUTPUT_QUEUE = SERIAL_OUTPUT_QUEUE;
        this.SERIAL_OUT = SERIAL_OUT;
        this.transferElement = transferElement;
        this.sendSignal = sendSignal;
        logger = new LoggerCollector().getInstance();
    }


    public void run()
    {
        try{
            Thread.sleep(1500);
        } catch (Exception e){
            logger.wrapper.log(Level.WARNING, "Can not set SerialOutputProcessing thread set to sleep for initialisation.");
        }
        while( running )
        {
            SerialProtocol request;
            if(sendSignal.get() == 0) {
                try {
                    request = SERIAL_OUTPUT_QUEUE.take();
                    logger.wrapper.log(Level.FINEST, "Request {0} taken from serial output queue.", request.getAll());

                    transferElement.setThreadID(request.getThreadID());
                    transferElement.setRequest(request.getRequest());
                    transferElement.setSyncFlag(request.getSyncFlag());

                    try {
                        SERIAL_OUT.write(transferElement.getRequest().getBytes());
                        logger.wrapper.log(Level.FINEST,
                                           "Request {0} written to serial output stream.",
                                           transferElement.getRequest());
                        SERIAL_OUT.flush();

                    } catch (IOException e) {
                        logger.wrapper.log(Level.WARNING,"Can't write to serial output stream.");
                        logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                    }

                } catch (InterruptedException e) {
                    logger.wrapper.log(Level.WARNING, "Unable to take request from request queue.");
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }
                sendSignal.incrementAndGet();
            }
        }
    }
    public void terminate(){
        logger.wrapper.log(Level.FINE, "SerialOutputProcessing thread will be terminating.");
        running = false;
    }
}