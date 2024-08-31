package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.*;

/**
 * @param <T>
 * @author daolivei
 */
public interface LispVisitor<T> {
    Void NONE = new Void();

    T visit(AtomObject obj);

    T visit(BooleanObject obj);

    T visit(DoubleObject obj);

    T visit(IntObject obj);

    T visit(JavaObject obj);

    T visit(ListObject obj);

    T visit(StringObject obj);

    T visit(FunctionObject obj);

    T visit(ErrorObject obj);

    T visit(VoidObject obj);
    
    T visit(ComputedLispObject obj);
    
    T visit(CellObject obj);

    class Void {
    }
}
