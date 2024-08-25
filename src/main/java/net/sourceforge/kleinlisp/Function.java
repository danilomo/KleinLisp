/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public interface Function {
    public LispObject evaluate(ListObject parameters);    
}
