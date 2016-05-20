package serialApi.listener;

import serialApi.Message;

/**
 * Created by robert on 13.05.16.
 */
public class ResponseListener implements EventClassListener {
    private final Message response;
    public ResponseListener(Message response){
        this.response = response;
    }

    /**
     * @param Event Is the event-object witch receives the response message
     */
    @Override
    public void responseArrived(Message Event){
        System.out.println("response event: " + Event.getText());
        response.setText(Event.getText());
    }

}
