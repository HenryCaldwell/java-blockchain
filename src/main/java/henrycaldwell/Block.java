package henrycaldwell;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a block in a blockchain with transactions, hashes, and other identifiers.
 */
public class Block {

    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); // The list of transactions in the block.
	public String hash, previousHash; // The hash and previous hash of the previous block.
	private long timeStamp;	// The time of the blocks creation in miliseconds.
	private int nonce; // The nonce value used for mining the block.
	public String merkleRoot; // The merkle root hash of all the transactions in the block.

	/**
     * Constructs a Block instance by initializing hashes and timeStamp.
	 * @param previousHash The hash of the previous block.
     */
	public Block(String previousHash) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}

	/**
     * Calculates the hash of the block by combining its identifiers and invoking the SHA-256 hashing algorithm.
     * @return The calculated hash.
     */
	public String calculateHash() {
		String calculatedhash = 
			StringUtil.applySha256(
			previousHash + 
			Long.toString(timeStamp) + 
			Integer.toString(nonce) + 
			merkleRoot);
		return calculatedhash;
	}

	/**
     * Mines the block by finding a hash that starts with a specific number of zeroes (the difficulty), creating a 'proof of work' system.
     * @param difficulty The difficulty level for mining, represented by the number of zeroes that must lead the hash.
     */
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = new String(new char[difficulty]).replace('\0', '0');

		while (!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}

		System.out.println("*Block Mined* HASH: " + hash);
	}

	/**
     * Adds a transaction to the block after verifying it.
     * @param transaction The transaction to be added to the block.
     * @return True if the transaction was added successfully, false otherwise.
     */
	public boolean addTransaction(Transaction transaction) {
		if (transaction == null) {
			return false;
		}

		if ((previousHash != "0000000000000000000000000000000000000000000000000000000000000000")) {
			if ((transaction.processTransaction() != true)) {
				return false;
			}
		}

		transactions.add(transaction);
		return true;
	}
}