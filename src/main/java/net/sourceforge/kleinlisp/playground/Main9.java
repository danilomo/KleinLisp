package net.sourceforge.kleinlisp.playground;

import jline.console.ConsoleReader;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;

import java.io.IOException;
import java.io.PrintWriter;

public class Main9 {

    public static void main(String[] args){

        try (ConsoleReader reader = new ConsoleReader()) {
            Lisp lisp = new Lisp();
            reader.setPrompt("prompt> ");

            String line;
            PrintWriter out = new PrintWriter(reader.getOutput());

            while ((line = reader.readLine()) != null) {
                LispObject result = null;
                try {
                    result = lisp.evaluate(line);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                System.out.println("==> " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
