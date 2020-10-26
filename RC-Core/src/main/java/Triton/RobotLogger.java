/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-19 00:58:53
 * @LastEditTime: 2020-10-25 23:36:01
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/RobotLogger.java
 */
package Triton;
import org.apache.log4j.*;

public class RobotLogger{
    private final static Logger logger = Looger.getLogger(RobotLogger.class);
    private FileAppender f = new FileAppender();

    public RobotLogger(String filename){
        f.setFile(filename);
    }

}