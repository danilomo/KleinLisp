package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

public class DefineForm implements Function {

    private final Environment environment;

    public DefineForm(Environment environment) {
        this.environment = environment;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        String symbol = parameters.car().asAtom().get().toString();
        LispObject obj = parameters.cdr().car().evaluate();

        this.environment.define(symbol, obj);

        return ListObject.NIL;
    }
}
