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

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author danilo
 */
public class FunctionCall implements Supplier<LispObject> {
    private final Supplier<LispObject> head;
    private final List<Supplier<LispObject>> parameters;

    public FunctionCall(Supplier<LispObject> head, List<Supplier<LispObject>> parameters) {
        this.head = head;
        this.parameters = parameters;
    }

    @Override
    public LispObject get() {
        Optional<Function> function = Optional
                .ofNullable( head.get().asFunction())
                .flatMap(
                        f -> Optional.of(f.function())
                );

        LispObject[] params = new LispObject[parameters.size()];
        int i = 0;
        for (Supplier<LispObject> param: parameters) {
            LispObject obj = param.get();
            params[i] = obj;
            i++;
        }

        return function
                .flatMap(f -> Optional.of(f.evaluate(params)))
                .orElse(ListObject.NIL);
    }
}
