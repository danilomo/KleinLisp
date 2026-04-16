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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;

/**
 * R7RS (scheme time) library functions.
 *
 * <p>Provides timing functions for measuring time and durations.
 */
public class TimeFunctions {

  /**
   * Returns an inexact number representing the current time on the International Atomic Time (TAI)
   * scale. The value 0.0 represents midnight on January 1, 1970 TAI (equivalent to ten seconds
   * before midnight Universal Coordinated Time (UTC)).
   *
   * <p>Implementation uses Unix timestamp (seconds since epoch).
   *
   * @param params no parameters expected
   * @return current time in seconds as a DoubleObject
   */
  public static LispObject currentSecond(LispObject[] params) {
    return new DoubleObject(System.currentTimeMillis() / 1000.0);
  }

  /**
   * Returns the number of jiffies (platform-dependent clock ticks) since some arbitrary, fixed
   * point in time. The epoch may differ each time the program is run.
   *
   * <p>Implementation uses System.nanoTime() which provides nanosecond precision. Note: Returns a
   * DoubleObject since KleinLisp lacks BigInteger support and nanoTime values exceed int range.
   *
   * @param params no parameters expected
   * @return current jiffy count as a DoubleObject
   */
  public static LispObject currentJiffy(LispObject[] params) {
    return new DoubleObject((double) System.nanoTime());
  }

  /**
   * Returns an exact integer representing the number of jiffies per second.
   *
   * <p>Since we use System.nanoTime(), this returns 1,000,000,000 (one billion jiffies per second).
   *
   * @param params no parameters expected
   * @return 1000000000 (nanoseconds per second) as an IntObject
   */
  public static LispObject jiffiesPerSecond(LispObject[] params) {
    return new IntObject(1000000000);
  }
}
