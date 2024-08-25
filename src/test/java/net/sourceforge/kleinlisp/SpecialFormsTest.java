package net.sourceforge.kleinlisp;

import java.util.Arrays;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class SpecialFormsTest {

    private Lisp lisp;

    public SpecialFormsTest() {
    }

    @Before
    public void setup() {
        lisp = new Lisp();
    }

    @Test
    public void testIfForm() {
        assertEquals(
                lisp.evaluate("(if (< 3 2) 1 (+ 2 2 2))").asInt().get().intValue(),
                6
        );
    }
    
    @Test
    public void testLetForm() {
        assertEquals(
                lisp.evaluate("(let ((x 1) (y 2)) (+ x y 1))").asInt().get().intValue(),
                4
        );
    }

    @Test
    public void testLambdaForm() {
        FunctionObject obj = lisp.evaluate("(lambda (x y z) (+ x y z))").asFunction().get();
        
        LispObject result = obj.function().evaluate(Arrays.asList(
                new IntObject(1),
                new IntObject(1),
                new IntObject(1)
        ));
        
        assertEquals(
                result.asInt().get().intValue(),
                3
        );

    }
    
    @Test
    public void testDefineLambdaForm() {
        lisp.evaluate("(define foo (lambda (x y z) (+ x y z)))");
        
        assertEquals(
                lisp.evaluate("(foo 1 2 3)").asInt().get().intValue(),
                6
        );
    } 
    
    @Test
    public void testDefineForm() {
        lisp.evaluate("(define (foo x y z) (+ x y z))");
        
        assertEquals(
                lisp.evaluate("(foo 1 2 3)").asInt().get().intValue(),
                6
        );                
    }      

}
