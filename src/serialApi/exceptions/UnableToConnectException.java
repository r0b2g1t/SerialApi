package serialApi.exceptions;

/**
 * Created by robert on 23.06.16.
 */
public class UnableToConnectException extends Exception {

    public UnableToConnectException() {

    }

    public String getMessage() {
        return ("Unable to connect to serial Port. Maybe in use.");
    }
}
