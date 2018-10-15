package net.sourceforge.kleinlisp.forms;

import net.sourceforge.kleinlisp.Form;

/**
 *
 * @author daolivei
 */
public interface NumericForm extends Form{
    public int toInt();
    public double toDouble();
}
