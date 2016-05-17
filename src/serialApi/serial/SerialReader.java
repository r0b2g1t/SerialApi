package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialApi.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class SerialReader implements SerialPortEventListener{

    private final BufferedReader inputBuffer;
    private final ConcurrentHashMap<Long, BlockingQueue<Protocol>> responseQueueMap;
    private final Protocol transferElement;

    /**
     * @param inputStream       InputStream for handling the available data
     * @param responseQueueMap  HashMap witch handle the response queues for every separate thread
     * @param transferElement   shared object witch carry the information of the last written request to the device
     *                          (ThreadID;request;response;meta-data;...)
     */
    public SerialReader(InputStream inputStream,
                        ConcurrentHashMap<Long, BlockingQueue<Protocol>> responseQueueMap,
                        Protocol transferElement)
    {
        this.inputBuffer = new BufferedReader(new InputStreamReader(inputStream));
        this.responseQueueMap = responseQueueMap;
        this.transferElement = transferElement;
    }

    /**
     * @param serialEvent   SerialEvent object for data input handling
     */
    public void serialEvent( SerialPortEvent serialEvent )
    {
        try{

            if( serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
            {
                String response;
                Protocol responseElement = new Protocol(null, null);

                response = inputBuffer.readLine();

                transferElement.setResponse(response);
                System.out.println( "Response element total: "
                                    + transferElement.getThreadID()
                                    + ";" + transferElement.getRequest()
                                    + ";" + transferElement.getResponse());

                responseElement.setThreadID(transferElement.getThreadID());
                responseElement.setRequest(transferElement.getRequest());
                responseElement.setResponse(transferElement.getResponse());

                responseQueueMap.get(transferElement.getThreadID()).add(responseElement);
                transferElement.flush();
            }
        }catch( IOException e ){
            e.printStackTrace();
        }
    }
}