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
 * Tests for R7RS mode flag functionality.
 *
 * @author Danilo Oliveira
 */
public class R7RSModeTest {

  @Test
  public void testDefaultModeIsNotR7RS() {
    Lisp lisp = new Lisp();
    assertFalse(lisp.isR7rsMode(), "Default mode should not be R7RS");
  }

  @Test
  public void testConstructorWithR7RSModeEnabled() {
    Lisp lisp = new Lisp(true);
    assertTrue(lisp.isR7rsMode(), "R7RS mode should be enabled when constructor parameter is true");
  }

  @Test
  public void testConstructorWithR7RSModeDisabled() {
    Lisp lisp = new Lisp(false);
    assertFalse(
        lisp.isR7rsMode(), "R7RS mode should be disabled when constructor parameter is false");
  }

  @Test
  public void testSetR7RSModeToTrue() {
    Lisp lisp = new Lisp();
    assertFalse(lisp.isR7rsMode(), "Initially should be false");

    lisp.setR7rsMode(true);
    assertTrue(lisp.isR7rsMode(), "R7RS mode should be enabled after setting to true");
  }

  @Test
  public void testSetR7RSModeToFalse() {
    Lisp lisp = new Lisp(true);
    assertTrue(lisp.isR7rsMode(), "Initially should be true");

    lisp.setR7rsMode(false);
    assertFalse(lisp.isR7rsMode(), "R7RS mode should be disabled after setting to false");
  }

  @Test
  public void testR7RSModeDoesNotAffectBasicEvaluation() {
    // Test that basic evaluation works the same in both modes
    Lisp lispDefaultMode = new Lisp(false);
    Lisp lispR7RSMode = new Lisp(true);

    String expression = "(+ 1 2 3)";
    int resultDefault = lispDefaultMode.evaluate(expression).asInt().value;
    int resultR7RS = lispR7RSMode.evaluate(expression).asInt().value;

    assertEquals(6, resultDefault, "Default mode should evaluate correctly");
    assertEquals(6, resultR7RS, "R7RS mode should evaluate correctly");
    assertEquals(resultDefault, resultR7RS, "Both modes should produce the same result");
  }

  @Test
  public void testR7RSModeToggling() {
    Lisp lisp = new Lisp();

    // Start in default mode
    assertFalse(lisp.isR7rsMode());

    // Toggle to R7RS mode
    lisp.setR7rsMode(true);
    assertTrue(lisp.isR7rsMode());

    // Toggle back to default mode
    lisp.setR7rsMode(false);
    assertFalse(lisp.isR7rsMode());

    // Toggle to R7RS mode again
    lisp.setR7rsMode(true);
    assertTrue(lisp.isR7rsMode());
  }

  @Test
  public void testMainWithR7RSFlag() throws Exception {
    // Test that Main class properly parses --r7rs flag
    // This is an integration test to verify the command line parsing works

    Main main = new Main();
    String[] args = {"--r7rs", "-e", "(+ 1 2)"};

    // This should not throw an exception
    assertDoesNotThrow(() -> main.run(args), "Main should handle --r7rs flag correctly");
  }
}
