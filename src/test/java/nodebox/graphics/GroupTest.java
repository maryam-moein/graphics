package nodebox.graphics;

import com.google.common.collect.Iterables;
import org.junit.Test;

import static junit.framework.Assert.*;

public class GroupTest {

    @Test
    public void testBounds() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        Group g = Group.of(p);
        assertEquals(new Rect(10, 20, 30, 40), g.getBounds());
    }

    @Test
    public void testTransformedBounds() {
        Path r1 = Path.EMPTY.rect(10, 20, 30, 40);
        Transform t = Transform.translateTransform(200, 300);
        Path r2 = r1.transform(t);
        Group g = Group.of(r2);
        assertEquals(new Rect(210, 320, 30, 40), g.getBounds());
    }

    /**
     * Check the bounds of a group with a normal and an empty path.
     */
    @Test
    public void testEmptyBounds() {
        assertEquals(new Rect(), Group.of().getBounds());
        Path p1 = Path.EMPTY.rect(100, 200, 30, 40);
        Group g1 = Group.of(p1);
        Rect r = new Rect(100, 200, 30, 40);
        assertEquals(r, g1.getBounds());
        Path p2 = Path.EMPTY;
        Group g2 = Group.of(p1, p2);
        assertEquals(r, g2.getBounds());
    }

    /**
     * Check if a contour is empty.
     */
    @Test
    public void testIsEmpty() {
        Group g1 = Group.of();
        assertTrue(g1.isEmpty());
        // Adding even an empty path makes the geometry not empty.
        Group g2 = Group.of(Path.EMPTY);
        assertFalse(g2.isEmpty());
    }

    @Test
    public void testTransformedElements() {
        Path r1 = Path.EMPTY.rect(10, 20, 30, 40);
        Path r2 = Path.EMPTY.rect(10, 120, 30, 40);
        Group g = Group.of(r1, r2);
        Rect rect1 = new Rect(10, 20, 30, 40);
        Rect rect2 = new Rect(10, 120, 30, 40);
        assertEquals(rect1.united(rect2), g.getBounds());
    }

    /**
     * Group added to the group is not cloned. Test if you can still change
     * the original geometry.
     */
    @Test
    public void testAdd() {
        Path p = Path.EMPTY.rect(10, 20, 30, 40);
        Group g = Group.of(p);
        Path p2 = p.transform(Transform.translateTransform(5, 7));
        // Since paths are immutable, the path in the group still has the original bounds.
        // the bounds of the group will be those of the translated path.
        assertEquals(new Rect(15, 27, 30, 40), p2.getBounds());
        assertEquals(new Rect(10, 20, 30, 40), g.getBounds());
    }

    @Test
    public void testTranslatePointsOfGroup() {
        Path p1 = Path.EMPTY.rect(10, 20, 30, 40);
        Path p2 = Path.EMPTY.rect(40, 20, 30, 40);
        Group g = Group.of(p1, p2);
        assertEquals(new Rect(10, 20, 60, 40), g.getBounds());
        Transform t = Transform.translateTransform(5, 7);
        Group g2 = t.map(g);
        assertEquals(new Rect(15, 27, 60, 40), g2.getBounds());
    }

    @Test
    public void testColors() {
        Path p1 = Path.EMPTY.rect(0, 0, 100, 100);
        Path p2 = Path.EMPTY.rect(150, 150, 100, 100);
        Group g = Group.of(p1, p2);
        assertEquals(2, g.size());
        // Each path has 4 points.
        assertEquals(8, Iterables.size(g.getPoints()));
        Color red = new Color(1, 0, 0);
        // TODO Do we want to change the fill on a group?
        //g.setFill(red);
        //assertEquals(red, p1.getFill());
        //assertEquals(red, p2.getFill());
    }

//    @Test
//    public void testMakePoints() {
//        // Create a continuous line from 0,0 to 100,0.
//        // The line is composed of one path from 0-50
//        // and another path with two contours, from 50-75 and 75-100.
//        Path p1 = new Path();
//        p1.line(0, 0, 50, 0);
//        Path p2 = new Path();
//        p2.line(50, 0, 75, 0);
//        p2.line(75, 0, 100, 0);
//        Group g = new Group();
//        g.add(p1);
//        g.add(p2);
//        assertEquals(100.0, g.getLength());
//        Point[] points = g.makePoints(5);
//        assertPointEquals(0, 0, points[0]);
//        assertPointEquals(25, 0, points[1]);
//        assertPointEquals(50, 0, points[2]);
//        assertPointEquals(75, 0, points[3]);
//        assertPointEquals(100, 0, points[4]);
//        // Achieve the same result using resampleByAmount.
//        Group resampledGroup = g.resampleByAmount(5, false);
//        List<Point> resampledPoints = resampledGroup.getPoints();
//        assertPointEquals(0, 0, resampledPoints.get(0));
//        assertPointEquals(25, 0, resampledPoints.get(1));
//        assertPointEquals(50, 0, resampledPoints.get(2));
//        assertPointEquals(75, 0, resampledPoints.get(3));
//        assertPointEquals(100, 0, resampledPoints.get(4));
//    }

//    /**
//     * Group uses a path length cache to speed up pointAt, makePoints and resample operations.
//     * Check if the cache is properly invalidated.
//     */
//    @Test
//    public void testCacheInvalidation() {
//        Group g = new Group();
//        assertEquals(0.0, g.getLength());
//        Path p1 = new Path();
//        p1.line(0, 0, 50, 0);
//        g.add(p1);
//        assertEquals(50.0, g.getLength());
//        // Change the Path after it was added to the Group.
//        p1.line(50, 0, 75, 0);
//        // This change is not detected by the Group, and thus the length is not updated.
//        assertEquals(50.0, g.getLength());
//        // Manually invalidate the group.
//        g.invalidate();
//        // This time, the length is correct.
//        assertEquals(75.0, g.getLength());
//    }
//
//    @Test
//    public void testLength() {
//        Group g = new Group();
//        Path p1 = new Path();
//        p1.line(0, 0, 100, 0);
//        Path p2 = new Path();
//        p2.line(0, 100, 100, 100);
//        g.add(p1);
//        g.add(p2);
//        assertEquals(200.0, g.getLength());
//    }

}
