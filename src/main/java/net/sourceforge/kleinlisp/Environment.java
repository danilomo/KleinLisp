/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

/**
 * @author daolivei
 */
public interface Environment {
    LispObject lookupValue(String name);

    void set(String name, LispObject obj);

    void define(String name, LispObject obj);

    void undefine(String name);

    boolean exists(String name);

    Binding lookup(String name);
}
