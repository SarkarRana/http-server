import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpService implements Runnable{
    private Socket clientSocket;

    public HttpService(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            OutputStream output = clientSocket.getOutputStream();

            if(line==null){
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                return;
            }
            String[] HttpRequest = line.split(" ");


            String[] str = HttpRequest[1].split("/");
            // System.out.println(HttpRequest[1]);
            if (HttpRequest[1].equals("/")) {
                System.out.println("version");
                String response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: 0\r\n\r\n";
                output.write(response.getBytes());
            } else if (str[1].equals("user-agent")) {
                reader.readLine();
                String useragent = reader.readLine().split("\\s+")[1];
                String reply = String.format(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s\r\n",
                        useragent.length(), useragent);
                output.write(reply.getBytes());
            } else if ((str.length > 2 && str[1].equals("echo"))) {
                String responsebody = str[2];
                String finalstr = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: " + responsebody.length() +
                        "\r\n\r\n" + responsebody;
                output.write(finalstr.getBytes());
            } else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            output.flush();
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
