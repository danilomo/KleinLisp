package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DebugFilterTest extends BaseTestClass {
  @Test
  public void testFilterWithSetDebug() {
    lisp.evaluate("(define s (p-set 2 4 6 8))");

    // Test direct call
    LispObject result1 = lisp.evaluate("(s 2)");
    LispObject result2 = lisp.evaluate("(s 1)");

    debug("(s 2) type: " + result1.getClass().getName());
    debug("(s 1) type: " + result2.getClass().getName());
    debug("(s 2) == NIL: " + (result1 == net.sourceforge.kleinlisp.objects.ListObject.NIL));
    debug("(s 1) == NIL: " + (result2 == net.sourceforge.kleinlisp.objects.ListObject.NIL));
    debug("Truthiness of (s 2): " + result1.truthiness());
    debug("Truthiness of (s 1): " + result2.truthiness());
  }
}
