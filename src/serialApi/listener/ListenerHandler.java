package serialApi.listener;

import serialApi.Message;
import serialApi.Protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by robert on 13.05.16.
 */
public class ListenerHandler implements Runnable {
    private final ConcurrentHashMap<Long, BlockingQueue<Protocol>> responseQueueMap;
    private final ConcurrentHashMap<Long, ArrayList> responderListenerListMap;
    private final Message responseMessage;

    /**
     * @param responseQueueMap          ConcurrentHasMap witch holds the response queues of the threads
     * @param responderListenerListMap  ConcurrentHashMap witch holds the registrated listeners
     */
    public ListenerHandler(ConcurrentHashMap<Long, BlockingQueue<Protocol>> responseQueueMap,
                           ConcurrentHashMap<Long, ArrayList> responderListenerListMap) {

        this.responseQueueMap = responseQueueMap;
        this.responderListenerListMap = responderListenerListMap;
        this.responseMessage = new Message("");
    }

    @Override
    public void run(){
        while(true){
            for(Long key :  responseQueueMap.keySet()) {
                Protocol response = null;
                try {
                    ArrayList listeners;
                    if(!responseQueueMap.get(key).isEmpty()){
                        response = responseQueueMap.get(key).take();
                        System.out.println("ThreadID in Handler: " + response.getThreadID());
                        listeners = responderListenerListMap.get(response.getThreadID());
                        Iterator i = listeners.iterator();

                        // triggers all listeners witch are registrated for response-events for the thread with threadID
                        while(i.hasNext()){
                            responseMessage.setText(response.getAll());
                            ((EventClassListener) i.next()).responseArrived(responseMessage);
                        }
                    }
                } catch (NullPointerException e) {
                    assert response != null;
                    System.err.println( "No listener added for the threadID: " + response.getThreadID() +
                                        " - " + e.getClass() + " in " + this.getClass().getName());

                } catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
    }
}
