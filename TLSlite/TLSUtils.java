import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Utility class for TLSlite. Provides methods for loading certificates/keys,
 * performing Diffie–Hellman operations, key derivation using a simplified HKDF,
 * and RSA signing/verification. Also defines serializable handshake message classes.
 */
public class TLSUtils {



    /////////////////////////////////////////
    // ----Certificate and Key Methods--- //
    ///////////////////////////////////////
    /**
     * Loads an X.509 certificate from a file.
     * @param filePath the path to the PEM-formatted certificate file.
     * @return the loaded Certificate.
     * @throws Exception if an error occurs while reading or parsing the file.
     */
    public static Certificate loadCertificate(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(fis);
        }
    }



    /**
     * Loads a DER-encoded private key (PKCS#8 format) from a file.
     * @param filePath the path to the private key file.
     * @param algorithm the algorithm, e.g., "RSA".
     * @return the loaded PrivateKey.
     * @throws Exception if an error occurs while reading or parsing the key.
     */
    public static PrivateKey loadPrivateKey(String filePath, String algorithm) throws Exception {
        File file = new File(filePath);
        byte[] keyBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(keyBytes);
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(spec);
    }




    /////////////////////////////////////////
    // -------Diffie–Hellman Methods-----//
    /////////////////////////////////////////
    /**
     * Computes the Diffie–Hellman public value.
     * @param g the generator.
     * @param privateKey the private key as a BigInteger.
     * @param p the prime modulus.
     * @return the DH public value (g^privateKey mod p).
     */
    public static BigInteger computeDHPublic(BigInteger g, BigInteger privateKey, BigInteger p) {
        return g.modPow(privateKey, p);
    }



    /**
     * Generates a random Diffie–Hellman private key.
     * @param p the prime modulus.
     * @return a random BigInteger less than p.
     */
    public static BigInteger generateDHPrivate(BigInteger p) {
        SecureRandom random = new SecureRandom();
        // Generates a random private key with bit length one less than p.
        return new BigInteger(p.bitLength() - 1, random);
    }



    //////////////////////////////////////////
    //---- HKDF and Key Derivation-----------//
    //////////////////////////////////////////
    /**
     * Expands the input key using a simplified HKDF process.
     * @param inputKey the input key material.
     * @param tag the tag string to differentiate keys.
     * @return the first 16 bytes of the HMAC result as the derived key.
     * @throws Exception if an error occurs during HMAC computation.
     */

    // It computes HMAC(key = inputKey, data = (tag || 0x01)) and returns the first 16 bytes.
    public static byte[] hkdfExpand(byte[] inputKey, String tag) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(inputKey, "HmacSHA256");
        hmac.init(keySpec);
        byte[] tagBytes = tag.getBytes("UTF-8");
        // Concatenate tag with a byte (value 1) per the HKDF pseudocode.
        byte[] data = new byte[tagBytes.length + 1];
        System.arraycopy(tagBytes, 0, data, 0, tagBytes.length);
        data[data.length - 1] = 1;
        byte[] okm = hmac.doFinal(data);
        byte[] result = new byte[16];
        System.arraycopy(okm, 0, result, 0, 16);
        return result;
    }




    /**
     * Derives session keys using the client nonce and shared Diffie–Hellman secret.
     * @param clientNonce the 32-byte nonce from the client.
     * @param sharedSecret the shared DH secret as a byte array.
     * @return a SecretKeys object containing all derived keys.
     * @throws Exception if an error occurs during key derivation.
     */
    // Derive all secret keys using the handshake nonce and shared secret.
    public static SecretKeys makeSecretKeys(byte[] clientNonce, byte[] sharedSecret) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeys keys = new SecretKeys();
        // Compute the pseudorandom key (prk) using the client nonce as the HMAC key.
        SecretKeySpec nonceKey = new SecretKeySpec(clientNonce, "HmacSHA256");
        hmac.init(nonceKey);
        byte[] prk = hmac.doFinal(sharedSecret);

        // Derive each session key by chaining HKDF expansion with specific tags.
        keys.serverEncrypt = hkdfExpand(prk, "server encrypt");
        keys.clientEncrypt = hkdfExpand(keys.serverEncrypt, "client encrypt");
        keys.serverMAC     = hkdfExpand(keys.clientEncrypt, "server MAC");
        keys.clientMAC     = hkdfExpand(keys.serverMAC, "client MAC");
        keys.serverIV      = hkdfExpand(keys.clientMAC, "server IV");
        keys.clientIV      = hkdfExpand(keys.serverIV, "client IV");

        return keys;
    }



    //////////////////////////////////////////
    // -----RSA Signing and Verification----//
    //////////////////////////////////////////

    /**
     * Signs the given data using RSA with SHA-256.
     * @param data the data to sign.
     * @param privateKey the RSA private key.
     * @return the digital signature as a byte array.
     * @throws Exception if an error occurs during signing.
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }



    /**
     * Verifies an RSA signature using SHA-256.
     * @param data the original data.
     * @param sig the signature to verify.
     * @param publicKey the RSA public key.
     * @return true if the signature is valid; false otherwise.
     * @throws Exception if an error occurs during verification.
     */
    public static boolean verifySignature(byte[] data, byte[] sig, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sig);
    }




    //////////////////////////////////////////
    // -------Handshake Message Classes------//
    //////////////////////////////////////////

    /**
     * Class representing the server's handshake message.
     * Contains the server's certificate, DH public value, and signature.
     */
    public static class ServerHandshakeMessage implements Serializable {
        public byte[] certificateBytes; // The server's certificate (DER-encoded)
        public BigInteger dhPublic;     // Server’s Diffie–Hellman public value
        public byte[] signature;        // RSA signature on the DH public value
    }


    /**
     * Class representing the client's handshake message.
     * Contains the client's certificate, DH public value, and signature.
     */
    public static class ClientHandshakeMessage implements Serializable {
        public byte[] certificateBytes; // The client’s certificate (DER-encoded)
        public BigInteger dhPublic;     // Client’s Diffie–Hellman public value
        public byte[] signature;        // RSA signature on the DH public value
    }

    /**
     * Container class for all derived session keys.
     */
    public static class SecretKeys {
        public byte[] serverEncrypt;
        public byte[] clientEncrypt;
        public byte[] serverMAC;
        public byte[] clientMAC;
        public byte[] serverIV;
        public byte[] clientIV;
    }
}
