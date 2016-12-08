package fr.ambox.f2f.connexion;

import fr.ambox.f2f.peers.Friend;
import fr.ambox.f2f.peers.PeerId;

public class ReceptionData {
	public int hops;
	public Friend emitterFriend;
	public PeerId sourcePeerId;
	public PeerId destinationPeerId;
}
