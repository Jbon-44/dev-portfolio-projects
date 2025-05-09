import java.util.HashMap;

public class DNSCache {
    // A HashMap to store cached DNS responses
    private final HashMap<DNSQuestion, DNSRecord> cache = new HashMap<>();

    /**
     * Checks the cache for a DNS answer.
     * @param question The DNS question being queried.
     * @return The cached DNS record if available and not expired, otherwise null.
     */
    public DNSRecord query(DNSQuestion question) {
        DNSRecord record = cache.get(question);

        // If record exists but is expired, remove it from the cache
        if (record != null && record.isExpired()) {
            cache.remove(question);
            return null;
        }
        return record;
    }

    /**
     * Inserts a new record into the cache.
     * @param question The DNS question being stored.
     * @param record The DNS answer associated with the question.
     */
    public void insert(DNSQuestion question, DNSRecord record) {
        cache.put(question, record);
    }
}
