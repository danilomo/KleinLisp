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

import java.util.concurrent.atomic.AtomicLong;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/** Symbol manipulation functions for KleinLisp. */
public class SymbolFunctions {

  private static final AtomicLong gensymCounter = new AtomicLong(0);

  private final LispEnvironment environment;

  public SymbolFunctions(LispEnvironment environment) {
    this.environment = environment;
  }

  /** Converts a symbol to a string. (symbol->string sym) */
  public LispObject symbolToString(LispObject[] params) {
    AtomObject atom = params[0].asAtom();
    if (atom == null) {
      throw new LispArgumentError("symbol->string requires a symbol");
    }
    String name = environment.valueOf(atom);
    if (name == null) {
      // Fallback: try to get from atom's toString
      name = atom.toString();
    }
    return new StringObject(name);
  }

  /** Converts a string to a symbol. (string->symbol s) */
  public LispObject stringToSymbol(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string->symbol requires a string");
    }
    return environment.atomOf(str.value());
  }

  /** Generates a unique symbol. (gensym) or (gensym prefix) */
  public LispObject gensym(LispObject[] params) {
    String prefix = "g";
    if (params.length > 0) {
      StringObject prefixStr = params[0].asString();
      if (prefixStr != null) {
        prefix = prefixStr.value();
      }
    }
    long id = gensymCounter.incrementAndGet();
    return environment.atomOf(prefix + id);
  }
}
