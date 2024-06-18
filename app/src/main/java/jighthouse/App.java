/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jighthouse;

import java.util.concurrent.ThreadLocalRandom;

public class App {

    // TODO: REMOVE this class

    private byte[] noisyTestFrame(){
        byte[] frame = new byte[1176];
        ThreadLocalRandom.current().nextBytes(frame);
        frame[1] = (byte) 127;
        return frame;
    }

    private void run() throws InterruptedException {
        // Setting name and token
        String username = "chris1234";
        String token    = "API-TOK_tj/m-45iF-sMqh-cbZh-ZCvX";
    
        // Instantiate and run the Jighthouse
        Jighthouse myJighthouse = new Jighthouse(username, token);
        myJighthouse.start();
        Thread.sleep(500);
    
        // Loop that sends frames
        for (int i = 0; i < 10; i++) {
            if (!myJighthouse.isRunning()) break;
            // Make a new frame
            byte[] frame = noisyTestFrame();
            // Use the JH to send generated frame to the lighthouse server
            myJighthouse.sendFrame(frame);
            // Wait
            Thread.sleep(2000);
        }
        myJighthouse.stop();
    }

    public static void main(String[] args) throws InterruptedException {
        App test = new App();
        test.run();
    }
}
