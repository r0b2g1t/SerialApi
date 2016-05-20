package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialApi.SerialProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SerialReader implements SerialPortEventListener{

    private final InputStream inputStream;
    private final BufferedReader inputBuffer;
    private final ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseQueueMap;
    private final SerialProtocol transferElement;
    private final SerialConfig CONFIGURATION;
    private final AtomicInteger sendSignal;

    private byte[] readBuffer = new byte[400];
    private String responseCache = "";
    private String responseBuilder;

    /**
     * @param CONFIGURATION
     * @param inputStream       InputStream for handling the available data
     * @param responseQueueMap  HashMap witch handle the response queues for every separate thread
     * @param transferElement   Shared object witch carry the information of the last written request to the device
     * @param sendSignal
     */
    public SerialReader(SerialConfig CONFIGURATION, InputStream inputStream,
                        ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseQueueMap,
                        SerialProtocol transferElement, AtomicInteger sendSignal)
    {
        this.CONFIGURATION = CONFIGURATION;
        this.inputStream = inputStream;
        this.inputBuffer = new BufferedReader(new InputStreamReader(inputStream));
        this.responseQueueMap = responseQueueMap;
        this.transferElement = transferElement;
        this.sendSignal = sendSignal;
    }

    /**
     * @param serialEvent   SerialEvent object for data input handling
     */
    public void serialEvent( SerialPortEvent serialEvent )
    {
        try{

            if( serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
            {
                String response = "";
                String responsePats[];
                SerialProtocol responseElement = new SerialProtocol(null, null);

                try {
                    int availableBytes = inputStream.available();
                    if (availableBytes > 0) {
                        // Read the serial port
                        inputStream.read(readBuffer, 0, availableBytes);

                        responseBuilder = new String(readBuffer, 0, availableBytes);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // response building and check for end character
                if(responseBuilder.contains(CONFIGURATION.getEndOfResponseCharacter())){
                    System.out.println("End character found!");
                    responsePats = responseBuilder.split(CONFIGURATION.getEndOfResponseCharacter());
                    response = responseCache + responsePats[0];
                    responseCache = "";
                    if(responsePats.length > 1) {
                        for (int i = 1; i <= responsePats.length - 1; i++) {
                            responseCache = responsePats[i] + CONFIGURATION.getEndOfResponseCharacter();
                        }
                        System.out.println("Multiple response received trash-> " + responseCache);
                        responseCache="";
                    }
                }else {
                    System.out.println("No end found add: " + responseBuilder + " to responseCache" + responseCache);
                    if(responseCache != "") {
                        responseCache += responseBuilder;
                    }else{
                        responseCache = responseBuilder;
                    }
                }

                if(response != "") {
                    if (response.contains(CONFIGURATION.getNotificationTag())) {
                        responseElement.flush();
                        responseElement.setThreadID(0L);
                        responseElement.setResponse(response.replace("!", ""));
                        responseElement.setSyncFlag(false);
                        responseQueueMap.get(0L).add(responseElement);
                    } else {
                        transferElement.setResponse(response);
                        System.out.println("Response element total: "
                                + transferElement.getThreadID()
                                + ";" + transferElement.getRequest()
                                + ";" + transferElement.getResponse()
                                + ";" + transferElement.getSyncFlag());

                        responseElement.setThreadID(transferElement.getThreadID());
                        responseElement.setRequest(transferElement.getRequest());
                        responseElement.setResponse(transferElement.getResponse());
                        responseElement.setSyncFlag(transferElement.getSyncFlag());

                        responseQueueMap.get(transferElement.getThreadID()).add(responseElement);
                        transferElement.flush();
                    }
                    responseCache="";
                    sendSignal.getAndDecrement();
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}