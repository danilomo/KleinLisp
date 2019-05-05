/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 *
 * @author daolivei
 */
public interface Environment {
    public LispObject lookupValue(AtomObject name);
    public void set(AtomObject name, LispObject obj);
    public void define(AtomObject name, LispObject obj);
    public void undefine(AtomObject name);
    public boolean exists(AtomObject name);    
    public Binding lookup(AtomObject name);
    public AtomObject atomOf(String atom);
    public String valueOf(AtomObject atom);
}
