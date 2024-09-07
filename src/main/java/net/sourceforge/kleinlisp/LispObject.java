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

import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.Optional;
import net.sourceforge.kleinlisp.objects.CellObject;

/**
 * A Lisp object, i.e., something that can be evaluated in Lisp programs: atoms,
 * strings, number literals, lists, etc.
 *
 * @author Danilo Oliveira
 */
public interface LispObject {

    /**
     * Return the form as a Java object, applying some conversion if necessary.
     * <p>
     * AtomForm, StringForm -> String, IntForm -> Integer, DoubleForm -> Double,
     * ListObject -> java.util.List, etc.
     *
     * @return A java.lang.Object according to the LispObject concrete type
     */
    Object asObject();

    /**
     * Return the boolean value corresponding to the object.Nil (empty list) ->
     * False, 0 -> False, False -> False, "" -> False, everything else -> true
     *
     * @return
     */
    boolean truthiness();

    /**
     * If the form represents a numeric value, returns Optional.of(number),
     * otherwise, return an Optional.empty().
     * <p>
     * For DoubleForm, the value is coerced to int.
     *
     * @return see description
     */
    default Optional<Integer> asInt() {
        return Optional.empty();
    }

    /**
     * If the form represents a numeric value, returns Optional.of(number),
     * otherwise, return an Optional.empty().
     * <p>
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    default Optional<Double> asDouble() {
        return Optional.empty();
    }

    /**
     * If the form represents a list, returns an Optional.of(list), otherwise,
     * return an Optional.empty().
     * <p>
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    default Optional<ListObject> asList() {
        return Optional.empty();
    }

    /**
     * If the form represents a list, returns an Optional.of(list), otherwise,
     * return an Optional.empty().
     * <p>
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    default Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }

    /**
     * @return see description
     */
    default Optional<AtomObject> asAtom() {
        return Optional.empty();
    }

    /**
     * Returns an Optional.of(reference) if it is an ObjectForm belonging to the
     * specified type, otherwise, returns an Optional.empty().
     *
     * @param <T>   The expected type
     * @param clazz The Class reference for the T type
     * @return see description
     */
    default <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }
    
    default Optional<CellObject> asCell() {
        return Optional.empty();
    }
    
    default void set(LispObject value) {
        throw new UnsupportedOperationException("This object is immutable: |"
                + this
                + "|");
    }

    default <T> Optional<T> as(Class<T> clazz) {

        if (clazz.equals(LispObject.class)) {
            return Optional.of((T) this);
        }

        if (clazz.equals(this.getClass())) {
            return Optional.of((T) this);
        }

        if (clazz.equals(Integer.class)) {
            return (Optional<T>) this.asInt();
        }

        if (clazz.equals(Double.class)) {
            return (Optional<T>) this.asDouble();
        }

        if (clazz.equals(String.class)) {
            return (Optional<T>) Optional.of(this.toString());
        }

        if (clazz.equals(Function.class)) {
            Optional<Function> func = this.asFunction().flatMap(f -> Optional.of(f.function()));
            return (Optional<T>) func;
        }
        
        if (clazz.equals(AtomObject.class)) {
            return (Optional<T>) this.asAtom();
        }
        
        if (clazz.equals(ListObject.class)) {
            return (Optional<T>) this.asList();
        }

        return Optional.empty();
    }

    <T> T accept(LispVisitor<T> visitor);

    boolean error();

}
