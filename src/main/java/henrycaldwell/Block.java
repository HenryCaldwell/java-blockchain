package henrycaldwell;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

/**
 * Represents a block in a blockchain.
 */
public class Block {

    private String hash, previousBlockHash; // The hash and previous hash of the previous block.
    private String merkleRoot; // The merkle root hash of all the transactions in the block.
    private long timestamp; // The time of the blocks creation in miliseconds.
    private int nonce; // The nonce value used for mining the block.

    private ArrayList<Transaction> transactions; // The list of transactions in the block.
    private Set<TransactionOutput> usedUTXOs; // A set to track used UTXOs within this block.

    /**
     * Constructs a Block by initializing hashes and timeStamp.
     * @param previousHash The hash of the previous block.
     */
    public Block(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
        this.timestamp = new Date().getTime();
        this.transactions = new ArrayList<>();
        this.usedUTXOs = new HashSet<>();
    }

    /**
     * Adds a transaction to the block after verifying it.
     * @param transaction The transaction to be added to the block.
     * @return True if the transaction was added successfully, false otherwise.
     */
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            System.out.println(StringUtil.formatText("BLK001: Null Transaction - Unable to Add to Block", StringUtil.ANSI_RED));
            return false;
        }

        if (!transaction.verifyTransaction()) {
            System.out.println(StringUtil.formatText("BLK002: Transaction Verification Failed - Unable to Add to Block", StringUtil.ANSI_RED));
            return false;
        }

        for (TransactionInput input : transaction.getInputs()) {
            TransactionOutput UTXO = Blockchain.UTXOs.get(input.getTransactionOutputId());

            if (usedUTXOs.contains(UTXO)) {
                System.out.println(StringUtil.formatText("BLK003: Referenced UTXO Already Used - Unable to Add to Block", StringUtil.ANSI_RED));
                return false;
            }

            usedUTXOs.add(UTXO);
        }

        transactions.add(transaction);
        merkleRoot = SecurityUtil.getMerkleRoot(transactions);
        return true;
    }

    /**
     * Mines the block by finding a hash that starts with a specific number of zeroes (the difficulty) and removes the used UTXOs from the blockchain.
     * @param difficulty The difficulty level for mining, represented by the number of zeroes that must lead the hash.
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');

        hash = calculateHash();

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }

        for (Transaction transaction : transactions) {
            for (TransactionInput input : transaction.getInputs()) {
                Blockchain.UTXOs.remove(input.getTransactionOutputId());
            }

            for (TransactionOutput output : transaction.getOutputs()) {
                Blockchain.UTXOs.put(output.getId(), output);
            }
        }

        System.out.println(StringUtil.formatText("Block Mined Successfully, HASH: " + StringUtil.formatText(hash, StringUtil.ANSI_ITALIC), StringUtil.ANSI_GREEN));
    }

    /**
     * Calculates the hash of the block.
     * @return The calculated hash.
     */
    public String calculateHash() {
        StringBuilder data = new StringBuilder();

        data.append(previousBlockHash)
                .append(merkleRoot)
                .append(Long.toString(timestamp))
                .append(Integer.toString(nonce));

        return SecurityUtil.applySha256(data.toString());
    }

    /**
     * Returns the block hash.
     * @return The block hash.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns the previous block hash.
     * @return The previous block hash.
     */
    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    /**
     * Returns the Merkle root.
     * @return The Merkle root.
     */
    public String getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * Returns the timestamp.
     * @return The timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the nonce.
     * @return The nonce.
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * Returns the list of transactions.
     * @return The list of transactions.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Returns the set of used UTXOs.
     * @return The set of used UTXOs.
     */
    public Set<TransactionOutput> getUsedUTXOs() {
        return usedUTXOs;
    }

    @Override
    public String toString() {
        return "Block{" +
                "previousBlockHash='" + previousBlockHash + '\'' +
                ", merkleRoot='" + merkleRoot + '\'' +
                ", timestamp=" + timestamp +
                ", nonce=" + nonce +
                ", transactions=" + transactions +
                '}';
    }
}