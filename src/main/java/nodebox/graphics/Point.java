package nodebox.graphics;

import com.google.common.base.Objects;

import java.awt.geom.Point2D;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a two-dimensional point.
 * <p/>
 * Points, like all GeoCore classes, are immutable.
 */
public final class Point {

    public static final Point ZERO = new Point(0, 0);
    public final double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point2D pt) {
        this(pt.getX(), pt.getY());
    }

    public static Point valueOf(String s) {
        String[] args = s.split(",");
        checkArgument(args.length == 2, "String '" + s + "' needs two components, i.e. 12.3,45.6");
        return new Point(Float.valueOf(args[0]), Float.valueOf(args[1]));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point translate(double tx, double ty) {
        return new Point(this.x + tx, this.y + ty);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof Point)) return false;

        final Point other = (Point) o;
        return Objects.equal(x, other.getX())
                && Objects.equal(y, other.getY());
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%.2f,%.2f]", x, y);
    }

    public Point2D toPoint2D() {
        return new Point2D.Double(x, y);
    }

}
