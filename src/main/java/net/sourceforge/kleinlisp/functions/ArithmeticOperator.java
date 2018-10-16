/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author Danilo Oliveira
 */
public class ArithmeticOperator implements Function {

    private final Operator operator;

    public ArithmeticOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        return evaluate(parameters.car(), parameters.cdr());
    }

    private LispObject evaluate(LispObject car, ListObject cdr) {
        if (cdr == ListObject.NIL) {
            return car;
        }

        LispObject op = cdr.car();

        if (car instanceof DoubleObject || op instanceof DoubleObject) {
            LispObject result = evaluateDouble(car, op);

            return evaluate(result, cdr.cdr());
        } else {
            LispObject result = evaluateInt(car, op);

            return evaluate(result, cdr.cdr());
        }
    }

    private LispObject evaluateDouble(LispObject a, LispObject b) {
        double aval = a.asDouble().get();
        double bval = b.asDouble().get();

        switch (operator) {
            case PLUS:
                return new DoubleObject(aval + bval);
            case MINUS:
                return new DoubleObject(aval - bval);
            case TIMES:
                return new DoubleObject(aval * bval);
            case DIV:
                return new DoubleObject(aval / bval);
            case MOD:
                return new DoubleObject(aval % bval);
        }

        return new DoubleObject(Double.NaN);
    }

    private LispObject evaluateInt(LispObject a, LispObject b) {
        int aval = a.asInt().get();
        int bval = b.asInt().get();

        switch (operator) {
            case PLUS:
                return new IntObject(aval + bval);
            case MINUS:
                return new IntObject(aval - bval);
            case TIMES:
                return new IntObject(aval * bval);
            case DIV:
                return new IntObject(aval / bval);
            case MOD:
                return new IntObject(aval % bval);
        }

        return new IntObject(Integer.MIN_VALUE);
    }

    public static enum Operator {
        PLUS,
        MINUS,
        DIV,
        TIMES,
        MOD
    }
}
