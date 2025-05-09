import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    private String[] domainName;  // The domain being queried
    private int qtype;  // Record type like: A, AAAA, CNAME
    private int qclass; // Usually 1 (IN - Internet)


    /**
     * Decodes a DNS question from an InputStream.
     * @param inputStream The input stream containing DNS question data.
     * @param message The DNS message containing the question.
     * @return A DNSQuestion object.
     */
    public static DNSQuestion decodeQuestion(InputStream inputStream, DNSMessage message) throws IOException {
        DNSQuestion question = new DNSQuestion();

        // Read the domain name using message helper method
        question.domainName = message.readDomainName(inputStream);

        DataInputStream dataInput = new DataInputStream(inputStream);

        // Read 16-bit type: A, AAAA, CNAME etc.
        question.qtype = dataInput.readUnsignedShort();

        // Read 16-bit class (usually 1 for Internet)
        question.qclass = dataInput.readUnsignedShort();

        return question;
    }

    /**
     * Writes the question into an output stream.
     * @param outputStream The output stream to write question bytes.
     * @param domainNameLocations HashMap to track domain name positions for compression.
     */
    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainNameLocations) throws IOException {

        // Write domain name, allowing for compression
        DNSMessage.writeDomainName(outputStream, domainNameLocations, domainName);

        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        // Write the type and class fields
        dataOutput.writeShort(qtype);
        dataOutput.writeShort(qclass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return qtype == that.qtype && qclass == that.qclass && Objects.equals(domainName, that.domainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainName, qtype, qclass);
    }

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "domainName=" + String.join(".", domainName) +
                ", qtype=" + qtype +
                ", qclass=" + qclass +
                '}';
    }
}
