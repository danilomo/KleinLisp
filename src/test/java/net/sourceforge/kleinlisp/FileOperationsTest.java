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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for R7RS file operations (scheme.file). */
public class FileOperationsTest extends BaseTestClass {

  @TempDir Path tempDir;

  @Test
  public void testFileExistsTrue() throws IOException {
    // Create a temporary file
    File tempFile = tempDir.resolve("test.txt").toFile();
    tempFile.createNewFile();

    String code = String.format("(file-exists? \"%s\")", tempFile.getAbsolutePath());
    assertTrue(evalAsBoolean(code));
  }

  @Test
  public void testFileExistsFalse() {
    String nonExistent = tempDir.resolve("nonexistent.txt").toFile().getAbsolutePath();
    String code = String.format("(file-exists? \"%s\")", nonExistent);
    assertFalse(evalAsBoolean(code));
  }

  @Test
  public void testFileExistsWithDirectory() throws IOException {
    // Directories should also return true
    File tempSubDir = tempDir.resolve("subdir").toFile();
    tempSubDir.mkdir();

    String code = String.format("(file-exists? \"%s\")", tempSubDir.getAbsolutePath());
    assertTrue(evalAsBoolean(code));
  }

  @Test
  public void testDeleteFileSuccess() throws IOException {
    // Create a file to delete
    File tempFile = tempDir.resolve("to-delete.txt").toFile();
    tempFile.createNewFile();
    assertTrue(tempFile.exists());

    String code = String.format("(delete-file \"%s\")", tempFile.getAbsolutePath());
    eval(code);

    // Verify file was deleted
    assertFalse(tempFile.exists());
  }

  @Test
  public void testDeleteFileNonExistent() {
    String nonExistent = tempDir.resolve("nonexistent.txt").toFile().getAbsolutePath();
    String code = String.format("(delete-file \"%s\")", nonExistent);

    assertThrows(LispFileException.class, () -> eval(code));
  }

  @Test
  public void testCallWithInputFile() throws IOException {
    // Create a test file with content
    File tempFile = tempDir.resolve("input.txt").toFile();
    Files.writeString(tempFile.toPath(), "Hello, World!\n");

    String code =
        String.format(
            "(call-with-input-file \"%s\" (lambda (port) (read-line port)))",
            tempFile.getAbsolutePath());

    assertEquals("Hello, World!", evalAsString(code));
  }

  @Test
  public void testCallWithInputFileReadsMultipleLines() throws IOException {
    // Create a test file with multiple lines
    File tempFile = tempDir.resolve("multiline.txt").toFile();
    Files.writeString(tempFile.toPath(), "Line 1\nLine 2\nLine 3\n");

    String code =
        String.format(
            "(call-with-input-file \"%s\" "
                + "(lambda (port) "
                + "(list (read-line port) (read-line port) (read-line port))))",
            tempFile.getAbsolutePath());

    assertEquals("(\"Line 1\" \"Line 2\" \"Line 3\")", evalAsString(code));
  }

  @Test
  public void testCallWithOutputFile() throws IOException {
    File tempFile = tempDir.resolve("output.txt").toFile();

    String code =
        String.format(
            "(call-with-output-file \"%s\" "
                + "(lambda (port) (write-string \"Test output\" port)))",
            tempFile.getAbsolutePath());

    eval(code);

    // Verify file was created with content
    String content = Files.readString(tempFile.toPath());
    assertEquals("Test output", content);
  }

  @Test
  public void testCallWithOutputFileMultipleWrites() throws IOException {
    File tempFile = tempDir.resolve("multi-output.txt").toFile();

    String code =
        String.format(
            "(call-with-output-file \"%s\" "
                + "(lambda (port) "
                + "(write-string \"Line 1\" port) "
                + "(write-string \"\\n\" port) "
                + "(write-string \"Line 2\" port)))",
            tempFile.getAbsolutePath());

    eval(code);

    // Verify file content
    String content = Files.readString(tempFile.toPath());
    assertEquals("Line 1\nLine 2", content);
  }

  @Test
  public void testWithInputFromFile() throws IOException {
    // Create a test file
    File tempFile = tempDir.resolve("input-redirect.txt").toFile();
    Files.writeString(tempFile.toPath(), "Redirected input\n");

    String code =
        String.format(
            "(with-input-from-file \"%s\" (lambda () (read-line)))", tempFile.getAbsolutePath());

    assertEquals("Redirected input", evalAsString(code));
  }

  @Test
  public void testWithInputFromFileRestoresPort() throws IOException {
    // Create a test file
    File tempFile = tempDir.resolve("temp-input.txt").toFile();
    Files.writeString(tempFile.toPath(), "From file\n");

    // The port should be restored after with-input-from-file
    String code =
        String.format(
            "(begin "
                + "(define result (with-input-from-file \"%s\" (lambda () (read-line)))) "
                + "result)",
            tempFile.getAbsolutePath());

    assertEquals("From file", evalAsString(code));

    // Verify current-input-port is restored to stdin
    String portCheckCode = "(input-port? (current-input-port))";
    assertTrue(evalAsBoolean(portCheckCode));
  }

  @Test
  public void testWithOutputToFile() throws IOException {
    File tempFile = tempDir.resolve("output-redirect.txt").toFile();

    String code =
        String.format(
            "(with-output-to-file \"%s\" "
                + "(lambda () (write-string \"Output via current-output-port\")))",
            tempFile.getAbsolutePath());

    eval(code);

    // Verify file content
    String content = Files.readString(tempFile.toPath());
    assertEquals("Output via current-output-port", content);
  }

  @Test
  public void testWithOutputToFileRestoresPort() throws IOException {
    File tempFile = tempDir.resolve("temp-output.txt").toFile();

    String code =
        String.format(
            "(with-output-to-file \"%s\" " + "(lambda () (display \"To file\")))",
            tempFile.getAbsolutePath());

    eval(code);

    // Verify current-output-port is restored
    String portCheckCode = "(output-port? (current-output-port))";
    assertTrue(evalAsBoolean(portCheckCode));
  }

  @Test
  public void testCallWithInputFileClosesPortOnError() {
    // Use a non-existent file to trigger an error
    String nonExistent = tempDir.resolve("nonexistent.txt").toFile().getAbsolutePath();
    String code =
        String.format(
            "(call-with-input-file \"%s\" (lambda (port) (read-line port)))", nonExistent);

    assertThrows(LispFileException.class, () -> eval(code));
  }

  @Test
  public void testFileOperationsIntegration() throws IOException {
    File tempFile = tempDir.resolve("integration.txt").toFile();

    // Write to file
    String writeCode =
        String.format(
            "(call-with-output-file \"%s\" "
                + "(lambda (port) "
                + "(write-string \"First line\\nSecond line\" port)))",
            tempFile.getAbsolutePath());
    eval(writeCode);

    // Check file exists
    String existsCode = String.format("(file-exists? \"%s\")", tempFile.getAbsolutePath());
    assertTrue(evalAsBoolean(existsCode));

    // Read from file
    String readCode =
        String.format(
            "(call-with-input-file \"%s\" " + "(lambda (port) (read-line port)))",
            tempFile.getAbsolutePath());
    assertEquals("First line", evalAsString(readCode));

    // Delete file
    String deleteCode = String.format("(delete-file \"%s\")", tempFile.getAbsolutePath());
    eval(deleteCode);

    // Verify deletion
    assertFalse(evalAsBoolean(existsCode));
  }
}
