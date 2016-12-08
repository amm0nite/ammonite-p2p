package fr.ambox.f2f.connexion;

import java.util.HashMap;

import fr.ambox.f2f.Service;
import fr.ambox.f2f.configuration.IdentityService;
import fr.ambox.f2f.peers.Friend;
import fr.ambox.f2f.peers.FriendComException;
import fr.ambox.f2f.peers.FriendshipService;
import fr.ambox.f2f.peers.PeerId;
import fr.ambox.f2f.utils.CircularList;

public class ConnexionEmitter extends Service {
	private static long pheromoneMaxAge = 10000;
	private static int pheromoneBuffer = 16;
	
	private HashMap<PeerId, CircularList<Pheromone>> pheromonesBase;

	public ConnexionEmitter() {
		this.pheromonesBase = new HashMap<PeerId, CircularList<Pheromone>>();
	}
	
	public void run() {
		
	}
	
	public void direct(PDU pdu, Friend f) {
		DirectMessage m = new DirectMessage();
		m.setPDU(pdu);
		this.sendFrame(f, m);
	}
	
	public void broadcast(PDU pdu) {
		IdentityService identity = (IdentityService) this.getService("identity");
		
		BroadcastP2PMessage m = new BroadcastP2PMessage(identity.getMyId());
		m.setPDU(pdu);
		this.flood(m);
	}
	
	public void unicast(PDU pdu, PeerId destinationPeerId) {
		IdentityService identity = (IdentityService) this.getService("identity");
		try {
			EncryptedSerializable encPDU = EncryptedSerializable.encrypt(pdu, identity.getPrivateKey(), destinationPeerId.getPublicKey());
			UnicastP2PMessage m = new UnicastP2PMessage(identity.getMyId(), destinationPeerId);
			m.setPDU(new EncryptedPDU(encPDU));
			this.forward(m);
		} catch (MessageEncryptionException e) { this.log("failed to encrypt message"); }
	}

	protected void flood(P2PMessage p2pm) {
		this.flood(p2pm, null);
	}
	protected void flood(P2PMessage p2pm, PeerId emitter) {
		FriendshipService friendship = (FriendshipService) this.getService("friendship"); 
		Friend[] friends = friendship.getFriends();
		for (Friend f : friends) {
			// do not send to the source of the msg
			if (!p2pm.getSourcePeerId().equals(f.getPeerId())) {
				// do not send to the emitter who relayed the msg
				if (emitter == null || !emitter.equals(f.getPeerId())) {
					this.sendFrame(f, p2pm);
				}
			}
		}
	}
	
	protected void forward(UnicastP2PMessage m) {
		Friend f = this.getPheromoneHint(m.getDestinationPeerId());
		if (f != null) {
			this.sendFrame(f, m);
		}
		else {
			this.flood(m);
		}
	}
	
	private void sendFrame(Friend f, Message message) {
		IdentityService identity = (IdentityService) this.getService("identity");
		this.log(identity.getMyId()+" ==> "+f.getPeerId());
		try {
			EncryptedSerializable encMsg = EncryptedSerializable.encrypt(message, identity.getPrivateKey(), f.getPeerId().getPublicKey());
			Frame frame = new Frame(identity.getMyId(), encMsg);
			f.send(frame);
		} 
		catch (MessageEncryptionException e) { this.log("failed to encrypt message"); } 
		catch (FriendComException e) { this.log(e.getMessage()); }
	}
	
	synchronized protected void addPheromone(PeerId peer, Pheromone phero) {
		CircularList<Pheromone> pheromones = this.pheromonesBase.get(peer);
		if (pheromones == null) {
			pheromones = new CircularList<Pheromone>(ConnexionEmitter.pheromoneBuffer);
		}
		pheromones.add(phero);
		this.pheromonesBase.put(peer, pheromones);
	}
	
	synchronized private Friend getPheromoneHint(PeerId to) {
		CircularList<Pheromone> pheromones = this.pheromonesBase.get(to);
		if (pheromones == null) {
			return null;
		}
		
		Pheromone choosen = null;
		int min = Integer.MAX_VALUE;
		for (Object o : pheromones.values()) {
			Pheromone p = (Pheromone) o;
			long age = System.currentTimeMillis() - p.getTime();
			if (age < ConnexionEmitter.pheromoneMaxAge && p.getHops() < min) {
				min = p.getHops();
				choosen = p;
			}
		}
		return choosen.getFriend();
	}
}
