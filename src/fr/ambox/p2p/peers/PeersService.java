package fr.ambox.p2p.peers;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	@SuppressWarnings("unchecked")
	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		JSONArray jsonArray = new JSONArray();
		for (PeerId peerId : this.list.values()) {
			JSONObject jsonPeerId = new JSONObject();
			jsonPeerId.put("id", peerId.getId());
			jsonPeerId.put("nickname", peerId.getNickname());
			jsonArray.add(jsonPeerId);
		}
		return new HttpResponse(200, jsonArray.toJSONString());
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
