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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for binary port operations (R7RS Session 7). */
public class BinaryPortTest extends BaseTestClass {

  @TempDir Path tempDir;

  @Test
  public void testBinaryPortPredicates() {
    String code =
        "(define bin-in (open-input-bytevector (bytevector 1 2 3)))\n"
            + "(define bin-out (open-output-bytevector))\n"
            + "(list\n"
            + "  (binary-port? bin-in)\n"
            + "  (binary-port? bin-out)\n"
            + "  (input-port? bin-in)\n"
            + "  (output-port? bin-out))";
    assertEquals("(true true true true)", evalAsList(code).toString());
  }

  @Test
  public void testOpenBinaryInputFile() throws IOException {
    Path testFile = tempDir.resolve("test.bin");
    Files.write(testFile, new byte[] {72, 101, 108, 108, 111}); // "Hello"

    String code =
        String.format(
            "(define port (open-binary-input-file \"%s\"))\n"
                + "(define result (list\n"
                + "  (read-u8 port)\n"
                + "  (read-u8 port)\n"
                + "  (read-u8 port)\n"
                + "  (read-u8 port)\n"
                + "  (read-u8 port)))\n"
                + "(close-port port)\n"
                + "result",
            testFile.toString().replace("\\", "\\\\"));
    assertEquals("(72 101 108 108 111)", evalAsList(code).toString());
  }

  @Test
  public void testOpenBinaryOutputFile() throws IOException {
    Path testFile = tempDir.resolve("output.bin");

    String code =
        String.format(
            "(define port (open-binary-output-file \"%s\"))\n"
                + "(write-u8 72 port)\n"
                + "(write-u8 105 port)\n"
                + "(close-port port)",
            testFile.toString().replace("\\", "\\\\"));
    lisp.evaluate(code);

    byte[] content = Files.readAllBytes(testFile);
    assertArrayEquals(new byte[] {72, 105}, content);
  }

  @Test
  public void testBytevectorInputPort() {
    String code =
        "(define bv (bytevector 10 20 30 40 50))\n"
            + "(define port (open-input-bytevector bv))\n"
            + "(list\n"
            + "  (read-u8 port)\n"
            + "  (peek-u8 port)\n"
            + "  (read-u8 port)\n"
            + "  (read-u8 port))";
    assertEquals("(10 20 20 30)", evalAsList(code).toString());
  }

  @Test
  public void testBytevectorOutputPort() {
    String code =
        "(define port (open-output-bytevector))\n"
            + "(write-u8 65 port)\n"
            + "(write-u8 66 port)\n"
            + "(write-u8 67 port)\n"
            + "(get-output-bytevector port)";
    assertEquals("#u8(65 66 67)", lisp.evaluate(code).toString());
  }

  @Test
  public void testReadU8EOF() {
    String code =
        "(define port (open-input-bytevector (bytevector 1 2)))\n"
            + "(list\n"
            + "  (read-u8 port)\n"
            + "  (read-u8 port)\n"
            + "  (eof-object? (read-u8 port)))";
    assertEquals("(1 2 true)", evalAsList(code).toString());
  }

  @Test
  public void testPeekU8() {
    String code =
        "(define port (open-input-bytevector (bytevector 99)))\n"
            + "(list\n"
            + "  (peek-u8 port)\n"
            + "  (peek-u8 port)\n"
            + "  (read-u8 port)\n"
            + "  (eof-object? (peek-u8 port)))";
    assertEquals("(99 99 99 true)", evalAsList(code).toString());
  }

  @Test
  public void testU8Ready() {
    String code =
        "(define port (open-input-bytevector (bytevector 42)))\n"
            + "(list\n"
            + "  (u8-ready? port)\n"
            + "  (read-u8 port)\n"
            + "  (u8-ready? port))";
    assertEquals("(true 42 false)", evalAsList(code).toString());
  }

  @Test
  public void testReadBytevector() {
    String code =
        "(define port (open-input-bytevector (bytevector 1 2 3 4 5)))\n"
            + "(read-bytevector 3 port)";
    assertEquals("#u8(1 2 3)", lisp.evaluate(code).toString());
  }

  @Test
  public void testReadBytevectorPartial() {
    String code =
        "(define port (open-input-bytevector (bytevector 1 2 3)))\n" + "(read-bytevector 10 port)";
    assertEquals("#u8(1 2 3)", lisp.evaluate(code).toString());
  }

  @Test
  public void testReadBytevectorEOF() {
    String code =
        "(define port (open-input-bytevector (bytevector)))\n"
            + "(eof-object? (read-bytevector 5 port))";
    assertEquals("true", lisp.evaluate(code).toString());
  }

  @Test
  public void testReadBytevectorMutate() {
    String code =
        "(define port (open-input-bytevector (bytevector 10 20 30 40)))\n"
            + "(define bv (make-bytevector 5 0))\n"
            + "(read-bytevector! bv port 1 4)\n"
            + "bv";
    assertEquals("#u8(0 10 20 30 0)", lisp.evaluate(code).toString());
  }

  @Test
  public void testReadBytevectorMutateReturnsCount() {
    String code =
        "(define port (open-input-bytevector (bytevector 1 2)))\n"
            + "(define bv (make-bytevector 10))\n"
            + "(read-bytevector! bv port)";
    assertEquals(2, evalAsInt(code));
  }

  @Test
  public void testWriteBytevector() {
    String code =
        "(define port (open-output-bytevector))\n"
            + "(write-bytevector (bytevector 10 20 30 40 50) port)\n"
            + "(get-output-bytevector port)";
    assertEquals("#u8(10 20 30 40 50)", lisp.evaluate(code).toString());
  }

  @Test
  public void testWriteBytevectorRange() {
    String code =
        "(define port (open-output-bytevector))\n"
            + "(write-bytevector (bytevector 10 20 30 40 50) port 1 4)\n"
            + "(get-output-bytevector port)";
    assertEquals("#u8(20 30 40)", lisp.evaluate(code).toString());
  }

  @Test
  public void testBinaryFileRoundTrip() throws IOException {
    Path testFile = tempDir.resolve("roundtrip.bin");

    String writeCode =
        String.format(
            "(define port (open-binary-output-file \"%s\"))\n"
                + "(write-bytevector (bytevector 0 1 127 128 255) port)\n"
                + "(close-port port)",
            testFile.toString().replace("\\", "\\\\"));
    lisp.evaluate(writeCode);

    String readCode =
        String.format(
            "(define port (open-binary-input-file \"%s\"))\n"
                + "(define result (read-bytevector 5 port))\n"
                + "(close-port port)\n"
                + "result",
            testFile.toString().replace("\\", "\\\\"));
    assertEquals("#u8(0 1 127 128 255)", lisp.evaluate(readCode).toString());
  }

  @Test
  public void testWriteU8OutOfRange() {
    assertThrows(
        LispArgumentError.class,
        () -> lisp.evaluate("(define port (open-output-bytevector))\n" + "(write-u8 256 port)"));

    assertThrows(
        LispArgumentError.class,
        () -> lisp.evaluate("(define port (open-output-bytevector))\n" + "(write-u8 -1 port)"));
  }

  @Test
  public void testReadFromClosedPort() throws IOException {
    Path testFile = tempDir.resolve("closed.bin");
    Files.write(testFile, new byte[] {1, 2, 3});

    String code =
        String.format(
            "(define port (open-binary-input-file \"%s\"))\n"
                + "(close-port port)\n"
                + "(read-u8 port)",
            testFile.toString().replace("\\", "\\\\"));
    assertThrows(Exception.class, () -> lisp.evaluate(code));
  }

  @Test
  public void testPortOpenPredicates() {
    String code =
        "(define port (open-input-bytevector (bytevector 1 2)))\n"
            + "(define open-before (port-open? port))\n"
            + "(close-port port)\n"
            + "(define open-after (port-open? port))\n"
            + "(list open-before open-after)";
    assertEquals("(true false)", evalAsList(code).toString());
  }

  @Test
  public void testDefaultPortsWithBinaryOperations() {
    String code =
        "(define bv (bytevector 5 10 15))\n"
            + "(define port (open-input-bytevector bv))\n"
            + "(read-u8 port)";
    assertEquals(5, evalAsInt(code));
  }

  @Test
  public void testMultipleBytevectorOperations() {
    String code =
        "(define out (open-output-bytevector))\n"
            + "(write-u8 100 out)\n"
            + "(write-u8 200 out)\n"
            + "(define bv (get-output-bytevector out))\n"
            + "(define in (open-input-bytevector bv))\n"
            + "(list (read-u8 in) (read-u8 in))";
    assertEquals("(100 200)", evalAsList(code).toString());
  }
}
