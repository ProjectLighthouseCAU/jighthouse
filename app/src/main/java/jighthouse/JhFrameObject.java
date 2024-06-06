package jighthouse;

/**
 * Class for wrapping and enqueueing images encoded as 3D-Int-Arrays.
 * 
 */
public class JhFrameObject {
    // ID flag
    public int id;
    // The actual image
    // public int[][][] image;
    private byte[] buffer;

    /**
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as int[][][]
     */
    public JhFrameObject(int img_id, byte[][][] img_data) {
        this.id = img_id;
        
    }

    private void encodePixel(byte[] nbuf, int pos, int n) {
		int x = n%28;
		int y = n/28;
		buffer[pos + 3 * ((13-y)*28+x)] = (byte) 0xff;
	}

    public byte[] getImage() {
        return buffer;
    }

}
