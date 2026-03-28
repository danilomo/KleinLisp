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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.JavaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for JavaObject equality, hashCode, and comparison support. */
public class JavaObjectTest {

  private Lisp lisp;

  @BeforeEach
  public void setup() {
    lisp = new Lisp();
  }

  @Test
  public void testJavaObjectEquality() {
    // Two JavaObjects wrapping equal strings should be equal
    JavaObject obj1 = new JavaObject("hello");
    JavaObject obj2 = new JavaObject("hello");
    JavaObject obj3 = new JavaObject("world");

    assertEquals(obj1, obj2);
    assertNotEquals(obj1, obj3);
    assertEquals(obj1.hashCode(), obj2.hashCode());
  }

  @Test
  public void testJavaObjectEqualityWithCustomClass() {
    // Test with a custom class that properly implements equals/hashCode
    Point p1 = new Point(1, 2);
    Point p2 = new Point(1, 2);
    Point p3 = new Point(3, 4);

    JavaObject obj1 = new JavaObject(p1);
    JavaObject obj2 = new JavaObject(p2);
    JavaObject obj3 = new JavaObject(p3);

    assertEquals(obj1, obj2);
    assertNotEquals(obj1, obj3);
    assertEquals(obj1.hashCode(), obj2.hashCode());
  }

  @Test
  public void testJavaObjectEqualityInScheme() {
    // Register Java objects in Scheme and test equal?
    JavaObject obj1 = new JavaObject("test");
    JavaObject obj2 = new JavaObject("test");
    JavaObject obj3 = new JavaObject("other");

    lisp.environment().set(lisp.environment().atomOf("obj1"), obj1);
    lisp.environment().set(lisp.environment().atomOf("obj2"), obj2);
    lisp.environment().set(lisp.environment().atomOf("obj3"), obj3);

    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? obj1 obj2)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? obj1 obj3)"));
  }

  @Test
  public void testJavaObjectComparison() {
    // Test comparison with Comparable objects (String)
    JavaObject obj1 = new JavaObject("apple");
    JavaObject obj2 = new JavaObject("banana");
    JavaObject obj3 = new JavaObject("apple");

    assertTrue(obj1.isComparable());
    assertTrue(obj1.compareTo(obj2) < 0);
    assertTrue(obj2.compareTo(obj1) > 0);
    assertEquals(0, obj1.compareTo(obj3));
  }

  @Test
  public void testJavaObjectComparisonInScheme() {
    // Register Comparable Java objects and test comparison operators
    JavaObject date1 = new JavaObject(LocalDate.of(2024, 1, 1));
    JavaObject date2 = new JavaObject(LocalDate.of(2024, 6, 15));
    JavaObject date3 = new JavaObject(LocalDate.of(2024, 1, 1));

    lisp.environment().set(lisp.environment().atomOf("d1"), date1);
    lisp.environment().set(lisp.environment().atomOf("d2"), date2);
    lisp.environment().set(lisp.environment().atomOf("d3"), date3);

    assertEquals(BooleanObject.TRUE, lisp.evaluate("(< d1 d2)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(< d2 d1)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(> d2 d1)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(<= d1 d3)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(>= d1 d3)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(= d1 d3)"));
  }

  @Test
  public void testJavaObjectComparisonWithBigDecimal() {
    // Test comparison with BigDecimal (numeric Comparable)
    JavaObject bd1 = new JavaObject(new BigDecimal("10.5"));
    JavaObject bd2 = new JavaObject(new BigDecimal("20.0"));
    JavaObject bd3 = new JavaObject(new BigDecimal("10.50")); // Equal to bd1

    lisp.environment().set(lisp.environment().atomOf("n1"), bd1);
    lisp.environment().set(lisp.environment().atomOf("n2"), bd2);
    lisp.environment().set(lisp.environment().atomOf("n3"), bd3);

    assertEquals(BooleanObject.TRUE, lisp.evaluate("(< n1 n2)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(= n1 n3)"));
    assertEquals(
        BooleanObject.FALSE,
        lisp.evaluate("(< n1 n2 n2)")); // Variadic: n1 < n2 but n2 < n2 is false
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(< n1 n2 n1)")); // n1 < n2 but n2 > n1
  }

  @Test
  public void testJavaObjectVariadicComparison() {
    // Test variadic comparison with strings
    JavaObject a = new JavaObject("a");
    JavaObject b = new JavaObject("b");
    JavaObject c = new JavaObject("c");

    lisp.environment().set(lisp.environment().atomOf("ja"), a);
    lisp.environment().set(lisp.environment().atomOf("jb"), b);
    lisp.environment().set(lisp.environment().atomOf("jc"), c);

    assertEquals(BooleanObject.TRUE, lisp.evaluate("(< ja jb jc)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(< ja jc jb)"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(> jc jb ja)"));
  }

  @Test
  public void testNonComparableJavaObject() {
    // Test that non-Comparable objects throw appropriate error
    JavaObject obj = new JavaObject(new Object());
    assertFalse(obj.isComparable());

    lisp.environment().set(lisp.environment().atomOf("ncobj"), obj);
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(< ncobj ncobj)"));
  }

  // Helper class for testing custom equals/hashCode
  private static class Point {
    private final int x;
    private final int y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Point)) return false;
      Point other = (Point) obj;
      return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }
}
