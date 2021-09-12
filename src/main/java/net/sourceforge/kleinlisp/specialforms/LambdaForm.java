/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.*;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author daolivei
 */
public class LambdaForm implements Function {

    private final Environment environment;
    private final String name;

    public LambdaForm(Environment environment) {
        this.environment = environment;
        this.name = "";
    }

    public LambdaForm(Environment environment, String name) {
        this.environment = environment;
        this.name = name;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        ListObject paramList = parameters.car().asList().get();
        ListObject body = parameters.cdr().asList().get();

        List<String> parameterList = new ArrayList<>();

        for (LispObject obj : paramList) {
            parameterList.add(obj.asAtom().get().toString());
        }

        Environment closuredEnv = createClosure(parameterList, body, environment);

        WithNewEnvironment visitor = new WithNewEnvironment(closuredEnv);

        body = body.accept(visitor).asList().get();

        return new FunctionObject(new LambdaFunction(parameterList, body, environment));
    }

    private Environment createClosure(List<String> paramList
            , ListObject body, Environment env) {

        Set<String> defined = new DefinedSymbolCollector().symbols(body);
        Set<String> used = new UsedSymbolCollector().symbols(body);

        if (!name.isEmpty()) {
            defined.add(name);
            used.remove(name);
        }

        paramList.forEach((ao) -> {
            defined.add(ao);
        });

        Set<String> diff = new HashSet<>();

        for (String s : used) {
            if (!defined.contains(s)) {
                diff.add(s);
            }
        }

        Environment newEnv = new LispEnvironment();

        for (String symbol : diff) {
            LispObject obj = env.lookupValue(symbol);
            newEnv.define(symbol, obj);
        }

        return new CompositeEnvironment(newEnv, env);
    }

    static class UsedSymbolCollector extends DefaultVisitor {

        private final Set<String> symbols = new HashSet<>();

        @Override
        public LispObject visit(AtomObject obj) {
            symbols.add(obj.toString());
            return obj;
        }

        public Set<String> symbols(LispObject obj) {
            obj.accept(this);
            symbols.remove("lambda");
            symbols.remove("set!");
            symbols.remove("begin");
            symbols.remove("cond");
            symbols.remove("if");
            return symbols;
        }

    }

    static class DefinedSymbolCollector extends DefaultVisitor {

        private final Set<String> symbols = new HashSet<>();

        @Override
        public LispObject visit(ListObject obj) {
            if (!obj.head().asAtom().isPresent()) {
                return super.visit(obj);
            }

            String symbol = obj.head().asAtom().get().toString();

            if (!"lambda".equals(symbol)) {
                return super.visit(obj);
            }

            ListObject parameters = obj.cdr().car().asList().get();

            for (LispObject par : parameters) {
                symbols.add(par.toString());
            }

            return super.visit(obj.cdr().cdr());
        }

        public Set<String> symbols(LispObject obj) {
            obj.accept(this);
            return symbols;
        }
    }

    static class WithNewEnvironment extends DefaultVisitor {
        private final Environment environment;

        public WithNewEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public LispObject visit(AtomObject obj) {
            return new AtomObject(obj.toString(), environment);
        }

    }

}
