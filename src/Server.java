import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * UDP datagram server.
 *
 * Messages are received in the form "room<>message" where <> is the room/message delimeter.
 * They are routed to listeners for the given room.
 *
 * @author Aran Long.
 */
public class Server implements Runnable{
    private final int port;
    private DatagramSocket serverSocket;

    // map from ip address -> thread on server, allowing us to route inbound messages
    private Map<InetAddress, ServerThread> connectedClients;

    // list of all functions listening for 'connection' events, this should be refactored
    // to accept more general events i.e. 'disconnection', 'reconnection' etc.
    private List<Consumer<ServerThread>> connectionCallbacks;

    private boolean running;

    public Server(int port) {
        this.port = port;
        this.connectedClients = new HashMap<>();
        this.connectionCallbacks = new ArrayList<>();
        this.running = true;

        new Thread(this).start();
    }

    public void run() {
        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("Failed to create server socket on port " + port);
            System.exit(1);
        }

        while(running) {
            byte[] receiveBuffer = new byte[1024];

            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                // a new packet has been received
                serverSocket.receive(packet);
            } catch (IOException e) {
                System.err.println("Failed to receive DatagramPacket");
                continue;
            }

            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();
            String packetData = new String(packet.getData());

            if(!connectedClients.containsKey(clientAddress)) {
                // this is the first message received from the client, set up a new thread on
                // the server for them
                ServerThread client = new ServerThread(clientAddress, clientPort, serverSocket);
                connectedClients.put(clientAddress, client);

                // let all 'connection' callbacks know about the new client
                connectionCallbacks.forEach(c -> c.accept(client));
            }

            // push the message data to the appropriate threads on the server
            connectedClients.get(clientAddress).push(packetData);
        }
    }

    /**
     * Subscribe to a meta-event on the server. e.g. 'connection'
     *
     * @param event
     * @param callback
     */
    public void on(String event, Consumer<ServerThread> callback) {
        connectionCallbacks.add(callback);
    }

    /**
     * Broadcast a message to all connected clients, messages must be in
     * the form: "room<>message"
     * @param room
     * @param message
     */
    public void broadcast(String room, String message) {
        connectedClients.values().forEach(c -> c.emit(room, message));
    }
}