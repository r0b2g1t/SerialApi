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

package serialApi.serial;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class SerialConfig{

    private final Properties config;
    private final List <Pattern> responsePatternList = new ArrayList<>();

    /**
     * @param file      Properties file input String
     */
    public SerialConfig (String file) {

        this();

        try{
            FileInputStream fileInputStream = new FileInputStream( file );
            config.load(fileInputStream);
            fileInputStream.close();

        }catch( Exception ioe ){
            System.out.println("Can not read configuration-file: " + file);
            ioe.printStackTrace();
            System.exit( 1 );
        }
    }

    /**
     * Configuration object witch holds the parameter set.
     */
    private SerialConfig() {
        config = new Properties();

    }

    /**
     * @return String           The port-name if the serial device in the configuration.
     */
    public String getPort()
    {
        return config.getProperty("SERIAL_PORT");
    }

    /**
     * @return Integer          The milliseconds to wait for serial connection is up.
     */
    public Integer getTimeoutMsWaitForOpen()
    {
        return Integer.parseInt( config.getProperty("TIMEOUT_MS_WAIT_FOR_OPEN"));
    }

    /**
     * @return Integer          The symbol rate for the connection.
     */
    public Integer getBaudRate()
    {
        return Integer.parseInt( config.getProperty("SERIAL_BAUD_RATE"));
    }

    /**
     * @return Integer          The number of data bits in a character.
     */
    public Integer getDataBits()
    {
        return Integer.parseInt( config.getProperty("SERIAL_DATA_BITS"));
    }

    /**
     * @return Integer          The number of stop bits for the signaling of the end of a character.
     */
    public Integer getStopBits()
    {
        return Integer.parseInt( config.getProperty("SERIAL_STOP_BITS"));
    }

    /**
     * @return Integer          The error detection of the transmission.
     *                          PARITY NONE=0, ODD=1, Even=2
     */
    public Integer getParity()
    {
        return Integer.parseInt( config.getProperty("SERIAL_PARITY"));
    }

    /**
     * @return Pattern          The tag witch indicates that the transmitted data is a notification.
     */
    public Pattern getNotificationPattern(){
        return Pattern.compile(config.getProperty("NOTIFICATION_TAG"));
    }

    /**
     * @return Integer          The milliseconds to wait for response of a synchronous request.
     */
    public Integer getSyncResponseTimeout()
    {
        return Integer.parseInt( config.getProperty("SYNC_RESPONSE_TIMEOUT"));
    }

    /**
     * @return String           The path to the logfile.
     */
    public String getLogPath(){
        return config.getProperty("LOG_PATH");
    }

    /**
     * @return String           The level of logging for the loggerWrapper.
     */
    public String getLogLevel(){
        return config.getProperty("LOG_LEVEL");
    }

    /**
     * @return String           The state of the logger true for on, false for off.
     */
    public String getSystemLog() {
        return config.getProperty("SYSTEM_LOG");
    }

    /**
     * @return String           The path to the system logfile.
     */
    public String getSystemLogPath(){
        return config.getProperty("SYSTEM_LOG_PATH");
    }

    /**
     * @return List&lt;Pattern&gt;    A list of patterns for the response parsing from response input data.
     */
    public List<Pattern> getResponseRegEx(){

        if(responsePatternList.isEmpty()){
            for(int i=0; config.containsKey("REG_EX" + Integer.toString(i)); i++){
                responsePatternList.add(Pattern.compile(config.getProperty("REG_EX" + Integer.toString(i))));
            }
            return responsePatternList;
        }else {
            return responsePatternList;
        }
    }

    /**
     *
     * SET METHODS
     *
     */

    /**
     * @param port  Sets the serial device COM-port.
     */
    public void setPort(String port)
    {
        config.setProperty("SERIAL_PORT", port);
    }

    /**
     * @param timeoutMSWaitForOpen  Sets the Milliseconds-to-wait-for-open-connection-timeout-variable.
     */
    public void setTimeoutMSWaitForOpen(Integer timeoutMSWaitForOpen)
    {
        config.setProperty("TIMEOUT_MS_WAIT_FOR_OPEN", timeoutMSWaitForOpen.toString());
    }

    /**
     * @param baudRate  Sets the symbol rate for the connection.
     */
    public void setBaudRate(Integer baudRate)
    {
        config.setProperty("SERIAL_BAUD_RATE",baudRate.toString());
    }


    /**
     * @param dataBits  Sets the number of data bits in a character.
     */
    public void setDataBits(Integer dataBits)
    {
        config.setProperty("SERIAL_DATA_BITS",dataBits.toString());
    }


    /**
     * @param stopBits  Set the number of stop bits for the signaling of the end of a character.
     */
    public void setStopBits(Integer stopBits) {
        config.setProperty("SERIAL_STOP_BITS", stopBits.toString());
    }

    /**
     * @param parity    Sets the error detection of the transmission.
     *                  PARITY NONE=0, ODD=1, Even=2
     */
    public void setParity(Integer parity)
    {
        config.setProperty("SERIAL_PARITY", parity.toString());
    }

    /**
     * @param notificationTag   Sets the tag witch indicates that the transmitted data is a notification.
     */
    public void setNotificationPattern(String notificationTag){
        config.setProperty("NOTIFICATION_TAG", notificationTag);
    }

    /**
     * @param logPath   Sets the path to the logfile.
     */
    public void setLogPath(String logPath){
        config.setProperty("LOG_PATH", logPath);
    }

    /**
     * @param logLevel  Sets the Level of logging for the loggerWrapper.
     */
    public void setLogLevel(String logLevel){
        config.setProperty("LOG_LEVEL", logLevel);
    }

    /**
     * @param onOff Switch the system logger on (true) or off(false).
     */
    public void setSystemLog(String onOff){
        config.setProperty("SYSTEM_LOG", onOff);
    }

    /**
     * @param systemLogPath    Sets the path of the system logfile.
     */
    public void setSystemLogPath(String systemLogPath){
        config.setProperty("SYSTEM_LOG_PATH", systemLogPath);
    }

    /**
     * @param responsePattern   Add the string to response pattern-list.
     */
    public void addResponsePattern(String responsePattern){
        responsePatternList.add(Pattern.compile(responsePattern));
    }

    /**
     * @param syncResponseTimeout  Sets the Milliseconds to wait for a response of a synchronous request.
     */
    public void setSyncResponseTimeout(Integer syncResponseTimeout)
    {
        config.setProperty("SYNC_RESPONSE_TIMEOUT", syncResponseTimeout.toString());
    }
}
