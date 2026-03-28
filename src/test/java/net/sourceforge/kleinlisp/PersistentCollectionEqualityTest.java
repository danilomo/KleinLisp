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

import net.sourceforge.kleinlisp.objects.BooleanObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for equal? with persistent collections (PVectorObject, PMapObject, PSetObject). */
public class PersistentCollectionEqualityTest {

  private Lisp lisp;

  @BeforeEach
  public void setup() {
    lisp = new Lisp();
  }

  // ===== PVectorObject tests =====

  @Test
  public void testPVectorEqualitySame() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-vec 1 2 3) (p-vec 1 2 3))"));
  }

  @Test
  public void testPVectorEqualityDifferent() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2 3) (p-vec 1 2 4))"));
  }

  @Test
  public void testPVectorEqualityEmpty() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-vec) (p-vec))"));
  }

  @Test
  public void testPVectorEqualityDifferentLength() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2) (p-vec 1 2 3))"));
  }

  @Test
  public void testPVectorEqualityNested() {
    assertEquals(
        BooleanObject.TRUE, lisp.evaluate("(equal? (p-vec 1 (p-vec 2 3)) (p-vec 1 (p-vec 2 3)))"));
  }

  @Test
  public void testPVectorNotEqualToList() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2 3) '(1 2 3))"));
  }

  @Test
  public void testPVectorNotEqualToVector() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2 3) (vector 1 2 3))"));
  }

  // ===== PSetObject tests =====

  @Test
  public void testPSetEqualitySame() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-set 1 2 3) (p-set 1 2 3))"));
  }

  @Test
  public void testPSetEqualityDifferentOrder() {
    // Sets should be equal regardless of order
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-set 1 2 3) (p-set 3 2 1))"));
  }

  @Test
  public void testPSetEqualityDifferent() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-set 1 2 3) (p-set 1 2 4))"));
  }

  @Test
  public void testPSetEqualityEmpty() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-set) (p-set))"));
  }

  @Test
  public void testPSetNotEqualToList() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-set 1 2 3) '(1 2 3))"));
  }

  // ===== PMapObject tests =====

  @Test
  public void testPMapEqualitySame() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-map 'a 1 'b 2) (p-map 'a 1 'b 2))"));
  }

  @Test
  public void testPMapEqualityDifferentOrder() {
    // Maps should be equal regardless of key order
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-map 'a 1 'b 2) (p-map 'b 2 'a 1))"));
  }

  @Test
  public void testPMapEqualityDifferentValue() {
    assertEquals(
        BooleanObject.FALSE, lisp.evaluate("(equal? (p-map 'a 1 'b 2) (p-map 'a 1 'b 3))"));
  }

  @Test
  public void testPMapEqualityDifferentKey() {
    assertEquals(
        BooleanObject.FALSE, lisp.evaluate("(equal? (p-map 'a 1 'b 2) (p-map 'a 1 'c 2))"));
  }

  @Test
  public void testPMapEqualityEmpty() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(equal? (p-map) (p-map))"));
  }

  // ===== Cross-type tests =====

  @Test
  public void testPVectorNotEqualToPSet() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2 3) (p-set 1 2 3))"));
  }

  @Test
  public void testPVectorNotEqualToPMap() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-vec 1 2) (p-map 1 2))"));
  }

  @Test
  public void testPSetNotEqualToPMap() {
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(equal? (p-set 1 2) (p-map 1 2))"));
  }
}
