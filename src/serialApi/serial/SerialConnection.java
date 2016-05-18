package serialApi.serial;

/**
 * Created by robert on 21.04.16.
 */

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import serialApi.SerialProtocol;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class SerialConnection
{

    private static SerialPort serialPort;
    private final SerialConfig CONFIGURATION;
    private final SerialProtocol transferElement = new SerialProtocol(null, null);

    /**
     * @param CONFIGURATION holds the port-configuration-parameters
     */
    public SerialConnection(final SerialConfig CONFIGURATION ){
        super();
        this.CONFIGURATION = CONFIGURATION;
    }


    /**
     * @param SERIAL_OUTPUT_QUEUE   request queue for all thread
     * @param responseQueueMap      HashMap witch handle the response queues for every separate thread
     * @throws Exception            throws Exceptions if necessary
     */
    public void connect(final LinkedBlockingQueue<SerialProtocol> SERIAL_OUTPUT_QUEUE,
                        final ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseQueueMap)
            throws Exception
    {
        CommPortIdentifier portIdentifier =
                CommPortIdentifier.getPortIdentifier(CONFIGURATION.getPort());

        System.out.println( "Trying to use serial port: " +
                            CONFIGURATION.getPort());

        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),CONFIGURATION.getTimeoutMsWaitForOpen());

            if ( commPort instanceof SerialPort )
            {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams( CONFIGURATION.getBaudRate(),
                                                CONFIGURATION.getDataBits(),
                                                CONFIGURATION.getStopBits(),
                                                CONFIGURATION.getParity());

                SerialOutputProcessing serialOutputProcessor =
                        new SerialOutputProcessing( SERIAL_OUTPUT_QUEUE,
                                                    serialPort.getOutputStream(),
                                                    transferElement);

                new Thread(serialOutputProcessor).start();

                serialPort.addEventListener(
                        new SerialReader( serialPort.getInputStream(),
                                          responseQueueMap,
                                          transferElement));

                serialPort.notifyOnDataAvailable(true);
            }
            else
            {
                System.out.println("Error: Only serial ports are handled");
            }
        }
    }

    /**
     * terminates the serialPort instance by the close method
     */
    private void close()
    {
        try{
            serialPort.close();

        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}