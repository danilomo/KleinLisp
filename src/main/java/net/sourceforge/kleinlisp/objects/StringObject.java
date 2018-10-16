package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public final class StringObject implements LispObject {

    private final String value;

    public StringObject(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public Object asObject() {
        return value;
    }

    @Override
    public LispObject evaluate() {
        return this;
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    @Override
    public Optional<ListObject> asList() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public boolean truthness() {
        return !value.isEmpty();
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }
    
    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }     
}
