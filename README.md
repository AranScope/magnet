# magnet
A Java UDP sockets library with an attractive name.

## Basics
UDP is a protocol allowing us to transmit data without the overhead of handshakes, confirmation or error checking. We're only using it for non 
critical data such as that used for a game server.

## Usage
### Server
Let's create a simple UDP echo server.
```Java
// create a server on port 8888
Server server = new Server(8888);

// when a client connects
server.on("connection", socket -> {
    
   // when a message is received on the echo room 
   socket.on("echo", message -> {
        
        // print the message
        System.out.println("message received: " + message);
        
        // echo the message
        socket.emit("echo! -> " + message);
   });
});
```
This shows the basics of magnet. We can test this using the tool 'netcat'
```Bash
nc -u 127.0.0.1 8888
Hello World
>> echo! -> Hello World!
```
