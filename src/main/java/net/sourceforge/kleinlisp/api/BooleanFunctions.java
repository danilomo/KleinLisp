package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;

import java.util.List;

public class BooleanFunctions {

    public static LispObject lt(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left < right);
    }

    public static LispObject le(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left <= right);
    }

    public static LispObject gt(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left > right);
    }

    public static LispObject ge(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left >= right);
    }

    public static LispObject eq(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left == right);
    }

    public static LispObject neq(List<LispObject> params) {
        int left = params.get(0).asInt().get();
        int right = params.get(1).asInt().get();

        return new BooleanObject(left != right);
    }

}
