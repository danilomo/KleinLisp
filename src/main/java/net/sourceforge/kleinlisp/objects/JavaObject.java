package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

import java.util.Optional;

/**
 * @author daolivei
 */
public final class JavaObject implements LispObject {

    private final Object object;

    public JavaObject(Object object) {
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
    public LispObject evaluate() {
        return this;
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
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return false;
    }

}
