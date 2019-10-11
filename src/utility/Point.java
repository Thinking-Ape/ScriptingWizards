package utility;

public class Point {
    private int x,y;
    public Point (int x,int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Point) {
            Point p = (Point)o;
            return x == p.getX() && y == p.getY();
        }
        return  super.equals(o);
    }
}
