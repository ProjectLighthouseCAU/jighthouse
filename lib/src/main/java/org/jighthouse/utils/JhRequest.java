package jighthouse;

import java.io.IOException;
import org.msgpack.core.*;

/**
 * Class that models an outgoing message to be sent to the lighthouse.
 */
class JhRequest {

    private int REID;
    private String USER;
    private String TOKEN;
    private String VERB;
    private String[] PATH;
    private byte[] PAYL;
    public final static int PAYL_SIZE = 1176;

    public JhRequest(int reid, String user, String token, byte[] payload) {
        this.REID = reid;
        this.USER = user;
        this.TOKEN = token;
        this.VERB = "PUT";
        this.PAYL = payload;
    }

    public byte[] toByteArray() throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packMapHeader(5);
        packer.packString("REID");
        packer.packInt(REID);
        packer.packString("AUTH");
        packer.packMapHeader(2);
        packer.packString("USER");
        packer.packString(USER);
        packer.packString("TOKEN");
        packer.packString(TOKEN);
        packer.packString("VERB");
        packer.packString("PUT");
        packer.packString("PATH");
        packer.packArrayHeader(3);
        packer.packString("user");
        packer.packString(USER);
        packer.packString("model");
        packer.packString("PAYL");
        packer.packBinaryHeader(PAYL_SIZE);
        packer.addPayload(PAYL, 0, PAYL_SIZE);
        packer.close();
        return packer.toByteArray();
    }
}
