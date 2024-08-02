package osrs.dev.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageUtil
{
    /**
     * Reads an image resource from a given path relative to a given class.
     * This method is primarily shorthand for the synchronization and error handling required for
     * loading image resources from the classpath.
     *
     * @param clazz    The class to be referenced for the package path.
     * @param path The path, relative to the given class.
     * @return     A {@link BufferedImage} of the loaded image resource from the given path.
     */
    public static BufferedImage loadImageResource(final Class<?> clazz, final String path)
    {
        try (InputStream in = clazz.getResourceAsStream(path))
        {
            assert in != null;
            synchronized (ImageIO.class)
            {
                return ImageIO.read(in);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
