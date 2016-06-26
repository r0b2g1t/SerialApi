package serialApi.exceptions;

/**
 * Created by robert on 23.06.16.
 */
public class NoSyncResponseException extends Exception {

    public NoSyncResponseException() {

    }

    public String getMessage() {
        return "No response for synchronous request received before timed out.";
    }
}
