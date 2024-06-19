package org.jighthouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

class WSConnector extends Thread {
    // Attributes
    private String username;
    private String token;
    private String address;
    private int waitPeriod;
    // Flags
    private boolean isRunning;
    private boolean isConnected;
    // Objects
    private Queue<JhFrameObject> frameQueue;
    private Queue<WSCStatus> statusQueue;
    private JhWebsockClient ws;
    // Stats
    private int framesReceived = 0;
    private int framesDisplayed = -1;

    /**
     * Create a new websocket client.
     * @param url
     * @param listener
     */
    public WSConnector(String username, String token, String address, 
        Queue<JhFrameObject> reqQueue, Queue<WSCStatus> statusQueue, double framerate) {
        this.username     = username;
        this.token        = token;
        this.address      = address;
        this.frameQueue   = reqQueue;
        this.statusQueue  = statusQueue;
        this.waitPeriod   = framerate > 0 ? ((int) (1000 / framerate)) : 1;
        this.isConnected  = false;
        this.isRunning    = false;
    }

    private boolean gotValidHttpCode() {
        int httpCode = ws.getHttpCode();
        return httpCode < 400;
    }

    /**
     * Method to test whether a connection was established.
     * @return true if connection successful
     * @throws InterruptedException
     */
    private boolean testConnection() throws InterruptedException {
        // Wait 1 second for initial handshake
        for (int i=0; i<20; i++) {
            sleep(50);
            // Check if HTTP Response is an error
            if (ws.getHttpCode() != 0) {
                break;
            } 
        }
        if (gotValidHttpCode()) {
            // Send test frame
            if (sendBlackFrame()) {
                // Wait for response
                for (int i=0; i<10; i++) {
                    sleep(50);
                    // Check if HTTP Response is an error
                    if (gotValidHttpCode()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Connect to Lighthouse server.
     * @return true on success and false if it fails.
     */
    private boolean connect() {
        System.out.println("Connecting to " + this.address + " ...");
        try {
            URI uri = new URI(address);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            headers.put("User", username);
            this.ws = new JhWebsockClient(uri, headers);
            try {
                ws.connect();
                if (testConnection()) {
                    System.out.println("Authentification successful!");
                    this.isConnected = true;
                    return true;
                } else {
                    int code = ws.getHttpCode();
                    System.err.println("Error while establishing connection! Code: " + code + ", Response: " + ws.getLastResponse());
                    if (code == 401) {
                        System.err.println("Please check if your username and API token are valid!");
                    }
                }
            } catch (WebsocketNotConnectedException | InterruptedException e) {
                System.err.println("Error: Could not connect to websocket!");
                //e.printStackTrace();
            }
        } catch (URISyntaxException ex) {
            System.err.println("Error: Invalid websocket URL given!");
            //ex.printStackTrace();
        }
        this.isConnected = false;
        return false;
    }

    /**
     * Sends an image encoded as byte[] as the payload.
     * @param image
     * @return
     */
    private boolean sendImage(byte[] image) {
        if (isConnected && isRunning) {
            try {
                // System.err.println("Making new request with tok "+token);
                // System.err.println("Bytearr " + image + " ; length " + image.length);
                JhRequest msg = new JhRequest(0, username, token, image);
                byte[] packagedData = msg.toByteArray();
                if (packagedData != null) {
                    ws.send(ByteBuffer.wrap(packagedData));
                }
                if (!gotValidHttpCode()) {
                    System.err.println("Error while sending frame! Code: " + ws.getHttpCode() + ", Response: " + ws.getLastResponse());
                    disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Sends a test image encoded as byte[] as the payload.
     * @param image
     * @return
     */
    private boolean sendBlackFrame() {
        byte[] image = new byte[1176];
        try {
            JhRequest msg = new JhRequest(0, username, token, image);
            byte[] packagedData = msg.toByteArray();
            if (packagedData != null) {
                ws.send(ByteBuffer.wrap(packagedData));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void printStats() {
        int framesSkipped = framesReceived - framesDisplayed;
        String stats = ("Frames received: " + framesReceived + " | Frames displayed: " + framesDisplayed
                        + " | Frames skipped: " + framesSkipped + " (" + 100*framesSkipped/framesReceived + " %)");
        System.out.println(stats);
    }

    /**
     * Disconnects from the Lighthouse server.
     */
    private void disconnect() {
        if (ws != null && ws.isOpen()) {
            sendBlackFrame();
            try {
                ws.close();
            } catch (WebsocketNotConnectedException ex) {
                System.err.println("Cannot disconnect: WebSocket is not connected!");
            }
        }
        this.isConnected = false;
    }

    /**
     * Method to make this thread sleep an amount of milliseconds.
     * The try-catch block is too cluttering to use "Thread.sleep" directly.
     * @param millis
     */
    private void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("WS Thread was interrupted!");
            Thread.currentThread().interrupt();
            this.isRunning = false;
        }
    }

    /**
     * Enqueues a status to let the Jighthouse know what happens.
     */
    private void setStatus(WSCStatus status) {
        this.statusQueue.add(status);
    }

    @Override
    public void run() {
        // Try to connect to server
        this.isRunning = true;
        connect();

        // Image variable
        byte[] image = null;

        // Timer variable
        int timeSinceReq = 0;

        JhFrameObject frame = JhFrameObject.getEmptyFrame();

        // Main loop
        while (this.isRunning && this.isConnected) {
            setStatus(WSCStatus.RUNNING);

            // 1. Try to get a frame from the queue
            while (frameQueue.isEmpty()) {
                waitMillis(1);
                timeSinceReq += 1;

                // 1b. Repeat last image after 1s to avoid timeout
                if (timeSinceReq > 1000 && image != null) {
                    sendImage(image);
                    timeSinceReq = 0;
                }
            }
            // Empty the whole queue, get newest frame
            while (!frameQueue.isEmpty()) {
                frame = frameQueue.poll();
                // Check for termination frame
                if (frame.isTerminationFrame()) {
                    this.isRunning = false;
                    break;
                } 
                framesReceived++;
            }
            
            // 2. Send image, set isConnected to return val of sendPAYL
            image = frame.getImage();
            sendImage(image);
            timeSinceReq = 0;
            framesDisplayed++;

            if (ws.millisSinceResponse() > (500 + 2 * waitPeriod)) {
                System.err.println("Error: Server not responding! Please check your network connection.");
                this.isRunning = false;
            }

            // 3. Sleep depending on framerate, with small negative offset.
            // The offset prevents stuttering.
            if (waitPeriod > 2) {
                waitMillis(waitPeriod - 2);
            }
        }


        if (framesDisplayed > 0) {
            printStats();
        }

        // 4. Disconnect from server
        disconnect();
        setStatus(WSCStatus.TERMINATED);
    }

}
