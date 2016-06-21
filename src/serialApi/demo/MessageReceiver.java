package serialApi.demo;

import serialApi.helper.Message;
import serialApi.listener.ResponseListener;
import serialApi.serial.SerialManager;

/**
 * Created by robert on 21.06.16.
 */
public class MessageReceiver implements Runnable {

    final SerialManager serialMgm;
    private final Message responseAll;
    private final Message responseCache;
    private ResponseListener responseListenerAll;

    private boolean running = true;

    public MessageReceiver(SerialManager serialMgm){
        this.serialMgm = serialMgm;
        responseAll = new Message("");
        responseCache = new Message("");
        responseListenerAll = new ResponseListener(responseAll);

    }

    @Override
    public void run() {
        serialMgm.addResponseListener( responseListenerAll, -1L);
        while (running){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!this.responseAll.getText().equals(this.responseCache.getText())){
                System.out.println("Response received: " + responseAll.getText());
                responseCache.setText(responseAll.getText());
            }
        }
    }

    public void terminate()
    {
        running = false;
        System.out.println("ReceiverThread terminating!");
    }
}
