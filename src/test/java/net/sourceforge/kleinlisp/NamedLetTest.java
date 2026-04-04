/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 */
package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for named let (let loop ((var init) ...) body...) support.
 *
 * @author danilo
 */
public class NamedLetTest extends BaseTestClass {

  @Test
  public void testSimpleNamedLet() {
    // Simple sum: 0+1+2+3+4+5 = 15
    assertEquals(
        15, evalAsInt("(let loop ((i 0) (sum 0)) (if (> i 5) sum (loop (+ i 1) (+ sum i))))"));
  }

  @Test
  public void testNamedLetCountdown() {
    // Count down from 10 to 0
    assertEquals(0, evalAsInt("(let loop ((n 10)) (if (= n 0) n (loop (- n 1))))"));
  }

  @Test
  public void testNamedLetBuildList() {
    // Build list (5 4 3 2 1)
    assertEquals(
        "(5 4 3 2 1)",
        lisp.evaluate("(let loop ((i 1) (lst '())) (if (> i 5) lst (loop (+ i 1) (cons i lst))))")
            .toString());
  }

  @Test
  public void testNamedLetFactorial() {
    // 5! = 120
    assertEquals(
        120, evalAsInt("(let fact ((n 5) (acc 1)) (if (= n 0) acc (fact (- n 1) (* n acc))))"));
  }

  @Test
  public void testNamedLetWithOuterScope() {
    // Access outer define
    lisp.evaluate("(define limit 5)");
    assertEquals(
        15, evalAsInt("(let loop ((i 0) (sum 0)) (if (> i limit) sum (loop (+ i 1) (+ sum i))))"));
  }

  @Test
  public void testNamedLetRange() {
    // The original example from the issue
    lisp.evaluate(
        "(define (range n) "
            + "  (let loop ((i 2) (result '())) "
            + "    (if (> i n) "
            + "        (reverse result) "
            + "        (loop (+ i 1) (cons i result)))))");
    assertEquals("(2 3 4 5)", lisp.evaluate("(range 5)").toString());
  }
}
