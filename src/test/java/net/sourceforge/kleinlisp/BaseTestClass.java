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
package net.sourceforge.kleinlisp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.sourceforge.kleinlisp.objects.AtomObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseTestClass {

  protected Lisp lisp;
  private ByteArrayOutputStream redirectedOut;
  private PrintStream originalOut;

  @BeforeEach
  public void setup() {
    lisp = new Lisp();
    originalOut = System.out;
    redirectedOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(redirectedOut));
  }

  @AfterEach
  public void tearDown() {
    disableStdoutCapture();
  }

  protected int evalAsInt(String str) {
    return lisp.evaluate(str).asInt().value;
  }

  protected AtomObject evalAsAtom(String str) {
    LispObject result = lisp.evaluate(str);
    return result.asAtom();
  }

  protected String getStdOut() {
    return new String(redirectedOut.toByteArray());
  }

  protected void disableStdoutCapture() {
    System.setOut(originalOut);
  }

  protected void debug(Object value) {
    originalOut.println("DEBUG: " + value);
  }
}
