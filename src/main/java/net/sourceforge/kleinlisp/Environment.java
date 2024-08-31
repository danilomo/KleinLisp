package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * @author daolivei
 */
public interface Environment {
    LispObject lookupValue(AtomObject name);

    void set(AtomObject name, LispObject obj);

    boolean exists(AtomObject name);
}
