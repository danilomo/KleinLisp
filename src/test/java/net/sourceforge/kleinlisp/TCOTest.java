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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Danilo Oliveira
 */
public class TCOTest {

  private String readFile(String filename) throws Exception {
    InputStream is = TCOTest.class.getClassLoader().getResourceAsStream(filename);

    String result =
        new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

    return result;
  }

  @Test
  public void evaluateTailRecursiveFunction() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    String script = readFile("fib.scm");

    lisp.evaluate(script);
    Assertions.assertEquals(55, lisp.evaluate("(iter 0 1 10)").asInt().value);
  }

  @Test
  public void evaluateLetrecTailRecursiveFunction() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    // Define a tail-recursive sum function using letrec
    // With only 5 stack frames, this would overflow without TCO
    String script =
        "(letrec ((sum (lambda (n acc)"
            + "               (if (= n 0)"
            + "                   acc"
            + "                   (sum (- n 1) (+ n acc))))))"
            + "  (sum 1000 0))";

    LispObject result = lisp.evaluate(script);
    Assertions.assertEquals(500500, result.asInt().value);
  }

  @Test
  public void evaluateLetrecStarTailRecursiveFunction() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    // Define a tail-recursive countdown function using letrec*
    String script =
        "(letrec* ((countdown (lambda (n)"
            + "                    (if (= n 0)"
            + "                        0"
            + "                        (countdown (- n 1))))))"
            + "  (countdown 1000))";

    LispObject result = lisp.evaluate(script);
    Assertions.assertEquals(0, result.asInt().value);
  }

  @Test
  public void evaluateNamedLetTailRecursiveFunction() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    // Named let should also support TCO
    String script =
        "(let loop ((n 1000) (acc 0))"
            + "  (if (= n 0)"
            + "      acc"
            + "      (loop (- n 1) (+ n acc))))";

    LispObject result = lisp.evaluate(script);
    Assertions.assertEquals(500500, result.asInt().value);
  }

  @Test
  public void evaluateLetrecTailCallThroughIf() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    // Tail calls through both branches of if
    String script =
        "(letrec ((even-count (lambda (n)"
            + "                       (if (= n 0)"
            + "                           #t"
            + "                           (odd-count (- n 1)))))"
            + "         (odd-count (lambda (n)"
            + "                      (if (= n 0)"
            + "                          #f"
            + "                          (even-count (- n 1))))))"
            + "  (even-count 1000))";

    LispObject result = lisp.evaluate(script);
    Assertions.assertTrue(result.truthiness());
  }

  @Test
  public void evaluateLetrecTailCallThroughLet() throws Exception {
    Lisp lisp = new Lisp();
    lisp.environment().setStackSize(5);

    // Tail call through a let form
    String script =
        "(letrec ((sum (lambda (n acc)"
            + "               (if (= n 0)"
            + "                   acc"
            + "                   (let ((next (- n 1))"
            + "                         (newacc (+ n acc)))"
            + "                     (sum next newacc))))))"
            + "  (sum 1000 0))";

    LispObject result = lisp.evaluate(script);
    Assertions.assertEquals(500500, result.asInt().value);
  }
}
