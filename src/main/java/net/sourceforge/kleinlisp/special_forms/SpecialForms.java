/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.special_forms;

import java.util.Map;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 *
 * @author danilo
 */
public class SpecialForms {
    private final Environment enviromnent;
    private Map<AtomObject, SpecialForm> forms;

    public SpecialForms(Environment enviromnent) {
        this.enviromnent = enviromnent;
    }
    
    SpecialForms insertForm(String name,  SpecialForm form){
        forms.put(enviromnent.atomOf(name), form);
        return this;
    }
    
    
}
