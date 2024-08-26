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

        return Optional.empty();
    }

    <T> T accept(LispVisitor<T> visitor);

    boolean error();

}
