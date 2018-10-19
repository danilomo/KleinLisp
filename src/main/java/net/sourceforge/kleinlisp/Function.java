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
    
    public default LispObject apply(LispObject l1){
        return this.evaluate( new ListObject(l1 ));
    }
    
    public default LispObject apply(LispObject l1, LispObject l2) {
        return this.evaluate( new ListObject(l1, new ListObject(l2) ));
    }    
}
