package nodebox.graphics;

/**
 * An element that draws something on the canvas. Graphics elements are shapes (paths), images, or text.
 */
public interface GraphicsElement extends Drawable, Transformable {

    public Rect getBounds();

}
