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
public class Binding {

    private LispObject value;

    public Binding(LispObject value) {
        this.value = value;
    }

    public LispObject value() {
        return value;
    }

    public void set(LispObject value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
