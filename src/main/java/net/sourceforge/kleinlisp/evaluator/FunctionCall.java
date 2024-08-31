package net.sourceforge.kleinlisp.evaluator;

import java.util.ArrayList;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author danilo
 */
public class FunctionCall implements Supplier<LispObject> {
    private final Supplier<LispObject> head;
    private final List<Supplier<LispObject>> parameters;

    public FunctionCall(Supplier<LispObject> head, List<Supplier<LispObject>> parameters) {
        this.head = head;
        this.parameters = parameters;
    }

    @Override
    public LispObject get() {
        Optional<Function> function = head
                .get()
                .asFunction()
                .flatMap(
                        f -> Optional.of(f.function())
                );

        /*final List<LispObject> params = this.parameters
                .stream()
                .map(x -> x.get())
                .collect(Collectors.toList());*/
        List<LispObject> params = new ArrayList<>();
        for (Supplier<LispObject> param: parameters) {
            LispObject obj = param.get();
            params.add(obj);
        }

        return function
                .flatMap(f -> Optional.of(f.evaluate(params)))
                .orElse(ListObject.NIL);
    }
}
