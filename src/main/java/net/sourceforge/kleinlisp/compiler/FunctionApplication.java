/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functions.MiscFunctions;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author daolivei
 */
public final class FunctionApplication extends LispObjDecorator {

    private final Function function;
    private final ListObject parameterList;
    private final LispObject obj;

    public FunctionApplication(Function function, ListObject parameterList) {
        this.function = function;
        this.parameterList = parameterList;
        this.obj = ListObject.NIL;
    }

    public FunctionApplication(LispObject obj, ListObject parameterList) {
        this.function = null;
        this.parameterList = parameterList;
        this.obj = obj;
    }

    public static void main(String[] args) {
        LispObject obj = new FunctionApplication(MiscFunctions::sum, new ListObject(new IntObject(1), new ListObject(new IntObject(2))));
        System.out.println(obj.evaluate());
    }

    @Override
    public LispObject evaluate() {
        if (function != null) {
            return function.evaluate(evaluateList(parameterList));
        } else {
            return obj.evaluate().asFunction().get().function().evaluate(evaluateList(parameterList));
        }
    }

    private ListObject evaluateList(ListObject obj) {
        if (obj == ListObject.NIL) {
            return obj;
        } else {
            return new ListObject(obj.car().evaluate(), evaluateList(obj.cdr()));
        }
    }

}
