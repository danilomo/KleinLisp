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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * Inline cache for function lookups. Caches the resolved LispObject and validates on each call by
 * checking if the environment definition version changed.
 *
 * <p>This optimization benefits repeated calls to the same function (common in loops) by avoiding
 * HashMap lookups after the first call.
 */
public class CachedFunctionSupplier implements Supplier<LispObject> {

  private final LispEnvironment environment;
  private final AtomObject atom;

  // Cache state
  private LispObject cachedValue;
  private long cacheVersion;

  public CachedFunctionSupplier(LispEnvironment environment, AtomObject atom) {
    this.environment = environment;
    this.atom = atom;
    this.cachedValue = null;
    this.cacheVersion = -1;
  }

  @Override
  public LispObject get() {
    // Fast path: if inside a let environment, check it first (let bindings are dynamic)
    // hasLetEnv() is O(1) - just checks if list is empty
    if (environment.hasLetEnv()) {
      LispObject letValue = environment.lookupInLetEnvStack(atom);
      if (letValue != null) {
        return letValue;
      }
    }

    // For global variables, use inline caching
    long currentVersion = environment.getDefinitionVersion(atom);

    if (cachedValue != null && currentVersion == cacheVersion) {
      return cachedValue;
    }

    // Cache miss - resolve and cache
    LispObject value = environment.lookupValue(atom);
    cachedValue = value;
    cacheVersion = currentVersion;

    return value;
  }
}
