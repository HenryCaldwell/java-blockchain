package henrycaldwell;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class Blockchain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static int difficulty = 5;

	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');

		for(int i = 1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);

			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}

			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}

			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
		}

		return true;
	}

    public static void main(String[] args) {	
		blockchain.add(new Block("First Block", "0"));
		System.out.println("Mining First Block...");
		blockchain.get(0).mineBlock(difficulty);

		blockchain.add(new Block("Second Block", blockchain.get(blockchain.size()-1).hash)); 
		System.out.println("Mining Second Block...");
		blockchain.get(1).mineBlock(difficulty);

		blockchain.add(new Block("Third Block", blockchain.get(blockchain.size()-1).hash));
		System.out.println("Mining Third Block...");
		blockchain.get(2).mineBlock(difficulty);

		System.out.println("\nBlockchain Valid: " + isChainValid());
		
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);
	}
}
