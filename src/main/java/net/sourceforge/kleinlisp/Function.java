package net.sourceforge.kleinlisp;

import java.util.List;

/**
 * @author daolivei
 */
public interface Function {
    LispObject evaluate(List<LispObject> parameters);
}
