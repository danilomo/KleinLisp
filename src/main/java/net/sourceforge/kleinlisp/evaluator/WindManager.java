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

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kleinlisp.LispObject;

/**
 * Thread-local dynamic-wind extent (the "current dynamic point").
 *
 * <p>The extent is a singly-linked list of {@link WindFrame} objects. {@code dynamic-wind} pushes
 * and pops frames around thunk execution. When a continuation is invoked, {@link #windTo} diffs the
 * current extent against the captured extent, running {@code after} thunks for exited frames (from
 * innermost out to the common ancestor) and {@code before} thunks for entered frames (from the
 * common ancestor in to the target), in that order.
 *
 * <p>This implements the R7RS §6.10 algorithm for {@code dynamic-wind}.
 */
public final class WindManager {

  private WindManager() {}

  /** The current dynamic-wind extent for this thread. null == at the top level. */
  public static final ThreadLocal<WindFrame> CURRENT = ThreadLocal.withInitial(() -> null);

  /** Snapshot the current extent (called by call/cc at capture time). */
  public static WindFrame snapshot() {
    return CURRENT.get();
  }

  /** Enter a wind frame (called by dynamic-wind before running the thunk). */
  public static void push(WindFrame frame) {
    CURRENT.set(frame);
  }

  /** Exit a wind frame (called by dynamic-wind after the thunk finishes or exits). */
  public static void pop(WindFrame parent) {
    CURRENT.set(parent);
  }

  /**
   * Transition from the current dynamic extent to {@code target}.
   *
   * <p>Runs {@code after} thunks for each frame in (current … ancestor], then {@code before} thunks
   * for each frame in (ancestor … target] (outermost first). Updates {@code CURRENT} as each thunk
   * runs so that the extent is always consistent if a thunk itself jumps.
   */
  public static void windTo(WindFrame target) {
    WindFrame current = CURRENT.get();
    if (current == target) {
      return;
    }

    WindFrame ancestor = commonAncestor(current, target);

    // Unwind: run after thunks from current up to (not including) ancestor
    for (WindFrame f = current; f != ancestor; f = f.parent) {
      CURRENT.set(f.parent);
      f.after.function().evaluate(new LispObject[0]);
    }

    // Rewind: collect frames to enter, then run before thunks outermost-first
    List<WindFrame> toEnter = new ArrayList<>();
    for (WindFrame f = target; f != ancestor; f = f.parent) {
      toEnter.add(f);
    }
    for (int i = toEnter.size() - 1; i >= 0; i--) {
      WindFrame f = toEnter.get(i);
      f.before.function().evaluate(new LispObject[0]);
      CURRENT.set(f);
    }
  }

  /**
   * Collect frames that must be entered when transitioning from the frame at {@code current} to
   * {@code target} (i.e. frames in target's chain that are not in current's chain), ordered
   * outermost first.
   */
  public static List<WindFrame> framesToEnter(WindFrame current, WindFrame target) {
    WindFrame ancestor = commonAncestor(current, target);
    List<WindFrame> frames = new ArrayList<>();
    for (WindFrame f = target; f != ancestor; f = f.parent) {
      frames.add(f);
    }
    // reverse so outermost (nearest to ancestor) comes first
    java.util.Collections.reverse(frames);
    return frames;
  }

  private static WindFrame commonAncestor(WindFrame a, WindFrame b) {
    // Bring both to the same depth, then walk up together.
    while (a != null && b != null && a.depth > b.depth) {
      a = a.parent;
    }
    while (a != null && b != null && b.depth > a.depth) {
      b = b.parent;
    }
    while (a != b) {
      if (a == null || b == null) {
        return null;
      }
      a = a.parent;
      b = b.parent;
    }
    return a;
  }
}
