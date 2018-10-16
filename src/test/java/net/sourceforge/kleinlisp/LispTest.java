package net.sourceforge.kleinlisp;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Danilo Oliveira
 */
public class LispTest {

    public LispTest() {
    }

    /**
     * Test of evaluate method, of class Lisp.
     */
    @Test
    public void testEvaluate() {
        System.out.println("evaluate");
        String expression = "(+ 10 20 30 40 50)";
        Lisp instance = new Lisp();
       
        LispObject result = instance.evaluate(expression);
        assertEquals(new Integer(150), result.asInt().get());
    }

    /**
     * Test of parse method, of class Lisp.
     */
    @Test
    public void testArithmetic() {
        System.out.println("parse");
        String expression = "(+ 10 20 30 40 50)";
        Lisp instance = new Lisp();
        LispObject result = instance.parse(expression).asList().get();
        List<LispObject> l = result.asList().get().toList();
        Integer[] arr = {-1, 10, 20, 30, 40, 50 };
        
        for(int i = 1; i < arr.length; i++){
            assertEquals(arr[i], l.get(i).asInt().get());
        }
    }

    /**
     * Test of addClass method, of class Lisp.
     */
    @Test
    public void testAddClass() {
        System.out.println("Testing Lisp.addClass");
        Class clazz = Point3D.class;
        Lisp instance = new Lisp();
        instance.addClass(clazz);
        
        Point3D p3d = new Point3D(10, 30, 1000);
        
        Point3D p3d2 = instance.evaluate("(Point3D 10 30 1000)").asObject(Point3D.class).get();
  
        assertEquals(p3d, p3d2);
        
        p3d = new Point3D(1, 30, 1000);
        
        assertNotEquals(p3d2, p3d);
    }

}

class Point3D {

    private int x;
    private int y;
    private int z;

    public Point3D() {
    }       
    
    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }    

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point3D)) {
            return false;
        }
        
        Point3D p3d = (Point3D) obj;
        
        return p3d.x == x && p3d.y == y && p3d.z == z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        hash = 97 * hash + this.z;
        return hash;
    }

}
