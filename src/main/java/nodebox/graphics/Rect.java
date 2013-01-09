package nodebox.graphics;

import com.google.common.base.Objects;

import java.awt.geom.Rectangle2D;

/**
 * Represents a rectangle.
 */
public final class Rect {

    public static final Rect EMPTY = new Rect(0, 0, 0, 0);
    public final double x, y, width, height;

    public Rect() {
        this(0, 0, 0, 0);
    }

    /**
     * Construct a new Rect. Rects with negative width or height are automatically normalized.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Rect(double x, double y, double width, double height) {
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect(Rect r) {
        this(r.x, r.y, r.width, r.height);
    }

    public Rect(java.awt.geom.Rectangle2D r) {
        this(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public static Rect centered(double cx, double cy, double width, double height) {
        return new Rect(cx - width / 2, cy - height / 2, width, height);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point getCentroid() {
        return new Point(x + width / 2, y + height / 2);
    }

    public boolean isEmpty() {
        return this.width <= 0 || this.height <= 0;
    }

    public Rect united(Rect r) {
        double x, y, width, height;
        x = Math.min(this.x, r.x);
        y = Math.min(this.y, r.y);
        width = Math.max(this.x + this.width, r.x + r.width) - x;
        height = Math.max(this.y + this.height, r.y + r.height) - y;
        return new Rect(x, y, width, height);
    }

    public boolean intersects(Rect r) {
        return Math.max(this.x, this.y) < Math.min(this.x + this.width, r.width) &&
                Math.max(this.y, r.y) < Math.min(this.y + this.height, r.y + r.height);
    }

    public boolean contains(double x, double y) {
        return x >= this.x && x <= this.x + this.width &&
                y >= this.y && y <= this.y + this.height;
    }

    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    public boolean contains(Rect r) {
        return r.x >= this.x && r.x + r.width <= this.x + this.width &&
                r.y >= this.y && r.y + r.height <= this.y + this.height;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rect)) return false;

        final Rect other = (Rect) o;
        return Objects.equal(x, other.x)
                && Objects.equal(y, other.y)
                && Objects.equal(width, other.width)
                && Objects.equal(height, other.height);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Rect(" + x + ", " + y + ", " + width + ", " + height + ")";
    }

    public Rectangle2D toRectangle2D() {
        return new Rectangle2D.Double(x, y, width, height);
    }

}
