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
package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.NumericObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class CaseForm implements SpecialForm {

    private static Set<Object> CATCH_ALL = new HashSet<>();
    
    private static class Branch {

        final Set<Object> cases;
        final Supplier<LispObject> body;
        final boolean catchAll;
        
        public Branch(Set<Object> cases, Supplier<LispObject> body) {
            this.cases = cases;
            this.body = body;
            this.catchAll = cases == CATCH_ALL;
        }

        private boolean match(Object obj) {
            if (catchAll) {
                return true;
            }
            
            return cases.contains(obj);
        }

    }

    private final Evaluator evaluator;

    public CaseForm(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Supplier<LispObject> apply(LispObject t) {
        ListObject body = t.asList().get().cdr();
        LispObject value = body.car();
        LispObject rest = body.cdr();

        
        List<Branch> branches = parseCaseBody(rest.asList().get());
        Supplier<LispObject> valueSupplier = value.accept(evaluator);
        
        return () -> {
            Object obj = toObj(valueSupplier.get());
            
            for (Branch branch: branches) {
                if (branch.match(obj)) {
                    return branch.body.get();
                }
            }
            
            return VoidObject.VOID;
        };
    }

    private List<Branch> parseCaseBody(ListObject body) {
        List<Branch> output = new ArrayList<>();
        
        for (LispObject obj: body) {
            Branch branch = parseCaseBranch(obj);
            output.add(branch);
        }
        
        return output;        
    }
    
    private Branch parseCaseBranch(LispObject obj) {
        ListObject list = obj.asList().get();
        LispObject cases = list.car();
        LispObject body = list.cdr().car();
        
        Supplier<LispObject> bodyEval = body.accept(evaluator);
        
        if (cases.asAtom().map(a -> a.toString()).orElse("").equals("else")) {
            return new Branch(CATCH_ALL, bodyEval);
        }
        
        Set<Object> casesSet = new HashSet<>();
        for (LispObject case_: cases.asList().get()) {
            Object caseObj = toObj(case_);
            casesSet.add(caseObj);
        }
        
        
        return new Branch(casesSet, bodyEval);
    }    
    
    private Object toObj(LispObject value) {
        if (value instanceof AtomObject) {
            return value.asAtom().get();
        }
        
        if (value instanceof NumericObject) {
            return value.asDouble().get();
        }
        
        return VoidObject.VOID;
    }
    
}
