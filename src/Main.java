public class Main {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8080;

        Client client = new Client();
        client.connect(host, port);
        client.waitForConnection();
        String message = "2 binario1.bin";
        String response = client.send(message);
        client.close();
    }
}