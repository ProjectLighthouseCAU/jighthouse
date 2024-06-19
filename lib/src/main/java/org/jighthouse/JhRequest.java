package org.jighthouse;

import java.io.IOException;
import org.msgpack.core.*;

/**
 * Class that models an outgoing message to be sent to the lighthouse.
 */
class JhRequest {

    private int REID;
    private String USER;
    private String TOKEN;
    //private String VERB;
    //private String[] PATH;
    private byte[] PAYL;
    public final static int PAYL_SIZE = 1176;

    public JhRequest(int reid, String user, String token, byte[] payload) {
        this.REID = reid;
        this.USER = user;
        this.TOKEN = token;
        //this.VERB = "PUT";
        this.PAYL = payload;
    }

    /**
     * Method that packs the request as a byte array using messagepack.
     * 
     * @return byte[] packed message
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packMapHeader(5)
                  .packString("REID").packInt(REID)
                  .packString("AUTH").packMapHeader(2)
                      .packString("USER").packString(USER)
                      .packString("TOKEN").packString(TOKEN)
                  .packString("VERB").packString("PUT")
                  .packString("PATH").packArrayHeader(3)
                      .packString("user").packString(USER)
                      .packString("model")
                  .packString("PAYL").packBinaryHeader(PAYL_SIZE)
                  .addPayload(PAYL, 0, PAYL_SIZE);
            return packer.toByteArray();
        }
    }    
}
