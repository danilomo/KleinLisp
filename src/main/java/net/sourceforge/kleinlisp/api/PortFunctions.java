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
package net.sourceforge.kleinlisp.api;

import java.io.IOException;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.BytevectorObject;
import net.sourceforge.kleinlisp.objects.CharObject;
import net.sourceforge.kleinlisp.objects.EofObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.PortObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** R7RS Port I/O functions for KleinLisp. */
public class PortFunctions {

  // Current ports (thread-local for thread safety)
  private static final ThreadLocal<PortObject> currentInputPort = new ThreadLocal<>();
  private static final ThreadLocal<PortObject> currentOutputPort = new ThreadLocal<>();
  private static final ThreadLocal<PortObject> currentErrorPort = new ThreadLocal<>();

  static {
    // Initialize default ports
    currentInputPort.set(PortObject.fromInputStream(System.in, "<stdin>"));
    currentOutputPort.set(PortObject.fromOutputStream(System.out, "<stdout>"));
    currentErrorPort.set(PortObject.fromOutputStream(System.err, "<stderr>"));
  }

  // Port predicates

  /** (port? obj) - Returns #t if obj is a port. */
  public static LispObject isPort(LispObject[] params) {
    assertArgCount("port?", params, 1);
    return fromBoolean(params[0] instanceof PortObject);
  }

  /** (input-port? obj) - Returns #t if obj is an input port. */
  public static LispObject isInputPort(LispObject[] params) {
    assertArgCount("input-port?", params, 1);
    if (params[0] instanceof PortObject) {
      return fromBoolean(((PortObject) params[0]).isInputPort());
    }
    return BooleanObject.FALSE;
  }

  /** (output-port? obj) - Returns #t if obj is an output port. */
  public static LispObject isOutputPort(LispObject[] params) {
    assertArgCount("output-port?", params, 1);
    if (params[0] instanceof PortObject) {
      return fromBoolean(((PortObject) params[0]).isOutputPort());
    }
    return BooleanObject.FALSE;
  }

  /** (textual-port? obj) - Returns #t if obj is a textual port. */
  public static LispObject isTextualPort(LispObject[] params) {
    assertArgCount("textual-port?", params, 1);
    if (params[0] instanceof PortObject) {
      return fromBoolean(((PortObject) params[0]).isTextualPort());
    }
    return BooleanObject.FALSE;
  }

  /** (binary-port? obj) - Returns #t if obj is a binary port. */
  public static LispObject isBinaryPort(LispObject[] params) {
    assertArgCount("binary-port?", params, 1);
    if (params[0] instanceof PortObject) {
      return fromBoolean(((PortObject) params[0]).isBinaryPort());
    }
    return BooleanObject.FALSE;
  }

  /** (port-open? port) - Returns #t if port is open. */
  public static LispObject isPortOpen(LispObject[] params) {
    assertArgCount("port-open?", params, 1);
    PortObject port = asPort("port-open?", params[0]);
    return fromBoolean(!port.isClosed());
  }

  /** (input-port-open? port) - Returns #t if input port is open. */
  public static LispObject isInputPortOpen(LispObject[] params) {
    assertArgCount("input-port-open?", params, 1);
    PortObject port = asPort("input-port-open?", params[0]);
    return fromBoolean(!port.isClosed() && port.isInputPort());
  }

  /** (output-port-open? port) - Returns #t if output port is open. */
  public static LispObject isOutputPortOpen(LispObject[] params) {
    assertArgCount("output-port-open?", params, 1);
    PortObject port = asPort("output-port-open?", params[0]);
    return fromBoolean(!port.isClosed() && port.isOutputPort());
  }

  // Current ports

  /** (current-input-port) - Returns the current input port. */
  public static LispObject currentInputPortFn(LispObject[] params) {
    assertArgCount("current-input-port", params, 0);
    return currentInputPort.get();
  }

  /** (current-output-port) - Returns the current output port. */
  public static LispObject currentOutputPortFn(LispObject[] params) {
    assertArgCount("current-output-port", params, 0);
    return currentOutputPort.get();
  }

  /** (current-error-port) - Returns the current error port. */
  public static LispObject currentErrorPortFn(LispObject[] params) {
    assertArgCount("current-error-port", params, 0);
    return currentErrorPort.get();
  }

  // Current port accessors (for use by FilePortFunctions)

  public static PortObject getCurrentInputPort() {
    return currentInputPort.get();
  }

  public static void setCurrentInputPort(PortObject port) {
    currentInputPort.set(port);
  }

  public static PortObject getCurrentOutputPort() {
    return currentOutputPort.get();
  }

  public static void setCurrentOutputPort(PortObject port) {
    currentOutputPort.set(port);
  }

  // File ports

  /** (open-input-file filename) - Opens file for reading. */
  public static LispObject openInputFile(LispObject[] params) {
    assertArgCount("open-input-file", params, 1);
    String filename = asString("open-input-file", params[0]);
    try {
      return PortObject.openInputFile(filename);
    } catch (IOException e) {
      throw new LispArgumentError("Cannot open file: " + filename + ": " + e.getMessage());
    }
  }

  /** (open-output-file filename) - Opens file for writing. */
  public static LispObject openOutputFile(LispObject[] params) {
    assertArgCount("open-output-file", params, 1);
    String filename = asString("open-output-file", params[0]);
    try {
      return PortObject.openOutputFile(filename);
    } catch (IOException e) {
      throw new LispArgumentError("Cannot create file: " + filename + ": " + e.getMessage());
    }
  }

  // Binary file ports

  /** (open-binary-input-file filename) - Opens binary file for reading. */
  public static LispObject openBinaryInputFile(LispObject[] params) {
    assertArgCount("open-binary-input-file", params, 1);
    String filename = asString("open-binary-input-file", params[0]);
    try {
      return PortObject.openBinaryInputFile(filename);
    } catch (IOException e) {
      throw new LispArgumentError("Cannot open file: " + filename + ": " + e.getMessage());
    }
  }

  /** (open-binary-output-file filename) - Opens binary file for writing. */
  public static LispObject openBinaryOutputFile(LispObject[] params) {
    assertArgCount("open-binary-output-file", params, 1);
    String filename = asString("open-binary-output-file", params[0]);
    try {
      return PortObject.openBinaryOutputFile(filename);
    } catch (IOException e) {
      throw new LispArgumentError("Cannot create file: " + filename + ": " + e.getMessage());
    }
  }

  // Bytevector ports

  /** (open-input-bytevector bv) - Opens bytevector for reading. */
  public static LispObject openInputBytevector(LispObject[] params) {
    assertArgCount("open-input-bytevector", params, 1);
    BytevectorObject bv = asBytevector("open-input-bytevector", params[0]);
    return PortObject.openInputBytevector(bv);
  }

  /** (open-output-bytevector) - Creates a bytevector output port. */
  public static LispObject openOutputBytevector(LispObject[] params) {
    assertArgCount("open-output-bytevector", params, 0);
    return PortObject.openOutputBytevector();
  }

  /** (get-output-bytevector port) - Gets accumulated bytes from bytevector output port. */
  public static LispObject getOutputBytevector(LispObject[] params) {
    assertArgCount("get-output-bytevector", params, 1);
    PortObject port = asPort("get-output-bytevector", params[0]);
    return port.getOutputBytevector();
  }

  // String ports

  /** (open-input-string string) - Opens a string for reading. */
  public static LispObject openInputString(LispObject[] params) {
    assertArgCount("open-input-string", params, 1);
    String content = asString("open-input-string", params[0]);
    return PortObject.openInputString(content);
  }

  /** (open-output-string) - Creates a string output port. */
  public static LispObject openOutputString(LispObject[] params) {
    assertArgCount("open-output-string", params, 0);
    return PortObject.openOutputString();
  }

  /** (get-output-string port) - Gets the accumulated string from a string output port. */
  public static LispObject getOutputString(LispObject[] params) {
    assertArgCount("get-output-string", params, 1);
    PortObject port = asPort("get-output-string", params[0]);
    if (!port.isStringOutputPort()) {
      throw new LispArgumentError("get-output-string: expected string output port");
    }
    return new StringObject(port.getOutputString());
  }

  // Close operations

  /** (close-port port) - Closes a port. */
  public static LispObject closePort(LispObject[] params) {
    assertArgCount("close-port", params, 1);
    PortObject port = asPort("close-port", params[0]);
    try {
      port.close();
    } catch (IOException e) {
      throw new LispArgumentError("Error closing port: " + e.getMessage());
    }
    return VoidObject.VOID;
  }

  /** (close-input-port port) - Closes an input port. */
  public static LispObject closeInputPort(LispObject[] params) {
    assertArgCount("close-input-port", params, 1);
    PortObject port = asPort("close-input-port", params[0]);
    try {
      port.close();
    } catch (IOException e) {
      throw new LispArgumentError("Error closing port: " + e.getMessage());
    }
    return VoidObject.VOID;
  }

  /** (close-output-port port) - Closes an output port. */
  public static LispObject closeOutputPort(LispObject[] params) {
    assertArgCount("close-output-port", params, 1);
    PortObject port = asPort("close-output-port", params[0]);
    try {
      port.close();
    } catch (IOException e) {
      throw new LispArgumentError("Error closing port: " + e.getMessage());
    }
    return VoidObject.VOID;
  }

  // Input operations

  /** (read-char [port]) - Reads a character from port. */
  public static LispObject readChar(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("read-char", params[0]) : currentInputPort.get();
    try {
      int c = port.readChar();
      if (c == -1) {
        return EofObject.EOF;
      }
      return new CharObject((char) c);
    } catch (IOException e) {
      throw new LispArgumentError("Error reading char: " + e.getMessage());
    }
  }

  /** (peek-char [port]) - Peeks at the next character without consuming it. */
  public static LispObject peekChar(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("peek-char", params[0]) : currentInputPort.get();
    try {
      int c = port.peekChar();
      if (c == -1) {
        return EofObject.EOF;
      }
      return new CharObject((char) c);
    } catch (IOException e) {
      throw new LispArgumentError("Error peeking char: " + e.getMessage());
    }
  }

  /** (read-line [port]) - Reads a line from port. */
  public static LispObject readLine(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("read-line", params[0]) : currentInputPort.get();
    try {
      String line = port.readLine();
      if (line == null) {
        return EofObject.EOF;
      }
      return new StringObject(line);
    } catch (IOException e) {
      throw new LispArgumentError("Error reading line: " + e.getMessage());
    }
  }

  /** (char-ready? [port]) - Returns #t if a character is ready to be read. */
  public static LispObject charReady(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("char-ready?", params[0]) : currentInputPort.get();
    try {
      return fromBoolean(port.charReady());
    } catch (IOException e) {
      throw new LispArgumentError("Error checking char ready: " + e.getMessage());
    }
  }

  // EOF

  /** (eof-object) - Returns the EOF object. */
  public static LispObject eofObject(LispObject[] params) {
    assertArgCount("eof-object", params, 0);
    return EofObject.EOF;
  }

  /** (eof-object? obj) - Returns #t if obj is the EOF object. */
  public static LispObject isEofObject(LispObject[] params) {
    assertArgCount("eof-object?", params, 1);
    return fromBoolean(params[0] instanceof EofObject);
  }

  // Output operations

  /** (write-char char [port]) - Writes a character to port. */
  public static LispObject writeChar(LispObject[] params) {
    assertArgCountRange("write-char", params, 1, 2);
    char c = asChar("write-char", params[0]);
    PortObject port = params.length > 1 ? asPort("write-char", params[1]) : currentOutputPort.get();
    port.writeChar(c);
    return VoidObject.VOID;
  }

  /** (write-string string [port [start [end]]]) - Writes string or substring to port. */
  public static LispObject writeString(LispObject[] params) {
    assertArgCountRange("write-string", params, 1, 4);
    String s = asString("write-string", params[0]);
    PortObject port =
        params.length > 1 ? asPort("write-string", params[1]) : currentOutputPort.get();

    if (params.length <= 2) {
      port.writeString(s);
    } else {
      int start = asInt("write-string", params[2]);
      int end = params.length > 3 ? asInt("write-string", params[3]) : s.length();
      port.writeString(s, start, end);
    }
    return VoidObject.VOID;
  }

  /** (newline [port]) - Writes a newline to port. */
  public static LispObject newlineFn(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("newline", params[0]) : currentOutputPort.get();
    port.newline();
    return VoidObject.VOID;
  }

  /** (flush-output-port [port]) - Flushes the output port. */
  public static LispObject flushOutputPort(LispObject[] params) {
    PortObject port =
        params.length > 0 ? asPort("flush-output-port", params[0]) : currentOutputPort.get();
    port.flush();
    return VoidObject.VOID;
  }

  // Binary I/O operations

  /** (read-u8 [port]) - Reads a byte from binary port. */
  public static LispObject readU8(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("read-u8", params[0]) : currentInputPort.get();
    try {
      int b = port.readU8();
      if (b == -1) {
        return EofObject.EOF;
      }
      return IntObject.valueOf(b);
    } catch (IOException e) {
      throw new LispArgumentError("Error reading byte: " + e.getMessage());
    }
  }

  /** (peek-u8 [port]) - Peeks at the next byte without consuming it. */
  public static LispObject peekU8(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("peek-u8", params[0]) : currentInputPort.get();
    try {
      int b = port.peekU8();
      if (b == -1) {
        return EofObject.EOF;
      }
      return IntObject.valueOf(b);
    } catch (IOException e) {
      throw new LispArgumentError("Error peeking byte: " + e.getMessage());
    }
  }

  /** (u8-ready? [port]) - Returns #t if a byte is ready to be read. */
  public static LispObject u8Ready(LispObject[] params) {
    PortObject port = params.length > 0 ? asPort("u8-ready?", params[0]) : currentInputPort.get();
    try {
      return fromBoolean(port.u8Ready());
    } catch (IOException e) {
      throw new LispArgumentError("Error checking byte ready: " + e.getMessage());
    }
  }

  /** (write-u8 byte [port]) - Writes a byte to binary port. */
  public static LispObject writeU8(LispObject[] params) {
    assertArgCountRange("write-u8", params, 1, 2);
    int b = asInt("write-u8", params[0]);
    PortObject port = params.length > 1 ? asPort("write-u8", params[1]) : currentOutputPort.get();
    try {
      port.writeU8(b);
      return VoidObject.VOID;
    } catch (IOException e) {
      throw new LispArgumentError("Error writing byte: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new LispArgumentError("write-u8: " + e.getMessage());
    }
  }

  // Helper methods

  private static String asString(String name, LispObject obj) {
    if (obj instanceof StringObject) {
      return ((StringObject) obj).value();
    }
    throw new LispArgumentError(name + ": expected string, got " + obj.getClass().getSimpleName());
  }

  private static PortObject asPort(String name, LispObject obj) {
    if (obj instanceof PortObject) {
      return (PortObject) obj;
    }
    throw new LispArgumentError(name + ": expected port, got " + obj.getClass().getSimpleName());
  }

  private static char asChar(String name, LispObject obj) {
    if (obj instanceof CharObject) {
      return ((CharObject) obj).getValue();
    }
    throw new LispArgumentError(
        name + ": expected character, got " + obj.getClass().getSimpleName());
  }

  private static int asInt(String name, LispObject obj) {
    if (obj.asInt() != null) {
      return obj.asInt().value;
    }
    throw new LispArgumentError(name + ": expected integer, got " + obj.getClass().getSimpleName());
  }

  private static BytevectorObject asBytevector(String name, LispObject obj) {
    if (obj instanceof BytevectorObject) {
      return (BytevectorObject) obj;
    }
    throw new LispArgumentError(
        name + ": expected bytevector, got " + obj.getClass().getSimpleName());
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " argument(s), got " + args.length);
    }
  }

  private static void assertArgCountRange(String name, LispObject[] args, int min, int max) {
    if (args.length < min || args.length > max) {
      throw new LispArgumentError(
          name + ": expected " + min + " to " + max + " argument(s), got " + args.length);
    }
  }

  private static BooleanObject fromBoolean(boolean value) {
    return value ? BooleanObject.TRUE : BooleanObject.FALSE;
  }
}
