package fr.ambox.f2f.test;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoTest {
	public static void main(String[] args) throws Exception {
		//CryptoTest.test3();
		CryptoTest.test4();
	}

	public static void test2() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.genKeyPair();

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		byte[] plain = (new String("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit")).getBytes("UTF-8");

		cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
		byte[] encrypted = cipher.doFinal(plain);

		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte[] decrypted = cipher.doFinal(encrypted);

		System.out.println("["+plain.length+"] "+new String(plain, "UTF-8"));
		System.out.println("["+encrypted.length+"] "+new String(encrypted, "UTF-8"));
		System.out.println("["+decrypted.length+"] "+new String(decrypted, "UTF-8"));
	}

	public static void test3() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
		keyGenerator.init(128);
		SecretKey blowfishKey = keyGenerator.generateKey();

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.genKeyPair();

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

		byte[] blowfishKeyBytes = blowfishKey.getEncoded();
		System.out.println(new String(blowfishKeyBytes));
		byte[] cipherText = cipher.doFinal(blowfishKeyBytes);
		System.out.println(new String(cipherText));
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

		byte[] decryptedKeyBytes = cipher.doFinal(cipherText);
		System.out.println(new String(decryptedKeyBytes));
		SecretKey newBlowfishKey = new SecretKeySpec(decryptedKeyBytes, "Blowfish");
		
		byte[] plain = (new String("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit")).getBytes("UTF-8");

		cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, blowfishKey);
		byte[] encrypted = cipher.doFinal(plain);

		cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, newBlowfishKey);
		byte[] decrypted = cipher.doFinal(encrypted);

		System.out.println("["+plain.length+"] "+new String(plain, "UTF-8"));
		System.out.println("["+encrypted.length+"] "+new String(encrypted, "UTF-8"));
		System.out.println("["+decrypted.length+"] "+new String(decrypted, "UTF-8"));
	}
	
	public static void test4() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
		System.out.println("=== public ===");
		System.out.println(new String(x509EncodedKeySpec.getEncoded()));
		 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
		System.out.println("=== private ===");
		System.out.println(new String(pkcs8EncodedKeySpec.getEncoded()));
	}

	public static String encryptBlowfish(String to_encrypt, String strkey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		SecretKeySpec key = new SecretKeySpec(strkey.getBytes("UTF-8"), "Blowfish");
		return new String(CryptoTest.encryptBlowfish(to_encrypt.getBytes("UTF-8"), key), "UTF-8");
	}
	
	public static byte[] encryptBlowfish(byte[] to_encrypt, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(to_encrypt);
	}

	public static String decryptBlowfish(String to_decrypt, String strkey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		SecretKeySpec key = new SecretKeySpec(strkey.getBytes("UTF-8"), "Blowfish");
		return new String(CryptoTest.decryptBlowfish(to_decrypt.getBytes("UTF-8"), key), "UTF-8");
	}
	
	public static byte[] decryptBlowfish(byte[] to_decrypt, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(to_decrypt);
	}
}