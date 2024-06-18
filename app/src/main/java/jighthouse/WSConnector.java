package jighthouse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

//import org.msgpack.core.*;
//import java.utiHashl.concurrent.BlockingQueue;

//import org.java_websocket.client.*;
//import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
public class WSConnector extends Thread {
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

    /**
     * Connect to Lighthouse server.
     * @return true on success and false if it fails.
     */
    private boolean connect() {
        System.out.println("Connection to server...");
        try {
            URI uri = new URI(address);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            headers.put("User", username);
            this.ws = new JhWebsockClient(uri, headers);
            try {
                ws.connect();
                System.out.println("Websocket connected.");
                return true;
            } catch (WebsocketNotConnectedException e) {
                System.err.println("Error: Could not connect to websocket!");
                e.printStackTrace();
                return false;
            }
        } catch (URISyntaxException ex) {
            System.err.println("Error: Invalid websocket URL given!");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Send a payload to websocket server
     * @param payl the payload
     * @return true on success and false if it fails.
     */
    // private boolean sendPAYL(Object payl) {
    //     if (isConnected && isRunning) {
    //         try {
    //             HashMap<String, Object> data = new HashMap<String, Object>();
    //             data.put("PAYL", payl);
    //             System.err.println("Making new request with tok "+token);
    //             JhRequest msg = new JhRequest(0, username, token, data);
    //             byte[] packagedData = msg.toByteArray();
    //             if (packagedData != null) {
    //                 ws.send(ByteBuffer.wrap(packagedData));
    //             }
    //             System.out.println(decodeMessage(packagedData));
    //             return true;
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return false;
    // }

    /**
     * Method purely meant for debugging purposes.
     * 
     * @param packagedData
     * @return
     */
    public void printMessageFields(byte[] packedData) {
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new ByteArrayInputStream(packedData));

            if (unpacker.hasNext()) {
                Value fieldName = unpacker.unpackValue();

                System.out.println(fieldName.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an image encoded as byte[] as the payload.
     * @param image
     * @return
     */
    private boolean sendImage(byte[] image) {
        if (isConnected && isRunning) {
            try {
                System.err.println("Making new request with tok "+token);
                System.err.println("Bytearr " + image + " ; length " + image.length);
                JhRequest msg = new JhRequest(0, username, token, image);
                byte[] packagedData = msg.toByteArray();
                if (packagedData != null) {
                    ws.send(ByteBuffer.wrap(packagedData));
                }
                printMessageFields(packagedData);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Disconnects from the Lighthouse server.
     */
    private void disconnect() {
        if (ws != null && ws.isOpen()) {
            try {
                ws.close();
            } catch (WebsocketNotConnectedException ex) {
                System.err.println("WebSocket is not connected.");
            }
        } else {
            System.err.println("WebSocket is not open.");
        }
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
        this.isConnected = connect();

        // Image variable
        byte[] image = null;

        // Timer variable
        int timeSinceReq = 0;

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
            JhFrameObject frame = frameQueue.poll();
            // Empty the whole queue, get newest frame
            while (!frameQueue.isEmpty()) frame = frameQueue.poll();
            image = frame.getImage();
            
            // 2. Send image, set isConnected to return val of sendPAYL
            if (image != null) {
                sendImage(image);
                timeSinceReq = 0;
            }
    
            // 3. Sleep depending on framerate, with small negative offset.
            // The offset prevents stuttering.
            if (waitPeriod > 2) {
                waitMillis(waitPeriod - 2);
            }
        }

        // 4. Disconnect from server
        if (this.isConnected) {
            disconnect();
        }
        this.isRunning = false;
        setStatus(WSCStatus.TERMINATED);
    }

    /**
     * Stops the thread before next iteration.
     */
    public synchronized void stopThread() {
        this.isRunning = false;
    }

}
