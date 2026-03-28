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

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for R7RS port operations. */
public class PortTest extends BaseTestClass {

  // Port predicates

  @Test
  public void testPortPredicates() {
    lisp.evaluate("(define sp (open-output-string))");
    assertEquals("true", lisp.evaluate("(port? sp)").toString());
    assertEquals("true", lisp.evaluate("(output-port? sp)").toString());
    assertEquals("false", lisp.evaluate("(input-port? sp)").toString());
    assertEquals("true", lisp.evaluate("(textual-port? sp)").toString());
    assertEquals("false", lisp.evaluate("(binary-port? sp)").toString());
  }

  @Test
  public void testInputPortPredicate() {
    lisp.evaluate("(define ip (open-input-string \"hello\"))");
    assertEquals("true", lisp.evaluate("(port? ip)").toString());
    assertEquals("true", lisp.evaluate("(input-port? ip)").toString());
    assertEquals("false", lisp.evaluate("(output-port? ip)").toString());
  }

  @Test
  public void testPortPredicatesOnNonPorts() {
    assertEquals("false", lisp.evaluate("(port? 42)").toString());
    assertEquals("false", lisp.evaluate("(port? \"hello\")").toString());
    assertEquals("false", lisp.evaluate("(input-port? 42)").toString());
    assertEquals("false", lisp.evaluate("(output-port? \"hello\")").toString());
  }

  // String ports

  @Test
  public void testOpenOutputString() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"hello\" sp)");
    assertEquals("\"hello\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  @Test
  public void testOpenInputString() {
    lisp.evaluate("(define sp (open-input-string \"hello\"))");
    assertEquals("#\\h", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\e", lisp.evaluate("(read-char sp)").toString());
  }

  @Test
  public void testReadLine() {
    lisp.evaluate("(define sp (open-input-string \"line1\\nline2\"))");
    assertEquals("\"line1\"", lisp.evaluate("(read-line sp)").toString());
    assertEquals("\"line2\"", lisp.evaluate("(read-line sp)").toString());
  }

  @Test
  public void testReadLineMultiple() {
    lisp.evaluate("(define sp (open-input-string \"a\\nb\\nc\"))");
    assertEquals("\"a\"", lisp.evaluate("(read-line sp)").toString());
    assertEquals("\"b\"", lisp.evaluate("(read-line sp)").toString());
    assertEquals("\"c\"", lisp.evaluate("(read-line sp)").toString());
    assertEquals("true", lisp.evaluate("(eof-object? (read-line sp))").toString());
  }

  @Test
  public void testEof() {
    lisp.evaluate("(define sp (open-input-string \"\"))");
    assertEquals("true", lisp.evaluate("(eof-object? (read-char sp))").toString());
  }

  @Test
  public void testEofObject() {
    assertEquals("true", lisp.evaluate("(eof-object? (eof-object))").toString());
    assertEquals("false", lisp.evaluate("(eof-object? #f)").toString());
    assertEquals("false", lisp.evaluate("(eof-object? 42)").toString());
    assertEquals("false", lisp.evaluate("(eof-object? \"hello\")").toString());
  }

  @Test
  public void testWriteChar() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-char #\\H sp)");
    lisp.evaluate("(write-char #\\i sp)");
    assertEquals("\"Hi\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  @Test
  public void testPeekChar() {
    lisp.evaluate("(define sp (open-input-string \"ab\"))");
    assertEquals("#\\a", lisp.evaluate("(peek-char sp)").toString());
    assertEquals("#\\a", lisp.evaluate("(peek-char sp)").toString()); // Still 'a'
    assertEquals("#\\a", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\b", lisp.evaluate("(peek-char sp)").toString());
  }

  @Test
  public void testClosePort() {
    lisp.evaluate("(define sp (open-output-string))");
    assertEquals("true", lisp.evaluate("(port-open? sp)").toString());
    lisp.evaluate("(close-port sp)");
    assertEquals("false", lisp.evaluate("(port-open? sp)").toString());
  }

  @Test
  public void testCloseInputPort() {
    lisp.evaluate("(define sp (open-input-string \"hello\"))");
    assertEquals("true", lisp.evaluate("(input-port-open? sp)").toString());
    lisp.evaluate("(close-input-port sp)");
    assertEquals("false", lisp.evaluate("(input-port-open? sp)").toString());
  }

  @Test
  public void testCloseOutputPort() {
    lisp.evaluate("(define sp (open-output-string))");
    assertEquals("true", lisp.evaluate("(output-port-open? sp)").toString());
    lisp.evaluate("(close-output-port sp)");
    assertEquals("false", lisp.evaluate("(output-port-open? sp)").toString());
  }

  @Test
  public void testCurrentPorts() {
    assertEquals("true", lisp.evaluate("(port? (current-input-port))").toString());
    assertEquals("true", lisp.evaluate("(port? (current-output-port))").toString());
    assertEquals("true", lisp.evaluate("(port? (current-error-port))").toString());
    assertEquals("true", lisp.evaluate("(input-port? (current-input-port))").toString());
    assertEquals("true", lisp.evaluate("(output-port? (current-output-port))").toString());
    assertEquals("true", lisp.evaluate("(output-port? (current-error-port))").toString());
  }

  @Test
  public void testWriteStringPartial() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"hello world\" sp 0 5)");
    assertEquals("\"hello\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  @Test
  public void testWriteStringPartialMiddle() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"hello world\" sp 6 11)");
    assertEquals("\"world\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  @Test
  public void testWriteStringConcatenate() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"Hello, \" sp)");
    lisp.evaluate("(write-string \"World!\" sp)");
    assertEquals("\"Hello, World!\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  @Test
  public void testReadCharSequence() {
    lisp.evaluate("(define sp (open-input-string \"abc\"))");
    assertEquals("#\\a", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\b", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\c", lisp.evaluate("(read-char sp)").toString());
    assertEquals("true", lisp.evaluate("(eof-object? (read-char sp))").toString());
  }

  @Test
  public void testReadCharSpecialChars() {
    lisp.evaluate("(define sp (open-input-string \" \\t\\n\"))");
    assertEquals("#\\space", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\tab", lisp.evaluate("(read-char sp)").toString());
    assertEquals("#\\newline", lisp.evaluate("(read-char sp)").toString());
  }

  @Test
  public void testWriteCharSpecialChars() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-char #\\space sp)");
    lisp.evaluate("(write-char #\\newline sp)");
    String result = lisp.evaluate("(get-output-string sp)").toString();
    assertTrue(result.contains(" "));
    assertTrue(result.contains("\n") || result.contains("\\n"));
  }

  @Test
  public void testCharReady() {
    lisp.evaluate("(define sp (open-input-string \"abc\"))");
    assertEquals("true", lisp.evaluate("(char-ready? sp)").toString());
  }

  @Test
  public void testFlushOutputPort() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"test\" sp)");
    lisp.evaluate("(flush-output-port sp)");
    assertEquals("\"test\"", lisp.evaluate("(get-output-string sp)").toString());
  }

  // File ports (with temp directory)

  @Test
  public void testFileIO(@TempDir Path tempDir) {
    File testFile = tempDir.resolve("test.txt").toFile();
    String path = testFile.getAbsolutePath().replace("\\", "\\\\");

    // Write to file
    lisp.evaluate("(define out (open-output-file \"" + path + "\"))");
    lisp.evaluate("(write-string \"hello\" out)");
    lisp.evaluate("(close-port out)");

    // Read from file
    lisp.evaluate("(define in (open-input-file \"" + path + "\"))");
    assertEquals("\"hello\"", lisp.evaluate("(read-line in)").toString());
    lisp.evaluate("(close-port in)");
  }

  @Test
  public void testFileIOMultipleLines(@TempDir Path tempDir) {
    File testFile = tempDir.resolve("test2.txt").toFile();
    String path = testFile.getAbsolutePath().replace("\\", "\\\\");

    // Write multiple lines to file
    lisp.evaluate("(define out (open-output-file \"" + path + "\"))");
    lisp.evaluate("(write-string \"line1\" out)");
    lisp.evaluate("(write-char #\\newline out)");
    lisp.evaluate("(write-string \"line2\" out)");
    lisp.evaluate("(close-port out)");

    // Read from file
    lisp.evaluate("(define in (open-input-file \"" + path + "\"))");
    assertEquals("\"line1\"", lisp.evaluate("(read-line in)").toString());
    assertEquals("\"line2\"", lisp.evaluate("(read-line in)").toString());
    assertEquals("true", lisp.evaluate("(eof-object? (read-line in))").toString());
    lisp.evaluate("(close-port in)");
  }

  @Test
  public void testFileIOChars(@TempDir Path tempDir) {
    File testFile = tempDir.resolve("test3.txt").toFile();
    String path = testFile.getAbsolutePath().replace("\\", "\\\\");

    // Write chars to file
    lisp.evaluate("(define out (open-output-file \"" + path + "\"))");
    lisp.evaluate("(write-char #\\A out)");
    lisp.evaluate("(write-char #\\B out)");
    lisp.evaluate("(write-char #\\C out)");
    lisp.evaluate("(close-port out)");

    // Read chars from file
    lisp.evaluate("(define in (open-input-file \"" + path + "\"))");
    assertEquals("#\\A", lisp.evaluate("(read-char in)").toString());
    assertEquals("#\\B", lisp.evaluate("(read-char in)").toString());
    assertEquals("#\\C", lisp.evaluate("(read-char in)").toString());
    assertEquals("true", lisp.evaluate("(eof-object? (read-char in))").toString());
    lisp.evaluate("(close-port in)");
  }

  // Error cases

  @Test
  public void testErrorOnClosedPort() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(close-port sp)");
    assertThrows(Exception.class, () -> lisp.evaluate("(write-string \"test\" sp)"));
  }

  @Test
  public void testErrorOnGetOutputStringFromInputPort() {
    lisp.evaluate("(define ip (open-input-string \"hello\"))");
    assertThrows(Exception.class, () -> lisp.evaluate("(get-output-string ip)"));
  }

  @Test
  public void testErrorOnWriteToInputPort() {
    lisp.evaluate("(define ip (open-input-string \"hello\"))");
    assertThrows(Exception.class, () -> lisp.evaluate("(write-string \"test\" ip)"));
  }

  @Test
  public void testErrorOnReadFromOutputPort() {
    lisp.evaluate("(define op (open-output-string))");
    assertThrows(Exception.class, () -> lisp.evaluate("(read-char op)"));
  }

  // Complex scenarios

  @Test
  public void testCopyStringViaPort() {
    lisp.evaluate("(define in (open-input-string \"hello world\"))");
    lisp.evaluate("(define out (open-output-string))");
    lisp.evaluate(
        "(define (copy-loop) "
            + "  (let ((c (read-char in))) "
            + "    (if (eof-object? c) "
            + "        #t "
            + "        (begin "
            + "          (write-char c out) "
            + "          (copy-loop)))))");
    lisp.evaluate("(copy-loop)");
    assertEquals("\"hello world\"", lisp.evaluate("(get-output-string out)").toString());
  }

  @Test
  public void testBuildStringCharByChar() {
    lisp.evaluate("(define out (open-output-string))");
    lisp.evaluate("(write-char #\\S out)");
    lisp.evaluate("(write-char #\\c out)");
    lisp.evaluate("(write-char #\\h out)");
    lisp.evaluate("(write-char #\\e out)");
    lisp.evaluate("(write-char #\\m out)");
    lisp.evaluate("(write-char #\\e out)");
    assertEquals("\"Scheme\"", lisp.evaluate("(get-output-string out)").toString());
  }

  @Test
  public void testReadLineLoopPattern() {
    lisp.evaluate("(define in (open-input-string \"a\\nb\\nc\"))");
    lisp.evaluate("(define out (open-output-string))");
    lisp.evaluate(
        "(define (read-loop line) "
            + "  (if (eof-object? line) "
            + "      #t "
            + "      (begin "
            + "        (write-string line out) "
            + "        (write-char #\\, out) "
            + "        (read-loop (read-line in)))))");
    lisp.evaluate("(read-loop (read-line in))");
    assertEquals("\"a,b,c,\"", lisp.evaluate("(get-output-string out)").toString());
  }
}
