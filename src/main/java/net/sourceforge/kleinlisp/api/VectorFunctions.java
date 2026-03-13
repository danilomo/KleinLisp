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

import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
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

  /** Copies a vector. (vector-copy v) */
  public static LispObject vectorCopy(LispObject[] params) {
    if (!(params[0] instanceof VectorObject)) {
      throw new LispArgumentError("vector-copy requires a vector");
    }
    VectorObject vec = (VectorObject) params[0];
    return new VectorObject(vec.toArray());
  }
}
