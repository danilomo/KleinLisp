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

import net.sourceforge.kleinlisp.LispObject;

/**
 * Thread-local pool of LispObject arrays, bucketed by size. Uses a stack-based allocation strategy
 * that matches function call/return patterns.
 *
 * <p>This pool eliminates array allocation overhead for function calls by reusing arrays. Each size
 * bucket maintains a stack of pre-allocated arrays. The pool depth handles recursive calls - each
 * recursion level gets its own array from the stack.
 */
public class ParameterArrayPool {

  // Max size to pool (larger arrays are allocated normally)
  private static final int MAX_POOLED_SIZE = 8;

  // Depth of pool per size (handles recursive calls)
  private static final int POOL_DEPTH = 128;

  // Specialized pools for common sizes (1, 2, 3)
  private final LispObject[][] pool1;
  private final LispObject[][] pool2;
  private final LispObject[][] pool3;
  private int pointer1 = 0;
  private int pointer2 = 0;
  private int pointer3 = 0;

  // General pools for sizes 4-8
  private final LispObject[][][] pools;
  private final int[] poolPointers;

  private static final ThreadLocal<ParameterArrayPool> INSTANCE =
      ThreadLocal.withInitial(ParameterArrayPool::new);

  public static ParameterArrayPool get() {
    return INSTANCE.get();
  }

  private ParameterArrayPool() {
    // Initialize specialized pools for sizes 1-3
    pool1 = new LispObject[POOL_DEPTH][];
    pool2 = new LispObject[POOL_DEPTH][];
    pool3 = new LispObject[POOL_DEPTH][];

    for (int i = 0; i < POOL_DEPTH; i++) {
      pool1[i] = new LispObject[1];
      pool2[i] = new LispObject[2];
      pool3[i] = new LispObject[3];
    }

    // Initialize general pools for sizes 4-8
    pools = new LispObject[MAX_POOLED_SIZE + 1][][];
    poolPointers = new int[MAX_POOLED_SIZE + 1];

    for (int size = 4; size <= MAX_POOLED_SIZE; size++) {
      pools[size] = new LispObject[POOL_DEPTH][];
      poolPointers[size] = 0;

      for (int depth = 0; depth < POOL_DEPTH; depth++) {
        pools[size][depth] = new LispObject[size];
      }
    }
  }

  /** Fast path for single-parameter functions. */
  public LispObject[] acquire1() {
    if (pointer1 < POOL_DEPTH) {
      return pool1[pointer1++];
    }
    return new LispObject[1];
  }

  /** Release a size-1 array back to the pool. */
  public void release1() {
    if (pointer1 > 0) {
      pool1[--pointer1][0] = null;
    }
  }

  /** Fast path for two-parameter functions. */
  public LispObject[] acquire2() {
    if (pointer2 < POOL_DEPTH) {
      return pool2[pointer2++];
    }
    return new LispObject[2];
  }

  /** Release a size-2 array back to the pool. */
  public void release2() {
    if (pointer2 > 0) {
      LispObject[] arr = pool2[--pointer2];
      arr[0] = null;
      arr[1] = null;
    }
  }

  /** Fast path for three-parameter functions. */
  public LispObject[] acquire3() {
    if (pointer3 < POOL_DEPTH) {
      return pool3[pointer3++];
    }
    return new LispObject[3];
  }

  /** Release a size-3 array back to the pool. */
  public void release3() {
    if (pointer3 > 0) {
      LispObject[] arr = pool3[--pointer3];
      arr[0] = null;
      arr[1] = null;
      arr[2] = null;
    }
  }

  /**
   * Acquire an array of the given size. Returns pooled array if available, otherwise allocates.
   */
  public LispObject[] acquire(int size) {
    if (size == 0) return new LispObject[0];
    if (size == 1) return acquire1();
    if (size == 2) return acquire2();
    if (size == 3) return acquire3();

    if (size > MAX_POOLED_SIZE) {
      return new LispObject[size];
    }

    int pointer = poolPointers[size];
    if (pointer < POOL_DEPTH) {
      poolPointers[size] = pointer + 1;
      return pools[size][pointer];
    }

    return new LispObject[size];
  }

  /** Release an array back to the pool. MUST be called in reverse order of acquire. */
  public void release(int size) {
    if (size <= 0) {
      return;
    }
    if (size == 1) {
      release1();
      return;
    }
    if (size == 2) {
      release2();
      return;
    }
    if (size == 3) {
      release3();
      return;
    }

    if (size > MAX_POOLED_SIZE) {
      return;
    }

    int pointer = poolPointers[size];
    if (pointer > 0) {
      poolPointers[size] = pointer - 1;
      LispObject[] arr = pools[size][pointer - 1];
      for (int i = 0; i < size; i++) {
        arr[i] = null;
      }
    }
  }
}
