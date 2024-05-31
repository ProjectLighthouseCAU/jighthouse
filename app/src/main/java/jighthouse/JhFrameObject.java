package jighthouse;

/**
 * Class for wrapping and enqueueing images encoded as 4D-Int-Arrays.
 * 
 */
public class JhFrameObject {
    // ID flag
    public int id;
    // The actual image
    public int[][][] image;

    /**
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as int[][][]
     */
    public JhFrameObject(int img_id, int[][][] img_data) {
        this.id = img_id;
        this.image = img_data;
    }

    public int[][][] getImage() {
        return image;
    }
}
