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
import net.sourceforge.kleinlisp.objects.EofObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.PortObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** R7RS Bytevector functions for KleinLisp. */
public class BytevectorFunctions {

  /** Type predicate. (bytevector? obj) */
  public static LispObject isBytevector(LispObject[] params) {
    assertArgCount("bytevector?", params, 1);
    return fromBoolean(params[0] instanceof BytevectorObject);
  }

  /** Constructor. (make-bytevector k) or (make-bytevector k byte) */
  public static LispObject makeBytevector(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError(
          "make-bytevector: expected 1 or 2 arguments, got " + params.length);
    }
    int size = asInt("make-bytevector", params[0]);
    if (size < 0) {
      throw new LispArgumentError("make-bytevector: negative size: " + size);
    }
    if (params.length == 2) {
      int fill = asInt("make-bytevector", params[1]);
      if (fill < 0 || fill > 255) {
        throw new LispArgumentError("make-bytevector: fill value out of range (0-255): " + fill);
      }
      return new BytevectorObject(size, fill);
    }
    return new BytevectorObject(size);
  }

  /** Bytevector from values. (bytevector byte ...) */
  public static LispObject bytevector(LispObject[] params) {
    int[] values = new int[params.length];
    for (int i = 0; i < params.length; i++) {
      values[i] = asInt("bytevector", params[i]);
    }
    return new BytevectorObject(values);
  }

  /** Length. (bytevector-length bv) */
  public static LispObject bytevectorLength(LispObject[] params) {
    assertArgCount("bytevector-length", params, 1);
    BytevectorObject bv = asBytevector("bytevector-length", params[0]);
    return IntObject.valueOf(bv.length());
  }

  /** Access. (bytevector-u8-ref bv k) */
  public static LispObject bytevectorU8Ref(LispObject[] params) {
    assertArgCount("bytevector-u8-ref", params, 2);
    BytevectorObject bv = asBytevector("bytevector-u8-ref", params[0]);
    int index = asInt("bytevector-u8-ref", params[1]);
    return IntObject.valueOf(bv.get(index));
  }

  /** Mutation. (bytevector-u8-set! bv k byte) */
  public static LispObject bytevectorU8Set(LispObject[] params) {
    assertArgCount("bytevector-u8-set!", params, 3);
    BytevectorObject bv = asBytevector("bytevector-u8-set!", params[0]);
    int index = asInt("bytevector-u8-set!", params[1]);
    int value = asInt("bytevector-u8-set!", params[2]);
    bv.set(index, value);
    return VoidObject.VOID;
  }

  /** Copy. (bytevector-copy bv) or (bytevector-copy bv start) or (bytevector-copy bv start end) */
  public static LispObject bytevectorCopy(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("bytevector-copy: expected 1-3 arguments, got " + params.length);
    }
    BytevectorObject bv = asBytevector("bytevector-copy", params[0]);
    if (params.length == 1) {
      return bv.copy();
    }
    int start = asInt("bytevector-copy", params[1]);
    if (params.length == 2) {
      return bv.copy(start);
    }
    int end = asInt("bytevector-copy", params[2]);
    return bv.copy(start, end);
  }

  /**
   * Mutating copy. (bytevector-copy! to at from) or (bytevector-copy! to at from start) or
   * (bytevector-copy! to at from start end)
   */
  public static LispObject bytevectorCopyMutate(LispObject[] params) {
    if (params.length < 3 || params.length > 5) {
      throw new LispArgumentError("bytevector-copy!: expected 3-5 arguments, got " + params.length);
    }
    BytevectorObject target = asBytevector("bytevector-copy!", params[0]);
    int at = asInt("bytevector-copy!", params[1]);
    BytevectorObject source = asBytevector("bytevector-copy!", params[2]);

    int start = params.length > 3 ? asInt("bytevector-copy!", params[3]) : 0;
    int end = params.length > 4 ? asInt("bytevector-copy!", params[4]) : source.length();

    source.copyTo(target, at, start, end);
    return VoidObject.VOID;
  }

  /** Append. (bytevector-append bv ...) */
  public static LispObject bytevectorAppend(LispObject[] params) {
    BytevectorObject[] bvs = new BytevectorObject[params.length];
    for (int i = 0; i < params.length; i++) {
      bvs[i] = asBytevector("bytevector-append", params[i]);
    }
    return BytevectorObject.append(bvs);
  }

  /**
   * UTF-8 to string. (utf8->string bv) or (utf8->string bv start) or (utf8->string bv start end)
   */
  public static LispObject utf8ToString(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("utf8->string: expected 1-3 arguments, got " + params.length);
    }
    BytevectorObject bv = asBytevector("utf8->string", params[0]);
    if (params.length == 1) {
      return new StringObject(bv.toUtf8String());
    }
    int start = asInt("utf8->string", params[1]);
    int end = params.length > 2 ? asInt("utf8->string", params[2]) : bv.length();
    return new StringObject(bv.copy(start, end).toUtf8String());
  }

  /**
   * String to UTF-8. (string->utf8 str) or (string->utf8 str start) or (string->utf8 str start end)
   */
  public static LispObject stringToUtf8(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("string->utf8: expected 1-3 arguments, got " + params.length);
    }
    String s = asString("string->utf8", params[0]);
    if (params.length == 1) {
      return BytevectorObject.fromUtf8String(s);
    }
    int start = asInt("string->utf8", params[1]);
    int end = params.length > 2 ? asInt("string->utf8", params[2]) : s.length();
    return BytevectorObject.fromUtf8String(s, start, end);
  }

  // Bytevector I/O

  /** (read-bytevector k [port]) - Reads k bytes from port. */
  public static LispObject readBytevector(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError("read-bytevector: expected 1-2 arguments, got " + params.length);
    }
    int k = asInt("read-bytevector", params[0]);
    PortObject port =
        params.length > 1
            ? asPort("read-bytevector", params[1])
            : PortFunctions.getCurrentInputPort();

    if (k < 0) {
      throw new LispArgumentError("read-bytevector: negative count: " + k);
    }

    try {
      byte[] buffer = new byte[k];
      int totalRead = 0;
      while (totalRead < k) {
        int b = port.readU8();
        if (b == -1) {
          if (totalRead == 0) {
            return EofObject.EOF;
          }
          break;
        }
        buffer[totalRead++] = (byte) b;
      }

      if (totalRead < k) {
        byte[] result = new byte[totalRead];
        System.arraycopy(buffer, 0, result, 0, totalRead);
        return new BytevectorObject(result);
      }
      return new BytevectorObject(buffer);
    } catch (IOException e) {
      throw new LispArgumentError("read-bytevector: " + e.getMessage());
    }
  }

  /** (read-bytevector! bv [port [start [end]]]) - Reads bytes into bytevector. */
  public static LispObject readBytevectorMutate(LispObject[] params) {
    if (params.length < 1 || params.length > 4) {
      throw new LispArgumentError("read-bytevector!: expected 1-4 arguments, got " + params.length);
    }
    BytevectorObject bv = asBytevector("read-bytevector!", params[0]);
    PortObject port =
        params.length > 1
            ? asPort("read-bytevector!", params[1])
            : PortFunctions.getCurrentInputPort();
    int start = params.length > 2 ? asInt("read-bytevector!", params[2]) : 0;
    int end = params.length > 3 ? asInt("read-bytevector!", params[3]) : bv.length();

    if (start < 0 || start > bv.length()) {
      throw new LispArgumentError("read-bytevector!: start index out of range: " + start);
    }
    if (end < start || end > bv.length()) {
      throw new LispArgumentError("read-bytevector!: end index out of range: " + end);
    }

    try {
      int count = 0;
      for (int i = start; i < end; i++) {
        int b = port.readU8();
        if (b == -1) {
          return count == 0 ? EofObject.EOF : IntObject.valueOf(count);
        }
        bv.set(i, b);
        count++;
      }
      return IntObject.valueOf(count);
    } catch (IOException e) {
      throw new LispArgumentError("read-bytevector!: " + e.getMessage());
    }
  }

  /** (write-bytevector bv [port [start [end]]]) - Writes bytevector to port. */
  public static LispObject writeBytevector(LispObject[] params) {
    if (params.length < 1 || params.length > 4) {
      throw new LispArgumentError("write-bytevector: expected 1-4 arguments, got " + params.length);
    }
    BytevectorObject bv = asBytevector("write-bytevector", params[0]);
    PortObject port =
        params.length > 1
            ? asPort("write-bytevector", params[1])
            : PortFunctions.getCurrentOutputPort();
    int start = params.length > 2 ? asInt("write-bytevector", params[2]) : 0;
    int end = params.length > 3 ? asInt("write-bytevector", params[3]) : bv.length();

    if (start < 0 || start > bv.length()) {
      throw new LispArgumentError("write-bytevector: start index out of range: " + start);
    }
    if (end < start || end > bv.length()) {
      throw new LispArgumentError("write-bytevector: end index out of range: " + end);
    }

    try {
      for (int i = start; i < end; i++) {
        port.writeU8(bv.get(i));
      }
      return VoidObject.VOID;
    } catch (IOException e) {
      throw new LispArgumentError("write-bytevector: " + e.getMessage());
    }
  }

  // Helper methods

  private static int asInt(String name, LispObject obj) {
    if (obj.asInt() != null) {
      return obj.asInt().value;
    }
    throw new LispArgumentError(name + ": expected integer, got " + obj.getClass().getSimpleName());
  }

  private static String asString(String name, LispObject obj) {
    if (obj.asString() != null) {
      return obj.asString().value();
    }
    throw new LispArgumentError(name + ": expected string, got " + obj.getClass().getSimpleName());
  }

  private static BytevectorObject asBytevector(String name, LispObject obj) {
    if (obj instanceof BytevectorObject) {
      return (BytevectorObject) obj;
    }
    throw new LispArgumentError(
        name + ": expected bytevector, got " + obj.getClass().getSimpleName());
  }

  private static PortObject asPort(String name, LispObject obj) {
    if (obj instanceof PortObject) {
      return (PortObject) obj;
    }
    throw new LispArgumentError(name + ": expected port, got " + obj.getClass().getSimpleName());
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
