/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.compiler.CompareTwo;
import net.sourceforge.kleinlisp.compiler.CondForm;
import net.sourceforge.kleinlisp.compiler.FunctionApplication;
import net.sourceforge.kleinlisp.compiler.SubtractTwo;
import net.sourceforge.kleinlisp.compiler.SumTwo;
import net.sourceforge.kleinlisp.compiler.VariableLookup;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.specialforms.LambdaFunction;

/**
 *
 * @author daolivei
 */
public class Main7 {

    public static void main(String[] args) {
        Lisp lisp = new Lisp();

//        lisp.evaluate("(define else 1)");
//        lisp.evaluate("(define (fib n) (cond ((= n 0) 1) ((= n 1) 1) (else(+ (fib (- n 1))(fib (- n 2))))))");                
//
//        System.out.println(lisp.evaluate("(fib 34)"));
        Environment env = lisp.environment();
        env.define("n", literal(1));

//        Function eq = lisp.environment().lookupValue("=").asFunction().get().function();
        Function plus = lisp.environment().lookupValue("+").asFunction().get().function();
        Function minus = lisp.environment().lookupValue("-").asFunction().get().function();

        List<Tuple2<LispObject, LispObject>> list = new ArrayList<>();
        
//        list.add(tuple(functionApplication(eq, lookup(env, "n"), literal(0)), literal(1)));
//        list.add(tuple(functionApplication(eq, lookup(env, "n"), literal(1)), literal(1)));
        list.add(tuple(new CompareTwo(lookup(env,"n"), literal(0)), literal(1)));
        list.add(tuple(new CompareTwo(lookup(env,"n"), literal(1)), literal(1)));
        
//        LispObject f1 = functionApplication(minus, lookup(env, "n"), literal(1));
//        LispObject f2 = functionApplication(minus, lookup(env, "n"), literal(2));
//        
//        f1 = functionApplication(lookup(env, "fib"), f1);
//        f2 = functionApplication(lookup(env, "fib"), f2);

        LispObject f1 = new SubtractTwo(lookup(env, "n"), literal(1));
        LispObject f2 = new SubtractTwo(lookup(env, "n"), literal(2));
        
        f1 = functionApplication(lookup(env, "fib"), f1);
        f2 = functionApplication(lookup(env, "fib"), f2);
        
//        LispObject elseClause = functionApplication(plus, f1, f2);
        LispObject elseClause = new SumTwo(f1, f2);
        
        CondForm cond = new CondForm(list, elseClause);
        
        ArrayList<String> pl = new ArrayList<>();
        pl.add("n");
        
        LambdaFunction l = new LambdaFunction(pl, new ListObject(cond), env);
        
        env.define("fib", new FunctionObject(l));

        System.out.println(lisp.evaluate("(fib 34)"));

    }

    public static LispObject functionApplication(Function f, LispObject par1, LispObject par2) {
        return new FunctionApplication(f, new ListObject(par1, new ListObject(par2)));
    }
    
    public static LispObject functionApplication(LispObject f, LispObject par1) {
        return new FunctionApplication(f, new ListObject(par1));
    }    

    public static LispObject lookup(Environment env, String str) {
        return new VariableLookup(str, env);
    }

    public static LispObject literal(int i) {
        return new IntObject(i);
    }

    public static Tuple2<LispObject, LispObject> tuple(LispObject f, LispObject s) {
        return new Tuple2<>(f, s);
    }

}
