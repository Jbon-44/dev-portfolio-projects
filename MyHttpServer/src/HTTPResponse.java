import java.io.*;
import java.net.Socket;

public class HTTPResponse {
    private Socket clientSocket;
    private HTTPRequest request; //parsed HTTP request

    public HTTPResponse(Socket clientSocket, HTTPRequest request) { //constructor
        this.clientSocket = clientSocket;
        this.request = request;
    }

    public void sendResponse() throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        File file = new File("src/" + request.getPath()); // Find the requested file in the "src" directory

        if (file.exists()) {
            String contentType = determineContentType(file.getName());
            sendHeaders(out, contentType, file.length()); //calls function to send HTTP headers (status, content type, etc.)
            sendFile(file);
        } else {
            send404(out);
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".css")) {
            return "text/css";
        }
        // Default to HTML
        return "text/html";
    }

    private void sendHeaders(PrintWriter out, String contentType, long contentLength) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: " + contentType); //text/html
        out.println("Content-Length: " + contentLength); //file size in bytes
        out.println(); //blank line to indicate end of headers
        out.flush(); //push headers to client
    }
    //sends requested files content to client
    private void sendFile( File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //makes sure file is closed when operation/block of code is done (try with resource)
            fileInputStream.transferTo(clientSocket.getOutputStream());
        }
    }
    //send 404 not found if file not found
    private void send404(PrintWriter out) {
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<h1>404 Not Found</h1>");
        out.flush();
    }
}