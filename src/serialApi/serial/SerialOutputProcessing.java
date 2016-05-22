package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import serialApi.LoggerCollector;
import serialApi.SerialProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SerialOutputProcessing implements Runnable{

    private static LoggerCollector logger;

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
        while( true )
        {
            SerialProtocol request;
            if(sendSignal.get() == 0) {
                try {
                    request = SERIAL_OUTPUT_QUEUE.take();
                    logger.wrapper.log(Level.FINEST, "Request {0} taken from serial output queue.", request.getAll());

                    transferElement.setThreadID(request.getThreadID());
                    transferElement.setRequest(request.getRequest());
                    transferElement.setSyncFlag(request.getSyncFlag());

                    System.out.println("Taken and writing request: " + transferElement.getRequest() +
                            " sync: " + transferElement.getSyncFlag() +
                            ", from thread: " + transferElement.getThreadID());

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
        Thread.currentThread().interrupt();
    }
}