package jighthouse;

//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.core.*;
//import org.msgpack.value.*;

/**
 * Class that models an outgoing message to be sent to the lighthouse.
 */
public class JhPacker {

    int REID;
    String USER;
    String TOKEN;
    String VERB;
    String[] PATH;
    Map<String, Object> DATA;

    public JhPacker(int reid, String user, String token, HashMap<String, Object> data) {
        this.REID = reid;
        this.USER = user;
        this.TOKEN = token;
        this.VERB = "PUT";
        this.DATA = data;
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
        packer.packString(USER);
        packer.packString("VERB");
        packer.packString("PUT");
        packer.packString("PATH");
        packer.packArrayHeader(3);
        packer.packString("user");
        packer.packString(USER);
        packer.packString("model");
        packer.packString("META");
        packer.packMapHeader(0);
        packer.packString("PAYL");
        packer.packString(DATA.toString());
        return packer.toByteArray();
    }
}
