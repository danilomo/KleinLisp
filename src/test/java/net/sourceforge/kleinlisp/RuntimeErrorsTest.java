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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Danilo Oliveira
 */
public class RuntimeErrorsTest extends BaseTestClass {

  @Test
  public void testAddition() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(+ 1 2 'test)");
        });
  }

  @Test
  public void testSyntaticError() {
    assertThrows(
        SyntaxError.class,
        () -> {
          lisp.parse("1 2 3 (+2 3) (+ 1 2) ³  (+ 1 2)");
        });
  }

  @Test
  public void testMalformedSExp() {
    String[] exps = {"1 2 3 (+2 3) (+ 1 2)  (+ 1 2) (+ 1", "(+ 2 (())))", "((1 2 3)", "(1 2 3 ) )"};
    for (String exp : exps) {
      assertThrows(
          SyntaxError.class,
          () -> {
            LispObject p = lisp.parse(exp);
            debug(p);
          });
    }
  }

  @Test
  public void testRuntimeError(@TempDir Path tempDir) {
    LispRuntimeException ex =
        assertThrows(
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
    assertFalse(ex.getLispStackTrace().isEmpty());
  }

  @Test
  public void testRuntimeErrorFromScript(@TempDir Path tempDir) throws Exception {
    disableStdoutCapture();
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
    Path scriptFile = tempDir.resolve("script.scm");
    Files.write(scriptFile, script.getBytes());

    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              lisp.execute(scriptFile);
            });

    lisp.environment().printStackTrace();

    assertFalse(ex.getLispStackTrace().isEmpty());
  }
}
