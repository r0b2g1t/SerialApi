package serialApi.exceptions;

/**
 * Created by robert on 21.05.16.
 */
public class NoListenerException extends Exception {

    public NoListenerException() {

    }

    public String getMessage(){
        return "No listener found";
    }
}
