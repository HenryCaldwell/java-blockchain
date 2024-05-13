package henrycaldwell;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * Represents a wallet in the blockchain system.
 */
public class Wallet {

	public PublicKey publicKey; // The public key of the wallet.
    public PrivateKey privateKey; // The private key of the wallet.

	public HashMap<String, TransactionOutput> ownedUTXOs = new HashMap<String, TransactionOutput>(); // The UTXOs owned by this wallet.

	/**
     * Constructs a Wallet instance and generates a new key pair.
     */
    public Wallet() {
        generateKeys();
    }

	/**
     * Generates a new public-private key pair for the wallet using ECDSA and Bouncy Castle.
     */
    private void generateKeys() {
        try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			keyGen.initialize(ecSpec, random);
	        KeyPair keyPair = keyGen.generateKeyPair();
	        privateKey = keyPair.getPrivate(); 
	        publicKey = keyPair.getPublic();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
    }

	/**
     * Calculates the balance of the wallet by summing the values of all owned UTXOs.
     * @return The total balance.
     */
	public float getBalance() {
		float total = 0;

		for (Map.Entry<String, TransactionOutput> item : Blockchain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(publicKey)) {
            	ownedUTXOs.put(UTXO.id, UTXO);
            	total += UTXO.value; 
            }
        }  

		return total;
	}

	/**
     * Creates and signs a new transaction to send funds to a recipient.
     * @param recipient The public key of the recipient.
     * @param value The amount to send.
     * @return The new transaction if successful, or null if there are insufficient funds.
     */
	public Transaction sendFunds(PublicKey recipient, float value) {
		if (getBalance() < value) {
			System.out.println("*Insufficient funds for transaction (Transaction discarded)*");
			return null;
		}

		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

		float total = 0;
		for (Map.Entry<String, TransactionOutput> item : ownedUTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if (total > value) break;
		}

		Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
		newTransaction.generateSignature(privateKey);

		for (TransactionInput input: inputs){
			ownedUTXOs.remove(input.transactionOutputId);
		}

		return newTransaction;
	}
}
