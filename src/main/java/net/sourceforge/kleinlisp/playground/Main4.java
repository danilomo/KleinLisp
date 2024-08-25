/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author Danilo Oliveira
 */
public class Main4 {

    public static void main(String[] args) {
        String code = "(lambda (x) (lambda (y) (+ x y)))";
        Lisp lisp = new Lisp();

        FunctionObject function =  lisp.evaluate(code).asFunction().get();
        
        FunctionObject plusOne = call( function, 1).asFunction().get();
        
        FunctionObject plusOneThousand = call( function, 1000).asFunction().get();
        
        System.out.println(call(plusOne, 2));
        
        System.out.println(call(plusOneThousand, 2));
    }
    
    public static LispObject call( FunctionObject function, int arg ){
        return function.function().evaluate(new ListObject(new IntObject(arg), ListObject.NIL));
    }
}


