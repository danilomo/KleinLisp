package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.FunctionObject;

import java.util.List;

/**
 *
 * @author Danilo Oliveira
 */
public class ListFunctions {

    public static LispObject length(List<LispObject> params) {
        ListObject arg = params.get(0).asList().get();

        return new IntObject(arg.length());
    }

    public static LispObject car(List<LispObject> params) {
        ListObject arg = params.get(0).asList().get();

        return arg.head();
    }

    public static LispObject cdr(List<LispObject> params) {
        ListObject arg = params.get(0).asList().get();

        return arg.tail();
    }

    public static LispObject list(List<LispObject> params) {
        return ListObject.fromList(params);
    }

    public static LispObject reverse(ListObject parameters) {
        ListObject newl = ListObject.NIL;

        for(LispObject par: parameters){
            newl = new ListObject(par, newl);
        }

        return newl;
    }

    /*public static LispObject map(ListObject parameters){
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
    }*/

}
