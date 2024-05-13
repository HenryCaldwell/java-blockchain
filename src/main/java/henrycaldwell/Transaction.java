package henrycaldwell;

import java.security.*;
import java.util.ArrayList;

/**
 * Represents a transaction in the blockchain.
 */
public class Transaction {

    public String transactionId; // The unique identifier of the transaction.
    public PublicKey sender; // The public key of the sender.
    public PublicKey recipient; // The public key of the recipient.
    public float value; // The value of the transaction.
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
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
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
			Float.toString(value) + 
			sequence);
	}

	/**
     * Generates the digital signature for the transaction using the sender's private key.
     * @param privateKey The private key of the sender.
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + 
			StringUtil.getStringFromKey(recipient) + 
			Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);		
    }

	/**
     * Verifies the digital signature of the transaction.
     * @return True if the signature is valid, false otherwise.
     */
    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + 
			StringUtil.getStringFromKey(recipient) + 
			Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

	/**
     * Processes the transaction by verifying its signature, checking its inputs, and updating the blockchain.
     * @return True if the transaction is successfully processed, false otherwise.
     */
    public boolean processTransaction() {
        if (verifiySignature() == false) {
			System.out.println("*Transaction Signature failed to verify*");
			return false;
		}

        for (TransactionInput input : inputs) {
			input.UTXO = Blockchain.UTXOs.get(input.transactionOutputId);
		}

        if (getInputsValue() < Blockchain.minimumTransaction) {
			System.out.println("*Transaction inputs to small: " + getInputsValue() + "*");
			return false;
		}

        float leftOver = getInputsValue() - value;
        transactionId = calulateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        for (TransactionOutput output : outputs) {
			Blockchain.UTXOs.put(output.id , output);
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
    public float getInputsValue() {
		float total = 0;

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
    public float getOutputsValue() {
		float total = 0;

		for (TransactionOutput output : outputs) {
			total += output.value;
		}

		return total;
	}
}
