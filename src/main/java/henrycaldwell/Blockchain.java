package henrycaldwell;

import java.util.ArrayList;
import java.util.HashMap;
import java.security.Security;

public class Blockchain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>(); // The list of blocks in the blockchain.
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); // The list of all unspent transaction outputs (UTXOs).

	public static int difficulty = 5; // The difficulty level for mining new blocks (Typically changed based on number of miners).
	public static double minimumTransaction = 0.1f; // The minimum transaction value.
	public static double feeRate = 0.0001f; // Fee rate in satoshis per byte
	public static Transaction genesisTransaction; // The genesis transaction, which initializes the blockchain.

	/**
     * Validates the entire blockchain, ensuring all blocks and transactions are valid.
     * @return True if the blockchain is valid, false otherwise.
     */
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		for(int i = 1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);

			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("*Current hashes not equal*");			
				return false;
			}

			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("*Previous hashes not equal*");
				return false;
			}

			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("*This block hasn't been mined*");
				return false;
			}

			TransactionOutput tempOutput;

			for (int j = 0; j < currentBlock.transactions.size(); j++) {
				Transaction currentTransaction = currentBlock.transactions.get(j);

				if (!currentTransaction.verifiySignature()) {
					System.out.println("*Signature on transaction(" + j + ") is invalid*");
					return false; 
				}

				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue() + currentTransaction.fee) {
					System.out.println("*Inputs are not equal to outputs on transaction(" + j + ")*");
					return false; 
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if (tempOutput == null) {
						System.out.println("*Referenced input on transaction(" + j + ") is missing*");
						return false;
					}

					if (input.UTXO.value != tempOutput.value) {
						System.out.println("*Referenced input transaction(" + j + ") value is invalid*");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				for (TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}

				if (currentTransaction.outputs.get(0).reciepient != currentTransaction.recipient) {
					System.out.println("*Transaction(" + j + ") output reciepient is not who it should be*");
					return false;
				}

				if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("*Transaction(" + j + ") output 'change' is not sender*");
					return false;
				}
			}
		}

		System.out.println("*Blockchain verified*");
		return true;
	}

	/**
     * Adds a new block to the blockchain after having it mined.
     * @param newBlock The new block to be added.
     */
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

	/**
     * Main method to initialize the blockchain with the genesis block.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {	
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		Wallet walletA = new Wallet();
		Wallet walletB = new Wallet();
		Wallet coinbase = new Wallet();

		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		System.out.println("Creating and mining genesis block... ");
		Block genesis = new Block("0000000000000000000000000000000000000000000000000000000000000000");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA is attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		isChainValid();
	}
}
