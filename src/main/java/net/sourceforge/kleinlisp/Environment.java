/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

/**
 *
 * @author daolivei
 */
public interface Environment {
    public LispObject lookupValue(String name);
    public void set(String name, LispObject obj);
    public void define(String name, LispObject obj);
    public void undefine(String name);
    public boolean exists(String name);    
    public Binding lookup(String name);
}
