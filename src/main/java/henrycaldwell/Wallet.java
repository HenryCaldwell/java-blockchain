package henrycaldwell;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Wallet {

    public PrivateKey privateKey;
	public PublicKey publicKey;

	public HashMap<String, TransactionOutput> ownedUTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeys();
    }

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

	public float getBalance() {
		float total = 0;

		for (Map.Entry<String, TransactionOutput> item: Blockchain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(publicKey)) {
            	ownedUTXOs.put(UTXO.id, UTXO);
            	total += UTXO.value; 
            }
        }  

		return total;
	}

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
