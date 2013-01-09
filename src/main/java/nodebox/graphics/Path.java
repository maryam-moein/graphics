package nodebox.graphics;

import clojure.lang.PersistentVector;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static nodebox.graphics.Geometry.*;
import static nodebox.graphics.PathElement.*;

public final class Path implements Shape {

    public static final Path EMPTY = new Path(Collections.<PathElement>emptyList(), Color.BLACK, null, 0);
    public static final int DEFAULT_CURVE_ACCURACY = 20;
    private final PersistentVector elements;
    private final Color fill;
    private final Color stroke;
    private final double strokeWidth;
    private transient int curveAccuracy = 0;
    private transient double pathLength = -1;
    private transient List<PathSegment> pathSegments;

    private Path(List<PathElement> elements, Color fill, Color stroke, double strokeWidth) {
        this.elements = PersistentVector.create(elements);
        this.fill = fill;
        this.stroke = stroke;
        this.strokeWidth = strokeWidth;
    }

    public static Path fromShape(java.awt.Shape s) {
        return fromShape(s, Color.BLACK, null, 0);
    }

    public static Path pieArc(double x, double y, double w, double h, double startAngle, double degrees) {
        return fromShape(new Arc2D.Double(x, y, w, h, -startAngle, -degrees, Arc2D.PIE));
    }

    public static Path fromShape(java.awt.Shape s, Color fill, Color stroke, double strokeWidth) {
        PathIterator iterator = s.getPathIterator(new AffineTransform());
        LinkedList<PathElement> commands = new LinkedList<PathElement>();
        double px = 0;
        double py = 0;
        while (!iterator.isDone()) {
            double[] points = new double[6];
            int cmd = iterator.currentSegment(points);
            if (cmd == PathIterator.SEG_MOVETO) {
                px = points[0];
                py = points[1];
                commands.add(moveToCommand(px, py));
            } else if (cmd == PathIterator.SEG_LINETO) {
                px = points[0];
                py = points[1];
                commands.add(lineToCommand(px, py));
            } else if (cmd == PathIterator.SEG_QUADTO) {
                // Convert the quadratic bézier to a cubic bézier.
                double c1x = px + (points[0] - px) * 2 / 3;
                double c1y = py + (points[1] - py) * 2 / 3;
                double c2x = points[0] + (points[2] - points[0]) / 3;
                double c2y = points[1] + (points[3] - points[1]) / 3;
                px = points[2];
                py = points[3];
                commands.add(curveToCommand(c1x, c1y, c2x, c2y, px, py));
            } else if (cmd == PathIterator.SEG_CUBICTO) {
                px = points[4];
                py = points[5];
                commands.add(curveToCommand(points[0], points[1], points[2], points[3], px, py));
            } else if (cmd == PathIterator.SEG_CLOSE) {
                px = py = 0;
                commands.add(closeCommand());
            } else {
                throw new AssertionError("Unknown path command " + cmd);
            }
            iterator.next();
        }
        return new Path(commands, fill, stroke, strokeWidth);
    }

    public static Path fromPoints(List<Point> points, boolean closed) {
        Path p = Path.EMPTY;
        if (points.size() == 0) {
            return p;
        }

        p = p.moveTo(points.get(0));
        for (int i = 1; i < points.size(); i++) {
            p = p.lineTo(points.get(i));
        }

        if (closed)
            p = p.close();
        return p;
    }

    /**
     * Create a line at the given starting point with the end point calculated by the angle and distance.
     */
    public Path lineAngle(double x, double y, double distance, double angle) {
        Point p2 = coordinates(x, y, distance, angle);
        return line(x, y, p2.x, p2.y);
    }

    public Path rect(Point p, double w, double h) {
        return rect(p.x, p.y, w, h);
    }

    public Path rect(Rect r) {
        return rect(r.x, r.y, r.width, r.height);
    }

    public Path rect(double x, double y, double w, double h) {
        return moveTo(x, y)
                .lineTo(x + w, y)
                .lineTo(x + w, y + h)
                .lineTo(x, y + h)
                .close();
    }

    public Path centerRect(Point p, double w, double h) {
        return centerRect(p.x, p.y, w, h);
    }

    public Path centerRect(double cx, double cy, double w, double h) {
        double w2 = w / 2;
        double h2 = h / 2;
        return moveTo(cx - w2, cy - h2)
                .lineTo(cx + w2, cy - h2)
                .lineTo(cx + w2, cy + h2)
                .lineTo(cx - w2, cy + h2)
                .close();
    }

    public Path quad(Point p1, Point p2, Point p3, Point p4) {
        return quad(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public Path quad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return moveTo(x1, y1)
                .lineTo(x2, y2)
                .lineTo(x3, y3)
                .lineTo(x4, y4)
                .close();
    }

    public Path ellipse(Point p, double w, double h) {
        return ellipse(p.x, p.y, w, h);
    }

    public Path ellipse(Rect r) {
        return ellipse(r.x, r.y, r.width, r.height);
    }

    public Path ellipse(double x, double y, double w, double h) {
        return extend(new Ellipse2D.Double(x, y, w, h));
    }

    public Path centerEllipse(Point p, double w, double h) {
        return centerEllipse(p.x, p.y, w, h);
    }

    public Path centerEllipse(double cx, double cy, double w, double h) {
        double w2 = w / 2;
        double h2 = h / 2;
        return extend(new Ellipse2D.Double(cx - w2, cy - h2, w, h));
    }

    public Path chordArc(double x, double y, double w, double h, double startAngle, double degrees) {
        return extend(new Arc2D.Double(x, y, w, h, -startAngle, -degrees, Arc2D.CHORD));
    }

    public Path openArc(double x, double y, double w, double h, double startAngle, double degrees) {
        return extend(new Arc2D.Double(x, y, w, h, -startAngle, -degrees, Arc2D.OPEN));
    }

    public Path line(Point p0, Point p1) {
        return line(p0.x, p0.y, p1.x, p1.y);
    }

    public Path line(double x0, double y0, double x1, double y1) {
        return moveTo(x0, y0).lineTo(x1, y1);
    }

    /**
     * Create a line at the given starting point with the end point calculated by the angle and distance.
     */
    public Path lineAngle(Point p, double distance, double angle) {
        Point p2 = coordinates(p, distance, angle);
        return line(p, p2);
    }

    public int count() {
        return elements.size();
    }

    @SuppressWarnings("unchecked")
    public List<PathElement> getElements() {
        return elements;
    }

    public List<Point> getPoints() {
        ImmutableList.Builder<Point> points = ImmutableList.builder();
        for (Object o : elements) {
            PathElement c = (PathElement) o;
            if (c.getCommand() == Command.CLOSE) {
                continue;
            }
            points.add(c.point);
        }
        return points.build();
    }

    public List<Path> getContours() {
        ImmutableList.Builder<Path> contours = ImmutableList.builder();
        Path currentContour = Path.EMPTY;
        boolean empty = true;
        for (Object o : elements) {
            PathElement c = (PathElement) o;
            if (c.command == Command.MOVE_TO) {
                if (!empty) {
                    contours.add(currentContour);
                }
                currentContour = Path.EMPTY.addCommand(c);
                empty = true;
            } else if (c.command == Command.LINE_TO) {
                empty = false;
                currentContour = currentContour.addCommand(c);
            } else if (c.command == Command.CURVE_TO) {
                empty = false;
                currentContour = currentContour.addCommand(c);
            } else if (c.command == Command.CLOSE) {
                currentContour = currentContour.addCommand(c);
            }
        }
        if (!empty) {
            contours.add(currentContour);
        }
        return contours.build();
    }


    //// Geometric queries ////

    public Rect getBounds() {
        return new Rect(toGeneralPath().getBounds());
    }

    public Point getCentroid() {
        return getBounds().getCentroid();
    }

    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    public boolean contains(double x, double y) {
        return toGeneralPath().contains(x, y);
    }

    public boolean contains(Rect r) {
        return toGeneralPath().contains(r.toRectangle2D());
    }

    /**
     * Fit this path to the specified bounds.
     * <p/>
     * The path will not be stretched.
     */
    public Path fit(Rect r) {
        return fit(r, true);
    }

    /**
     * Fit this path to the specified bounds.
     *
     * @param r                    The bounds to fit the path in.
     * @param constrainProportions If true, the path will not be stretched.
     */
    public Path fit(Rect r, boolean constrainProportions) {
        Rect bounds = getBounds();

        // Make sure bw and bh aren't infinitely small numbers.
        // This will lead to incorrect transformations with for examples lines.
        double bw = bounds.width > 0.000000000001 ? bounds.width : 0;
        double bh = bounds.height > 0.000000000001 ? bounds.height : 0;

        Transform t = Transform.IDENTITY;
        t = t.translate(r.x, r.y);
        double sx, sy;
        if (constrainProportions) {
            // Don't scale width or height that are equal to zero.
            sx = bw > 0 ? r.width / bw : Double.MAX_VALUE;
            sy = bh > 0 ? r.height / bh : Double.MAX_VALUE;
            sx = sy = Math.min(sx, sy);
        } else {
            sx = bw > 0 ? r.width / bw : 1;
            sy = bh > 0 ? r.height / bh : 1;
        }
        t = t.scale(sx, sy);
        t = t.translate(-bounds.x, -bounds.y);
        return transform(t);
    }


    //// Boolean operations ////

    public boolean intersects(Rect r) {
        return toGeneralPath().intersects(r.toRectangle2D());
    }

    public boolean intersects(Path p) {
        Area a1 = new Area(toGeneralPath());
        Area a2 = new Area(p.toGeneralPath());
        a1.intersect(a2);
        return !a1.isEmpty();
    }

    public Path intersect(Path p) {
        Area a1 = new Area(toGeneralPath());
        Area a2 = new Area(p.toGeneralPath());
        a1.intersect(a2);
        return Path.EMPTY.extend(a1);
    }

    public Path subtract(Path p) {
        Area a1 = new Area(toGeneralPath());
        Area a2 = new Area(p.toGeneralPath());
        a1.subtract(a2);
        return Path.EMPTY.extend(a1);
    }

    public Path unite(Path p) {
        Area a1 = new Area(toGeneralPath());
        Area a2 = new Area(p.toGeneralPath());
        a1.add(a2);
        return Path.EMPTY.extend(a1);
    }


    //// AWT Interop ///

    public GeneralPath toGeneralPath() {
        GeneralPath gp = new GeneralPath();
        for (Object o : elements) {
            PathElement c = (PathElement) o;
            if (c.command == Command.MOVE_TO) {
                gp.moveTo(c.point.x, c.point.y);
            } else if (c.command == Command.LINE_TO) {
                gp.lineTo(c.point.x, c.point.y);
            } else if (c.command == Command.CURVE_TO) {
                gp.curveTo(c.control1.x, c.control1.y, c.control2.x, c.control2.y, c.point.x, c.point.y);
            } else if (c.command == Command.CLOSE) {
                gp.closePath();
            } else {
                throw new AssertionError("Unknown command " + c.command);
            }
        }
        return gp;
    }

    @Override
    public void draw(Graphics2D g) {
        GeneralPath gp = toGeneralPath();
        if (fill != null) {
            g.setColor(fill.toAwtColor());
            g.fill(gp);
        }
        if (stroke != null) {
            g.setColor(stroke.toAwtColor());
            g.setStroke(new BasicStroke((float) strokeWidth));
            g.draw(gp);
        }
    }

    public Path moveTo(Point p) {
        return moveTo(p.x, p.y);
    }

    public Path moveTo(double x, double y) {
        return elements(elements.cons(moveToCommand(x, y)));
    }

    public Path lineTo(Point p) {
        return lineTo(p.x, p.y);
    }

    public Path lineTo(double x, double y) {
        return elements(elements.cons(lineToCommand(x, y)));
    }

    public Path curveTo(Point p0, Point p1, Point p2) {
        return curveTo(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
    }

    public Path curveTo(double c1x, double c1y, double c2x, double c2y, double x, double y) {
        return elements(elements.cons(curveToCommand(c1x, c1y, c2x, c2y, x, y)));
    }

    public Path close() {
        return elements(elements.cons(closeCommand()));
    }

    public Path addCommand(PathElement c) {
        return elements(elements.cons(c));
    }

    public Path extend(Path p) {
        return extend(p.getElements());
    }

    public Path extend(List<PathElement> commands) {
        ArrayList<PathElement> newCommands = new ArrayList<PathElement>(this.elements.size() + commands.size());
        newCommands.addAll(this.elements);
        newCommands.addAll(commands);
        return elements(newCommands);
    }

    public Path extend(java.awt.Shape s) {
        return extend(Path.fromShape(s));
    }

    //// Style ////

    public void setStyle(String styleKey, String styleValue) {
        // TODO Implement fill / stroke / opacity
        // http://www.w3.org/TR/SVG/color.html
        // http://www.w3.org/TR/SVG/styling.html
    }


    //// Mapping operations ////

    public Path mapCommands(Function<PathElement, PathElement> fn) {
        ArrayList<PathElement> newCommands = new ArrayList<PathElement>();
        for (Object o : elements) {
            PathElement c = (PathElement) o;
            PathElement newCommand = fn.apply(c);
            newCommands.add(newCommand);
        }
        return elements(newCommands);
    }


    //// "Mutation" methods ////

    private Path elements(List<PathElement> elements) {
        return new Path(elements, fill, stroke, strokeWidth);
    }

    public Path fill(Color fill) {
        return new Path(elements, fill, stroke, strokeWidth);
    }

    public Path stroke(Color stroke) {
        return new Path(elements, fill, stroke, strokeWidth);
    }

    public Path strokeWidth(double strokeWidth) {
        return new Path(elements, fill, stroke, strokeWidth);
    }

    //// Transformation ////

    public Path transform(Transform t) {
        return t.map(this);
    }

    public Path translate(double tx, double ty) {
        Transform t = Transform.IDENTITY.translate(tx, ty);
        return transform(t);
    }

    //// Color ////

    public Color getFill() {
        return fill;
    }

    public Color getStroke() {
        return stroke;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }


    //// Geometric math ////

    /**
     * Calculate the length horizontal the path. This is not the number horizontal segments, but rather the sum horizontal all segment lengths.
     *
     * @return the length horizontal the path.
     */
    public double getLength() {
        return getLength(DEFAULT_CURVE_ACCURACY);
    }

    /**
     * Calculate the length horizontal the path. This is not the number horizontal segments, but rather the sum horizontal all segment lengths.
     *
     * @return the length horizontal the path.
     */
    public double getLength(int n) {
        // We call this just to get the path
        computePathSegmentsAndLength(n);
        if (pathLength < 0) {
            throw new AssertionError("Path length calculation was not done, or done incorrectly.");
        }
        return pathLength;
    }

    private List<PathSegment> getPathSegments(int n) {
        computePathSegmentsAndLength(n);
        return pathSegments;
    }

    /**
     * Calculate the path segments and path length.
     * <p/>
     * Segments do not correspond to path elements, but contain enough info to calculate a point on the segment.
     *
     * @param n The amount horizontal points to sample for curves. The more points, the more accurate the length.
     * @return An immutable list horizontal path segments.
     */
    private void computePathSegmentsAndLength(int n) {
        // If the user requests a different accuracy than we already have, we recompute the points.
        if (pathSegments != null && curveAccuracy == n) {
            return;
        }
        if (elements.isEmpty()) {
            pathSegments = Collections.emptyList();
            pathLength = 0;
            curveAccuracy = n;
            return;
        }

        ImmutableList.Builder<PathSegment> segments = ImmutableList.builder();
        PathElement firstCommand = (PathElement) elements.get(0);
        checkState(firstCommand.command == Command.MOVE_TO, "First command in path needs to be MOVETO.");
        Point startPoint = null;
        Point previousPoint = null;
        double totalLength = 0;

        for (Object o : elements) {
            PathElement c = (PathElement) o;
            if (c.getCommand() == Command.MOVE_TO) {
                startPoint = c.point;
            } else if (c.getCommand() == Command.LINE_TO) {
                double length = lineLength(previousPoint, c.point);
                segments.add(new PathSegment(previousPoint, PathSegmentType.LINE, c, length));
                totalLength += length;
            } else if (c.getCommand() == Command.CURVE_TO) {
                double length = curveLength(previousPoint, c.control1, c.control2, c.point, n);
                segments.add(new PathSegment(previousPoint, PathSegmentType.CURVE, c, length));
                totalLength += length;
            } else if (c.getCommand() == Command.CLOSE) {
                double length = lineLength(previousPoint, startPoint);
                segments.add(new PathSegment(previousPoint, PathSegmentType.LINE, PathElement.lineToCommand(startPoint), length));
                totalLength += length;
            }

            previousPoint = c.point;
        }

        pathSegments = segments.build();
        pathLength = totalLength;
        curveAccuracy = n;
    }

    /**
     * Calculate coordinates for point at t on the path.
     * <p/>
     * Gets the length horizontal the path, based on the length
     * horizontal each curve and line in the path.
     * Determines in what segment t falls.
     * Gets the point on that segment.
     *
     * @param t relative coordinate horizontal the point (between 0.0 and 1.0)
     *          Results outside horizontal this range are clamped.
     * @return coordinates for point at t.
     */
    public Point pointAt(double t) {
        double totalLength = getLength();
        double absoluteT = totalLength * clamp(t);

        for (PathSegment segment : getPathSegments(DEFAULT_CURVE_ACCURACY)) {
            if (absoluteT <= segment.absoluteLength) {
                double relativeT = absoluteT / segment.absoluteLength;
                if (segment.type == PathSegmentType.LINE) {
                    return linePoint(relativeT, segment.startPoint, segment.endCommand.point);
                } else {
                    return curvePoint(relativeT, segment.startPoint, segment.endCommand.control1, segment.endCommand.control2, segment.endCommand.point);
                }
            }
            absoluteT -= segment.absoluteLength;
        }

        return Point.ZERO;
    }

    /**
     * Same as pointAt(t).
     * <p/>
     * This method is here for compatibility with NodeBox 1.
     *
     * @param t relative coordinate horizontal the point.
     * @return coordinates for point at t.
     * @see #pointAt(double)
     */
    public Point point(double t) {
        return pointAt(t);
    }

    /**
     * Calculate new points along the given path.
     *
     * @param amount The amount horizontal points to create.
     * @return A list horizontal points.
     */
    public List<Point> makePoints(int amount) {
        ImmutableList.Builder<Point> points = ImmutableList.builder();
        double delta = amount <= 1 ? 0 : 1.0 / (amount - 1);
        for (int i = 0; i < amount; i++) {
            points.add(pointAt(i * delta));
        }
        return points.build();
    }

    /**
     * Create a new path where the curve is recreated using a given amount horizontal lines.
     *
     * @param amount The amount horizontal lines to use.
     * @return A new path.
     */
    public Path resampleByAmount(int amount) {
        return Path
                .fromPoints(makePoints(amount), isClosed())
                .fill(fill)
                .stroke(stroke)
                .strokeWidth(strokeWidth);
    }

    /**
     * Create a new path where the shape horizontal the path is recreated using lines horizontal the same length.
     *
     * @param segmentLength The maximum length horizontal each segment. Note that the last segment horizontal each contour can be shorter.
     * @return A new path.
     */
    public Path resampleByLength(double segmentLength) {
        ArrayList<PathElement> newCommands = new ArrayList<PathElement>();
        for (Path c : getContours()) {
            newCommands.addAll(c.resampleContourByLength(segmentLength));
        }
        return elements(newCommands);
    }

    private List<PathElement> resampleContourByLength(double segmentLength) {
        int amount = (int) Math.ceil(getLength() / segmentLength);
        double delta = amount <= 1 ? 0 : 1.0 / (amount - 1);

        ArrayList<PathElement> commands = new ArrayList<PathElement>(amount);
        for (int i = 0; i < amount; i++) {
            if (i == 0) {
                commands.add(moveToCommand(pointAt(i * delta)));
            } else {
                commands.add(lineToCommand(pointAt(i * delta)));
            }
        }
        if (isClosed()) {
            commands.add(closeCommand());
        }
        return commands;
    }

    /**
     * Check if the last element horizontal a Path is a CLOSE command.
     */
    private boolean isClosed() {
        if (elements.isEmpty()) return false;
        PathElement lastCommand = (PathElement) elements.get(elements.size() - 1);
        return lastCommand.command == Command.CLOSE;
    }

    private enum PathSegmentType {LINE, CURVE}

    /**
     * The path segment is an internal data structure that has all needed information to calculate a pointAt.
     * It divides a path up in curves and lines, each with start and end points.
     */
    private class PathSegment {
        private final Point startPoint;
        private final PathSegmentType type;
        private final PathElement endCommand;
        private final double absoluteLength;

        private PathSegment(Point startPoint, PathSegmentType type, PathElement endCommand, double absoluteLength) {
            this.startPoint = startPoint;
            this.type = type;
            this.endCommand = endCommand;
            this.absoluteLength = absoluteLength;
        }
    }

}
