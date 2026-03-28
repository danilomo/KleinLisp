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
import net.sourceforge.kleinlisp.api.BooleanFunctions;
import net.sourceforge.kleinlisp.evaluator.TypeAnalyzer.ExpressionType;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Fast paths for common operations. Includes constant folding for literal integers and specialized
 * unchecked operations when types are statically known.
 *
 * @author danilo
 */
public class CommonCases {

  static Supplier<LispObject> apply(ListObject list, Evaluator evaluator) {
    AtomObject head = list.car().asAtom();
    if (head == null) {
      return null;
    }

    int length = list.length();

    switch (head.toString()) {
        // Binary arithmetic operations (length == 3)
      case "+":
        return length == 3 ? plus(list.cdr(), evaluator) : null;
      case "-":
        return length == 3 ? minus(list.cdr(), evaluator) : null;
      case "*":
        return length == 3 ? multiply(list.cdr(), evaluator) : null;
      case "/":
        return length == 3 ? divide(list.cdr(), evaluator) : null;
      case "mod":
        return length == 3 ? modulo(list.cdr(), evaluator) : null;

        // Binary comparison operations (length == 3)
      case "<":
        return length == 3 ? lessThan(list.cdr(), evaluator) : null;
      case "<=":
        return length == 3 ? lessThanOrEqual(list.cdr(), evaluator) : null;
      case ">":
        return length == 3 ? greaterThan(list.cdr(), evaluator) : null;
      case ">=":
        return length == 3 ? greaterThanOrEqual(list.cdr(), evaluator) : null;
      case "=":
        return length == 3 ? equals(list.cdr(), evaluator) : null;

        // Binary list operations (length == 3)
      case "cons":
        return length == 3 ? cons(list.cdr(), evaluator) : null;

        // Unary operations (length == 2)
      case "car":
        return length == 2 ? car(list.cdr(), evaluator) : null;
      case "cdr":
        return length == 2 ? cdr(list.cdr(), evaluator) : null;
      case "null?":
        return length == 2 ? isNull(list.cdr(), evaluator) : null;
      case "length":
        return length == 2 ? listLength(list.cdr(), evaluator) : null;

        // Variadic operations
      case "list":
        return listOf(list.cdr(), evaluator);

      default:
        return null;
    }
  }

  // ============ Constant folding helpers ============

  /** Try constant folding for addition. Returns null if not applicable. */
  private static Supplier<LispObject> tryConstantFoldPlus(LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      IntObject result = IntObject.valueOf(leftLit.value + rightLit.value);
      return () -> result;
    }
    return null;
  }

  /** Try constant folding for subtraction. */
  private static Supplier<LispObject> tryConstantFoldMinus(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      IntObject result = IntObject.valueOf(leftLit.value - rightLit.value);
      return () -> result;
    }
    return null;
  }

  /** Try constant folding for multiplication. */
  private static Supplier<LispObject> tryConstantFoldMultiply(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      IntObject result = IntObject.valueOf(leftLit.value * rightLit.value);
      return () -> result;
    }
    return null;
  }

  /** Try constant folding for division. */
  private static Supplier<LispObject> tryConstantFoldDivide(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null && rightLit.value != 0) {
      IntObject result = IntObject.valueOf(leftLit.value / rightLit.value);
      return () -> result;
    }
    return null;
  }

  /** Try constant folding for modulo. */
  private static Supplier<LispObject> tryConstantFoldModulo(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null && rightLit.value != 0) {
      IntObject result = IntObject.valueOf(leftLit.value % rightLit.value);
      return () -> result;
    }
    return null;
  }

  /** Try constant folding for comparison. */
  private static Supplier<LispObject> tryConstantFoldLessThan(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      BooleanObject result =
          leftLit.value < rightLit.value ? BooleanObject.TRUE : BooleanObject.FALSE;
      return () -> result;
    }
    return null;
  }

  private static Supplier<LispObject> tryConstantFoldLessThanOrEqual(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      BooleanObject result =
          leftLit.value <= rightLit.value ? BooleanObject.TRUE : BooleanObject.FALSE;
      return () -> result;
    }
    return null;
  }

  private static Supplier<LispObject> tryConstantFoldGreaterThan(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      BooleanObject result =
          leftLit.value > rightLit.value ? BooleanObject.TRUE : BooleanObject.FALSE;
      return () -> result;
    }
    return null;
  }

  private static Supplier<LispObject> tryConstantFoldGreaterThanOrEqual(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      BooleanObject result =
          leftLit.value >= rightLit.value ? BooleanObject.TRUE : BooleanObject.FALSE;
      return () -> result;
    }
    return null;
  }

  private static Supplier<LispObject> tryConstantFoldEquals(
      LispObject leftObj, LispObject rightObj) {
    IntObject leftLit = leftObj.asInt();
    IntObject rightLit = rightObj.asInt();
    if (leftLit != null && rightLit != null) {
      BooleanObject result =
          leftLit.value == rightLit.value ? BooleanObject.TRUE : BooleanObject.FALSE;
      return () -> result;
    }
    return null;
  }

  // ============ Unchecked operations (when types are known) ============

  private static Supplier<LispObject> plusIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () -> IntObject.valueOf(left.get().asInt().value + right.get().asInt().value);
  }

  private static Supplier<LispObject> minusIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () -> IntObject.valueOf(left.get().asInt().value - right.get().asInt().value);
  }

  private static Supplier<LispObject> multiplyIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () -> IntObject.valueOf(left.get().asInt().value * right.get().asInt().value);
  }

  private static Supplier<LispObject> divideIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () -> IntObject.valueOf(left.get().asInt().value / right.get().asInt().value);
  }

  private static Supplier<LispObject> moduloIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () -> IntObject.valueOf(left.get().asInt().value % right.get().asInt().value);
  }

  private static Supplier<LispObject> lessThanIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () ->
        left.get().asInt().value < right.get().asInt().value
            ? BooleanObject.TRUE
            : BooleanObject.FALSE;
  }

  private static Supplier<LispObject> lessThanOrEqualIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () ->
        left.get().asInt().value <= right.get().asInt().value
            ? BooleanObject.TRUE
            : BooleanObject.FALSE;
  }

  private static Supplier<LispObject> greaterThanIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () ->
        left.get().asInt().value > right.get().asInt().value
            ? BooleanObject.TRUE
            : BooleanObject.FALSE;
  }

  private static Supplier<LispObject> greaterThanOrEqualIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () ->
        left.get().asInt().value >= right.get().asInt().value
            ? BooleanObject.TRUE
            : BooleanObject.FALSE;
  }

  private static Supplier<LispObject> equalsIntUnchecked(
      Supplier<LispObject> left, Supplier<LispObject> right) {
    return () ->
        left.get().asInt().value == right.get().asInt().value
            ? BooleanObject.TRUE
            : BooleanObject.FALSE;
  }

  // ============ Arithmetic operations with optimization ============

  private static Supplier<LispObject> plus(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldPlus(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return plusIntUnchecked(left, right);
    }

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
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldMinus(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return minusIntUnchecked(left, right);
    }

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to - function");
      }
      return IntObject.valueOf(leftInt.value - rightInt.value);
    };
  }

  private static Supplier<LispObject> multiply(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldMultiply(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return multiplyIntUnchecked(left, right);
    }

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to * function");
      }
      return IntObject.valueOf(leftInt.value * rightInt.value);
    };
  }

  private static Supplier<LispObject> divide(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldDivide(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return divideIntUnchecked(left, right);
    }

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to / function");
      }
      return IntObject.valueOf(leftInt.value / rightInt.value);
    };
  }

  private static Supplier<LispObject> modulo(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldModulo(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return moduloIntUnchecked(left, right);
    }

    return () -> {
      IntObject leftInt = left.get().asInt();
      IntObject rightInt = right.get().asInt();
      if (leftInt == null || rightInt == null) {
        throw new LispArgumentError("Wrong argument type passed to mod function");
      }
      return IntObject.valueOf(leftInt.value % rightInt.value);
    };
  }

  // ============ Comparison operations with optimization ============

  private static Supplier<LispObject> lessThan(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldLessThan(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return lessThanIntUnchecked(left, right);
    }

    // Delegate to BooleanFunctions for full numeric/JavaObject support
    return () -> BooleanFunctions.lt(new LispObject[] {left.get(), right.get()});
  }

  private static Supplier<LispObject> lessThanOrEqual(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldLessThanOrEqual(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return lessThanOrEqualIntUnchecked(left, right);
    }

    // Delegate to BooleanFunctions for full numeric/JavaObject support
    return () -> BooleanFunctions.le(new LispObject[] {left.get(), right.get()});
  }

  private static Supplier<LispObject> greaterThan(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldGreaterThan(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return greaterThanIntUnchecked(left, right);
    }

    // Delegate to BooleanFunctions for full numeric/JavaObject support
    return () -> BooleanFunctions.gt(new LispObject[] {left.get(), right.get()});
  }

  private static Supplier<LispObject> greaterThanOrEqual(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldGreaterThanOrEqual(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return greaterThanOrEqualIntUnchecked(left, right);
    }

    // Delegate to BooleanFunctions for full numeric/JavaObject support
    return () -> BooleanFunctions.ge(new LispObject[] {left.get(), right.get()});
  }

  private static Supplier<LispObject> equals(ListObject list, Evaluator evaluator) {
    LispObject leftObj = list.car();
    LispObject rightObj = list.cdr().car();

    // Try constant folding first
    Supplier<LispObject> folded = tryConstantFoldEquals(leftObj, rightObj);
    if (folded != null) {
      return folded;
    }

    Supplier<LispObject> left = leftObj.accept(evaluator);
    Supplier<LispObject> right = rightObj.accept(evaluator);

    // Use unchecked if types are known
    ExpressionType leftType = TypeAnalyzer.analyze(leftObj);
    ExpressionType rightType = TypeAnalyzer.analyze(rightObj);
    if (leftType == ExpressionType.INT && rightType == ExpressionType.INT) {
      return equalsIntUnchecked(left, right);
    }

    // Delegate to BooleanFunctions for full numeric/JavaObject support
    return () -> BooleanFunctions.eq(new LispObject[] {left.get(), right.get()});
  }

  // ============ List operations ============

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

  private static Supplier<LispObject> cons(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> head = list.car().accept(evaluator);
    Supplier<LispObject> tail = list.cdr().car().accept(evaluator);

    return () -> new ListObject(head.get(), tail.get());
  }

  private static Supplier<LispObject> listLength(ListObject list, Evaluator evaluator) {
    Supplier<LispObject> arg = list.car().accept(evaluator);

    return () -> {
      ListObject lst = arg.get().asList();
      if (lst == null) {
        throw new LispArgumentError("Wrong argument type passed to length function");
      }
      return IntObject.valueOf(lst.length());
    };
  }

  @SuppressWarnings("unchecked")
  private static Supplier<LispObject> listOf(ListObject list, Evaluator evaluator) {
    // Handle empty list
    if (list == ListObject.NIL) {
      return () -> ListObject.NIL;
    }

    // Collect all argument suppliers
    int size = list.length();

    // Optimize for common cases
    if (size == 1) {
      Supplier<LispObject> s0 = list.car().accept(evaluator);
      return () -> new ListObject(s0.get(), ListObject.NIL);
    }

    if (size == 2) {
      Supplier<LispObject> s0 = list.car().accept(evaluator);
      Supplier<LispObject> s1 = list.cdr().car().accept(evaluator);
      return () -> new ListObject(s0.get(), new ListObject(s1.get(), ListObject.NIL));
    }

    // General case: build list at evaluation time
    Supplier<LispObject>[] arr = new Supplier[size];
    int i = 0;
    for (LispObject obj : list) {
      arr[i++] = obj.accept(evaluator);
    }

    return () -> {
      LispObject[] evaluated = new LispObject[arr.length];
      for (int j = 0; j < arr.length; j++) {
        evaluated[j] = arr[j].get();
      }
      return ListObject.fromList(evaluated);
    };
  }
}
