package serialApi;

/**
 * Created by robert on 14.05.16.
 */
public class Protocol {
    private Long threadID;
    private String request;
    private String response;

    public Protocol(Long threadID, String request){
        this.threadID = threadID;
        this.request = request;
    }

    public void setThreadID(Long threadID){
        this.threadID = threadID;
    }

    public void setRequest(String request){
        this.request = request;
    }

    public void setResponse(String response){
        this.response = response;
    }

    public Long getThreadID(){
        return threadID;
    }

    public String getRequest(){
        return request;
    }

    public String getResponse(){
        return response;
    }
    public void flush(){
        this.threadID = null;
        this.request = null;
        this.response = null;
    }
    public String getAll(){
        return String.valueOf(threadID) + ";" + request + ";" + response;
    }
}
