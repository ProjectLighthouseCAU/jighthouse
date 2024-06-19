# Jighthouse
**A Java Client SDK for sending images to the CAU's Lighthouse Server.**


## Installation

### Visual Studio Code

In VSCode, you can simply copy the downloaded .jar file (jighthouse-X.Y.Z.jar) into the 'lib'-Folder located inside your Java project folder. When you installed the automatically recommended Java Extension pack, it should be found automatically afterwards.

Now simply add the following line at the beginning of your class file:
```java
import org.jighthouse.Jighthouse;
```

### Eclipse

*To be added...*

## How to use it

### Initializing the Jighthouse
First, make sure you have
1. Created an account on the Jighhouse Webpage.
2. Your **username** from said Webpage.
3. A **recent** access **token** acquired from the Jighthouse webpage. The access token has a limited lifetime, so you have to generate a new one after 3 days. Generating a new one also invalidates your old token.  

The Jighthouse expects both of these as a **String**.
```java
String username = "myUserName";
String token    = "API-TOK_XXXX-XXXX-XXXX-XXXX-XXXX";
```
  
Now you can instantiate the Jighthouse inside your project:
```java
Jighthouse myJighthouse = new Jighthouse(username, token);
```

### Running the Jighthouse
Before you can send Images to the Lighthouse server, the Jighthouse must be started:
```java
myJighthouse.start();
```
This will tell your Jighthouse to connect to the Lighthouse servers. 

### Frame/Image format
There are three data formats which you can use for sending frames to the Lighthouse: 


#### 1. int[c][x][y]: A 3D array using Integers
The indices of the array are assinged as follows:
- First index (c) contains the color: 0=Red, 1=Green, 2=Blue
- Second index (x) contains the window column starting from the left: From 0=Leftmost to 13=Rightmost window
- Third index (y) contains the window row starting from the top: From 0=14th Floor to 13=1st Floor.

A frame for a 4x2 Lighthouse that shows a red horizontal stripe over a blue horizontal stripe would look like this:
```java
int[][][] exampleFrame = {
    {{255,0,0},{255,0,0},{255,0,0},{255,0,0}},
    {{0,0,255},{0,0,255},{0,0,255},{0,0,255}}
}
```

The frame you send to the Lighthouse have a size of 3\*28\*14.

#### 2. byte[c][x][y]: A 3D array using Bytes 
The java data type **byte** contains numbers from -128 to 127, unlike the lighthouse, which will interpret it as an unsigned number, where 0 will be mapped to 0, 127 to 127 and -128 will be interpreted as 255.  
To get the appropiate byte from an integer ranging from 0 to 255, you can simply use a cast `(byte) someIntValue`. But keep in mind that 256 would be interpreted as 0 again.  

The indices of the array are assinged as follow:
- First index (c) contains the color: 0=Red, 1=Green, 2=Blue
- Second index (x) contains the window column starting from the left: From 0=Leftmost to 13=Rightmost window
- Third index (y) contains the window row starting from the top: From 0=14th Floor to 13=1st Floor.

A frame for a 4x2 Lighthouse that shows a red horizontal stripe over a blue horizontal stripe would look like this:
```java
byte[][][] exampleFrame = {
    {{255,0,0},{255,0,0},{255,0,0},{255,0,0}},
    {{0,0,255},{0,0,255},{0,0,255},{0,0,255}}
}
```

The frame you send to the Lighthouse have a size of 3\*28\*14.

#### 3. byte[]: A simple pre-encoded byte array  
This might be the hardest format to implement, but it is also the most efficient one since it can be directly sent to the server without re-encoding.

Three of the indices are grouped as the color of a single pixel. Then the pixels are grouped a line of windows from left to right, then those are grouped into levels from top to bottom.

Assuming that $i$ is your array index, $i\mod 3$ would be your color, $\frac{i}{3}\mod 14$ would be number of the window on the current floor and $\frac{i}{3\cdot 14}\mod 28$ would be the current floor.

A frame for a 4x2 Lighthouse that shows a red horizontal stripe over a blue horizontal stripe would look like this:
```java
byte[] exampleFrame = {
    255,0,0,  255,0,0,  255,0,0,  255,0,0,  
    0,0,255,  0,0,255,  0,0,255,  0,0,255
}
```

The byte array sent to the actual lighthouse must have a length of $3\cdot 14\cdot 28 = 1176$.

### Sending frames to the Lighthouse server
For sending the frame in one of the specified formats, you can use the following method:
```java
myJighthouse.sendFrame(exampleFrame);
```
For this to work your Jighthouse *must* be running.

### Stopping the Jighthouse
When your program is done sending stuff to the Lighthouse, you should terminate the Jighthouse.
```java
myJighthouse.stop();
```
### Example Code Snippet
The following example will show a series of randomly colored Pixels on the Lighthouse for 5 seconds:

```java
    private byte[] randomColors(){
        // Initialize byte array
        byte[] frame = new byte[1176];
        // Fill array with random bytes
        ThreadLocalRandom.current().nextBytes(frame);
        return frame;
    }

    private void run() throws InterruptedException {
        // Setting name and token
        String username = "myUsername";
        String token    = "API-TOK_XXXX-XXXX-XXXX-XXXX-XXXX";
    
        // Instantiate and run the Jighthouse
        Jighthouse myJighthouse = new Jighthouse(username, token);
        myJighthouse.start();
    
        // Loop that sends frames
        for (int i = 0; i < 10; i++) {
            // Make a new frame
            byte[] frame = randomColors();
            // Use the JH to send generated frame to the lighthouse server
            myJighthouse.sendFrame(frame);
            // Wait
            Thread.sleep(500);
        }
        myJighthouse.stop();
    }
```

### Q&A

*Q: How often can I send a frame to the Lighthouse?*

A: The standard framerate limit is set to **60 FPS**. This means that you can invoke the **sendFrame()** method every ~17 milliseconds.  
  If you invoke it more often than that, nothing bad will happen, but the additional frames will be skipped.

*Q: But my animation would look really great at higher framerates. Is there a way to raise the framerate?*

A: If needed, the framerate limit can be changed to values **between 1 and 180**, using the method *setFpsLimit()*. Ideally this should be done right after instantiation, before calling *start()* on the Jighthouse, else it will re-initialize the connection.

```java
myJighthouse.setFpsLimit(120); // Example for setting a framerate limit of 120 fps
```