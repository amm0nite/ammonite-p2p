package fr.ambox.f2f.connexion;

import fr.ambox.f2f.peers.PeerId;

@SuppressWarnings("serial")
public class BroadcastP2PMessage extends P2PMessage {
	
	public BroadcastP2PMessage(PeerId source) {
		super(source);
	}
}
