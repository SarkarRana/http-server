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
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible
        // when running tests.
        System.out.println("Logs from your program will appear here!");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        // Uncomment this block to pass the first stage
        try{
            ServerSocket serverSocket = null;
            Socket clientSocket = null;
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            while(true){
                clientSocket = serverSocket.accept(); // Wait for connection
                executor.execute(new HttpService(clientSocket));
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

