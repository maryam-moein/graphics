package nodebox.graphics;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.awt.geom.Dimension2D;
import java.util.Iterator;

public final class Size implements Iterable {

    public static final Size EMPTY = new Size();
    public final double width, height;

    public Size() {
        this(0, 0);
    }

    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Size(Dimension2D d) {
        this.width = d.getWidth();
        this.height = d.getHeight();
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Dimension2D getDimension2D() {
        return new Dimension2D() {
            @Override
            public double getWidth() {
                return width;
            }

            @Override
            public double getHeight() {
                return height;
            }

            @Override
            public void setSize(double v, double v2) {
                throw new UnsupportedOperationException("Size is immutable.");
            }
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof Size)) return false;

        final Size other = (Size) o;
        return Objects.equal(width, other.width)
                && Objects.equal(height, other.height);
    }

    @Override
    public String toString() {
        return "Size(" + width + ", " + height + ")";
    }

    public Iterator<Double> iterator() {
        return ImmutableList.of(width, height).iterator();
    }

}
