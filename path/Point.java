import java.util.Vector;
import java.lang.*;

class Point
{
    double x, y, z;

    public Point() 
    {
        x = 0;
        y = 0;
        z = 0;
    }
    public Point(double x, double y, double z) 
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Point b)
    {
        return this.x == b.x && this.y == b.y && this.z == b.z;
    }

    public String toString()
    {
        return "(" + x.toString() + ", " + y.toString() + ", " + z.toString() + ")";
    }
}
