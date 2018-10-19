/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import java.util.Optional;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.functional.Tuple3;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public class Main7 {
    public static void main(String[] args) {
        Lisp lisp = new Lisp();
        
        
        Optional<ListObject> list = lisp.evaluate("[1 2 \"3\"]").asList();
        
        Optional<Tuple3<Integer,Integer,String>> tuple = list.flatMap(
                t -> t.unpack(Integer.class, Integer.class, String.class) 
        );
        
        System.out.println(tuple.get());
        
        
    }
}
