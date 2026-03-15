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

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Seq;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Sequence functions for KleinLisp.
 * Provides polymorphic sequence operations that work with lists, vectors, sets,
 * and any other seqable type.
 *
 * <p>Inspired by Clojure's sequence abstraction.
 *
 * @author Danilo Oliveira
 */
public class SeqFunctions {

  /**
   * Coerces an object to a Seq.
   * (seq coll) returns nil if coll is empty, otherwise returns a seq of coll.
   */
  public static LispObject seq(LispObject[] params) {
    LispObject obj = params[0];

    // Handle nil/empty list
    if (obj == ListObject.NIL) {
      return ListObject.NIL;
    }

    Seq s = obj.asSeq();
    if (s == null) {
      return ListObject.NIL;
    }

    // Return nil if the seq is empty
    if (s.isEmpty()) {
      return ListObject.NIL;
    }

    // Convert to list for consistency
    return seqToList(s);
  }

  /**
   * Returns the first element of a seqable.
   * (first coll) returns the first element, or nil if coll is empty.
   */
  public static LispObject first(LispObject[] params) {
    LispObject obj = params[0];

    // Handle nil/empty list
    if (obj == ListObject.NIL) {
      return ListObject.NIL;
    }

    Seq s = obj.asSeq();
    if (s == null || s.isEmpty()) {
      return ListObject.NIL;
    }

    LispObject f = s.first();
    return f != null ? f : ListObject.NIL;
  }

  /**
   * Returns the rest of a seqable as a list.
   * (rest coll) returns a list of all elements except the first.
   * Returns () if coll is empty or has only one element.
   */
  public static LispObject rest(LispObject[] params) {
    LispObject obj = params[0];

    // Handle nil/empty list
    if (obj == ListObject.NIL) {
      return ListObject.NIL;
    }

    Seq s = obj.asSeq();
    if (s == null || s.isEmpty()) {
      return ListObject.NIL;
    }

    Seq r = s.rest();
    if (r.isEmpty()) {
      return ListObject.NIL;
    }

    return seqToList(r);
  }

  /**
   * Returns the next elements of a seqable (rest, but nil if no more elements).
   * (next coll) returns nil if coll has 0 or 1 element, otherwise returns rest.
   * This is the Clojure convention - next can be used for termination checks.
   */
  public static LispObject next(LispObject[] params) {
    LispObject obj = params[0];

    // Handle nil/empty list
    if (obj == ListObject.NIL) {
      return ListObject.NIL;
    }

    Seq s = obj.asSeq();
    if (s == null || s.isEmpty()) {
      return ListObject.NIL;
    }

    Seq r = s.rest();
    if (r.isEmpty()) {
      return ListObject.NIL;
    }

    return seqToList(r);
  }

  /**
   * Tests if an object is seqable.
   * (seqable? obj) returns #t if obj can be converted to a seq.
   */
  public static LispObject seqable(LispObject[] params) {
    LispObject obj = params[0];

    // NIL is seqable (empty sequence)
    if (obj == ListObject.NIL) {
      return BooleanObject.TRUE;
    }

    return obj.asSeq() != null ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * Converts a Seq to a ListObject.
   * Helper method for returning sequences as lists.
   */
  public static ListObject seqToList(Seq s) {
    if (s == null || s.isEmpty()) {
      return ListObject.NIL;
    }

    List<LispObject> elements = new ArrayList<>();
    while (!s.isEmpty()) {
      elements.add(s.first());
      s = s.rest();
    }

    return (ListObject) ListObject.fromList(elements.toArray(new LispObject[0]));
  }

  /**
   * Gets a Seq from a LispObject, handling nil and non-seqable objects.
   * Returns null if the object is not seqable or is empty.
   * This is a utility method for use by other functions.
   */
  public static Seq getSeq(LispObject obj) {
    if (obj == ListObject.NIL) {
      return null;
    }
    return obj.asSeq();
  }

  /**
   * Checks if an object is seqable (can be converted to a sequence).
   * Returns true even for empty collections.
   * This is useful for HOFs to distinguish between empty collections
   * and non-seqable objects.
   */
  public static boolean isSeqable(LispObject obj) {
    if (obj == ListObject.NIL) {
      return true;
    }

    // Check for known seqable types
    if (obj instanceof net.sourceforge.kleinlisp.objects.PVectorObject) {
      return true;
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.PSetObject) {
      return true;
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.LazySeqObject) {
      return true;
    }
    if (obj.asList() != null) {
      return true;
    }

    // Check if asSeq returns non-null for non-empty collections
    return obj.asSeq() != null;
  }
}
