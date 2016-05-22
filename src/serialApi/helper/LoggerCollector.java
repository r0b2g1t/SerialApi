package serialApi.helper;

import serialApi.serial.SerialConfig;

import java.io.IOException;
import java.util.logging.*;


/**
 * Created by robert on 22.05.16.
 */
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
