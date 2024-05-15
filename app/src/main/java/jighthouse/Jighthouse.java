package jighthouse;

/**
 * 'Main' class to be used by external applications.
 */
public class Jighthouse {

    String username;
    String token;
    String address;
    double framerate;

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

    public class JHMessageHandler {
        // TODO implement
    }

    public class JHThread extends Thread {

        public void run() {
            // TODO implement
        }

    }

}
