package net.sourceforge.kleinlisp;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class FunctionsTest {

    private Lisp lisp;

    public FunctionsTest() {
    }

    @Before
    public void setup() {
        lisp = new Lisp();
    }

    @Test
    public void testRecursiveFunction() {
        lisp.evaluate("(define (fib n)\n"
                + "  (if (< n 2)\n"
                + "      n\n"
                + "      (+ (fib (- n 1)) (fib (- n 2)))))");

        assertEquals(
                evalAsInt("(fib 10)"),
                55
        );
    }

    //@Test
    public void testClosure() {
        lisp.evaluate("(define (new-counter)\n"
                + "  (let ((i 0))\n"
                + "    (lambda ()\n"
                + "      (set! i (+ i 1))\n"
                + "      i)))");
        lisp.evaluate("(define c1 (new-counter))");

        assertEquals(
                evalAsInt("(c1)"),
                1
        );

        assertEquals(
                evalAsInt("(c2)"),
                2
        );
    }
    
    @Test
    public void testDefineWithLet() {
        lisp.evaluate("(define (foo a b) (let ((c (+ a b))) (* c c)))");
        
        assertEquals(
                evalAsInt("(foo 1 1)"),
                4
        );
    }
    
    //@Test
    public void testMultiExpressionFunction() {
        lisp.evaluate("(define (foo) (print 1) (print 2) 3)");
        
        assertEquals(
                evalAsInt("(foo 1 1)"),
                4
        );
    }

    private int evalAsInt(String str) {
        return lisp.evaluate(str).asInt().get();
    }
}
