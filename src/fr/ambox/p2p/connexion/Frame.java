package fr.ambox.p2p.connexion;

import java.io.Serializable;

import fr.ambox.p2p.peers.PeerId;

@SuppressWarnings("serial")
public class Frame implements Serializable {
	private EncryptedSerializable encryptedMessage;
	private PeerId emitterPeerId;
	
	public Frame(PeerId emitter, EncryptedSerializable encryptedMessage) {
		this.encryptedMessage = encryptedMessage;
		this.emitterPeerId = emitter;
	}
	
	public PeerId getEmitterPeerId() {
		return this.emitterPeerId;
	}
	
	public EncryptedSerializable getEncryptedMessage() {
		return encryptedMessage;
	}
}
