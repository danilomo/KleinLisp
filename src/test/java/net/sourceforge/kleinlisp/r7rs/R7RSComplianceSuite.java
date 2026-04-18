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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Master test suite for R7RS compliance testing.
 *
 * <p>This suite aggregates all R7RS-related tests to provide comprehensive coverage of the R7RS
 * Scheme specification. Running this suite tests:
 *
 * <ul>
 *   <li>Core language semantics (truthiness, evaluation)
 *   <li>Library system (imports, exports, validation)
 *   <li>Standard procedures (numeric, string, list, I/O)
 *   <li>Special forms (include, define-library, import)
 *   <li>Compatibility with reference implementations
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * ./gradlew test --tests "R7RSComplianceSuite"
 * </pre>
 *
 * @author Danilo Oliveira
 */
@Suite
@SuiteDisplayName("R7RS Compliance Test Suite")
@SelectClasses({
  // Core language tests
  R7RSTestSuite.class, // Dynamic tests from .scm files (numeric, string, char, comparison,
  // truthiness)

  // Library system tests
  R7RSImportFilterTest.class, // Import parsing and basic functionality
  R7RSImportValidationTest.class, // Import filter validation in R7RS mode
  R7RSLibraryExportAudit.class, // Library export population audit

  // Special forms tests
  R7RSIncludeTest.class, // Include and include-ci functionality

  // Procedure existence and functionality tests
  R7RSProcedureChecklist.class // Systematic check of all R7RS procedures
})
public class R7RSComplianceSuite {
  // This class serves as a test suite container
  // Individual test classes are executed via the @SelectClasses annotation
}
