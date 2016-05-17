package serialApi.serial;

import serialApi.Protocol;
import serialApi.listener.EventClassListener;
import serialApi.listener.ListenerHandler;
import serialApi.listener.ResponseListener;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by robert on 27.04.16.
 */
public class SerialManager {
    private static SerialConfig CONFIGURATION;
    private static LinkedBlockingQueue<Protocol> SerialOutputQueue;
    private static ConcurrentHashMap<Long, BlockingQueue<Protocol>> responseQueueMap;
    private static ConcurrentHashMap<Long, ArrayList> responderListenerListMap;
    private static ConcurrentHashMap<EventClassListener, Long> listenerThreadMap;
    private static ListenerHandler listenerHandler;


    /**
     * @param CONFIGURATION holds the port-configuration-parameters
     */
    public SerialManager(final SerialConfig CONFIGURATION){
        super();
        SerialManager.CONFIGURATION = CONFIGURATION;
        SerialOutputQueue = new LinkedBlockingQueue<>();
        responseQueueMap = new ConcurrentHashMap<>();
        responderListenerListMap = new ConcurrentHashMap<>();
        listenerThreadMap = new ConcurrentHashMap<>();
        listenerHandler = new ListenerHandler(responseQueueMap, responderListenerListMap);
        final Thread listenerHandlerThread = new Thread(listenerHandler);
        listenerHandlerThread.start();


    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be added
     * @param threadID  Thread-ID of the thread witch will be listened
     */
    public synchronized void addResponseListener(ResponseListener listener, Long threadID){
        if(!responderListenerListMap.containsKey(threadID)) {
            try {
                responderListenerListMap.put(threadID, new ArrayList());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        listenerThreadMap.put(listener, threadID);
        responderListenerListMap.get(threadID).add(listener);
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be removed
     */
    public synchronized void removeResponseListener(ResponseListener listener){
        responderListenerListMap.get(listenerThreadMap.get(listener)).remove(listener);
        listenerThreadMap.remove(listener);
    }


    /**
     * initial the connection to the serial device by using the rxtx-library
     */
    public void connect(){

        SerialConnection serialConn = new SerialConnection(CONFIGURATION);
        try {
            serialConn.connect(SerialOutputQueue,
                                responseQueueMap);
            // short delay for serial connection initialisation
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    /**
     * @param data  input data for the serial write method
     * @return      returns the response of the synchronous request
     */
    public String syncWrite(final String data){
        String response;
        write(data);
        response = read();
        return response;
    }

    /**
     * @param data  input data for the serial write method
     */
    public void write(final String data){

        Long threadID = Thread.currentThread().getId();

        Protocol transferElement = new Protocol(null, null);
        transferElement.setThreadID(Thread.currentThread().getId());
        transferElement.setRequest(data);

        System.out.println("ThreadID in write method: " + threadID);
        try {
            responseQueueMap.put(threadID, new LinkedBlockingQueue<>());
            SerialOutputQueue.add(transferElement);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @return      returns the response of the synchronous request
     */
    public String read(){

        Long threadID = Thread.currentThread().getId();
        try{
            Protocol responseElement;
            responseElement = responseQueueMap.get(threadID).poll(10, TimeUnit.SECONDS);
            System.out.println("bla" + responseElement.getThreadID() + ";" + responseElement.getRequest() + ";" + responseElement.getResponse());
            return responseElement.getResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
