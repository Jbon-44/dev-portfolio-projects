import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TLSClient implements the client side of the simplified TLS handshake.
 * It connects to the server, sends its nonce, exchanges handshake messages,
 * derives session keys, and receives a secure message at the end.
 */
public class TLSClient {
    // These Diffie–Hellman parameters must match the server’s---//
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
     * Main method for TLSClient.
     */
    public static void main(String[] args) {
        try {
            //--- Load the CA certificate (to verify the server’s certificate)---//
            Certificate caCert = TLSUtils.loadCertificate("CAcertificate.pem");

            //--- Load the client’s certificate and private key---//
            Certificate clientCert = TLSUtils.loadCertificate("CASignedClientCertificate.pem");
            PrivateKey clientPrivateKey = TLSUtils.loadPrivateKey("clientPrivateKey.der", "RSA");

            //--- Connect to the server---//
            Socket socket = new Socket("localhost", 4444);
            System.out.println("Connected to server.");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());


            ////////////////////////////////////////////////////
            // -------------- Handshake Phase ----------------//
            ////////////////////////////////////////////////////

            // ----Generate and send a random 32-byte nonce---//
            byte[] clientNonce = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(clientNonce);
            out.writeObject(clientNonce);
            System.out.println("Sent client nonce.");

            // ---Receive the server handshake messag---//
            TLSUtils.ServerHandshakeMessage serverMsg = (TLSUtils.ServerHandshakeMessage) in.readObject();
            System.out.println("Received server handshake message.");


            // -- Generate the client’s Diffie–Hellman private and public key--//
            BigInteger clientDHPriv = TLSUtils.generateDHPrivate(p);
            BigInteger clientDHPub = TLSUtils.computeDHPublic(g, clientDHPriv, p);
            byte[] clientDHPubBytes = clientDHPub.toByteArray();

            // ---Sign the client’s DH public value---//
            byte[] clientSignature = TLSUtils.signData(clientDHPubBytes, clientPrivateKey);

            //-- Build and send the client handshake message---//
            TLSUtils.ClientHandshakeMessage clientMsg = new TLSUtils.ClientHandshakeMessage();
            clientMsg.certificateBytes = clientCert.getEncoded();
            clientMsg.dhPublic = clientDHPub;
            clientMsg.signature = clientSignature;
            out.writeObject(clientMsg);
            System.out.println("Sent client handshake message.");

            // -- Compute the shared Diffie–Hellman secret ---//
            BigInteger sharedSecret = serverMsg.dhPublic.modPow(clientDHPriv, p);
            byte[] sharedSecretBytes = sharedSecret.toByteArray();

            // ----Derive session keys----//
            TLSUtils.SecretKeys keys = TLSUtils.makeSecretKeys(clientNonce, sharedSecretBytes);

            // ----Assemble handshake message---//
            ByteArrayOutputStream handshakeStream = new ByteArrayOutputStream();
            ObjectOutputStream handshakeOut = new ObjectOutputStream(handshakeStream);
            handshakeOut.writeObject(clientNonce);
            handshakeOut.writeObject(serverMsg);
            handshakeOut.writeObject(clientMsg);
            handshakeOut.flush();
            byte[] handshakeData = handshakeStream.toByteArray();

            // ----Receive the server’s handshake HMAC ---//
            byte[] serverHandshakeHMAC = (byte[]) in.readObject();
            System.out.println("Received server handshake HMAC.");

            // ---Compute and send the client’s handshake HMAC---//
            ByteArrayOutputStream fullHandshakeStream = new ByteArrayOutputStream();
            fullHandshakeStream.write(handshakeData);
            fullHandshakeStream.write(serverHandshakeHMAC);
            byte[] fullHandshakeData = fullHandshakeStream.toByteArray();
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec macKeySpec = new SecretKeySpec(keys.clientMAC, "HmacSHA256");
            hmac.init(macKeySpec);
            byte[] clientHandshakeHMAC = hmac.doFinal(fullHandshakeData);
            out.writeObject(clientHandshakeHMAC);
            System.out.println("Sent client handshake HMAC.");

            System.out.println("Handshake complete.");

            // --- Secure Communication Phase --- //
            String secureMessage = (String) in.readObject();
            System.out.println("Received secure message: " + secureMessage);

            // Cleanup //
            in.close();
            out.close();
            socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
