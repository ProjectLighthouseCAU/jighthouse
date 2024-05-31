package jighthouse;

import java.util.Queue; 
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
    private Queue<JhFrameObject> frameQueue;

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
        this.framerate    = framerate > 180 ? 180 : framerate;
        this.framecounter = 0;
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
        this.frameQueue = new ConcurrentLinkedQueue<>();
        // TODO init and run thread
    }

    /**
     * Changes the framerate limit.
     * @param framerate new fps limit
     */
    public void setFpsLimit(int framerate) {
        this.framerate = framerate;
        // TODO: send this to the thread, else its useless
    }

    /**
     * Sends a new frame to the Lighthouse Server.
     * @param image encoded as X*Y*color
     */
    public void sendFrame(int [][][] image) {
        // Create and enqueue frame
        JhFrameObject frame = new JhFrameObject(framecounter, image);
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
        // TODO: implement
    }
}
