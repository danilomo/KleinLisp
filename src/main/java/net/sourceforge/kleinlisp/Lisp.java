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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.parser.SourceLexicalAnalyzer;
import net.sourceforge.kleinlisp.parser.java_cup.Symbol;
import net.sourceforge.kleinlisp.parser.sym;

/**
 * @author daolivei
 */
public class Lisp {

    private final LispEnvironment environment;
    private final Parser parser;
    private final Evaluator evaluator;

    public Lisp() {
        environment = new LispEnvironment();
        parser = Parser.defaultParser();
        evaluator = new Evaluator(environment);
    }

    public LispObject parse(String expression) {
        return parser.parse(expression, environment);
    }

    public LispObject evaluate(String expression) {
        return evaluator.evaluate(parse(expression));
    }

    public LispEnvironment environment() {
        return environment;
    }

    public Evaluator evaluator() {
        return evaluator;
    }

    public void runScript(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            runScript(in);
        }
    }

    public void runScript(InputStream in) throws IOException {
        SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(in);

        while (true) {
            Symbol s = sla.next_token();
            if (s.value == null) {
                return;
            }

            evaluate(s.value.toString());
            if (s.sym == sym.EOF) {
                return;
            }
        }
    }

}
