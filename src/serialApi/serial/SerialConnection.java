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

import gnu.io.*;
import serialApi.exceptions.UnableToConnectException;
import serialApi.helper.LoggerCollector;
import serialApi.helper.SerialProtocol;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;


public class SerialConnection
{
    private static LoggerCollector logger;
    private static SerialPort serialPort;
    private static CommPortIdentifier portIdentifier;
    private static CommPort commPort;

    private final SerialConfig CONFIGURATION;
    private final SerialProtocol transferElement = new SerialProtocol(null, null);
    private final AtomicInteger sendSignal = new AtomicInteger(0);

    private SerialOutputProcessing serialOutputProcessor;


    /**
     * @param CONFIGURATION Holds the port-configuration-parameters.
     */
    public SerialConnection(final SerialConfig CONFIGURATION ){
        super();
        this.CONFIGURATION = CONFIGURATION;
        logger = new LoggerCollector().getInstance();
    }


    /**
     * @param serialOutputQueue     Request queue for all thread.
     * @param serialInputQueue      Response queue for response transmission from SerialReader to ListenerHandler.
     * @throws Exception            Throws Exceptions if necessary.
     */
    public void connect(final LinkedBlockingQueue<SerialProtocol> serialOutputQueue,
                        final LinkedBlockingQueue<SerialProtocol> serialInputQueue)
            throws Exception
    {
        sendSignal.set(0);
        try{
            portIdentifier = CommPortIdentifier.getPortIdentifier(CONFIGURATION.getPort());
        } catch (NoSuchPortException e){
            logger.wrapper.log(Level.SEVERE,"Port {0} not found.", CONFIGURATION.getPort());
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }

        if (portIdentifier.isCurrentlyOwned())
        {
            logger.wrapper.log(Level.SEVERE, "Error: Port is currently in use.");
        }
        else
        {
            try {
                commPort = portIdentifier.open(this.getClass().getName(), CONFIGURATION.getTimeoutMsWaitForOpen());
                logger.wrapper.log(Level.FINEST, "Connection to port {0} established.", CONFIGURATION.getPort());
            } catch (PortInUseException e){
                logger.wrapper.log(Level.SEVERE, "Port {0} is in use. Stop Application.", CONFIGURATION.getPort());
                logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                throw (new UnableToConnectException());
            }

            if ( commPort instanceof SerialPort )
            {
                serialPort = (SerialPort) commPort;
                try {
                    serialPort.setSerialPortParams(CONFIGURATION.getBaudRate(),
                            CONFIGURATION.getDataBits(),
                            CONFIGURATION.getStopBits(),
                            CONFIGURATION.getParity());
                    logger.wrapper.log(Level.FINEST, "Port configuration successful.");
                } catch (final UnsupportedCommOperationException e){
                    logger.wrapper.log(Level.SEVERE,"Port {0} did not allow parameter settings.",
                            CONFIGURATION.getPort());
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }
                try {
                    serialPort.addEventListener(
                            new SerialReader(CONFIGURATION,
                                    serialPort.getInputStream(),
                                    serialInputQueue,
                                    transferElement,
                                    sendSignal));
                    logger.wrapper.log(Level.FINEST, "Serial port event listener added.");
                } catch (IOException e){
                    logger.wrapper.log(Level.SEVERE,"Port {0} raised input stream error.", CONFIGURATION.getPort());
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }
                try {
                    this.serialOutputProcessor =
                            new SerialOutputProcessing(serialOutputQueue,
                                    serialPort.getOutputStream(),
                                    transferElement,
                                    sendSignal);

                    new Thread(serialOutputProcessor).start();
                    logger.wrapper.log(Level.FINEST, "SerialOutputProcessor thread started.");
                } catch (IOException e){
                    logger.wrapper.log(Level.SEVERE,"Port {0} raised output stream error.", CONFIGURATION.getPort());
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }

                serialPort.notifyOnDataAvailable(true);
            }
            else
            {
                logger.wrapper.log(Level.SEVERE,"Port {0} is not a serial port.", CONFIGURATION.getPort());
            }
        }
    }

    /**
     * Terminates the serialPort instance by the close method.
     */
    public synchronized void close()
    {
        try{
            serialPort.getOutputStream().close();
            serialPort.getInputStream().close();
            serialPort.removeEventListener();
            serialOutputProcessor.terminate();
            serialPort.close();
            logger.wrapper.log(Level.FINEST, "Serial connection closed.");

        }catch( IOException e ){
            logger.wrapper.log(Level.SEVERE,"I/O exception on close out.");
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }
}