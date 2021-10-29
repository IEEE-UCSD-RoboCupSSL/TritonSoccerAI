package triton.manualTests.testUtil;

import triton.misc.math.linearAlgebra.Vec2D;

import java.util.Scanner;

public class TestUtil {
    public static boolean testVec2dEq(String testName, Vec2D expected, Vec2D actual, double precision) {

        if(actual == null) {
            System.out.printf("[Test PASSED] test: [%s] with expected = <%f, %f>, precision = %f, and actual = NULL\n",
                    testName, expected.x, expected.y, precision);
            return true;
        }

        boolean res = (actual.x < expected.x + precision) && (actual.x > expected.x - precision) && (actual.y < expected.y + precision) && (actual.y > expected.y - precision);
        if (res) {
            System.out.printf("[Test PASSED] test: [%s] with expected = <%f, %f>, precision = %f, and actual = <%f, %f>\n",
                    testName, expected.x, expected.y, precision, actual.x, actual.y);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = <%f, %f>, precision = %f, and actual = <%f, %f>\n",
                    testName, expected.x, expected.y, precision, actual.x, actual.y);
            return false;
        }

    }

    public static boolean testIntEq(String testName, int expected, int actual){
        if(expected == actual){
            System.out.printf("[Test PASSED] test: [%s] with expected = %d, and actual = %d\n", testName, expected, actual);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = %d, and actual = %d\n", testName, expected, actual);
            return false;
        }
    }

    public static boolean testDoubleEq(String testName, double expected, double actual, double precision) {
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
        try {
            res = actual.equals(expected);
        } catch (NullPointerException e) {
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
            System.out.printf("[Test PASSED] test: [%s] with expected = [%s], and actual = [%s]\n", testName, expected, actual);
            return true;
        } else {
            System.out.printf("[Test FAILED] test: [%s] with expected = [%s], and actual = [%s]\n", testName, expected, actual);
            return false;
        }
    }

    public static void enterKeyToContinue() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter any key to continue: ");
        String s = scanner.nextLine();
    }
}
