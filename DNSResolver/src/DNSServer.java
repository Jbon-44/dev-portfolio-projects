/**Author: Jose Bonilla
 * Class: CS6014
 * Assignment: A DNS Resolver
 * Date: 02/03/2025
 *
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class DNSServer {
    private static final int PORT = 8053; // Listening port for DNS queries
    private static final String GOOGLE_DNS = "8.8.8.8"; // Upstream resolver
    private static final int GOOGLE_DNS_PORT = 53; // Standard DNS port

    private final DNSCache cache = new DNSCache(); // Local DNS cache

    /**
     * Starts the DNS server, listening for queries and handling responses.
     */
    public void start() throws IOException {

        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("DNS Server started on port " + PORT);

        while (true) {

            // Prepare buffer for incoming request
            DatagramPacket requestPacket = new DatagramPacket(new byte[512], 512);
            socket.receive(requestPacket);

            // Extract client address and port
            InetAddress clientAddress = requestPacket.getAddress();
            System.out.println("Client address " + clientAddress);

            int clientPort = requestPacket.getPort();
            System.out.println("client port " + clientPort);

            // Convert request bytes into DNSMessage object
            byte[] requestData = requestPacket.getData();
            DNSMessage requestMessage = DNSMessage.decodeMessage(requestData);

            ArrayList<DNSRecord> answers = new ArrayList<>(); //holds answers, or IPs

            // Check cache for each question
            for (DNSQuestion question : requestMessage.getQuestions()) {

                DNSRecord cachedRecord = cache.query(question);

                if (cachedRecord != null) { // Cache hit: add the cached record to response
                    answers.add(cachedRecord);
                } else { // Cache miss: forward request to Google DNS

                    byte[] googleResponse = forwardToGoogle(requestData);


                    DNSMessage googleMessage = DNSMessage.decodeMessage(googleResponse);


                    // Cache the response and add it to the answer list
                    for (DNSRecord record : googleMessage.getAnswers()) {
                        cache.insert(question, record);
                        answers.add(record);
                    }
                }
            }

            // Construct the response message
            DNSMessage responseMessage = DNSMessage.buildResponse(requestMessage, answers.toArray(new DNSRecord[0]));


            byte[] responseData = responseMessage.toBytes();

            // Send response to client
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
            socket.send(responsePacket);
        }
    }




    /**
     * Forwards the DNS request to Google's public DNS and returns the response.
     * @param requestData The DNS request bytes.
     * @return The response bytes from Google’s DNS.
     */
    private byte[] forwardToGoogle(byte[] requestData) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        // Send the DNS request to Google's DNS server
        DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(GOOGLE_DNS), GOOGLE_DNS_PORT);
        socket.send(requestPacket);

        // Receive the response from Google
        DatagramPacket responsePacket = new DatagramPacket(new byte[512], 512);
        socket.receive(responsePacket);
        socket.close();

        // Return the response data
        return responsePacket.getData();
    }

    

    /**
     * Main entry point for starting the DNS server.
     */
    public static void main(String[] args) throws IOException {
        new DNSServer().start();
    }
}
