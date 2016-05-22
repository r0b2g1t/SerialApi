package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialApi.LoggerCollector;
import serialApi.SerialProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SerialReader implements SerialPortEventListener{

    private static LoggerCollector logger;

    private final InputStream inputStream;
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
        this.responseQueueMap = responseQueueMap;
        this.transferElement = transferElement;
        this.sendSignal = sendSignal;

        logger = new LoggerCollector().getInstance();
        logger.wrapper.log(Level.FINEST, "SerialReader thread initialized.");
    }

    /**
     * @param serialEvent   SerialEvent object for data input handling
     */
    public void serialEvent( SerialPortEvent serialEvent )
    {
        try{
            if( serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
            {
                logger.wrapper.log(Level.FINE, "Serial input data available.");
                String response = "";
                String responsePats[];
                SerialProtocol responseElement = new SerialProtocol(null, null);

                try {
                    int availableBytes = inputStream.available();

                    if (availableBytes > 0) {
                        inputStream.read(readBuffer, 0, availableBytes);
                        responseBuilder = new String(readBuffer, 0, availableBytes);
                    }
                    logger.wrapper.log(Level.FINEST, "Read data {0}.", responseBuilder);
                } catch (IOException e) {
                    logger.wrapper.log(Level.WARNING, "Unable to read response from input stream.");
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }

                // response building and check for end character
                if(responseBuilder.contains(CONFIGURATION.getEndOfResponseCharacter())){
                    responsePats = responseBuilder.split(CONFIGURATION.getEndOfResponseCharacter());
                    response = responseCache + responsePats[0];
                    responseCache = "";
                    if(responsePats.length > 1) {
                        for (int i = 1; i <= responsePats.length - 1; i++) {
                            responseCache = responsePats[i] + CONFIGURATION.getEndOfResponseCharacter();
                        }
                        logger.wrapper.log(Level.FINEST, "Response trash: {0} is dropped.", responseCache);
                        System.out.println("Multiple response received trash-> " + responseCache);
                        responseCache="";
                    }
                }else {
                    // Response caching to responseCache for buildup
                    logger.wrapper.log(Level.FINEST,
                                        "No end of response found add {0} to responseCache {1}.",
                                        new Object[]{responseBuilder, responseCache});

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

                        try {
                            responseQueueMap.get(0L).add(responseElement);
                            logger.wrapper.log(Level.FINEST,
                                                "Notification {0} add to the notification queue.",
                                                responseElement.getAll());

                        }catch(NullPointerException e){
                            logger.wrapper.log(Level.WARNING, "Unable to add notification to the queue.");
                            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                        }

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

                        try {
                            responseQueueMap.get(transferElement.getThreadID()).add(responseElement);
                            logger.wrapper.log(Level.FINEST,
                                    "Response {0} add to the response queue for threadID {1}.",
                                    new Object[]{responseElement.getAll(),
                                                 responseElement.getThreadID()});

                        } catch (NullPointerException e){
                            logger.wrapper.log(Level.WARNING,
                                                "Unable to add response for thread{0} to queue.",
                                                transferElement.getThreadID());

                            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                        }
                        transferElement.flush();
                    }
                    responseCache="";
                    sendSignal.getAndDecrement();
                }
            }
        }catch( Exception e ){
            logger.wrapper.log(Level.WARNING, "An error occurred by response reading.");
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }
}