package net.sourceforge.kleinlisp;

import org.junit.Before;

abstract public class BaseTestClass {

    protected Lisp lisp;

    @Before
    public void setup() {
        lisp = new Lisp();
    }

    protected int evalAsInt(String str) {
        return lisp.evaluate(str).asInt().get();
    }
}
