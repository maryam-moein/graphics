package nodebox.graphics;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.Iterator;

public class Text implements GraphicsElement {

    private final static String DEFAULT_FONT_NAME = "Helvetica";
    private final static double DEFAULT_FONT_SIZE = 24;
    private final static double DEFAULT_LINE_HEIGHT = 1.2;
    private final static Align DEFAULT_ALIGN = Align.LEFT;
    private final static Color DEFAULT_FILL_COLOR = Color.BLACK;
    private final String text;
    private final Point position;
    private final double width;
    private final String fontName;
    private final double fontSize;
    private final double lineHeight;
    private final Align align;
    private final Color fill;
    private final Transform transform;

    private Text(String text, Point position, double width, String fontName, double fontSize, double lineHeight, Align align, Color fill, Transform transform) {
        this.text = text;
        this.position = position;
        this.width = width;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.lineHeight = lineHeight;
        this.align = align;
        this.fill = fill;
        this.transform = transform;
    }

    public static Text create(String text) {
        return new Text(text, Point.ZERO, 0, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, DEFAULT_LINE_HEIGHT, DEFAULT_ALIGN, DEFAULT_FILL_COLOR, Transform.IDENTITY);
    }

    public static Text create(String text, Point position) {
        return new Text(text, position, 0, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, DEFAULT_LINE_HEIGHT, DEFAULT_ALIGN, DEFAULT_FILL_COLOR, Transform.IDENTITY);
    }

    public static Text create(String text, double x, double y) {
        return create(text, new Point(x, y));
    }

    //// Getters /////

    public static boolean fontExists(String fontName) {
        // TODO: Move getAllFonts() in static attribute.
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = env.getAllFonts();
        for (Font font : allFonts) {
            if (font.getName().equals(fontName)) {
                return true;
            }
        }
        return false;
    }

    public String getText() {
        return text;
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

    public double getWidth() {
        return width;
    }

    public String getFontName() {
        return fontName;
    }

    public double getFontSize() {
        return fontSize;
    }

    public Font getFont() {
        return new Font(fontName, Font.PLAIN, (int) fontSize);
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public Align getAlign() {
        return align;
    }

    public Color getFill() {
        return fill;
    }

    //// "Mutation" methods ////

    public Transform getTransform() {
        return transform;
    }

    public Text text(String text) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text position(Point position) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text x(double x) {
        Point p = new Point(x, position.y);
        return position(p);
    }

    public Text y(double y) {
        Point p = new Point(position.x, y);
        return position(p);
    }

    public Text width(double width) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text fontName(String fontName) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text fontSize(double fontSize) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text font(String fontName, double fontSize) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text lineHeight(double lineHeight) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text align(Align align) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, transform);
    }

    public Text fillColor(Color fillColor) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fillColor, transform);
    }

    public Text transform(Transform transform) {
        return new Text(text, position, width, fontName, fontSize, lineHeight, align, fill, this.transform.concatenate(transform));
    }

    //// Font management ////

    private AttributedString getStyledText(String text) {
        // TODO: Find a better way to handle empty Strings (like for example paragraph line breaks)
        if (text.length() == 0)
            text = " ";
        AttributedString attrString = new AttributedString(text);
        attrString.addAttribute(TextAttribute.FONT, getFont());
        if (fill != null)
            attrString.addAttribute(TextAttribute.FOREGROUND, fill.toAwtColor());
        if (align == Align.RIGHT) {
            //attrString.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        } else if (align == Align.CENTER) {
            // TODO: Center alignment?
        } else if (align == Align.JUSTIFY) {
            attrString.addAttribute(TextAttribute.JUSTIFICATION, TextAttribute.JUSTIFICATION_FULL);
        }
        return attrString;
    }

    //// Metrics ////

    public Rect getMetrics() {
        if (text == null || text.length() == 0) return new Rect();
        TextLayoutIterator iterator = new TextLayoutIterator();
        Rectangle2D bounds = new Rectangle2D.Double();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            // TODO: Compensate X, Y
            bounds = bounds.createUnion(layout.getBounds());
        }
        return new Rect(bounds);
    }

    //// Transformations ////

    public void draw(Graphics2D g) {
        if (fill == null) return;
        AffineTransform savedTransform = new AffineTransform(g.getTransform());
        g.getTransform().concatenate(transform.getAffineTransform());
        if (text == null || text.length() == 0) return;
        TextLayoutIterator iterator = new TextLayoutIterator();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            layout.draw(g, (float) (getX() + iterator.getX()), (float) (getY() + iterator.getY()));
        }
        g.setTransform(savedTransform);
    }

    public Path getPath() {
        Path p = Path.EMPTY;
        p = p.fill(fill == null ? null : fill);
        TextLayoutIterator iterator = new TextLayoutIterator();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            AffineTransform trans = new AffineTransform();
            trans.translate(getX() + iterator.getX(), getY() + iterator.getY());
            java.awt.Shape shape = layout.getOutline(trans);
            p = p.extend(shape);
        }
        p = p.transform(getTransform());
        return p;
    }

    public boolean isEmpty() {
        return text.trim().length() == 0;
    }

    public Rect getBounds() {
        // TODO: This is correct, but creating a full path just for measuring bounds is slow.
        return getPath().getBounds();
    }

    public enum Align {
        LEFT, RIGHT, CENTER, JUSTIFY
    }

    private class TextLayoutIterator implements Iterator<TextLayout> {

        private double x, y;
        private double ascent;
        private int currentIndex = 0;
        private String[] textParts;
        private LineBreakMeasurer[] measurers;
        private LineBreakMeasurer currentMeasurer;
        private String currentText;
        private boolean first;

        private TextLayoutIterator() {
            x = 0;
            y = 0;
            textParts = text.split("\n");
            measurers = new LineBreakMeasurer[textParts.length];
            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            for (int i = 0; i < textParts.length; i++) {
                AttributedString s = getStyledText(textParts[i]);
                measurers[i] = new LineBreakMeasurer(s.getIterator(), frc);
            }
            currentMeasurer = measurers[currentIndex];
            currentText = textParts[currentIndex];
            first = true;
        }

        public boolean hasNext() {
            if (currentMeasurer.getPosition() < currentText.length())
                return true;
            else {
                currentIndex++;
                if (currentIndex < textParts.length) {
                    currentMeasurer = measurers[currentIndex];
                    currentText = textParts[currentIndex];
                    return hasNext();
                } else {
                    return false;
                }
            }
        }

        public TextLayout next() {
            if (first) {
                first = false;
            } else {
                y += ascent * lineHeight;
            }
            double layoutWidth = width == 0 ? Float.MAX_VALUE : width;

            TextLayout layout = currentMeasurer.nextLayout((float) layoutWidth);
            if (width == 0) {
                layoutWidth = layout.getAdvance();
                if (align == Align.RIGHT) {
                    x = -layoutWidth;
                } else if (align == Align.CENTER) {
                    x = -layoutWidth / 2.0;
                }
            } else if (align == Align.RIGHT) {
                x = width - layout.getAdvance();
            } else if (align == Align.CENTER) {
                x = (width - layout.getAdvance()) / 2.0;
            } else if (align == Align.JUSTIFY) {
                // Don't justify the last line.
                if (currentMeasurer.getPosition() < currentText.length()) {
                    layout = layout.getJustifiedLayout((float) width);
                }
            }
            ascent = layout.getAscent();
            // y += layout.getDescent() + layout.getLeading() + layout.getAscent();

            return layout;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void remove() {
            throw new AssertionError("This operation is not implemented");
        }
    }

}
