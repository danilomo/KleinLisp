/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.sourceforge.kleinlisp.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;

/**
 *
 * @author danilo
 */
public class ClosureVisitor extends DefaultVisitor {

    public static LispObject addClosureMeta(LispObject obj) {
        ClosureVisitor visitor = new ClosureVisitor();
        LispObject proc = obj.accept(visitor);
        
        collectIndirectUsedSymbols(visitor.currentFunction);
        
        return proc;
    }

    private static void collectIndirectUsedSymbols(LambdaMeta currentFunction) {
        for (LambdaMeta func : currentFunction.children) {
            collectIndirectUsedSymbols(func);
        }

        for (LambdaMeta func : currentFunction.children) {
            for (AtomObject symbol : func.symbols) {
                if (currentFunction.parameters.contains(symbol)) {
                    continue;
                }

                currentFunction.symbols.add(symbol);
            }
        }
    }

    public static class LambdaMeta {

        private final Map<AtomObject, Integer> positions = new HashMap<>();
        private List<AtomObject> parameters;
        private Set<AtomObject> symbols = new HashSet<>();
        private List<LambdaMeta> children = new ArrayList<>();
        
        public LambdaMeta(List<AtomObject> parameters) {
            this.parameters = parameters;
            
            for (int i = 0; i < parameters.size(); i++) {
                positions.put(parameters.get(i), i);
            }
        }

        public List<LambdaMeta> getChildren() {
            return children;
        }

        public List<AtomObject> getParameters() {
            return parameters;
        }

        public Set<AtomObject> getUsedSymbols() {
            return symbols;
        }
        
        public int parameterPos(AtomObject obj) {
            return positions.get(obj);
        }

        @Override
        public String toString() {
            return "LambdaFunction{" + "parameters=" + parameters + ", symbols=" + symbols + '}';
        }                

    }

    Set<AtomObject> definedSymbols = new HashSet<>();
    LambdaMeta currentFunction = new LambdaMeta(Collections.emptyList());

    @Override
    public LispObject visit(ListObject obj) {
        if (obj == ListObject.NIL) {
            return obj;
        }

        AtomObject head = obj.car().asAtom().orElse(null);

        if (head == null) {
            return super.visit(obj);
        }

        if (head.specialForm() != SpecialFormEnum.LAMBDA) {
            return super.visit(obj);
        }

        List<AtomObject> parameters = new ArrayList<>();
        ListObject paramList = obj.cdr().car().asList().get();
        for (LispObject param : paramList) {
            AtomObject atom = param.asAtom().get();
            parameters.add(atom);
            definedSymbols.add(atom);
        }

        LambdaMeta newFunction = new LambdaMeta(parameters);
        currentFunction.children.add(newFunction);
        LambdaMeta temp = currentFunction;
        currentFunction = newFunction;

        ListObject result = (ListObject) super.visit(obj);

        for (AtomObject param : currentFunction.parameters) {
            currentFunction.symbols.remove(param);
        }

        result.setMeta(currentFunction);

        currentFunction = temp;

        return result;
    }

    @Override
    public LispObject visit(AtomObject obj) {
        if (!definedSymbols.contains(obj)) {
            return obj;
        }

        if (obj.specialForm() == SpecialFormEnum.NONE) {
            currentFunction.symbols.add(obj);
        }

        return obj;
    }
}
