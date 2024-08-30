package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.evaluator.ClosureVisitor;
import net.sourceforge.kleinlisp.special_forms.LambdaForm;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class ClosureTest {

    @Test
    public void testClosure() {
        Lisp lisp = new Lisp();
        LispObject obj = lisp.parse("(lambda (incr)\n"
                + "                        (lambda (counter) \n"
                + "                                     (lambda ()\n"
                + "                                       (set! counter (+ counter incr))\n"
                + "                                       counter)))");
        obj = ClosureVisitor.addClosureMeta(obj);
        
        System.out.println(obj);
    }
    
    @Test
    public void testClosure2() {
        Lisp lisp = new Lisp();
        LispObject obj = lisp.parse("(lambda (counter) \n"
                + "                                     (lambda ()\n"
                + "                                       (set! counter (+ counter 1))\n"
                + "                                       counter))");
        obj = ClosureVisitor.addClosureMeta(obj);
        
        LambdaForm form = new LambdaForm(lisp.evaluator(), lisp.environment());
        
        form.apply(obj);
    }
    
    @Test
    public void testClosure3() {
        Lisp lisp = new Lisp();
        LispObject obj = lisp.parse("(lambda ()"
                + "  (set! counter (+ counter 1))\n"
                + "   counter))");
        obj = ClosureVisitor.addClosureMeta(obj);
        
        LambdaForm form = new LambdaForm(lisp.evaluator(), lisp.environment());
        
        form.apply(obj);
    }    
}
