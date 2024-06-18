package jighthouse;

/**
 * Class for wrapping and enqueueing images encoded as 3D-Int-Arrays.
 * 
 */
class JhFrameObject {
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
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as int[][][]
     */
    public JhFrameObject(int img_id, int[][][] img_data) {
        this.id = img_id;
        this.buffer = from3DIntArray(img_data);
    }

    /**
     * Converts 3D to 1D Byte Array
     * @param byteArray3D the 3D byte array to be converted
     * @return the resulting 1D byte array
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

    /**
     * Converts 3D int array to 1D byte array, clipping values to the range 0-255
     * @param intArray3D the 3D int array to be converted
     * @return the resulting 1D byte array
     */
    private byte[] from3DIntArray(int[][][] intArray3D) {

        int yLength = intArray3D.length;
        int xLength = intArray3D[0].length;
        int colorDepth = intArray3D[0][0].length;

        // resulting byte[] array has length (y * x * color)
        byte[] byteArray = new byte[yLength * xLength * colorDepth];
        int index = 0;

        // Iterate through given 3D array
        for (int y = 0; y < yLength; y++) {
            for (int x = 0; x < xLength; x++) {
                for (int c = 0; c < colorDepth; c++) {
                    // Clip the value to be within 0 .. 255
                    int value = intArray3D[y][x][c];
                    if (value < 0) {
                        value = 0;
                    } else if (value > 255) {
                        value = 255;
                    }
                    // Write value into array
                    byteArray[index++] = (byte) value;
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

    public boolean isTerminationFrame() {
        return id == -1;
    }

    public static JhFrameObject getTerminationFrame() {
        byte[] arr = new byte[1176];
        JhFrameObject frame = new JhFrameObject(-1, arr);
        return frame;
    }

    public static JhFrameObject getEmptyFrame() {
        byte[] arr = new byte[1176];
        JhFrameObject frame = new JhFrameObject(0, arr);
        return frame;
    }

}
