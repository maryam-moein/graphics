package nodebox.graphics;

import com.google.common.base.Objects;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;

import static nodebox.graphics.Geometry.clamp;

public class Image implements GraphicsElement {

    public static final Image BLANK = new Image(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), Point.ZERO, 1, 1, 1, Transform.IDENTITY);
    private final BufferedImage image;
    private final Point position;
    private final double desiredWidth, desiredHeight;
    private final double alpha;
    private final Transform transform;

    private Image(BufferedImage image, Point position, double desiredWidth, double desiredHeight, double alpha, Transform transform) {
        this.image = image;
        this.position = position;
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.alpha = alpha;
        this.transform = transform;
    }

    public static Image fromFile(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            return new Image(image, Point.ZERO, 0, 0, 1, Transform.IDENTITY);
        } catch (IOException e) {
            throw new RuntimeException("Could not read image file " + file, e);
        }
    }

    public static Image fromFile(String fileName) {
        return fromFile(new File(fileName));
    }

    public static Image fromData(byte[] data) {
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(data));
        try {
            return new Image(ImageIO.read(in), Point.ZERO, 0, 0, 1, Transform.IDENTITY);
        } catch (IOException e) {
            throw new RuntimeErrorException(null, "Could not read image data.");
        }
    }

    //// Attribute access ////

    public double getOriginalWidth() {
        if (image == null) return 0;
        return image.getWidth();
    }

    public double getOriginalHeight() {
        if (image == null) return 0;
        return image.getHeight();
    }

    public double getWidth() {
        return getOriginalWidth() * getScaleFactor();
    }

    public double getHeight() {
        return getOriginalHeight() * getScaleFactor();
    }

    public Point getPosition() {
        return position;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double getAlpha() {
        return alpha;
    }

    public Size getOriginalSize() {
        return new Size(getOriginalWidth(), getOriginalHeight());
    }

    //// "Mutation" methods ////

    public Image position(Point position) {
        return new Image(image, position, desiredWidth, desiredHeight, alpha, transform);
    }

    public Image x(double x) {
        Point pt = new Point(x, position.y);
        return position(pt);
    }

    public Image y(double y) {
        Point pt = new Point(position.x, y);
        return position(pt);
    }

    public Image desiredWidth(double desiredWidth) {
        return new Image(image, position, desiredWidth, desiredHeight, alpha, transform);
    }

    public Image desiredHeight(double desiredHeight) {
        return new Image(image, position, desiredWidth, desiredHeight, alpha, transform);
    }

    public Image alpha(double alpha) {
        return new Image(image, position, desiredWidth, desiredHeight, alpha, transform);
    }

    @Override
    public GraphicsElement transform(Transform t) {
        return new Image(image, position, desiredWidth, desiredHeight, alpha, transform.concatenate(t));
    }

    //// Grob support ////

    public boolean isEmpty() {
        return image == null || image.getWidth() == 0 || image.getHeight() == 0;
    }

    public Rect getBounds() {
        if (image == null) return new Rect();
        double factor = getScaleFactor();
        double finalWidth = image.getWidth() * factor;
        double finalHeight = image.getHeight() * factor;
        return new Rect(getX() - finalWidth / 2, getY() - finalHeight / 2, finalWidth, finalHeight);
    }

    public double getScaleFactor() {
        if (desiredWidth != 0 || desiredHeight != 0) {
            double srcW = image.getWidth();
            double srcH = image.getHeight();
            if (desiredWidth != 0 && desiredHeight != 0) {
                // Both width and height were given, constrain to smallest
                return Math.min(desiredWidth / srcW, desiredHeight / srcH);
            } else if (desiredWidth != 0) {
                return desiredWidth / srcW;
            } else {
                return desiredHeight / srcH;
            }
        } else {
            return 1;
        }
    }

    public void draw(Graphics2D g) {
        AffineTransform originalTransform = g.getTransform();
        // You can only position an image using an affine transformation.
        // We use the transformation to translate the image to the specified
        // position, and scale it according to the given width and height.
        Transform imageTrans = Transform.IDENTITY;
        // Move to the image position. Convert x, y, which are centered coordinates,
        // to "real" coordinates. 
        double factor = getScaleFactor();
        double finalWidth = image.getWidth() * factor;
        double finalHeight = image.getHeight() * factor;
        imageTrans = imageTrans.translate(getX() - finalWidth / 2, getY() - finalHeight / 2);
        // Scaling only applies to image that have their desired width and/or height set.
        // However, getScaleFactor return 1 if height/width are not set, in effect negating
        // the effect of the scale.
        imageTrans = imageTrans.scale(getScaleFactor());
        double a = clamp(alpha);
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a);
        Composite oldComposite = g.getComposite();
        g.setComposite(composite);
        g.drawRenderedImage(image, imageTrans.getAffineTransform());
        g.setComposite(oldComposite);
        g.setTransform(originalTransform);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(image, position, desiredWidth, desiredHeight, alpha, transform);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof Image)) return false;

        final Image other = (Image) o;
        return Objects.equal(image, other.image)
                && Objects.equal(position, other.position)
                && Objects.equal(desiredWidth, other.desiredWidth)
                && Objects.equal(alpha, other.alpha)
                && Objects.equal(transform, other.transform);
    }

    @Override
    public String toString() {
        return "<Image (" + getWidth() + ", " + getHeight() + ")>";
    }

}
