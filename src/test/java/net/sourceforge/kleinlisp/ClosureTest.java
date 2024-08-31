package net.sourceforge.kleinlisp;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class ClosureTest {

    private Lisp lisp;

    @Before
    public void setup() {
        lisp = new Lisp();
    }

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

    private int evalAsInt(String str) {
        return lisp.evaluate(str).asInt().get();
    }
}
