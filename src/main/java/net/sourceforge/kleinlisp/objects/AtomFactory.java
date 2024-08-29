/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            
            cache.put(atom, new AtomObject(environment, specialForm));
        }

        return cache.get(atom);
    }

}
