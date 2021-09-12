package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;

import java.util.Optional;

/**
 * @author Danilo Oliveira
 */
public class SpecialForm {
    public static Optional<Function> of(String str, Environment env) {
        switch (str) {
            case "quote":
                return Optional.of(QuoteForm.instance());
            case "if":
                return Optional.of(IfForm.instance());
            case "lambda":
                return Optional.of(new LambdaForm(env));
            case "set!":
                return Optional.of(new SetForm(env));
            case "begin":
                return Optional.of(BeginForm.instance());
            case "define":
                return Optional.of(new DefineForm(env));
            case "cond":
                return Optional.of(CondForm.instance());
        }

        return Optional.empty();
    }
}
