/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * @author daolivei
 */
public class CompositeEnvironment implements Environment {

    private final Environment first;
    private final Environment second;

    public CompositeEnvironment(Environment first, Environment second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public LispObject lookupValue(AtomObject name) {
        if (first.exists(name)) {
            return first.lookupValue(name);
        } else {
            return second.lookupValue(name);
        }
    }

    @Override
    public void set(AtomObject name, LispObject obj) {
        if (first.exists(name)) {
            first.set(name, obj);
        } else {
            second.set(name, obj);
        }
    }

    @Override
    public void define(AtomObject name, LispObject obj) {
        first.define(name, obj);
    }

    @Override
    public void undefine(AtomObject name) {
        if (first.exists(name)) {
            first.undefine(name);
        } else {
            second.undefine(name);
        }
    }

    @Override
    public boolean exists(AtomObject name) {
        return first.exists(name) || second.exists(name);
    }

    @Override
    public Binding lookup(AtomObject name) {
        if (first.exists(name)) {
            return first.lookup(name);
        } else {
            return second.lookup(name);
        }
    }

    @Override
    public AtomObject atomOf(String atom) {
        return first.atomOf(atom);
    }

    @Override
    public String valueOf(AtomObject atom) {
        return first.valueOf(atom);
    }

}
