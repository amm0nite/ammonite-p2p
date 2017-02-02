package fr.ambox.p2p.chat;

import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.ambox.p2p.UserService;
import fr.ambox.p2p.configuration.IdentityService;
import fr.ambox.p2p.connexion.ConnexionEmitter;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;
import fr.ambox.p2p.peers.Friend;
import fr.ambox.p2p.peers.FriendshipService;
import fr.ambox.p2p.peers.PeerId;
import fr.ambox.p2p.peers.PeersService;
import fr.ambox.p2p.utils.CircularList;

public class ChatService extends UserService {
	private static int ListLength = 256;

	private CircularList<ChatLineData> publicMessages;
	private CircularList<ChatLineData> friendsMessages;
	private HashMap<PeerId, CircularList<ChatLineData>> privateMessages;

	public ChatService() {
		this.publicMessages = new CircularList<ChatLineData>(ChatService.ListLength);
		this.friendsMessages = new CircularList<ChatLineData>(ChatService.ListLength);
		this.privateMessages = new HashMap<PeerId, CircularList<ChatLineData>>();
	}

	@Override
	public void run() {

	}

	synchronized private void storeLine(ChatLineData cl) {
		if (cl.range == ChatPDURange.PUBLIC) {
			this.publicMessages.add(cl);
		}
		else if (cl.range == ChatPDURange.FRIENDS) {
			this.friendsMessages.add(cl);
		}
		else if (cl.range == ChatPDURange.PRIVATE) {
			//this.access.getLogger().log("storing in private ("+cl.local+")");
			PeerId peerId;
			if (cl.local) {
				peerId = cl.receptionData.destinationPeerId;
			}
			else {
				peerId = cl.receptionData.sourcePeerId;
			}
			CircularList<ChatLineData> list = this.privateMessages.get(peerId);
			if (list == null) {
				//this.access.getLogger().log("creating message buffer for "+peerId.getId());
				list = new CircularList<ChatLineData>(ChatService.ListLength);
				this.privateMessages.put(peerId, list);
			}
			list.add(cl);
			//this.access.getLogger().log("stored in "+peerId.getId());
		}
	}

	@SuppressWarnings("unchecked")
	synchronized private JSONArray JsonMessageList(ChatPDURange range, String id) {
		Object[] data = new Object[0];
		if (range == ChatPDURange.PUBLIC) {
			data = this.publicMessages.values();
		}
		else if (range == ChatPDURange.FRIENDS) {
			data = this.friendsMessages.values();
		}
		else if (range == ChatPDURange.PRIVATE) {
			PeersService peersService = (PeersService) this.getService("peers");
			PeerId peerId = peersService.getPeer(id);

			if (peerId != null) {
				CircularList<ChatLineData> list = this.privateMessages.get(peerId);
				if (list != null) {
					data = list.values();
				}
				else {
					this.log("no discussion with "+id);
				}
			}
		}

		JSONArray jsonArray = new JSONArray();
		for (Object o : data) {	
			ChatLineData cl = (ChatLineData) o;
			JSONObject mjson = new JSONObject();
			mjson.put("author", cl.receptionData.sourcePeerId.getNickname());
			mjson.put("text", 	cl.text);
			mjson.put("time", 	cl.time);
			mjson.put("hops", 	cl.receptionData.hops);
			jsonArray.add(mjson);
		}
		return jsonArray;
	}

	@Override
	public void handle(PDU pdu, ReceptionData ReceptionData) {
		ChatPDU chatpdu = (ChatPDU) pdu;
		ChatLineData cl = new ChatLineData();
		cl.receptionData = ReceptionData;
		cl.range = chatpdu.getRange();
		cl.text = chatpdu.getText();
		cl.time = chatpdu.getTime();
		this.storeLine(cl);
		this.log("ChatPDU received : "+cl.text);
	}

	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		String range = params.get("range");
		String id = params.get("id");
		if (range != null && !range.isEmpty()) {
			JSONArray res = new JSONArray();
			if (range.equals("private") && id != null && !id.isEmpty()) {
				res = this.JsonMessageList(ChatPDURange.PRIVATE, id);
			}
			else if (range.equals("friends")) {
				res = this.JsonMessageList(ChatPDURange.FRIENDS, null);
			}
			else if (range.equals("public")) {
				res = this.JsonMessageList(ChatPDURange.PUBLIC, null);
			}

			return new HttpResponse(200, res.toJSONString());	
		}
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
		IdentityService identity = (IdentityService) this.getService("identity");

		String text = params.get("text");
		String range = params.get("range");
		String id = params.get("id");
		
		if (text != null && !text.isEmpty() && range != null && !range.isEmpty()) {
			ChatPDU chatpdu = new ChatPDU(text);

			// prepare fake recpetion for the local chat line
			ReceptionData fakeReceptionData = new ReceptionData();
			fakeReceptionData.sourcePeerId = identity.getMyId();
			fakeReceptionData.hops = 0;

			// publish the chat pdu
			ConnexionEmitter emitter = (ConnexionEmitter) this.getService("emitter");

			if (range.equals("private") && id != null && !id.isEmpty()) {
				chatpdu.setRange(ChatPDURange.PRIVATE);

				PeersService peersService = (PeersService) this.getService("peers");
				PeerId peerId = peersService.getPeer(id);

				if (peerId != null) {
					fakeReceptionData.destinationPeerId = peerId;
					emitter.unicast(chatpdu, peerId);
				}
				else {
					this.log("unknown peer "+id);
				}
			}
			else if (range.equals("friends")) {
				chatpdu.setRange(ChatPDURange.FRIENDS);

				FriendshipService friendshipService = (FriendshipService) this.getService("friendship");
				for (Friend f : friendshipService.getFriends()) {
					emitter.direct(chatpdu, f);
				}
			}
			else if (range.equals("public")) {
				chatpdu.setRange(ChatPDURange.PUBLIC);
				emitter.broadcast(chatpdu);
			}

			// store the sent chat line
			ChatLineData cl = new ChatLineData();
			cl.receptionData = fakeReceptionData;
			cl.range = chatpdu.getRange();
			cl.text = chatpdu.getText();
			cl.time = chatpdu.getTime();
			cl.local = true;
			this.storeLine(cl);
			
			return HttpResponse.success();
		}
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}
}
