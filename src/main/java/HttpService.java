import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class HttpService implements Runnable{
    private Socket clientSocket;
    private String basePath;

    public HttpService(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public HttpService(Socket clientSocket, String basePath) {
        this.clientSocket = clientSocket;
        this.basePath = basePath;
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
            System.out.println(String.join(",",HttpRequest));
            String headerLine;
            StringBuffer headers = new StringBuffer();
            while((headerLine= reader.readLine())!=null && !headerLine.isEmpty()){
                headers.append(headerLine);
            }
            String requestBody = reader.readLine();
            System.out.println("body:: "+requestBody);


            String[] str = HttpRequest[1].split("/");
            // System.out.println(HttpRequest[1]);
            if (HttpRequest[1].equals("/")) {
                System.out.println("Here1:::::");

                System.out.println("version");
                String response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: 0\r\n\r\n";
                output.write(response.getBytes());
            } else if (str[1].equals("user-agent")) {
                System.out.println("Here1:::::");

                reader.readLine();
                String useragent = reader.readLine().split("\\s+")[1];
                String reply = String.format(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s\r\n",
                        useragent.length(), useragent);
                output.write(reply.getBytes());
            } else if ((str.length > 2 && str[1].equals("echo"))) {
                System.out.println("Here1:::::");

                String responsebody = str[2];
                String finalstr = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: " + responsebody.length() +
                        "\r\n\r\n" + responsebody;
                output.write(finalstr.getBytes());
            } else if (HttpRequest[0].equalsIgnoreCase("GET") && (str.length > 2 && str[1].equals("files"))) {
                System.out.println("Here1:::::");

                String filePath = basePath+str[2];
                File file = new File(filePath);
                if(file.exists()){
                    String body = Files.readString(file.toPath());

                    System.out.println("body:: "+body);
                    String finalstr = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: application/octet-stream\r\n"
                            + "Content-Length: " + body.length() +
                            "\r\n\r\n" + body;
                    output.write(finalstr.getBytes());
                }else{
                    System.out.println("Here1:::::");
                    output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }


            }else if(HttpRequest[0].startsWith("POST") && (str.length > 2 && str[1].equals("files"))){
                System.out.println("Here:::::");
                String fileName = str[2];
                try {
                    String fullFilePath = basePath + fileName + ".txt";
                    System.out.println("Filename " + fullFilePath);
                    File outputFile = new File(basePath,fileName);
                    if (outputFile.exists()) {
                        String finalstr = "HTTP/1.1 201 Created\r\n" + "Content-Type: application/octet-stream\r\n"
                                + "Content-Length: " + requestBody.length() +
                                "\r\n\r\n" + requestBody;
                        System.out.println("finalstring:: " + finalstr);
                        output.write(finalstr.getBytes());
                        System.out.println("output sent");
                    }



                }catch (Exception ex){
                    System.out.println("Exception occurred:: "+ex.getMessage());
                }
            }else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            output.flush();
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
