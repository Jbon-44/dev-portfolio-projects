import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {
    private String[] domainName;  // The domain name this record corresponds to
    private int type;   // Record type:  A, AAAA, CNAME
    private int clazz;  // Usually 1 (IN - Internet)
    private int ttl;    // Time-to-live (how long this record is valid)
    private byte[] data; // The actual answer data : IP address
    private Date creationDate; // Timestamp when the record was cached

    /**
     * Decodes a DNS record from an InputStream.
     * @param inputStream The input stream containing DNS record data.
     * @param message The DNSMessage object containing this record.
     * @return A DNSRecord object.
     */
    public static DNSRecord decodeRecord(InputStream inputStream, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();

        // Read the domain name using message helper method (handles compression)
        record.domainName = message.readDomainName(inputStream);

        DataInputStream dataInput = new DataInputStream(inputStream);

        // Read 16-bit type: A, AAAA, CNAME
        record.type = dataInput.readUnsignedShort();

        // Read 16-bit class (usually 1 for Internet)
        record.clazz = dataInput.readUnsignedShort();

        // Read 32-bit TTL (time-to-live)
        record.ttl = dataInput.readInt();

        // Read 16-bit data length
        int dataLength = dataInput.readUnsignedShort();

        // Read the actual answer data
        record.data = new byte[dataLength];
        dataInput.readFully(record.data);

        // Set the creation time to now (used for expiration checks)
        record.creationDate = new Date();

        return record;
    }


    /**
     * Writes this record to an output stream.
     * @param outputStream The output stream to write the record bytes.
     * @param domainLocations HashMap to track domain name positions for compression.
     */
    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations) throws IOException {

        // Write domain name using DNS encoding (supports compression)
        DNSMessage.writeDomainName(outputStream, domainLocations, domainName);

        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        // Write type, class, TTL, and data length
        dataOutput.writeShort(type);
        dataOutput.writeShort(clazz);
        dataOutput.writeInt(ttl);
        dataOutput.writeShort(data.length);
        dataOutput.write(data);
    }

    /**
     * Checks if the record has expired based on its TTL.
     * @return true if expired, false otherwise.
     */
    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = creationDate.getTime() + (ttl * 1000L);
        return currentTime > expirationTime;
    }

    @Override
    //modified version for debugging
    public String toString() {
        return "DNSRecord{" +
                "domainName=" + String.join(".", domainName) +
                ", type=" + type +
                ", clazz=" + clazz +
                ", ttl=" + ttl +
                ", data=" + Arrays.toString(data) + //Print raw byte array
                ", creationDate=" + creationDate +
                '}';
    }

}
