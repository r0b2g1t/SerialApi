package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialApi.helper.LoggerCollector;
import serialApi.helper.SerialProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerialReader implements SerialPortEventListener{

    private static LoggerCollector logger;

    private final InputStream inputStream;
    private final ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseQueueMap;
    private final SerialProtocol transferElement;
    private final SerialConfig CONFIGURATION;
    private final AtomicInteger sendSignal;
    private final byte[] readBuffer = new byte[400];

    private String responseCache = "";
    private String responseBuilder;

    /**
     * @param CONFIGURATION     Holds the port-configuration-parameters.
     * @param inputStream       InputStream for handling the available data
     * @param responseQueueMap  HashMap witch handle the response queues for every separate thread
     * @param transferElement   Shared object witch carry the information of the last written request to the device
     * @param sendSignal        Signal object indicat the possibility to send request to device.
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
                String response;
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

                /**
                 * Response pattern check
                 */

                for(Pattern p : CONFIGURATION.getResponseRegEx()) {
                    Matcher responseMatcher = p.matcher(responseCache + responseBuilder).reset();

                    while (responseMatcher.find()){
                        response = responseMatcher.group();
                        transferElement.setResponse(response);
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

                        // resetting variables
                        transferElement.flush();
                        responseCache = "";
                        responseBuilder = "";
                        sendSignal.getAndDecrement();
                    }
                }

                /**
                 * Notification pattern check
                 */

                Matcher notificationMatcher = CONFIGURATION.getNotificationPattern()
                        .matcher(responseCache + responseBuilder)
                        .reset();

                while (notificationMatcher.find()){
                    String notification;
                    notification = notificationMatcher.group();
                    responseElement.flush();
                    responseElement.setThreadID(0L);
                    responseElement.setResponse(notification);
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
                }

                // Collect input data
                if(!responseCache.equals("")) {
                    responseCache += responseBuilder;
                }else{
                    responseCache = responseBuilder;
                }

            }
        }catch( Exception e ){
            logger.wrapper.log(Level.WARNING, "An error occurred by response reading.");
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }
}