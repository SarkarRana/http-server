import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
        String line = null;

            line = reader.readLine();

        OutputStream output = clientSocket.getOutputStream();
            System.out.println(line);
            if(line==null){
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                return;
            }
            String[] HttpRequest = line.split(" ");
            System.out.println(String.join(",",HttpRequest));
            String headerLine=null;
            int contentLength = 0;
            String userAgent = "";
            String encoding = "";
            while((headerLine= reader.readLine())!=null && !headerLine.isEmpty()){
                System.out.println(headerLine);
                if (headerLine.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(headerLine.substring("Content-Length:".length()).trim());
                    System.out.println("contentlength:: "+contentLength);
                }else if(headerLine.startsWith("User-Agent:")){
                    System.out.println("Collected");
                    userAgent = headerLine.substring("User-Agent:".length()).trim();
                }else if(headerLine.startsWith("Accept-Encoding:")){
                    encoding = headerLine.substring("Accept-Encoding:".length()).trim();
                }
            }
            System.out.println("outside");



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
                System.out.println("user agent:::::"+userAgent);


                String reply = String.format(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s\r\n",
                        userAgent.length(), userAgent);
                output.write(reply.getBytes());
            } else if ((str.length > 2 && str[1].equals("echo"))) {
                System.out.println("Here1:::::");

                String responsebody = str[2];
                String finalstr = "";
                if(!encoding.equalsIgnoreCase("invalid-encoding")){
                    finalstr = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/plain\r\n"
                            + "Content-Length: " + responsebody.length()
                            + "\r\nContent-Encoding: gzip"+
                            "\r\n\r\n" + responsebody;
                }else{
                    finalstr = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/plain\r\n"
                            + "Content-Length: " + responsebody.length()
                            + "\r\n\r\n" + responsebody;

                }

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
                StringBuffer bodyBuffer = new StringBuffer();
                while (reader.ready()) {
                    bodyBuffer.append((char)reader.read());
                }
                String requestBody = bodyBuffer.toString();
//                char[] body = new char[contentLength];
//                reader.read(body, 0, contentLength);
//                String requestBody = new String(body);
                System.out.println("body:: "+requestBody);
                try {
                    String fullFilePath = basePath + fileName;
                    Path path = Paths.get(fullFilePath);
                    System.out.println("Filename " + fullFilePath);

                    try{

                        Files.createDirectories(path.getParent());
                        if (!Files.exists(path)) {
                            Files.createFile(path);
                        }
                        Files.write(path, requestBody.getBytes());
                        String finalstr = "HTTP/1.1 201 Created\r\n"
                                + "Content-Type: application/octet-stream\r\n"
                                + "Content-Length: " + "requestBody".length() +
                                "\r\n\r\n" + "requestBody";
                        output.write(finalstr.getBytes());
                    }catch (Exception ex){
                        System.out.println("here"+ Arrays.toString(ex.getStackTrace()));
                    }



                    System.out.println("Not Sent");

                    output.write(
                            "HTTP/1.1 404 Not Found\r\n\r\n".getBytes());

                }catch (Exception ex){
                    System.out.println("Exception occurred:: "+ex.getMessage());
                }
            }else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            output.flush();
            System.out.println("accepted new connection");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
