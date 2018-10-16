/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author Danilo Oliveira
 */
public class ComparisonOperator implements Function {

    private final Operator operator;

    public ComparisonOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        boolean result = evaluate(parameters.car(), parameters.cdr());

        return new BooleanObject(result);
    }

    private boolean evaluate(LispObject car, ListObject cdr) {
        if (cdr == ListObject.NIL) {
            return true;
        }

        LispObject op = cdr.car();

        Object c1 = car.asObject();
        Object c2 = op.asObject();

        if (comparisonIsTrue(c1, c2)) {
            return evaluate(op, cdr.cdr());
        } else {
            return false;
        }

    }
    

    private boolean comparisonIsTrue(Object c1, Object c2) {
        switch (operator) {            
            case EQ:
                return c1.equals(c2);
            case NEQ:
                return ! c2.equals(c2);
            case LT:
                return ((Comparable)c1).compareTo(c2) < 0;
            case GT:
                return ((Comparable)c1).compareTo(c2) > 0;
            case LEQ:
                return ((Comparable)c1).compareTo(c2) <= 0;
            case GEQ:
                return ((Comparable)c1).compareTo(c2) >= 0;
            default:
                return false;
        }
    }

    public static enum Operator {
        EQ,
        NEQ,
        LT,
        GT,
        GEQ,
        LEQ
    }
}
