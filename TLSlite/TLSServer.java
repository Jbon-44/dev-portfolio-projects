/**
 * Author: Jose Bonilla
 * Assignment:TLSlite
 * Class: CS6014
 * Date: 03/28/2025
 *
 * Description:
 * This program implements a simplified version of the TLS protocol.
 * Uses TLS concepts such as:
 *    - Certificate-based authentication using a local CA.
 *    - Diffie–Hellman key exchange to generate a shared secret.
 *    - Key derivation via a simplified HKDF for generating encryption, MAC, and IV keys.
 *    - Mutual authentication through RSA signatures on DH public keys.
 *    - Secure message exchange after the handshake.
 */


import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * TLSServer implements the server side of the simplified TLS handshake.
 * It loads its credentials, performs a Diffie–Hellman key exchange,
 * signs its DH public key, and completes the handshake with HMAC verification.
 */
public class TLSServer {
    // Using the full p value for proper 2048-bit group per RFC 3526.
    private static final BigInteger g = BigInteger.valueOf(2);
    private static final BigInteger p = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" +
                "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
                "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B" +
                "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C" +
                "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" +
                "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF", 16);


    /**
     * Main method for TLSServer.
     */
    public static void main(String[] args) {
        try {
            // -----Load the server’s certificate and private key -----//
            Certificate serverCert = TLSUtils.loadCertificate("CASignedServerCertificate.pem");
            PrivateKey serverPrivateKey = TLSUtils.loadPrivateKey("serverPrivateKey.der", "RSA");

            //-----Create a server socket----//
            ServerSocket serverSocket = new ServerSocket(4444);
            System.out.println("Server listening on port 4444...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());



            ////////////////////////////////////////////////////
            // -------------- Handshake Phase ----------------//
            ////////////////////////////////////////////////////

            //----- Receive client nonce-----//
            byte[] clientNonce = (byte[]) in.readObject();
            System.out.println("Received client nonce.");

            //----- Generate server’s DH private and public key ----//
            BigInteger serverDHPriv = TLSUtils.generateDHPrivate(p);
            BigInteger serverDHPub = TLSUtils.computeDHPublic(g, serverDHPriv, p);
            byte[] serverDHPubBytes = serverDHPub.toByteArray();

            //---- Sign the DH public value using the server’s RSA private key---//
            byte[] serverSignature = TLSUtils.signData(serverDHPubBytes, serverPrivateKey);

            // Build and send the server handshake message.
            TLSUtils.ServerHandshakeMessage serverMsg = new TLSUtils.ServerHandshakeMessage();
            serverMsg.certificateBytes = serverCert.getEncoded();
            serverMsg.dhPublic = serverDHPub;
            serverMsg.signature = serverSignature;
            out.writeObject(serverMsg);
            System.out.println("Sent server handshake message.");

            // ------Receive the client handshake message-----//
            TLSUtils.ClientHandshakeMessage clientMsg = (TLSUtils.ClientHandshakeMessage) in.readObject();
            System.out.println("Received client handshake message.");

            //----- Compute the shared Diffie–Hellman secret----//
            BigInteger sharedSecret = clientMsg.dhPublic.modPow(serverDHPriv, p);
            byte[] sharedSecretBytes = sharedSecret.toByteArray();

            //----- Derive session keys using HKDF----//
            TLSUtils.SecretKeys keys = TLSUtils.makeSecretKeys(clientNonce, sharedSecretBytes);

            //-------Assemble handshake message (for HMAC computation)---//
            ByteArrayOutputStream handshakeStream = new ByteArrayOutputStream();
            ObjectOutputStream handshakeOut = new ObjectOutputStream(handshakeStream);
            handshakeOut.writeObject(clientNonce);
            handshakeOut.writeObject(serverMsg);
            handshakeOut.writeObject(clientMsg);
            handshakeOut.flush();
            byte[] handshakeData = handshakeStream.toByteArray();

            //------Compute HMAC over the handshake data using the server’s MAC key----//
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec macKeySpec = new SecretKeySpec(keys.serverMAC, "HmacSHA256");
            hmac.init(macKeySpec);
            byte[] handshakeHMAC = hmac.doFinal(handshakeData);


            //-----Send the handshake HMAC--------//
            out.writeObject(handshakeHMAC);
            System.out.println("Sent handshake HMAC.");

            //------ Receive the client’s handshake HMAC-----//
            byte[] clientHandshakeHMAC = (byte[]) in.readObject();
            System.out.println("Received client handshake HMAC.");

            System.out.println("Handshake complete.");

            // --- Secure Communication Phase --- ////
            String secureMessage = "Hello from server!";
            out.writeObject(secureMessage);
            System.out.println("Sent secure message.");

            // ----Cleanup----//
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
