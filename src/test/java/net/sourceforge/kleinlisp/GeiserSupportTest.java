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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for Geiser support functions loaded from geiser/kleinlisp.scm. */
public class GeiserSupportTest extends BaseTestClass {

  @BeforeEach
  public void loadGeiserSupport() throws Exception {
    // Load the geiser support file
    InputStream geiserInit = getClass().getResourceAsStream("/geiser/kleinlisp.scm");
    if (geiserInit != null) {
      String initCode = new String(geiserInit.readAllBytes(), StandardCharsets.UTF_8);
      lisp.evaluate(initCode);
      geiserInit.close();
    }
  }

  @Test
  public void testGeiserEvalSimple() {
    // geiser:eval returns Geiser protocol format: ((result "3") (output . ""))
    LispObject result = lisp.evaluate("(geiser:eval '(+ 1 2))");
    ListObject list = result.asList();
    assertNotNull(list, "geiser:eval should return a list");
    // Extract (result "3") - first element
    ListObject resultPair = list.car().asList();
    assertEquals("result", resultPair.car().asAtom().value());
    assertEquals("3", resultPair.cdr().car().asString().value());
  }

  @Test
  public void testGeiserEvalWithDefine() {
    // geiser:eval returns protocol format, but still defines the variable
    lisp.evaluate("(geiser:eval '(define geiser-test-x 42))");
    assertEquals(42, evalAsInt("geiser-test-x"));
  }

  @Test
  public void testGeiserCompletionsReturnsListOfStrings() {
    LispObject result = lisp.evaluate("(geiser:completions \"str\")");
    assertNotNull(result);
    // Could be empty list or list of strings
    if (result != ListObject.NIL) {
      ListObject list = result.asList();
      assertNotNull(list, "geiser:completions should return a list");
    }
  }

  @Test
  public void testGeiserCompletionsFindsStringFunctions() {
    LispObject result = lisp.evaluate("(geiser:completions \"string-app\")");
    ListObject list = result.asList();
    boolean found = false;
    for (LispObject item : list) {
      StringObject str = item.asString();
      if (str != null && str.value().equals("string-append")) {
        found = true;
        break;
      }
    }
    assertTrue(found, "geiser:completions for 'string-app' should find 'string-append'");
  }

  @Test
  public void testGeiserCompletionsEmptyPrefix() {
    LispObject result = lisp.evaluate("(geiser:completions \"\")");
    ListObject list = result.asList();
    assertNotNull(list, "geiser:completions with empty prefix should return a list");
    assertTrue(list.length() > 0, "geiser:completions with empty prefix should return all symbols");
  }

  @Test
  public void testGeiserModuleCompletionsReturnsEmptyList() {
    LispObject result = lisp.evaluate("(geiser:module-completions \"anything\")");
    assertEquals(ListObject.NIL, result, "geiser:module-completions should return empty list");
  }

  @Test
  public void testGeiserAutodocReturnsGeiserFormat() {
    // geiser:autodoc returns Geiser protocol format with empty result
    LispObject result = lisp.evaluate("(geiser:autodoc '(+))");
    ListObject list = result.asList();
    assertNotNull(list, "geiser:autodoc should return a list in Geiser protocol format");
    // First element should be (result "()")
    ListObject resultPair = list.car().asList();
    assertEquals("result", resultPair.car().asAtom().value());
  }

  @Test
  public void testGeiserNoValuesReturnsGeiserFormat() {
    // geiser:no-values returns Geiser protocol format with empty result
    LispObject result = lisp.evaluate("(geiser:no-values)");
    ListObject list = result.asList();
    assertNotNull(list, "geiser:no-values should return a list in Geiser protocol format");
    // First element should be (result "()")
    ListObject resultPair = list.car().asList();
    assertEquals("result", resultPair.car().asAtom().value());
  }

  @Test
  public void testGeiserModuleExportsReturnsEmptyList() {
    LispObject result = lisp.evaluate("(geiser:module-exports 'anything)");
    assertEquals(ListObject.NIL, result, "geiser:module-exports should return empty list");
  }

  @Test
  public void testGeiserModuleLocationReturnsEmptyList() {
    LispObject result = lisp.evaluate("(geiser:module-location 'anything)");
    assertEquals(ListObject.NIL, result, "geiser:module-location should return empty list");
  }

  @Test
  public void testGeiserNewline() {
    // geiser:newline should not throw and return void
    LispObject result = lisp.evaluate("(geiser:newline)");
    // Just verify it doesn't throw - void result is acceptable
    assertNotNull(result);
  }
}
