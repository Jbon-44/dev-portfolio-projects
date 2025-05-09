import java.io.*;

public class DNSHeader {
    private int id;        // Unique ID for the request/response
    private int flags;     // Contains query/response type and other control bits
    int qdCount;   // Number of questions in the request
    int anCount;   // Number of answer records
    private int nsCount;   // Number of authority records
    int arCount;   // Number of additional records 

    /**
     * Decodes the DNS header from an InputStream.
     * @param inputStream The input stream containing the DNS request bytes.
     * @return A DNSHeader object representing the parsed header.
     */
    public static DNSHeader decodeHeader(InputStream inputStream) throws IOException {
        DNSHeader header = new DNSHeader();
        DataInputStream dataInput = new DataInputStream(inputStream);

        // Read the header fields in the correct order
        header.id = dataInput.readUnsignedShort();    // Read 16-bit ID
        header.flags = dataInput.readUnsignedShort(); // Read 16-bit flags
        header.qdCount = dataInput.readUnsignedShort(); // Number of questions
        header.anCount = dataInput.readUnsignedShort(); // Number of answers
        header.nsCount = dataInput.readUnsignedShort(); // Number of authority records (ignored)
        header.arCount = dataInput.readUnsignedShort(); // Number of additional records

        return header;
    }

    /**
     * Builds a DNS response header based on the request and response.
     * @param request The original request message.
     * @param response The response message being constructed.
     * @return A DNSHeader object representing the response header.
     */
    public static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {
        DNSHeader header = new DNSHeader();
        header.id = request.getHeader().id;   // Copy request ID to response
        header.flags = 0x8180; // Standard response: QR=1, Opcode=0, AA=0, TC=0, RD=1, RA=1, RCODE=0
        header.qdCount = request.getHeader().qdCount; // Keep question count

//        header.anCount = (response.getAnswers() != null && response.getAnswers().length > 0) ? response.getAnswers().length : 0;
        header.anCount = (response.getAnswers() != null) ? response.getAnswers().length : 0;


        header.nsCount = 0; // No authority records

        header.arCount = (response.getAdditionalRecords() != null) ? response.getAdditionalRecords().length : 0;


        return header;
    }

    /**
     * Writes the DNS header to an OutputStream
     * @param outputStream The output stream to write the header bytes
     */
    public void writeBytes(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        // Write each header field as 16-bit values
        dataOutput.writeShort(id);
        dataOutput.writeShort(flags);
        dataOutput.writeShort(qdCount);
        dataOutput.writeShort(anCount);
        dataOutput.writeShort(nsCount);
        dataOutput.writeShort(arCount);
    }

    @Override //to string method
    public String toString() {
        return "DNSHeader{" +
                "id=" + id +
                ", flags=" + flags +
                ", questionCount=" + qdCount +
                ", answerCount=" + anCount +
                ", nsCount=" + nsCount +
                ", additionalRecordCount=" + arCount +
                '}';
    }
}
