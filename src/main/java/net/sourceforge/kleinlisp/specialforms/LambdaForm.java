/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public class LambdaForm implements Function {

    private final Environment environment;

    public LambdaForm(Environment environment) {
        this.environment = environment;
    }        

    @Override
    public LispObject evaluate(ListObject parameters) {
        ListObject paramList = parameters.car().asList().get();
        ListObject body = parameters.cdr().car().asList().get();
        
        List<AtomObject> parameterList = new ArrayList<>();
        
        for(LispObject obj: paramList){
            parameterList.add(obj.asAtom().get());
        }
        
        return new FunctionObject(new LambdaFunction(parameterList, body, environment));
    }

}
