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

import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class MacroTransformation {
    
    private static class ListBuffer {
        private final ListObject first;   
        private ListObject last;

        public ListBuffer() {
            first = new ListObject(VoidObject.VOID);
            last = first;
        }
        
        public void add(LispObject obj) {
            last.setCdr(new ListObject(obj));
            last = last.cdr();
        }
        
        public void concat(ListObject list) {
            last.setCdr(list);                        
        }
        
        public ListObject getList() {
            return first.cdr();
        }
        
    }
    
    private final ListObject transformationBody;

    public MacroTransformation(ListObject transformationBody) {
        this.transformationBody = transformationBody;
    }
    
    private class TransformationVisitor extends DefaultVisitor {
        
        final MatchResult match;

        public TransformationVisitor(MatchResult match) {
            this.match = match;
        }
        
        @Override
        public LispObject visit(ListObject obj) {
            ListBuffer buffer = new ListBuffer();
            ListObject pointer = obj;
            AtomObject last;
            AtomObject atom = null;
            
            while(pointer != ListObject.NIL) {
                LispObject elem = pointer.car();
                
                if (elem.asAtom().isPresent()) {
                    last = atom;
                    atom = elem.asAtom().get();
                    
                    if (atom.toString().equals("...")) {
                        ListObject ellipsis = match.getEllipsis(last).asList().get();
                        buffer.concat(ellipsis);
                        break;
                    }
                    
                    if (match.getTransformation(atom) != null) {
                        buffer.add(match.getTransformation(atom));                       
                    } else {                        
                        buffer.add(atom);
                    }
                    
                } else {                
                    buffer.add(elem.accept(this));                    
                }
                
                pointer = pointer.cdr();
            }
            
            return buffer.getList();
        }        
    }
    
    public ListObject transform(MatchResult match) {
        TransformationVisitor visitor = new TransformationVisitor(match);
        
        return transformationBody.accept(visitor).asList().get();
    }
}
