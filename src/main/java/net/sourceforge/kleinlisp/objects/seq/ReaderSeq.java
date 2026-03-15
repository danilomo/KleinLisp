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
package net.sourceforge.kleinlisp.objects.seq;

import java.io.BufferedReader;
import java.io.IOException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Seq;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 * A lazy Seq implementation that reads lines from a BufferedReader.
 * Lines are read on demand - calling first() reads the next line,
 * and rest() returns a new ReaderSeq that will continue reading.
 *
 * <p>This is similar to Clojure's line-seq function.
 *
 * <p>Note: The reader should be closed after the sequence is consumed.
 * Consider using with-open or similar resource management patterns.
 *
 * @author Danilo Oliveira
 */
public final class ReaderSeq implements Seq {

  private final BufferedReader reader;
  private String cachedLine;
  private boolean lineCached;
  private boolean eof;
  private Seq rest;
  private boolean restComputed;

  public ReaderSeq(BufferedReader reader) {
    this.reader = reader;
    this.lineCached = false;
    this.eof = false;
    this.restComputed = false;
  }

  // Private constructor for rest sequences sharing the same reader
  private ReaderSeq(BufferedReader reader, String firstLine) {
    this.reader = reader;
    this.cachedLine = firstLine;
    this.lineCached = true;
    this.eof = (firstLine == null);
    this.restComputed = false;
  }

  @Override
  public LispObject first() {
    ensureLineCached();
    if (eof) {
      return null;
    }
    return new StringObject(cachedLine);
  }

  @Override
  public Seq rest() {
    if (!restComputed) {
      ensureLineCached();
      if (eof) {
        rest = new EmptyReaderSeq(reader);
      } else {
        // Read the next line for the rest sequence
        String nextLine = readLine();
        rest = new ReaderSeq(reader, nextLine);
      }
      restComputed = true;
    }
    return rest;
  }

  @Override
  public boolean isEmpty() {
    ensureLineCached();
    return eof;
  }

  private void ensureLineCached() {
    if (!lineCached) {
      cachedLine = readLine();
      eof = (cachedLine == null);
      lineCached = true;
    }
  }

  private String readLine() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      throw new RuntimeException("Error reading from file: " + e.getMessage(), e);
    }
  }

  /**
   * Returns the underlying BufferedReader.
   * Can be used to close the reader when done.
   */
  public BufferedReader getReader() {
    return reader;
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      // Ignore close errors
    }
  }

  /**
   * An empty sequence that still holds a reference to the reader for closing.
   */
  private static final class EmptyReaderSeq implements Seq {
    private final BufferedReader reader;

    EmptyReaderSeq(BufferedReader reader) {
      this.reader = reader;
    }

    @Override
    public LispObject first() {
      return null;
    }

    @Override
    public Seq rest() {
      return this;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
  }
}
