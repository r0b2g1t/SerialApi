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

package serialApi.demo;

import serialApi.helper.Message;
import serialApi.listener.ResponseListener;
import serialApi.serial.SerialManager;

public class MessageReceiver implements Runnable {

    final SerialManager serialMgm;
    private final Message responseAll;
    private final Message responseCache;
    private ResponseListener responseListenerAll;

    private boolean running = true;

    public MessageReceiver(SerialManager serialMgm){
        this.serialMgm = serialMgm;
        responseAll = new Message("");
        responseCache = new Message("");
        responseListenerAll = new ResponseListener(responseAll);

    }

    @Override
    public void run() {
        serialMgm.addResponseListener( responseListenerAll, -1L);
        while (running){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!this.responseAll.getText().equals(this.responseCache.getText())){
                System.out.println("Response received in MessageReceiver: " + responseAll.getText());
                responseCache.setText(responseAll.getText());
            }
        }
    }

    public void terminate()
    {
        running = false;
        System.out.println("ReceiverThread terminating!");
    }
}
