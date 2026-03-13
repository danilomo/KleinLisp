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

import net.sourceforge.kleinlisp.objects.KeywordObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Test;

/** Tests for keyword syntax (#:keyword). */
public class KeywordSyntaxTest extends BaseTestClass {

  @Test
  public void testKeywordParsing() {
    LispObject result = lisp.evaluate("#:test");
    assertNotNull(result.asKeyword());
    assertEquals("test", result.asKeyword().name());
  }

  @Test
  public void testKeywordToString() {
    LispObject result = lisp.evaluate("#:my-keyword");
    assertEquals("#:my-keyword", result.toString());
  }

  @Test
  public void testKeywordWithHyphen() {
    LispObject result = lisp.evaluate("#:on-click");
    assertNotNull(result.asKeyword());
    assertEquals("on-click", result.asKeyword().name());
  }

  @Test
  public void testKeywordWithUnderscore() {
    LispObject result = lisp.evaluate("#:my_keyword");
    assertNotNull(result.asKeyword());
    assertEquals("my_keyword", result.asKeyword().name());
  }

  @Test
  public void testKeywordSelfEvaluating() {
    // Keywords should be self-evaluating (evaluate to themselves)
    LispObject result1 = lisp.evaluate("#:foo");
    LispObject result2 = lisp.evaluate("#:foo");
    assertEquals(result1.toString(), result2.toString());
  }

  @Test
  public void testKeywordInList() {
    LispObject result = lisp.evaluate("(list #:name \"test\")");
    ListObject list = result.asList();
    assertNotNull(list);
    assertEquals(2, list.length());
    assertNotNull(list.car().asKeyword());
    assertEquals("name", list.car().asKeyword().name());
  }

  @Test
  public void testKeywordMultipleInList() {
    LispObject result = lisp.evaluate("(list #:a 1 #:b 2 #:c 3)");
    ListObject list = result.asList();
    assertEquals(6, list.length());
  }

  @Test
  public void testKeywordEquality() {
    // Test that keywords with same name are equal
    KeywordObject k1 = lisp.evaluate("#:test").asKeyword();
    KeywordObject k2 = lisp.evaluate("#:test").asKeyword();
    assertEquals(k1, k2);
  }

  @Test
  public void testKeywordInequality() {
    // Test that keywords with different names are not equal
    KeywordObject k1 = lisp.evaluate("#:foo").asKeyword();
    KeywordObject k2 = lisp.evaluate("#:bar").asKeyword();
    assertNotEquals(k1, k2);
  }

  @Test
  public void testKeywordTruthiness() {
    // Keywords should be truthy
    assertTrue(lisp.evaluate("#:anything").truthiness());
  }

  @Test
  public void testKeywordInQuotedList() {
    LispObject result = lisp.evaluate("'(#:key value)");
    ListObject list = result.asList();
    assertNotNull(list.car().asKeyword());
  }

  @Test
  public void testKeywordAsMapKey() {
    // Test using keywords in association list pattern
    lisp.evaluate("(define props '((#:name . \"test\") (#:value . 42)))");
    // Just verify it doesn't throw
    LispObject result = lisp.evaluate("props");
    assertNotNull(result.asList());
  }

  @Test
  public void testKeywordWithNumbers() {
    LispObject result = lisp.evaluate("#:item1");
    assertNotNull(result.asKeyword());
    assertEquals("item1", result.asKeyword().name());
  }

  @Test
  public void testKeywordInFunctionCall() {
    // Keywords can be passed as arguments
    lisp.evaluate("(define (identity x) x)");
    LispObject result = lisp.evaluate("(identity #:test)");
    assertNotNull(result.asKeyword());
    assertEquals("test", result.asKeyword().name());
  }

  @Test
  public void testKeywordInConditional() {
    // Keywords are truthy, so (if #:key ...) should take the true branch
    assertEquals(1, evalAsInt("(if #:key 1 2)"));
  }
}
