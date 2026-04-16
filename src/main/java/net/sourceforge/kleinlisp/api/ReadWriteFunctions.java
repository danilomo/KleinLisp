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
import java.util.concurrent.atomic.AtomicReference;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispReadException;
import net.sourceforge.kleinlisp.Parser;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.BytevectorObject;
import net.sourceforge.kleinlisp.objects.CharObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.EofObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PortObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VectorObject;

/** R7RS Read/Write functions for S-expression serialization. */
public class ReadWriteFunctions {

  private final Parser parser;
  private final LispEnvironment environment;

  public ReadWriteFunctions(Parser parser, LispEnvironment environment) {
    this.parser = parser;
    this.environment = environment;
  }

  /** (read [port]) - Reads an S-expression from port. */
  public LispObject read(LispObject[] params) {
    PortObject port =
        params.length > 0 ? asPort("read", params[0]) : PortFunctions.getCurrentInputPort();

    try {
      StringBuilder buffer = new StringBuilder();
      int parenDepth = 0;
      boolean inString = false;
      boolean escaped = false;
      boolean started = false;

      while (true) {
        int c = port.readChar();
        if (c == -1) {
          if (!started) {
            return EofObject.EOF;
          }
          break;
        }

        char ch = (char) c;

        // Skip leading whitespace
        if (!started && Character.isWhitespace(ch)) {
          continue;
        }

        started = true;
        buffer.append(ch);

        // Track string state
        if (inString) {
          if (escaped) {
            escaped = false;
          } else if (ch == '\\') {
            escaped = true;
          } else if (ch == '"') {
            inString = false;
            // String literal complete, check if we're done
            if (parenDepth == 0) {
              break;
            }
          }
        } else {
          // Not in string
          if (ch == '"') {
            inString = true;
          } else if (ch == '(') {
            parenDepth++;
          } else if (ch == ')') {
            parenDepth--;
            if (parenDepth == 0) {
              break;
            }
            if (parenDepth < 0) {
              throw new LispReadException("read: unexpected closing parenthesis");
            }
          } else if (parenDepth == 0 && Character.isWhitespace(ch)) {
            // Non-list expression complete (atom)
            break;
          }
        }
      }

      // Parse the accumulated string
      String expression = buffer.toString().trim();
      if (expression.isEmpty()) {
        return EofObject.EOF;
      }

      AtomicReference<LispObject> result = new AtomicReference<>();
      parser.parse(expression, environment, result::set);
      return result.get();
    } catch (IOException e) {
      throw new LispReadException("read: " + e.getMessage());
    }
  }

  /** (write obj [port]) - Writes obj in machine-readable format. */
  public static LispObject write(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError("write: expected 1-2 arguments, got " + params.length);
    }
    LispObject obj = params[0];
    PortObject port =
        params.length > 1 ? asPort("write", params[1]) : PortFunctions.getCurrentOutputPort();

    String output = serializeToString(obj);
    port.writeString(output);
    return obj;
  }

  /** (write-simple obj [port]) - Writes obj in simple human-readable format. */
  public static LispObject writeSimple(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError("write-simple: expected 1-2 arguments, got " + params.length);
    }
    LispObject obj = params[0];
    PortObject port =
        params.length > 1
            ? asPort("write-simple", params[1])
            : PortFunctions.getCurrentOutputPort();

    port.writeString(obj.toString());
    return obj;
  }

  private static String serializeToString(LispObject obj) {
    if (obj instanceof StringObject) {
      return "\"" + escapeString(((StringObject) obj).value()) + "\"";
    } else if (obj instanceof CharObject) {
      return charToString(((CharObject) obj).getValue());
    } else if (obj instanceof AtomObject) {
      return ((AtomObject) obj).value();
    } else if (obj instanceof IntObject) {
      return String.valueOf(((IntObject) obj).value);
    } else if (obj instanceof DoubleObject) {
      return String.valueOf(((DoubleObject) obj).value);
    } else if (obj instanceof BooleanObject) {
      return obj.toString();
    } else if (obj instanceof ListObject) {
      return serializeList((ListObject) obj);
    } else if (obj instanceof VectorObject) {
      return serializeVector((VectorObject) obj);
    } else if (obj instanceof BytevectorObject) {
      return obj.toString();
    } else if (obj instanceof EofObject) {
      return "#<eof>";
    } else if (obj instanceof FunctionObject) {
      return obj.toString();
    } else if (obj instanceof PortObject) {
      return obj.toString();
    } else {
      return obj.toString();
    }
  }

  private static String serializeList(ListObject list) {
    if (list == ListObject.NIL) {
      return "()";
    }

    StringBuilder sb = new StringBuilder("(");
    ListObject current = list;
    boolean first = true;

    while (current != ListObject.NIL) {
      if (!first) {
        sb.append(" ");
      }
      first = false;

      sb.append(serializeToString(current.car()));

      if (current.cdr() == ListObject.NIL) {
        // Proper list
        break;
      } else if (current.cdr() instanceof ListObject) {
        // Continue with next element
        current = (ListObject) current.cdr();
      } else {
        // Improper list (dotted pair)
        sb.append(" . ");
        sb.append(serializeToString(current.cdr()));
        break;
      }
    }

    sb.append(")");
    return sb.toString();
  }

  private static String serializeVector(VectorObject vec) {
    StringBuilder sb = new StringBuilder("#(");
    for (int i = 0; i < vec.length(); i++) {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(serializeToString(vec.ref(i)));
    }
    sb.append(")");
    return sb.toString();
  }

  private static String escapeString(String str) {
    StringBuilder sb = new StringBuilder();
    for (char c : str.toCharArray()) {
      switch (c) {
        case '\\':
          sb.append("\\\\");
          break;
        case '"':
          sb.append("\\\"");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\r':
          sb.append("\\r");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String charToString(char c) {
    switch (c) {
      case ' ':
        return "#\\space";
      case '\n':
        return "#\\newline";
      case '\t':
        return "#\\tab";
      case '\r':
        return "#\\return";
      case '\u0007':
        return "#\\alarm";
      case '\b':
        return "#\\backspace";
      case '\u001B':
        return "#\\escape";
      case '\u007F':
        return "#\\delete";
      default:
        if (c < 32 || c == 127) {
          return "#\\x" + Integer.toHexString(c);
        }
        return "#\\" + c;
    }
  }

  private static PortObject asPort(String name, LispObject obj) {
    if (obj instanceof PortObject) {
      return (PortObject) obj;
    }
    throw new LispArgumentError(name + ": expected port, got " + obj.getClass().getSimpleName());
  }
}
