/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import java.util.List;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author Danilo Oliveira
 */
public class LambdaFunction implements Function{
    private final List<AtomObject> parameterList;
    private final LispObject body;
    private final Environment env;

    public LambdaFunction(List<AtomObject> parameterList, LispObject body, Environment env) {
        this.parameterList = parameterList;
        this.body = body;
        this.env = env;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        setParameters(parameters);
        
        LispObject result = body.evaluate();
        
        unsetParameters(parameters);
        
        return result;
    }

    private void setParameters(ListObject parameters) {
        ListObject iter = parameters;
        
        for(AtomObject atom: parameterList){
            env.define(atom.toString(), iter.car());
            iter = iter.cdr();
        }        
    }

    private void unsetParameters(ListObject parameters) {
        for(AtomObject atom: parameterList){
            env.undefine(atom.toString());
        }
    }
}
