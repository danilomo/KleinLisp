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
package net.sourceforge.kleinlisp.r7rs;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.kleinlisp.BaseTestClass;
import net.sourceforge.kleinlisp.Library;
import org.junit.jupiter.api.Test;

/**
 * Audit test to check the state of standard library export sets.
 *
 * <p>This test documents which libraries have populated exports and which need to be populated
 * according to R7RS Appendix A.
 *
 * @author Danilo Oliveira
 */
public class R7RSLibraryExportAudit extends BaseTestClass {

  @Test
  public void auditSchemeBaseExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "base"));

    assertNotNull(lib, "(scheme base) should be registered");
    assertNotNull(lib.getExports(), "Exports should not be null");

    System.out.println("\n=== (scheme base) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports (~100 symbols) from R7RS Section 6:");
      System.out.println("  - Arithmetic: +, -, *, /, quotient, remainder, modulo, etc.");
      System.out.println("  - Comparison: =, <, >, <=, >=, eq?, eqv?, equal?");
      System.out.println("  - Lists: car, cdr, cons, list, append, reverse, etc.");
      System.out.println("  - Booleans: not, and, or, boolean?");
      System.out.println("  - Strings: string, string-append, string-ref, etc.");
      System.out.println("  - Vectors: vector, vector-ref, vector-set!, etc.");
      System.out.println("  - Type predicates: number?, string?, symbol?, pair?, etc.");
      System.out.println("  - Special forms: define, lambda, if, cond, let, etc.");
    } else {
      System.out.println("Sample exports: " + sampleExports(lib.getExports(), 10));
    }
  }

  @Test
  public void auditSchemeWriteExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "write"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme write) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: write, display, newline");
    }
  }

  @Test
  public void auditSchemeReadExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "read"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme read) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: read");
    }
  }

  @Test
  public void auditSchemeFileExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "file"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme file) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: call-with-input-file, call-with-output-file,");
      System.out.println("  open-input-file, open-output-file, open-binary-input-file,");
      System.out.println("  open-binary-output-file, close-input-port, close-output-port,");
      System.out.println("  with-input-from-file, with-output-to-file, file-exists?, delete-file");
    }
  }

  @Test
  public void auditSchemeCxrExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "cxr"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme cxr) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: caar, cadr, cdar, cddr, caaar, caadr, ..., cddddr");
      System.out.println("  (24 combinations total)");
    }
  }

  @Test
  public void auditSchemeCharExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "char"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme char) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: char-alphabetic?, char-numeric?, char-whitespace?,");
      System.out.println("  char-upper-case?, char-lower-case?, char-upcase, char-downcase,");
      System.out.println("  char-foldcase, digit-value, char-ci=?, char-ci<?, etc.");
    }
  }

  @Test
  public void auditSchemeProcessContextExports() {
    Library lib =
        lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "process-context"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme process-context) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: command-line, exit,");
      System.out.println("  get-environment-variable, get-environment-variables");
    }
  }

  @Test
  public void auditSchemeTimeExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "time"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme time) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: current-second, current-jiffy, jiffies-per-second");
    }
  }

  @Test
  public void auditSchemeLazyExports() {
    Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "lazy"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme lazy) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: delay, delay-force, force, make-promise, promise?");
    }
  }

  @Test
  public void auditSchemeCaseLambdaExports() {
    Library lib =
        lisp.environment().getLibraryRegistry().get(Arrays.asList("scheme", "case-lambda"));

    assertNotNull(lib);
    System.out.println("\n=== (scheme case-lambda) Export Audit ===");
    System.out.println("Export count: " + lib.getExports().size());

    if (lib.getExports().isEmpty()) {
      System.out.println("STATUS: NEEDS POPULATION");
      System.out.println("Required exports: case-lambda");
    }
  }

  @Test
  public void auditAllStandardLibraries() {
    System.out.println("\n=== All Standard Libraries Audit ===");

    String[] libraries = {
      "scheme.base", "scheme.write", "scheme.read", "scheme.file",
      "scheme.cxr", "scheme.char", "scheme.process-context", "scheme.time",
      "scheme.lazy", "scheme.case-lambda", "scheme.eval", "scheme.inexact",
      "scheme.load", "scheme.repl"
    };

    int emptyCount = 0;
    int populatedCount = 0;

    for (String libKey : libraries) {
      if (lisp.environment().getLibraryRegistry().getAllLibraryNames().contains(libKey)) {
        String[] parts = libKey.split("\\.");
        Library lib = lisp.environment().getLibraryRegistry().get(Arrays.asList((Object[]) parts));

        if (lib != null && lib.getExports().isEmpty()) {
          emptyCount++;
        } else if (lib != null) {
          populatedCount++;
        }
      }
    }

    System.out.println("Libraries with empty exports: " + emptyCount);
    System.out.println("Libraries with populated exports: " + populatedCount);
    System.out.println("\nCONCLUSION: All standard libraries need export population (Phase 3.1)");
  }

  /** Helper to get a sample of exports for display. */
  private String sampleExports(Set<String> exports, int count) {
    Set<String> sample = new HashSet<>();
    int i = 0;
    for (String export : exports) {
      if (i++ >= count) break;
      sample.add(export);
    }
    return sample.toString();
  }
}
