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

import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;


public class DefineForm implements SpecialForm {

    private final Evaluator evaluator;  
    private final LispEnvironment environment; 
    private final AtomObject lambdaAtom;

    public DefineForm(Evaluator evaluator, LispEnvironment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
        this.lambdaAtom = environment.atomOf("lambda");
    }
    
    @Override
    public Supplier<LispObject> apply(LispObject t) {
        ListObject list = t.asList();
        list = list.cdr();
        LispObject first = list.car();
        
        AtomObject idOpt = first.asAtom();
        
        if (idOpt != null) {
            AtomObject id = idOpt;
            LispObject value = list.cdr().car();
            environment.set(id, evaluator.evaluate(value));
        } else {
            ListObject signature = first.asList();
            
            defineFunction(signature, list.cdr());
        }

        return () -> VoidObject.VOID;
    }

    private void defineFunction(ListObject signature, LispObject value) {
        AtomObject id = signature.car().asAtom();
        LispObject parameters = signature.cdr();
        
        ListObject lambda = new ListObject(
                lambdaAtom,
                new ListObject(parameters, value)
        );
        LispObject function = evaluator.evaluate(lambda);
        
        environment.set(id, function);
    }

}
