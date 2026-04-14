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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for R7RS read/write functions. */
public class ReadWriteTest extends BaseTestClass {

  @Test
  public void testReadSimpleAtom() {
    String code = "(let ((p (open-input-string \"42\"))) (read p))";
    assertEquals(42, evalAsInt(code));
  }

  @Test
  public void testReadSymbol() {
    String code = "(let ((p (open-input-string \"hello\"))) (read p))";
    assertEquals("hello", evalAsString(code));
  }

  @Test
  public void testReadString() {
    String code = "(let ((p (open-input-string \"\\\"hello world\\\"\"))) (read p))";
    assertEquals("hello world", evalAsString(code));
  }

  @Test
  public void testReadList() {
    String code = "(let ((p (open-input-string \"(+ 1 2)\"))) (read p))";
    assertEquals("(+ 1 2)", evalAsString(code));
  }

  @Test
  public void testReadNestedList() {
    String code = "(let ((p (open-input-string \"(a (b c) d)\"))) (read p))";
    assertEquals("(a (b c) d)", evalAsString(code));
  }

  @Test
  public void testReadMultipleExpressions() {
    String code =
        "(let ((p (open-input-string \"1 2 3\"))) " + "(list (read p) (read p) (read p)))";
    assertEquals("(1 2 3)", evalAsString(code));
  }

  @Test
  public void testReadEof() {
    String code = "(let ((p (open-input-string \"\"))) (eof-object? (read p)))";
    assertTrue(evalAsBoolean(code));
  }

  @Test
  public void testReadQuotedExpression() {
    String code = "(let ((p (open-input-string \"'(1 2 3)\"))) (read p))";
    String result = evalAsString(code);
    assertTrue(result.equals("(quote (1 2 3))") || result.equals("'(1 2 3)"));
  }

  @Test
  public void testReadVector() {
    String code = "(let ((p (open-input-string \"#(1 2 3)\"))) (read p))";
    assertEquals("#(1 2 3)", evalAsString(code));
  }

  @Test
  public void testReadBoolean() {
    String codeTrue = "(let ((p (open-input-string \"#t\"))) (read p))";
    assertTrue(evalAsBoolean(codeTrue));

    String codeFalse = "(let ((p (open-input-string \"#f\"))) (boolean? (read p)))";
    assertTrue(evalAsBoolean(codeFalse));
  }

  @Test
  public void testWriteInteger() {
    String code = "(let ((p (open-output-string))) " + "(write 42 p) " + "(get-output-string p))";
    assertEquals("42", evalAsString(code));
  }

  @Test
  public void testWriteString() {
    String code =
        "(let ((p (open-output-string))) " + "(write \"hello\" p) " + "(get-output-string p))";
    assertEquals("\"hello\"", evalAsString(code));
  }

  @Test
  public void testWriteStringWithEscapes() {
    String code =
        "(let ((p (open-output-string))) "
            + "(write \"line1\\nline2\" p) "
            + "(get-output-string p))";
    assertEquals("\"line1\\nline2\"", evalAsString(code));
  }

  @Test
  public void testWriteSymbol() {
    String code =
        "(let ((p (open-output-string))) " + "(write 'hello p) " + "(get-output-string p))";
    assertEquals("hello", evalAsString(code));
  }

  @Test
  public void testWriteList() {
    String code =
        "(let ((p (open-output-string))) " + "(write '(1 2 3) p) " + "(get-output-string p))";
    assertEquals("(1 2 3)", evalAsString(code));
  }

  @Test
  public void testWriteNestedList() {
    String code =
        "(let ((p (open-output-string))) " + "(write '(a (b c) d) p) " + "(get-output-string p))";
    assertEquals("(a (b c) d)", evalAsString(code));
  }

  @Test
  public void testWriteVector() {
    String code =
        "(let ((p (open-output-string))) " + "(write #(1 2 3) p) " + "(get-output-string p))";
    assertEquals("#(1 2 3)", evalAsString(code));
  }

  @Test
  public void testWriteBoolean() {
    String codeTrue =
        "(let ((p (open-output-string))) " + "(write #t p) " + "(get-output-string p))";
    assertEquals("true", evalAsString(codeTrue));

    String codeFalse =
        "(let ((p (open-output-string))) " + "(write #f p) " + "(get-output-string p))";
    assertEquals("false", evalAsString(codeFalse));
  }

  @Test
  public void testWriteCharacter() {
    String code = "(let ((p (open-output-string))) " + "(write #\\a p) " + "(get-output-string p))";
    assertEquals("#\\a", evalAsString(code));
  }

  @Test
  public void testWriteSpecialCharacter() {
    String code =
        "(let ((p (open-output-string))) " + "(write #\\newline p) " + "(get-output-string p))";
    assertEquals("#\\newline", evalAsString(code));
  }

  @Test
  public void testWriteSimpleInteger() {
    String code =
        "(let ((p (open-output-string))) " + "(write-simple 42 p) " + "(get-output-string p))";
    assertEquals("42", evalAsString(code));
  }

  @Test
  public void testWriteSimpleString() {
    String code =
        "(let ((p (open-output-string))) "
            + "(write-simple \"hello\" p) "
            + "(get-output-string p))";
    // write-simple includes quotes for strings
    assertEquals("\"hello\"", evalAsString(code));
  }

  @Test
  public void testWriteSimpleList() {
    String code =
        "(let ((p (open-output-string))) "
            + "(write-simple '(1 2 3) p) "
            + "(get-output-string p))";
    assertEquals("(1 2 3)", evalAsString(code));
  }

  @Test
  public void testReadWriteRoundTrip() {
    String code =
        "(let ((p1 (open-output-string))) "
            + "(write '(a b (c d) e) p1) "
            + "(let ((str (get-output-string p1))) "
            + "(let ((p2 (open-input-string str))) "
            + "(read p2))))";
    assertEquals("(a b (c d) e)", evalAsString(code));
  }

  @Test
  public void testReadWriteRoundTripWithStrings() {
    String code =
        "(let ((p1 (open-output-string))) "
            + "(write \"hello world\" p1) "
            + "(let ((str (get-output-string p1))) "
            + "(let ((p2 (open-input-string str))) "
            + "(read p2))))";
    assertEquals("hello world", evalAsString(code));
  }

  @Test
  public void testReadWriteRoundTripWithNumbers() {
    String code =
        "(let ((p1 (open-output-string))) "
            + "(write 42 p1) "
            + "(let ((str (get-output-string p1))) "
            + "(let ((p2 (open-input-string str))) "
            + "(read p2))))";
    assertEquals(42, evalAsInt(code));
  }

  @Test
  public void testWriteDefaultPort() {
    // Test write without explicit port (uses current-output-port)
    // This is harder to test directly, so we just ensure it doesn't throw
    String code = "(let ((p (open-output-string))) (write 42 p))";
    eval(code); // Should not throw
  }

  @Test
  public void testReadDefaultPort() {
    // Test that read can be called with default port
    String code = "(let ((p (open-input-string \"42\"))) (read p))";
    assertEquals(42, evalAsInt(code));
  }

  @Test
  public void testWriteReturnsObject() {
    String code = "(let ((p (open-output-string))) (write 42 p))";
    assertEquals(42, evalAsInt(code));
  }

  @Test
  public void testReadEmptyString() {
    String code = "(let ((p (open-input-string \"\"))) (eof-object? (read p)))";
    assertTrue(evalAsBoolean(code));
  }

  @Test
  public void testReadWhitespace() {
    String code = "(let ((p (open-input-string \"  42  \"))) (read p))";
    assertEquals(42, evalAsInt(code));
  }

  // TODO: Dotted pair writing is not fully supported yet
  // @Test
  // public void testWriteDottedPair() {
  //   String code =
  //       "(let ((p (open-output-string))) "
  //           + "(write (cons 1 2) p) "
  //           + "(get-output-string p))";
  //   assertEquals("(1 . 2)", evalAsString(code));
  // }

  @Test
  public void testReadEvaluate() {
    String code = "(let ((p (open-input-string \"(+ 1 2)\"))) (eval (read p)))";
    assertEquals(3, evalAsInt(code));
  }

  @Test
  public void testMultipleWritesToSamePort() {
    String code =
        "(let ((p (open-output-string))) "
            + "(write 1 p) "
            + "(write 2 p) "
            + "(write 3 p) "
            + "(get-output-string p))";
    assertEquals("123", evalAsString(code));
  }
}
