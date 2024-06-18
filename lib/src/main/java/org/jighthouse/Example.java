package jighthouse;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Example {

    private byte[] randomColors() {
        byte[] frame = new byte[1176];
        ThreadLocalRandom.current().nextBytes(frame);
        frame[1] = (byte) 127;
        return frame;
    }

    private void run(String username, String token, int frameCount) throws InterruptedException {
        // Instantiate and run the Jighthouse
        Jighthouse myJighthouse = new Jighthouse(username, token);
        myJighthouse.start();
        Thread.sleep(500);

        // Loop that sends frames
        for (int i = 0; i < frameCount; i++) {
            // Make a new frame
            byte[] frame = randomColors();
            // Use the JH to send generated frame to the lighthouse server
            myJighthouse.sendFrame(frame);
            // Wait
            Thread.sleep(500);
        }
        myJighthouse.stop();
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your API token: ");
        String token = scanner.nextLine();

        System.out.print("Enter the number of frames to send: ");
        int frameCount = scanner.nextInt();

        Example test = new Example();
        test.run(username, token, frameCount);
    }
}
