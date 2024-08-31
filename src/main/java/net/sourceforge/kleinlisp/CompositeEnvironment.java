package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.AtomObject;

public class CompositeEnvironment implements Environment {
    private final Environment left;
    private final Environment right;

    public CompositeEnvironment(Environment left, Environment right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public LispObject lookupValue(AtomObject name) {
        if (left.exists(name)) {
            return left.lookupValue(name);
        }
        
        return right.lookupValue(name);
    }

    @Override
    public void set(AtomObject name, LispObject obj) {
        left.set(name, obj);
    }

    @Override
    public boolean exists(AtomObject name) {
        return left.exists(name) || right.exists(name);
    }

    @Override
    public String toString() {
        return "CompositeEnvironment{" + "left=" + left + ", right=" + right + '}';
    }
    
    
}