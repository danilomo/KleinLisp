package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public interface NumericObject extends LispObject{
    public int toInt();
    public double toDouble();
}
