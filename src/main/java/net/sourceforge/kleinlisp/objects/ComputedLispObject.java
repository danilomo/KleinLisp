/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.sourceforge.kleinlisp.objects;

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author danilo
 */
public class ComputedLispObject implements LispObject {

    private Supplier<LispObject> computed;

    public ComputedLispObject(Supplier<LispObject> computed) {
        this.computed = computed;
    }

    public Supplier<LispObject> getComputed() {
        return computed;
    }     
    
    @Override
    public Object asObject() {
        return computed.get().asObject();
    }

    @Override
    public boolean truthiness() {
        return computed.get().truthiness();
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return computed.get().error();
    }
    
}
