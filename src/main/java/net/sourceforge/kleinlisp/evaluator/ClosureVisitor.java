/*
 * MIT License
 * 
 * Copyright (c) 2018 Danilo Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        collectClosureInfo(visitor.currentFunction);
        
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
    
    private static void collectClosureInfo(LambdaMeta currentFunction) {
        
        if (currentFunction.children.isEmpty()) {
            return;
        }
        
        Set<AtomObject> symbols = new HashSet<>();
        
        for (LambdaMeta func : currentFunction.children) {
            symbols.addAll(func.getUsedSymbols());
        }
        
        Map<AtomObject, Integer> closureInfo = new HashMap<>();
        
        for (int i = 0; i < currentFunction.parameters.size(); i++) {            
            AtomObject param = currentFunction.parameters.get(i);
            
            if (!symbols.contains(param)) {
                continue;
            }
            
            closureInfo.put(param, i);
        }
        
        for (AtomObject param: symbols) {
            if (closureInfo.containsKey(param)) {
                continue;
            }
            
            closureInfo.put(param, -1);
        }
        
        currentFunction.closureInfo = closureInfo;
        
        for (LambdaMeta func : currentFunction.children) {
            collectClosureInfo(func);
        }            
    }

    public static class LambdaMeta {

        private List<AtomObject> parameters;
        private Set<AtomObject> symbols = new HashSet<>();
        private List<LambdaMeta> children = new ArrayList<>();
        private LambdaMeta parent;
        private Map<AtomObject, Integer> closureInfo = Collections.emptyMap();
        private String repr;
        
        public LambdaMeta(List<AtomObject> parameters) {
            this.parameters = parameters;           
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

        public Map<AtomObject, Integer> getClosureInfo() {
            return closureInfo;
        }   

        public LambdaMeta getParent() {
            return parent;
        }                

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("LambdaMeta{");
            sb.append("parameters=").append(parameters);
            sb.append(", symbols=").append(symbols);
            sb.append(", closureInfo=").append(closureInfo);
            sb.append('}');
            return sb.toString();
        }

        public String getRepr() {
            return repr;
        }                

    }

    Set<AtomObject> definedSymbols = new HashSet<>();
    LambdaMeta currentFunction = new LambdaMeta(Collections.emptyList());

    @Override
    public LispObject visit(ListObject obj) {
        if (obj == ListObject.NIL) {
            return obj;
        }

        AtomObject head = obj.car().asAtom();

        if (head == null) {
            return super.visit(obj);
        }

        if (head.specialForm() != SpecialFormEnum.LAMBDA) {
            return super.visit(obj);
        }

        List<AtomObject> parameters = new ArrayList<>();
        ListObject paramList = obj.cdr().car().asList();
        for (LispObject param : paramList) {
            AtomObject atom = param.asAtom();
            parameters.add(atom);
            definedSymbols.add(atom);
        }

        LambdaMeta newFunction = new LambdaMeta(parameters);
        newFunction.repr = obj.toString();
        currentFunction.children.add(newFunction);
        newFunction.parent = currentFunction;
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
