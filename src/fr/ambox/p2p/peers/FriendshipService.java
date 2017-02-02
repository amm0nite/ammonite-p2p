package fr.ambox.p2p.peers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;

import fr.ambox.p2p.UserService;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;

public class FriendshipService extends UserService {
	private ArrayList<Friend> friends;
	
	public FriendshipService() {
		this.friends = new ArrayList<Friend>();
	}

	@Override
	public void run() {
		
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray JsonFriendList() {
		JSONArray jsonArray = new JSONArray();
		for (Friend f : this.friends) {
			jsonArray.add(f.toJSON());
		}
		return jsonArray;
	}
	
	private void add(Friend f) {
		this.friends.add(f);
	}

	private void deleteFriend(HostAndPort hp) {
		Iterator<Friend> it = this.friends.iterator();
		while (it.hasNext()) {
			Friend f = it.next();
			if (f.getHostAndPort().equals(hp)) {
				it.remove();
			}
		}
	}

	public Friend[] getFriends() {
		Friend[] res = new Friend[this.friends.size()];
		return this.friends.toArray(res);
	}
	
	@Override
	public void handle(PDU pdu, ReceptionData receptionData) {
		
	}

	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		return new HttpResponse(200, this.JsonFriendList().toJSONString());
	}

	@Override
	public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
		String address = params.get("address");
		String identity = params.get("identity");
		
		if (address != null && !address.isEmpty() && identity != null && !identity.isEmpty()) {
			try {
				PeerId peerId = PeerId.fromBase64(identity);
				Friend f = new Friend(HostAndPort.fromString(address).withDefaultPort(8080), new PeerId(peerId.getPublicKey()));
				this.add(f);
				
				PeersService peers = (PeersService) this.getService("peers");
				peers.addPeer(peerId);
				
				return HttpResponse.success();
			}
			catch (ClassNotFoundException | IOException | BadPeerIdException e) {
				e.printStackTrace();
				this.log("failed to read peer id file");
				return HttpResponse.error(e.getMessage());
			}
		}
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
		String address = params.get("address");
		this.deleteFriend(HostAndPort.fromString(address).withDefaultPort(8080));
		return HttpResponse.success();
	}
}
