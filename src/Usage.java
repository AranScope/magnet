public class Usage {

    public Usage() {
        Server server = new Server(8888);
        System.out.println("Started server on 8888");

        server.on("connection", socket -> {
            System.out.println("new connection: " + socket.address);

            socket.on("message", System.out::println);

            socket.on("echo", m -> {
                System.out.println("message: " + m);
                socket.emit("echo", m);
            });
        });

    }

    public static void main(String[] args) {
        new Usage();
    }

}
