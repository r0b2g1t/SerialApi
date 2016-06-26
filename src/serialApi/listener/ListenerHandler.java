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
