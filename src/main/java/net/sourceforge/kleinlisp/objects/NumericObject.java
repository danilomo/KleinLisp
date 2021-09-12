package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;

/**
 * @author daolivei
 */
public interface NumericObject extends LispObject {
    int toInt();

    double toDouble();
}
