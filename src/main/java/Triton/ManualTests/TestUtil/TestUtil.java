package Triton.ManualTests.TestUtil;

import java.util.Scanner;

public class TestUtil {
    public static boolean testDoublesEq(String testName, double expected, double actual, double precision) {
        boolean res = (expected < actual + precision) && (expected > actual - precision);

        if (res) {
            System.out.printf("[Test PASSED] test: [%s] with expected = %f, precision = %f, and actual = %f\n", testName, expected, precision, actual);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = %f, precision = %f, and actual = %f\n", testName, expected, precision, actual);
            return false;
        }
    }

    public static boolean testStringEq(String testName, String expected, String actual) {
        boolean res;
        try{
             res = actual.equals(expected);
        } catch (NullPointerException e){
            res = false;
        }

        if (res) {
            System.out.printf("[Test PASSED] test: [%s] with expected = %s, and actual = %s\n", testName, expected, actual);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = %s, and actual = %s\n", testName, expected, actual);
            return false;
        }
    }

    public static boolean testReferenceEq(String testName, Object expected, Object actual) {
        boolean res = actual == expected;

        if (res) {
            System.out.printf("[Test PASSED] test: [%s] with expected = %s, and actual = %s\n", testName, expected, actual);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = %s, and actual = %s\n", testName, expected, actual);
            return false;
        }
    }

    public static void enterKeyToContinue() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter any key to continue: ");
        String s = scanner.nextLine();
    }
}
