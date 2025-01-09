# Java Blockchain

THIS PROJECT WAS MADE WITH THE HELP OF [KASS'](https://medium.com/@cryptokass) BLOCKCHAIN DEVELOPMENT GUIDES, AND FURTHER EXPANDED UPON WITH THINGS SUCH AS TRANSACTION FEES, IMPROVED ORGANIZATION, IMPROVED UTXO HANDLING, AND MORE.

This repository contains a Java implementation of a simple blockchain, featuring multiple classes that work together to simulate the blockchain operations, including block creation, transaction handling, and blockchain validation.

## Classes Overview

### `Block`

The `Block` class represents a single block in the blockchain, containing a list of transactions, hashes, timestamps, and other relevant information.

#### Attributes
- `transactions`: List of transactions included in the block.
- `previousHash`: Hash of the previous block in the chain.
- `timeStamp`: Timestamp when the block was created.
- `hash`: Hash of the current block.
- `nonce`: Nonce value used for mining the block.
- `merkleRoot`: Merkle root hash of all transactions in the block.

#### Methods
- `calculateHash()`: Calculates the hash of the block.
- `mineBlock(int difficulty)`: Mines the block by finding a hash that meets the specified difficulty.
- `addTransaction(Transaction transaction)`: Adds a transaction to the block after processing it.

### `Blockchain`

The `Blockchain` class represents the blockchain, a distributed ledger of transactions grouped into blocks. It provides methods for validating the chain, adding blocks, and managing UTXOs.

#### Attributes
- `blockchain`: List of blocks in the blockchain.
- `UTXOs`: List of all unspent transaction outputs.
- `difficulty`: Difficulty level for mining new blocks.
- `minimumTransaction`: Minimum transaction value.
- `genesisTransaction`: Genesis transaction, which initializes the blockchain.

#### Methods
- `isChainValid()`: Validates the entire blockchain, ensuring all blocks and transactions are valid.
- `addBlock(Block newBlock)`: Adds a new block to the blockchain after mining it.
- `main(String[] args)`: Initializes wallets, creates and mines the genesis block, and processes subsequent transactions.

### `StringUtil`

The `StringUtil` class provides various cryptographic functions and other utilities.

#### Methods
- `applySha256(String input)`: Applies SHA-256 hash function to the input string and returns the resulting hash as a hexadecimal string.
- `applyECDSASig(PrivateKey privateKey, String input)`: Generates an ECDSA signature for the given input using the provided private key.
- `verifyECDSASig(PublicKey publicKey, String data, byte[] signature)`: Verifies an ECDSA signature using the provided public key and data.
- `getStringFromKey(Key key)`: Converts a cryptographic key to its string representation using Base64 encoding.
- `getMerkleRoot(ArrayList<Transaction> transactions)`: Calculates the Merkle root for a list of transactions.

### `Transaction`

The `Transaction` class represents a transaction in the blockchain, including inputs, outputs, sender, recipient, and value.

#### Attributes
- `transactionId`: Unique identifier of the transaction.
- `sender`: Public key of the sender.
- `recipient`: Public key of the recipient.
- `value`: Value of the transaction.
- `fee`: Value of the transaction fee.
- `signature`: Digital signature of the transaction.
- `inputs`: List of transaction inputs.
- `outputs`: List of transaction outputs.
- `sequence`: Counter to avoid identical hashes.

#### Methods
- `calulateTransactionFee()`: Calculates the transaction fee based on the transaction size and fee rate.
- `calulateTransactionSize()`: Calculates the size of the transactionin bytes.
- `calulateHash()`: Calculates the hash of the transaction.
- `generateSignature(PrivateKey privateKey)`: Generates the digital signature for the transaction using the sender's private key.
- `verifiySignature()`: Verifies the digital signature of the transaction.
- `processTransaction()`: Processes the transaction by verifying its signature, checking its inputs, and updating the blockchain.
- `getInputsValue()`: Calculates the total value of the transaction inputs.
- `getOutputsValue()`: Calculates the total value of the transaction outputs.

### `TransactionInput`

The `TransactionInput` class represents an input in a blockchain transaction, referencing a previous transaction output.

#### Attributes
- `transactionOutputId`: ID of the transaction output being referenced as input.
- `UTXO`: Unspent transaction output (UTXO) that this input references.

#### Methods
- `TransactionInput(String transactionOutputId)`: Constructs a TransactionInput with the specified transaction output ID.

### `TransactionOutput`

The `TransactionOutput` class represents an output in a blockchain transaction, specifying a recipient and a value.

#### Attributes
- `id`: Unique identifier of the transaction output.
- `reciepient`: Public key of the recipient.
- `value`: Value of the output.
- `parentTransactionId`: ID of the transaction this output belongs to.

#### Methods
- `TransactionOutput(PublicKey reciepient, double value, String parentTransactionId)`: Constructs a TransactionOutput with the specified recipient, value, and parent transaction ID.
- `isMine(PublicKey publicKey)`: Checks if the provided public key matches the recipient's public key.

### `Wallet`

The `Wallet` class represents a wallet in the blockchain system, managing a pair of public and private keys and tracking UTXOs owned by the user.

#### Attributes
- `publicKey`: Public key of the wallet.
- `privateKey`: Private key of the wallet.
- `ownedUTXOs`: UTXOs owned by this wallet.

#### Methods
- `generateKeys()`: Generates a new public-private key pair for the wallet using ECDSA.
- `getBalance()`: Calculates the balance of the wallet by summing the values of all owned UTXOs.
- `sendFunds(PublicKey recipient, double value)`: Creates and signs a new transaction to send funds to a recipient.

## Libraries Used

This project uses the following library:

- **Bouncy Castle**: A Java implementation of cryptographic algorithms.

### Maven Dependency

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78.1</version>
</dependency>
