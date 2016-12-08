package fr.ambox.f2f.configuration;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashMap;

import org.json.simple.JSONObject;

import fr.ambox.f2f.UserService;
import fr.ambox.f2f.connexion.PDU;
import fr.ambox.f2f.connexion.ReceptionData;
import fr.ambox.f2f.http.HttpResponse;
import fr.ambox.f2f.peers.PeerId;

public class IdentityService extends UserService {
	
	private PeerId myId;
	private PrivateKey privateKey;

	public IdentityService() {
		try {
			KeyPairGenerator keyPairGenerator;
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			
			this.myId = new PeerId(keyPair.getPublic());
			this.privateKey = keyPair.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	@Override
	public void run() {
		
	}

	public PeerId getMyId() {
		return this.myId;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject JsonIdentity() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("nickname", this.getMyId().getNickname());
		return jsonObject;
	}

	@Override
	public void handle(PDU pdu, ReceptionData receptionData) {
		
	}

	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		if (elements.length >= 1 && elements[0].equalsIgnoreCase("download")) {
			try {
				return new HttpResponse(200, this.myId.toBase64());
			} catch (IOException e) {
				e.printStackTrace();
				return HttpResponse.error(e.getMessage());
			}
		}
		else {
			return new HttpResponse(200, this.JsonIdentity().toJSONString());
		}
	}

	@Override
	public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
		IdentityService identity = (IdentityService) this.getService("identity");
		String nickname = params.get("nickname");
		if (nickname != null && !nickname.isEmpty()) {
			identity.getMyId().setNickname(nickname);
			return HttpResponse.success();
		}
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}
}
