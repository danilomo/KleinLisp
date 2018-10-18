/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.IntObject;

/**
 *
 * @author daolivei
 */
public class Main3 {
    public static void main(String[] args) {
        Environment env = new LispEnvironment();
        
        env.define("x", int_(10));
        env.define("y", int_(10));
        env.define("z", int_(10));
        System.out.println(env);
        
        env.set("x", int_(20));
        System.out.println(env);
        
        env.define("y", int_(30));
        System.out.println(env);
        
        env.undefine("y");
        System.out.println(env);
    }

    private static LispObject int_(int i) {
        return new IntObject(i);
    }
}
