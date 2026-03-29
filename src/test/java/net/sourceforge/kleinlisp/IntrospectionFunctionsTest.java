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
import net.sourceforge.kleinlisp.objects.StringObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for introspection functions: environment-symbols, procedure-name, procedure-arity, load,
 * eval.
 */
public class IntrospectionFunctionsTest extends BaseTestClass {

  @Test
  public void testEnvironmentSymbolsReturnsNonEmptyList() {
    LispObject result = lisp.evaluate("(environment-symbols)");
    assertNotNull(result);
    ListObject list = result.asList();
    assertNotNull(list, "environment-symbols should return a list");
    assertTrue(list.length() > 0, "environment-symbols should return a non-empty list");
  }

  @Test
  public void testEnvironmentSymbolsContainsBuiltins() {
    LispObject result = lisp.evaluate("(environment-symbols)");
    ListObject list = result.asList();
    boolean foundPlus = false;
    boolean foundDefine = false;
    for (LispObject item : list) {
      StringObject str = item.asString();
      if (str != null) {
        String value = str.value();
        if (value.equals("+")) foundPlus = true;
        if (value.equals("map")) foundDefine = true;
      }
    }
    assertTrue(foundPlus, "environment-symbols should contain '+'");
    assertTrue(foundDefine, "environment-symbols should contain 'map'");
  }

  @Test
  public void testEnvironmentSymbolsContainsUserDefinitions() {
    lisp.evaluate("(define my-test-var 42)");
    LispObject result = lisp.evaluate("(environment-symbols)");
    ListObject list = result.asList();
    boolean found = false;
    for (LispObject item : list) {
      StringObject str = item.asString();
      if (str != null && str.value().equals("my-test-var")) {
        found = true;
        break;
      }
    }
    assertTrue(found, "environment-symbols should contain user-defined 'my-test-var'");
  }

  @Test
  public void testProcedureNameForBuiltin() {
    LispObject result = lisp.evaluate("(procedure-name +)");
    StringObject str = result.asString();
    assertNotNull(str, "procedure-name of + should return a string");
    assertEquals("+", str.value());
  }

  @Test
  public void testProcedureNameForUserDefined() {
    lisp.evaluate("(define (my-func x) x)");
    LispObject result = lisp.evaluate("(procedure-name my-func)");
    StringObject str = result.asString();
    assertNotNull(str, "procedure-name of my-func should return a string");
    assertEquals("my-func", str.value());
  }

  @Test
  public void testProcedureArityReturnsInteger() {
    LispObject result = lisp.evaluate("(procedure-arity +)");
    assertNotNull(result.asInt(), "procedure-arity should return an integer");
  }

  @Test
  public void testEvalSimpleExpression() {
    LispObject result = lisp.evaluate("(eval '(+ 1 2 3))");
    assertEquals(6, result.asInt().value);
  }

  @Test
  public void testEvalQuotedDefine() {
    lisp.evaluate("(eval '(define eval-test-var 100))");
    assertEquals(100, evalAsInt("eval-test-var"));
  }

  @Test
  public void testEvalWithSymbol() {
    lisp.evaluate("(define x 5)");
    lisp.evaluate("(define expr '(+ x 10))");
    LispObject result = lisp.evaluate("(eval expr)");
    assertEquals(15, result.asInt().value);
  }
}
