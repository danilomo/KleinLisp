package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author danilo
 */
public class CellObject implements LispObject {

    private LispObject object;
    
    @Override
    public Object asObject() {
        return object.asObject();
    }

    @Override
    public boolean truthiness() {
        return object.truthiness();
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Optional<Integer> asInt() {
        return object.asInt();
    }

    @Override
    public Optional<Double> asDouble() {
        return object.asDouble();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return object.asAtom();
    }

    @Override
    public Optional<ListObject> asList() {
        return object.asList();
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return object.asFunction();
    }

    @Override
    public <T> Optional<T> as(Class<T> clazz) {
        return object.as(clazz);
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return object.asObject(clazz);
    } 
    
    @Override
    public Optional<CellObject> asCell() {
        return Optional.of(this);
    }
    
    public void set(LispObject obj) {
        object = obj;
    }
    
    public LispObject get() {
        return object;
    }

    @Override
    public boolean error() {
        return false;
    }
    
}
