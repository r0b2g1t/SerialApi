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


import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialApi.helper.LoggerCollector;
import serialApi.helper.SerialProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerialReader implements SerialPortEventListener{

    private static LoggerCollector logger;

    private final InputStream inputStream;
    private final LinkedBlockingQueue<SerialProtocol> serialInputQueue;
    private final SerialProtocol transferElement;
    private final SerialConfig CONFIGURATION;
    private final AtomicInteger sendSignal;
    private final byte[] readBuffer = new byte[400];

    private String responseCache = "";
    private String responseBuilder;

    /**
     * @param CONFIGURATION     Holds the port-configuration-parameters.
     * @param inputStream       InputStream for handling the available data
     * @param serialInputQueue  response queue for response transmission from SerialReader to ListenerHandler.
     * @param transferElement   Shared object witch carry the information of the last written request to the device.
     * @param sendSignal        Signal object indicat the possibility to send request to device.
     */
    public SerialReader(SerialConfig CONFIGURATION, InputStream inputStream,
                        LinkedBlockingQueue<SerialProtocol> serialInputQueue,
                        SerialProtocol transferElement, AtomicInteger sendSignal)
    {
        this.CONFIGURATION = CONFIGURATION;
        this.inputStream = inputStream;
        this.serialInputQueue = serialInputQueue;
        this.transferElement = transferElement;
        this.sendSignal = sendSignal;

        logger = new LoggerCollector().getInstance();
        logger.wrapper.log(Level.FINEST, "SerialReader thread initialized.");
    }

    /**
     * @param serialEvent   SerialEvent object for data input handling.
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
                            serialInputQueue.add(responseElement);
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
                        serialInputQueue.add(responseElement);
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