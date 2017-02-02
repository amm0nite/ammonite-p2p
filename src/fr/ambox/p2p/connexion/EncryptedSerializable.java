package fr.ambox.p2p.connexion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import fr.ambox.p2p.chat.ChatPDU;
import fr.ambox.p2p.utils.Hashing;

@SuppressWarnings("serial")
public class EncryptedSerializable implements Serializable {
	private byte[] secretKey;
	private byte[] data;
	private byte[] signature;

	protected EncryptedSerializable() {
		this.secretKey = null;
		this.data = null;
		this.signature = null;
	}

	public static EncryptedSerializable encrypt(Serializable m, PrivateKey myPrivateKey, PublicKey peerPublicKey) throws MessageEncryptionException {
		try {
			EncryptedSerializable result = new EncryptedSerializable();
			Cipher cipher;

			// create secret key
			KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
			keyGenerator.init(128);
			SecretKey blowfishKey = keyGenerator.generateKey();

			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			
			// encrypt secretKey with the peer public key
			cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey);
			result.secretKey = cipher.doFinal(blowfishKey.getEncoded());
			
			// get message data and signature
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(m);
			out.flush();
			out.close();
			byte[] plainData = byteOut.toByteArray();
			byte[] plainSignature = Hashing.sha1(plainData);
			
			// encrypt signature with my private key
			cipher.init(Cipher.ENCRYPT_MODE, myPrivateKey);
			byte[] encryptedSignature = cipher.doFinal(plainSignature);

			// symetric encryption of data and signature
			cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, blowfishKey);
			result.data = cipher.doFinal(plainData);
			cipher.init(Cipher.ENCRYPT_MODE, blowfishKey);
			result.signature = cipher.doFinal(encryptedSignature);
			
			return result;
		}
		catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new MessageEncryptionException();
		}
	}

	public static Serializable decrypt(EncryptedSerializable encryptedMessage, PrivateKey myPrivateKey, PublicKey peerPublicKey) throws MessageEncryptionException {
		try{
			Cipher cipher;

			// decrypt secretKey with my private key
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, myPrivateKey);
			byte[] decryptedBlowfishKey = cipher.doFinal(encryptedMessage.secretKey);
			SecretKey blowfishKey = new SecretKeySpec(decryptedBlowfishKey, "Blowfish");

			// decrypt message data and signature
			cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, blowfishKey);
			byte[] plainData = cipher.doFinal(encryptedMessage.data);
			cipher.init(Cipher.DECRYPT_MODE, blowfishKey);
			byte[] encryptedSignature = cipher.doFinal(encryptedMessage.signature);
			
			// decrypt signature with the peer public key
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, peerPublicKey);
			byte[] signature = cipher.doFinal(encryptedSignature);
			
			// check data signature
			byte[] dataHash = Hashing.sha1(plainData);
			if (signature.length != dataHash.length || signature.length != 20) {
				throw new MessageSignatureException("signature is wrong");
			}
			for (int i=0; i<signature.length; i++) {
				if (signature[i] != dataHash[i]) {
					throw new MessageSignatureException("signature check failed");
				}
			}

			// recovering the object 
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(plainData));
			Serializable result = (Serializable) in.readObject();
			in.close();
			
			return result;
		}
		catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException | MessageSignatureException e) {
			e.printStackTrace();
			throw new MessageEncryptionException();
		}
	}
	
	public static void main(String[] args) throws MessageEncryptionException, NoSuchAlgorithmException, IOException, ClassNotFoundException {
		DirectMessage a = new DirectMessage();
		ChatPDU apdu = new ChatPDU("test");
		a.setPDU(apdu);
		
		System.out.println("Direct Message "+Hashing.object_sha1_str(a)+" "+a.hashCode());
		System.out.println("PDU "+Hashing.object_sha1_str(apdu));
		System.out.println(apdu);
		System.out.println("expected: "+apdu.getText());
		
		Message b;
		DirectMessage bd;
		ChatPDU bpdu;
		
		System.out.println("==== test 1 ====");
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(a);
		out.flush();
		out.close();
		byte[] plainData = byteOut.toByteArray();
		byte[] plainSignature = Hashing.sha1(plainData);
		
		System.out.println(plainData.length+"_"+Hashing.bytesToHex(Hashing.sha1(plainData)));
		System.out.println(Hashing.bytesToHex(plainSignature));
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(plainData));
		b = (Message) in.readObject();
		in.close();
		
		bd = (DirectMessage) b;
		bpdu = (ChatPDU) bd.getPDU();
		
		System.out.println("Direct Message "+Hashing.object_sha1_str(b)+" "+b.hashCode());
		System.out.println("PDU "+Hashing.object_sha1_str(bpdu));
		System.out.println(bpdu);
		System.out.println("result: "+bpdu.getText());
		
		System.out.println("==== test 2 ====");
		
		KeyPairGenerator keyPairGenerator1 = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator1.initialize(1024);
		KeyPair mykeyPair = keyPairGenerator1.genKeyPair();
		KeyPairGenerator keyPairGenerator2 = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator2.initialize(1024);
		KeyPair peerkeyPair = keyPairGenerator2.genKeyPair();

		EncryptedSerializable encMsg = EncryptedSerializable.encrypt(a, mykeyPair.getPrivate(), peerkeyPair.getPublic());
		b = (Message) EncryptedSerializable.decrypt(encMsg, peerkeyPair.getPrivate(), mykeyPair.getPublic());
		bd = (DirectMessage) b;
		bpdu = (ChatPDU) bd.getPDU();
		
		System.out.println("Direct Message "+Hashing.object_sha1_str(b)+" "+b.hashCode());
		System.out.println("PDU "+Hashing.object_sha1_str(bpdu));
		System.out.println(bpdu);
		System.out.println("result: "+bpdu.getText());
	}
}
