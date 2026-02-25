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
package net.sourceforge.kleinlisp.evaluator;

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author danilo
 */
public class CommonCases {

  static Supplier<LispObject> apply(ListObject list, Evaluator evaluator) {
    AtomObject head = list.car().asAtom();
    if (head == null) {
      return null;
    }

    if (list.length() != 3) {
      return null;
    }

    switch (head.toString()) {
      case "+":
        return plus(list.cdr(), evaluator);
      case "-":
        return minus(list.cdr(), evaluator);
      case "<":
        return lessthan(list.cdr(), evaluator);
      case "<=":
        return lessthanOrEqual(list.cdr(), evaluator);
      case ">":
        return greaterThan(list.cdr(), evaluator);
      case ">=":
        return greaterThanOrEqual(list.cdr(), evaluator);
      case "car":
        if (list.length() == 2) {
          return car(list.cdr(), evaluator);
        }
        break;
      case "cdr":
        if (list.length() == 2) {
          return cdr(list.cdr(), evaluator);
        }
        break;
      case "null?":
        if (list.length() == 2) {
          return isNull(list.cdr(), evaluator);
        }
        break;
    }

    return null;
  }

  private static Supplier<LispObject> plus(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to + function");
      }
      return IntObject.valueOf(leftInt.value + rightInt.value);
    };
  }

  private static Supplier<LispObject> minus(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to - function");
      }
      return IntObject.valueOf(leftInt.value - rightInt.value);
    };
  }

  private static Supplier<LispObject> lessthan(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to < function");
      }
      return leftInt.value < rightInt.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    };
  }

  private static Supplier<LispObject> lessthanOrEqual(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to <= function");
      }
      return leftInt.value <= rightInt.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    };
  }

  private static Supplier<LispObject> greaterThan(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to > function");
      }
      return leftInt.value > rightInt.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    };
  }

  private static Supplier<LispObject> greaterThanOrEqual(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> left = list.car().accept(evaluator);
    Supplier<LispObject> right = list.cdr().car().accept(evaluator);

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to >= function");
      }
      return leftInt.value >= rightInt.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    };
  }

  private static Supplier<LispObject> car(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> arg = list.car().accept(evaluator);
    return () -> arg.get().asList().head();
  }

  private static Supplier<LispObject> cdr(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> arg = list.car().accept(evaluator);
    return () -> arg.get().asList().tail();
  }

  private static Supplier<LispObject> isNull(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> arg = list.car().accept(evaluator);
    return () -> {
      LispObject val = arg.get();
      return (val == null || val == ListObject.NIL) ? BooleanObject.TRUE : BooleanObject.FALSE;
    };
  }
}
