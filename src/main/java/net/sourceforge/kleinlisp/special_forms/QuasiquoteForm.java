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
package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.CellObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements quasiquote (`) special form with support for unquote (,) and unquote-splicing (,@).
 *
 * @author danilo
 */
public class QuasiquoteForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;
  private final AtomObject unquoteAtom;
  private final AtomObject quasiquoteAtom;
  private final AtomObject unquoteSplicingAtom;

  public QuasiquoteForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
    this.unquoteAtom = environment.atomOf("unquote");
    this.quasiquoteAtom = environment.atomOf("quasiquote");
    this.unquoteSplicingAtom = environment.atomOf("unquote-splicing");
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject list = t.asList();
    if (list.cdr() == ListObject.NIL) {
      throw new IllegalArgumentException("quasiquote requires an argument");
    }
    LispObject arg = list.cdr().car();
    return () -> expand(arg, 1);
  }

  private LispObject expand(LispObject obj, int depth) {
    if (obj instanceof ListObject) {
      ListObject list = (ListObject) obj;
      if (list == ListObject.NIL) {
        return list;
      }

      LispObject car = list.car();

      // Check for unquote
      if (isAtom(car, "unquote")) {
        if (depth == 1) {
          // Evaluate the unquoted expression
          ListObject rest = list.cdr();
          if (rest == ListObject.NIL) {
            throw new IllegalArgumentException("unquote requires an argument");
          }
          return unwrapCell(evaluator.evaluate(rest.car()));
        } else {
          // Nested quasiquote - decrease depth
          return new ListObject(
              unquoteAtom, new ListObject(expand(list.cdr().car(), depth - 1), ListObject.NIL));
        }
      }

      // Check for quasiquote (nested)
      if (isAtom(car, "quasiquote")) {
        return new ListObject(
            quasiquoteAtom, new ListObject(expand(list.cdr().car(), depth + 1), ListObject.NIL));
      }

      // Process list elements, handling unquote-splicing
      List<LispObject> elements = new ArrayList<>();
      ListObject current = list;

      while (current != ListObject.NIL) {
        LispObject elem = current.car();

        // Check for unquote-splicing
        if (elem instanceof ListObject) {
          ListObject elemList = (ListObject) elem;
          if (elemList != ListObject.NIL && isAtom(elemList.car(), "unquote-splicing")) {
            if (depth == 1) {
              // Splice the result into the current list
              ListObject splicedRest = elemList.cdr();
              if (splicedRest == ListObject.NIL) {
                throw new IllegalArgumentException("unquote-splicing requires an argument");
              }
              LispObject spliced = unwrapCell(evaluator.evaluate(splicedRest.car()));
              if (!(spliced instanceof ListObject)) {
                throw new IllegalArgumentException(
                    "unquote-splicing requires a list, got: " + spliced);
              }
              ListObject splicedList = (ListObject) spliced;
              for (LispObject splicedElem : splicedList) {
                elements.add(splicedElem);
              }
              current = current.cdr();
              continue;
            } else {
              // Nested quasiquote - decrease depth for unquote-splicing
              elements.add(
                  new ListObject(
                      unquoteSplicingAtom,
                      new ListObject(expand(elemList.cdr().car(), depth - 1), ListObject.NIL)));
              current = current.cdr();
              continue;
            }
          }
        }

        // Recursively expand the element
        elements.add(expand(elem, depth));
        current = current.cdr();
      }

      return ListObject.fromList(elements);
    }

    // Non-list objects are returned as-is
    return obj;
  }

  private boolean isAtom(LispObject obj, String name) {
    AtomObject atom = obj.asAtom();
    if (atom != null) {
      return atom.value().equals(name);
    }
    return false;
  }

  private LispObject unwrapCell(LispObject obj) {
    if (obj instanceof CellObject) {
      return ((CellObject) obj).get();
    }
    return obj;
  }
}
