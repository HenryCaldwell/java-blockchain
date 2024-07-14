package henrycaldwell;

import java.security.PublicKey;

/**
 * Represents an output in a blockchain transaction.
 */
public class TransactionOutput {

    public String id; // The unique identifier of the transaction output.
    public PublicKey reciepient; // The public key of the recipient who can claim this output.
    public double value; // The value of the output.
    public String parentTransactionId; // The ID of the transaction this output belongs to.

    /**
     * Constructs a TransactionOutput instance with the specified recipient, value, and IDs.
     * @param reciepient The public key of the recipient.
     * @param value The value that was transfered.
     * @param parentTransactionId The ID of the parent transaction.
     */
    public TransactionOutput(PublicKey reciepient, double value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient) + Double.toString(value) + parentTransactionId);
    }

    /**
     * Checks if the provided public key matches the recipient's public key.
     * @param publicKey The public key to check.
     * @return True if the public key matches the recipient's public key, false otherwise.
     */
    public boolean isMine(PublicKey publicKey) {
        return publicKey == reciepient;
    }
}
