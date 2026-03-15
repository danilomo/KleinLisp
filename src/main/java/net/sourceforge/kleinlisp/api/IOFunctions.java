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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.LazySeqObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;
import net.sourceforge.kleinlisp.objects.seq.ReaderSeq;

/**
 * @author danilo
 */
public class IOFunctions {

  public static LispObject print(LispObject[] params) {
    String asString = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(" "));
    System.out.print(asString);
    return VoidObject.VOID;
  }

  public static LispObject println(LispObject[] params) {
    String asString = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(" "));
    System.out.println(asString);
    return VoidObject.VOID;
  }

  public static LispObject newline(LispObject[] params) {
    System.out.println();
    return VoidObject.VOID;
  }

  /** Raises an error with a message. (error message) or (error message value ...) */
  public static LispObject error(LispObject[] params) {
    String message = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(" "));
    throw new LispArgumentError(message);
  }

  /**
   * Returns a lazy sequence of lines from a file.
   * (line-seq filename) - opens the file and returns a lazy seq of strings (one per line).
   *
   * <p>The sequence is lazy - lines are read only when requested.
   * The file is automatically closed when the sequence is exhausted.
   *
   * <p>Example:
   * <pre>
   * (define lines (line-seq "myfile.txt"))
   * (first lines)  ; => first line of file
   * (map println (line-seq "data.txt"))  ; print all lines
   * </pre>
   */
  public static LispObject lineSeq(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("line-seq requires a filename");
    }

    StringObject filenameObj = params[0].asString();
    if (filenameObj == null) {
      throw new LispArgumentError("line-seq requires a string filename, got: " + params[0]);
    }

    String filename = filenameObj.value();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      ReaderSeq seq = new ReaderSeq(reader);

      // If file is empty, close reader and return NIL
      if (seq.isEmpty()) {
        seq.close();
        return ListObject.NIL;
      }

      return new LazySeqObject(seq, "line-seq:" + filename);
    } catch (IOException e) {
      throw new LispArgumentError("Cannot open file: " + filename + " - " + e.getMessage());
    }
  }

  /**
   * Reads the entire contents of a file as a string.
   * (slurp filename) - returns the entire file content as a single string.
   *
   * <p>Example:
   * <pre>
   * (slurp "myfile.txt")  ; => "file contents..."
   * </pre>
   */
  public static LispObject slurp(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("slurp requires a filename");
    }

    StringObject filenameObj = params[0].asString();
    if (filenameObj == null) {
      throw new LispArgumentError("slurp requires a string filename, got: " + params[0]);
    }

    String filename = filenameObj.value();

    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      StringBuilder content = new StringBuilder();
      String line;
      boolean first = true;
      while ((line = reader.readLine()) != null) {
        if (!first) {
          content.append("\n");
        }
        content.append(line);
        first = false;
      }
      return new StringObject(content.toString());
    } catch (IOException e) {
      throw new LispArgumentError("Cannot read file: " + filename + " - " + e.getMessage());
    }
  }
}
