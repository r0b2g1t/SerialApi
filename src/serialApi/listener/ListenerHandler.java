package serialApi.listener;

import serialApi.helper.LoggerCollector;
import serialApi.helper.Message;
import serialApi.helper.SerialProtocol;
import serialApi.exceptions.NoListenerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Created by robert on 13.05.16.
 */
public class ListenerHandler implements Runnable {

    private static LoggerCollector logger;

    private final LinkedBlockingQueue<SerialProtocol> serialInputQueue;

    private final ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseSyncQueueMap;
    private final ConcurrentHashMap<Long, ArrayList<ResponseListener>> responderListenerListMap;

    private final Message responseMessage;

    /**
     * @param serialInputQueue          response queue for response transmission from SerialReader to ListenerHandler.
     * @param responseSyncQueueMap      ConcurrentHasMap witch holds the response queues of the synchronous requests.
     * @param responderListenerListMap  ConcurrentHashMap witch holds the registrated listeners.
     */
    public ListenerHandler(LinkedBlockingQueue<SerialProtocol> serialInputQueue,
                           ConcurrentHashMap<Long, BlockingQueue<SerialProtocol>> responseSyncQueueMap,
                           ConcurrentHashMap<Long, ArrayList<ResponseListener>> responderListenerListMap) {

        this.serialInputQueue = serialInputQueue;

        this.responseSyncQueueMap = responseSyncQueueMap;
        this.responderListenerListMap = responderListenerListMap;
        this.responseMessage = new Message("");

        logger = new LoggerCollector().getInstance();
    }

    @Override
    public void run(){
        while(true){
                SerialProtocol response = null;
                try {
                    ArrayList listeners;
                    response = serialInputQueue.take();

                        logger.wrapper.log(Level.FINEST, "Response {0} taken from queue.", response.getAll());

                        if(response.getSyncFlag().equals(true)) {
                            responseSyncQueueMap.get(response.getThreadID()).add(response);
                        }
                        if(responderListenerListMap.get(response.getThreadID()) != null) {
                            listeners = responderListenerListMap.get(response.getThreadID());

                            if(listeners == null){
                                throw(new NoListenerException());
                            }
                            Iterator i = listeners.iterator();

                            // triggers all listeners witch are registrated
                            // for response-events for the thread with threadID
                            while (i.hasNext()) {
                                responseMessage.setText(response.getAll());
                                ((EventClassListener) i.next()).responseArrived(responseMessage);
                                logger.wrapper.log(Level.FINEST,
                                        "ResponseMessage {0} send to listener {1}.",
                                        new Object[]{responseMessage.getText(), i});
                            }
                        }
                        if(responderListenerListMap.get(-1L) != null){
                            listeners = responderListenerListMap.get(-1L);
                            Iterator iAll = listeners.iterator();

                            // triggers all listeners witch are registrated
                            // for all responses
                            while (iAll.hasNext()) {
                                responseMessage.setText(response.getAll());
                                ((EventClassListener) iAll.next()).responseArrived(responseMessage);
                            }
                        }

                } catch (NoListenerException e) {
                    logger.wrapper.log(Level.WARNING, "No listener found for thread {0}.", response.getThreadID());
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                } catch (InterruptedException e) {
                    logger.wrapper.log(Level.WARNING, "SerialInputQueue take-operation was interrupted.");
                    logger.wrapper.log(Level.FINE, "Stacktrace: ", e);
                }
        }
    }
}
