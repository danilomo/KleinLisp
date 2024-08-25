package net.sourceforge.kleinlisp.specialforms;

import java.util.Optional;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ErrorObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

public class DefineForm implements Function {

    private final Environment environment;

    public DefineForm(Environment environment) {
        this.environment = environment;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        Optional<Tuple2<AtomObject, LispObject>> tuple1 = parameters.unpack(AtomObject.class, LispObject.class);

        if (tuple1.isPresent()) {
            defineSymbol(tuple1.get().first(), tuple1.get().second());
        } else {
            if (parameters.car() instanceof ListObject) {
                return defineFunction(parameters.car().asList().get(), parameters.cdr());
            }
        }

        return VoidObject.VOID;
    }

    private void defineSymbol(AtomObject first, LispObject second) {
        String s = first.toString();
        LispObject obj = second.evaluate();

        this.environment.define(s, obj);
    }

    private LispObject defineFunction(ListObject first, ListObject body) {
        Optional<AtomObject> symbol = first.car().as(AtomObject.class);

        if (symbol.isPresent()) {
            String s = symbol.get().toString();

            LambdaForm form = new LambdaForm(this.environment, s);
            ListObject parameterList = first.cdr();
            ListObject paramLambda = new ListObject(parameterList, body);

            LispObject obj = form.evaluate(paramLambda);
            this.environment.define(s, obj);

            return VoidObject.VOID;
        } else {
            return new ErrorObject("Invalid parameter list");
        }
    }
}
