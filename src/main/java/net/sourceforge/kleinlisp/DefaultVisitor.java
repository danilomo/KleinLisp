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
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.*;

/**
 * @author daolivei
 */
public class DefaultVisitor implements LispVisitor<LispObject> {

    @Override
    public LispObject visit(AtomObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(BooleanObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(DoubleObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(IntObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(JavaObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(VoidObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ListObject obj) {

        if (obj == ListObject.NIL) {
            return obj;
        }

        if (obj.tail() != ListObject.NIL) {
            return new ListObject(obj.head().accept(this), obj.tail().accept(this));
        } else {
            return new ListObject(obj.head().accept(this), ListObject.NIL);
        }
    }

    @Override
    public LispObject visit(StringObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(FunctionObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ErrorObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ComputedLispObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(CellObject obj) {
        return obj;
    } 

    @Override
    public LispObject visit(IdentifierObject obj) {
        return obj;
    }

    

}
