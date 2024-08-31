package net.sourceforge.kleinlisp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Danilo Oliveira
 */
public class BasicFunctionsTest  extends BaseTestClass {



    @Test
    public void testArithmetic() {
        Object[][] expressions = {
                {"(+ 14 5)", 19},
                {"(+ 1 2 3 4)", 10},
                {"(- (+ 3 4) 7)", 0},
                {"(* (+ 2 5) (- 7 (/ 21 7)))", 28}
        };

        for (Object[] arr : expressions) {
            int result = lisp.evaluate(arr[0].toString()).asInt().get();
            int expected = (Integer) arr[1];

            System.out.println(result + ", " + expected);

            assertEquals(result, expected);
        }
    }

    @Test
    public void testComparison() {
        Object[][] expressions = {
            {"(= (+ 2 3) 5)", true},
            {"(> (* 5 6) (+ 4 5))", true}
        };

        for (Object[] arr : expressions) {
            boolean result = lisp.evaluate(arr[0].toString()).truthiness();
            boolean expected = (Boolean) arr[1];

            System.out.println(result + ", " + expected);

            assertEquals(result, expected);
        }
    }
}
