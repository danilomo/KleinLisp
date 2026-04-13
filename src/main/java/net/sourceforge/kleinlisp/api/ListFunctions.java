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

import static net.sourceforge.kleinlisp.api.GuileErrors.*;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * @author Danilo Oliveira
 */
public class ListFunctions {

  public static LispObject length(LispObject[] params) {
    ListObject arg = params[0].asList();

    return IntObject.valueOf(arg.length());
  }

  public static LispObject car(LispObject[] params) {
    ListObject arg = requirePair("car", params[0], 1);

    return arg.head();
  }

  public static LispObject cdr(LispObject[] params) {
    ListObject arg = requirePair("cdr", params[0], 1);

    return arg.tail();
  }

  public static LispObject cons(LispObject[] params) {
    return new ListObject(params[0], params[1]);
  }

  public static LispObject isNull(LispObject[] params) {
    if (params[0] == null || params[0] == ListObject.NIL) {
      return BooleanObject.TRUE;
    }
    return BooleanObject.FALSE;
  }

  public static LispObject list(LispObject[] params) {
    return ListObject.fromList(params);
  }

  public static LispObject reverse(ListObject parameters) {
    ListObject newl = ListObject.NIL;

    for (LispObject par : parameters) {
      newl = new ListObject(par, newl);
    }

    return newl;
  }

  /** Appends multiple lists. (append list1 list2 ...) */
  public static LispObject append(LispObject[] params) {
    if (params.length == 0) {
      return ListObject.NIL;
    }

    List<LispObject> result = new ArrayList<>();

    // Process all but the last argument (they must be proper lists)
    for (int i = 0; i < params.length - 1; i++) {
      if (params[i] == ListObject.NIL) {
        continue;
      }
      ListObject list = params[i].asList();
      if (list == null) {
        throw new LispArgumentError("append requires list arguments");
      }
      for (LispObject elem : list) {
        result.add(elem);
      }
    }

    // Last argument can be anything (for improper lists support)
    LispObject lastArg = params[params.length - 1];
    if (lastArg == ListObject.NIL) {
      return ListObject.fromList(result.toArray(new LispObject[0]));
    }

    ListObject lastList = lastArg.asList();
    if (lastList != null) {
      for (LispObject elem : lastList) {
        result.add(elem);
      }
      return ListObject.fromList(result.toArray(new LispObject[0]));
    }

    // If last arg is not a list, we just append the elements we have
    // (Standard Scheme allows non-list as last arg for improper lists)
    if (result.isEmpty()) {
      return lastArg;
    }

    // Build list from result and append lastArg
    ListObject resultList = new ListObject(lastArg, ListObject.NIL);
    for (int i = result.size() - 1; i >= 0; i--) {
      resultList = new ListObject(result.get(i), resultList);
    }
    return resultList;
  }

  /** Finds an element in a list using equal?. (member obj list) */
  public static LispObject member(LispObject[] params) {
    LispObject obj = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject list = params[1].asList();
    if (list == null) {
      throw new LispArgumentError("member requires a list as second argument");
    }

    while (list != ListObject.NIL) {
      if (EqualityFunctions.equal(new LispObject[] {obj, list.car()}) == BooleanObject.TRUE) {
        return list;
      }
      list = list.cdr();
    }
    return BooleanObject.FALSE;
  }

  /** Finds an element in a list using eq?. (memq obj list) */
  public static LispObject memq(LispObject[] params) {
    LispObject obj = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject list = params[1].asList();
    if (list == null) {
      throw new LispArgumentError("memq requires a list as second argument");
    }

    while (list != ListObject.NIL) {
      if (EqualityFunctions.eq(new LispObject[] {obj, list.car()}) == BooleanObject.TRUE) {
        return list;
      }
      list = list.cdr();
    }
    return BooleanObject.FALSE;
  }

  /** Finds an element in a list using eqv?. (memv obj list) */
  public static LispObject memv(LispObject[] params) {
    LispObject obj = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject list = params[1].asList();
    if (list == null) {
      throw new LispArgumentError("memv requires a list as second argument");
    }

    while (list != ListObject.NIL) {
      if (EqualityFunctions.eqv(new LispObject[] {obj, list.car()}) == BooleanObject.TRUE) {
        return list;
      }
      list = list.cdr();
    }
    return BooleanObject.FALSE;
  }

  /** Association list lookup using equal?. (assoc key alist) */
  public static LispObject assoc(LispObject[] params) {
    LispObject key = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject alist = params[1].asList();
    if (alist == null) {
      throw new LispArgumentError("assoc requires a list as second argument");
    }

    for (LispObject pair : alist) {
      ListObject pairList = pair.asList();
      if (pairList != null && pairList != ListObject.NIL) {
        if (EqualityFunctions.equal(new LispObject[] {key, pairList.car()}) == BooleanObject.TRUE) {
          return pair;
        }
      }
    }
    return BooleanObject.FALSE;
  }

  /** Association list lookup using eq?. (assq key alist) */
  public static LispObject assq(LispObject[] params) {
    LispObject key = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject alist = params[1].asList();
    if (alist == null) {
      throw new LispArgumentError("assq requires a list as second argument");
    }

    for (LispObject pair : alist) {
      ListObject pairList = pair.asList();
      if (pairList != null && pairList != ListObject.NIL) {
        if (EqualityFunctions.eq(new LispObject[] {key, pairList.car()}) == BooleanObject.TRUE) {
          return pair;
        }
      }
    }
    return BooleanObject.FALSE;
  }

  /** Association list lookup using eqv?. (assv key alist) */
  public static LispObject assv(LispObject[] params) {
    LispObject key = params[0];
    if (params[1] == ListObject.NIL) {
      return BooleanObject.FALSE;
    }
    ListObject alist = params[1].asList();
    if (alist == null) {
      throw new LispArgumentError("assv requires a list as second argument");
    }

    for (LispObject pair : alist) {
      ListObject pairList = pair.asList();
      if (pairList != null && pairList != ListObject.NIL) {
        if (EqualityFunctions.eqv(new LispObject[] {key, pairList.car()}) == BooleanObject.TRUE) {
          return pair;
        }
      }
    }
    return BooleanObject.FALSE;
  }

  /** Gets the value directly from an association list. (assoc-ref key alist) */
  public static LispObject assocRef(LispObject[] params) {
    LispObject result = assoc(params);
    if (result == BooleanObject.FALSE) {
      return BooleanObject.FALSE;
    }
    ListObject pair = result.asList();
    if (pair != null && pair.cdr() != ListObject.NIL) {
      // For dotted pairs like (key . value), cdr returns the value directly
      LispObject cdrVal = pair.cdr();
      if (cdrVal.asList() != null) {
        // For proper lists like (key value), return car of cdr
        return cdrVal.asList().car();
      }
      // For dotted pairs, return cdr directly
      return cdrVal;
    }
    return BooleanObject.FALSE;
  }

  /** Gets element at 0-indexed position. (nth index list) - alias for list-ref with swapped args */
  public static LispObject nth(LispObject[] params) {
    // nth takes (index list) while list-ref takes (list index)
    return listRef(new LispObject[] {params[1], params[0]});
  }

  /** Gets element at index. (list-ref list k) */
  public static LispObject listRef(LispObject[] params) {
    ListObject list = requireList("list-ref", params[0], 1);
    int k = requireNonNegativeInt("list-ref", params[1], 2);

    if (list == ListObject.NIL) {
      throw argOutOfRange("list-ref", 2, k);
    }

    ListObject current = list;
    for (int i = 0; i < k; i++) {
      if (current == ListObject.NIL) {
        throw argOutOfRange("list-ref", 2, k);
      }
      current = current.cdr();
    }

    if (current == ListObject.NIL) {
      throw argOutOfRange("list-ref", 2, k);
    }
    return current.car();
  }

  /** Gets tail starting at index. (list-tail list k) */
  public static LispObject listTail(LispObject[] params) {
    ListObject list = requireList("list-tail", params[0], 1);
    int k = requireNonNegativeInt("list-tail", params[1], 2);

    if (list == ListObject.NIL && k == 0) {
      return ListObject.NIL;
    }

    if (list == ListObject.NIL) {
      throw argOutOfRange("list-tail", 2, k);
    }

    ListObject current = list;
    for (int i = 0; i < k; i++) {
      if (current == ListObject.NIL) {
        throw argOutOfRange("list-tail", 2, k);
      }
      current = current.cdr();
    }

    return current;
  }

  // Car/Cdr compositions
  public static LispObject caar(LispObject[] params) {
    return params[0].asList().car().asList().car();
  }

  public static LispObject cadr(LispObject[] params) {
    return params[0].asList().cdr().car();
  }

  public static LispObject cdar(LispObject[] params) {
    return params[0].asList().car().asList().cdr();
  }

  public static LispObject cddr(LispObject[] params) {
    return params[0].asList().cdr().cdr();
  }

  public static LispObject caaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().car();
  }

  public static LispObject caadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().car();
  }

  public static LispObject cadar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().car();
  }

  public static LispObject caddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().car();
  }

  public static LispObject cdaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().cdr();
  }

  public static LispObject cdadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().cdr();
  }

  public static LispObject cddar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().cdr();
  }

  public static LispObject cdddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().cdr();
  }

  public static LispObject cadddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().cdr().car();
  }

  public static LispObject caaaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().car().asList().car();
  }

  public static LispObject caaadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().car().asList().car();
  }

  public static LispObject caadar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().car().asList().car();
  }

  public static LispObject caaddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().car().asList().car();
  }

  public static LispObject cadaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().cdr().car();
  }

  public static LispObject cadadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().cdr().car();
  }

  public static LispObject caddar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().cdr().car();
  }

  public static LispObject cdaaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().car().asList().cdr();
  }

  public static LispObject cdaadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().car().asList().cdr();
  }

  public static LispObject cdadar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().car().asList().cdr();
  }

  public static LispObject cdaddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().car().asList().cdr();
  }

  public static LispObject cddaar(LispObject[] params) {
    return params[0].asList().car().asList().car().asList().cdr().cdr();
  }

  public static LispObject cddadr(LispObject[] params) {
    return params[0].asList().cdr().car().asList().cdr().cdr();
  }

  public static LispObject cdddar(LispObject[] params) {
    return params[0].asList().car().asList().cdr().cdr().cdr();
  }

  public static LispObject cddddr(LispObject[] params) {
    return params[0].asList().cdr().cdr().cdr().cdr();
  }

  /** Returns the last pair in a list. (last-pair list) */
  public static LispObject lastPair(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      throw new LispArgumentError("last-pair: empty list");
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("last-pair requires a list");
    }

    while (list.cdr() != ListObject.NIL) {
      list = list.cdr();
    }
    return list;
  }

  /** Returns the last element of a list. (last list) */
  public static LispObject last(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      throw new LispArgumentError("last: empty list");
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("last requires a list");
    }

    while (list.cdr() != ListObject.NIL) {
      list = list.cdr();
    }
    return list.car();
  }

  /** Returns list without the last element. (butlast list) or (drop-right list 1) */
  public static LispObject butlast(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return ListObject.NIL;
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("butlast requires a list");
    }

    if (list.cdr() == ListObject.NIL) {
      return ListObject.NIL;
    }

    List<LispObject> result = new ArrayList<>();
    while (list.cdr() != ListObject.NIL) {
      result.add(list.car());
      list = list.cdr();
    }
    return ListObject.fromList(result.toArray(new LispObject[0]));
  }

  /** Takes first n elements of a list. (take list n) */
  public static LispObject take(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return ListObject.NIL;
    }
    ListObject list = params[0].asList();
    IntObject n = params[1].asInt();
    if (list == null) {
      throw new LispArgumentError("take requires a list as first argument");
    }
    if (n == null || n.value < 0) {
      throw new LispArgumentError("take requires a non-negative integer");
    }

    List<LispObject> result = new ArrayList<>();
    int count = n.value;
    for (LispObject elem : list) {
      if (count-- <= 0) break;
      result.add(elem);
    }
    return ListObject.fromList(result.toArray(new LispObject[0]));
  }

  /** Drops first n elements of a list. (drop list n) */
  public static LispObject drop(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return ListObject.NIL;
    }
    ListObject list = params[0].asList();
    IntObject n = params[1].asInt();
    if (list == null) {
      throw new LispArgumentError("drop requires a list as first argument");
    }
    if (n == null || n.value < 0) {
      throw new LispArgumentError("drop requires a non-negative integer");
    }

    int count = n.value;
    while (count-- > 0 && list != ListObject.NIL) {
      list = list.cdr();
    }
    return list;
  }

  /**
   * Creates a list of integers from start to end-1. (iota count) or (iota count start) or (iota
   * count start step)
   */
  public static LispObject iota(LispObject[] params) {
    IntObject countObj = params[0].asInt();
    if (countObj == null || countObj.value < 0) {
      throw new LispArgumentError("iota requires a non-negative integer count");
    }

    int count = countObj.value;
    int start = 0;
    int step = 1;

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("iota requires an integer start");
      }
      start = startObj.value;
    }

    if (params.length > 2) {
      IntObject stepObj = params[2].asInt();
      if (stepObj == null) {
        throw new LispArgumentError("iota requires an integer step");
      }
      step = stepObj.value;
    }

    LispObject[] result = new LispObject[count];
    for (int i = 0; i < count; i++) {
      result[i] = IntObject.valueOf(start + i * step);
    }
    return ListObject.fromList(result);
  }

  /** Creates a shallow copy of a list. (list-copy list) */
  public static LispObject listCopy(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("list-copy: expected 1 argument");
    }
    if (params[0] == ListObject.NIL) {
      return ListObject.NIL;
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list-copy: expected list");
    }
    List<LispObject> copy = new ArrayList<>();
    for (LispObject obj : list) {
      copy.add(obj);
    }
    return ListObject.fromList(copy.toArray(new LispObject[0]));
  }

  /** Mutates the car of a pair. (set-car! pair obj) */
  public static LispObject setCar(LispObject[] params) {
    ListObject pair = requirePair("set-car!", params[0], 1);
    pair.setHead(params[1]);
    return VoidObject.VOID;
  }

  /** Mutates the cdr of a pair. (set-cdr! pair obj) */
  public static LispObject setCdr(LispObject[] params) {
    ListObject pair = requirePair("set-cdr!", params[0], 1);
    pair.setTail(params[1]);
    return VoidObject.VOID;
  }

  /** Mutates an element at index k in a list. (list-set! list k obj) */
  public static LispObject listSet(LispObject[] params) {
    ListObject list = requireList("list-set!", params[0], 1);
    int k = requireNonNegativeInt("list-set!", params[1], 2);

    if (list == ListObject.NIL) {
      throw argOutOfRange("list-set!", 2, k);
    }

    ListObject current = list;
    for (int i = 0; i < k; i++) {
      if (current == ListObject.NIL) {
        throw argOutOfRange("list-set!", 2, k);
      }
      current = current.cdr();
    }

    if (current == ListObject.NIL) {
      throw argOutOfRange("list-set!", 2, k);
    }
    current.setHead(params[2]);
    return VoidObject.VOID;
  }

  /** Creates a list of k elements, all initialized to fill. (make-list k [fill]) */
  public static LispObject makeList(LispObject[] params) {
    int k = requireNonNegativeInt("make-list", params[0], 1);

    LispObject fill = ListObject.NIL;
    if (params.length > 1) {
      fill = params[1];
    }

    LispObject[] elements = new LispObject[k];
    for (int i = 0; i < k; i++) {
      elements[i] = fill;
    }
    return ListObject.fromList(elements);
  }
}
