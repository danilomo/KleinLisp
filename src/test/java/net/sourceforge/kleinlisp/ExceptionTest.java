/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import net.sourceforge.kleinlisp.objects.BooleanObject;
import org.junit.jupiter.api.Test;

/** Tests for R7RS exception handling: guard, raise, raise-continuable, and error objects. */
public class ExceptionTest extends BaseTestClass {

  @Test
  public void testRaiseAndGuard() {
    assertEquals("caught", lisp.evaluate("(guard (e (else 'caught)) (raise 'error))").toString());
  }

  @Test
  public void testGuardWithStringCondition() {
    assertEquals(
        "string-error",
        lisp.evaluate(
                "(guard (e "
                    + "        ((string? e) 'string-error) "
                    + "        ((number? e) 'number-error)) "
                    + "  (raise \"oops\"))")
            .toString());
  }

  @Test
  public void testGuardWithNumberCondition() {
    assertEquals(
        "number-error",
        lisp.evaluate(
                "(guard (e "
                    + "        ((string? e) 'string-error) "
                    + "        ((number? e) 'number-error)) "
                    + "  (raise 42))")
            .toString());
  }

  @Test
  public void testGuardNoMatchReraises() {
    // No matching handler - exception should propagate
    assertThrows(
        LispRaisedException.class,
        () -> {
          lisp.evaluate("(guard (e ((string? e) 'caught)) (raise 42))");
        });
  }

  @Test
  public void testGuardBodySucceeds() {
    // No exception raised - body result is returned
    assertEquals("42", lisp.evaluate("(guard (e (else 'caught)) 42)").toString());
  }

  @Test
  public void testErrorCreatesErrorObject() {
    assertEquals(
        "caught-error",
        lisp.evaluate(
                "(guard (e ((error-object? e) 'caught-error)) " + "  (error \"message\"))")
            .toString());
  }

  @Test
  public void testErrorObjectMessage() {
    assertEquals(
        "\"test message\"",
        lisp.evaluate(
                "(guard (e ((error-object? e) (error-object-message e))) "
                    + "  (error \"test message\" 1 2 3))")
            .toString());
  }

  @Test
  public void testErrorObjectIrritants() {
    assertEquals(
        "(1 2 3)",
        lisp.evaluate(
                "(guard (e ((error-object? e) (error-object-irritants e))) "
                    + "  (error \"msg\" 1 2 3))")
            .toString());
  }

  @Test
  public void testGuardExpressionResult() {
    // When handler has no expressions, return test result
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(guard (e ((= e 42))) (raise 42))"));
  }

  @Test
  public void testGuardMultipleExpressions() {
    assertEquals(
        "3",
        lisp.evaluate(
                "(guard (e "
                    + "        (else "
                    + "          (+ 1 1) " // Evaluated but discarded
                    + "          (+ 1 2)))" // Last expression returned
                    + "  (raise 'anything))")
            .toString());
  }

  @Test
  public void testNestedGuard() {
    assertEquals(
        "inner",
        lisp.evaluate(
                "(guard (outer "
                    + "        (else 'outer)) "
                    + "  (guard (inner "
                    + "          (else 'inner)) "
                    + "    (raise 'error)))")
            .toString());
  }

  @Test
  public void testGuardReraiseFromInner() {
    assertEquals(
        "outer",
        lisp.evaluate(
                "(guard (outer "
                    + "        (else 'outer)) "
                    + "  (guard (inner "
                    + "          ((number? inner) 'inner)) "
                    + "    (raise 'symbol)))" // Not a number, so outer catches it
            )
            .toString());
  }

  @Test
  public void testErrorObjectPredicate() {
    assertEquals(
        BooleanObject.TRUE,
        lisp.evaluate("(guard (e (else (error-object? e))) " + "  (error \"test\"))"));
  }

  @Test
  public void testRaiseNonError() {
    assertEquals(
        BooleanObject.FALSE,
        lisp.evaluate("(guard (e (else (error-object? e))) " + "  (raise 42))"));
  }

  @Test
  public void testGuardVariableBinding() {
    assertEquals(
        "100",
        lisp.evaluate("(guard (val " + "        ((> val 50) val)) " + "  (raise 100))").toString());
  }

  @Test
  public void testRaiseContinuable() {
    // Without call/cc, raise-continuable behaves like raise
    assertEquals(
        "caught",
        lisp.evaluate("(guard (e (else 'caught)) (raise-continuable 'error))").toString());
  }

  @Test
  public void testGuardWithMultipleBodyExpressions() {
    lisp.evaluate("(define result 0)");
    lisp.evaluate(
        "(guard (e (else 'caught)) "
            + "  (set! result 1) "
            + "  (set! result 2) "
            + "  (set! result 3))");
    assertEquals("3", lisp.evaluate("result").toString());
  }

  @Test
  public void testGuardWithRaiseInMiddle() {
    lisp.evaluate("(define result 0)");
    assertEquals(
        "caught",
        lisp.evaluate(
                "(guard (e (else 'caught)) "
                    + "  (set! result 1) "
                    + "  (raise 'error) "
                    + "  (set! result 2))")
            .toString());
    // Only the first set! should have executed
    assertEquals("1", lisp.evaluate("result").toString());
  }

  @Test
  public void testErrorWithNoIrritants() {
    assertEquals(
        "()",
        lisp.evaluate(
                "(guard (e ((error-object? e) (error-object-irritants e))) " + "  (error \"msg\"))")
            .toString());
  }

  @Test
  public void testGuardWithSymbolCondition() {
    assertEquals(
        "symbol-error",
        lisp.evaluate(
                "(guard (e "
                    + "        ((symbol? e) 'symbol-error) "
                    + "        (else 'other)) "
                    + "  (raise 'my-symbol))")
            .toString());
  }

  @Test
  public void testGuardWithListRaised() {
    assertEquals(
        "(1 2 3)",
        lisp.evaluate("(guard (e ((list? e) e) (else 'not-list)) " + "  (raise '(1 2 3)))")
            .toString());
  }

  @Test
  public void testDeeplyNestedGuard() {
    assertEquals(
        "level3",
        lisp.evaluate(
                "(guard (e1 (else 'level1)) "
                    + "  (guard (e2 (else 'level2)) "
                    + "    (guard (e3 (else 'level3)) "
                    + "      (raise 'error))))")
            .toString());
  }

  @Test
  public void testGuardInFunction() {
    lisp.evaluate(
        "(define (safe-div a b) "
            + "  (guard (e (else 0)) "
            + "    (/ a b)))");
    assertEquals("5", lisp.evaluate("(safe-div 10 2)").toString());
    // Division by zero would throw, caught by guard
    assertDoesNotThrow(() -> lisp.evaluate("(safe-div 10 0)"));
  }

  @Test
  public void testRaiseWithDifferentTypes() {
    assertEquals(
        "42",
        lisp.evaluate("(guard (e ((number? e) e)) " + "  (raise 42))").toString());
    assertEquals(
        "\"hello\"",
        lisp.evaluate("(guard (e ((string? e) e)) " + "  (raise \"hello\"))").toString());
    assertEquals(
        BooleanObject.TRUE,
        lisp.evaluate("(guard (e ((boolean? e) e)) " + "  (raise #t))"));
  }

  @Test
  public void testErrorObjectToString() {
    String result = lisp.evaluate(
        "(guard (e (else e)) (error \"test message\" 1 2 3))").toString();
    assertTrue(result.contains("error"));
    assertTrue(result.contains("test message"));
  }
}
