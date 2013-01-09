package nodebox.graphics;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CanvasTest {

    @Test
    public void testCloning() {
        Color backgroundColor = new Color(0.1, 0.2, 0.3);
        Canvas c = Canvas.EMPTY
                .size(200, 300)
                .background(backgroundColor);
        assertEquals(200.0, c.getWidth());
        assertEquals(300.0, c.getHeight());
        assertEquals(backgroundColor, c.getBackground());
    }

}
