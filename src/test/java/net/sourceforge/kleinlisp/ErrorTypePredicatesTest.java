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

/** Tests for R7RS error type predicates: read-error? and file-error?. */
class ErrorTypePredicatesTest extends BaseTestClass {

  @Test
  void testReadErrorPredicateReturnsFalseForNonErrors() {
    assertFalse(evalAsBoolean("(read-error? 42)"));
    assertFalse(evalAsBoolean("(read-error? \"hello\")"));
    assertFalse(evalAsBoolean("(read-error? '())"));
    assertFalse(evalAsBoolean("(read-error? #t)"));
  }

  @Test
  void testFileErrorPredicateReturnsFalseForNonErrors() {
    assertFalse(evalAsBoolean("(file-error? 42)"));
    assertFalse(evalAsBoolean("(file-error? \"hello\")"));
    assertFalse(evalAsBoolean("(file-error? '())"));
    assertFalse(evalAsBoolean("(file-error? #t)"));
  }

  @Test
  void testGenericErrorIsNotFileOrReadError() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(error \"generic error\"))";
    assertEquals("(false false)", evalAsString(code));
  }

  @Test
  void testFileErrorFromDeleteFile() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e) (error-object-message"
            + " e)))) (delete-file \"nonexistent-file-12345.txt\"))";
    String result = evalAsString(code);
    assertTrue(result.startsWith("(true false"));
    assertTrue(result.contains("does not exist"));
  }

  @Test
  void testFileErrorFromCallWithInputFile() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(call-with-input-file \"nonexistent-file-12345.txt\" (lambda (p) (read p))))";
    assertEquals("(true false)", evalAsString(code));
  }

  @Test
  void testFileErrorFromCallWithOutputFile() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(call-with-output-file \"/invalid/path/12345.txt\" (lambda (p) (write 42 p))))";
    assertEquals("(true false)", evalAsString(code));
  }

  @Test
  void testFileErrorFromWithInputFromFile() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(with-input-from-file \"nonexistent-file-12345.txt\" (lambda () (read))))";
    assertEquals("(true false)", evalAsString(code));
  }

  @Test
  void testFileErrorFromWithOutputToFile() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(with-output-to-file \"/invalid/path/12345.txt\" (lambda () (write 42))))";
    assertEquals("(true false)", evalAsString(code));
  }

  @Test
  void testReadErrorFromRead() {
    String code =
        "(guard (e ((error-object? e) (list (file-error? e) (read-error? e)))) "
            + "(read (open-input-string \")(\")))";
    assertEquals("(false true)", evalAsString(code));
  }

  @Test
  void testErrorObjectMessageFromFileError() {
    String code =
        "(guard (e ((file-error? e) (error-object-message e))) "
            + "(delete-file \"nonexistent-file-12345.txt\"))";
    String result = evalAsString(code);
    assertTrue(result.contains("does not exist"));
  }

  @Test
  void testMultipleArgsForFileErrorPredicate() {
    assertThrows(
        Exception.class, () -> lisp.evaluate("(file-error? 1 2)"), "Should reject multiple args");
  }

  @Test
  void testMultipleArgsForReadErrorPredicate() {
    assertThrows(
        Exception.class, () -> lisp.evaluate("(read-error? 1 2)"), "Should reject multiple args");
  }

  @Test
  void testFileErrorIsStillAnErrorObject() {
    String code =
        "(guard (e ((file-error? e) (error-object? e))) "
            + "(delete-file \"nonexistent-file-12345.txt\"))";
    assertTrue(evalAsBoolean(code));
  }

  @Test
  void testReadErrorIsStillAnErrorObject() {
    String code =
        "(guard (e ((read-error? e) (error-object? e))) " + "(read (open-input-string \")(\")))";
    assertTrue(evalAsBoolean(code));
  }

  @Test
  void testFileErrorCanBeRaised() {
    String code =
        "(guard (e ((file-error? e) 'caught)) "
            + "(call-with-input-file \"nonexistent-12345.txt\" (lambda (p) (read p))))";
    assertEquals("caught", evalAsString(code));
  }

  @Test
  void testReadErrorCanBeRaised() {
    String code = "(guard (e ((read-error? e) 'caught)) " + "(read (open-input-string \")(\")))";
    assertEquals("caught", evalAsString(code));
  }
}
