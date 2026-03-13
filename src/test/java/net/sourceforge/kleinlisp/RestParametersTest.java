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

import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Test;

/** Tests for rest parameters (variadic functions with . rest syntax). */
public class RestParametersTest extends BaseTestClass {

  @Test
  public void testBasicRestParameter() {
    lisp.evaluate("(define (collect . rest) rest)");
    ListObject result = evalAsList("(collect 1 2 3)");
    assertEquals(3, result.length());
    assertEquals(1, result.car().asInt().value);
  }

  @Test
  public void testRestParameterEmpty() {
    lisp.evaluate("(define (collect . rest) rest)");
    ListObject result = evalAsList("(collect)");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testRestWithRegularParams() {
    lisp.evaluate("(define (func a b . rest) rest)");
    ListObject result = evalAsList("(func 1 2 3 4 5)");
    assertEquals(3, result.length());
    assertEquals(3, result.car().asInt().value);
  }

  @Test
  public void testRestWithRegularParamsEmpty() {
    lisp.evaluate("(define (func a b . rest) rest)");
    ListObject result = evalAsList("(func 1 2)");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testRestParameterAccess() {
    lisp.evaluate("(define (first-rest . rest) (car rest))");
    assertEquals(10, evalAsInt("(first-rest 10 20 30)"));
  }

  @Test
  public void testRestParameterLength() {
    lisp.evaluate("(define (count-rest . rest) (length rest))");
    assertEquals(5, evalAsInt("(count-rest 1 2 3 4 5)"));
    assertEquals(0, evalAsInt("(count-rest)"));
  }

  @Test
  public void testRestWithSingleRegularParam() {
    lisp.evaluate("(define (func a . rest) (list a rest))");
    ListObject result = evalAsList("(func 1 2 3)");
    assertEquals(2, result.length());
    assertEquals(1, result.car().asInt().value);
    assertEquals(2, result.cdr().car().asList().length());
  }

  @Test
  public void testRestParameterSum() {
    lisp.evaluate("(define (sum-all first . rest) (fold-left + first rest))");
    assertEquals(15, evalAsInt("(sum-all 1 2 3 4 5)"));
    assertEquals(10, evalAsInt("(sum-all 10)"));
  }

  @Test
  public void testRestInLambda() {
    LispObject result = lisp.evaluate("((lambda (a . rest) rest) 1 2 3 4)");
    ListObject list = result.asList();
    assertEquals(3, list.length());
    assertEquals(2, list.car().asInt().value);
  }

  @Test
  public void testRestInLambdaReturnFirst() {
    assertEquals(1, evalAsInt("((lambda (a . rest) a) 1 2 3 4)"));
  }

  @Test
  public void testRestInLambdaEmpty() {
    ListObject result = evalAsList("((lambda (a . rest) rest) 1)");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testNestedFunctionWithRest() {
    lisp.evaluate("(define (outer . args) (apply + args))");
    assertEquals(10, evalAsInt("(outer 1 2 3 4)"));
  }

  @Test
  public void testRestWithMap() {
    lisp.evaluate("(define (double-all . nums) (map (lambda (x) (* x 2)) nums))");
    ListObject result = evalAsList("(double-all 1 2 3)");
    assertEquals(3, result.length());
    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testRestWithFilter() {
    lisp.evaluate("(define (filter-positive . nums) (filter (lambda (x) (> x 0)) nums))");
    ListObject result = evalAsList("(filter-positive -1 2 -3 4 -5)");
    assertEquals(2, result.length());
    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
  }

  @Test
  public void testRestPreservesOrder() {
    lisp.evaluate("(define (get-rest a . rest) rest)");
    ListObject result = evalAsList("(get-rest 0 1 2 3 4 5)");
    for (int i = 1; i <= 5; i++) {
      assertEquals(i, result.car().asInt().value);
      result = result.cdr();
    }
  }

  @Test
  public void testRestWithStrings() {
    lisp.evaluate("(define (join-all . strings) (apply string-append strings))");
    LispObject result = lisp.evaluate("(join-all \"a\" \"b\" \"c\")");
    assertEquals("abc", result.asString().value());
  }

  @Test
  public void testRestWithMixedTypes() {
    lisp.evaluate("(define (collect-all . items) items)");
    ListObject result = evalAsList("(collect-all 1 \"two\" 3)");
    assertEquals(3, result.length());
    assertEquals(1, result.car().asInt().value);
    assertEquals("two", result.cdr().car().asString().value());
    assertEquals(3, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testRestRecursive() {
    // Sum using recursion with rest parameters
    lisp.evaluate(
        "(define (sum-recursive first . rest) "
            + "(if (null? rest) first "
            + "(+ first (apply sum-recursive rest))))");
    assertEquals(15, evalAsInt("(sum-recursive 1 2 3 4 5)"));
  }

  @Test
  public void testRestWithHigherOrderReturn() {
    // Function that returns a function using rest params
    lisp.evaluate("(define (make-adder . addends) (lambda (x) (+ x (apply + addends))))");
    lisp.evaluate("(define add-10-20-30 (make-adder 10 20 30))");
    assertEquals(65, evalAsInt("(add-10-20-30 5)"));
  }

  @Test
  public void testRestInClosure() {
    // Rest parameter captured in closure
    lisp.evaluate("(define (make-list-appender . prefix) (lambda (x) (append prefix (list x))))");
    lisp.evaluate("(define appender (make-list-appender 1 2 3))");
    ListObject result = evalAsList("(appender 4)");
    assertEquals(4, result.length());
  }

  @Test
  public void testRestParameterNotMutated() {
    // Verify rest parameter is a fresh list each call
    lisp.evaluate("(define (get-rest . rest) rest)");
    ListObject result1 = evalAsList("(get-rest 1 2 3)");
    ListObject result2 = evalAsList("(get-rest 4 5)");
    assertEquals(3, result1.length());
    assertEquals(2, result2.length());
    assertEquals(1, result1.car().asInt().value);
    assertEquals(4, result2.car().asInt().value);
  }
}
