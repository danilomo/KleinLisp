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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Danilo Oliveira
 */
public class RuntimeErrorsTest extends BaseTestClass {

  @Test
  public void testAddition() {
    Assert.assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(+ 1 2 'test)");
        });
  }

  @Test
  public void testSyntaticError() {
    Assert.assertThrows(
        SyntaxError.class,
        () -> {
          lisp.parse("1 2 3 (+2 3) (+ 1 2) ³  (+ 1 2)");
        });
  }

  @Test
  public void testMalformedSExp() {
    Assert.assertThrows(
        SyntaxError.class,
        () -> {
          LispObject p = lisp.parse("1 2 3 (+2 3) (+ 1 2)  (+ 1 2) (+ 1");
          debug(p);
        });
  }

  @Test
  public void testRuntimeError() {
    LispRuntimeException ex =
        Assert.assertThrows(
            LispRuntimeException.class,
            () -> {
              String script =
                  "(define (bar a b)\n"
                      + " (+ a b))\n"
                      + "\n"
                      + "(define (foo a b)\n"
                      + "  (println a)\n"
                      + "  (println b)\n"
                      + "  (bar a b))\n"
                      + "\n"
                      + "(foo 1 'dois)\n";

              lisp.evaluate(script);
            });
    debug("Stack trace: " + ex.getLispStackTrace());
    Assert.assertFalse(ex.getLispStackTrace().isEmpty());
  }
}
