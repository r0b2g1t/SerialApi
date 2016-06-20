package serialApi.listener;

import serialApi.helper.Message;

/**
 * Created by robert on 13.05.16.
 */
public class ResponseListener implements EventClassListener {
    private final Message response;
    public ResponseListener(Message response){
        this.response = response;
    }

    /**
     * @param event Is the event-object witch receives the response message
     */
    @Override
    public void responseArrived(Message event){
        response.setText(event.getText());
    }
}
