/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import java.util.List;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;

/**
 *
 * @author danilo
 */
public class TestParser {
    public static void main(String[] args) {
        Lisp l = new Lisp();
        
        AtomObject a = l.environment().atomOf("+");
        l.environment().define(a, new FunctionObject(TestParser::add));
        
        LispObject obj = l.parse("(+ 1 2 3)");
        
        Evaluator eval = new Evaluator(l.environment());
        
        LispObject res = eval.evaluate(obj);
        
        System.out.println(res);
    }
    
    public static LispObject add(List<LispObject> params){
        int sum = 0;
        
        System.out.println(">>> "+ params);
        
        for(LispObject i: params){
            sum += i.asInt().get();
        }
        
        return new IntObject(sum);
    }
}
