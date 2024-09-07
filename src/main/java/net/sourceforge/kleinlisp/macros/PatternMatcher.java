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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class PatternMatcher {

    private final ListObject pattern;
    private final Set<AtomObject> syntaxVariables;

    public PatternMatcher(ListObject pattern, Set<AtomObject> syntaxVariables) {
        this.pattern = pattern;
        this.syntaxVariables = syntaxVariables;
    }

    public MatchResult match(ListObject obj) {
        PatternTreeWalker walker = new PatternTreeWalker();
        walker.walkTree(pattern, obj);

        return walker.getMatchResult();
    }

    private class PatternTreeWalker {

        Map<AtomObject, LispObject> patterns = new HashMap<>();
        Map<AtomObject, LispObject> ellipisis = new HashMap<>();
        boolean matches = true;
        
        AtomObject last = null;
        AtomObject currentAtom = null;

        void walkTree(ListObject pattern, ListObject input) {
            ListObject refPattern = pattern;
            ListObject refInput = input;

            while (refPattern != ListObject.NIL && refInput != ListObject.NIL) {                
                Optional<ListObject> listFromPattern = refPattern.car().asList();
                
                if (listFromPattern.isPresent()) {
                    Optional<ListObject> listFromInput = refInput.car().asList();
                    
                    if (listFromInput.isPresent()) {
                        walkTree( listFromPattern.get(), listFromInput.get());
                        
                        if (!matches) {
                            return;
                        }
                    } else {
                        matches = false;
                        return;                                
                    }
                    
                    refPattern = refPattern.cdr();
                    refInput = refInput.cdr();
                    continue;
                }
                
                last = currentAtom;
                currentAtom = refPattern.car().asAtom().get();
                LispObject elem = refInput.car();

                boolean recordPattern = true;
                
                if (currentAtom.toString().equals("_")) {
                    recordPattern = false;
                } else if (currentAtom.toString().equals("...")) {
                    ellipisis.put(last, refInput);
                    return;
                } else if (syntaxVariables.contains(currentAtom)) {
                    if (currentAtom != elem) {
                        matches = false;
                        return;
                    }
                    recordPattern = false;
                }
                
                if (recordPattern) {
                    patterns.put(currentAtom, elem);
                }

                refPattern = refPattern.cdr();
                refInput = refInput.cdr();
            }

            if (refPattern == ListObject.NIL && refInput != ListObject.NIL) {
                matches = false;
            }
        }

        @Override
        public String toString() {
            return "PatternTreeWalker{" + "patterns=" + patterns + ", ellipisis=" + ellipisis + ", matches=" + matches + '}';
        }

        private MatchResult getMatchResult() {
            return new MatchResult(patterns, ellipisis, matches);
        }

    }

}
