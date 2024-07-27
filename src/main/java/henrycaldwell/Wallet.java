package henrycaldwell;

import java.util.ArrayList;
import java.util.HashMap;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

/**
 * Represents a wallet in the blockchain system.
 */
public class Wallet {

    private PublicKey publicKey; // The public key of the wallet.
    private PrivateKey privateKey; // The private key of the wallet.

    private HashMap<String, TransactionOutput> ownedUTXOs = new HashMap<String, TransactionOutput>(); // The UTXOs owned by this wallet.

    /**
     * Constructs a Wallet and generates a new key pair.
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and signs a new transaction to send funds to a recipient.
     * @param recipient The public key of the recipient.
     * @param value The amount to send.
     * @return The new transaction if successful, or null if failed.
     */
    public Transaction sendFunds(PublicKey recipient, double value) {
        if (recipient == null) {
            System.out.println(StringUtil.formatText("WAL001: Recipient is Null - Transaction Discarded", StringUtil.ANSI_RED));
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();
        double total = 0;

        double requiredAmount = value;
        double fee = 0;

        getBalance();

        for (TransactionOutput output : ownedUTXOs.values()) {
            total += output.getValue();
            inputs.add(new TransactionInput(output.getId()));

            Transaction tempTransaction = new Transaction(publicKey, recipient, value, inputs);
            fee = tempTransaction.getFee();
            requiredAmount = value + fee;

            if (total >= requiredAmount) {
                break;
            }
        }

        if (total < requiredAmount) {
            System.out.println(StringUtil.formatText("WAL002: Insufficient Funds for Transaction - Transaction Discarded", StringUtil.ANSI_RED));
            return null;
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        if (!newTransaction.verifyTransaction()) {
            System.out.println(StringUtil.formatText("WAL003: Transaction Verification Failed - Transaction Discarded", StringUtil.ANSI_RED));
            return null;
        }

        return newTransaction;
    }

    /**
     * Returns the public key.
     * @return The public key.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Returns the private key.
     * @return The private key.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the total balance and updates ownedUTXOs.
     * @return The total balance.
     */
    public double getBalance() {
        ownedUTXOs.clear();
        double total = 0;

        for (TransactionOutput output : Blockchain.UTXOs.values()) {
            if (output.isMine(publicKey)) {
                ownedUTXOs.put(output.getId(), output);
                total += output.getValue();
            }
        }

        return total;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "publicKey=" + publicKey +
                ", privateKey=" + privateKey +
                ", UTXOs=" + ownedUTXOs +
                '}';
    }
}
