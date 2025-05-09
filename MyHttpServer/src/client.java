import java.io.IOException;
import java.net.Socket;

public class client {
    public static void handleClient(Socket clientSocket) {
        try {
            // Parse HTTP request
            HTTPRequest request = new HTTPRequest(clientSocket);

            // Handle response
            HTTPResponse response = new HTTPResponse(clientSocket, request); //respond object
            response.sendResponse(); //send response to client (file or error)

        } catch (IOException e) { //handle any I/O errors

            System.err.println("I/O Error: " + e.getMessage());

        }
        try { //close socket after request is processed
            clientSocket.close(); //can throw closing error

        } catch (IOException e) { //handle error when closing socket
            System.err.println("Failed to close socket: " + e.getMessage());
        }
        }
    }
