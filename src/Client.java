import java.net.*;

public class Client {
    private DatagramSocket socket;

    public Client(String address, int port) {
        try {
            socket = new DatagramSocket(port, InetAddress.getByName(address));
        } catch (SocketException e) {
            System.err.println("Failed to open datagram socket on port " + port);
        } catch (UnknownHostException e) {
            System.err.println("Failed to locate host " + address);
        }

    }

    public void emit(String room, String data) {

    }
}