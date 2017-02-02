package fr.ambox.p2p.connexion;

import fr.ambox.p2p.peers.PeerId;

@SuppressWarnings("serial")
public class UnicastP2PMessage extends P2PMessage {
	private PeerId destinationPeerId;

	public UnicastP2PMessage(PeerId source, PeerId destination) {
		super(source);
		this.destinationPeerId = destination;
	}
	
	public PeerId getDestinationPeerId() {
		return this.destinationPeerId;
	}
}
