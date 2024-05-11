package henrycaldwell;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public String previousHash;
	private long timeStamp;
	public String hash;
	private int nonce;
	public String merkleRoot;

	public Block(String previousHash) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}

	public String calculateHash() {
		String calculatedhash = StringUtil.invokeSha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		return calculatedhash;
	}

	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = new String(new char[difficulty]).replace('\0', '0');

		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}

		System.out.println("*Block Mined* HASH: " + hash);
	}

	public boolean addTransaction(Transaction transaction) {
		if (transaction == null) return false;

		if ((previousHash != "0")) {
			if ((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("Transaction successfully added to block");
		return true;
	}
}