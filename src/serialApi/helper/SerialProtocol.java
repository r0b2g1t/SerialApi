/**
 * MIT-license: https://opensource.org/licenses/MIT
 * Copyright (c) 2016 Robert Neumann
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package serialApi.helper;

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
