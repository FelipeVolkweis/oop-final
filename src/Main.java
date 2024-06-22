import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8080;

        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;

        try {
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            String message = "1 dado1.csv binario1.bin";
            byte[] messageBytes = message.getBytes();
            int messageLength = messageBytes.length;

            out.writeInt(messageLength);
            out.write(messageBytes);
            out.flush();
            System.out.println("Sent: " + message);

            int responseLength = in.readInt();
            byte[] responseBytes = new byte[responseLength];

            in.readFully(responseBytes);
            String response = new String(responseBytes);
            System.out.println("Received: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}