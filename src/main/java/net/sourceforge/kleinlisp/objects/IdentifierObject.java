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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * @author danilo
 */
public class IdentifierObject implements LispObject {

  private final AtomObject atom;
  private final String source;
  private final int line;
  private final int col;

  public IdentifierObject(AtomObject atom, String source, int line, int col) {
    this.atom = atom;
    this.source = source;
    this.line = line;
    this.col = col;
  }

  public int getLine() {
    return line;
  }

  public int getCol() {
    return col;
  }

  public String getSource() {
    return source;
  }

  @Override
  public Object asObject() {
    return atom.asObject();
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public AtomObject asAtom() {
    return atom;
  }

  @Override
  public IdentifierObject asIdentifier() {
    return this;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return atom.toString() + "[" + line + ", " + col + "]";
  }
}
