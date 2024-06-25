package org.jighthouse;

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
     * @param img_data The actual image as byte[]
     */
    public JhFrameObject(int img_id, byte[] img_data) {
        this.id = img_id;
        this.buffer = img_data;

    }

    /**
     * Constructor for a frame object.
     * @param img_id ID of frame
     * @param img_data The actual image as int[]
     */
    public JhFrameObject(int img_id, int[] img_data) {
        this.id = img_id;
        this.buffer = fromIntArray(img_data);

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

    private byte[] fromIntArray(int[] intArray) {
        int arrSize = intArray.length;
        byte[] byteArray = new byte[arrSize];
        for (int i = 0; i < arrSize; i++) {
            byteArray[i] = intToByte(intArray[i]);
        }
        return byteArray;
    }

    /**
     * Converts 3D to 1D Byte Array
     * @param byteArray3D the 3D byte array to be converted
     * @return the resulting 1D byte array
     */
    private byte[] from3DByteArray(byte[][][] byteArray3D) {

        int depth   = byteArray3D[0][0].length;
        int xLength = byteArray3D[0].length;
        int yLength = byteArray3D.length;
        int arrSize = yLength * xLength * depth;

        byte[] byteArray = new byte[arrSize];

        for (int i = 0; i < arrSize; i++) {
            int c =  i % depth;
            int x = (i / depth) % xLength;
            int y = (i / (depth * xLength)) % yLength;
            byteArray[i] = byteArray3D[y][x][c];
        }
        
        return byteArray;
    }

    /**
     * Converts 3D int array to 1D byte array, clipping values to the range 0-255
     * @param intArray3D the 3D int array to be converted
     * @return the resulting 1D byte array
     */
    private byte[] from3DIntArray(int[][][] intArray3D) {

        int depth   = intArray3D[0][0].length;
        int xLength = intArray3D[0].length;
        int yLength = intArray3D.length;
        int arrSize = yLength * xLength * depth;

        byte[] byteArray = new byte[arrSize];

        for (int i = 0; i < arrSize; i++) {
            int c =  i % depth;
            int x = (i / depth) % xLength;
            int y = (i / (depth * xLength)) % yLength;
            byteArray[i] = intToByte(intArray3D[y][x][c]);
        }
        
        return byteArray;
    }

    /**
     * Converts integer to byte, clipping the value between 0..250
     */
    private byte intToByte(int n) {
        if (n > 255) {
            return (byte) 255;
        } else if (n < 0) {
            return (byte) 0;
        } else {
            return (byte) n;
        }
    }

    /**
     * Returns image as byte[]
     */
    public byte[] getImage() {
        return buffer;
    }

    /**
     * Returns frame ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns termination flag
     */
    public boolean isTerminationFrame() {
        return id == -1;
    }

    /**
     * Special constructor for frame with termination flag
     */
    public static JhFrameObject getTerminationFrame() {
        byte[] arr = new byte[1176];
        JhFrameObject frame = new JhFrameObject(-1, arr);
        return frame;
    }

    /**
     * Special contructor for empty frame
     */
    public static JhFrameObject getEmptyFrame() {
        byte[] arr = new byte[1176];
        JhFrameObject frame = new JhFrameObject(0, arr);
        return frame;
    }

}
