/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.Environment;

import java.util.HashMap;
import java.util.Map;

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
            cache.put(atom, new AtomObject(environment));
        }

        return cache.get(atom);
    }

}
