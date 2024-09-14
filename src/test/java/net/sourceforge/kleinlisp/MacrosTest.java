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

import java.util.Arrays;
import java.util.HashSet;
import net.sourceforge.kleinlisp.macros.MacroTransformation;
import net.sourceforge.kleinlisp.macros.MatchResult;
import net.sourceforge.kleinlisp.macros.PatternMatcher;
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
                object = object.cdr();
                
                return new ListObject(atom("print"), object);
            }
        }
        MacroEnv env = new MacroEnv();

        String script = "(when (= 1 1) (__display 2) (__display (+ 1 2)))";
        lisp.environment().registerMacro("__display", env::display);
        lisp.evaluate(script);

        Assert.assertEquals("23", getStdOut());
    }

    @Test
    public void testPatternMatching() {
        ListObject pattern = lisp.parse("(_ ana _ else bananinha)").asList();
        ListObject input = lisp.parse("(ronaldo 1 2 else 3)").asList();

        System.out.println(pattern);
        System.out.println(input);

        PatternMatcher pm = new PatternMatcher(pattern, new HashSet<>(Arrays.asList(
                lisp.environment().atomOf("else")
        )));
        pm.match(input);
    }

    @Test
    public void testPatternMatching2() {
        ListObject pattern = lisp.parse("(_ ana else bananinha)").asList();
        ListObject input = lisp.parse("(ronaldo 1 2 else 3)").asList();

        System.out.println(pattern);
        System.out.println(input);

        PatternMatcher pm = new PatternMatcher(pattern, new HashSet<>(Arrays.asList(
                lisp.environment().atomOf("else")
        )));
        pm.match(input);
    }

    @Test
    public void testPatternMatching3() {
        ListObject pattern = lisp.parse("(_ (e1 e2 e3 _REST_) (e4 e5 e6 _REST_))").asList();
        ListObject input = lisp.parse("(cond (a b c 4 5 6) (d e f 7))").asList();

        System.out.println(pattern);
        System.out.println(input);

        PatternMatcher pm = new PatternMatcher(pattern, new HashSet<>(Arrays.asList(
                lisp.environment().atomOf("else")
        )));
        pm.match(input);
    }

    @Test
    public void testPatternMatching4() {
        ListObject pattern = lisp.parse("(_ condition body ...)").asList();
        ListObject input = lisp.parse("(when (> 1 2) (print like) (print this) (print like) (print that))").asList();
        ListObject transformation = lisp.parse("(if (not condition) (begin body ...))").asList();

        System.out.println(pattern);
        System.out.println(input);

        PatternMatcher pm = new PatternMatcher(pattern, new HashSet<>(Arrays.asList()));
        MatchResult match = pm.match(input);

        MacroTransformation trans = new MacroTransformation(transformation);

        System.out.println(trans.transform(match));
    }

    @Test
    public void testDefineSyntaxWithoutKeyword() {
        String macroDef = "(define-syntax unless\n"
                + "  (syntax-rules ()\n"
                + "    ((unless condition body ...)\n"
                + "     (if (not condition)\n"
                + "         (begin body ...) '() ))))";

        lisp.evaluate(macroDef);
        lisp.evaluate("(define x 3)");
        lisp.evaluate("(unless (> x 5)\n"
                + "  (display \"x is not greater than 5\")\n"
                + "  (newline))");
        
        Assert.assertEquals(
                "\"x is not greater than 5\"\n\n",
                getStdOut()
        );
    }
}
