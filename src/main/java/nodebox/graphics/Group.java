package nodebox.graphics;

import clojure.lang.PersistentVector;
import com.google.common.collect.Iterables;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public final class Group implements GraphicsElement {

    private final PersistentVector elements;

    private Group(PersistentVector elements) {
        this.elements = elements;
    }

    public static Group of() {
        return new Group(PersistentVector.EMPTY);
    }

    public static Group of(GraphicsElement... elements) {
        return new Group(PersistentVector.create((Object[]) elements));
    }

    public static Group of(List<GraphicsElement> elements) {
        return new Group(PersistentVector.create(elements));
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    @Override
    public Rect getBounds() {
        if (elements.isEmpty()) return Rect.EMPTY;

        Rect totalBounds = null;
        for (Object o : elements) {
            Rect bounds = ((Shape) o).getBounds();
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
        for (Object o : elements) {
            if (o instanceof Shape) {
                Shape g = (Shape) o;
                pointIterables.add(g.getPoints());
            }
        }
        return Iterables.concat(pointIterables);
    }

    @SuppressWarnings("unchecked")
    public List<Shape> getElements() {
        return elements;
    }

    public Group cons(Shape g) {
        return new Group(elements.cons(g));
    }

    public Group transform(Transform t) {
        return t.map(this);
    }

    @Override
    public void draw(Graphics2D g) {
        for (Object o : elements) {
            Drawable d = (Drawable) o;
            d.draw(g);
        }
    }

}
