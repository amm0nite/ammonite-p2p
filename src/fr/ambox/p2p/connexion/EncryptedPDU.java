package fr.ambox.p2p.connexion;

@SuppressWarnings("serial")
public class EncryptedPDU extends PDU {
	private EncryptedSerializable encryptedPDU;

	public EncryptedPDU(EncryptedSerializable encPDU) {
		this.encryptedPDU = encPDU;
	}

	public EncryptedSerializable getEncryptedPDU() {
		return encryptedPDU;
	}
}
