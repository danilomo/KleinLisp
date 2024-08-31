package net.sourceforge.kleinlisp;

import java.nio.file.Paths;

/**
 *
 * @author danilo
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String script = args[0];
        Lisp lisp = new Lisp();
        lisp.runScript(Paths.get(script));
    }
}
