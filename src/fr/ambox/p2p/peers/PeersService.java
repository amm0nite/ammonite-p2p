package fr.ambox.p2p.peers;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.ambox.p2p.UserService;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;

public class PeersService extends UserService {
	private HashMap<String, PeerId> list;
	
	public PeersService() {
		this.list = new HashMap<String, PeerId>();
	}

	public void addPeer(PeerId peerId) {
		this.list.put(peerId.getId(), peerId);
	}
	
	public PeerId getPeer(String id) {
		return this.list.get(id);
	}
	
	@Override
	public void run() {

	}

	@Override
	public void handle(PDU pdu, ReceptionData receptionData) {

	}

	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		JsonArray jsonArray = new JsonArray();
		for (PeerId peerId : this.list.values()) {
			JsonObject jsonPeerId = new JsonObject();
			jsonPeerId.addProperty("id", peerId.getId());
			jsonPeerId.addProperty("nickname", peerId.getNickname());
			jsonArray.add(jsonPeerId);
		}
		return new HttpResponse(200, jsonArray.toString());
	}

	@Override
	public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}
}
