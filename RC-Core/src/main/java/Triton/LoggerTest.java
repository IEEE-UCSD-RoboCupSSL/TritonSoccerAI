/*
 * @Author: your name
 * @Date: 2020-10-25 22:28:29
 * @LastEditTime: 2020-10-25 23:27:38
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \Robo Cup\SimuBot\RC-Core\src\main\java\Triton\LoggerTest.java
 */
package Triton;
import org.apache.logging.log4j.*;

public class LoggerTest{
	private static Logger logger = LogManager.getLogger(LoggerTest.class);
	public static void main(String[] args) {
		System.out.println("\n      Start logger testing:   \n");
		logger.info("information");
		logger.error("error");
	}
}