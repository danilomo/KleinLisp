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
package net.sourceforge.kleinlisp.macros;

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class StandardMacros {

    private Lisp lisp;

    public ListObject when(ListObject object) {
        ListObject body = object.cdr();
        LispObject condition = body.car();
        ListObject statements = body.cdr();

        ListObject trueBranch = new ListObject(atom("begin"), statements);
        ListObject ifBody = new ListObject(trueBranch, ListObject.NIL);

        return new ListObject(
                atom("if"),
                new ListObject(condition, ifBody)
        );
    }
    
    public ListObject banana(ListObject object) {
        return new ListObject(atom("banana"), object);
    }

    private AtomObject atom(String atom) {
        return lisp.environment().atomOf(atom);
    }
}
