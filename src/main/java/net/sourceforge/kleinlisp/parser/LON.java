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
package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.parser.java_cup.Symbol;

import java.io.*;

/**
 * @author daolivei
 */
public class LON {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
//        SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(new FileInputStream(new File("/uni-mainz.de/homes/daolivei/.emacs.d/init.el")));

        String code = "(a de e ( dfsdf ) ) ;;;;;dfsfds\n  \t\t\n(a)";

        InputStream in = new ByteArrayInputStream(code.getBytes());

        SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(in);

        sla = new SourceLexicalAnalyzer(new FileInputStream(new
                File("/home/danilo/.emacs.d/init.el")));

        System.out.println("___");
        while (true) {
            Symbol s = sla.next_token();
            System.out.println(s.value);
            System.out.println("___");
            if (s.sym == sym.EOF) {
                return;
            }
        }
    }

}
