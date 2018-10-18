package net.sourceforge.kleinlisp;

import java.util.Optional;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * A Lisp object, i.e., something that can be evaluated in Lisp programs: atoms,
 * strings, number literals, lists, etc.
 *
 * @author Danilo Oliveira
 */
public interface LispObject {

    /**
     * Return the form as a Java object, applying some conversion if necessary.
     *
     * AtomForm, StringForm -> String, IntForm -> Integer, DoubleForm -> Double,
     * ListObject -> java.util.List, etc.
     *
     * @return A java.lang.Object according to the LispObject concrete type
     */
    public Object asObject();

    /**
     * Return the boolean value corresponding to the object.
     *
     * Nil (empty list) -> False, 0 -> False, False -> False, "" -> False,
     * everything else -> true
     */
    public boolean truthness();

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
    public Optional<ListObject> asList();

    /**
     * If the form represents a list, returns an Optional.of(list), otherwise,
     * return an Optional.empty().
     *
     * For IntForm, the value is promoted to int.
     *
     * @return see description
     */
    public Optional<FunctionObject> asFunction();

    /**
     * @return see description
     */
    public Optional<AtomObject> asAtom();

    /**
     * Returns an Optional.of(reference) if it is an ObjectForm belonging to the
     * specified type, otherwise, returns an Optional.empty().
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
    public LispObject evaluate();

    public <T> T accept(LispVisitor<T> visitor);
}
