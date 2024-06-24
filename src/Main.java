import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8080;

        Client client = new Client();
        client.connect(host, port);
        client.waitForConnection();

        List<String> ls = new ArrayList<>();
        ls.add("1 dado1.csv binario1.bin");
        ls.add("5 binario1.bin indice.bin 1 1 id 212198");
        ls.add("6 binario1.bin indice.bin 1 212198 27 \"PELÉ MEIA BOCA\" NULO NULO");
        ls.add("2 binario1.bin");
        ls.add("5 binario1.bin indice.bin 1 1 id 212198");
        ls.add("6 binario1.bin indice.bin 1 212198 27 \"PELÉ MEIA BOCA\" NULO NULO");
        ls.add("2 binario1.bin");
        ls.add("5 binario1.bin indice.bin 1 1 id 212198");
        ls.add("2 binario1.bin");
        
        for (String s : ls) {
            String response = client.send(s);
            System.out.println(response);
        }

        client.close();
    }
}