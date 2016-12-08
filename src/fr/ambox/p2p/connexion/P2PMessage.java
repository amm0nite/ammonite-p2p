package fr.ambox.f2f.connexion;

import fr.ambox.f2f.peers.PeerId;

@SuppressWarnings("serial")
public abstract class P2PMessage extends Message {
	private PeerId sourcePeerId;
	
	private int ttl;
	private int hops;

	public P2PMessage(PeerId source) {
		super();
		this.sourcePeerId = source;

		this.ttl = 10;
		this.hops = 0;
	}
	
	public void hop() {
		this.ttl--;
		this.hops++;
	}

	public int getTTL() {
		return this.ttl;
	}
	
	public PeerId getSourcePeerId() {
		return this.sourcePeerId;
	}

	public int getHops() {
		return this.hops;
	}
}
