/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-19 00:58:53
 * @LastEditTime: 2020-10-25 23:12:21
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/RobotLogger.java
 */
import org.apache.log4j.*;

public class RobotLogger{
    private final static Logger logger = Looger.getLogger(RobotLogger.class);
    private FileAppender f = newFileAppender();

    public RobotLogger(String filename){
        f.setFile(filename);
    }

}