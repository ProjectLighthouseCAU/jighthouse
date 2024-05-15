package jighthouse;

import java.net.URI;
import java.util.Map;
//import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class JhWebsockClient extends WebSocketClient {

	public JhWebsockClient(URI serverUri, Map<String, String> headers) {
		super(serverUri, new Draft_6455(), headers, 0);
	}

	public JhWebsockClient(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO: send("...");
		System.out.println("WS connection to " + super.uri.toString() + " opened.");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("WS connection closed with exit code " + code + "\nAdditional info: " + reason);
	}

	@Override
	public void onMessage(String message) {
		System.out.println("WS Received message: " + message);
	}

	@Override
	public void onMessage(ByteBuffer message) {
		System.out.println("WS Received ByteBuffer");
	}

	@Override
	public void onError(Exception ex) {
		System.err.println("WS: an error occurred:" + ex);
	}

}