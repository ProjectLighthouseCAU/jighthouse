package jighthouse;

//import java.util.Queue; 
//import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue; 

/**
 * 'Main' class to be used by external applications.
 */
public class Jighthouse {

    // Params for access to lighthouse
    private String  username;
    private String  token;
    private String  address;

    // Framerate limit
    private double  framerate;

    // Flags
    private int     framecounter;
    private boolean isRunning;

    // Queue
    private ConcurrentLinkedQueue<JhFrameObject> frameQueue;
    private WSConnector wsThread;

    /**
     * Constructor for Jighthouse class.
     * @param username Name of the lighthouse user
     * @param token Access token of the current user
     * @param address Websocket address of lighthouse server
     * @param framerate Framerate limit (0 for no limit)
     */
    public Jighthouse(String username, String token, String address, double framerate) {
        this.username     = username;
        this.token        = token;
        this.address      = address;
        this.framecounter = 0;
        if (framerate > 180) {
            this.framerate = 180;
        } else if (framerate < 1) {
            this.framerate = 1;
        } else {
            this.framerate = framerate;
        }
    }

    /**
     * Constructor for Jighthouse class.
     * Address and fps limit are set to standard values.
     * @param username Name of the lighthouse user
     * @param token Access token of the current user
     */
    public Jighthouse(String username, String token) {
        this.username     = username;
        this.token        = token;
        this.address      = "wss://lighthouse.uni-kiel.de/websocket";
        this.framerate    = 60;
        this.framecounter = 0;
    }

    /**
     * Initializes the Jighthouse.
     */
    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            // Create Queue and Thread
            this.frameQueue = new ConcurrentLinkedQueue<JhFrameObject>();
            this.wsThread = new WSConnector(username, token, address, frameQueue, framerate);
            // Start thread
            this.wsThread.start();
        }
    }

    /**
     * Sends a new frame to the Lighthouse Server.
     * @param image as Y*X*color in a 3D array
     */
    public void sendFrame(byte[][][] image) {
        if (image.length != 14) {
            throw new IllegalArgumentException("Invalid Y dimension in Y*X*Color frame: expected 14, but got " + image.length);
        }
        if (image[0].length != 28) {
            throw new IllegalArgumentException("Invalid X dimension in Y*X*Color frame: expected 28, but got " + image[0].length);
        }
        if (image[0][0].length != 3) {
            throw new IllegalArgumentException("Invalid color depth in Y*X*Color frame: expected 3, but got " + image[0][0].length);
        }
        // Create and enqueue frame
        JhFrameObject frame = new JhFrameObject(framecounter, image);
        this.frameQueue.add(frame);
        // Increase counter for ID
        this.framecounter += 1;
    }

    /**
     * Sends a new frame to the Lighthouse Server.
     * @param image as Y*X*color in a 3D array
     */
    public void sendFrame(int[][][] image) {
        if (image.length != 14) {
            throw new IllegalArgumentException("Invalid Y dimension in Y*X*Color frame: expected 14, but got " + image.length);
        }
        if (image[0].length != 28) {
            throw new IllegalArgumentException("Invalid X dimension in Y*X*Color frame: expected 28, but got " + image[0].length);
        }
        if (image[0][0].length != 3) {
            throw new IllegalArgumentException("Invalid color depth in Y*X*Color frame: expected 3, but got " + image[0][0].length);
        }
        // Create and enqueue frame
        JhFrameObject frame = new JhFrameObject(framecounter, image);
        this.frameQueue.add(frame);
        // Increase counter for ID
        this.framecounter += 1;
    }

    /**
     * Sends a new frame to the Lighthouse Server.
     * @param image encoded as Y*X*color
     */
    public void sendFrame(byte[] buffer) {
        if (buffer.length != 1176) {
            throw new IllegalArgumentException("Invalid image size: Expected 1176 bytes, but got "+ buffer.length);
        }
        // Create and enqueue frame
        JhFrameObject frame = new JhFrameObject(framecounter, buffer);
        this.frameQueue.add(frame);
        // Increase counter for ID
        this.framecounter += 1;
    }

    /**
     * Check if the Jighthouse is active and running.
     * @return running state
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Tells the Jighthouse to disconnect from the server.
     */
    public void stop() {
        if (this.isRunning) {
            this.wsThread.stopThread();
            this.isRunning = false;
        }
    }
}
