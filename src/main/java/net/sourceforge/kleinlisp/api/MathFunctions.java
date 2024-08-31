package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;

import java.util.List;

public class MathFunctions {
    public static LispObject add(List<LispObject> params) {
        int sum = 0;
        for (LispObject i : params) {            
            sum += i.asInt().get();
        }
        return new IntObject(sum);
    }

    public static LispObject sub(List<LispObject> params) {
        boolean first = true;
        int sum = 0;

        for (LispObject i : params) {
            if (first) {
                first = false;
                sum = i.asInt().get();
            } else {
                sum -= i.asInt().get();
            }
        }
        return new IntObject(sum);
    }

    public static LispObject mul(List<LispObject> params) {
        int prod = 1;

        for (LispObject i : params) {
            prod *= i.asInt().get();
        }
        return new IntObject(prod);
    }

    public static LispObject div(List<LispObject> params) {
        int prod = 1;
        boolean first = true;

        for (LispObject i : params) {
            if (first) {
                first = false;
                prod = i.asInt().get();
            } else {
                prod /= i.asInt().get();
            }
        }
        return new IntObject(prod);
    }

    public static LispObject mod(List<LispObject> params) {
        int prod = 1;

        for (LispObject i : params) {
            prod %= i.asInt().get();
        }
        return new IntObject(prod);
    }

    public static LispObject lt(List<LispObject> params) {
        Integer i1 = params.get(0).asInt().get();
        Integer i2 = params.get(1).asInt().get();

        return new BooleanObject(i1 < i2);
    }
}
