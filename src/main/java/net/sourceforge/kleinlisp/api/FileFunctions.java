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

import java.io.File;
import java.io.IOException;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.PortObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** R7RS File operations (scheme.file). */
public class FileFunctions {

  /** (file-exists? filename) - Returns #t if the file exists. */
  public static LispObject fileExists(LispObject[] params) {
    assertArgCount("file-exists?", params, 1);
    String filename = asString("file-exists?", params[0]);
    File file = new File(filename);
    return fromBoolean(file.exists());
  }

  /** (delete-file filename) - Deletes the specified file. */
  public static LispObject deleteFile(LispObject[] params) {
    assertArgCount("delete-file", params, 1);
    String filename = asString("delete-file", params[0]);
    File file = new File(filename);

    if (!file.exists()) {
      throw new LispArgumentError("delete-file: file does not exist: " + filename);
    }

    boolean success = file.delete();
    if (!success) {
      throw new LispArgumentError("delete-file: failed to delete file: " + filename);
    }

    return VoidObject.VOID;
  }

  /**
   * (call-with-input-file filename proc) - Opens filename for input, calls proc with the port, then
   * closes the port.
   */
  public static LispObject callWithInputFile(LispObject[] params) {
    assertArgCount("call-with-input-file", params, 2);
    String filename = asString("call-with-input-file", params[0]);
    FunctionObject procObj = asFunction("call-with-input-file", params[1]);
    Function proc = procObj.function();

    PortObject port = null;
    try {
      port = PortObject.openInputFile(filename);
      LispObject[] args = new LispObject[] {port};

      // Call the procedure with the port
      return proc.evaluate(args);
    } catch (IOException e) {
      throw new LispArgumentError("call-with-input-file: " + e.getMessage());
    } finally {
      if (port != null) {
        try {
          port.close();
        } catch (IOException ignored) {
          // Ignore errors during cleanup
        }
      }
    }
  }

  /**
   * (call-with-output-file filename proc) - Opens filename for output, calls proc with the port,
   * then closes the port.
   */
  public static LispObject callWithOutputFile(LispObject[] params) {
    assertArgCount("call-with-output-file", params, 2);
    String filename = asString("call-with-output-file", params[0]);
    FunctionObject procObj = asFunction("call-with-output-file", params[1]);
    Function proc = procObj.function();

    PortObject port = null;
    try {
      port = PortObject.openOutputFile(filename);
      LispObject[] args = new LispObject[] {port};

      // Call the procedure with the port
      return proc.evaluate(args);
    } catch (IOException e) {
      throw new LispArgumentError("call-with-output-file: " + e.getMessage());
    } finally {
      if (port != null) {
        try {
          port.flush();
          port.close();
        } catch (IOException ignored) {
          // Ignore errors during cleanup
        }
      }
    }
  }

  /**
   * (with-input-from-file filename thunk) - Temporarily binds current-input-port to filename, calls
   * thunk, then restores the original port.
   */
  public static LispObject withInputFromFile(LispObject[] params) {
    assertArgCount("with-input-from-file", params, 2);
    String filename = asString("with-input-from-file", params[0]);
    FunctionObject thunkObj = asFunction("with-input-from-file", params[1]);
    Function thunk = thunkObj.function();

    PortObject port = null;
    PortObject savedPort = PortFunctions.getCurrentInputPort();

    try {
      port = PortObject.openInputFile(filename);
      PortFunctions.setCurrentInputPort(port);

      // Call the thunk
      return thunk.evaluate(new LispObject[0]);
    } catch (IOException e) {
      throw new LispArgumentError("with-input-from-file: " + e.getMessage());
    } finally {
      // Always restore the original port
      PortFunctions.setCurrentInputPort(savedPort);
      if (port != null) {
        try {
          port.close();
        } catch (IOException ignored) {
          // Ignore errors during cleanup
        }
      }
    }
  }

  /**
   * (with-output-to-file filename thunk) - Temporarily binds current-output-port to filename, calls
   * thunk, then restores the original port.
   */
  public static LispObject withOutputToFile(LispObject[] params) {
    assertArgCount("with-output-to-file", params, 2);
    String filename = asString("with-output-to-file", params[0]);
    FunctionObject thunkObj = asFunction("with-output-to-file", params[1]);
    Function thunk = thunkObj.function();

    PortObject port = null;
    PortObject savedPort = PortFunctions.getCurrentOutputPort();

    try {
      port = PortObject.openOutputFile(filename);
      PortFunctions.setCurrentOutputPort(port);

      // Call the thunk
      return thunk.evaluate(new LispObject[0]);
    } catch (IOException e) {
      throw new LispArgumentError("with-output-to-file: " + e.getMessage());
    } finally {
      // Always restore the original port
      PortFunctions.setCurrentOutputPort(savedPort);
      if (port != null) {
        try {
          port.flush();
          port.close();
        } catch (IOException ignored) {
          // Ignore errors during cleanup
        }
      }
    }
  }

  // Helper methods

  private static String asString(String name, LispObject obj) {
    if (obj instanceof StringObject) {
      return ((StringObject) obj).value();
    }
    throw new LispArgumentError(name + ": expected string, got " + obj.getClass().getSimpleName());
  }

  private static FunctionObject asFunction(String name, LispObject obj) {
    if (obj instanceof FunctionObject) {
      return (FunctionObject) obj;
    }
    throw new LispArgumentError(
        name + ": expected procedure, got " + obj.getClass().getSimpleName());
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " argument(s), got " + args.length);
    }
  }

  private static BooleanObject fromBoolean(boolean value) {
    return value ? BooleanObject.TRUE : BooleanObject.FALSE;
  }
}
