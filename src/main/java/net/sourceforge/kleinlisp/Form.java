package net.sourceforge.kleinlisp;

import java.util.Optional;

/**
 * A Lisp "form", i.e., something that can be evaluated in Lisp programs: atoms,
 * strings, number literals, lists, etc.
 *
 * @author Danilo Oliveira
 */
public interface Form {

    /**
     * Return the form as a Java object, applying some conversion if necessary.
     *
     * AtomForm, StringForm -> String, IntForm -> Integer, DoubleForm -> Double,
     * ListForm -> java.util.List, etc.
     *
     * @return A java.lang.Object according to the Form concrete type
     */
    public Object asObject();

    /**
     * If the form represents a numeric value, returns Optional.of(number),
     * otherwise, return an Optional.empty().
     *
     * For DoubleForm, the value is coerced to int.
     *
     * @return see description
     */
    public Optional<Integer> asInt();

    /**
     * If the form represents a numeric value, returns Optional.of(number),
     * otherwise, return an Optional.empty().
     *
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    public Optional<Double> asDouble();

    /**
     * If the form represents a list, returns an Optional.of(list), otherwise,
     * return an Optional.empty().
     *
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    public Optional<ListForm> asList();

    /**
     * Returns an Optional.of(reference) if it is an ObjectForm belonging to
     * the specified type, otherwise, returns an Optional.empty().
     *
     * @param <T> The expected type
     * @param clazz The Class reference for the T type
     * @return see description
     */
    public <T> Optional<T> asObject(Class<T> clazz);

    /**
     * Evaluates the form. 
     * 
     * Literals and atoms are evaluated to themselves, ListForms are evaluated
     * according the Lisp semantics: the first element should be a symbol in the
     * function environment. All parameters are evaluated before invoking the
     * function, in according to the expected Lisp behavior.
     *
     * @return An evaluated form.
     */
    public Form evaluate();
}
