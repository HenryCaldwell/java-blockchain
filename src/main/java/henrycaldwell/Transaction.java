package henrycaldwell;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Represents a transaction in the blockchain.
 */
public class Transaction {

    public String transactionId; // The unique identifier of the transaction.
    public PublicKey sender; // The public key of the sender.
    public PublicKey recipient; // The public key of the recipient.
    public double value; // The value of the transaction.
    public double fee; // The value of the transaction fee.
    public byte[] signature; // The digital signature of the transaction.

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>(); // The list of transaction inputs.
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>(); // The list of transaction outputs.

    private static int sequence = 0; // A counter to avoid identical hashes.

	/**
     * Constructs a Transaction with the specified sender, recipient, value, and inputs.
     * @param from The public key of the sender.
     * @param to The public key of the recipient.
     * @param value The value of the transaction.
     * @param inputs The list of inputs for the transaction.
     */
    public Transaction(PublicKey from, PublicKey to, double value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
        this.fee = calculateTransactionFee();
	}

    /**
     * Calculates the transaction fee based on the size of the transaction and the fee rate.
     * @return The calculated transaction fee.
     */
    private double calculateTransactionFee() {
        return calculateTransactionSize() * Blockchain.feeRate;
    }

    /**
     * Calculates the size of the transaction in bytes.
     * @return The size of the transaction.
     */
    private int calculateTransactionSize() {
        int size = 0;

        size += 4;  // Version
        size += 1;  // Input Count
        size += 1;  // Output Count
        size += 4;  // Locktime

        // Inputs
        if (inputs != null) {
            for (@SuppressWarnings("unused") TransactionInput input : inputs) {
                size += 32; // Previous Transaction Hash
                size += 4;  // Previous Transaction Output Index
                size += 1;  // ScriptSig Size
                size += 72; // Signature
                size += 33; // Public Key (compressed)
                size += 4;  // Sequence
            }
        }

        // Outputs
        if (outputs != null) {
            for (@SuppressWarnings("unused") TransactionOutput output : outputs) {
                size += 8;  // Value
                size += 1;  // ScriptPubKey Size
                size += 25; // ScriptPubKey
            }
        }

        return size;
    }

	/**
     * Calculates the hash of the transaction.
     * @return The calculated hash.
     */
    private String calulateHash() {
		sequence++;
		return StringUtil.applySha256(
			StringUtil.getStringFromKey(sender) + 
			StringUtil.getStringFromKey(recipient) + 
			Double.toString(value) + 
            Double.toString(fee) +
			sequence);
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
    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + 
			StringUtil.getStringFromKey(recipient) + 
			Double.toString(value) +
            Double.toString(fee);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

	/**
     * Processes the transaction by verifying its signature, checking its inputs, and updating the blockchain.
     * @return True if the transaction is successfully processed, false otherwise.
     */
    public boolean processTransaction() {
        if (!verifiySignature()) {
			System.out.println("*Transaction signature failed to verify*");
			return false;
		}

        for (TransactionInput input : inputs) {
			input.UTXO = Blockchain.UTXOs.get(input.transactionOutputId);
		}

        if (getInputsValue() < Blockchain.minimumTransaction) {
			System.out.println("*Transaction inputs too small: " + getInputsValue() + "*");
			return false;
		}

        Double totalValue = value + fee;

        if (getInputsValue() < totalValue) {
            System.out.println("*Transaction inputs too small to cover value and fee: " + getInputsValue() + "*");
            return false;
        }

        Double leftOver = getInputsValue() - totalValue;
        transactionId = calulateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        for (TransactionOutput output : outputs) {
			Blockchain.UTXOs.put(output.id, output);
		}

        for (TransactionInput input : inputs) {
			if (input.UTXO == null) continue;
			Blockchain.UTXOs.remove(input.UTXO.id);
		}

        return true;
    }

	/**
     * Calculates the total value of the transaction inputs.
     * @return The total value.
     */
    public double getInputsValue() {
		double total = 0;

		for (TransactionInput i : inputs) {
			if (i.UTXO == null) continue;
			total += i.UTXO.value;
		}

		return total;
	}

	/**
     * Calculates the total value of the transaction outputs.
     * @return The total value.
     */
    public double getOutputsValue() {
		double total = 0;

		for (TransactionOutput output : outputs) {
			total += output.value;
		}

		return total;
	}
}
