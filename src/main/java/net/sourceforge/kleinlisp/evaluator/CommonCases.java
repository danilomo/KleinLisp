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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class CommonCases {

    static Supplier<LispObject> apply(ListObject list, Evaluator evaluator) {
        AtomObject head = list.car().asAtom();
        if (head == null) {
            return null;
        }
        
        if (list.length() != 3) {
            return null;
        }
        
        switch(head.toString()) {
            case "+":
                return plus(list.cdr(), evaluator);
            case "-":
                return minus(list.cdr(), evaluator);
            case "<":
                return lessthan(list.cdr(), evaluator);
        }
        
        return null;
    }

    private static Supplier<LispObject> plus(ListObject list, Evaluator evaluator) {
        Supplier<LispObject> left = list.car().accept(evaluator);
        Supplier<LispObject> right = list.cdr().car().accept(evaluator);
                
        return () -> {
            return new IntObject(left.get().asInt().value + right.get().asInt().value);
        };
    }

    private static Supplier<LispObject> minus(ListObject list, Evaluator evaluator) {
        Supplier<LispObject> left = list.car().accept(evaluator);
        Supplier<LispObject> right = list.cdr().car().accept(evaluator);
                
        return () -> {
            return new IntObject(left.get().asInt().value - right.get().asInt().value);
        };        
    }

    private static Supplier<LispObject> lessthan(ListObject list, Evaluator evaluator) {
        Supplier<LispObject> left = list.car().accept(evaluator);
        Supplier<LispObject> right = list.cdr().car().accept(evaluator);
                
        return () -> {
            if (left.get().asInt().value < right.get().asInt().value) {
                return BooleanObject.TRUE;
            }
            
            return BooleanObject.FALSE;
        };        
    }
    
}
