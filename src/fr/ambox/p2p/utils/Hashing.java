package fr.ambox.p2p.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

	public static byte[] sha1(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data);
			byte[] hash = md.digest();
			return hash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static String object_sha1_str(Serializable object) {
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			out.flush();
			out.close();
			return Hashing.bytesToHex(Hashing.sha1(byteOut.toByteArray()));
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public static String sha1_str(byte[] data) {
		return Hashing.bytesToHex(Hashing.sha1(data));
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
