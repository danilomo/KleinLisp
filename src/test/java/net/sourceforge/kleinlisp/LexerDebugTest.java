package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LexerDebugTest extends BaseTestClass {

    @Test
    public void testParseDoubleLiteral() {
        LispObject result = lisp.evaluate("1.2");
        System.out.println("1.2 parsed as: " + result.getClass().getSimpleName() + " = " + result);
        System.out.println("Is DoubleObject: " + (result instanceof net.sourceforge.kleinlisp.objects.DoubleObject));
        System.out.println("Is IntObject: " + (result instanceof net.sourceforge.kleinlisp.objects.IntObject));
        assertTrue(result instanceof net.sourceforge.kleinlisp.objects.DoubleObject,
            "1.2 should be parsed as DoubleObject but was " + result.getClass().getSimpleName());
    }

    @Test
    public void testParseIntLiteral() {
        LispObject result = lisp.evaluate("42");
        System.out.println("42 parsed as: " + result.getClass().getSimpleName() + " = " + result);
        assertTrue(result instanceof net.sourceforge.kleinlisp.objects.IntObject,
            "42 should be parsed as IntObject but was " + result.getClass().getSimpleName());
    }

    @Test
    public void testParseZeroPointFive() {
        LispObject result = lisp.evaluate("0.5");
        System.out.println("0.5 parsed as: " + result.getClass().getSimpleName() + " = " + result);
        assertTrue(result instanceof net.sourceforge.kleinlisp.objects.DoubleObject,
            "0.5 should be parsed as DoubleObject but was " + result.getClass().getSimpleName());
    }

    @Test
    public void testAddMixedDebug() {
        // This is the key test - check that (+ 1 1.2) returns 2.2 as a double
        LispObject result = lisp.evaluate("(+ 1 1.2)");
        System.out.println("(+ 1 1.2) = " + result.getClass().getSimpleName() + " = " + result);
        assertTrue(result instanceof net.sourceforge.kleinlisp.objects.DoubleObject,
            "(+ 1 1.2) should return DoubleObject but was " + result.getClass().getSimpleName() + " = " + result);
        assertEquals("2.2", result.toString(),
            "(+ 1 1.2) should be 2.2 but was " + result);
    }

    @Test
    public void testNestedMixedDebug() {
        // Test nested expressions step by step
        LispObject mul = lisp.evaluate("(* 2 2.5)");
        assertEquals("5.0", mul.toString(), "(* 2 2.5) should be 5.0");
        assertTrue(mul instanceof net.sourceforge.kleinlisp.objects.DoubleObject,
            "(* 2 2.5) should be DoubleObject but was " + mul.getClass().getSimpleName());

        // Test adding a double literal to a variable holding a double
        lisp.evaluate("(define x (* 2 2.5))");
        LispObject addVar = lisp.evaluate("(+ x 1.5)");
        assertEquals("6.5", addVar.toString(), "(+ x 1.5) where x=5.0 should be 6.5");

        // Test nested expression directly
        LispObject nested = lisp.evaluate("(+ (* 2 2.5) 1.5)");
        assertEquals("6.5", nested.toString(), "(+ (* 2 2.5) 1.5) should be 6.5");
    }

    @Test
    public void testComparisonMixedDebug() {
        LispObject result = lisp.evaluate("(= 2 2.0)");
        System.out.println("(= 2 2.0) = " + result);
        assertEquals("true", result.toString(), "(= 2 2.0) should be true");
    }
}
