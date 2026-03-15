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
import java.io.FileWriter;
import java.io.IOException;
import net.sourceforge.kleinlisp.objects.LazySeqObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for line-seq and slurp functions. */
public class LineSeqTest extends BaseTestClass {

  private File tempFile;

  @BeforeEach
  public void createTempFile() throws IOException {
    tempFile = File.createTempFile("kleinlisp-test-", ".txt");
    tempFile.deleteOnExit();
  }

  @AfterEach
  public void deleteTempFile() {
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  private void writeToFile(String... lines) throws IOException {
    try (FileWriter writer = new FileWriter(tempFile)) {
      for (int i = 0; i < lines.length; i++) {
        writer.write(lines[i]);
        if (i < lines.length - 1) {
          writer.write("\n");
        }
      }
    }
  }

  // --- line-seq tests ---

  @Test
  public void testLineSeqReturnsLazySeq() throws IOException {
    writeToFile("line1", "line2", "line3");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(line-seq filename)");
    assertTrue(result instanceof LazySeqObject);
  }

  @Test
  public void testLineSeqFirst() throws IOException {
    writeToFile("first line", "second line", "third line");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(first (line-seq filename))");
    assertEquals("first line", result.asString().value());
  }

  @Test
  public void testLineSeqRest() throws IOException {
    writeToFile("line1", "line2", "line3");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    lisp.evaluate("(define lines (line-seq filename))");

    // Get rest and then first of rest
    LispObject result = lisp.evaluate("(first (rest lines))");
    assertEquals("line2", result.asString().value());
  }

  @Test
  public void testLineSeqWithMap() throws IOException {
    writeToFile("hello", "world", "test");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");

    // Map string-length over the lines
    LispObject result = lisp.evaluate("(map string-length (line-seq filename))");
    assertEquals(3, result.asList().length());
    assertEquals(5, result.asList().car().asInt().value); // "hello" = 5
    assertEquals(5, result.asList().cdr().car().asInt().value); // "world" = 5
    assertEquals(4, result.asList().cdr().cdr().car().asInt().value); // "test" = 4
  }

  @Test
  public void testLineSeqWithFilter() throws IOException {
    writeToFile("short", "a very long line indeed", "med", "another long line here");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");

    // Filter lines with length > 10
    lisp.evaluate("(define (long-line? s) (> (string-length s) 10))");
    LispObject result = lisp.evaluate("(filter long-line? (line-seq filename))");
    assertEquals(2, result.asList().length());
  }

  @Test
  public void testLineSeqWithReduce() throws IOException {
    writeToFile("abc", "defgh", "ij");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");

    // Count total characters across all lines
    lisp.evaluate("(define (add-length acc s) (+ acc (string-length s)))");
    LispObject result = lisp.evaluate("(reduce add-length 0 (line-seq filename))");
    assertEquals(10, result.asInt().value); // 3 + 5 + 2 = 10
  }

  @Test
  public void testLineSeqEmpty() throws IOException {
    writeToFile(); // Empty file
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(line-seq filename)");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testLineSeqSingleLine() throws IOException {
    writeToFile("only one line");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");

    LispObject first = lisp.evaluate("(first (line-seq filename))");
    assertEquals("only one line", first.asString().value());

    // Rest should be empty
    LispObject rest = lisp.evaluate("(rest (line-seq filename))");
    assertEquals(ListObject.NIL, rest);
  }

  @Test
  public void testLineSeqSeqable() throws IOException {
    writeToFile("test");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    assertTrue(lisp.evaluate("(seqable? (line-seq filename))").truthiness());
  }

  // --- slurp tests ---

  @Test
  public void testSlurp() throws IOException {
    writeToFile("line1", "line2", "line3");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(slurp filename)");
    assertEquals("line1\nline2\nline3", result.asString().value());
  }

  @Test
  public void testSlurpEmpty() throws IOException {
    writeToFile();
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(slurp filename)");
    assertEquals("", result.asString().value());
  }

  @Test
  public void testSlurpSingleLine() throws IOException {
    writeToFile("single line content");
    lisp.evaluate("(define filename \"" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "\")");
    LispObject result = lisp.evaluate("(slurp filename)");
    assertEquals("single line content", result.asString().value());
  }

  // --- error handling tests ---

  @Test
  public void testLineSeqFileNotFound() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(line-seq \"/nonexistent/file/path.txt\")");
        });
  }

  @Test
  public void testSlurpFileNotFound() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(slurp \"/nonexistent/file/path.txt\")");
        });
  }

  @Test
  public void testLineSeqInvalidArgument() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(line-seq 123)");
        });
  }

  @Test
  public void testSlurpInvalidArgument() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(slurp 123)");
        });
  }
}
