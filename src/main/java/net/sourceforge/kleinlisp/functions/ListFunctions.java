/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.FunctionObject;

/**
 *
 * @author Danilo Oliveira
 */
public class ListFunctions {

    public static LispObject length(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        return new IntObject(arg.length());
    }

    public static LispObject car(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        return arg.head();
    }

    public static LispObject cdr(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        return arg.tail();
    }

    public static LispObject list(ListObject parameters) {
        return parameters;
    }
    
    public static LispObject reverse(ListObject parameters) {
        ListObject newl = ListObject.NIL;
        
        for(LispObject par: parameters){
            newl = new ListObject(par, newl);
        }
        
        return newl;
    } 
    
    public static LispObject map(ListObject parameters){
        FunctionObject func  = parameters.car().asFunction().get();
        ListObject     param = parameters.cdr().car().asList().get();
        
        ListObject newL = ListObject.NIL;
        ListObject it = param;
        
        while(it != ListObject.NIL){
            newL = new ListObject( func.call(it.car()), newL );
            it = it.cdr();
        }
        
        return ListFunctions.reverse(newL);
    }    
    
    public static LispObject filter(ListObject parameters){
        FunctionObject func  = parameters.car().asFunction().get();
        ListObject     param = parameters.cdr().car().asList().get();
        
        ListObject newL = ListObject.NIL;
        ListObject it = param;
        
        while(it != ListObject.NIL){
            if(func.call(it.car()).truthness()){
                newL = new ListObject( it.car(), newL );
            }            
            it = it.cdr();
        }
        
        return ListFunctions.reverse(newL);
    }

    public static LispObject takewhile(ListObject parameters){
        Tuple2<FunctionObject, ListObject> tuple =
                parameters.unpack(FunctionObject.class
                        , ListObject.class).get();

        return takewhile(tuple.first(), tuple.second());
    }

    private static ListObject takewhile(FunctionObject func, ListObject list) {
        if(list == ListObject.NIL){
            return ListObject.NIL;
        }

        if(func.call(list.car()).truthness()){
            return new ListObject( list.car(), takewhile( func, list.cdr() ));
        }else{
            return ListObject.NIL;
        }
    }
    
    
}
