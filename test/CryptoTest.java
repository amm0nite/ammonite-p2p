import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoTest {

    @Test
    public void rsaEncryptionTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        String plainString = new String("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit");
        byte[] plain = (plainString).getBytes("UTF-8");

        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] encrypted = cipher.doFinal(plain);

        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decrypted = cipher.doFinal(encrypted);
        String decryptedString = new String(decrypted, "UTF-8");

        Assert.assertEquals(plainString, decryptedString);
    }

    @Test
    public void encryptedKeyTest() throws Exception {
        String symmetricAlgorithm = "AES";

        KeyGenerator keyGenerator = KeyGenerator.getInstance(symmetricAlgorithm);
        keyGenerator.init(128);
        SecretKey blowfishKey = keyGenerator.generateKey();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

        byte[] blowfishKeyBytes = blowfishKey.getEncoded();
        byte[] cipherText = cipher.doFinal(blowfishKeyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        byte[] decryptedKeyBytes = cipher.doFinal(cipherText);
        SecretKey newBlowfishKey = new SecretKeySpec(decryptedKeyBytes, symmetricAlgorithm);

        String bigString = new String("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit");

        byte[] plain = bigString.getBytes("UTF-8");
        cipher = Cipher.getInstance(symmetricAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, blowfishKey);
        byte[] encrypted = cipher.doFinal(plain);

        cipher = Cipher.getInstance(symmetricAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, newBlowfishKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        String decryptedString = new String(decrypted, "UTF-8");

        Assert.assertEquals(bigString, decryptedString);
    }

    @Test
    public void keyPairDumpTest() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        // Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        byte[] publicKeyBytes = x509EncodedKeySpec.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);

        // Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        byte[] privateKeyBytes = pkcs8EncodedKeySpec.getEncoded();
        String privateKeyString = Base64.getEncoder().encodeToString(privateKeyBytes);

        Assert.assertNotNull(publicKeyString);
        Assert.assertNotNull(privateKeyString);
    }
}