/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-19 00:58:53
 * @LastEditTime: 2020-11-08 20:50:10
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/RobotLogger.java
 */
package Triton;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class RobotLogger{
    //private final static Logger logger = Logger.getLogger(RobotLogger.class);
    //private FileAppender f = FileAppender.newBuilder()
    //         .setName("testFile")
    //         .setLayout(
    //                 PatternLayout.newBuilder()
    //                         .withPattern("[%d][%-5.-5p][%-14.-14c{1}:%4L] %-22.-22M - %m%n")
    //                         .build())
    //         .build();
    public Logger logger = LogManager.getLogger(RobotLogger.class);

    public RobotLogger(){
        //f.setFile(filename);
    }
    public void info(String info) {
        logger.info(info);
    }

}