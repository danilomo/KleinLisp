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
package net.sourceforge.kleinlisp.api;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class IOFunctions {
    
    public static LispObject print(LispObject[] params) {
        String asString = Arrays
                .stream(params)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        System.out.print(asString);
        return VoidObject.VOID;
    }
    
    public static LispObject println(LispObject[] params) {
        String asString = Arrays
                .stream(params)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        System.out.println(asString);
        return VoidObject.VOID;
    }
    
    public static LispObject newline(LispObject[] params) {
        System.out.println();
        return VoidObject.VOID;
    }    
}
