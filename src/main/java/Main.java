import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        // You can use print statements as follows for debugging, they'll be visible
        // when running tests.
        System.out.println("Logs from your program will appear here!");
        String basePath = null;
        if(args.length > 1){
            basePath = args[1];
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        // Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try{

            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            System.out.println(serverSocket.getLocalPort());
            while(true){
                clientSocket = serverSocket.accept(); // Wait for connection
                HttpService service = new HttpService(clientSocket,basePath);
                executor.execute(service);
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }finally {
            assert serverSocket != null;
            serverSocket.close();
            assert clientSocket != null;
            clientSocket.close();
        }
    }
}

