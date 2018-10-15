/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.forms.DoubleForm;
import net.sourceforge.kleinlisp.Form;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.forms.IntForm;
import net.sourceforge.kleinlisp.forms.ListForm;

/**
 *
 * @author daolivei
 */
public class ArithmeticOperator implements Function {

    private final Operator operator;

    public ArithmeticOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public Form evaluate(ListForm parameters) {
        return evaluate(parameters.car(), parameters.cdr());
    }

    private Form evaluate(Form car, ListForm cdr) {
        if (cdr == ListForm.NIL) {
            return car;
        }

        Form op = cdr.car();

        if (car instanceof DoubleForm || op instanceof DoubleForm) {
            Form result = evaluateDouble(car, op);

            return evaluate(result, cdr.cdr());
        } else {
            Form result = evaluateInt(car, op);

            return evaluate(result, cdr.cdr());
        }
    }

    private Form evaluateDouble(Form a, Form b) {
        double aval = a.asDouble().get();
        double bval = b.asDouble().get();

        switch (operator) {
            case PLUS:
                return new DoubleForm(aval + bval);
            case MINUS:
                return new DoubleForm(aval - bval);
            case TIMES:
                return new DoubleForm(aval * bval);
            case DIV:
                return new DoubleForm(aval / bval);
            case MOD:
                return new DoubleForm(aval % bval);
        }

        return new DoubleForm(Double.NaN);
    }

    private Form evaluateInt(Form a, Form b) {
        int aval = a.asInt().get();
        int bval = b.asInt().get();

        switch (operator) {
            case PLUS:
                return new IntForm(aval + bval);
            case MINUS:
                return new IntForm(aval - bval);
            case TIMES:
                return new IntForm(aval * bval);
            case DIV:
                return new IntForm(aval / bval);
            case MOD:
                return new IntForm(aval % bval);
        }

        return new IntForm(Integer.MIN_VALUE);
    }

    public static enum Operator {
        PLUS,
        MINUS,
        DIV,
        TIMES,
        MOD
    }
}
