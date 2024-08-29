package net.sourceforge.kleinlisp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class ClosureTest {

    private void dfs(LambdaFunction currentFunction, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("   ");
        }
        
        System.out.println(currentFunction.parameters + ", " + currentFunction.symbols);
        for (LambdaFunction func: currentFunction.children) {
            dfs(func, level + 1);
        }
        
    }
   
    
    private static class LambdaFunction {
        
        private List<AtomObject> parameters;
        private Set<AtomObject> symbols = new HashSet<>();
        private List<LambdaFunction> children = new ArrayList<>();

        public LambdaFunction(List<AtomObject> parameters) {
 
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return "LambdaFunction{" + "parameters=" + parameters + ", symbols=" + symbols + '}';
        }

    }
    
    private static class Visitor extends DefaultVisitor {
        LambdaFunction currentFunction = new LambdaFunction(Collections.emptyList());
        
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
            for (LispObject param: paramList) {
                parameters.add(param.asAtom().get());
            }
            
            LambdaFunction newFunction = new LambdaFunction(parameters);
            currentFunction.children.add(newFunction);
            LambdaFunction temp = currentFunction;
            currentFunction = newFunction;
            
            ListObject result = (ListObject) super.visit(obj);
            
            for (AtomObject param: currentFunction.parameters) {
                currentFunction.symbols.remove(param);
            }              
            
            result.setMeta(currentFunction);
            
            currentFunction = temp;
            
            // undo params
            
            return result;
        }

        @Override
        public LispObject visit(AtomObject obj) {
            if (obj.specialForm() == SpecialFormEnum.NONE) {
                currentFunction.symbols.add(obj);
            }
            
            return obj;
        }
        
    }

    @Test
    public void testClosure() {
        Lisp lisp = new Lisp();
        LispObject obj = lisp.parse("(lambda (incr)\n"
                + "                        (lambda (counter) \n"
                + "                                     (lambda ()\n"
                + "                                       (set! counter (+ counter incr))\n"
                + "                                       counter)))");
        Visitor visitor = new Visitor();
        LispObject proc = obj.accept(visitor);
        
        System.out.println(proc);
        dfs(visitor.currentFunction, 0);
    }
}
