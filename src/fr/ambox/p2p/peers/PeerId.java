package fr.ambox.f2f.peers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;

import fr.ambox.f2f.utils.Hashing;


@SuppressWarnings("serial")
public class PeerId implements Serializable {
	private String nickname;
	private PublicKey publicKey;
	private String id;
	
	public PeerId(PublicKey pub) {
		this.id = Hashing.sha1_str(pub.getEncoded());
		this.nickname = "anonymous";
		this.publicKey = pub;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getId() {
		return this.id;
	}
	
	public PublicKey getPublicKey() {
		return this.publicKey;
	}
	
	public void setPublicKey(PublicKey pub) {
		this.publicKey = pub;
	}
	
	public String toString() {
		return this.id.toString()+"("+this.nickname+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PeerId)) {
			return false;
		}
		PeerId other = (PeerId) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	

	private static PeerId fromInputStream(InputStream content) throws IOException, ClassNotFoundException, BadPeerIdException {
		ObjectInputStream in = new ObjectInputStream(content);
		PeerId result = (PeerId) in.readObject();
		in.close();
		if (Hashing.sha1_str(result.publicKey.getEncoded()).equals(result.id)) {
			return result;
		}
		else {
			throw new BadPeerIdException();
		}
	}

	private byte[] toBytes() throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); 
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(this);
		out.flush();
		out.close();
		return byteOut.toByteArray();
	}
	
	public String toBase64() throws IOException {
		return Base64.encodeBase64URLSafeString(this.toBytes());
	}

	public static PeerId fromBase64(String identity) throws ClassNotFoundException, IOException, BadPeerIdException {
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.decodeBase64(identity));
		return PeerId.fromInputStream(in);
	}
}
