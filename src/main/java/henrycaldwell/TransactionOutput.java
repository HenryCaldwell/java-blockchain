package henrycaldwell;

import java.security.PublicKey;

/**
 * Represents an output in a blockchain transaction.
 */
public class TransactionOutput {

    private String id; // The unique identifier of the transaction output.
    private PublicKey recipient; // The public key of the recipient.
    private double value; // The value of the output.
    private String parentTransactionId; // The ID of the transaction this output belongs to.

    /**
     * Constructs a TransactionOutput with the specified recipient, value, and IDs.
     * @param recipient The public key of the recipient.
     * @param value The value that was transfered.
     * @param parentTransactionId The ID of the parent transaction.
     */
    public TransactionOutput(PublicKey recipient, double value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + Double.toString(value) + parentTransactionId);
    }

    /**
     * Checks if the provided public key matches the recipient's public key.
     * @param publicKey The public key to check.
     * @return True if the public key matches the recipient's public key, false otherwise.
     */
    public boolean isMine(PublicKey publicKey) {
        return publicKey == recipient;
    }

    /**
     * Returns the unique identifier of the transaction output.
     * @return The unique identifier of the transaction output.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the public key of the recipient who can claim this output.
     * @return The public key of the recipient.
     */
    public PublicKey getRecipient() {
        return recipient;
    }

    /**
     * Returns the value of the output.
     * @return The value of the output.
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the ID of the transaction this output belongs to.
     * @return The ID of the parent transaction.
     */
    public String getParentTransactionId() {
        return parentTransactionId;
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "id='" + id + '\'' +
                ", recipient='" + recipient + '\'' +
                ", value=" + value +
                ", parentTransactionId='" + parentTransactionId + '\'' +
                '}';
    }
}
