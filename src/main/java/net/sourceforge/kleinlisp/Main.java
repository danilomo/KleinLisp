package net.sourceforge.kleinlisp;

import java.util.List;

/**
 *
 * @author daolivei
 */
public class Main {

    public static void main(String[] args) {
        String code = "(poligon [(point (+ 40 90) 10) (point 67 60) (point 111 79) (point 80 132)])";

        Lisp runtime = new Lisp();
        runtime.addClass(Point.class);
        runtime.addClass(Poligon.class);

        System.out.println(runtime.parse(code));

        Poligon p = runtime.evaluate(code)
                .asObject(Poligon.class)
                .get();

        System.out.println(p);

        System.out.println(runtime.evaluate("(* 1 2 3 4)"));
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

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "Poligon( " + points + ")";
    }

}
