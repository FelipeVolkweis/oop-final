import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    private boolean isConnected = false;
    
    public Client() {}

    public synchronized void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                synchronized (this) {
                    isConnected = true;
                    this.notifyAll();
                }
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }).start();
    }

    public synchronized void waitForConnection() {
        while (!isConnected) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String send(String message) {
        try {
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

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return null;
    }

    public void close() {
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