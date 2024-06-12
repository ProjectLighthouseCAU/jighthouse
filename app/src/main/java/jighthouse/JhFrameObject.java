package jighthouse;

/**
 * Class for wrapping and enqueueing images encoded as 3D-Int-Arrays.
 * 
 */
public class JhFrameObject {
    // ID flag
    private int id;
    // The actual image
    private byte[] buffer;

    /**
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as byte
     */
    public JhFrameObject(int img_id, byte[] img_data) {
        this.id = img_id;
        this.buffer = img_data;

    }
    /**
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as byte[][][]
     */
    public JhFrameObject(int img_id, byte[][][] img_data) {
        this.id = img_id;
        this.buffer = from3DByteArray(img_data);
    }

    /**
     * Converts 3D to 1D Byte Array
     * @param byteArray3D
     * @return
     */
    private byte[] from3DByteArray(byte[][][] byteArray3D) {

        int yLength = byteArray3D.length;
        int xLength = byteArray3D[0].length;
        int colorDepth = byteArray3D[0][0].length;

        // resulting byte[] array has length (y * x * color)
        byte[] byteArray = new byte[yLength * xLength * colorDepth];
        int index = 0;

        // Iterate through given 3D array
        for (int y = 0; y < yLength; y++) {
            for (int x = 0; x < xLength; x++) {
                for (int c = 0; c < 3; c++) {
                    // Write value into array
                    byteArray[index++] = byteArray3D[y][x][Math.min(colorDepth, c)];
                }
            }
        }
        return byteArray;
    }

    public byte[] getImage() {
        return buffer;
    }

    public int getId() {
        return id;
    }

}
