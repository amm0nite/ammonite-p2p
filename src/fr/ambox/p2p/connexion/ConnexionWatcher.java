package fr.ambox.f2f.connexion;

import java.util.HashMap;

import fr.ambox.f2f.Service;
import fr.ambox.f2f.peers.PeerId;

public class ConnexionWatcher extends Service {
	private HashMap<PeerId, Long> receivedP2PMessageIdBase;
	
	public ConnexionWatcher() {
		super();
		this.receivedP2PMessageIdBase = new HashMap<PeerId, Long>();
	}
	
	@Override
	public void run() {
		
	}

	synchronized protected boolean knowsThisP2PMessage(P2PMessage p2pm) {
		// this method discard has to recognize already received message
		Long savedId = this.receivedP2PMessageIdBase.get(p2pm.getSourcePeerId());
		this.receivedP2PMessageIdBase.put(p2pm.getSourcePeerId(), p2pm.getId());
		
		if (savedId != null) {
			return (savedId >= p2pm.getId()); 
			// TODO this enforce message order and its not necessary (should search in the hashmap instead)
		}
		else {
			return false;
		}
	}
}
