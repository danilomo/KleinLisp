/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.evaluator;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class FunctionCall implements Supplier<LispObject>{
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
        
        final List<LispObject> params = this.parameters
                .stream()
                .map(x -> x.get())
                .collect(Collectors.toList());
        
        return function
                .flatMap(f -> Optional.of(f.evaluate(params)))
                .orElse(ListObject.NIL);
    } 
}
