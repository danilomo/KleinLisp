/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author danilo
 */
public class OrForm extends LispObjDecorator{
    private LispObject obj1;
    private LispObject obj2;

    public OrForm(LispObject obj1, LispObject obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }   
    
    @Override
    public LispObject evaluate() {
        LispObject o1 = obj1.evaluate();
        if(o1.truthness()){
            return o1;
        }
        
        LispObject o2 = obj2.evaluate();
        
        return o2;
    }
}
