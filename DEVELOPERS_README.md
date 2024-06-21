# Functional overview for the library 'jighthouse'

## Main classes

### 'Jighthouse' class
This class is the API which is directly accessible by the user. It is available under the path 'org.jighthouse.Jighthouse'.

The user can spawn an instance of Jighthouse, start() it and then send frames to it.

Upon being started, Jighthouse instantiates and runs the "WSConnector" class as a Thread.  
Communication between Jighthouse and WSConnector is done through two Queues, frameQueue and StatusQueue.
The jighthouse will then block until the state of WSConnector changes to either "RUNNING" (in case of success) or "TERMINATED" (in case of a failure).

Whenever Jighthouse is given a new Frame to Display, it will enqueue this frame to frameQueue.
It will also read the current state of the WSConnector thread from the StatusQueue. 
Sending a frame is not blocking so that it will not impact the performance of the program block generating the frames.

After stop() is being called, Jighthouse will enqueue an empty frame with an ID of -1. This ID of -1 is a termination flag which tells WSConnector to stop sending frames and quitting the connection.
Jighthouse will block until it receives a "TERMINATED" signal from the WSConnector.


### 'WSConnector'
This class handles the network communication between this client and the lighthouse servers.

Upon start, it will try to initiate a websocket communication to the lighthouse server. 
After the initial handshake it will check for a valid http response code. This first response code is usually 101 and does not become invalid if the user has given invalid authentification data.  

Therefore a second check will be performed by sending a black test image to the lighthouse.  
This will result in another response, which will either enqueue the status "RUNNING" to the status queue in case of a valid response code or, in case of an invalid code (4xx, 5xx), trigger termination of the thread and enqueing of the "TERMINATED" status.

After successful authentification, the WSConnector will go into a loop where it will dequeue the most recent frame from the frameQueue, while checking all enqueues Objects for a termination flag.  
The frame then gets send to the lighthouse, after which the thread will sleep for a period depending on the framerate limit which was set.  
If the WSConnector spends more than 1000 ms waiting for a frame, it will send to most recent frame again to prevent the server from disconnecting.

Upon failure or receiving a termination frame, the running-or connected-Flag will be set to false, which terminates the loop and causes the WSConnector to disconnect.


## Utility classes

### 'JhFrameObject'
Contains an ID (integer) and an image as a byte[] buffer.  
Has several functions to create a byte buffer from other formats, like a int[][][] or byte[][][].

### 'JhWebsockClient'
Extension of the imported Websock Client. Provides a method for decoding the received response and getters for polling the most recent response and http code.

### 'JhRequest'
Utility class for packing and encoding the authentification data and payload via messagepack.

### 'WSCStatus'
A simple enum used for thread communication. WSConnector enqueues its status as WSCStatus.

### 'Example' 
An example animation showing random pixels which can be imported and used by the user. Needs some documentation.