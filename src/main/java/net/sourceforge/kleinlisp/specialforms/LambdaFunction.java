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
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author Danilo Oliveira
 */
public class LambdaFunction implements Function {

    private final List<String> parameterList;
    private final ListObject body;
    private final Environment env;

    public LambdaFunction(List<String> parameterList, ListObject body, Environment env) {
        this.parameterList = parameterList;
        this.body = body;
        this.env = env;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        setParameters(parameters);

        LispObject result = ListObject.NIL;

        for (LispObject obj : body) {
            result = obj.evaluate();
        }

        unsetParameters();

        return result;
    }

    private void setParameters(ListObject parameters) {
        ListObject iter = parameters;

        for (String par : parameterList) {
            env.define(par, iter.car());
            iter = iter.cdr();
        }
    }

    private void unsetParameters() {
        for (String par : parameterList) {
            env.undefine(par);
        }
    }
}
