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

import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author Danilo Oliveira
 */
public class TcoOptimizer {

  private final LispObject root;
  private final AtomObject name;
  private final LispEnvironment env;

  public TcoOptimizer(LispObject root, AtomObject name, LispEnvironment env) {
    this.root = root;
    this.name = name;
    this.env = env;
  }

  public void optimize() {
    optimizeTailCalls(name, root);
  }

  private void optimizeTailCalls(AtomObject name, LispObject obj) {
    if (obj.asList() == null) {
      return;
    }

    ListObject list = obj.asList();

    if (list == ListObject.NIL) {
      return;
    }

    LispObject car = list.car();
    LispObject cdr = list.cdr();

    if (car.asList() == null) {
      optimizeTailCalls(name, cdr);
      return;
    }

    if (cdr != ListObject.NIL) {
      optimizeTailCalls(name, cdr);
      return;
    }

    ListObject carList = car.asList();
    optimizeTailCall(name, carList);
  }

  private void optimizeTailCall(AtomObject name, ListObject list) {
    if (list == null) {
      return;
    }

    AtomObject head = list.car().asAtom();

    if (head == null) {
      return;
    }

    if (head == name) {
      LispObject recur = new ListObject(env.atomOf("recur"), new ListObject(name));
      list.setHead(recur);
      return;
    }

    SpecialFormEnum specialForm = head.specialForm();

    if (specialForm == null) {
      return;
    }

    switch (specialForm) {
      case IF:
        optimizeIf(name, list);
      default:
        break;
    }
  }

  private void optimizeIf(AtomObject name, ListObject list) {
    ListObject tail = list.cdr();

    LispObject cond = tail.car();
    LispObject trueForm = tail.cdr().car();
    LispObject elseForm = tail.cdr().cdr().car();

    optimizeTailCall(name, cond.asList());
    optimizeTailCall(name, trueForm.asList());
    optimizeTailCall(name, elseForm.asList());
  }
}
