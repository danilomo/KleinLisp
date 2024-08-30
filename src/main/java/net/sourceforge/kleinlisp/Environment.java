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
public interface Environment {
    LispObject lookupValue(AtomObject name);

    void set(AtomObject name, LispObject obj);

    boolean exists(AtomObject name);
}
