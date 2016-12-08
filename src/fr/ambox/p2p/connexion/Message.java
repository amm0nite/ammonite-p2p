package fr.ambox.f2f.connexion;

import java.io.Serializable;


@SuppressWarnings("serial")
public abstract class Message implements Serializable {
	private PDU pdu;
	
	private long id;
	
	public Message() {
		this.id = System.currentTimeMillis();
	}

	public PDU getPDU() {
		return this.pdu;
	}
	
	public void setPDU(PDU pdu) {
		this.pdu = pdu;
	}
	
	public long getId() {
		return this.id;
	}
}
