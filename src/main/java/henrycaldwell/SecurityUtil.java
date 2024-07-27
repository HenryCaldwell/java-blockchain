package henrycaldwell;

import java.util.ArrayList;
import java.util.Base64;

import java.security.Key;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.PublicKey;
import java.security.PrivateKey;

/**
 * Utility class providing cryptographic functions and other utilities.
 */
public class SecurityUtil {

    /**
     * Applies the SHA-256 hash function to the given string and converts it to a hashed hexadecimal string.
     * @param input The input string.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates an ECDSA signature for the given input using the provided private key.
     * @param privateKey The private key used.
     * @param data The data to be signed.
     * @return The generated ECDSA signature.
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String data) {
        Signature dsa;
        byte[] output = new byte[0];

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = data.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    /**
     * Verifies an ECDSA signature using the provided public key and data.
     * @param publicKey The public key used for verification.
     * @param data The original data.
     * @param signature The ECDSA signature to be verified.
     * @return True if the signature is valid, false otherwise.
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a key to its string representation using Base64 encoding.
     * @param key The key to be converted.
     * @return The Base64 encoded string representation.
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Gets the Merkle root for a list of transactions.
     * @param transactions The list of transactions in the tree.
     * @return The Merkle root as a hashed hexidecimal.
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();

        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTransactionId());
        }

        ArrayList<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<String>();

            for (int i = 1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(SecurityUtil.applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
