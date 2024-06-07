import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(4221);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
         InputStream inputStream = clientSocket.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         String firstLine = reader.readLine();
         String line;
         String userAgent = null;
         while((line = reader.readLine())!=null){
             if(line.startsWith("User-Agent:")){
                 String[] userAgentTokens = line.split(":");
                 userAgent = userAgentTokens[1];
                 System.out.println(userAgent);
                 break;
             }
         }

         if(firstLine==null || firstLine.isEmpty()){
             clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
             return;
         }
         String[] tokens = firstLine.split(" ");
         String endpoint = tokens[1];
         System.out.println(endpoint);
         if(endpoint.equalsIgnoreCase("/user-agent")){
             if(userAgent!=null){
                 String output = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s",userAgent.trim().length(),userAgent.trim());
                 clientSocket.getOutputStream().write(output.getBytes());
                 return;
             }
         }
         if(endpoint.equalsIgnoreCase("/")){
             clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
             return;
         }
         String[] endpointTokens = endpoint.split("/");
         if(endpointTokens[1].equalsIgnoreCase("echo")){
             System.out.println("Here");
             String output1 = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s",endpointTokens[2].length(),endpointTokens[2]);
             System.out.println(output1);
             clientSocket.getOutputStream().write(output1.getBytes());
         }else{
             clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
         }

       System.out.println("accepted new connection");
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
