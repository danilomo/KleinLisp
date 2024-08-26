package net.sourceforge.kleinlisp.api;

import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class IOFunctions {
    
    public static LispObject print(List<LispObject> params) {
        String asString = params
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        System.out.print(asString);
        return VoidObject.VOID;
    }
    
    public static LispObject println(List<LispObject> params) {
        String asString = params
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        System.out.println(asString);
        return VoidObject.VOID;
    }
}
