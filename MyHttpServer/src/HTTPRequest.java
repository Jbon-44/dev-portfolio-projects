import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class HTTPRequest {
    private String method;
    private String path;

    public HTTPRequest(Socket clientSocket) throws IOException {
        parseRequest(clientSocket);
    }

    private void parseRequest(Socket clientSocket) throws IOException {
        Scanner scanner = new Scanner(clientSocket.getInputStream()); //scanner to read input stream
        if (!scanner.hasNextLine()) {
            throw new IOException("Empty request"); //nothing to process if line is empty
        }

        String requestLine = scanner.nextLine(); //read first line like: "GET /index.html HTTP/1.1"
        System.out.println("Request: " + requestLine); //see what client is requesting

        String[] requestParts = requestLine.split(" ");

        if (requestParts.length < 2) { // if less than 2 request may be not formed correctly
            throw new IOException("Invalid HTTP request");
        }

        this.method = requestParts[0]; //extra HTTP method "GET"


        if (requestParts[1].equals("/")) {
            this.path = "index.html"; //set default index.html
        } else {
            this.path = requestParts[1].substring(1); //or remove leading '/' from path
        }
    }

    public String getPath() {
        return path;
    }
}
