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

import net.sourceforge.kleinlisp.objects.MutableStringObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for R7RS mutable string operations (Session 12).
 *
 * <p>Tests string-set!, string-copy!, and string-fill! functions as well as make-string returning
 * MutableStringObject.
 */
public class MutableStringTest extends BaseTestClass {

  @Test
  public void testMakeStringReturnsMutableString() {
    LispObject result = lisp.evaluate("(make-string 5 #\\a)");
    assertTrue(
        result instanceof MutableStringObject, "make-string should return MutableStringObject");
    assertEquals("aaaaa", ((MutableStringObject) result).value());
  }

  @Test
  public void testMakeStringDefaultFill() {
    LispObject result = lisp.evaluate("(make-string 3)");
    assertTrue(result instanceof MutableStringObject);
    MutableStringObject str = (MutableStringObject) result;
    assertEquals(3, str.length());
    // Default fill is null character
    assertEquals('\0', str.charAt(0));
  }

  @Test
  public void testStringSetBasic() {
    String code = "(let ((s (make-string 3 #\\a))) (string-set! s 1 #\\b) s)";
    LispObject result = lisp.evaluate(code);
    assertTrue(result instanceof MutableStringObject);
    assertEquals("aba", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringSetMultipleChanges() {
    String code =
        "(let ((s (make-string 5 #\\x)))"
            + "  (string-set! s 0 #\\h)"
            + "  (string-set! s 1 #\\e)"
            + "  (string-set! s 2 #\\l)"
            + "  (string-set! s 3 #\\l)"
            + "  (string-set! s 4 #\\o)"
            + "  s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("hello", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringSetOutOfBounds() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(let ((s (make-string 3 #\\a))) (string-set! s 5 #\\b))");
        });
  }

  @Test
  public void testStringSetNegativeIndex() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(let ((s (make-string 3 #\\a))) (string-set! s -1 #\\b))");
        });
  }

  @Test
  public void testStringSetOnImmutableStringFails() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(string-set! \"hello\" 0 #\\H)");
        });
  }

  @Test
  public void testStringFillBasic() {
    String code = "(let ((s (make-string 5 #\\a))) (string-fill! s #\\z) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("zzzzz", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringFillWithRange() {
    String code = "(let ((s (make-string 5 #\\a))) (string-fill! s #\\b 1 4) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("abbba", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringFillWithStartOnly() {
    String code = "(let ((s (make-string 5 #\\a))) (string-fill! s #\\c 2) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("aaccc", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringFillEmptyRange() {
    String code = "(let ((s (make-string 5 #\\a))) (string-fill! s #\\b 2 2) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("aaaaa", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringFillOnImmutableStringFails() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(string-fill! \"hello\" #\\x)");
        });
  }

  @Test
  public void testStringCopyBasic() {
    String code = "(let ((to (make-string 5 #\\-)))" + "  (string-copy! to 0 \"abc\")" + "  to)";
    LispObject result = lisp.evaluate(code);
    assertEquals("abc--", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyWithRange() {
    String code =
        "(let ((to (make-string 5 #\\-)))" + "  (string-copy! to 1 \"hello\" 1 4)" + "  to)";
    LispObject result = lisp.evaluate(code);
    assertEquals("-ell-", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyFromMutableString() {
    String code =
        "(let ((from (make-string 5 #\\a))"
            + "      (to (make-string 5 #\\b)))"
            + "  (string-fill! from #\\x 0 3)"
            + "  (string-copy! to 1 from 0 3)"
            + "  to)";
    LispObject result = lisp.evaluate(code);
    assertEquals("bxxxb", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyFullString() {
    String code = "(let ((to (make-string 10 #\\-)))" + "  (string-copy! to 2 \"hello\")" + "  to)";
    LispObject result = lisp.evaluate(code);
    assertEquals("--hello---", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyOverwrite() {
    String code =
        "(let ((s (make-string 5 #\\a)))"
            + "  (string-copy! s 0 \"xyz\")"
            + "  (string-copy! s 2 \"12\")"
            + "  s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("xy12a", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyOnImmutableStringFails() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(string-copy! \"hello\" 0 \"world\")");
        });
  }

  @Test
  public void testStringCopyNotEnoughSpace() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(let ((to (make-string 3 #\\-))) (string-copy! to 0 \"hello\"))");
        });
  }

  @Test
  public void testR7RSExample() {
    // From the R7RS spec example
    String code = "(let ((s (make-string 3 #\\a))) (string-set! s 1 #\\b) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("aba", ((MutableStringObject) result).value());
  }

  @Test
  public void testMutableStringEquality() {
    String code =
        "(let ((s1 (make-string 3 #\\a))"
            + "      (s2 (make-string 3 #\\a)))"
            + "  (string=? s1 s2))";
    assertTrue(lisp.evaluate(code).truthiness());
  }

  @Test
  public void testMutableStringToString() {
    LispObject result = lisp.evaluate("(make-string 3 #\\z)");
    assertEquals("\"zzz\"", result.toString());
  }

  @Test
  public void testMutableStringLength() {
    String code = "(string-length (make-string 7 #\\a))";
    assertEquals(7, lisp.evaluate(code).asInt().value);
  }

  @Test
  public void testMutableStringRef() {
    String code = "(string-ref (make-string 5 #\\x) 2)";
    assertEquals("#\\x", lisp.evaluate(code).toString());
  }

  @Test
  public void testMutableStringWithOtherFunctions() {
    String code =
        "(let ((s (make-string 5 #\\a)))"
            + "  (string-set! s 0 #\\h)"
            + "  (string-set! s 4 #\\o)"
            + "  (string-append s \"!\"))";
    assertEquals("haaao!", lisp.evaluate(code).asString().value());
  }

  @Test
  public void testComplexMutationSequence() {
    String code =
        "(let ((s (make-string 10 #\\-)))"
            + "  (string-fill! s #\\a 0 5)"
            + "  (string-fill! s #\\b 5 10)"
            + "  (string-set! s 4 #\\X)"
            + "  (string-set! s 5 #\\Y)"
            + "  (string-copy! s 2 \"12\" 0 2)"
            + "  s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("aa12XYbbbb", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringCopyAtEnd() {
    String code = "(let ((to (make-string 7 #\\-)))" + "  (string-copy! to 5 \"xy\")" + "  to)";
    LispObject result = lisp.evaluate(code);
    assertEquals("-----xy", ((MutableStringObject) result).value());
  }

  @Test
  public void testStringFillEntireString() {
    String code = "(let ((s (make-string 4 #\\a))) (string-fill! s #\\z 0 4) s)";
    LispObject result = lisp.evaluate(code);
    assertEquals("zzzz", ((MutableStringObject) result).value());
  }

  @Test
  public void testZeroLengthMutableString() {
    LispObject result = lisp.evaluate("(make-string 0)");
    assertTrue(result instanceof MutableStringObject);
    assertEquals(0, ((MutableStringObject) result).length());
    assertEquals("", ((MutableStringObject) result).value());
  }
}
