import java.io.*;
import java.util.*;

public class DNSMessage {
    private DNSHeader header;          // The DNS header
    private DNSQuestion[] questions;   // The list of questions in the DNS request
    private DNSRecord[] answers;       // The list of answers in the DNS response
    private DNSRecord[] additionalRecords; // Additional records
    private byte[] rawMessage;         // Raw bytes of the original message

    private HashMap<String, Integer> domainNameLocations = new HashMap<>(); // Stores domain positions for compression

    /**
     * Decodes a raw DNS message byte array into a DNSMessage object.
     * @param bytes The raw bytes of the DNS message.
     * @return A DNSMessage object representing the parsed message.
     */

    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage message = new DNSMessage();
        message.rawMessage = bytes;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        // Decode the DNS header
        message.header = DNSHeader.decodeHeader(inputStream);

        // Decode questions
        message.questions = new DNSQuestion[message.header.qdCount];
        for (int i = 0; i < message.questions.length; i++) {
            message.questions[i] = DNSQuestion.decodeQuestion(inputStream, message);
        }

        // Always initialize 'answers' even if empty
        message.answers = (message.header.anCount > 0) ? new DNSRecord[message.header.anCount] : new DNSRecord[0];


        for (int i = 0; i < message.answers.length; i++) {
            message.answers[i] = DNSRecord.decodeRecord(inputStream, message);
        }

        // Always initialize 'additionalRecords'
        message.additionalRecords = (message.header.arCount > 0) ? new DNSRecord[message.header.arCount] : new DNSRecord[0];


        for (int i = 0; i < message.additionalRecords.length; i++) {
            message.additionalRecords[i] = DNSRecord.decodeRecord(inputStream, message);
        }

        return message;
    }


    /**
     * Reads a domain name from an InputStream.
     * Handles compression if needed.
     * @param inputStream The input stream containing the DNS message.
     * @return An array of domain name segments.
     */
    public String[] readDomainName(InputStream inputStream) throws IOException {
        List<String> domainParts = new ArrayList<>();
        int length = inputStream.read(); // Read the first length byte

        while (length > 0) {
            // Check if this is a pointer (compression is used)
            if ((length & 0xC0) == 0xC0) { // 0xC0 = 11000000 in binary, checks if the first two bits are set to 1 (compression flag)

                int pointer = ((length & 0x3F) << 8) | inputStream.read();

                // (length & 0x3F) masks out the first two bits, leaving the actual pointer bits
                // << 8 shifts these bits left by 8 to make room for the next byte
                // | combines the shifted bits with the next byte to form the full 14-bit pointer

                domainParts.addAll(Arrays.asList(readDomainName(pointer))); // Recursively read the domain name from the pointer location
                break; // Exit loop after resolving the pointer

            } else {
                // Read a full domain part
                byte[] part = new byte[length];
                inputStream.read(part);
                domainParts.add(new String(part));
                length = inputStream.read(); // Read the next length byte
            }
        }

        return domainParts.toArray(new String[0]);
    }


    /**
     * Reads a domain name from a given byte index.
     * This is used when handling compressed domain names.
     * @param firstByte The position in the message to start reading from.
     * @return An array of domain name segments.
     */
    public String[] readDomainName(int firstByte) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(rawMessage);
        byteStream.skip(firstByte);
        return readDomainName(byteStream);
    }



    /**
     * Builds a DNS response message based on a request and a set of answers.
     * @param request The original DNS request message.
     * @param answers The DNS records to include in the response.
     * @return A DNSMessage object representing the response.
     */
    public static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) {
        DNSMessage response = new DNSMessage();

        response.answers = (answers != null) ? answers : new DNSRecord[0];

        response.header = DNSHeader.buildHeaderForResponse(request, response);

        response.questions = request.questions; // Copy the questions

        response.answers = (answers != null) ? answers : new DNSRecord[0]; //added to ensure response answer is never null

        response.additionalRecords = new DNSRecord[0]; // No additional records for now

        return response;


    }

    /**
     * Converts the DNSMessage object into a byte array for network transmission.
     * @return The byte array representing the DNS message.
     * @throws IOException If an I/O error occurs.
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Write the header to the output stream
        header.writeBytes(outputStream);


        // Write the question section
        for (DNSQuestion question : questions) {
            question.writeBytes(outputStream, domainNameLocations);
        }


        // Write the answer section
        for (DNSRecord answer : answers) {
            answer.writeBytes(outputStream, domainNameLocations);
        }

        if (answers == null || answers.length == 0) {
        } else {
            for (DNSRecord answer : answers) {
                answer.writeBytes(outputStream, domainNameLocations);
            }
        }

        return outputStream.toByteArray();

    }

    /**
     * Writes a domain name to an output stream with DNS compression support.
     * If the domain has been written before, a pointer is used instead.
     * @param outputStream The output stream to write the domain name.
     * @param domainLocations A map storing domain name positions for compression.
     * @param domainPieces The domain name split into segments.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations, String[] domainPieces) throws IOException {
        String domainName = String.join(".", domainPieces);

        // Check if we have written this domain before (for compression)
        if (domainLocations.containsKey(domainName)) {
            int pointer = domainLocations.get(domainName);
            outputStream.write((pointer >> 8) | 0xC0); // Write compression flag (0xC0)

            // (pointer >> 8) gets the upper byte of the pointer
            // | 0xC0 ensures the first two bits are set to indicate compression

            outputStream.write(pointer & 0xFF); // Write offset -- lower byte of the pointer AND with 0xFF to keep only the last 8 bits

        } else {
            // Store the domain location for potential future compression
            domainLocations.put(domainName, outputStream.size());

            // Write each domain segment with its length prefix
            for (String part : domainPieces) {
                outputStream.write(part.length());
                outputStream.write(part.getBytes());
            }
            outputStream.write(0); // Null terminator
        }
    }


    /**
     * Getter for the DNS header.
     * @return The DNSHeader object.
     */
    public DNSHeader getHeader() {
        return header;
    }

    /**
     * Getter for the DNS questions.
     * @return An array of DNSQuestion objects.
     */
    public DNSQuestion[] getQuestions() {
        return questions;
    }

    /**
     * Getter for the DNS answers.
     * @return An array of DNSRecord objects.
     */
    public DNSRecord[] getAnswers() {
        return answers;
    }

    /**
     * Getter for additional records.
     * @return An array of additional DNS records.
     */
    public DNSRecord[] getAdditionalRecords() {
        return additionalRecords;
    }
}
