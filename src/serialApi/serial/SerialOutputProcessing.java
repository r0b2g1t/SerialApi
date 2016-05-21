package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import serialApi.SerialProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SerialOutputProcessing implements Runnable{

    private final LinkedBlockingQueue<SerialProtocol> SERIAL_OUTPUT_QUEUE;
    private final OutputStream SERIAL_OUT;
    private final SerialProtocol transferElement;
    private final AtomicInteger sendSignal;

    /**
     * @param SERIAL_OUTPUT_QUEUE   Request queue for data transmission to the rxtx-library
     * @param SERIAL_OUT            Serial output stream of the serial device via the rxtx-library
     * @param transferElement       Shared object witch carries the information of the last written request to
     * @param sendSignal
     */
    public SerialOutputProcessing(final LinkedBlockingQueue<SerialProtocol> SERIAL_OUTPUT_QUEUE,
                                  final OutputStream SERIAL_OUT,
                                  final SerialProtocol transferElement, AtomicInteger sendSignal)
    {

        this.SERIAL_OUTPUT_QUEUE = SERIAL_OUTPUT_QUEUE;
        this.SERIAL_OUT = SERIAL_OUT;
        this.transferElement = transferElement;
        this.sendSignal = sendSignal;
    }


    public void run()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while( true )
        {
            SerialProtocol request;
            if(sendSignal.get() == 0) {
                try {
                    request = SERIAL_OUTPUT_QUEUE.take();

                    transferElement.setThreadID(request.getThreadID());
                    transferElement.setRequest(request.getRequest());
                    transferElement.setSyncFlag(request.getSyncFlag());

                    System.out.println("Taken and writing request: " + transferElement.getRequest() +
                            " sync: " + transferElement.getSyncFlag() +
                            ", from thread: " + transferElement.getThreadID());

                    try {
                        SERIAL_OUT.write(transferElement.getRequest().getBytes());
                        SERIAL_OUT.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                sendSignal.incrementAndGet();
            }
        }
    }
    public void terminate(){
        Thread.currentThread().interrupt();
    }
}