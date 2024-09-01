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

import net.sourceforge.kleinlisp.Environment;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;

/**
 * @author danilo
 */
public class AtomFactory {
    private final Map<String, AtomObject> cache;
    private final Environment environment;

    public AtomFactory(Environment env) {
        this.cache = new HashMap<>();
        this.environment = env;
    }

    public AtomObject newAtom(String atom) {
        if (!cache.containsKey(atom)) {
            SpecialFormEnum specialForm = SpecialFormEnum.NONE;
            switch(atom) {
                case "lambda":
                    specialForm = SpecialFormEnum.LAMBDA;
                    break;
                case "set!":
                    specialForm = SpecialFormEnum.SET;
                    break;
                case "define":
                    specialForm = SpecialFormEnum.DEFINE;
                    break;
                case "let":
                    specialForm = SpecialFormEnum.LET;
                    break;
                case "begin":
                    specialForm = SpecialFormEnum.BEGIN;
                    break;
                case "if":
                    specialForm = SpecialFormEnum.IF;
                    break;
                case "or":
                    specialForm = SpecialFormEnum.OR;
                    break;
                case "and":
                    specialForm = SpecialFormEnum.AND;
                    break;
                default:
                    break;
            }
            
            cache.put(atom, new AtomObject(environment, atom, specialForm));
        }

        return cache.get(atom);
    }

}
