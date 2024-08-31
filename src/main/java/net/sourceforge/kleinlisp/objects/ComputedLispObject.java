package net.sourceforge.kleinlisp.objects;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author danilo
 */
public class ComputedLispObject implements LispObject {

    private Supplier<LispObject> getter;
    private Consumer<LispObject> setter;
    private LispObject cache = null;

    public ComputedLispObject(Supplier<LispObject> getter, Consumer<LispObject> setter) {
        this.getter = getter;        
        this.setter = setter;
    }
    
    public ComputedLispObject(Supplier<LispObject> getter) {
        this(getter, null);
    }

    public Supplier<LispObject> getComputed() {
        return getter;
    }     
    
    @Override
    public Object asObject() {
        return getObj().asObject();
    }

    @Override
    public boolean truthiness() {
        return getObj().truthiness();
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
    @Override
    public void set(LispObject value) {
        setter.accept(value);
    }

    @Override
    public boolean error() {
        return getObj().error();
    }

    private LispObject getObj() {
        if (setter != null) {
            return getter.get();
        }
        
        if (cache == null) {
            cache = getter.get();
        }
        
        return cache;
    }
    
    
}
