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

}
