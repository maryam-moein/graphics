package nodebox.graphics;

import org.junit.Test;

import java.awt.*;

import static junit.framework.Assert.*;

public class SizeTest {

    @Test
    public void testEmpty() {
        Size sz = Size.EMPTY;
        assertEquals(0.0, sz.width);
        assertEquals(0.0, sz.height);
    }

    @Test
    public void testEquals() {
        Size sz1 = new Size(100, 200);
        Size sz2 = new Size(100, 200);
        assertTrue(sz1.equals(sz2));
        Size sz3 = new Size(100.1, 200);
        assertFalse(sz1.equals(sz3));
    }

    @Test
    public void testDimension2D() {
        Size sz = new Size(new Dimension(20, 30));
        assertEquals(20.0, sz.width);
        assertEquals(30.0, sz.height);
    }

}
