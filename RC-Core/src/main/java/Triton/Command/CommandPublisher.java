package Triton.Command;

import java.util.Scanner;

public class CommandPublisher implements Runnable {

    CommandData commands = new CommandData();

    // taking in commands
    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            // First letter determine the type of command
            // S: switch
            // M B1 [0,0] 10: Move B1 to [0,0] with 10 units per ms
            String s = sc.nextLine();
            String[] commandString = s.split(" ");

            switch (commandString[0]) {
                case "S":
                    commands.add(new SwitchCommand());
                    break;
                case "M":
                    String robot = commandString[1];
                    break;
                default:
                    break;
            }
        }
    }
}