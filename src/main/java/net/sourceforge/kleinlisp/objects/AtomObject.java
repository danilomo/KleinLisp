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
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;

/**
 * @author daolivei
 */
public final class AtomObject implements LispObject {

  private final Environment env;
  private final String value;
  private SpecialFormEnum specialForm;

  AtomObject(Environment env, String value, SpecialFormEnum specialForm) {
    this.env = env;
    this.value = value;
    this.specialForm = specialForm;
  }

  public Environment environment() {
    return env;
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    // R7RS: Symbols with special characters (spaces, newlines, etc.) must be
    // enclosed in vertical bars |...|
    if (needsEscaping(value)) {
      return "|" + escapeSymbol(value) + "|";
    }
    return value;
  }

  /** Check if a symbol name needs to be escaped with |...| */
  private static boolean needsEscaping(String name) {
    if (name.isEmpty()) {
      return true;
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '|' || c == '\\' || c == '('
          || c == ')' || c == '[' || c == ']' || c == '"' || c == '\'' || c == '`' || c == ','
          || c == ';') {
        return true;
      }
    }
    return false;
  }

  /** Escape special characters within |...| syntax */
  private static String escapeSymbol(String name) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '|') {
        sb.append("\\|");
      } else if (c == '\\') {
        sb.append("\\\\");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return value();
  }

  @Override
  public AtomObject asAtom() {
    return this;
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public SpecialFormEnum specialForm() {
    return specialForm;
  }
}
