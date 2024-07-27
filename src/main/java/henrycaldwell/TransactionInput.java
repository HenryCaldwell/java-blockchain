package henrycaldwell;

/**
 * Represents an input in a blockchain transaction.
 */
public class TransactionInput {
    
    private String transactionOutputId; // The ID of the transaction output.
    private TransactionOutput UTXO; // The unspent transaction output (UTXO) that this input references.

    /**
     * Constructs a TransactionInput with the specified transaction output ID.
	 * @param transactionOutputId The ID of the transaction output to be used.
     */
    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
	}

    /**
     * Returns the ID of the transaction output.
     * @return The ID of the transaction output.
     */
    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    /**
     * Returns the unspent transaction output (UTXO) that this input references.
     * @return The UTXO that this input references.
     */
    public TransactionOutput getUTXO() {
        return UTXO;
    }

    /**
     * Sets the unspent transaction output (UTXO) that this input references.
     * @param UTXO The UTXO to set.
     */
    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }

    @Override
    public String toString() {
        return "TransactionInput{" +
                "transactionOutputId='" + transactionOutputId + '\'' +
                ", UTXO=" + UTXO +
                '}';
    }
}
