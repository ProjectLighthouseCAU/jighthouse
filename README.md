# Jighthouse
**A Java Client SDK for sending images to the CAU's Lighthouse Server.**


## Installation
*TODO: Write this*

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
private myJighthouse = new Jighthouse(username, token);
```

### Running the Jighthouse
Before you can send Images to the Lighthouse server, the Jighthouse must be running.   
To run the JH, simply use  `myJighthouse.start();` **at the beginning** of your program.  
This will connect your Jighthouse to our Lighthouse servers. If something goes wrong, an error will be thrown.

### Frame/Image format
There are three data formats which you can use for sending frames to the Lighthouse: 

#### 1. byte[c][x][y]: A 3D array using Bytes 
The java data type **byte** contains numbers from 0 to 255. This is convenient because we use a color depth of 8 bit (1 byte) per channel.  
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

The frame you send to the Lighthouse *must* be exactly 3\*28\*14.

#### 2. int[c][x][y]: A 3D array using Integers (not recommended)
Instead if using a 3D array with bytes, you can also use the int data type. This is not recommended because internally it will converted into an byte array anyway. The conversion involves clipping to get rid of values outside the specified range and eats processing power.  
It is much more efficient if you configure your program using the *byte* data type for colors from the beginning.

A frame for a 4x2 Lighthouse that shows a red horizontal stripe over a blue horizontal stripe would look like this:
```java
int[][][] exampleFrame = {
    {{255,0,0},{255,0,0},{255,0,0},{255,0,0}},
    {{0,0,255},{0,0,255},{0,0,255},{0,0,255}}
}
```

The indices and expected frame size are the same as for the previous method.

#### 3. byte[]: A simple pre-encoded byte array  
Instead of using the other two methods, you can also encode the byte array yourself.
First we have a series of colors, then a series of window rows, then a series of window colums.

A frame for a 4x2 Lighthouse that shows a red horizontal stripe over a blue horizontal stripe would look like this:
```java
byte[] exampleFrame = {
    255,0,0,  0,0,255,  255,0,0,  0,0,255,
    255,0,0,  0,0,255,  255,0,0,  0,0,255
}
```

The byte array sent to the actual lighthouse must have a length of $3\cdot 14\cdot 28 = 1176$.

### Sending frames to the Lighthouse server
For sending the frame in one of the specified formats, you can use the following method:
```java
myJighthouse.sendFrame(exampleFrame);
```
For this to work your Jighthouse *must* be running.

#### Q&A

*Q: How often can I send a frame to the Lighthouse?*

A: The standard framerate limit is set to **60 FPS**. This means that you can invoke the **sendFrame()** method every ~17 milliseconds.  
  If you invoke it more often than that, in which case frames will be skipped.

*Q: But my animation would look really great at higher framerates. Is there a way to raise the framerate?*

A: If needed, the framerate limit can be changed to values **between 1 and 180**, using the method:
```java
myJighthouse.setFpsLimit(120); // Example for setting a framerate limit of 120 fps
```
If you want to set a custom fps limit, this should be done before starting/connection your JH, else this method will cause it to re-initialize the JH connection.

### Example Code Snippet

```java
private void run() {
    // Setting name and token
    username = "myUserName";
    token    = "API-TOK_XXXX-XXXX-XXXX-XXXX-XXXX";

    // Instantiate and run the Jighthouse
    private myJighthouse = new Jighthouse(username, token);
    myJighthouse.start();

    // Loop that sends frames
    while(myJighthouse.isRunning()) {
        // Make a new frame
        byte[][][] frame = yourMethodForGeneratingAFrame();
        // Use the JH to send generated frame to the lighthouse server
        myJighthouse.sendFrame(frame);
        // Wait for 17 ms
        thread.sleep(17);
    }
}
```