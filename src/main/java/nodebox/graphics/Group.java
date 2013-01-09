package nodebox.graphics;

import clojure.lang.PersistentVector;
import com.google.common.collect.Iterables;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public final class Group implements Shape {

    private final PersistentVector shapes;

    private Group(PersistentVector shapes) {
        this.shapes = shapes;
    }

    public static Group of() {
        return new Group(PersistentVector.EMPTY);
    }

    public static Group of(Shape... geometries) {
        return new Group(PersistentVector.create((Object[])geometries));
    }

    public static Group of(List<Shape> geometries) {
        return new Group(PersistentVector.create(geometries));
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    public int size() {
        return shapes.size();
    }

    @Override
    public Rect getBounds() {
        if (shapes.isEmpty()) return Rect.EMPTY;

        Rect totalBounds = null;
        for (Object o : shapes) {
            Rect bounds  =((Shape)o).getBounds();
            if (!bounds.isEmpty()) {
                if (totalBounds == null) {
                    totalBounds = bounds;
                } else {
                    totalBounds = totalBounds.united(bounds);
                }
            }
        }
        return totalBounds;
    }

    public Iterable<Point> getPoints() {
        LinkedList<Iterable<Point>> pointIterables = new LinkedList<Iterable<Point>>();
        for (Object o : shapes) {
            Shape g = (Shape) o;
            pointIterables.add(g.getPoints());

        }
        return Iterables.concat(pointIterables);
    }

    @SuppressWarnings("unchecked")
    public List<Shape> getShapes() {
        return shapes;
    }

    public Group cons(Shape g) {
        return new Group(shapes.cons(g));
    }

    public Group transform(Transform t) {
        return t.map(this);
    }

    @Override
    public void draw(Graphics2D g) {
        for (Object o : shapes) {
            Drawable d = (Drawable) o;
            d.draw(g);
        }
    }

}
