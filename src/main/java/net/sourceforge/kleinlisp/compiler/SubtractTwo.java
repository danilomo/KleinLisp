/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.IntObject;

/**
 * @author danilo
 */
public class SubtractTwo extends LispObjDecorator {

    private final LispObject int1;
    private final LispObject int2;

    public SubtractTwo(LispObject int1, LispObject int2) {
        this.int1 = int1;
        this.int2 = int2;
    }

    @Override
    public LispObject evaluate() {
        IntObject i1 = (IntObject) int1.evaluate();
        IntObject i2 = (IntObject) int2.evaluate();

        int result = i1.value - i2.value;

        return new IntObject(result);
    }

}
