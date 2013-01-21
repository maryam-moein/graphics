package nodebox.graphics;

import com.google.common.base.Function;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static nodebox.graphics.Assertions.assertPointEquals;

public class PathTest {

    public static final double SIDE = 50;

    @Test
    public void testEmptyPath() {
        Path p = Path.EMPTY;
        assertEquals(0, p.getElements().size());
        assertEquals(0, p.count());
    }

    @Test
    public void testMakeRect() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        assertEquals(new Rect(10, 20, 30, 40), p.getBounds());
        assertEquals(5, p.count());
    }

    @Test
    public void testMakeEllipse() {
        Path p = Path.EMPTY.ellipse(10, 20, 30, 40);
        assertEquals(new Rect(10, 20, 30, 40), p.getBounds());
        assertEquals(6, p.count());
    }

    @Test
    public void testTranslatePoints() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        Path p2 = p.translate(5, 0);
        assertEquals(new Rect(15, 20, 30, 40), p2.getBounds());
    }

    @Test
    public void testGetTranslatedElements() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        List<PathElement> elements = p.getElements();
        assertEquals(PathElement.moveToElement(10, 20), elements.get(0));
        assertEquals(PathElement.lineToElement(40, 20), elements.get(1));
        assertEquals(PathElement.lineToElement(40, 60), elements.get(2));
        assertEquals(PathElement.lineToElement(10, 60), elements.get(3));
        assertEquals(PathElement.closeElement(), elements.get(4));

        Path p2 = p.transform(Transform.translateTransform(100, 200));
        elements = p2.getElements();
        assertEquals(PathElement.moveToElement(110, 220), elements.get(0));
        assertEquals(PathElement.lineToElement(140, 220), elements.get(1));
        assertEquals(PathElement.lineToElement(140, 260), elements.get(2));
        assertEquals(PathElement.lineToElement(110, 260), elements.get(3));
        assertEquals(PathElement.closeElement(), elements.get(4));

        Path p3 = p2.untransformed();
        assertEquals(p, p3);
        elements = p3.getElements();
        assertEquals(PathElement.moveToElement(10, 20), elements.get(0));
    }

    @Test
    public void testGetTranslatedPoints() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        List<Point> points = p.getPoints();
        assertPointEquals(10, 20, points.get(0));
        assertPointEquals(40, 20, points.get(1));
        assertPointEquals(40, 60, points.get(2));
        assertPointEquals(10, 60, points.get(3));

        Path p2 = p.transform(Transform.translateTransform(100, 200));
        points = p2.getPoints();
        assertPointEquals(110, 220, points.get(0));
        assertPointEquals(140, 220, points.get(1));
        assertPointEquals(140, 260, points.get(2));
        assertPointEquals(110, 260, points.get(3));
    }

    @Test
    public void testMakeText() {
        Text t = Text.create("A", 0, 20);
        Path p = t.getPath();
        // The letter "A" has 2 contours: the outer shape and the "hole".
        assertEquals(2, p.getContours().size());
    }

    @Test
    public void testElementsSize() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        assertEquals(5, p.getElements().size());
    }

    @Test
    public void testTransform() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        Path p2 = p.transform(Transform.translateTransform(5, 7));
        assertEquals(new Rect(15, 27, 30, 40), p2.getBounds());
    }

    @Test
    public void testTransformedExtend() {
        Path p1 = Path.EMPTY.moveTo(0, 0);
        Path p2 = Path.EMPTY.lineTo(30, 40);
        Path p3 = p1.extend(p2);
        assertEquals(new Rect(0, 0, 30, 40), p3.getBounds());

        Path p4 = p2.translate(100, 200);
        Path p5 = p1.extend(p4);
        assertEquals(new Rect(0, 0, 130, 240), p5.getBounds());
    }

    @Test
    public void testMapElements() {
        Function<PathElement, PathElement> moveALittle = new Function<PathElement, PathElement>() {
            @Override
            public PathElement apply(PathElement input) {
                return input.translate(1, 2);
            }
        };
        Path p1 = Path.EMPTY.rect(10, 20, 30, 40);
        Path p2 = p1.mapElements(moveALittle);
        assertEquals(new Rect(11, 22, 30, 40), p2.getBounds());

        Path tp1 = p1.translate(100, 200);
        Path tp2 = tp1.mapElements(moveALittle);
        assertEquals(new Rect(111, 222, 30, 40), tp2.getBounds());
    }

    /**
     * How easy is it to convert the contours of a path to paths themselves?
     */
    @Test
    public void testContoursToPaths() {
        // Create a path with two contours.
        Path p = Path.EMPTY.rect(0, 0, 100, 100).rect(150, 150, 100, 100);
        List<Path> contours = p.getContours();
        assertEquals(2, contours.size());
    }

    @Test
    public void testLength() {
        testLength(0, 0);
        testLength(200, 300);
        Path p = Path.EMPTY
                .line(0, 0, 50, 0)
                .line(50, 0, 100, 0);
        assertEquals(100.0, p.getLength());
    }

    private void testLength(float x, float y) {
        Path p = openRect(x, y, SIDE, SIDE);
        assertEquals(SIDE * 3, p.getLength());
        p = p.close();
        assertEquals(SIDE * 4, p.getLength());
    }

    @Test
    public void testLengthMultipleContours() {
        Path p1 = Path.EMPTY.line(0, 0, 0, 100);
        assertEquals(100.0, p1.getLength());
        Path p2 = p1.extend(Path.EMPTY.line(0, 100, 100, 100));
        assertEquals(200.0, p2.getLength());
        Path p3 = p2.close();
        assertEquals(300.0, p3.getLength());
    }

    @Test
    public void testPointAt() {
        Path p = Path.EMPTY
                .line(0, 0, 50, 0)
                .line(50, 0, 100, 0);
        assertEquals(100.0, p.getLength());
        assertPointEquals(0, 0, p.pointAt(0));
        assertPointEquals(10, 0, p.pointAt(0.1));
        assertPointEquals(25, 0, p.pointAt(0.25));
        assertPointEquals(40, 0, p.pointAt(0.4));
        assertPointEquals(50, 0, p.pointAt(0.5));
        assertPointEquals(75, 0, p.pointAt(0.75));
        assertPointEquals(80, 0, p.pointAt(0.8));
        assertPointEquals(100, 0, p.pointAt(1));
    }

    @Test
    public void testTransformedPointAt() {
        Path p = Path.EMPTY
                .line(0, 0, 50, 0)
                .line(50, 0, 100, 0)
                .translate(100, 200);
        assertPointEquals(100, 200, p.pointAt(0.0));
        assertPointEquals(150, 200, p.pointAt(0.5));
        assertPointEquals(200, 200, p.pointAt(1.0));
    }

    @Test
    public void testPointAtCurve() {
        Path p = Path.EMPTY.moveTo(0, 0).curveTo(0, 0, 100, 0, 100, 0);
        assertPointEquals(0, 0, p.pointAt(0));
        assertPointEquals(50, 0, p.pointAt(0.5));
        assertPointEquals(100, 0, p.pointAt(1));
        // if t is outside the 0.0-1.0 range, the results are clamped.
        assertPointEquals(0, 0, p.pointAt(-0.5));
        assertPointEquals(100, 0, p.pointAt(1.5));
    }

    @Test
    public void testPointAtEmptyPath() {
        Path p = Path.EMPTY;
        assertPointEquals(0, 0, p.pointAt(0.1));
    }

    @Test
    public void testPointAtOnePoint() {
        // A single MOVETO command is not a proper path: pointAt returns 0,0.
        Path p = Path.EMPTY.moveTo(33, 44);
        assertPointEquals(0, 0, p.pointAt(0.1));
        assertPointEquals(0, 0, p.pointAt(100));
        assertPointEquals(0, 0, p.pointAt(-12));
    }

    @Test
    public void testPointAtClosed() {
        Path p = openRect(0, 0, SIDE, SIDE);
        assertEquals(SIDE * 3, p.getLength());
        assertPointEquals(0, 0, p.pointAt(0));
        assertPointEquals(SIDE, SIDE / 2, p.pointAt(0.5));
        assertPointEquals(0, SIDE, p.pointAt(1));
        Path p2 = p.close();
        assertEquals(SIDE * 4, p2.getLength());
        assertPointEquals(0, 0, p2.pointAt(0));
        assertPointEquals(SIDE, SIDE, p2.pointAt(0.5));
        assertPointEquals(0, 0, p2.pointAt(1));
    }

    @Test
    public void testPointAtMultiple() {
        Path p = Path.EMPTY
                .moveTo(0, 0)
                .lineTo(50, 0)
                .lineTo(100, 0);
        // Results out of t's 0.0-1.0 range are clamped.
        assertPointEquals(0, 0, p.pointAt(-0.5));
        assertPointEquals(0, 0, p.pointAt(0));
        assertPointEquals(25, 0, p.pointAt(0.25));
        assertPointEquals(50, 0, p.pointAt(0.5));
        assertPointEquals(60, 0, p.pointAt(0.6));
        assertPointEquals(100, 0, p.pointAt(1));
        // Results out of t's 0.0-1.0 range are clamped.
        assertPointEquals(100, 0, p.pointAt(1.5));
    }

    @Test
    public void testMakePoints() {
        final double SIDE = 50;
        List<Point> points;
        Path p1 = openRect(0, 0, SIDE, SIDE);
        assertEquals(SIDE * 3, p1.getLength());
        points = p1.makePoints(7);
        assertPointEquals(0, 0, points.get(0));
        assertPointEquals(SIDE / 2, 0, points.get(1));
        assertPointEquals(SIDE, 0, points.get(2));
        assertPointEquals(0, SIDE, points.get(6));

        // Closing the contour will increase the length of the path and thus will also
        // have an effect on point positions.
        Path p2 = p1.close();
        assertEquals(SIDE * 4, p2.getLength());
        points = p2.makePoints(9);
        assertEquals(new Point(0, 0), points.get(0));
        assertPointEquals(SIDE / 2, 0, points.get(1));
        assertPointEquals(SIDE, 0, points.get(2));
        assertPointEquals(0, SIDE, points.get(6));
        assertPointEquals(0, SIDE / 2, points.get(7));
    }

    @Test
    public void testMakePointsEmptyPath() {
        Path p = Path.EMPTY;
        List<Point> points = p.makePoints(10);
        assertEquals(10, points.size());
        assertPointEquals(0, 0, points.get(0));
        assertPointEquals(0, 0, points.get(9));
    }

    @Test
    public void testIntersected() {
        // Create two non-overlapping rectangles.
        Path p1 = Path.EMPTY.rect(0, 0, 100, 100);
        Path p2 = Path.EMPTY.rect(100, 0, 100, 100);
        // The intersection of the two is empty.
        assertEquals(new Rect(), p1.intersect(p2).getBounds());
        // Create two paths were one is entirely enclosed within the other.
        p1 = Path.EMPTY.rect(0, 0, 100, 100);
        p2 = Path.EMPTY.rect(20, 30, 10, 10);
        // The intersection is the smaller path.
        assertEquals(p2.getBounds(), p1.intersect(p2).getBounds());
    }

    /**
     * Path uses a contour length cache to speed up pointAt, makePoints and resample operations.
     * Check if the cache is properly invalidated.
     */
    @Test
    public void testCacheInvalidation() {
        Path p1 = Path.EMPTY;
        assertEquals(0.0, p1.getLength());
        Path p2 = p1.extend(Path.EMPTY.line(0, 0, 50, 0));
        assertEquals(0.0, p1.getLength());
        assertEquals(50.0, p2.getLength());
    }

    @Test
    public void testNegativeBounds() {
        Path p1 = Path.EMPTY.rect(10, 20, 30, 40);
        assertEquals(new Rect(10, 20, 30, 40), p1.getBounds());

        Path p2 = Path.EMPTY.rect(-80, -200, 100, 100);
        assertEquals(new Rect(-80, -200, 100, 100), p2.getBounds());

        Path p3 = Path.EMPTY.rect(100, 200, -30, -40);
        assertEquals(new Rect(70, 160, 30, 40), p3.getBounds());
    }

    /**
     * Check the bounds for an empty path.
     */
    @Test
    public void testEmptyBounds() {
        Path p1 = Path.EMPTY;
        assertEquals(Rect.EMPTY, p1.getBounds());
        // A path with just a moveTo has no bounds either.
        Path p2 = Path.EMPTY.moveTo(0, 0);
        assertEquals(Rect.EMPTY, p2.getBounds());
        // A path with a horizontal line has no height.
        Path p3 = Path.EMPTY.line(0, 0, 100, 0);
        assertEquals(new Rect(0, 0, 100, 0), p3.getBounds());
    }

    @Test
    public void testResample() {
        Path r;
        List<Point> points;

        Path p1 = Path.EMPTY;
        r = p1.resampleByAmount(10);
        assertEquals(10, r.getElements().size());

        Path p2 = openRect(0, 0, SIDE, SIDE);
        r = p2.resampleByAmount(4);
        assertEquals(4, r.getElements().size());
        points = r.getPoints();
        assertPointEquals(0, 0, points.get(0));
        assertPointEquals(SIDE, 0, points.get(1));
        assertPointEquals(SIDE, SIDE, points.get(2));
        assertPointEquals(0, SIDE, points.get(3));

        Path p3 = p2.close();
        r = p3.resampleByAmount(5);
        assertEquals(6, r.getElements().size());
        points = r.getPoints();
        assertPointEquals(0, 0, points.get(0));
        assertPointEquals(0, SIDE, points.get(3));
        assertEquals(PathElement.CLOSE_ELEMENT, r.getElements().get(5));
    }

    @Test
    public void testResampleByLength() {
        Path r;

        Path p1 = Path.EMPTY;
        r = p1.resampleByLength(1);
        assertEquals(0, r.getElements().size());

        Path p2 = openRect(0, 0, SIDE, SIDE);
        r = p2.resampleByLength(SIDE);
        assertEquals(3, r.getElements().size());
        assertPointEquals(0, 0, r.getElements().get(0).getPoint());
        assertPointEquals(50, 25, r.getElements().get(1).getPoint());
        assertPointEquals(0, 50, r.getElements().get(2).getPoint());

        Path p3 = p2.close();
        r = p3.resampleByLength(SIDE);
        assertEquals(5, r.getElements().size());
        assertPointEquals(0, 0, r.getElements().get(0).getPoint());
        assertPointEquals(50, 16.666, r.getElements().get(1).getPoint());
        assertPointEquals(16.666, 50, r.getElements().get(2).getPoint());
        assertPointEquals(0, 0, r.getElements().get(3).getPoint());
        assertEquals(PathElement.CLOSE_ELEMENT, r.getElements().get(4));
    }

    private Path openRect(double x, double y, double width, double height) {
        return Path.EMPTY
                .moveTo(0, 0)
                .lineTo(width, 0)
                .lineTo(width, height)
                .lineTo(0, height);
    }

}
