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
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Seq;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** Higher-order functions for KleinLisp. */
public class HigherOrderFunctions {

  /**
   * Applies a procedure to each element of a seqable, returning a list of results. (map proc coll)
   * - works with lists, vectors, sets, etc. Also supports multi-collection mapping: (map proc coll1
   * coll2 ...)
   */
  public static LispObject map(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("map requires a procedure as first argument");
    }
    Function func = funcObj.function();

    // Single collection case (most common)
    if (params.length == 2) {
      if (!SeqFunctions.isSeqable(params[1])) {
        throw new LispArgumentError("map requires a seqable as second argument");
      }
      Seq seq = SeqFunctions.getSeq(params[1]);
      if (seq == null) {
        return ListObject.NIL; // Empty collection
      }

      List<LispObject> results = new ArrayList<>();
      while (!seq.isEmpty()) {
        results.add(func.evaluate(new LispObject[] {seq.first()}));
        seq = seq.rest();
      }
      return ListObject.fromList(results.toArray(new LispObject[0]));
    }

    // Multi-collection case
    List<Seq> seqs = new ArrayList<>();
    for (int i = 1; i < params.length; i++) {
      if (!SeqFunctions.isSeqable(params[i])) {
        throw new LispArgumentError("map requires seqables as arguments");
      }
      Seq seq = SeqFunctions.getSeq(params[i]);
      if (seq == null) {
        return ListObject.NIL; // Empty collection
      }
      seqs.add(seq);
    }

    List<LispObject> results = new ArrayList<>();
    while (true) {
      // Check if any seq is exhausted
      boolean anyEmpty = false;
      for (Seq seq : seqs) {
        if (seq.isEmpty()) {
          anyEmpty = true;
          break;
        }
      }
      if (anyEmpty) break;

      // Gather elements from each seq
      LispObject[] args = new LispObject[seqs.size()];
      for (int i = 0; i < seqs.size(); i++) {
        args[i] = seqs.get(i).first();
        seqs.set(i, seqs.get(i).rest());
      }

      results.add(func.evaluate(args));
    }
    return ListObject.fromList(results.toArray(new LispObject[0]));
  }

  /**
   * Filters a seqable by a predicate. (filter pred coll) - works with lists, vectors, sets, etc.
   * Returns a list of elements for which pred returns true.
   */
  public static LispObject filter(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("filter requires a procedure as first argument");
    }
    Function pred = funcObj.function();

    if (!SeqFunctions.isSeqable(params[1])) {
      throw new LispArgumentError("filter requires a seqable as second argument");
    }
    Seq seq = SeqFunctions.getSeq(params[1]);
    if (seq == null) {
      return ListObject.NIL; // Empty collection
    }

    List<LispObject> results = new ArrayList<>();
    while (!seq.isEmpty()) {
      LispObject elem = seq.first();
      if (pred.evaluate(new LispObject[] {elem}).truthiness()) {
        results.add(elem);
      }
      seq = seq.rest();
    }
    return ListObject.fromList(results.toArray(new LispObject[0]));
  }

  /**
   * Applies a procedure to each element of a seqable for side effects. (for-each proc coll) - works
   * with lists, vectors, sets, etc. Returns void.
   */
  public static LispObject forEach(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("for-each requires a procedure as first argument");
    }
    Function func = funcObj.function();

    // Single collection case (most common)
    if (params.length == 2) {
      if (!SeqFunctions.isSeqable(params[1])) {
        throw new LispArgumentError("for-each requires a seqable as second argument");
      }
      Seq seq = SeqFunctions.getSeq(params[1]);
      if (seq == null) {
        return VoidObject.VOID; // Empty collection
      }

      while (!seq.isEmpty()) {
        func.evaluate(new LispObject[] {seq.first()});
        seq = seq.rest();
      }
      return VoidObject.VOID;
    }

    // Multi-collection case
    List<Seq> seqs = new ArrayList<>();
    for (int i = 1; i < params.length; i++) {
      if (!SeqFunctions.isSeqable(params[i])) {
        throw new LispArgumentError("for-each requires seqables as arguments");
      }
      Seq seq = SeqFunctions.getSeq(params[i]);
      if (seq == null) {
        return VoidObject.VOID; // Empty collection
      }
      seqs.add(seq);
    }

    while (true) {
      // Check if any seq is exhausted
      boolean anyEmpty = false;
      for (Seq seq : seqs) {
        if (seq.isEmpty()) {
          anyEmpty = true;
          break;
        }
      }
      if (anyEmpty) break;

      // Gather elements from each seq
      LispObject[] args = new LispObject[seqs.size()];
      for (int i = 0; i < seqs.size(); i++) {
        args[i] = seqs.get(i).first();
        seqs.set(i, seqs.get(i).rest());
      }

      func.evaluate(args);
    }
    return VoidObject.VOID;
  }

  /**
   * Left fold over a seqable. (fold-left proc init coll) - works with lists, vectors, sets, etc.
   * Applies (proc acc elem) from left to right, starting with init.
   */
  public static LispObject foldLeft(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("fold-left requires a procedure as first argument");
    }
    Function func = funcObj.function();

    LispObject acc = params[1];

    if (!SeqFunctions.isSeqable(params[2])) {
      throw new LispArgumentError("fold-left requires a seqable as third argument");
    }
    Seq seq = SeqFunctions.getSeq(params[2]);
    if (seq == null) {
      return acc; // Empty collection
    }

    while (!seq.isEmpty()) {
      acc = func.evaluate(new LispObject[] {acc, seq.first()});
      seq = seq.rest();
    }
    return acc;
  }

  /**
   * Right fold over a seqable. (fold-right proc init coll) - works with lists, vectors, sets, etc.
   * Applies (proc elem acc) from right to left.
   */
  public static LispObject foldRight(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("fold-right requires a procedure as first argument");
    }
    Function func = funcObj.function();

    LispObject init = params[1];

    if (!SeqFunctions.isSeqable(params[2])) {
      throw new LispArgumentError("fold-right requires a seqable as third argument");
    }
    Seq seq = SeqFunctions.getSeq(params[2]);
    if (seq == null) {
      return init; // Empty collection
    }

    // Convert to array for right-to-left traversal
    List<LispObject> elements = new ArrayList<>();
    while (!seq.isEmpty()) {
      elements.add(seq.first());
      seq = seq.rest();
    }

    LispObject acc = init;
    for (int i = elements.size() - 1; i >= 0; i--) {
      acc = func.evaluate(new LispObject[] {elements.get(i), acc});
    }
    return acc;
  }

  /** Alias for fold-left. (reduce proc init list) */
  public static LispObject reduce(LispObject[] params) {
    return foldLeft(params);
  }

  /**
   * Applies a procedure with a list of arguments. (apply proc args) or (apply proc arg1 ... args)
   */
  public static LispObject apply(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("apply requires a procedure as first argument");
    }
    Function func = funcObj.function();

    // Simple case: (apply proc list)
    if (params.length == 2) {
      if (params[1] == ListObject.NIL) {
        return func.evaluate(new LispObject[0]);
      }
      ListObject argList = params[1].asList();
      if (argList == null) {
        throw new LispArgumentError("apply requires a list as last argument");
      }
      LispObject[] args = new LispObject[argList.length()];
      int i = 0;
      for (LispObject elem : argList) {
        args[i++] = elem;
      }
      return func.evaluate(args);
    }

    // Complex case: (apply proc arg1 ... argN list)
    // The last argument must be a list
    LispObject lastArg = params[params.length - 1];
    ListObject tailList;
    if (lastArg == ListObject.NIL) {
      tailList = ListObject.NIL;
    } else {
      tailList = lastArg.asList();
      if (tailList == null) {
        throw new LispArgumentError("apply requires a list as last argument");
      }
    }

    // Count total arguments
    int fixedArgs = params.length - 2; // Exclude proc and final list
    int tailLength = tailList != null ? tailList.length() : 0;
    LispObject[] args = new LispObject[fixedArgs + tailLength];

    // Copy fixed arguments
    for (int i = 0; i < fixedArgs; i++) {
      args[i] = params[i + 1];
    }

    // Copy tail list arguments
    int idx = fixedArgs;
    if (tailList != null) {
      for (LispObject elem : tailList) {
        args[idx++] = elem;
      }
    }

    return func.evaluate(args);
  }

  /** Composes two functions. (compose f g) returns a function that computes (f (g x)) */
  public static LispObject compose(LispObject[] params) {
    FunctionObject fObj = params[0].asFunction();
    FunctionObject gObj = params[1].asFunction();
    if (fObj == null || gObj == null) {
      throw new LispArgumentError("compose requires two procedures");
    }
    Function f = fObj.function();
    Function g = gObj.function();

    return new FunctionObject(
        (args) -> {
          LispObject gResult = g.evaluate(args);
          return f.evaluate(new LispObject[] {gResult});
        });
  }

  /** Returns the identity function. (identity x) returns x */
  public static LispObject identity(LispObject[] params) {
    return params[0];
  }

  /** Negates a predicate. (negate pred) returns a function that returns (not (pred x)) */
  public static LispObject negate(LispObject[] params) {
    FunctionObject predObj = params[0].asFunction();
    if (predObj == null) {
      throw new LispArgumentError("negate requires a procedure");
    }
    Function pred = predObj.function();

    return new FunctionObject(
        (args) -> {
          return pred.evaluate(args).truthiness()
              ? net.sourceforge.kleinlisp.objects.BooleanObject.FALSE
              : net.sourceforge.kleinlisp.objects.BooleanObject.TRUE;
        });
  }

  /**
   * Tests if any element satisfies a predicate. (any pred coll) - works with lists, vectors, sets,
   * etc.
   */
  public static LispObject any(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("any requires a procedure as first argument");
    }
    Function pred = funcObj.function();

    if (!SeqFunctions.isSeqable(params[1])) {
      throw new LispArgumentError("any requires a seqable as second argument");
    }
    Seq seq = SeqFunctions.getSeq(params[1]);
    if (seq == null) {
      return net.sourceforge.kleinlisp.objects.BooleanObject.FALSE; // Empty collection
    }

    while (!seq.isEmpty()) {
      if (pred.evaluate(new LispObject[] {seq.first()}).truthiness()) {
        return net.sourceforge.kleinlisp.objects.BooleanObject.TRUE;
      }
      seq = seq.rest();
    }
    return net.sourceforge.kleinlisp.objects.BooleanObject.FALSE;
  }

  /**
   * Tests if all elements satisfy a predicate. (all pred coll) or (every pred coll) - works with
   * lists, vectors, sets, etc.
   */
  public static LispObject all(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("all requires a procedure as first argument");
    }
    Function pred = funcObj.function();

    if (!SeqFunctions.isSeqable(params[1])) {
      throw new LispArgumentError("all requires a seqable as second argument");
    }
    Seq seq = SeqFunctions.getSeq(params[1]);
    if (seq == null) {
      return net.sourceforge.kleinlisp.objects.BooleanObject
          .TRUE; // Empty collection, vacuously true
    }

    while (!seq.isEmpty()) {
      if (!pred.evaluate(new LispObject[] {seq.first()}).truthiness()) {
        return net.sourceforge.kleinlisp.objects.BooleanObject.FALSE;
      }
      seq = seq.rest();
    }
    return net.sourceforge.kleinlisp.objects.BooleanObject.TRUE;
  }

  /**
   * Finds the first element that satisfies a predicate. (find pred coll) - works with lists,
   * vectors, sets, etc.
   */
  public static LispObject find(LispObject[] params) {
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("find requires a procedure as first argument");
    }
    Function pred = funcObj.function();

    if (!SeqFunctions.isSeqable(params[1])) {
      throw new LispArgumentError("find requires a seqable as second argument");
    }
    Seq seq = SeqFunctions.getSeq(params[1]);
    if (seq == null) {
      return net.sourceforge.kleinlisp.objects.BooleanObject.FALSE; // Empty collection
    }

    while (!seq.isEmpty()) {
      LispObject elem = seq.first();
      if (pred.evaluate(new LispObject[] {elem}).truthiness()) {
        return elem;
      }
      seq = seq.rest();
    }
    return net.sourceforge.kleinlisp.objects.BooleanObject.FALSE;
  }
}
