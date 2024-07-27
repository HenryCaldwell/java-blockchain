package henrycaldwell;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transaction in the blockchain.
 */
public class Transaction {

    private String transactionId; // The unique identifier of the transaction.
    private PublicKey sender; // The public key of the sender.
    private PublicKey recipient; // The public key of the recipient.
    private double value; // The value of the transaction.
    private double fee; // The value of the transaction fee.
    private byte[] signature; // The digital signature of the transaction.

    private ArrayList<TransactionInput> inputs; // The list of transaction inputs.
    private ArrayList<TransactionOutput> outputs; // The list of transaction outputs.

    /**
     * Constructs a Transaction with the specified sender, recipient, value, and inputs.
     * @param from The public key of the sender.
     * @param to The public key of the recipient.
     * @param value The value of the transaction.
     * @param inputs The list of inputs for the transaction.
     */
    public Transaction(PublicKey sender, PublicKey recipient, double value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>();
        this.fee = calculateTransactionFee();
        processTransaction();
    }

    /**
     * Calculates the hash of the transaction.
     * @return The calculated hash.
     */
    public String calculateHash() {
        StringBuilder data = new StringBuilder();

        data.append(StringUtil.getStringFromKey(sender))
                .append(StringUtil.getStringFromKey(recipient))
                .append(Double.toString(value))
                .append(Double.toString(fee));

        for (TransactionInput input : inputs) {
            data.append(input.getTransactionOutputId());
        }

        for (TransactionOutput output : outputs) {
            data.append(output.getId());
        }

        return StringUtil.applySha256(data.toString());
    }

    /**
     * Generates the digital signature for the transaction using the sender's private key.
     * @param privateKey The private key of the sender.
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Double.toString(value) +
                Double.toString(fee);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * Verifies the digital signature of the transaction.
     * @return True if the signature is valid, false otherwise.
     */
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Double.toString(value) +
                Double.toString(fee);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    /**
     * Processes the transaction by setting inputs and creating outputs.
     */
    public void processTransaction() {
        for (TransactionInput input : inputs) {
            input.setUTXO(Blockchain.UTXOs.get(input.getTransactionOutputId()));
        }

        Double totalValue = value + fee;
        Double leftOver = getInputsValue() - totalValue;

        outputs.add(new TransactionOutput(recipient, value, transactionId));

        if (leftOver > 0) {
            outputs.add(new TransactionOutput(sender, leftOver, transactionId));
        }

        transactionId = calculateHash();
    }

    /**
     * Verifies the transaction by checking the signature and the input values.
     * @return True if the transaction is valid, false otherwise.
     */
    public boolean verifyTransaction() {
        if (!verifySignature()) {
            System.out.println(StringUtil.formatText("TRX001: Transaction Signature Verification Failed - Transaction Failed to Verify", StringUtil.ANSI_RED));
            return false;
        }

        if (getInputsValue() < Blockchain.minimumTransaction) {
            System.out.println(StringUtil.formatText("TRX002: Transaction Inputs Too Small for Minimum Transaction - Transaction Failed to Verify", StringUtil.ANSI_RED));
            return false;
        }

        Double totalValue = value + fee;

        if (getInputsValue() < totalValue) {
            System.out.println(StringUtil.formatText("TRX003: Transaction Inputs Too Small for Value and Fee - Transaction Failed to Verify", StringUtil.ANSI_RED));
            return false;
        }

        return true;
    }

    /**
     * Calculates the size of the transaction in bytes.
     * @return The size of the transaction.
     */
    public int calculateTransactionSize() {
        int size = 0;

        // size += 4; // Version (not included yet)
        // size += 4; // Locktime (not included yet)

        // Inputs
        if (inputs != null) {
            for (@SuppressWarnings("unused")
            TransactionInput input : inputs) {
                size += 32; // Previous Transaction Hash
                size += 4; // Previous Transaction Output Index
                size += 1; // ScriptSig Size
                size += 72; // Signature
                size += 33; // Public Key (Compressed)
            }
        }

        // Outputs
        if (outputs != null) {
            for (@SuppressWarnings("unused")
            TransactionOutput output : outputs) {
                size += 8; // Value
                size += 1; // ScriptPubKey Size
                size += 25; // ScriptPubKey
            }
        }

        return size;
    }

    /**
     * Calculates the transaction fee based on the size of the transaction and the fee rate.
     * @return The calculated transaction fee.
     */
    public double calculateTransactionFee() {
        return calculateTransactionSize() * Blockchain.feeRate;
    }

    /**
     * Returns the transaction ID.
     * @return The transaction ID.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the sender's public key.
     * @return The sender's public key.
     */
    public PublicKey getSender() {
        return sender;
    }

    /**
     * Returns the recipient's public key.
     * @return The recipient's public key.
     */
    public PublicKey getRecipient() {
        return recipient;
    }

    /**
     * Returns the value of the transaction.
     * @return The value of the transaction.
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the transaction fee.
     * @return The transaction fee.
     */
    public double getFee() {
        return fee;
    }

    /**
     * Returns the digital signature of the transaction.
     * @return The digital signature of the transaction.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Returns the list of transaction inputs.
     * @return The list of transaction inputs.
     */
    public List<TransactionInput> getInputs() {
        return inputs;
    }

    /**
     * Returns the total value of the transaction inputs.
     * @return The total value of the transaction inputs.
     */
    public double getInputsValue() {
        double total = 0;

        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null) {
                continue;
            }

            total += input.getUTXO().getValue();
        }

        return total;
    }

    /**
     * Returns the list of transaction outputs.
     * @return The list of transaction outputs.
     */
    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    /**
     * Returns the total value of the transaction outputs.
     * @return The total value of the transaction outputs.
     */
    public double getOutputsValue() {
        double total = 0;

        for (TransactionOutput output : outputs) {
            total += output.getValue();
        }

        return total;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", value=" + value +
                ", fee=" + fee +
                ", signature=" + new String(signature) +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }
}
