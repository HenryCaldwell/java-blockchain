package henrycaldwell;

/**
 * Represents an input in a blockchain transaction.
 */
public class TransactionInput {
    
    public String transactionOutputId; // The ID of the transaction output.
    public TransactionOutput UTXO; // The unspent transaction output (UTXO) that this input references.

    /**
     * Constructs a TransactionInput instance with the specified transaction output ID.
	 * @param transactionOutputId The ID of the transaction output to be used.
     */
    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
	}
}
