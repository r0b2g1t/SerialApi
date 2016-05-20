package serialApi;

/**
 * Created by robert on 03.05.16.
 */
public class Message {

    private String text;

    /**
     * @param text  Set the object text value
     */
    public Message(String text) {
        this.text = text;
    }

    /**
     * @return      Returns text from the message object
     */
    public String getText() {
        return text;
    }

    /**
     * @param text  Sets the text of the message object
     */
    public void setText(String text) {
        this.text = text;
    }
}
