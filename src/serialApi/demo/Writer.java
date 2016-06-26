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

import serialApi.serial.SerialManager;

import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    private boolean running = true;

    private final BlockingQueue<String> inputQueue;

    private final SerialManager SerialMgm;

    public Writer (BlockingQueue<String> inputQueue, SerialManager SerialMgm){

        this.inputQueue = inputQueue;
        this.SerialMgm = SerialMgm;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(running){
            String request;
            try{
                request = inputQueue.take();
                SerialMgm.write(request);
                Thread.sleep(10);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void terminate()
    {
        running = false;
        System.out.println("Thread terminating!!!!!");
    }
}
