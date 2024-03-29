package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;

import java.util.List;

/**
 * @author daolivei
 */
public class Main {

    public static void main(String[] args) {
        Lisp runtime = new Lisp();

        String code = "(if 0 (log [1 2 3 4 5 6]) (log \"banana\"))";

        code = "((lambda (x y) (+ x y)) 10 20)";

//        System.out.println(runtime.parse(code));
//        System.out.println("");
//        System.out.println(runtime.evaluate(code));
    }
}

class Point {

    private int x;
    private int y;

    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}

class Poligon {

    private List<Point> points;

    public Poligon() {
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "Poligon( " + points + ")";
    }

}
