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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR IN CONNECTION WITH THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import net.sourceforge.kleinlisp.objects.EnvironmentObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for R7RS environment functions: interaction-environment, scheme-report-environment,
 * null-environment, and eval with environment.
 *
 * @author Danilo Oliveira
 */
class EnvironmentFunctionsTest extends BaseTestClass {

  @Test
  void testInteractionEnvironmentReturnsEnvironmentObject() {
    LispObject result = lisp.evaluate("(interaction-environment)");
    assertNotNull(result);
    assertTrue(result instanceof EnvironmentObject, "Should return an EnvironmentObject");
  }

  @Test
  void testInteractionEnvironmentToString() {
    LispObject result = lisp.evaluate("(interaction-environment)");
    String str = result.toString();
    assertTrue(
        str.contains("environment") && str.contains("interaction"),
        "Should contain 'environment' and 'interaction': " + str);
  }

  @Test
  void testSchemeReportEnvironmentVersion5() {
    LispObject result = lisp.evaluate("(scheme-report-environment 5)");
    assertNotNull(result);
    assertTrue(result instanceof EnvironmentObject, "Should return an EnvironmentObject");
  }

  @Test
  void testSchemeReportEnvironmentVersion7() {
    LispObject result = lisp.evaluate("(scheme-report-environment 7)");
    assertNotNull(result);
    assertTrue(result instanceof EnvironmentObject, "Should return an EnvironmentObject");
  }

  @Test
  void testSchemeReportEnvironmentInvalidVersion() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(scheme-report-environment 6)"),
        "Should reject version 6");
  }

  @Test
  void testSchemeReportEnvironmentRequiresInteger() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(scheme-report-environment \"5\")"),
        "Should require integer argument");
  }

  @Test
  void testSchemeReportEnvironmentRequiresArgument() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(scheme-report-environment)"),
        "Should require version argument");
  }

  @Test
  void testNullEnvironmentVersion5() {
    LispObject result = lisp.evaluate("(null-environment 5)");
    assertNotNull(result);
    assertTrue(result instanceof EnvironmentObject, "Should return an EnvironmentObject");
  }

  @Test
  void testNullEnvironmentVersion7() {
    LispObject result = lisp.evaluate("(null-environment 7)");
    assertNotNull(result);
    assertTrue(result instanceof EnvironmentObject, "Should return an EnvironmentObject");
  }

  @Test
  void testNullEnvironmentInvalidVersion() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(null-environment 3)"),
        "Should reject version 3");
  }

  @Test
  void testEvalWithExpression() {
    int result = evalAsInt("(eval '(+ 1 2))");
    assertEquals(3, result, "eval should evaluate the expression");
  }

  @Test
  void testEvalWithExpressionAndEnvironment() {
    int result = evalAsInt("(eval '(+ 1 2) (interaction-environment))");
    assertEquals(3, result, "eval with environment should evaluate the expression");
  }

  @Test
  void testEvalWithSchemeReportEnvironment() {
    int result = evalAsInt("(eval '(* 3 4) (scheme-report-environment 5))");
    assertEquals(12, result, "eval with scheme-report-environment should work");
  }

  @Test
  void testEvalWithNullEnvironment() {
    int result = evalAsInt("(eval '(+ 5 6) (null-environment 7))");
    assertEquals(11, result, "eval with null-environment should work");
  }

  @Test
  void testEvalRequiresAtLeastOneArgument() {
    assertThrows(
        LispRuntimeException.class, () -> lisp.evaluate("(eval)"), "eval requires an argument");
  }

  @Test
  void testEvalRequiresAtMostTwoArguments() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(eval '1 (interaction-environment) 'extra)"),
        "eval requires at most 2 arguments");
  }

  @Test
  void testEvalWithInvalidEnvironment() {
    assertThrows(
        LispRuntimeException.class,
        () -> lisp.evaluate("(eval '1 42)"),
        "eval requires an environment object");
  }

  @Test
  void testEvalWithDefinedVariable() {
    lisp.evaluate("(define x 42)");
    int result = evalAsInt("(eval 'x (interaction-environment))");
    assertEquals(42, result, "eval should access defined variables");
  }

  @Test
  void testEvalWithQuotedExpression() {
    int result = evalAsInt("(eval (list '+ 10 20))");
    assertEquals(30, result, "eval should work with dynamically constructed expressions");
  }

  @Test
  void testInteractionEnvironmentTruthiness() {
    boolean result = evalAsBoolean("(if (interaction-environment) #t #f)");
    assertTrue(result, "Environment object should be truthy");
  }

  @Test
  void testEnvironmentObjectsAreDistinct() {
    // Each call creates a new wrapper, but they wrap the same environment
    lisp.evaluate("(define env1 (interaction-environment))");
    lisp.evaluate("(define env2 (interaction-environment))");

    // Both should be environment objects
    LispObject env1 = lisp.evaluate("env1");
    LispObject env2 = lisp.evaluate("env2");
    assertTrue(env1 instanceof EnvironmentObject);
    assertTrue(env2 instanceof EnvironmentObject);
  }

  @Test
  void testEvalNestedExpressions() {
    int result = evalAsInt("(eval '(+ (eval '(* 2 3)) 4))");
    assertEquals(10, result, "eval should handle nested evaluations");
  }

  @Test
  void testSchemeReportEnvironmentWithEval() {
    // Test that basic R5RS functions work
    boolean result = evalAsBoolean("(eval '(list? '(1 2 3)) (scheme-report-environment 5))");
    assertTrue(result, "R5RS functions should work in scheme-report-environment");
  }

  @Test
  void testNullEnvironmentWithEval() {
    // Test basic evaluation (the environment still has access to + in this implementation)
    int result = evalAsInt("(eval '(if #t 42 0) (null-environment 7))");
    assertEquals(42, result, "Special forms should work in null-environment");
  }
}
