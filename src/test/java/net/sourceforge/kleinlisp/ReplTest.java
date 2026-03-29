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

/** Tests for the REPL class. */
public class ReplTest {

  @Test
  public void testReplCreation() {
    Repl repl = new Repl();
    assertNotNull(repl);
    assertNotNull(repl.getLisp());
  }

  @Test
  public void testReplWithExistingLisp() {
    Lisp lisp = new Lisp();
    lisp.evaluate("(define test-repl-var 123)");
    Repl repl = new Repl(lisp);
    assertEquals(lisp, repl.getLisp());
    // Verify the variable is still accessible
    assertEquals(123, lisp.evaluate("test-repl-var").asInt().value);
  }

  @Test
  public void testReplLoadsGeiserSupport() {
    Repl repl = new Repl();
    Lisp lisp = repl.getLisp();
    // Verify geiser functions are available
    LispObject result = lisp.evaluate("(procedure? geiser:eval)");
    assertTrue(result.truthiness(), "geiser:eval should be defined after REPL initialization");
  }

  @Test
  public void testReplGeiserCompletionsAvailable() {
    Repl repl = new Repl();
    Lisp lisp = repl.getLisp();
    // Test geiser:completions works
    LispObject result = lisp.evaluate("(geiser:completions \"cons\")");
    assertNotNull(result);
    // Should find "cons" in completions
    boolean found = false;
    for (LispObject item : result.asList()) {
      if (item.asString() != null && item.asString().value().equals("cons")) {
        found = true;
        break;
      }
    }
    assertTrue(found, "geiser:completions for 'cons' should find 'cons'");
  }
}
