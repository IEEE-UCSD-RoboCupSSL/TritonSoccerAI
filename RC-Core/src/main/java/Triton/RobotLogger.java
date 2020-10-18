/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-19 00:58:53
 * @LastEditTime: 2020-10-18 11:00:10
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/RobotLogger.java
 */
import java.io.IOException; 
import java.util.logging.Level; 
import java.util.logging.Logger; 
import java.util.logging.*; 

public class RobotLogger{
    private final static Logger LOGGER = Looger.getLogger(Logger.GLOBAL_LOGGER);
    
    public void makeSomeLog() 
    { 
        LOGGER.log(Level.INFO, "Message for debugging");  
    }
}