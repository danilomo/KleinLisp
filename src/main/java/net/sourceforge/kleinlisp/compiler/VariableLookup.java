/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public class VariableLookup extends LispObjDecorator{
    public String name;
    public Environment environment;

    public VariableLookup(String name, Environment environment) {
        this.name = name;
        this.environment = environment;
    }

    @Override
    public LispObject evaluate() {
        return environment.lookupValue(name);
    }

    
}
