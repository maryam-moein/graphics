package nodebox.graphics;

import static junit.framework.Assert.assertEquals;

public class Assertions {

    public static void assertPointEquals(double x, double y, Point actual) {
        assertEquals(x, actual.x, 0.001);
        assertEquals(y, actual.y, 0.001);
    }

}
