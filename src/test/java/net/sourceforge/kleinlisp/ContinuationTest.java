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

/** Tests for call/cc (escape continuations), dynamic-wind, and with-exception-handler. */
public class ContinuationTest extends BaseTestClass {

  // --- call/cc basic ---

  @Test
  public void testCallCcReturnNormally() {
    // When the continuation is never invoked, proc's return value is used
    assertEquals(42, evalAsInt("(call/cc (lambda (k) 42))"));
  }

  @Test
  public void testCallCcEscape() {
    // Classic escape: (+ 1 (k 42)) short-circuits, returns 42 not 43
    assertEquals(42, evalAsInt("(call/cc (lambda (k) (+ 1 (k 42))))"));
  }

  @Test
  public void testCallCcAbbrev() {
    assertEquals(7, evalAsInt("(call-with-current-continuation (lambda (k) (k 7)))"));
  }

  @Test
  public void testCallCcNestedEscape() {
    // Inner call/cc escapes to outer
    assertEquals(
        10,
        evalAsInt(
            "(call/cc (lambda (outer)"
                + "  (call/cc (lambda (inner) (inner 5)))"
                + "  (outer 10)))"));
  }

  @Test
  public void testCallCcEarlyExitFromForEach() {
    // Use call/cc to break out of for-each early
    assertEquals(
        3,
        evalAsInt(
            "(call-with-current-continuation"
                + "  (lambda (break)"
                + "    (for-each (lambda (x)"
                + "                (if (= x 3) (break x)))"
                + "              '(1 2 3 4 5))"
                + "    0))"));
  }

  @Test
  public void testCallCcEarlyExitFromList() {
    // Find first even number
    assertEquals(
        2,
        evalAsInt(
            "(call/cc"
                + "  (lambda (return)"
                + "    (for-each (lambda (x)"
                + "                (if (= (mod x 2) 0) (return x)))"
                + "              '(1 3 2 4 6))"
                + "    -1))"));
  }

  @Test
  public void testCallCcReturnFromBegin() {
    assertEquals(
        99,
        evalAsInt(
            "(call/cc"
                + "  (lambda (k)"
                + "    (begin"
                + "      (k 99)"
                + "      (error \"should not reach\"))))"));
  }

  @Test
  public void testCallCcProcedurePredicate() {
    // The captured continuation should pass procedure?
    assertTrue(evalAsBoolean("(call/cc (lambda (k) (procedure? k)))"));
  }

  @Test
  public void testContinuationPredicate() {
    assertTrue(evalAsBoolean("(call/cc (lambda (k) (continuation? k)))"));
  }

  @Test
  public void testContinuationPredicateFalseForLambda() {
    assertFalse(evalAsBoolean("(continuation? (lambda (x) x))"));
  }

  @Test
  public void testContinuationPredicateFalseForInt() {
    assertFalse(evalAsBoolean("(continuation? 42)"));
  }

  // --- dynamic-wind ---

  @Test
  public void testDynamicWindNormalExit() {
    lisp.evaluate("(define log '())");
    lisp.evaluate(
        "(dynamic-wind"
            + "  (lambda () (set! log (cons 'before log)))"
            + "  (lambda () (set! log (cons 'during log)))"
            + "  (lambda () (set! log (cons 'after log))))");
    // log built by cons prepend, so reverse gives chronological order
    assertEquals("(before during after)", lisp.evaluate("(reverse log)").toString());
  }

  @Test
  public void testDynamicWindEscapeRunsAfter() {
    lisp.evaluate("(define log '())");
    lisp.evaluate(
        "(call/cc"
            + "  (lambda (k)"
            + "    (dynamic-wind"
            + "      (lambda () (set! log (cons 'before log)))"
            + "      (lambda () (set! log (cons 'during log)) (k 'escaped))"
            + "      (lambda () (set! log (cons 'after log))))))");
    // after thunk must run even though escape continuation exits the thunk
    assertEquals("(before during after)", lisp.evaluate("(reverse log)").toString());
  }

  @Test
  public void testDynamicWindThunkReturnValue() {
    assertEquals(42, evalAsInt("(dynamic-wind (lambda () #f) (lambda () 42) (lambda () #f))"));
  }

  @Test
  public void testDynamicWindReentryRunsBefore() {
    // before must run again when a continuation re-enters the wind frame
    lisp.evaluate("(define log '())");
    lisp.evaluate("(define k #f)");
    lisp.evaluate(
        "(dynamic-wind"
            + "  (lambda () (set! log (cons 'in log)))"
            + "  (lambda () (call/cc (lambda (c) (set! k c))))"
            + "  (lambda () (set! log (cons 'out log))))");
    // Initial run: in, out
    assertEquals("(in out)", lisp.evaluate("(reverse log)").toString());
    // Re-invoke: enters again (in), exits again (out)
    lisp.evaluate("(k #f)");
    assertEquals("(in out in out)", lisp.evaluate("(reverse log)").toString());
  }

  @Test
  public void testDynamicWindCanonicalR7rs() {
    // Canonical R7RS §6.10 example using add-to-path style
    lisp.evaluate(
        "(define path '())"
            + "(define add (lambda (s) (set! path (cons s path))))"
            + "(define c #f)");
    lisp.evaluate(
        "(dynamic-wind"
            + "  (lambda () (add 'connect))"
            + "  (lambda () (add (call-with-current-continuation"
            + "                    (lambda (k0) (set! c k0) 'talk1))))"
            + "  (lambda () (add 'disconnect)))");
    // path built by cons-prepend: (disconnect talk1 connect)
    // If path length < 4, invoke c once more with talk2
    lisp.evaluate("(if (< (length path) 4) (c 'talk2) #f)");
    // Final path (reversed for reading): connect talk1 disconnect connect talk2 disconnect
    assertEquals(
        "(connect talk1 disconnect connect talk2 disconnect)",
        lisp.evaluate("(reverse path)").toString());
  }

  // --- with-exception-handler ---

  @Test
  public void testWithExceptionHandlerCatchesRaise() {
    // raise a string, handler should receive it
    assertEquals(
        "caught",
        evalAsString(
            "(with-exception-handler"
                + "  (lambda (e) \"caught\")"
                + "  (lambda () (raise \"oops\")))"));
  }

  @Test
  public void testWithExceptionHandlerReceivesRaisedValue() {
    // handler receives the exact raised object
    assertEquals(
        "oops",
        evalAsString(
            "(with-exception-handler" + "  (lambda (e) e)" + "  (lambda () (raise \"oops\")))"));
  }

  @Test
  public void testWithExceptionHandlerNoException() {
    assertEquals(42, evalAsInt("(with-exception-handler (lambda (e) -1) (lambda () 42))"));
  }

  // --- Interaction: call/cc inside guard ---

  @Test
  public void testCallCcInsideGuard() {
    // ContinuationException (extends Error) must not be swallowed by guard's RuntimeException catch
    assertEquals(
        99,
        evalAsInt(
            "(call/cc"
                + "  (lambda (escape)"
                + "    (guard (exn (#t 0))"
                + "      (escape 99)"
                + "      -1)))"));
  }

  @Test
  public void testCallCcOutsideGuardWithError() {
    // guard catches errors normally; call/cc escape bypasses guard
    assertEquals(
        "escaped",
        evalAsString(
            "(call/cc"
                + "  (lambda (k)"
                + "    (guard (e (#t \"caught\"))"
                + "      (k \"escaped\")"
                + "      \"unreachable\")))"));
  }

  // --- Nested continuations correctness ---

  @Test
  public void testCallCcDeepNesting() {
    // Three nested call/cc — innermost escapes to innermost, then middle escapes to middle
    lisp.evaluate("(define result 0)");
    lisp.evaluate(
        "(call/cc (lambda (a)"
            + "  (call/cc (lambda (b)"
            + "    (call/cc (lambda (c) (c 1)))"
            + "    (b 100)))"
            + "  (set! result 99)))");
    assertEquals(99, evalAsInt("result"));
  }

  // --- Multi-shot continuations (Phase 3 target) ---

  @Test
  public void testMultiShotContinuation() {
    // Capture k, return 11 first, then re-invoke k twice to get 21 and 31
    lisp.evaluate("(define k #f)");
    assertEquals(11, evalAsInt("(+ 1 (call/cc (lambda (c) (set! k c) 10)))"));
    assertEquals(21, evalAsInt("(k 20)"));
    assertEquals(31, evalAsInt("(k 30)"));
  }

  @Test
  public void testMultiShotContinuationPredicates() {
    // k captured via call/cc should satisfy both procedure? and continuation?
    lisp.evaluate("(define k2 #f)");
    lisp.evaluate("(call/cc (lambda (c) (set! k2 c)))");
    assertTrue(evalAsBoolean("(procedure? k2)"));
    assertTrue(evalAsBoolean("(continuation? k2)"));
  }

  @Test
  public void testMultiShotFromBegin() {
    // call/cc inside begin — continuation is the rest of the begin sequence
    lisp.evaluate("(define saved #f)");
    lisp.evaluate("(begin" + "  (call/cc (lambda (c) (set! saved c)))" + "  #f)");
    // Calling saved should produce #f (the last value of the begin)
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(saved 'ignored)"));
  }

  @Test
  public void testContinuationPredicateCpsStyle() {
    // continuation? must return #t for a CPS continuation, #f for a plain lambda
    lisp.evaluate("(define cps-k #f)");
    lisp.evaluate("(call/cc (lambda (k) (set! cps-k k)))");
    assertTrue(evalAsBoolean("(continuation? cps-k)"));
    assertFalse(evalAsBoolean("(continuation? (lambda (x) x))"));
  }
}
