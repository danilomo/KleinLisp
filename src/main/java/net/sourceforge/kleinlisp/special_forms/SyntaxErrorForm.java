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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispRuntimeException;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 * Implements R7RS syntax-error for signaling errors during macro expansion.
 *
 * <p>Syntax: (syntax-error message irritant ...)
 *
 * <p>When evaluated, throws a LispRuntimeException with the given message and optional irritants
 * (additional objects that are included in the error message).
 *
 * @author Danilo Oliveira
 */
public class SyntaxErrorForm implements SpecialForm {

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject form = obj.asList();
    FormErrors.assertMinArgs("syntax-error", form, 1);

    ListObject args = form.cdr();
    LispObject messageObj = args.car();

    // Build error message
    StringBuilder message = new StringBuilder("syntax-error: ");

    // Get the message string
    StringObject msgStr = messageObj.asString();
    if (msgStr != null) {
      message.append(msgStr.value());
    } else {
      message.append(messageObj.toString());
    }

    // Append any irritants
    ListObject irritants = args.cdr();
    if (irritants != ListObject.NIL) {
      message.append(" ");
      boolean first = true;
      for (LispObject irritant : irritants) {
        if (!first) {
          message.append(" ");
        }
        message.append(irritant.toString());
        first = false;
      }
    }

    // Throw immediately - syntax-error is always an error
    throw new LispRuntimeException(message.toString());
  }
}
