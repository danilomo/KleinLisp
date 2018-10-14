package net.sourceforge.kleinlisp;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 *
 * @author daolivei
 */
public final class StringForm implements Form {

    private final String value;

    public StringForm(String value) {
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
    public Form evaluate() {
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
    public Optional<ListForm> asList() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }
}
