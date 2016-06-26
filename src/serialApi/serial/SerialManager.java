package serialApi.serial;

import serialApi.exceptions.NoSyncResponseException;
import serialApi.exceptions.UnableToConnectException;
import serialApi.helper.LoggerCollector;
import serialApi.helper.SerialProtocol;
import serialApi.listener.EventClassListener;
import serialApi.listener.ListenerHandler;
import serialApi.listener.ResponseListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by robert on 27.04.16.
 */
public class SerialManager {
    private static LoggerCollector logger;
    private static SerialConfig CONFIGURATION;
    private static LinkedBlockingQueue<SerialProtocol> serialOutputQueue;
    private static LinkedBlockingQueue<SerialProtocol> serialInputQueue;

    private static ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseSyncQueueMap;
    private static ConcurrentHashMap<Long, ArrayList<ResponseListener>> responderListenerListMap;
    private static ConcurrentHashMap<EventClassListener, ArrayList<Long>> listenerThreadMap;

    private static SerialConnection serialConn;

    /**
     * @param CONFIGURATION Holds the port-configuration-parameters.
     */
    public SerialManager(final SerialConfig CONFIGURATION){
        super();

        SerialManager.CONFIGURATION = CONFIGURATION;
        logger = new LoggerCollector().prepare(CONFIGURATION);

        serialOutputQueue = new LinkedBlockingQueue<>();
        serialInputQueue = new LinkedBlockingQueue<>();

        responseSyncQueueMap = new ConcurrentHashMap<>();
        responderListenerListMap = new ConcurrentHashMap<>();
        listenerThreadMap = new ConcurrentHashMap<>();

        serialConn = new SerialConnection(CONFIGURATION);

        ListenerHandler listenerHandler = new ListenerHandler(serialInputQueue,
                                                              responseSyncQueueMap,
                                                              responderListenerListMap);

        final Thread listenerHandlerThread = new Thread(listenerHandler);
        listenerHandlerThread.start();
        logger.wrapper.log(Level.FINEST, "ListenerHandlerThread started.");

    }

    /**
     * Initial the connection to the serial device by using the rxtx-library.
     */
    public void init() throws Exception{
        try {
            serialConn.connect(serialOutputQueue,
                                serialInputQueue);

        } catch (Exception e) {
            logger.wrapper.log(Level.SEVERE, "Unable to initialize the connection.");
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
            throw(new UnableToConnectException());
        }
        logger.wrapper.log(Level.FINEST, "Connection established.");
    }

    /**
     * Is reconnecting the serial device.
     */
    public void reconnect(){
        try {
            serialConn.close();
            serialConn.connect(serialOutputQueue, serialInputQueue);
            logger.wrapper.log(Level.FINEST, "Reconnect complete.");
        } catch (Exception e) {
            logger.wrapper.log(Level.SEVERE, "Unable to reconnect the serial port {0}", CONFIGURATION.getPort());
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
            e.printStackTrace();
        }
    }

    /**
     * Closing the connection to the serial device.
     */
    public synchronized void close(){
        serialConn.close();
    }

    /**
     * @param data  Input data for the serial write method.
     * @return      the response of the synchronous request.
     */
    public String syncWrite(final String data) throws NoSyncResponseException {
        String response;
        Long threadID = Thread.currentThread().getId();

        SerialProtocol transferElement = new SerialProtocol(null, null);
        transferElement.setThreadID(Thread.currentThread().getId());
        transferElement.setRequest(data);
        transferElement.setSyncFlag(true);

        try {
            if (!responseSyncQueueMap.containsKey(threadID)) {
                responseSyncQueueMap.put(threadID, new LinkedBlockingQueue<>());
            }
        } catch (Exception e) {
            logger.wrapper.log(Level.SEVERE, "Unable to initialize the synchronous response queue for the thread {0}",
                    threadID);
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
        try {
            serialOutputQueue.add(transferElement);
            logger.wrapper.log(Level.FINEST, "Request {0} added to SerialOutputQueue.", transferElement.getAll());

        }catch (Exception e){
            logger.wrapper.log(Level.SEVERE, "Unable to add the request {0} to SerialOutPutQueue",
                    transferElement.getAll());
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
        response = read();
        if(response != null) {
            logger.wrapper.log(Level.FINEST, "Response {0} read in syncWrite", response);
            return response;
        } else {
            throw(new NoSyncResponseException());
        }
    }

    /**
     * @param data  Input data for the serial write method.
     */
    public void write(final String data){

        SerialProtocol transferElement = new SerialProtocol(null, null);
        transferElement.setThreadID(Thread.currentThread().getId());
        transferElement.setRequest(data);
        transferElement.setSyncFlag(false);

        try {
            serialOutputQueue.add(transferElement);
            logger.wrapper.log(Level.FINEST, "Asynchronous request {0} added to SerialOutputQueue.",
                    transferElement.getAll());
        }catch (Exception e){
            logger.wrapper.log(Level.SEVERE, "Unable to add the request {0} to SerialOutPutQueue",
                    transferElement.getAll());
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }

    /**
     * @return      the response of the synchronous request.
     */
    private String read(){

        Long threadID = Thread.currentThread().getId();
        try{
            SerialProtocol responseElement;
            logger.wrapper.log(Level.FINEST, "Poll from response queue starts.");
            responseElement = responseSyncQueueMap.get(threadID).poll(CONFIGURATION.getSyncResponseTimeout(),
                                                                      TimeUnit.MILLISECONDS);
            if(responseElement != null) {
                logger.wrapper.log(Level.FINEST, "Response {0} received in read method.", responseElement.getAll());
                return responseElement.getResponse();
            }
        } catch (InterruptedException e) {
            logger.wrapper.log(Level.WARNING, "No response received.");
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
        logger.wrapper.log(Level.WARNING, "No response received after {0} milliseconds.",
                           CONFIGURATION.getSyncResponseTimeout());
        return null;
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be added.
     * @param threadID  Thread-ID of the thread witch will be listened.
     */
    public synchronized void addResponseListener(ResponseListener listener, Long threadID){
        if(!responderListenerListMap.containsKey(threadID)) {
            try {
                logger.wrapper.log(Level.FINEST, "No listener array for thread {0} exists.", threadID);
                responderListenerListMap.put(threadID, new ArrayList<>());
                listenerThreadMap.put(listener, new ArrayList<>());
                logger.wrapper.log(Level.FINEST, "Listener array for thread {0} added.", threadID);
            }catch (Exception e){
                logger.wrapper.log(Level.WARNING, "Unable to add responseListener for the thread {0}.", threadID );
                logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
            }
        }
        try {
            listenerThreadMap.get(listener).add(threadID);
            responderListenerListMap.get(threadID).add(listener);
            logger.wrapper.log(Level.FINEST, "Listener {0} for thread {1} added.", new Object[]{listener,threadID});
        } catch(Exception e){
            logger.wrapper.log(Level.WARNING, "Unable to add listener for thread {0}", threadID);
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }

    /**
     * @param listener  Listener object for the thread with the ID "threadID" witch will be removed.
     */
    public synchronized void removeResponseListener(ResponseListener listener){
        try {
            ArrayList  listenerThreads;
            listenerThreads = listenerThreadMap.get(listener);

            Iterator threadID = listenerThreads.iterator();
            while (threadID.hasNext()) {
                responderListenerListMap.get(threadID.next()).remove(listener);

                logger.wrapper.log(Level.FINEST, "Listener {0} for thread {1} is removed.", new Object[]{listener, threadID});
            }
            listenerThreadMap.remove(listener);
        } catch(Exception e){
            logger.wrapper.log(Level.WARNING, "Unable to remove listener for thread {0}",
                    listenerThreadMap.get(listener));
            logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
    }

    /**
     * @param listener  Listener object for notification receiving witch will be added.
     */
    public synchronized void addNotificationListener(ResponseListener listener){
        addResponseListener(listener, 0L);
        logger.wrapper.log(Level.FINEST, "Notification listener {0} added.", listener);
    }

    /**
     * @param listener  Listener object for notification receiving witch will be removed.
     */
    public synchronized void removeNotificationListener(ResponseListener listener){
        removeResponseListener(listener);
        logger.wrapper.log(Level.FINEST, "Notification listener {0} removed.", listener);
    }

}
