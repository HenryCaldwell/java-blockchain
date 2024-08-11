package henrycaldwell;

import java.util.ArrayList;
import java.util.HashMap;

import java.security.Security;

/**
 * Represents a blockchain consisting of blocks.
 */
public class Blockchain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>(); // The list of blocks in the blockchain.
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); // The list of all unspent transaction outputs (UTXOs).

    public static double minimumTransaction = 0.01; // The minimum transaction value.
    public static double feeRate = 0.0001; // Fee rate in satoshis per byte
    public static int difficulty = 5; // The difficulty level for mining new blocks.
    public static Transaction genesisTransaction; // The genesis transaction, which acts as the initialize total currency.
    public static Block genesisBlock; // The genesis block, which initializes the blockchain.

    /**
     * Adds a new block to the blockchain after having it mined.
     * @param newBlock The new block to be added.
     */
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    /**
     * Verifies the entire blockchain, ensuring all blocks and transactions are valid.
     * @return True if the blockchain is verified, false otherwise.
     */
    public static Boolean verifyBlockchain() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        // Loop through the blockchain to check hashes and transactions.
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Check if current block hash is correct.
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println(StringUtil.formatText("BLC001: Invalid Current Block Hash - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                return false;
            }

            // Check if previous block hash is correct.
            if (!previousBlock.getHash().equals(currentBlock.getPreviousBlockHash())) {
                System.out.println(StringUtil.formatText("BLC002: Invalid Previous Block Hash - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                return false;
            }

            // Check if hash is solved.
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println(StringUtil.formatText("BLC003: Block Not Mined - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                return false;
            }

            // Loop through block's transactions to check their validity.
            for (Transaction currentTransaction : currentBlock.getTransactions()) {
                // Check if the transaction's signature is valid.
                if (!currentTransaction.verifySignature()) {
                    System.out.println(StringUtil.formatText("BLC004: Invalid Transaction Signature in Transaction - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                    return false;
                }

                // Check if inputs' value equals the sum of the transaction's value and fee.
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue() + currentTransaction.getFee()) {
                    System.out.println(StringUtil.formatText("BLC005: Mismatched Transaction Inputs and Outputs in Transaction - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                    return false;
                }

                // Loop through transaction inputs to verify them.
                for (TransactionInput input : currentTransaction.getInputs()) {
                    TransactionOutput tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    // Check if the referenced output exists.
                    if (tempOutput == null) {
                        System.out.println(StringUtil.formatText("BLC006: Missing Referenced Output in Transaction Input - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                        return false;
                    }

                    // Check if the input's value matches the referenced output's value.
                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
                        System.out.println(StringUtil.formatText("BLC007: Invalid Value for Referenced Input in Transaction Input - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                // Loop through transaction outputs to update the temporary UTXO list.
                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                // Check if the output recipient is correct.
                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println(StringUtil.formatText("BLC008: Incorrect Output Recipient in Transaction - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                    return false;
                }

                // Check if the output 'change' is returned to the sender.
                if (currentTransaction.getOutputs().size() > 1 && currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    System.out.println(StringUtil.formatText("BLC009: Incorrect Change Output in Transaction - Blockchain Failed to Verify", StringUtil.ANSI_RED));
                    return false;
                }
            }
        }

        System.out.println(StringUtil.formatText("Blockchain Verified Successfully", StringUtil.ANSI_GREEN));
        return true;
    }

    /**
     * Main method to initialize the blockchain with the genesis block.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Wallet coinbase = new Wallet();
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();

        // Creation of genesis UTXO
        TransactionOutput genesisUTXO = new TransactionOutput(coinbase.getPublicKey(), 1000000, null);
        UTXOs.put(genesisUTXO.getId(), genesisUTXO);
        genesisTransaction = coinbase.sendFunds(walletA.getPublicKey(), 500);
        genesisTransaction.generateSignature(coinbase.getPrivateKey());

        // Successful addition of genesis block
        System.out.println("Creating and mining genesis block... ");
        genesisBlock = new Block("0".repeat(64));
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);

        // Successful transaction
        Block block1 = new Block(genesisBlock.getHash());
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletA is attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40));
        addBlock(block1);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        // Unsuccessful transaction (Exceeds funds)
        Block block2 = new Block(block1.getHash());
        System.out.println("WalletA is attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000));
        addBlock(block2);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        // Unsuccessful transaction (Duplicate UTXOs)
        Block block3 = new Block(block2.getHash());
        System.out.println("WalletB is attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20));
        System.out.println("WalletB is attempting to send funds (20) to WalletA with identical UTXOs...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20));
        addBlock(block3);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        verifyBlockchain();
    }
}
