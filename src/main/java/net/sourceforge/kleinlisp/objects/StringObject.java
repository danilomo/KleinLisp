package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
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
    public boolean truthiness() {
        return !value.isEmpty();
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
