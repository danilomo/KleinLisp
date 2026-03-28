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
package net.sourceforge.kleinlisp.objects;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * R7RS Port object for I/O operations.
 *
 * @author daolivei
 */
public class PortObject implements LispObject, Closeable {

  public enum Direction {
    INPUT,
    OUTPUT,
    INPUT_OUTPUT
  }

  public enum Type {
    TEXTUAL,
    BINARY
  }

  private final Direction direction;
  private final Type type;
  private final String name;

  private BufferedReader reader;
  private PrintWriter writer;
  private StringWriter stringWriter;

  private boolean closed = false;

  private PortObject(Direction direction, Type type, String name) {
    this.direction = direction;
    this.type = type;
    this.name = name;
  }

  // Factory methods
  public static PortObject openInputFile(String filename) throws IOException {
    PortObject port = new PortObject(Direction.INPUT, Type.TEXTUAL, filename);
    port.reader = new BufferedReader(new FileReader(filename));
    return port;
  }

  public static PortObject openOutputFile(String filename) throws IOException {
    PortObject port = new PortObject(Direction.OUTPUT, Type.TEXTUAL, filename);
    port.writer = new PrintWriter(new FileWriter(filename));
    return port;
  }

  public static PortObject openInputString(String content) {
    PortObject port = new PortObject(Direction.INPUT, Type.TEXTUAL, "<string>");
    port.reader = new BufferedReader(new StringReader(content));
    return port;
  }

  public static PortObject openOutputString() {
    PortObject port = new PortObject(Direction.OUTPUT, Type.TEXTUAL, "<string>");
    port.stringWriter = new StringWriter();
    port.writer = new PrintWriter(port.stringWriter);
    return port;
  }

  public static PortObject fromInputStream(InputStream in, String name) {
    PortObject port = new PortObject(Direction.INPUT, Type.TEXTUAL, name);
    port.reader = new BufferedReader(new InputStreamReader(in));
    return port;
  }

  public static PortObject fromOutputStream(OutputStream out, String name) {
    PortObject port = new PortObject(Direction.OUTPUT, Type.TEXTUAL, name);
    port.writer = new PrintWriter(out, true);
    return port;
  }

  // Properties
  public boolean isInputPort() {
    return direction == Direction.INPUT || direction == Direction.INPUT_OUTPUT;
  }

  public boolean isOutputPort() {
    return direction == Direction.OUTPUT || direction == Direction.INPUT_OUTPUT;
  }

  public boolean isTextualPort() {
    return type == Type.TEXTUAL;
  }

  public boolean isBinaryPort() {
    return type == Type.BINARY;
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isStringOutputPort() {
    return stringWriter != null;
  }

  // Input operations
  public int readChar() throws IOException {
    ensureOpen();
    ensureInput();
    return reader.read();
  }

  public int peekChar() throws IOException {
    ensureOpen();
    ensureInput();
    reader.mark(1);
    int c = reader.read();
    reader.reset();
    return c;
  }

  public String readLine() throws IOException {
    ensureOpen();
    ensureInput();
    return reader.readLine();
  }

  public boolean charReady() throws IOException {
    ensureOpen();
    ensureInput();
    return reader.ready();
  }

  // Output operations
  public void writeChar(char c) {
    ensureOpen();
    ensureOutput();
    writer.print(c);
  }

  public void writeString(String s) {
    ensureOpen();
    ensureOutput();
    writer.print(s);
  }

  public void writeString(String s, int start, int end) {
    ensureOpen();
    ensureOutput();
    writer.print(s.substring(start, end));
  }

  public void flush() {
    ensureOpen();
    if (writer != null) {
      writer.flush();
    }
  }

  public void newline() {
    ensureOpen();
    ensureOutput();
    writer.println();
  }

  // For string output ports
  public String getOutputString() {
    if (stringWriter == null) {
      throw new IllegalStateException("Not a string output port");
    }
    writer.flush();
    return stringWriter.toString();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (reader != null) {
        reader.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  private void ensureOpen() {
    if (closed) {
      throw new IllegalStateException("Port is closed");
    }
  }

  private void ensureInput() {
    if (!isInputPort()) {
      throw new IllegalStateException("Not an input port");
    }
  }

  private void ensureOutput() {
    if (!isOutputPort()) {
      throw new IllegalStateException("Not an output port");
    }
  }

  @Override
  public Object asObject() {
    return this;
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    String status = closed ? " closed" : "";
    return "#<" + direction.toString().toLowerCase() + "-port" + status + ": " + name + ">";
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
