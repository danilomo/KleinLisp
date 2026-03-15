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
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PVectorObject;
import org.pcollections.TreePVector;

/**
 * Persistent vector functions for KleinLisp. These functions provide Clojure-like persistent
 * vector operations with structural sharing.
 */
public class PersistentVectorFunctions {

  /** Creates an empty persistent vector. (p-vec) or creates from elements (p-vec e1 e2 ...) */
  public static LispObject pVec(LispObject[] params) {
    if (params.length == 0) {
      return PVectorObject.EMPTY;
    }
    return PVectorObject.of(params);
  }

  /** Returns the element at the given index. (p-vec-ref v i) */
  public static LispObject pVecRef(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-ref requires a persistent vector as first argument");
    }
    IntObject idx = params[1].asInt();
    if (idx == null) {
      throw new LispArgumentError("p-vec-ref requires an integer index");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.get(idx.value);
  }

  /**
   * Returns a new vector with the element at index replaced. (p-vec-assoc v i val) Does not
   * modify the original vector.
   */
  public static LispObject pVecAssoc(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-assoc requires a persistent vector as first argument");
    }
    IntObject idx = params[1].asInt();
    if (idx == null) {
      throw new LispArgumentError("p-vec-assoc requires an integer index");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.assoc(idx.value, params[2]);
  }

  /** Returns a new vector with the element added at the end. (p-vec-conj v val) */
  public static LispObject pVecConj(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-conj requires a persistent vector as first argument");
    }
    PVectorObject vec = (PVectorObject) params[0];
    if (params.length == 2) {
      return vec.conj(params[1]);
    }
    // Multiple values to add
    PVectorObject result = vec;
    for (int i = 1; i < params.length; i++) {
      result = result.conj(params[i]);
    }
    return result;
  }

  /** Returns a new vector without the last element. (p-vec-pop v) */
  public static LispObject pVecPop(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-pop requires a persistent vector");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.pop();
  }

  /** Returns the last element of the vector. (p-vec-peek v) */
  public static LispObject pVecPeek(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-peek requires a persistent vector");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.peek();
  }

  /** Returns the length of the vector. (p-vec-length v) */
  public static LispObject pVecLength(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-length requires a persistent vector");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return IntObject.valueOf(vec.length());
  }

  /** Returns a subvector from start (inclusive) to end (exclusive). (p-vec-subvec v start end) */
  public static LispObject pVecSubvec(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-subvec requires a persistent vector");
    }
    IntObject start = params[1].asInt();
    IntObject end = params[2].asInt();
    if (start == null || end == null) {
      throw new LispArgumentError("p-vec-subvec requires integer indices");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.subvec(start.value, end.value);
  }

  /** Converts a persistent vector to a list. (p-vec->list v) */
  public static LispObject pVecToList(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec->list requires a persistent vector");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.toList();
  }

  /** Converts a list to a persistent vector. (list->p-vec l) */
  public static LispObject listToPVec(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return PVectorObject.EMPTY;
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list->p-vec requires a list");
    }
    org.pcollections.PVector<LispObject> pvec = TreePVector.empty();
    for (LispObject elem : list) {
      pvec = pvec.plus(elem);
    }
    return new PVectorObject(pvec);
  }

  /** Concatenates two or more persistent vectors. (p-vec-concat v1 v2 ...) */
  public static LispObject pVecConcat(LispObject[] params) {
    if (params.length == 0) {
      return PVectorObject.EMPTY;
    }
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-concat requires persistent vectors");
    }
    PVectorObject result = (PVectorObject) params[0];
    for (int i = 1; i < params.length; i++) {
      if (!(params[i] instanceof PVectorObject)) {
        throw new LispArgumentError("p-vec-concat requires persistent vectors");
      }
      result = result.concat((PVectorObject) params[i]);
    }
    return result;
  }

  /** Tests if the value is a persistent vector. (p-vec? x) */
  public static LispObject isPVec(LispObject[] params) {
    return (params[0] instanceof PVectorObject) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the persistent vector is empty. (p-vec-empty? v) */
  public static LispObject isPVecEmpty(LispObject[] params) {
    if (!(params[0] instanceof PVectorObject)) {
      throw new LispArgumentError("p-vec-empty? requires a persistent vector");
    }
    PVectorObject vec = (PVectorObject) params[0];
    return vec.length() == 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
  }
}
