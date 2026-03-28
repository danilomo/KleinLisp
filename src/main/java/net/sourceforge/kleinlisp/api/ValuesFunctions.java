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

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ValuesObject;

/** R7RS multiple values support functions: values and call-with-values. */
public class ValuesFunctions {

  /**
   * (values obj ...) - returns multiple values.
   *
   * <p>Single value case returns the value directly without wrapping.
   */
  public static LispObject values(LispObject[] args) {
    if (args.length == 1) {
      return args[0]; // Single value doesn't need wrapping
    }
    return new ValuesObject(args);
  }

  /**
   * (call-with-values producer consumer)
   *
   * <p>Calls producer with no arguments, then passes the produced values to consumer.
   */
  public static LispObject callWithValues(LispObject[] args) {
    assertArgCount("call-with-values", args, 2);

    FunctionObject producerObj = args[0].asFunction();
    FunctionObject consumerObj = args[1].asFunction();

    if (producerObj == null) {
      throw new LispArgumentError("call-with-values: producer must be a procedure");
    }
    if (consumerObj == null) {
      throw new LispArgumentError("call-with-values: consumer must be a procedure");
    }

    Function producer = producerObj.function();
    Function consumer = consumerObj.function();

    // Call producer with no arguments
    LispObject produced = producer.evaluate(new LispObject[0]);

    // If producer returned multiple values, spread them to consumer
    if (produced instanceof ValuesObject) {
      ValuesObject values = (ValuesObject) produced;
      return consumer.evaluate(values.getValues());
    } else {
      // Single value case
      return consumer.evaluate(new LispObject[] {produced});
    }
  }

  /**
   * (values? obj) - not standard R7RS but useful for testing.
   *
   * <p>Returns true if obj is a multiple values object.
   */
  public static LispObject isValues(LispObject[] args) {
    assertArgCount("values?", args, 1);
    return args[0] instanceof ValuesObject ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " arguments, got " + args.length);
    }
  }
}
