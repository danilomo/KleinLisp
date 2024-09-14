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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;


public class BooleanFunctions {

    public static LispObject lt(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left < right);
    }

    public static LispObject le(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left <= right);
    }

    public static LispObject gt(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left > right);
    }

    public static LispObject ge(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left >= right);
    }

    public static LispObject eq(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left == right);
    }

    public static LispObject neq(LispObject[] params) {
        int left = params[0].asInt().value;
        int right = params[1].asInt().value;

        return new BooleanObject(left != right);
    }
    
    public static LispObject not(LispObject[] params) {
        return new BooleanObject(!params[0].truthiness());
    }

}
