/**Copyright (c) 2016 Robert Neumann
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

import serialApi.serial.SerialConfig;

import java.io.IOException;
import java.util.logging.*;

public class LoggerCollector {

    public final Logger wrapper = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private SerialConfig CONFIGURATION;

    public LoggerCollector(){
        CONFIGURATION = null;
    }
    private static LoggerCollector instance = null;

    public LoggerCollector getInstance() {
        if(instance == null) {
            prepare(CONFIGURATION);
            instance = new LoggerCollector ();
        }
        return instance;
    }

    public LoggerCollector prepare(SerialConfig CONFIGURATION) {
        this.CONFIGURATION = CONFIGURATION;
        try {
            FileHandler myFileHandler = new FileHandler(CONFIGURATION.getLogPath());
            myFileHandler.setFormatter(new SimpleFormatter());
            myFileHandler.setLevel(Level.parse(CONFIGURATION.getLogLevel()));

            if(CONFIGURATION.getSystemLog().equals("true")) {
                FileHandler systemLogFileHandler = new FileHandler(CONFIGURATION.getSystemLogPath());
                systemLogFileHandler.setFormatter(new SimpleFormatter());
                systemLogFileHandler.setLevel(Level.FINEST);
                wrapper.addHandler(systemLogFileHandler);
            }

            wrapper.addHandler(myFileHandler);
            wrapper.setUseParentHandlers(false);
            wrapper.setLevel(Level.FINEST);
        } catch (IOException e){
            wrapper.log(Level.WARNING, "File {0} inaccessible IO error", CONFIGURATION.getLogPath());
            wrapper.log(Level.FINE, "Stacktrace: ", e);
        }
        instance = new LoggerCollector();
        return instance;
    }
}
