package net.sourceforge.kleinlisp.special_forms;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class BeginForm  implements SpecialForm {

    private final Evaluator evaluator;

    public BeginForm(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {
        List<Supplier<LispObject>> commands = obj
                .asList()
                .get()
                .toList()
                .stream()
                .map(elem -> elem.accept(evaluator))
                .collect(Collectors.toList());
        
        return () -> {
            LispObject result = VoidObject.VOID;
            
            for (Supplier<LispObject> command: commands) {
                result = command.get();
            }
            
            return result;
        };
    }
    
}
