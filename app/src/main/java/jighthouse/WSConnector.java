package jighthouse;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.java_websocket.client.*;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
//import org.msgpack.core.*;

public class WSConnector {
    // Attributes
    private String username;
    private String token;
    private String address;
    private LhWebsockClient ws;

    /**
     * Create a new websocket client.
     * @param url
     * @param listener
     */
    public WSConnector(String username, String token, String address) {
        this.username = username;
        this.token = token;
        this.address = address;
    }

    /**
     * Connect to websocket server
     */
    public void connect() {
        try {
            URI uri = new URI(address);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            headers.put("User", username);
            this.ws = new LhWebsockClient(uri, headers);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send a payload to websocket server
     * @param data
     */
    public void send(Object data) {
        try {
            LhPayload msg = new LhPayload(0, username, token, null);
            byte[] packagedData = msg.toByteArray();
            if (packagedData != null) {
                ws.send(ByteBuffer.wrap(packagedData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
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

}
