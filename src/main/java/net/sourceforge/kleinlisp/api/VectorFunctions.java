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

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.CharObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VectorObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** Vector manipulation functions for KleinLisp. */
public class VectorFunctions {

  /** Creates a vector of the given size. (make-vector n) or (make-vector n fill) */
  public static LispObject makeVector(LispObject[] params) {
    IntObject size = params[0].asInt();
    if (size == null) {
      throw new LispArgumentError("make-vector requires an integer size");
    }
    if (size.value < 0) {
      throw new LispArgumentError("make-vector: size must be non-negative: " + size.value);
    }
    if (params.length > 1) {
      return new VectorObject(size.value, params[1]);
    }
    return new VectorObject(size.value);
  }

  /** Creates a vector from arguments. (vector e1 e2 ...) */
  public static LispObject vector(LispObject[] params) {
    return new VectorObject(params);
  }

  /** Returns the element at the given index. (vector-ref v i) */
  public static LispObject vectorRef(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-ref requires a vector as first argument");
    }
    IntObject idx = params[1].asInt();
    if (idx == null) {
      throw new LispArgumentError("vector-ref requires an integer index");
    }
    VectorObject vec = (VectorObject) params[0];
    return vec.ref(idx.value);
  }

  /** Sets the element at the given index. (vector-set! v i val) */
  public static LispObject vectorSet(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-set! requires a vector as first argument");
    }
    IntObject idx = params[1].asInt();
    if (idx == null) {
      throw new LispArgumentError("vector-set! requires an integer index");
    }
    VectorObject vec = (VectorObject) params[0];
    vec.set(idx.value, params[2]);
    return VoidObject.VOID;
  }

  /** Returns the length of the vector. (vector-length v) */
  public static LispObject vectorLength(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-length requires a vector");
    }
    VectorObject vec = (VectorObject) params[0];
    return IntObject.valueOf(vec.length());
  }

  /** Converts a vector to a list. (vector->list v) */
  public static LispObject vectorToList(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector->list requires a vector");
    }
    VectorObject vec = (VectorObject) params[0];
    return vec.toList();
  }

  /** Converts a list to a vector. (list->vector l) */
  public static LispObject listToVector(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return new VectorObject(0);
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list->vector requires a list");
    }
    LispObject[] elements = new LispObject[list.length()];
    int i = 0;
    for (LispObject elem : list) {
      elements[i++] = elem;
    }
    return new VectorObject(elements);
  }

  /** Fills a vector with a value. (vector-fill! v fill) */
  public static LispObject vectorFill(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-fill! requires a vector");
    }
    VectorObject vec = (VectorObject) params[0];
    LispObject fill = params[1];
    for (int i = 0; i < vec.length(); i++) {
      vec.set(i, fill);
    }
    return VoidObject.VOID;
  }

  /** Copies a vector. (vector-copy v) or with start/end */
  public static LispObject vectorCopy(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("vector-copy: expected 1-3 arguments");
    }
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-copy requires a vector");
    }
    VectorObject vec = (VectorObject) params[0];

    int start = 0;
    int end = vec.length();

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("vector-copy: expected integer start index");
      }
      start = startObj.value;
    }
    if (params.length > 2) {
      IntObject endObj = params[2].asInt();
      if (endObj == null) {
        throw new LispArgumentError("vector-copy: expected integer end index");
      }
      end = endObj.value;
    }

    if (start < 0 || end > vec.length() || start > end) {
      throw new LispArgumentError("vector-copy: index out of bounds");
    }

    LispObject[] elements = new LispObject[end - start];
    for (int i = start; i < end; i++) {
      elements[i - start] = vec.ref(i);
    }
    return new VectorObject(elements);
  }

  /** Appends multiple vectors. (vector-append vector ...) */
  public static LispObject vectorAppend(LispObject[] params) {
    int totalLen = 0;
    for (LispObject arg : params) {
      if (!(arg instanceof VectorObject)) {
        throw new LispArgumentError("vector-append: expected vector arguments");
      }
      totalLen += ((VectorObject) arg).length();
    }

    LispObject[] result = new LispObject[totalLen];
    int pos = 0;
    for (LispObject arg : params) {
      VectorObject v = (VectorObject) arg;
      for (int i = 0; i < v.length(); i++) {
        result[pos++] = v.ref(i);
      }
    }
    return new VectorObject(result);
  }

  /** Maps a procedure over vector elements. (vector-map proc vector ...) */
  public static LispObject vectorMap(LispObject[] params) {
    if (params.length < 2) {
      throw new LispArgumentError("vector-map: expected at least 2 arguments");
    }
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("vector-map: expected procedure as first argument");
    }
    Function proc = funcObj.function();

    VectorObject[] vectors = new VectorObject[params.length - 1];
    int minLen = Integer.MAX_VALUE;
    for (int i = 0; i < vectors.length; i++) {
      if (!(params[i + 1] instanceof VectorObject)) {
        throw new LispArgumentError("vector-map: expected vector arguments");
      }
      vectors[i] = (VectorObject) params[i + 1];
      minLen = Math.min(minLen, vectors[i].length());
    }

    LispObject[] result = new LispObject[minLen];
    for (int i = 0; i < minLen; i++) {
      LispObject[] elements = new LispObject[vectors.length];
      for (int j = 0; j < vectors.length; j++) {
        elements[j] = vectors[j].ref(i);
      }
      result[i] = proc.evaluate(elements);
    }
    return new VectorObject(result);
  }

  /**
   * Applies a procedure to each vector element for side effects. (vector-for-each proc vector ...)
   */
  public static LispObject vectorForEach(LispObject[] params) {
    if (params.length < 2) {
      throw new LispArgumentError("vector-for-each: expected at least 2 arguments");
    }
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("vector-for-each: expected procedure as first argument");
    }
    Function proc = funcObj.function();

    VectorObject[] vectors = new VectorObject[params.length - 1];
    int minLen = Integer.MAX_VALUE;
    for (int i = 0; i < vectors.length; i++) {
      if (!(params[i + 1] instanceof VectorObject)) {
        throw new LispArgumentError("vector-for-each: expected vector arguments");
      }
      vectors[i] = (VectorObject) params[i + 1];
      minLen = Math.min(minLen, vectors[i].length());
    }

    for (int i = 0; i < minLen; i++) {
      LispObject[] elements = new LispObject[vectors.length];
      for (int j = 0; j < vectors.length; j++) {
        elements[j] = vectors[j].ref(i);
      }
      proc.evaluate(elements);
    }
    return VoidObject.VOID;
  }

  /** Converts a vector of characters to a string. (vector->string vector) or with start/end */
  public static LispObject vectorToString(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("vector->string: expected 1-3 arguments");
    }
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector->string: expected vector");
    }
    VectorObject vec = (VectorObject) params[0];

    int start = 0;
    int end = vec.length();

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("vector->string: expected integer start index");
      }
      start = startObj.value;
    }
    if (params.length > 2) {
      IntObject endObj = params[2].asInt();
      if (endObj == null) {
        throw new LispArgumentError("vector->string: expected integer end index");
      }
      end = endObj.value;
    }

    if (start < 0 || end > vec.length() || start > end) {
      throw new LispArgumentError("vector->string: index out of bounds");
    }

    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      LispObject obj = vec.ref(i);
      if (!(obj instanceof CharObject)) {
        throw new LispArgumentError("vector->string: vector must contain characters");
      }
      sb.append(((CharObject) obj).getValue());
    }
    return new StringObject(sb.toString());
  }

  /** Converts a string to a vector of characters. (string->vector string) or with start/end */
  public static LispObject stringToVector(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("string->vector: expected 1-3 arguments");
    }
    StringObject strObj = params[0].asString();
    if (strObj == null) {
      throw new LispArgumentError("string->vector: expected string");
    }
    String s = strObj.value();

    int start = 0;
    int end = s.length();

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("string->vector: expected integer start index");
      }
      start = startObj.value;
    }
    if (params.length > 2) {
      IntObject endObj = params[2].asInt();
      if (endObj == null) {
        throw new LispArgumentError("string->vector: expected integer end index");
      }
      end = endObj.value;
    }

    if (start < 0 || end > s.length() || start > end) {
      throw new LispArgumentError("string->vector: index out of bounds");
    }

    LispObject[] chars = new LispObject[end - start];
    for (int i = start; i < end; i++) {
      chars[i - start] = new CharObject(s.charAt(i));
    }
    return new VectorObject(chars);
  }
}
