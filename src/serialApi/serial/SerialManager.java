package serialApi.serial;

import serialApi.SerialProtocol;
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
    private static LinkedBlockingQueue<SerialProtocol> SerialOutputQueue;
    private static ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseQueueMap;
    private static ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseSyncQueueMap;
    private static ConcurrentHashMap<Long, ArrayList<ResponseListener>> responderListenerListMap;
    private static ConcurrentHashMap<EventClassListener, Long> listenerThreadMap;
    private static SerialConnection serialConn;


    /**
     * @param CONFIGURATION Holds the port-configuration-parameters
     */
    public SerialManager(final SerialConfig CONFIGURATION){
        super();
        SerialManager.CONFIGURATION = CONFIGURATION;
        SerialOutputQueue = new LinkedBlockingQueue<>();
        responseQueueMap = new ConcurrentHashMap<>();
        responseSyncQueueMap = new ConcurrentHashMap<>();
        responderListenerListMap = new ConcurrentHashMap<>();
        listenerThreadMap = new ConcurrentHashMap<>();
        ListenerHandler listenerHandler = new ListenerHandler(responseQueueMap, responseSyncQueueMap, responderListenerListMap);
        final Thread listenerHandlerThread = new Thread(listenerHandler);
        listenerHandlerThread.start();
        this.serialConn = new SerialConnection(CONFIGURATION);
    }

    /**
     * Initial the connection to the serial device by using the rxtx-library
     */
    public void init(){

        //SerialConnection serialConn = new SerialConnection(CONFIGURATION);
        try {
            serialConn.connect(SerialOutputQueue,
                                responseQueueMap);
            // short delay for serial connection initialisation
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit( 1 );
        }


        /**
         * Creates the notification queue by using the threadID 0
         */
        try {
            if(!responseQueueMap.containsKey(0L)) {
                responseQueueMap.put(0L, new LinkedBlockingQueue<>());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Is reconnecting the serial device.
     */
    public void reconnect(){
        try {
            serialConn.close();
            serialConn.connect(SerialOutputQueue, responseQueueMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closing the connection to the serial device.
     */
    public void close(){
        serialConn.close();
    }

    /**
     * @param data  Input data for the serial write method
     * @return      Returns the response of the synchronous request
     */
    public String syncWrite(final String data){
        String response;
        Long threadID = Thread.currentThread().getId();

        System.out.println("ThreadID in syncWrite: " + threadID);

        SerialProtocol transferElement = new SerialProtocol(null, null);
        transferElement.setThreadID(Thread.currentThread().getId());
        transferElement.setRequest(data);
        transferElement.setSyncFlag(true);
        try {
            if(!responseQueueMap.containsKey(threadID)) {
                responseQueueMap.put(threadID, new LinkedBlockingQueue<>());
            }
            if(!responseSyncQueueMap.containsKey(threadID)){
                responseSyncQueueMap.put(threadID, new LinkedBlockingQueue<>());
            }
            SerialOutputQueue.add(transferElement);
        }catch (Exception e){
            e.printStackTrace();
        }

        response = read();
        return response;
    }

    /**
     * @param data  Input data for the serial write method
     */
    public void write(final String data){

        Long threadID = Thread.currentThread().getId();

        SerialProtocol transferElement = new SerialProtocol(null, null);
        transferElement.setThreadID(Thread.currentThread().getId());
        transferElement.setRequest(data);
        transferElement.setSyncFlag(false);

        System.out.println("ThreadID in write method: " + threadID);
        try {
            if(!responseQueueMap.containsKey(threadID)) {
                responseQueueMap.put(threadID, new LinkedBlockingQueue<>());
            }
            SerialOutputQueue.add(transferElement);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @return      Returns the response of the synchronous request
     */
    private String read(){

        Long threadID = Thread.currentThread().getId();
        System.out.println("ThreadID in read: " + threadID);
        try{
            SerialProtocol responseElement;
            responseElement = responseSyncQueueMap.get(threadID).poll(10, TimeUnit.SECONDS);
            System.out.println( "Response: " + responseElement.getAll());
            return responseElement.getResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be added
     * @param threadID  Thread-ID of the thread witch will be listened
     */
    public synchronized void addResponseListener(ResponseListener listener, Long threadID){
        if(!responderListenerListMap.containsKey(threadID)) {
            try {
                responderListenerListMap.put(threadID, new ArrayList<>());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            listenerThreadMap.put(listener, threadID);
            responderListenerListMap.get(threadID).add(listener);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be removed
     */
    public synchronized void removeResponseListener(ResponseListener listener){
        try {
            responderListenerListMap.get(listenerThreadMap.get(listener)).remove(listener);
            listenerThreadMap.remove(listener);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be added
     */
    public synchronized void addNotificationListener(ResponseListener listener){
        addResponseListener(listener, 0L);
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be removed
     */
    public synchronized void removeNotificationListener(ResponseListener listener){
        removeResponseListener(listener);
    }

}
