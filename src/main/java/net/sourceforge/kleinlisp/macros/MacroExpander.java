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

import java.util.Map;
import java.util.Optional;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class MacroExpander extends DefaultVisitor {
    private Map<AtomObject, MacroDefinition> macros;

    @Override
    public LispObject visit(ListObject obj) {        
        if (obj == ListObject.NIL) {
            return obj;
        }
        
        Optional<AtomObject> head = obj.car().asAtom();
        
        if (head.isEmpty()) {
            return super.visit(obj);
        }
        
        AtomObject atom = head.get();
        
        if (macros.containsKey(atom)) {
            MacroDefinition macro = macros.get(atom);
            return macroExpand(macro, obj);
        }
        
        return super.visit(obj);
    }

    private LispObject macroExpand(MacroDefinition macro, ListObject obj) {
        return null;
    }
    
    
}
