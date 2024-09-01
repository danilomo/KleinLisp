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
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author danilo
 */
public class CellObject implements LispObject {

    private LispObject object;

    @Override
    public Object asObject() {
        return object.asObject();
    }

    @Override
    public boolean truthiness() {
        return object.truthiness();
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Optional<Integer> asInt() {
        return object.asInt();
    }

    @Override
    public Optional<Double> asDouble() {
        return object.asDouble();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return object.asAtom();
    }

    @Override
    public Optional<ListObject> asList() {
        return object.asList();
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return object.asFunction();
    }

    @Override
    public <T> Optional<T> as(Class<T> clazz) {
        return object.as(clazz);
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return object.asObject(clazz);
    } 
    
    @Override
    public Optional<CellObject> asCell() {
        return Optional.of(this);
    }
    
    @Override
    public void set(LispObject obj) {
        object = obj;
    }
    
    public LispObject get() {
        return object;
    }

    @Override
    public boolean error() {
        return false;
    }

    @Override
    public String toString() {
        return "CellObject{" + "object=" + object + '}';
    }

    
}
