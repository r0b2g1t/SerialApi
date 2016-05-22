package serialApi.helper;

/**
 * Created by robert on 14.05.16.
 */
public class SerialProtocol {
    private Long threadID;
    private Boolean syncFlag;
    private String request;
    private String response;

    /**
     * @param threadID  The threadID from the request thread.
     * @param request   The request information in the object.
     */
    public SerialProtocol(Long threadID, String request){
        this.threadID = threadID;
        this.request = request;
    }

    /**
     * @param threadID  Sets the threadID from the request thread.
     */
    public void setThreadID(Long threadID){
        this.threadID = threadID;
    }

    /**
     * @param request   Sets the request information.
     */
    public void setRequest(String request){
        this.request = request;
    }

    /**
     * @param response  Sets the response message.
     */
    public void setResponse(String response){
        this.response = response;
    }

    /**
     * @param syncFlag  Sets the flag witch indicat if the request synchronous(true) or asynchronous(false).
     */
    public void setSyncFlag(Boolean syncFlag){
        this.syncFlag = syncFlag;
    }

    /**
     * @return  the threadID of the request.
     */
    public Long getThreadID(){
        return threadID;
    }

    /**
     * @return  the request information.
     */
    public String getRequest(){
        return request;
    }

    /**
     * @return  the request message.
     */
    public String getResponse(){
        return response;
    }

    /**
     * @return  the flag witch indicat if the request synchronous(true) or asynchronous(false).
     */
    public Boolean getSyncFlag(){
        return syncFlag;
    }

    /**
     *  Flush the SerialProtocol object data.
     */
    public void flush(){
        this.threadID = null;
        this.request = null;
        this.response = null;
        this.syncFlag = null;
    }

    /**
     * @return  all information together.
     */
    public String getAll(){
        return String.valueOf(threadID) + ";" + request + ";" + response + ";" + syncFlag;
    }
}
