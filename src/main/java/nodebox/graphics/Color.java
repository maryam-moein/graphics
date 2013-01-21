package nodebox.graphics;

import com.google.common.base.Objects;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nodebox.graphics.Geometry.clamp;

public final class Color {

    public static final Color BLACK = new Color(0, 0, 0, 1);
    public static final Color BLUE = new Color(0, 0, 1, 1);
    public static final Color CYAN = new Color(0, 1, 1, 1);
    public static final Color GREEN = new Color(0, 1, 0, 1);
    public static final Color MAGENTA = new Color(1, 0, 1, 1);
    public static final Color RED = new Color(1, 0, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color YELLOW = new Color(1, 1, 0, 1);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Pattern HEX_STRING_PATTERN = Pattern.compile("^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})?$");
    public final double r, g, b, a;

    public Color(double r, double g, double b) {
        this(r, g, b, 1);
    }

    public Color(double r, double g, double b, double a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public static Color valueOf(String hex) {
        // We can only parse RGBA hex color values.
        Matcher m = HEX_STRING_PATTERN.matcher(hex);
        if (!m.matches())
            throw new IllegalArgumentException("The given value '" + hex + "' is not of the format #112233ff.");
        int r255 = Integer.parseInt(m.group(1), 16);
        int g255 = Integer.parseInt(m.group(2), 16);
        int b255 = Integer.parseInt(m.group(3), 16);
        int a255;
        if (m.groupCount() >= 5) {
            a255 = Integer.parseInt(m.group(4), 16);
        } else {
            a255 = 255;
        }
        return new Color(r255 / 255.0, g255 / 255.0, b255 / 255.0, a255 / 255.0);
    }

    public boolean isVisible() {
        return a > 0;
    }

    public double getR() {
        return r;
    }

    public double getRed() {
        return r;
    }

    public double getG() {
        return g;
    }

    public double getGreen() {
        return g;
    }

    public double getB() {
        return b;
    }

    public double getBlue() {
        return b;
    }

    public double getA() {
        return a;
    }

    public double getAlpha() {
        return a;
    }

    public java.awt.Color toAwtColor() {
        return new java.awt.Color((float) getRed(), (float) getGreen(), (float) getBlue(), (float) getAlpha());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(r, g, b, a);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof Color)) return false;

        final Color other = (Color) o;
        return Objects.equal(r, other.r)
                && Objects.equal(g, other.g)
                && Objects.equal(b, other.b)
                && Objects.equal(a, other.a);
    }

    public String toHex() {
        String r255 = Integer.toHexString((int) Math.round(r * 255));
        String g255 = Integer.toHexString((int) Math.round(g * 255));
        String b255 = Integer.toHexString((int) Math.round(b * 255));
        String a255 = Integer.toHexString((int) Math.round(a * 255));
        if (r255.length() == 1) r255 = "0" + r255;
        if (g255.length() == 1) g255 = "0" + g255;
        if (b255.length() == 1) b255 = "0" + b255;
        if (a255.length() == 1) a255 = "0" + a255;
        return String.format("#%s%s%s%s", r255, g255, b255, a255);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%.2f,%.2f,%.2f,%.2f]", r, g, b, a);
    }

}
