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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class CondForm implements SpecialForm {

    private static class Branch {
        final Supplier<LispObject> condition;
        final Supplier<LispObject> body;

        public Branch(Supplier<LispObject> condition, Supplier<LispObject> body) {
            this.condition = condition;
            this.body = body;
        }
                
    }
    
    private final Evaluator evaluator;

    public CondForm(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Supplier<LispObject> apply(LispObject t) {
        ListObject body = t.asList().cdr();              
        
        List<Branch> branches = parseCondBody(body);
        
        return () -> {
            for (Branch b: branches) {
                if (b.condition.get().truthiness()) {
                    return b.body.get();
                }
            }
            
            return ListObject.NIL;
        };
    }

    private List<Branch> parseCondBody(ListObject body) {
        List<Branch> output = new ArrayList<>();
        
        for (LispObject obj: body) {
            Branch branch = parseCondBranch(obj);
            output.add(branch);
        }
        
        return output;
    }

    private Branch parseCondBranch(LispObject obj) {
        Tuple2<LispObject, LispObject> tuple = Optional
                .ofNullable( obj
                .asList())
                .flatMap(l -> l.unpack(LispObject.class, LispObject.class))
                .get();
        
        LispObject cond = tuple.first();
        LispObject body = tuple.second();
        
        boolean isElse = Optional
                .ofNullable(cond
                .asAtom())
                .map(a -> a.toString().equals("else"))
                .orElse(false);
        
        Supplier<LispObject> condEval;
        
        if (isElse) {
            condEval = () -> BooleanObject.FALSE;
        } else {
            condEval = cond.accept(evaluator);
        }
        
        Supplier<LispObject> bodyEval = body.accept(evaluator);
        
        return new Branch(condEval, bodyEval);
    }    
    
}
