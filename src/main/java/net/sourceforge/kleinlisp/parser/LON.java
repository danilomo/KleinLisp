/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.sourceforge.kleinlisp.parser.java_cup.Symbol;

/**
 *
 * @author daolivei
 */
public class LON {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
//        SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(new FileInputStream(new File("/uni-mainz.de/homes/daolivei/.emacs.d/init.el")));
        
        String code = "(a de e ( dfsdf ) ) ;;;;;dfsfds\n  \t\t\n(a)";

        InputStream in = new ByteArrayInputStream(code.getBytes());                

        SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(in);
        
        sla = new SourceLexicalAnalyzer(new FileInputStream(new 
        File("/home/danilo/.emacs.d/init.el")));

        System.out.println("___");
        while(true){
            Symbol s = sla.next_token();
            System.out.println(s.value);
            System.out.println("___");
            if(s.sym == sym.EOF){
                return;
            }
        }
    }    

}
