package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.List;

/**
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

        for (LispObject par : parameters) {
            newl = new ListObject(par, newl);
        }

        return newl;
    }

}
