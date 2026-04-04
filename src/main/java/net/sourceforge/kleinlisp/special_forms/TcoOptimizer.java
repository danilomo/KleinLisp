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
        break;
      case LET:
      case LET_STAR:
      case LETREC:
      case LETREC_STAR:
        optimizeLet(name, list);
        break;
      case BEGIN:
        optimizeBegin(name, list);
        break;
      case AND:
      case OR:
        optimizeAndOr(name, list);
        break;
      case COND:
        optimizeCond(name, list);
        break;
      case CASE:
        optimizeCase(name, list);
        break;
      default:
        break;
    }
  }

  private void optimizeIf(AtomObject name, ListObject list) {
    ListObject tail = list.cdr();

    LispObject cond = tail.car();
    LispObject trueForm = tail.cdr().car();
    ListObject elseTail = tail.cdr().cdr();
    LispObject elseForm = (elseTail != ListObject.NIL) ? elseTail.car() : null;

    optimizeTailCall(name, cond.asList());
    optimizeTailCall(name, trueForm.asList());
    if (elseForm != null) {
      optimizeTailCall(name, elseForm.asList());
    }
  }

  private void optimizeLet(AtomObject name, ListObject list) {
    // let form: (let ((var1 val1) ...) body...)
    ListObject tail = list.cdr();
    // tail.car() is the bindings list, tail.cdr() is the body
    ListObject body = tail.cdr();

    // Find the last expression in the body (which is in tail position)
    LispObject lastExpr = null;
    for (LispObject obj : body) {
      lastExpr = obj;
    }

    if (lastExpr != null) {
      optimizeTailCall(name, lastExpr.asList());
    }
  }

  private void optimizeBegin(AtomObject name, ListObject list) {
    // begin form: (begin expr1 expr2 ... last-expr)
    // Only the last expression is in tail position
    ListObject body = list.cdr();

    LispObject lastExpr = null;
    for (LispObject obj : body) {
      lastExpr = obj;
    }

    if (lastExpr != null) {
      optimizeTailCall(name, lastExpr.asList());
    }
  }

  private void optimizeAndOr(AtomObject name, ListObject list) {
    // and/or form: (and/or expr1 expr2 ... last-expr)
    // Only the last expression is in tail position
    ListObject body = list.cdr();

    LispObject lastExpr = null;
    for (LispObject obj : body) {
      lastExpr = obj;
    }

    if (lastExpr != null) {
      optimizeTailCall(name, lastExpr.asList());
    }
  }

  private void optimizeCond(AtomObject name, ListObject list) {
    // cond form: (cond (test1 expr1...) (test2 expr2...) ...)
    // Each clause's last expression is in tail position
    ListObject clauses = list.cdr();

    for (LispObject clause : clauses) {
      ListObject clauseList = clause.asList();
      if (clauseList == null || clauseList == ListObject.NIL) {
        continue;
      }

      // Get the expressions after the test (which may be => or regular expressions)
      ListObject exprs = clauseList.cdr();
      if (exprs == ListObject.NIL) {
        // Test-only clause, the test itself is in tail position
        optimizeTailCall(name, clauseList.car().asList());
      } else {
        // Find the last expression
        LispObject lastExpr = null;
        for (LispObject obj : exprs) {
          lastExpr = obj;
        }
        if (lastExpr != null) {
          optimizeTailCall(name, lastExpr.asList());
        }
      }
    }
  }

  private void optimizeCase(AtomObject name, ListObject list) {
    // case form: (case key ((datum...) expr...) ...)
    // Each clause's last expression is in tail position
    ListObject clauses = list.cdr().cdr(); // Skip 'case' and key

    for (LispObject clause : clauses) {
      ListObject clauseList = clause.asList();
      if (clauseList == null || clauseList == ListObject.NIL) {
        continue;
      }

      // Skip the datum list and get the expressions
      ListObject exprs = clauseList.cdr();

      // Find the last expression
      LispObject lastExpr = null;
      for (LispObject obj : exprs) {
        lastExpr = obj;
      }
      if (lastExpr != null) {
        optimizeTailCall(name, lastExpr.asList());
      }
    }
  }
}
