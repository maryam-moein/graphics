package nodebox.graphics;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ImageTest {

    @Test
    public void testBlank() {
        Image img = Image.BLANK;
        assertEquals(1.0, img.getWidth());
        assertEquals(1.0, img.getHeight());
    }

    @Test
    public void testGetBounds() {
        Image img = Image.fromStream(getClass().getResourceAsStream("/red-rect.png"));
        assertEquals(32.0, img.getWidth());
        assertEquals(16.0, img.getHeight());
    }

}
