package henrycaldwell;

import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}

    private String calulateHash() {
		sequence++;
		return StringUtil.invokeSha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value) + sequence);
	}

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;
        signature = StringUtil.invokeECDSASig(privateKey,data);		
    }

    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value)	;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
        if (verifiySignature() == false) {
			System.out.println("*Transaction Signature failed to verify*");
			return false;
		}

        for (TransactionInput input : inputs) {
			input.UTXO = Blockchain.UTXOs.get(input.transactionOutputId);
		}

        if (getInputsValue() < Blockchain.minimumTransaction) {
			System.out.println("*Transaction inputs to small: " + getInputsValue() + "*");
			return false;
		}

        float leftOver = getInputsValue() - value;
        transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId));
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId));

        for (TransactionOutput output : outputs) {
			Blockchain.UTXOs.put(output.id , output);
		}

        for (TransactionInput input : inputs) {
			if (input.UTXO == null) continue;
			Blockchain.UTXOs.remove(input.UTXO.id);
		}

        return true;
    }

    public float getInputsValue() {
		float total = 0;

		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue;
			total += i.UTXO.value;
		}

		return total;
	}

    public float getOutputsValue() {
		float total = 0;

		for(TransactionOutput output : outputs) {
			total += output.value;
		}

		return total;
	}
}
