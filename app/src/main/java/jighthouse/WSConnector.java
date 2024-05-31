package jighthouse;

import java.io.IOException;
import java.nio.ByteBuffer;
//import org.java_websocket.client.*;
//import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;
import java.util.HashMap;
import java.util.Queue; 
//import java.util.concurrent.ConcurrentLinkedQueue; 
//import org.msgpack.core.*;
import java.util.concurrent.BlockingQueue;

public class WSConnector extends Thread {
    // Attributes
    private String username;
    private String token;
    private String address;
    private JhWebsockClient ws;
    private boolean isRunning;
    private boolean isConnected;
    private BlockingQueue<JhFrameObject> reqQueue;
    // TODO: Does JH need the response or do we just print it here if ERR?
    //private Queue<JhFrameObject> respQueue; 

    /**
     * Create a new websocket client.
     * @param url
     * @param listener
     */
    public WSConnector(String username, String token, String address, 
        BlockingQueue<JhFrameObject> reqQueue) {
        this.username   = username;
        this.token      = token;
        this.address    = address;
        this.reqQueue   = reqQueue;
    }

    /**
     * Connect to Lighthouse server.
     * @return true on success and false if it fails.
     */
    private boolean connect() {
        try {
            URI uri = new URI(address);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            headers.put("User", username);
            this.ws = new JhWebsockClient(uri, headers);
            return true;
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Send a payload to websocket server
     * @param data the payload
     * @return true on success and false if it fails.
     */
    private boolean sendPAYL(Object data) {
        try {
            JhRequest msg = new JhRequest(0, username, token, null);
            byte[] packagedData = msg.toByteArray();
            if (packagedData != null) {
                ws.send(ByteBuffer.wrap(packagedData));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

    @Override
    public void run() {
        // Try to connect to server
        this.isRunning = true;
        this.isConnected = connect();

        int [][][] image;
        while (this.isRunning && this.isConnected) {
            // 1. Parse/extract img from obj on queue
            try {
                JhFrameObject frame = reqQueue.take();
                image = frame.getImage();

                // TODO 2. Send image, set isConnected to return val of sendPAYL
    
                // TODO 3. Check for response


                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.isRunning = false;
                }
    
            // TODO 4. Sleep depending on framerate, with small negative offset
            
        }

        // Disconnect from server
        if (this.isConnected) {
            disconnect();
        }
        this.isRunning = false;
    }

    /**
     * Stops the thread before next iteration.
     */
    public synchronized void stopThread() {
        this.isRunning = false;
    }

}
