/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import java.util.ArrayList;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.compiler.CompareTwo;
import net.sourceforge.kleinlisp.compiler.FunctionApplication;
import net.sourceforge.kleinlisp.compiler.IfForm;
import net.sourceforge.kleinlisp.compiler.OrForm;
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
public class Main8 {

    public static void main(String[] args) {
        
        double x = System.currentTimeMillis();
        
        Lisp lisp = new Lisp();

        Environment env = lisp.environment();
        env.define("n", literal(1));

        LispObject test = new OrForm(
                new CompareTwo(lookup(env, "n"), literal(0)),
                new CompareTwo(lookup(env, "n"), literal(1))
        );

        LispObject f1 = new SubtractTwo(lookup(env, "n"), literal(1));
        LispObject f2 = new SubtractTwo(lookup(env, "n"), literal(2));

        f1 = functionApplication(lookup(env, "fib"), f1);
        f2 = functionApplication(lookup(env, "fib"), f2);

        LispObject elseForm = new SumTwo(f1, f2);

        LispObject ifForm = new IfForm(
                test,
                literal(1),
                elseForm
        );

        ArrayList<String> pl = new ArrayList<>();
        pl.add("n");

        LambdaFunction l = new LambdaFunction(pl, new ListObject(ifForm), env);
        
        

        env.define("fib", new FunctionObject(l));
        
        double y = System.currentTimeMillis();
        
        y = (y - x)/1000.0;
        
        System.out.println(">> " + y);
         
        for (int i = 0; i < 30; i++) {
            
            long l1 = System.currentTimeMillis();
            lisp.evaluate("(fib 34)");
            long l2 = System.currentTimeMillis();
            
            double l3 = (l2 - l1) / 1000.0;
            
            System.out.println(l3 + ", ");
        }

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
