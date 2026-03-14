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

import org.junit.jupiter.api.Test;

/**
 * Tests for the new enhancements added based on KleinLispGUI spec.
 *
 * @author danilo
 */
public class NewEnhancementsTest extends BaseTestClass {

  // === String Functions ===

  @Test
  public void testStringCiEqual() {
    assertTrue(lisp.evaluate("(string-ci=? \"Hello\" \"hello\")").truthiness());
    assertTrue(lisp.evaluate("(string-ci=? \"WORLD\" \"world\")").truthiness());
    assertFalse(lisp.evaluate("(string-ci=? \"Hello\" \"world\")").truthiness());
  }

  @Test
  public void testStringContainsAlias() {
    // Test that string-contains (without ?) works as alias
    assertTrue(lisp.evaluate("(string-contains \"hello\" \"ell\")").truthiness());
    assertFalse(lisp.evaluate("(string-contains \"hello\" \"xyz\")").truthiness());
  }

  // === List Functions ===

  @Test
  public void testNth() {
    assertEquals("b", lisp.evaluate("(nth 1 '(a b c))").asAtom().toString());
    assertEquals("a", lisp.evaluate("(nth 0 '(a b c))").asAtom().toString());
    assertEquals("c", lisp.evaluate("(nth 2 '(a b c))").asAtom().toString());
  }

  @Test
  public void testAssocRef() {
    // With proper lists
    assertEquals(
        "value", lisp.evaluate("(assoc-ref 'key '((key value) (other data)))").asAtom().toString());
    // Not found
    assertFalse(lisp.evaluate("(assoc-ref 'missing '((key value)))").truthiness());
  }

  @Test
  public void testFind() {
    assertEquals(3, lisp.evaluate("(find (lambda (x) (> x 2)) '(1 2 3 4))").asInt().value);
    assertFalse(lisp.evaluate("(find (lambda (x) (> x 10)) '(1 2 3 4))").truthiness());
    assertFalse(lisp.evaluate("(find (lambda (x) (> x 0)) '())").truthiness());
  }

  // === Let* and Letrec ===

  @Test
  public void testLetStar() {
    // let* allows sequential binding where later bindings can reference earlier ones
    assertEquals(3, lisp.evaluate("(let* ((x 1) (y (+ x 2))) y)").asInt().value);
    // a=1, b=(+ a 1)=2, c=(+ a b)=(+ 1 2)=3
    assertEquals(3, lisp.evaluate("(let* ((a 1) (b (+ a 1)) (c (+ a b))) c)").asInt().value);
    // More complex: a=2, b=(* a 3)=6, c=(+ a b)=(+ 2 6)=8
    assertEquals(8, lisp.evaluate("(let* ((a 2) (b (* a 3)) (c (+ a b))) c)").asInt().value);
  }

  @Test
  public void testLetrec() {
    // letrec allows mutually recursive functions
    String code =
        "(letrec ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1)))))"
            + "         (odd? (lambda (n) (if (= n 0) #f (even? (- n 1))))))"
            + "  (even? 4))";
    assertTrue(lisp.evaluate(code).truthiness());

    String code2 =
        "(letrec ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1)))))"
            + "         (odd? (lambda (n) (if (= n 0) #f (even? (- n 1))))))"
            + "  (odd? 3))";
    assertTrue(lisp.evaluate(code2).truthiness());
  }

  @Test
  public void testLetrecFactorial() {
    // Test recursive factorial in letrec
    String code =
        "(letrec ((fact (lambda (n) (if (= n 0) 1 (* n (fact (- n 1)))))))" + "  (fact 5))";
    assertEquals(120, lisp.evaluate(code).asInt().value);
  }

  // === Error Function ===

  @Test
  public void testError() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(error \"Something went wrong\")");
        });

    try {
      lisp.evaluate("(error \"Error:\" 42)");
      fail("Should have thrown LispArgumentError");
    } catch (LispArgumentError e) {
      assertTrue(e.getMessage().contains("Error:"));
      assertTrue(e.getMessage().contains("42"));
    }
  }
}
