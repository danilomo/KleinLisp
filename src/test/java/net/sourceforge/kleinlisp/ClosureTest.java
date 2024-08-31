package net.sourceforge.kleinlisp;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class ClosureTest extends BaseTestClass {

    @Test
    public void testClosure() {
        lisp.evaluate("(define (foo val)\n"
                + "   ((lambda () (set! val (+ val 10)) (println val)))\n"
                + "    val)");
        assertEquals(
                evalAsInt("(foo 10)"),
                20
        );
    }
}
