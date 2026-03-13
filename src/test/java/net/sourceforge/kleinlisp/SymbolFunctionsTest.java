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

/** Tests for symbol manipulation functions. */
public class SymbolFunctionsTest extends BaseTestClass {

  @Test
  public void testSymbolToString() {
    assertEquals("hello", lisp.evaluate("(symbol->string 'hello)").asString().value());
    assertEquals("foo-bar", lisp.evaluate("(symbol->string 'foo-bar)").asString().value());
  }

  @Test
  public void testStringToSymbol() {
    LispObject result = lisp.evaluate("(string->symbol \"hello\")");
    assertTrue(result.asAtom() != null);

    // Should be equal to quoted symbol
    assertTrue(lisp.evaluate("(eq? (string->symbol \"hello\") 'hello)").truthiness());
  }

  @Test
  public void testGensym() {
    // gensym should return a symbol
    LispObject result = lisp.evaluate("(gensym)");
    assertTrue(result.asAtom() != null);

    // Each call should return a unique symbol
    lisp.evaluate("(define s1 (gensym))");
    lisp.evaluate("(define s2 (gensym))");
    assertFalse(lisp.evaluate("(eq? s1 s2)").truthiness());
  }

  @Test
  public void testGensymWithPrefix() {
    LispObject result = lisp.evaluate("(gensym \"temp\")");
    assertTrue(result.asAtom() != null);

    String name = lisp.evaluate("(symbol->string (gensym \"my-prefix\"))").asString().value();
    assertTrue(name.startsWith("my-prefix"));
  }

  @Test
  public void testSymbolRoundtrip() {
    // symbol->string->symbol should work
    assertTrue(lisp.evaluate("(eq? 'foo (string->symbol (symbol->string 'foo)))").truthiness());
  }

  @Test
  public void testSymbolPredicate() {
    assertTrue(lisp.evaluate("(symbol? (gensym))").truthiness());
    assertTrue(lisp.evaluate("(symbol? (string->symbol \"x\"))").truthiness());
  }
}
