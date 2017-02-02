package fr.ambox.p2p.connexion;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import fr.ambox.p2p.Service;
import fr.ambox.p2p.UserService;
import fr.ambox.p2p.configuration.IdentityService;
import fr.ambox.p2p.peers.Friend;
import fr.ambox.p2p.peers.FriendshipService;
import fr.ambox.p2p.peers.PeerId;
import fr.ambox.p2p.peers.PeersService;

public class ConnexionHandler extends Service {
	private Socket requestSocket;
	private Friend sourceFriend;
	private ReceptionData receptionData;

	public ConnexionHandler(Socket requestSocket) {
		this.requestSocket = requestSocket;
		this.sourceFriend = null;
	}

	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());

			while (true) {
				Frame frame;
				try {
					frame = (Frame) in.readObject();
					if (this.recognizeFriend(frame.getEmitterPeerId(), this.requestSocket)) {
						this.log("accepted frame from "+this.sourceFriend.getPeerId());
						this.handleFrame(frame);
					}
					else {
						this.log("refused frame from "+frame.getEmitterPeerId());
					}
				} catch (ClassNotFoundException e) {
					// deserialization
					e.printStackTrace();
				}
			}
		} catch (EOFException e) {
			// end of file
			e.printStackTrace();
		} catch (IOException e) {
			// creation ObjectInputStream
			e.printStackTrace();
		}
	}

	private void handleFrame(Frame frame) {
		this.receptionData = new ReceptionData();
		this.receptionData.emitterFriend = this.sourceFriend;
		this.sourceFriend.updateNickname(frame.getEmitterPeerId().getNickname());
		PeersService peersService = (PeersService) this.getService("peers");
		peersService.addPeer(this.sourceFriend.getPeerId());

		IdentityService identity = (IdentityService) this.getService("identity");
		PeerId myId = identity.getMyId();

		if (!myId.equals(frame.getEmitterPeerId())) {
			EncryptedSerializable encMsg = frame.getEncryptedMessage();
			if (encMsg != null) {
				Message m;
				try {
					m = (Message) EncryptedSerializable.decrypt(encMsg, 
							identity.getPrivateKey(), 
							this.sourceFriend.getPeerId().getPublicKey());
					this.handleMessage(m);
				} catch (MessageEncryptionException e) { 
					//System.out.println(this.connexionServer.getRouterService().getAccess().getMyId().getNickname()+" failed decryption with frame from "+this.sourceFriend.getPeerId().getNickname()+" (id="+this.sourceFriend.getPeerId().getId()+" pub="+Hashing.sha1_str(this.sourceFriend.getPeerId().getPublicKey().getEncoded())+")");
					this.log("unable to decrypt message");
				}
			}
		}
	}

	private void handleMessage(Message m) {
		if (m instanceof DirectMessage) {
			this.handleDirectMessage((DirectMessage) m);
		}
		else if (m instanceof P2PMessage) {
			this.handleP2PMessage((P2PMessage) m);
		}
	}

	private void handleP2PMessage(P2PMessage m) {
		// if message already processed (comes from loop)
		ConnexionWatcher watcher = (ConnexionWatcher) this.getService("watcher");
		if (watcher.knowsThisP2PMessage(m)) {
			this.log("refused message : loop");
			return;
		}
			
		// processing message
		m.hop();
		this.receptionData.sourcePeerId = m.getSourcePeerId();
		this.receptionData.hops = m.getHops();
		PeersService peersService = (PeersService) this.getService("peers");
		peersService.addPeer(m.getSourcePeerId());

		IdentityService identity = (IdentityService) this.getService("identity");
		PeerId myId = identity.getMyId();
		if (!myId.equals(m.getSourcePeerId())) {
			ConnexionEmitter emitter = (ConnexionEmitter) this.getService("emitter");
			emitter.addPheromone(m.getSourcePeerId(), new Pheromone(this.sourceFriend, m.getHops()));

			if (m instanceof UnicastP2PMessage) {
				this.handleUnicastP2PMessage((UnicastP2PMessage) m);
			}
			else if (m instanceof BroadcastP2PMessage) {
				this.handleBroadcastP2PMessage((BroadcastP2PMessage) m);
			}
		}
	}

	private void handleBroadcastP2PMessage(BroadcastP2PMessage m) {
		this.handlePDU(m.getPDU());
		
		if (m.getTTL() > 0) {
			// flood to relay, without sending to emitter friend
			ConnexionEmitter emitter = (ConnexionEmitter) this.getService("emitter");
			emitter.flood(m, this.receptionData.emitterFriend.getPeerId());
		}
	}

	private void handleUnicastP2PMessage(UnicastP2PMessage m) {
		this.receptionData.destinationPeerId = m.getDestinationPeerId();
		PeersService peersService = (PeersService) this.getService("peers");
		peersService.addPeer(m.getDestinationPeerId());

		IdentityService identity = (IdentityService) this.getService("identity");
		PeerId myId = identity.getMyId();
		if (myId.equals(m.getDestinationPeerId())) {
			this.handlePDU(m.getPDU());
		}
		else {
			if (m.getTTL() > 0) {
				ConnexionEmitter emitter = (ConnexionEmitter) this.getService("emitter");
				emitter.forward(m);
			}
		}
	}

	private void handleDirectMessage(DirectMessage m) {
		this.receptionData.sourcePeerId = this.sourceFriend.getPeerId();
		this.handlePDU(m.getPDU());
	}

	private void handlePDU(PDU pdu) {
		try {
			if (pdu instanceof EncryptedPDU) {
				EncryptedPDU encPDU = (EncryptedPDU) pdu;
				EncryptedSerializable encrypted = encPDU.getEncryptedPDU();
				
				IdentityService identity = (IdentityService) this.getService("identity");
				pdu = (PDU) EncryptedSerializable.decrypt(encrypted, 
						identity.getPrivateKey(), 
						receptionData.sourcePeerId.getPublicKey());
			}
			
			Service s = this.getService(pdu.getService());
			if (s != null && s instanceof UserService) {
				UserService us = (UserService) s;
				us.handle(pdu, this.receptionData);
			}
		} catch (MessageEncryptionException e) { 
			this.log("unable to decrypt pdu"); 
		}
	}
	
	private boolean recognizeFriend(PeerId emitterPeerId, Socket clientSocket) {
		FriendshipService fs = (FriendshipService) this.getService("friendship");
		
		// use pubkey to recognize the friend
		Friend found = null;
		for (Friend f : fs.getFriends()) {
			if (f.getPeerId().getId().equals(emitterPeerId.getId()) 
					&& f.getPeerId().getPublicKey().equals(emitterPeerId.getPublicKey())) {
				found = f;
			}
		}
		if (found == null) {
			this.log(emitterPeerId.getId()+" not found in friend list");
			return false;
		}
		
		// check hostname/ip
		boolean res = false;
		String hostAddress = clientSocket.getInetAddress().getHostAddress();
		if (found.getHostAndPort().getHost().equalsIgnoreCase(hostAddress)) {
			res = true;
		}
		else {
			this.log("host check 1 failed ("+hostAddress+" != "+found.getHostAndPort().getHost()+")");
			String hostName = clientSocket.getInetAddress().getHostName();
			if (found.getHostAndPort().getHost().equalsIgnoreCase(hostName)) {
				res = true;
			}
			else {
				this.log("host check 2 failed ("+hostName+" != "+found.getHostAndPort().getHost()+")");
			}
		}
		
		if (!res) {
			this.log("friend id used by a wrong host");
			return false;
		}
		else {
			this.sourceFriend = found;
			return true;
		}
	}
}
