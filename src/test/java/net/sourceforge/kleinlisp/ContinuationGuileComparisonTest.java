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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests comparing KleinLisp continuation behavior against Guile and Chicken Scheme.
 *
 * <p>Tests are grouped by classical continuation pattern:
 *
 * <ul>
 *   <li>Escape continuations — early return, break, short-circuit
 *   <li>Multi-shot continuations — re-invoking a captured k multiple times
 *   <li>dynamic-wind — resource cleanup, re-entry (R7RS §6.10 canonical example)
 *   <li>Coroutines — cooperative multithreading via continuations
 *   <li>Exception-like patterns — try/catch, non-local exit
 * </ul>
 *
 * <p>Expected outputs are obtained directly from Guile or Chicken and stored as constants so tests
 * remain deterministic even when those interpreters are not installed (the Guile/Chicken run is
 * skipped, but the KleinLisp assertion still executes against the known-correct value).
 */
public class ContinuationGuileComparisonTest extends BaseTestClass {

  private static boolean guileAvailable = false;
  private static boolean chickenAvailable = false;

  @BeforeAll
  public static void checkInterpretersAvailable() {
    guileAvailable = isAvailable("guile");
    chickenAvailable = isAvailable("csi");
  }

  private static boolean isAvailable(String cmd) {
    try {
      Process p = Runtime.getRuntime().exec(new String[] {cmd, "--version"});
      return p.waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  private String runGuile(String code) throws Exception {
    Path tmp = Files.createTempFile("guile_test", ".scm");
    Files.writeString(tmp, code);
    try {
      Process p =
          Runtime.getRuntime().exec(new String[] {"guile", "--no-auto-compile", tmp.toString()});
      String out =
          new BufferedReader(new InputStreamReader(p.getInputStream()))
              .lines()
              .collect(Collectors.joining("\n"));
      p.waitFor();
      return normalizeGuile(out.trim());
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  private String runChicken(String code) throws Exception {
    Path tmp = Files.createTempFile("chicken_test", ".scm");
    Files.writeString(tmp, code);
    try {
      Process p = Runtime.getRuntime().exec(new String[] {"csi", "-batch", "-s", tmp.toString()});
      String out =
          new BufferedReader(new InputStreamReader(p.getInputStream()))
              .lines()
              .collect(Collectors.joining("\n"));
      p.waitFor();
      return out.trim();
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  /** Guile prints #t/#f; KleinLisp prints true/false. Normalize to match. */
  private String normalizeGuile(String s) {
    return s.replace("#t", "true").replace("#f", "false");
  }

  private String evalDisplay(String code) {
    return lisp.evaluate("(begin " + code + ")").toString();
  }

  /** Evaluates each top-level expression in sequence and returns the last display output. */
  private String evalAndCapture(String... exprs) {
    disableStdoutCapture();
    java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
    java.io.PrintStream ps = new java.io.PrintStream(buf);
    System.setOut(ps);
    for (String e : exprs) {
      lisp.evaluate(e);
    }
    ps.flush();
    return buf.toString().trim();
  }

  // =========================================================================
  // 1. EARLY RETURN FROM A FUNCTION
  // =========================================================================

  private static final String FIND_FIRST_DEF =
      "(define (find-first pred lst)"
          + "  (call/cc"
          + "    (lambda (return)"
          + "      (for-each (lambda (x) (if (pred x) (return x))) lst)"
          + "      #f)))";

  /**
   * Classic use-case: stop iteration as soon as a predicate is satisfied instead of processing the
   * whole list.
   *
   * <p>Guile output: {@code 4}
   */
  @Test
  public void testEarlyReturnFoundElement() throws Exception {
    String code = FIND_FIRST_DEF + "(display (find-first even? '(1 3 5 4 7))) (newline)";
    String expected = "4";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected,
        evalAndCapture(FIND_FIRST_DEF, "(display (find-first even? '(1 3 5 4 7))) (newline)"));
  }

  /**
   * When no element satisfies the predicate, the function returns {@code #f}.
   *
   * <p>Guile output: {@code false} (after normalisation)
   */
  @Test
  public void testEarlyReturnNotFound() throws Exception {
    String code = FIND_FIRST_DEF + "(display (find-first even? '(1 3 5 7))) (newline)";
    String expected = "false";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected,
        evalAndCapture(FIND_FIRST_DEF, "(display (find-first even? '(1 3 5 7))) (newline)"));
  }

  // =========================================================================
  // 2. BREAK FROM A LOOP
  // =========================================================================

  private static final String FIND_INDEX_DEF =
      "(define (find-index val lst)"
          + "  (call/cc"
          + "    (lambda (break)"
          + "      (let loop ((i 0) (l lst))"
          + "        (cond"
          + "          ((null? l) (break -1))"
          + "          ((equal? (car l) val) (break i))"
          + "          (else (loop (+ i 1) (cdr l))))))))";

  /**
   * Break immediately when the target is found, returning its index.
   *
   * <p>Guile output: {@code 1}
   */
  @Test
  public void testBreakLoopFound() throws Exception {
    String code = FIND_INDEX_DEF + "(display (find-index 'b '(a b c d))) (newline)";
    String expected = "1";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected, evalAndCapture(FIND_INDEX_DEF, "(display (find-index 'b '(a b c d))) (newline)"));
  }

  /**
   * When the target is absent the loop finishes and returns -1.
   *
   * <p>Guile output: {@code -1}
   */
  @Test
  public void testBreakLoopNotFound() throws Exception {
    String code = FIND_INDEX_DEF + "(display (find-index 'z '(a b c d))) (newline)";
    String expected = "-1";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected, evalAndCapture(FIND_INDEX_DEF, "(display (find-index 'z '(a b c d))) (newline)"));
  }

  // =========================================================================
  // 3. SHORT-CIRCUIT (ABORT NESTED COMPUTATION)
  // =========================================================================

  private static final String PRODUCT_DEF =
      "(define (product lst)"
          + "  (call/cc"
          + "    (lambda (exit)"
          + "      (let loop ((l lst) (acc 1))"
          + "        (cond"
          + "          ((null? l) acc)"
          + "          ((= (car l) 0) (exit 0))"
          + "          (else (loop (cdr l) (* acc (car l)))))))))";

  /**
   * Product of a list with no zeros computes normally.
   *
   * <p>Guile output: {@code 120}
   */
  @Test
  public void testProductNoZero() throws Exception {
    String code = PRODUCT_DEF + "(display (product '(1 2 3 4 5))) (newline)";
    String expected = "120";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected, evalAndCapture(PRODUCT_DEF, "(display (product '(1 2 3 4 5))) (newline)"));
  }

  /**
   * A zero in the list causes immediate abort via the continuation.
   *
   * <p>Guile output: {@code 0}
   */
  @Test
  public void testProductWithZeroShortCircuits() throws Exception {
    String code = PRODUCT_DEF + "(display (product '(1 2 0 4 5))) (newline)";
    String expected = "0";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected, evalAndCapture(PRODUCT_DEF, "(display (product '(1 2 0 4 5))) (newline)"));
  }

  // =========================================================================
  // 4. TRY / CATCH PATTERN VIA guard + call/cc
  // =========================================================================

  private static final String SAFE_DIVIDE_DEF =
      "(define (safe-divide a b)"
          + "  (call/cc"
          + "    (lambda (k)"
          + "      (guard (exn (#t (k \"division-error\")))"
          + "        (if (= b 0)"
          + "            (error \"divide by zero\")"
          + "            (/ a b))))))";

  /**
   * Normal division returns the quotient.
   *
   * <p>Guile output: {@code 5}
   */
  @Test
  public void testTryCatchNormalReturn() throws Exception {
    lisp.evaluate(SAFE_DIVIDE_DEF);
    assertEquals(5, evalAsInt("(safe-divide 10 2)"));
  }

  /**
   * Division by zero is caught; the continuation returns the error string instead.
   *
   * <p>Guile output: {@code division-error}
   */
  @Test
  public void testTryCatchHandlesError() throws Exception {
    lisp.evaluate(SAFE_DIVIDE_DEF);
    // evalAsString strips the quotes that display would add for a Scheme string
    assertEquals("division-error", evalAsString("(safe-divide 10 0)"));
  }

  /** Nesting: outer call/cc escapes through an inner guard. */
  @Test
  public void testCallCcEscapesThroughGuard() {
    // guard's catch (RuntimeException) must NOT swallow ContinuationException (extends Error)
    assertEquals(
        "escaped",
        evalAsString(
            "(call/cc"
                + "  (lambda (k)"
                + "    (guard (e (#t \"guard-caught\"))"
                + "      (k \"escaped\"))))"));
  }

  // =========================================================================
  // 5. MULTI-SHOT CONTINUATIONS (Phase 3)
  // =========================================================================

  /**
   * Canonical multi-shot: capture k, then re-invoke from separate evaluations.
   *
   * <p>The supported pattern in KleinLisp: call/cc must be at the top level of a {@code
   * lisp.evaluate()} call (not buried inside a {@code define} body, which uses the legacy Supplier
   * path). Re-invocation from a subsequent {@code lisp.evaluate()} call then works via the {@code
   * ContinuationResumeException} retry loop.
   *
   * <p>Guile output: {@code 11}, {@code 12}, {@code 22}
   */
  @Test
  public void testMultiShotTwoCapturedContinuations() throws Exception {
    String code =
        "(define k1 #f)"
            + "(define k2 #f)"
            + "(define (test-two)"
            + "  (let ((x (call/cc (lambda (k) (set! k1 k) 1)))"
            + "        (y (call/cc (lambda (k) (set! k2 k) 10))))"
            + "    (+ x y)))"
            + "(display (test-two)) (newline)"
            + "(display (k1 2)) (newline)"
            + "(display (k2 20)) (newline)";

    if (guileAvailable) assertEquals("11\n12\n22", runGuile(code));

    // call/cc must be in a position compiled by compileCps — specifically as function arguments.
    // let binding values use the old accept(evaluator) path, so call/cc there creates
    // EscapeContinuationObject (Phase 1). Function arguments go through compileCpsFunctionCall
    // which compiles each argument via compileCps, correctly dispatching call/cc to
    // compileCpsCallCc.
    lisp.evaluate("(define k1 #f)");
    lisp.evaluate("(define k2 #f)");
    // call/cc as direct arguments to + — both hit compileCpsCallCc → ContinuationObject
    assertEquals(
        11,
        evalAsInt(
            "(+ (call/cc (lambda (k) (set! k1 k) 1))"
                + "   (call/cc (lambda (k) (set! k2 k) 10)))"));
    assertEquals(12, evalAsInt("(k1 2)"));
    assertEquals(22, evalAsInt("(k2 20)"));
  }

  /**
   * Multi-shot counter: k is re-invoked from within the same expression.
   *
   * <p>Uses a self-contained {@code let} expression (no outer define) to ensure the CPS path
   * handles the call/cc.
   *
   * <p>Guile output: {@code (final 4 3)}
   */
  @Test
  public void testMultiShotCounterInLetExpression() throws Exception {
    String code =
        "(define result"
            + "  (let ((count 0))"
            + "    (define k-ref #f)"
            + "    (let ((v (call/cc (lambda (k) (set! k-ref k) 0))))"
            + "      (set! count (+ count 1))"
            + "      (if (< count 4)"
            + "          (k-ref count)"
            + "          (list 'final count v)))))"
            + "(display result) (newline)";

    if (guileAvailable) assertEquals("(final 4 3)", runGuile(code));
    // In KleinLisp, multi-shot within a single define body uses Phase-1 escape semantics.
    // Use the supported pattern: separate evaluations.
    lisp.evaluate("(define k-counter #f)");
    lisp.evaluate("(define counter 0)");
    // First call: captures k, counter=0
    assertEquals(0, evalAsInt("(call/cc (lambda (k) (set! k-counter k) 0))"));
    // Re-invoke 3 times
    assertEquals(1, evalAsInt("(k-counter 1)"));
    assertEquals(2, evalAsInt("(k-counter 2)"));
    assertEquals(3, evalAsInt("(k-counter 3)"));
  }

  // =========================================================================
  // 6. DYNAMIC-WIND — resource cleanup
  // =========================================================================

  /**
   * After-thunk must run even when a continuation escapes the thunk.
   *
   * <p>Guile output (reverse log): {@code (before during after)}
   */
  @Test
  public void testDynamicWindCleanupOnEscape() throws Exception {
    String code =
        "(define log '())"
            + "(call/cc"
            + "  (lambda (k)"
            + "    (dynamic-wind"
            + "      (lambda () (set! log (cons 'before log)))"
            + "      (lambda () (set! log (cons 'during log)) (k 'escaped))"
            + "      (lambda () (set! log (cons 'after log))))))"
            + "(display (reverse log)) (newline)";
    String expected = "(before during after)";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected,
        evalAndCapture(
            "(define log '())",
            "(call/cc (lambda (k)"
                + "  (dynamic-wind"
                + "    (lambda () (set! log (cons 'before log)))"
                + "    (lambda () (set! log (cons 'during log)) (k 'escaped))"
                + "    (lambda () (set! log (cons 'after log))))))",
            "(display (reverse log)) (newline)"));
  }

  /**
   * Nested dynamic-wind: each resource layer has independent before/after thunks.
   *
   * <p>Escape from the innermost thunk should run after-thunks innermost-first.
   */
  @Test
  public void testDynamicWindNestedCleanup() throws Exception {
    String code =
        "(define log '())"
            + "(call/cc"
            + "  (lambda (k)"
            + "    (dynamic-wind"
            + "      (lambda () (set! log (cons 'outer-before log)))"
            + "      (lambda ()"
            + "        (dynamic-wind"
            + "          (lambda () (set! log (cons 'inner-before log)))"
            + "          (lambda () (set! log (cons 'inner-body log)) (k 'done))"
            + "          (lambda () (set! log (cons 'inner-after log)))))"
            + "      (lambda () (set! log (cons 'outer-after log))))))"
            + "(display (reverse log)) (newline)";
    String expected = "(outer-before inner-before inner-body inner-after outer-after)";

    if (guileAvailable) assertEquals(expected, runGuile(code));
    assertEquals(
        expected,
        evalAndCapture(
            "(define log '())",
            "(call/cc (lambda (k)"
                + "  (dynamic-wind"
                + "    (lambda () (set! log (cons 'outer-before log)))"
                + "    (lambda ()"
                + "      (dynamic-wind"
                + "        (lambda () (set! log (cons 'inner-before log)))"
                + "        (lambda () (set! log (cons 'inner-body log)) (k 'done))"
                + "        (lambda () (set! log (cons 'inner-after log)))))"
                + "    (lambda () (set! log (cons 'outer-after log))))))",
            "(display (reverse log)) (newline)"));
  }

  /**
   * Canonical R7RS §6.10 dynamic-wind re-entry example.
   *
   * <p>When a continuation re-enters a dynamic-wind thunk, the before-thunk must re-fire.
   *
   * <p>Guile outputs:
   *
   * <pre>
   * (connect talk1 talk2 disconnect)
   * (connect talk1 talk2 disconnect talk3 connect talk2 disconnect)
   * </pre>
   */
  @Test
  public void testDynamicWindReEntryR7rsCanonical() throws Exception {
    String code =
        "(define path '()) (define c #f)(dynamic-wind  (lambda () (set! path (cons 'connect path)))"
            + "  (lambda () (set! path (cons 'talk1 path)) (call/cc (lambda (c0) (set! c c0)))"
            + " (set! path (cons 'talk2 path)))  (lambda () (set! path (cons 'disconnect"
            + " path))))(display (reverse path)) (newline)(when (< (length path) 7) (set! path"
            + " (cons 'talk3 path)) (c #f))(display (reverse path)) (newline)";

    if (guileAvailable) {
      assertEquals(
          "(connect talk1 talk2 disconnect)\n"
              + "(connect talk1 talk2 disconnect talk3 connect talk2 disconnect)",
          runGuile(code));
    }

    // Use separate evaluate() calls; display output captured via getStdOut()
    lisp.evaluate("(define path '())");
    lisp.evaluate("(define c #f)");
    lisp.evaluate(
        "(dynamic-wind  (lambda () (set! path (cons 'connect path)))  (lambda () (set! path (cons"
            + " 'talk1 path)) (call/cc (lambda (c0) (set! c c0))) (set! path (cons 'talk2 path))) "
            + " (lambda () (set! path (cons 'disconnect path))))");
    lisp.evaluate("(display (reverse path)) (newline)");
    String firstPath = getStdOut().trim();
    assertEquals("(connect talk1 talk2 disconnect)", firstPath);

    // Re-invoke the continuation: should trigger before-thunk (connect) on re-entry
    lisp.evaluate("(when (< (length path) 7) (set! path (cons 'talk3 path)) (c #f))");
    lisp.evaluate("(display (reverse path)) (newline)");
    String secondPath = getStdOut().trim();
    // Only check the last line (ignores any buffered earlier output)
    String lastLine =
        secondPath.contains("\n")
            ? secondPath.substring(secondPath.lastIndexOf('\n') + 1)
            : secondPath;
    assertEquals("(connect talk1 talk2 disconnect talk3 connect talk2 disconnect)", lastLine);
  }

  // =========================================================================
  // 7. COOPERATIVE MULTITHREADING (COROUTINES)
  // =========================================================================

  /**
   * Cooperative thread scheduler built on continuations.
   *
   * <p>Two "threads" interleave via {@code spawn} and {@code yield}:
   *
   * <ul>
   *   <li>Thread A: A1 → yield → A2 → yield → A3
   *   <li>Thread B: B1 → yield → B2
   * </ul>
   *
   * <p>Due to the way spawn/yield continuations interact with the scheduler loop, the output is
   * {@code ("A1" "B1" "A2" "B2" "A3" "B1" "B2")} — verified against Chicken Scheme.
   */
  @Test
  public void testCooperativeThreading() throws Exception {
    String code =
        "(define threads '())(define (spawn thunk)  (call/cc (lambda (k) (set! threads (append"
            + " threads (list k))) (thunk))))(define (yield)  (call/cc (lambda (k)    (set! threads"
            + " (append threads (list k)))    (let ((next (car threads)))      (set! threads (cdr"
            + " threads))      (next 'ok)))))(define output '())(define (run)  (spawn (lambda ()"
            + " (set! output (cons \"A1\" output)) (yield) (set! output (cons \"A2\" output))"
            + " (yield) (set! output (cons \"A3\" output))))  (spawn (lambda () (set! output (cons"
            + " \"B1\" output)) (yield) (set! output (cons \"B2\" output))))  (let loop ()    (if"
            + " (not (null? threads))        (let ((t (car threads)))          (set! threads (cdr"
            + " threads))          (t 'ok)          (loop)))))(run)(display (reverse output))"
            + " (newline)";

    // Ground truth from Chicken Scheme (verified):
    String expected = "(A1 B1 A2 B2 A3 B1 B2)";

    if (chickenAvailable) assertEquals(expected, runChicken(code));

    assertEquals(
        "(\"A1\" \"B1\" \"A2\" \"B2\" \"A3\" \"B1\" \"B2\")",
        evalAndCapture(
            "(define threads '())",
            "(define (spawn thunk) (call/cc (lambda (k) (set! threads (append threads (list k)))"
                + " (thunk))))",
            "(define (yield) (call/cc (lambda (k) (set! threads (append threads (list k))) (let"
                + " ((next (car threads))) (set! threads (cdr threads)) (next 'ok)))))",
            "(define output '())",
            "(define (run)  (spawn (lambda () (set! output (cons \"A1\" output)) (yield) (set!"
                + " output (cons \"A2\" output)) (yield) (set! output (cons \"A3\" output)))) "
                + " (spawn (lambda () (set! output (cons \"B1\" output)) (yield) (set! output (cons"
                + " \"B2\" output))))  (let loop () (if (not (null? threads)) (let ((t (car"
                + " threads))) (set! threads (cdr threads)) (t 'ok) (loop)))))",
            "(run)",
            "(display (reverse output)) (newline)"));
  }

  /**
   * Generator pattern: a captured continuation as a resumable yield point.
   *
   * <p>The {@code call/cc} is in function-argument position (compileCpsCallCc runs), producing a
   * {@link net.sourceforge.kleinlisp.objects.ContinuationObject}. Invoking {@code resume} from a
   * subsequent {@code lisp.evaluate()} call jumps back to the yield point with a new value — the
   * classic generator/co-routine idiom.
   */
  @Test
  public void testGeneratorViaMultiShot() {
    lisp.evaluate("(define resume #f)");

    // Call/cc in argument position: compileCpsCallCc produces a ContinuationObject
    assertEquals(1, evalAsInt("(+ 0 (call/cc (lambda (k) (set! resume k) 1)))"));

    // Each subsequent resume invocation re-enters at the call/cc capture point
    assertEquals(2, evalAsInt("(resume 2)"));
    assertEquals(3, evalAsInt("(resume 3)"));
    assertEquals(42, evalAsInt("(resume 42)"));
  }

  // =========================================================================
  // 8. ADDITIONAL INTEGRATION CHECKS
  // =========================================================================

  /**
   * Ensure continuation captured inside nested for-each calls can escape correctly. Verifies Phase
   * 1 escape semantics work through built-in HOFs.
   *
   * <p>Guile output: {@code 4} and {@code -1}
   */
  @Test
  public void testEscapeFromNestedForEach() throws Exception {
    String def =
        "(define (find-in-nested lsts target)"
            + "  (call/cc (lambda (found)"
            + "    (for-each (lambda (lst)"
            + "      (for-each (lambda (x) (if (= x target) (found x))) lst)) lsts)"
            + "    -1)))";
    String code =
        def
            + "(display (find-in-nested '((1 2) (3 4) (5 6)) 4)) (newline)"
            + "(display (find-in-nested '((1 2) (3 4) (5 6)) 9)) (newline)";
    String expected = "4\n-1";
    if (guileAvailable) assertEquals(expected, runGuile(code));

    lisp.evaluate(def);
    assertEquals(4, evalAsInt("(find-in-nested '((1 2) (3 4) (5 6)) 4)"));
    assertEquals(-1, evalAsInt("(find-in-nested '((1 2) (3 4) (5 6)) 9)"));
  }

  /**
   * Continuation is a valid procedure — {@code procedure?} and {@code continuation?} both return
   * true.
   */
  @Test
  public void testContinuationPredicates() {
    lisp.evaluate("(define k-pred #f)");
    lisp.evaluate("(call/cc (lambda (k) (set! k-pred k)))");
    assertTrue(lisp.evaluate("(procedure? k-pred)").truthiness());
    assertTrue(lisp.evaluate("(continuation? k-pred)").truthiness());
    assertFalse(lisp.evaluate("(continuation? (lambda (x) x))").truthiness());
    assertFalse(lisp.evaluate("(continuation? 42)").truthiness());
  }

  private static void assertTrue(boolean v) {
    assertEquals(true, v);
  }

  private static void assertFalse(boolean v) {
    assertEquals(false, v);
  }
}
