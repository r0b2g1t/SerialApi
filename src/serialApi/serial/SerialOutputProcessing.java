package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import serialApi.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialOutputProcessing implements Runnable{

    private final LinkedBlockingQueue<Protocol> SERIAL_OUTPUT_QUEUE;
    private final OutputStream SERIAL_OUT;
    private final Protocol transferElement;

    /**
     * @param SERIAL_OUTPUT_QUEUE   request queue
     * @param SERIAL_OUT            serial output stream of the serial device via the rxtx-library
     * @param transferElement       shared object witch carries the information of the last written request to
     *                              the device, for the transmission to the SerialReader-class
     */
    public SerialOutputProcessing(final LinkedBlockingQueue<Protocol> SERIAL_OUTPUT_QUEUE,
                                  final OutputStream SERIAL_OUT,
                                  final Protocol transferElement)
    {

        this.SERIAL_OUTPUT_QUEUE = SERIAL_OUTPUT_QUEUE;
        this.SERIAL_OUT = SERIAL_OUT;
        this.transferElement = transferElement;
    }


    public void run()
    {
        while( true )
        {
            Protocol request;
            try{
                request = SERIAL_OUTPUT_QUEUE.take();

                transferElement.setThreadID(request.getThreadID());
                transferElement.setRequest(request.getRequest());


                System.out.println("Taken and writing request: " + transferElement.getRequest() + " from thread: " + transferElement.getThreadID());

                try{
                    SERIAL_OUT.write(transferElement.getRequest().getBytes());
                    SERIAL_OUT.flush();
                }catch(IOException  ioe ){
                    ioe.printStackTrace();
                }

            }catch( InterruptedException ie ){
                ie.printStackTrace();
            }
        }
    }
}