package net.sourceforge.kleinlisp.forms;

import net.sourceforge.kleinlisp.Form;
import java.util.Optional;

/**
 *
 * @author daolivei
 */
public class ObjectForm implements Form {

    private final Object object;

    public ObjectForm(Object object) {
        this.object = object;
    }

    public Object object() {
        return object;
    }

    @Override
    public String toString() {
        return "Object: " + object.toString();
    }

    @Override
    public Object asObject() {
        return object;
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
        if (clazz.isAssignableFrom(object.getClass())) {
            return Optional.of((T) object);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean truthness() {
        return true;
    }

    @Override
    public Optional<FunctionForm> asFunction() {
        return Optional.empty();
    }
}
