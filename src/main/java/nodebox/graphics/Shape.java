package nodebox.graphics;

import java.util.List;

/**
 * A graphics element that is defined by some combination of straight lines and curves.
 */
public interface Shape extends GraphicsElement {

    public Iterable<Point> getPoints();

}