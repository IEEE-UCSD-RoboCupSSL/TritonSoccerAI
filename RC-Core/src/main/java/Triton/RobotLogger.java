/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-19 00:58:53
 * @LastEditTime: 2020-10-26 15:10:38
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/RobotLogger.java
 */
package Triton;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class RobotLogger{
    //private final static Logger logger = Logger.getLogger(RobotLogger.class);
    private FileAppender f = FileAppender.newBuilder()
            .setName("file")
            .setLayout(
                    PatternLayout.newBuilder()
                            .withPattern("[%d][%-5.-5p][%-14.-14c{1}:%4L] %-22.-22M - %m%n")
                            .build())
            .build();

    public RobotLogger(String filename){
        //f.setFile(filename);
    }

}