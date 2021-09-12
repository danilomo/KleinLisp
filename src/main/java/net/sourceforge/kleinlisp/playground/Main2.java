/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 * @author daolivei
 */
public class Main2 {

    public static void main(String[] args) {
        Lisp runtime = new Lisp();

        String code = "((lambda (x y) (+ x y)) 10 10)";

        System.out.println(runtime.evaluate(code));

//        LispObject tree = runtime.parse(code);
//
//        System.out.println(tree.accept(new UppercaseVisitor()));
    }

}

class UppercaseVisitor extends DefaultVisitor {

    @Override
    public LispObject visit(AtomObject obj) {
        return new AtomObject(obj.toString().toUpperCase());
    }

    @Override
    public LispObject visit(StringObject obj) {
        return new StringObject(obj.toString().toUpperCase());
    }

    @Override
    public LispObject visit(IntObject obj) {
        return new StringObject("" + obj.value());
    }


}
