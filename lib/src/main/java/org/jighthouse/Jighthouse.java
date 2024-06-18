package jighthouse;

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
    private int       framecounter;
    private WSCStatus threadState = WSCStatus.READY;

    // Queues and thread
    private ConcurrentLinkedQueue<JhFrameObject> frameQueue;
    private ConcurrentLinkedQueue<WSCStatus> statusQueue;
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
        this.statusQueue = new ConcurrentLinkedQueue<WSCStatus>();
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
        this.statusQueue = new ConcurrentLinkedQueue<WSCStatus>();
    }

    /**
     * Get current status of the queue.
     */
    private void refreshStatus() {
        while (!statusQueue.isEmpty()) {
            this.threadState = statusQueue.poll();
        }
    }

    /**
     * Restarts the Jighthouse if it is running already.
     */
    private void restartIfRunning() {
        if (this.threadState == WSCStatus.RUNNING) {
            this.stop();
            this.start();
        }
    }

    /**
     * Sets a different framerate limit.
     * @param fpsLimit
     */
    public void setFramerateLimit(int fpsLimit) {
        this.framerate = fpsLimit;
        restartIfRunning();
    }

    /**
     * Initializes the Jighthouse.
     */
    public void start() {
        refreshStatus();
        checkParamValidity();

        if (this.threadState == WSCStatus.READY) {
            this.threadState = WSCStatus.RUNNING;
            // Create Queue and Thread
            this.frameQueue = new ConcurrentLinkedQueue<JhFrameObject>();
            this.statusQueue = new ConcurrentLinkedQueue<WSCStatus>();
            this.wsThread = new WSConnector(username, token, address, frameQueue, statusQueue, framerate);
            // Start thread
            this.wsThread.start();
        } else if (this.threadState == WSCStatus.RUNNING) {
            System.err.println("ERROR: Jighthouse is already running!");
        } else {
            System.err.println("ERROR: Jighthouse is not ready!");
        }
    }

    /**
     * Sends a new frame to the Lighthouse Server.
     * @param image as Y*X*color in a 3D array
     */
    public void sendFrame(byte[][][] image) {
        refreshStatus();
        if (this.threadState != WSCStatus.RUNNING) {
            throw new IllegalStateException("ERROR: Cannot send frame when JH is not connected!");
        }
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
        refreshStatus();
        if (this.threadState != WSCStatus.RUNNING) {
            throw new IllegalStateException("ERROR: Cannot send frame when JH is not connected!");
        }
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
        refreshStatus();
        if (this.threadState != WSCStatus.RUNNING) {
            throw new IllegalStateException("ERROR: Cannot send frame when JH is not connected!");
        }
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
     * @return running true if Jighthouse is running
     */
    public boolean isRunning() {
        refreshStatus();
        return this.threadState == WSCStatus.RUNNING;
    }

    /**
     * Tells the Jighthouse to disconnect from the server.
     */
    public void stop() {
        refreshStatus();
        if (this.threadState == WSCStatus.RUNNING) {
            this.frameQueue.add(JhFrameObject.getTerminationFrame());
        }
        
        // Wait for termination, max. 1000 ms
        for (int i = 0; i < 10; i+=1) {
            refreshStatus();
            if (!(this.threadState == WSCStatus.RUNNING)){ break;}
            System.out.println("Waiting for WS Thread to terminate...");
            sleep(500);
        }

        // Set thread to ready
        this.threadState = WSCStatus.READY;
    }

    /**
     * Sleep surrounded by try-catch
     * @param millis Duration to sleep for in ms
     */
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    /**
     * Checks if given parameters are valid and throws an error if something is invalid.
     */
    private void checkParamValidity() {
        if (this.username == null) {
            throw new IllegalArgumentException("ERROR: Username must not be null!");
        }
        if (this.token == null) {
            throw new IllegalArgumentException("ERROR: Access token must not be null!");
        }
        if (this.address == null) {
            throw new IllegalArgumentException("ERROR: Lighthouse Server URL must not be null!");
        }
        if (this.framerate < 1 || this.framerate > 180) {
            throw new IllegalArgumentException("ERROR: Framerate must be between 1 and 180!");
        }
    }
}
