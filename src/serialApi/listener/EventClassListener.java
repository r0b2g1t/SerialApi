package serialApi.listener;

import serialApi.Message;

/**
 * Created by robert on 14.05.16.
 */
public interface EventClassListener {
    void responseArrived(Message e);
}
