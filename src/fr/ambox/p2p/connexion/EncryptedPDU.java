package fr.ambox.f2f.connexion;

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
