package fr.ambox.p2p.connexion;

import fr.ambox.p2p.peers.PeerId;

@SuppressWarnings("serial")
public class BroadcastP2PMessage extends P2PMessage {
	
	public BroadcastP2PMessage(PeerId source) {
		super(source);
	}
}
