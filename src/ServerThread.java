import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * UDP datagram thread.
 *
 * Managed communication with a single connected client. Routing
 * messages to their appropriate callbacks and handline sending
 * new messages.
 *
 * @author Aran Long.
 */
public class ServerThread {

    final InetAddress address;
    final int port;
    private final DatagramSocket serverSocket;
    private Map<String, List<Consumer<String>>> callbacks;

    /**
     * Construct a new thread on the server.
     *
     * @param address The address of the client.
     * @param port The port of the client/server.
     * @param serverSocket The server socket (used to send messages).
     */
    public ServerThread(InetAddress address, int port, DatagramSocket serverSocket) {
        this.address = address;
        this.port = port;
        this.serverSocket = serverSocket;
        this.callbacks = new HashMap<>();
    }

    /**
     * Subscribe to any messages in the given room.
     *
     * @param room The room to listen to.
     * @param callback The consumer of messages on this room.
     */
    public void on(String room, Consumer<String> callback) {
        if(!callbacks.containsKey(room)) {
            callbacks.put(room, new ArrayList<>());
        }

        callbacks.get(room).add(callback);
    }

    /**
     * Send a message to the client.
     *
     * @param room The room to send the message in.
     * @param data The body of the message.
     */
    public void emit(String room, String data) {
        byte[] sendBuffer = (room + "<>" + data).getBytes();

        DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, address, port);

        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send packet to address " + address + " on port " + port);
        }
    }

    /**
     * Supply a message to be routed to the appropriate callbacks.
     * @param message The received to route.
     */
    void push(String message) {
        new Thread(() -> {
            routeMessage(message);
        }).start();
    }

    /**
     * Route a message to the appropriate callbacks, after extracting
     * the room and body of the message.
     *
     * @param message The message to route.
     */
    private void routeMessage(String message) {
        try {
            String delimiter = "<>";

            int i = message.indexOf(delimiter);
            String room = message.substring(0, i);
            String data = message.substring(i + 2, message.length() - 1);

            if (callbacks.containsKey(room)) {
                callbacks.get(room).forEach(c -> c.accept(data));
            } else {
                System.err.println("No such callback for room: " + room);
            }
        } catch(StringIndexOutOfBoundsException ex) {
            System.err.println("Message supplied in incorrect format: " + message);
        }
    }
}