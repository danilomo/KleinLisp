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

import static org.junit.jupiter.api.Assertions.*;

import net.sourceforge.kleinlisp.objects.PMapObject;
import org.junit.jupiter.api.Test;

/** Tests for JSON parsing functions. */
public class JsonFunctionsTest extends BaseTestClass {

  @Test
  public void testParseEmptyObject() {
    LispObject result = lisp.evaluate("(json-parse \"{}\")");
    assertTrue(result instanceof PMapObject);
    assertEquals(0, lisp.evaluate("(p-map-size (json-parse \"{}\"))").asInt().value);
  }

  @Test
  public void testParseSimpleObject() {
    lisp.evaluate("(define m (json-parse \"{\\\"name\\\": \\\"Alice\\\", \\\"age\\\": 30}\"))");
    assertTrue(lisp.evaluate("(p-map? m)").truthiness());
    assertEquals(2, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals("Alice", lisp.evaluate("(p-map-get m \"name\")").asString().value());
    assertEquals(30, lisp.evaluate("(p-map-get m \"age\")").asInt().value);
  }

  @Test
  public void testParseNestedObject() {
    String json = "{\\\"person\\\": {\\\"name\\\": \\\"Bob\\\", \\\"age\\\": 25}}";
    lisp.evaluate("(define m (json-parse \"" + json + "\"))");
    assertTrue(lisp.evaluate("(p-map? (p-map-get m \"person\"))").truthiness());
    assertEquals(
        "Bob", lisp.evaluate("(p-map-get (p-map-get m \"person\") \"name\")").asString().value());
  }

  @Test
  public void testParseArray() {
    lisp.evaluate("(define lst (json-parse \"[1, 2, 3, 4, 5]\"))");
    assertTrue(lisp.evaluate("(list? lst)").truthiness());
    assertEquals(5, lisp.evaluate("(length lst)").asInt().value);
    assertEquals(1, lisp.evaluate("(car lst)").asInt().value);
    assertEquals(3, lisp.evaluate("(caddr lst)").asInt().value);
  }

  @Test
  public void testParseEmptyArray() {
    lisp.evaluate("(define lst (json-parse \"[]\"))");
    assertTrue(lisp.evaluate("(null? lst)").truthiness());
  }

  @Test
  public void testParseMixedArray() {
    lisp.evaluate("(define lst (json-parse \"[1, \\\"hello\\\", true, null]\"))");
    assertEquals(4, lisp.evaluate("(length lst)").asInt().value);
    assertEquals(1, lisp.evaluate("(car lst)").asInt().value);
    assertEquals("hello", lisp.evaluate("(cadr lst)").asString().value());
    assertTrue(lisp.evaluate("(caddr lst)").truthiness());
    assertTrue(lisp.evaluate("(null? (cadddr lst))").truthiness());
  }

  @Test
  public void testParseArrayOfObjects() {
    String json = "[{\\\"id\\\": 1}, {\\\"id\\\": 2}]";
    lisp.evaluate("(define lst (json-parse \"" + json + "\"))");
    assertEquals(2, lisp.evaluate("(length lst)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-map-get (car lst) \"id\")").asInt().value);
    assertEquals(2, lisp.evaluate("(p-map-get (cadr lst) \"id\")").asInt().value);
  }

  @Test
  public void testParseBooleans() {
    lisp.evaluate("(define m (json-parse \"{\\\"active\\\": true, \\\"deleted\\\": false}\"))");
    assertTrue(lisp.evaluate("(p-map-get m \"active\")").truthiness());
    assertFalse(lisp.evaluate("(p-map-get m \"deleted\")").truthiness());
  }

  @Test
  public void testParseNull() {
    lisp.evaluate("(define m (json-parse \"{\\\"value\\\": null}\"))");
    assertTrue(lisp.evaluate("(null? (p-map-get m \"value\"))").truthiness());
  }

  @Test
  public void testParseNumbers() {
    lisp.evaluate("(define m (json-parse \"{\\\"int\\\": 42, \\\"float\\\": 3.14}\"))");
    assertEquals(42, lisp.evaluate("(p-map-get m \"int\")").asInt().value);
    assertEquals(3.14, lisp.evaluate("(p-map-get m \"float\")").asDouble().value, 0.001);
  }

  @Test
  public void testParseStringPrimitive() {
    LispObject result = lisp.evaluate("(json-parse \"\\\"hello\\\"\")");
    assertEquals("hello", result.asString().value());
  }

  @Test
  public void testParseNumberPrimitive() {
    assertEquals(42, lisp.evaluate("(json-parse \"42\")").asInt().value);
    assertEquals(3.14, lisp.evaluate("(json-parse \"3.14\")").asDouble().value, 0.001);
  }

  @Test
  public void testParseBooleanPrimitive() {
    assertTrue(lisp.evaluate("(json-parse \"true\")").truthiness());
    assertFalse(lisp.evaluate("(json-parse \"false\")").truthiness());
  }

  @Test
  public void testParseNullPrimitive() {
    assertTrue(lisp.evaluate("(null? (json-parse \"null\"))").truthiness());
  }

  @Test
  public void testInvalidJson() {
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(json-parse \"{invalid}\")"));
  }

  @Test
  public void testEmptyString() {
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(json-parse \"\")"));
  }

  @Test
  public void testNonStringArgument() {
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(json-parse 42)"));
  }

  @Test
  public void testNoArguments() {
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(json-parse)"));
  }

  @Test
  public void testComplexNestedStructure() {
    String json =
        "{\\\"users\\\": [{\\\"name\\\": \\\"Alice\\\", \\\"tags\\\": [\\\"admin\\\","
            + " \\\"user\\\"]}, {\\\"name\\\": \\\"Bob\\\", \\\"tags\\\": [\\\"user\\\"]}]}";
    lisp.evaluate("(define data (json-parse \"" + json + "\"))");

    // Access nested structure
    assertEquals(2, lisp.evaluate("(length (p-map-get data \"users\"))").asInt().value);
    assertEquals(
        "Alice",
        lisp.evaluate("(p-map-get (car (p-map-get data \"users\")) \"name\")").asString().value());
    assertEquals(
        2,
        lisp.evaluate("(length (p-map-get (car (p-map-get data \"users\")) \"tags\"))")
            .asInt()
            .value);
    assertEquals(
        "admin",
        lisp.evaluate("(car (p-map-get (car (p-map-get data \"users\")) \"tags\"))")
            .asString()
            .value());
  }
}
