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

import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danilo
 */
public class ConsListTest extends BaseTestClass {

  @Test
  public void testLast() {
    disableStdoutCapture();

    ListObject list = evalAsList("'(1 2 3 4 5 6)");
    ListObject last = list.last();

    Assertions.assertEquals(6, last.car().asInt().value);
  }

  @Test
  public void testConsPair() {
    disableStdoutCapture();

    ListObject list = evalAsList("'(1 . \"2\")");
    LispObject head = list.head();
    LispObject tail = list.tail();

    Assertions.assertEquals(1, head.asInt().value);
    Assertions.assertEquals("2", tail.asString().value());
  }

  @Test
  public void testWellFormedList() {
    disableStdoutCapture();

    ListObject list = evalAsList("'(1 2 3 4 5 6)");

    Assertions.assertEquals("(1 2 3 4 5 6)", list.toString());
  }

  @Test
  public void testMalformedList() {
    disableStdoutCapture();

    ListObject malformed = evalAsList("'(1 2 3 4 . 5)");

    Assertions.assertEquals("(1 2 3 4 . 5)", malformed.toString());
  }
}
