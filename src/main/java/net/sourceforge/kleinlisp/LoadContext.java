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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks the context for file loading operations, including the current file being loaded and
 * detection of cyclical references.
 *
 * <p>This class is used by the (load ...) function to:
 *
 * <ul>
 *   <li>Resolve relative paths relative to the currently loading file
 *   <li>Detect and prevent cyclical load references
 *   <li>Provide the current-load-pathname for introspection
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class LoadContext {

  /** Stack of files currently being loaded (for relative path resolution). */
  private final Deque<Path> loadStack = new ArrayDeque<>();

  /** Set of canonical paths currently being loaded (for cycle detection). */
  private final Set<Path> loadingPaths = new HashSet<>();

  /**
   * Returns the current file being loaded, or null if not loading any file.
   *
   * @return the path of the current file, or null
   */
  public Path getCurrentLoadPath() {
    return loadStack.peek();
  }

  /**
   * Returns the directory of the current file being loaded, or the current working directory if not
   * loading any file.
   *
   * @return the directory for resolving relative paths
   */
  public Path getBaseDirectory() {
    Path current = loadStack.peek();
    if (current != null) {
      return current.getParent();
    }
    return Paths.get("").toAbsolutePath();
  }

  /**
   * Resolves a filename relative to the current loading context. If the filename is absolute, it is
   * returned as-is. Otherwise, it is resolved relative to the directory of the current file being
   * loaded.
   *
   * @param filename the filename to resolve
   * @return the resolved absolute path
   */
  public Path resolvePath(String filename) {
    Path path = Paths.get(filename);
    if (path.isAbsolute()) {
      return path.normalize();
    }
    return getBaseDirectory().resolve(path).normalize();
  }

  /**
   * Begins loading a file. Must be paired with a call to {@link #endLoad()}.
   *
   * @param path the normalized, absolute path of the file to load
   * @throws LispRuntimeException if the file is already being loaded (cyclical reference)
   */
  public void beginLoad(Path path) {
    Path normalized = path.toAbsolutePath().normalize();

    if (loadingPaths.contains(normalized)) {
      throw new LispRuntimeException(buildCycleErrorMessage(normalized));
    }

    loadingPaths.add(normalized);
    loadStack.push(normalized);
  }

  /**
   * Ends loading the current file. Must be called after {@link #beginLoad(Path)} completes.
   *
   * @throws IllegalStateException if no file is currently being loaded
   */
  public void endLoad() {
    if (loadStack.isEmpty()) {
      throw new IllegalStateException("endLoad called without matching beginLoad");
    }

    Path path = loadStack.pop();
    loadingPaths.remove(path);
  }

  /**
   * Returns true if currently loading any file.
   *
   * @return true if a file is being loaded
   */
  public boolean isLoading() {
    return !loadStack.isEmpty();
  }

  /**
   * Returns the depth of the current load stack (number of nested loads).
   *
   * @return the load depth
   */
  public int getLoadDepth() {
    return loadStack.size();
  }

  private String buildCycleErrorMessage(Path cyclePath) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cyclical load detected: ");
    sb.append(cyclePath);
    sb.append("\nLoad stack:\n");

    int depth = 0;
    for (Path p : loadStack) {
      for (int i = 0; i < depth; i++) {
        sb.append("  ");
      }
      sb.append("-> ");
      sb.append(p);
      sb.append("\n");
      depth++;
    }

    for (int i = 0; i < depth; i++) {
      sb.append("  ");
    }
    sb.append("-> ");
    sb.append(cyclePath);
    sb.append(" (CYCLE)");

    return sb.toString();
  }
}
