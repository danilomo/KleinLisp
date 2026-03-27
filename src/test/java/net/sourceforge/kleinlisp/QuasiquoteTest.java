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

/** Unit tests for quasiquote, unquote, and unquote-splicing. */
public class QuasiquoteTest extends BaseTestClass {

  @Test
  public void testSimpleQuasiquote() {
    assertEquals("(1 2 3)", lisp.evaluate("`(1 2 3)").toString());
  }

  @Test
  public void testQuasiquoteSymbol() {
    assertEquals("a", lisp.evaluate("`a").toString());
  }

  @Test
  public void testQuasiquoteEmptyList() {
    assertEquals("()", lisp.evaluate("`()").toString());
  }

  @Test
  public void testUnquote() {
    assertEquals("(1 2 3)", lisp.evaluate("`(1 ,(+ 1 1) 3)").toString());
  }

  @Test
  public void testUnquoteAtBeginning() {
    assertEquals("(5 2 3)", lisp.evaluate("`(,(+ 2 3) 2 3)").toString());
  }

  @Test
  public void testUnquoteAtEnd() {
    assertEquals("(1 2 6)", lisp.evaluate("`(1 2 ,(* 2 3))").toString());
  }

  @Test
  public void testUnquoteVariable() {
    lisp.evaluate("(define x 42)");
    assertEquals("(a 42 b)", lisp.evaluate("`(a ,x b)").toString());
  }

  @Test
  public void testUnquoteSplicing() {
    assertEquals("(1 2 3 4)", lisp.evaluate("`(1 ,@(list 2 3) 4)").toString());
  }

  @Test
  public void testUnquoteSplicingAtBeginning() {
    assertEquals("(1 2 3 4)", lisp.evaluate("`(,@(list 1 2) 3 4)").toString());
  }

  @Test
  public void testUnquoteSplicingAtEnd() {
    assertEquals("(1 2 3 4)", lisp.evaluate("`(1 2 ,@(list 3 4))").toString());
  }

  @Test
  public void testUnquoteSplicingEmpty() {
    assertEquals("(1 4)", lisp.evaluate("`(1 ,@(list) 4)").toString());
  }

  @Test
  public void testUnquoteSplicingVariable() {
    lisp.evaluate("(define xs (list 2 3 4))");
    assertEquals("(1 2 3 4 5)", lisp.evaluate("`(1 ,@xs 5)").toString());
  }

  @Test
  public void testNestedQuasiquote() {
    // Inner quasiquote should remain as quasiquote with unquote preserved
    String result = lisp.evaluate("`(a `(b ,(+ 1 2)))").toString();
    assertEquals("(a (quasiquote (b (unquote (+ 1 2)))))", result);
  }

  @Test
  public void testMixedUnquoteAndSplicing() {
    assertEquals("(a 3 4 5 b)", lisp.evaluate("`(a ,(+ 1 2) ,@(list 4 5) b)").toString());
  }

  @Test
  public void testQuasiquoteWithQuote() {
    assertEquals("(quote x)", lisp.evaluate("`(quote x)").toString());
  }

  @Test
  public void testQuasiquoteWithLet() {
    assertEquals("(1 10 3)", lisp.evaluate("(let ((x 10)) `(1 ,x 3))").toString());
  }

  @Test
  public void testQuasiquoteInDefine() {
    lisp.evaluate("(define (make-adder n) `(lambda (x) (+ x ,n)))");
    assertEquals("(lambda (x) (+ x 5))", lisp.evaluate("(make-adder 5)").toString());
  }

  @Test
  public void testQuasiquoteWithNestedList() {
    assertEquals("(a (b 3 c) d)", lisp.evaluate("`(a (b ,(+ 1 2) c) d)").toString());
  }

  @Test
  public void testQuasiquoteWithMultipleUnquotes() {
    assertEquals("(1 2 3)", lisp.evaluate("`(,(+ 0 1) ,(+ 1 1) ,(+ 1 2))").toString());
  }

  @Test
  public void testQuasiquoteWithFunctionCall() {
    lisp.evaluate("(define (square x) (* x x))");
    assertEquals("(result 16)", lisp.evaluate("`(result ,(square 4))").toString());
  }

  @Test
  public void testUnquoteSplicingMultiple() {
    assertEquals("(a 1 2 b 3 4 c)", lisp.evaluate("`(a ,@(list 1 2) b ,@(list 3 4) c)").toString());
  }

  @Test
  public void testDeepNesting() {
    lisp.evaluate("(define x 5)");
    assertEquals("((a 5) (b 5))", lisp.evaluate("`((a ,x) (b ,x))").toString());
  }

  @Test
  public void testQuasiquotePreservesStructure() {
    assertEquals(
        "(if (= x 0) 1 (* x (factorial (- x 1))))",
        lisp.evaluate("`(if (= x 0) 1 (* x (factorial (- x 1))))").toString());
  }
}
