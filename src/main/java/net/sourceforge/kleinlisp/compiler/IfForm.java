/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public class IfForm extends LispObjDecorator{
    private final LispObject test;
    private final LispObject ifTrue;
    private final LispObject ifFalse;

    public IfForm(LispObject test, LispObject ifTrue, LispObject ifFalse) {
        this.test = test;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public LispObject evaluate() {
        if(test.evaluate().truthness()){
            return ifTrue.evaluate();
        }else{
            return ifFalse.evaluate();
        }
    }
}
