package fr.ambox.p2p.connexion;

import fr.ambox.p2p.peers.Friend;
import fr.ambox.p2p.peers.PeerId;

public class ReceptionData {
	public int hops;
	public Friend emitterFriend;
	public PeerId sourcePeerId;
	public PeerId destinationPeerId;
}
