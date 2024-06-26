package org.jighthouse;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableStringValueImpl;

class JhWebsockClient extends WebSocketClient {

	private int httpCode = 0;
	private String lastResponse = "";
	long timeStamp = System.currentTimeMillis();

	public int getHttpCode() {
		return httpCode;
	}

	public int millisSinceResponse() {
		return (int) (System.currentTimeMillis() - timeStamp);
	}

	public String getLastResponse() {
		return lastResponse;
	}

	public JhWebsockClient(URI serverUri, Map<String, String> headers) {
		super(serverUri, new Draft_6455(), headers, 0);
	}

	public JhWebsockClient(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Websocket connection opened.");
		httpCode = handshakedata.getHttpStatus();
		lastResponse = handshakedata.getHttpStatusMessage();
		timeStamp = System.currentTimeMillis();
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("Websocket connection closed.");
	}

	@Override
	public void onMessage(String message) {
		System.out.println("WS Received message: " + message);
		timeStamp = System.currentTimeMillis();
	}

	@Override
    public void onMessage(ByteBuffer message) {
        byte[] buf = new byte[message.remaining()];
        message.get(buf);

        MessageUnpacker unp = MessagePack.newDefaultUnpacker(buf);
        try {
            Value v = unp.unpackValue();
            Map<Value, Value> vmap = v.asMapValue().map();

            int httpCode = vmap.get(new ImmutableStringValueImpl("RNUM")).asIntegerValue().toInt();
            Value responseValue = vmap.get(new ImmutableStringValueImpl("RESPONSE"));
			this.httpCode = httpCode;
			timeStamp = System.currentTimeMillis();
            String response = "";
            if (responseValue != null && responseValue.isStringValue()) {
                response = responseValue.asStringValue().asString();
				this.lastResponse = response;
            }

            //System.out.println("HTTP Code: " + httpCode);
            //System.out.println("Response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getLocalizedMessage());
        } catch (Exception ignored) { // Catching all other exceptions
            System.err.println("Error: Malformed message");
        }
    }

	@Override
	public void onError(Exception ex) {
		System.err.println("WS: an error occurred:" + ex);
	}

}
