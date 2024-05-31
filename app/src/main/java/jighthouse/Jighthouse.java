package jighthouse;

import java.util.Queue; 
import java.util.concurrent.ConcurrentLinkedQueue; 

/**
 * 'Main' class to be used by external applications.
 */
public class Jighthouse {

    private String username;
    private String token;
    private String address;
    private double framerate;
    private boolean isRunning;
    private Queue<JhFrameObject> frameQueue;

    /**
     * Constructor for Jighthouse class.
     * @param username Name of the lighthouse user
     * @param token Access token of the current user
     * @param address Websocket address of lighthouse server
     * @param framerate Framerate limit (0 for no limit)
     */
    public Jighthouse(String username, String token, String address, double framerate) {
        this.username = username;
        this.token = token;
        this.address = address;
        this.framerate = framerate > 180 ? 180 : framerate;
    }

    public Jighthouse(String username, String token) {
        this.username = username;
        this.token = token;
        this.address = "wss://lighthouse.uni-kiel.de/websocket";
        this.framerate = 60;
    }

    public void start() {
        this.frameQueue = new ConcurrentLinkedQueue<>();
        // TODO init and run thread
    }

    public void setFpsLimit(int framerate) {
        this.framerate = framerate;
        // TODO send this to the thread
    }

    public void sendFrame() {
        // TODO implement
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() {
        // TODO
    }
}
