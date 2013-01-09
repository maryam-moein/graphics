package nodebox.graphics;

import clojure.lang.PersistentVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

public final class Canvas implements GraphicsElement {

    public static final double DEFAULT_WIDTH = 1000;
    public static final double DEFAULT_HEIGHT = 1000;
    public static final Canvas EMPTY = new Canvas(Collections.EMPTY_LIST, Color.TRANSPARENT, DEFAULT_WIDTH, DEFAULT_HEIGHT, Transform.IDENTITY);
    private final PersistentVector elements;
    private final Color background;
    private final double width;
    private final double height;
    private final Transform transform;

    private Canvas(Iterable<GraphicsElement> elements, Color background, double width, double height, Transform transform) {
        this.elements = PersistentVector.create(elements);
        this.background = background;
        this.width = width;
        this.height = height;
        this.transform = transform;
    }

    public Color getBackground() {
        return background;
    }

    @Override
    public Rect getBounds() {
        return new Rect(0, 0, width, height);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    //// "Mutation" methods ////

    public Canvas elements(Iterable<GraphicsElement> elements) {
        return new Canvas(elements, background, width, height, transform);
    }

    public Canvas background(Color c) {
        return new Canvas(elements, c, width, height, transform);
    }

    public Canvas size(double w, double h) {
        return new Canvas(elements, background, w, h, transform);
    }

    @Override
    public GraphicsElement transform(Transform t) {
        return new Canvas(elements, background, width, height, transform.concatenate(t));
    }

    @Override
    public void draw(Graphics2D g) {
        if (background != null) {
            g.setColor(background.toAwtColor());
            g.fill(getBounds().toRectangle2D());
        }
        java.awt.Shape clipShape = g.getClip();
        g.clip(getBounds().toRectangle2D());
        for (Object o : elements) {
            ((GraphicsElement) o).draw(g);
        }
        g.setClip(clipShape);
    }

    public BufferedImage toImage() {
        Rect bounds = getBounds();
        BufferedImage img = new BufferedImage((int) Math.round(bounds.getWidth()), (int) Math.round(bounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(-bounds.getX(), -bounds.getY());
        draw(g);
        img.flush();
        return img;
    }

    public void save(File file) {
        if (file.getName().endsWith(".pdf")) {
            PDFRenderer.render(this, file);
        } else {
            try {
                ImageIO.write(toImage(), getFileExtension(file), file);
            } catch (IOException e) {
                throw new RuntimeException("Could not write image file " + file, e);
            }
        }
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        String ext = null;
        int i = fileName.lastIndexOf('.');

        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase(Locale.US);
        }
        return ext;
    }

    @Override
    public String toString() {
        return "<" + getClass().getSimpleName() + ": " + width + ", " + height + ">";
    }

}
