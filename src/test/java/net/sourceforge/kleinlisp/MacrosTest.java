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

import net.sourceforge.kleinlisp.macros.StandardMacros;
import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class MacrosTest extends BaseTestClass {

    @Test
    public void testMacro() {
        class MacroEnv extends StandardMacros {

            public MacroEnv() {
                super((lisp.environment()));
            }

            ListObject display(ListObject object) {
                return new ListObject(atom("print"), object);
            }
        }
        MacroEnv env = new MacroEnv();

        String script = "(when (= 1 1) (display 2) (display (+ 1 2)))";
        lisp.environment().registerMacro("display", env::display);        
        lisp.evaluate(script);
        
        Assert.assertEquals("23", getStdOut());
    }

}
